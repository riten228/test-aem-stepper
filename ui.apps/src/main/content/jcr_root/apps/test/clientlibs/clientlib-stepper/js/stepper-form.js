/**
 * Stepper Form Component — Client-side JavaScript
 *
 * Responsibilities:
 *  - Manages step navigation (PREV / NEXT / SUBMIT)
 *  - Tracks completion state for each step
 *  - Allows clicking any completed or active step in the left panel to jump to it
 *  - Validates required fields before advancing to the next step
 *  - Updates ARIA attributes for accessibility
 */
(function () {
    'use strict';

    var ACTIVE_CLASS = 'step-item--active';
    var COMPLETED_CLASS = 'step-item--completed';
    var PANEL_ACTIVE_CLASS = 'step-panel--active';
    var FIELD_ERROR_CLASS = 'field-error';

    /**
     * @param {HTMLElement} wrapper  The .stepper-wrapper root element.
     */
    function StepperForm(wrapper) {
        this.wrapper = wrapper;
        this.stepItems = Array.prototype.slice.call(
            wrapper.querySelectorAll('.step-item')
        );
        this.stepPanels = Array.prototype.slice.call(
            wrapper.querySelectorAll('.step-panel')
        );
        this.prevBtn = wrapper.querySelector('#btn-prev');
        this.nextBtn = wrapper.querySelector('#btn-next');
        this.submitBtn = wrapper.querySelector('#btn-submit');
        this.totalSteps = this.stepItems.length;
        this.currentIndex = 0;

        this._bindEvents();
        this._syncUI(0);
    }

    /* ── Event Binding ─────────────────────────────────── */

    StepperForm.prototype._bindEvents = function () {
        var self = this;

        // Left-panel step clicks
        this.stepItems.forEach(function (item) {
            item.addEventListener('click', function () {
                var idx = parseInt(item.getAttribute('data-step-index'), 10);
                self._handleStepClick(idx);
            });
            item.addEventListener('keydown', function (e) {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    var idx = parseInt(item.getAttribute('data-step-index'), 10);
                    self._handleStepClick(idx);
                }
            });
        });

        // PREV button
        if (this.prevBtn) {
            this.prevBtn.addEventListener('click', function () {
                if (self.currentIndex > 0) {
                    self._goToStep(self.currentIndex - 1, false);
                }
            });
        }

        // NEXT button
        if (this.nextBtn) {
            this.nextBtn.addEventListener('click', function () {
                if (!self._validateCurrentStep()) {
                    return;
                }
                if (self.currentIndex < self.totalSteps - 1) {
                    self._markCompleted(self.currentIndex);
                    self._goToStep(self.currentIndex + 1, false);
                }
            });
        }

        // SUBMIT button
        if (this.submitBtn) {
            this.submitBtn.addEventListener('click', function (e) {
                if (!self._validateCurrentStep()) {
                    e.preventDefault();
                    return;
                }
                self._markCompleted(self.currentIndex);
                // Allow the native form submit to proceed.
                // Custom submit logic (e.g. fetch) can replace this handler.
            });
        }
    };

    /* ── Navigation ────────────────────────────────────── */

    /**
     * Handle a click on a left-panel step item.
     * Users may jump to any completed step or move backward freely.
     * Jumping forward past the current step requires the current step to be valid.
     *
     * @param {number} targetIndex
     */
    StepperForm.prototype._handleStepClick = function (targetIndex) {
        if (targetIndex === this.currentIndex) {
            return;
        }

        var targetItem = this.stepItems[targetIndex];
        var isCompleted = targetItem.classList.contains(COMPLETED_CLASS);
        var isGoingBack = targetIndex < this.currentIndex;

        // Allow going backward always; forward only to completed steps or
        // the immediately next step (after validating the current one).
        if (!isGoingBack && !isCompleted) {
            if (targetIndex === this.currentIndex + 1) {
                if (!this._validateCurrentStep()) {
                    return;
                }
                this._markCompleted(this.currentIndex);
            } else {
                // Cannot skip ahead to non-completed steps.
                return;
            }
        }

        this._goToStep(targetIndex, false);
    };

    /**
     * Activate the step at {@code index}.
     *
     * @param {number}  index
     * @param {boolean} [skipValidation=false]
     */
    StepperForm.prototype._goToStep = function (index) {
        if (index < 0 || index >= this.totalSteps) {
            return;
        }
        this.currentIndex = index;
        this._syncUI(index);
    };

    /* ── State Management ──────────────────────────────── */

    /**
     * Mark a step as completed in the left panel.
     * @param {number} index
     */
    StepperForm.prototype._markCompleted = function (index) {
        var item = this.stepItems[index];
        if (item) {
            item.classList.add(COMPLETED_CLASS);
        }
    };

    /* ── Validation ────────────────────────────────────── */

    /**
     * Validates all required fields in the currently active step panel.
     * Marks invalid fields with the error class.
     *
     * @returns {boolean} true when all required fields have values.
     */
    StepperForm.prototype._validateCurrentStep = function () {
        var panel = this.stepPanels[this.currentIndex];
        if (!panel) {
            return true;
        }

        var isValid = true;
        var requiredFields = Array.prototype.slice.call(panel.querySelectorAll('[required]'));

        requiredFields.forEach(function (field) {
            // Clear previous error state.
            field.classList.remove(FIELD_ERROR_CLASS);
            var errorMsg = field.parentNode.querySelector('.field-error-message');
            if (errorMsg) {
                errorMsg.parentNode.removeChild(errorMsg);
            }

            var isEmpty = false;
            if (field.type === 'checkbox' || field.type === 'radio') {
                isEmpty = !field.checked;
            } else {
                isEmpty = !field.value || field.value.trim() === '';
            }

            if (isEmpty) {
                field.classList.add(FIELD_ERROR_CLASS);
                var msg = document.createElement('span');
                msg.className = 'field-error-message';
                msg.textContent = 'This field is required.';
                field.parentNode.appendChild(msg);
                isValid = false;
            }
        });

        if (!isValid && requiredFields.length > 0) {
            // Scroll first invalid field into view.
            var firstError = panel.querySelector('.' + FIELD_ERROR_CLASS);
            if (firstError) {
                firstError.focus();
            }
        }

        return isValid;
    };

    /* ── UI Synchronisation ────────────────────────────── */

    /**
     * Update all visual state to reflect {@code activeIndex}.
     * @param {number} activeIndex
     */
    StepperForm.prototype._syncUI = function (activeIndex) {
        var self = this;

        // Left panel step items
        this.stepItems.forEach(function (item, idx) {
            var isActive = idx === activeIndex;
            var isCompleted = item.classList.contains(COMPLETED_CLASS);
            var isPending = !isActive && !isCompleted;

            item.classList.toggle(ACTIVE_CLASS, isActive);

            // All items stay in tab order (tabindex=0); aria-disabled signals
            // pending state to assistive technology.
            item.setAttribute('aria-disabled', isPending ? 'true' : 'false');
            item.setAttribute('aria-current', isActive ? 'step' : 'false');
        });

        // Right panel step panels
        this.stepPanels.forEach(function (panel, idx) {
            var isActive = idx === activeIndex;
            panel.classList.toggle(PANEL_ACTIVE_CLASS, isActive);
            panel.setAttribute('aria-hidden', isActive ? 'false' : 'true');
        });

        // Navigation buttons
        var isFirst = activeIndex === 0;
        var isLast = activeIndex === self.totalSteps - 1;

        if (this.prevBtn) {
            this.prevBtn.disabled = isFirst;
        }
        if (this.nextBtn) {
            this.nextBtn.style.display = isLast ? 'none' : '';
        }
        if (this.submitBtn) {
            this.submitBtn.style.display = isLast ? '' : 'none';
        }
    };

    /* ── Initialisation ────────────────────────────────── */

    function init() {
        var wrappers = Array.prototype.slice.call(document.querySelectorAll('[data-cmp-is="stepper-form"]'));
        wrappers.forEach(function (wrapper) {
            new StepperForm(wrapper);
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

}());
