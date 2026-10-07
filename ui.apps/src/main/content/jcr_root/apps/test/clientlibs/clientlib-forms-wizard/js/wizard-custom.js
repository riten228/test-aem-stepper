(function (document, window) {
    'use strict';

    var VARIANT = 'cmp-adaptiveform-wizard--vertical-stepper';
    var SELECTORS = {
        tab: '.cmp-adaptiveform-wizard__tab',
        panel: '.cmp-adaptiveform-wizard__wizardpanel',
        prev: '.cmp-adaptiveform-wizard__previousNav',
        next: '.cmp-adaptiveform-wizard__nextNav',
        widget: '.cmp-adaptiveform-wizard__widget'
    };
    var ACTIVE_TAB = 'cmp-adaptiveform-wizard__tab--active';
    var DEFAULT_TEXT = {step: 'Step {0} of {1}', previous: 'Previous', next: 'Next', submit: 'Submit'};

    function format(template, values) {
        return template.replace(/\{(\d+)\}/g, function (match, index) {
            return values[index];
        });
    }

    function isVisible(element) {
        return element.getAttribute('data-cmp-visible') !== 'false';
    }

    function submitForm() {
        var bridge = window.guideBridge;
        var model = bridge && bridge.getFormModel && bridge.getFormModel();
        if (model && window.FormView && window.FormView.Actions) {
            model.dispatch(new window.FormView.Actions.Submit());
        }
    }

    function Stepper(wizard, config) {
        this.wizard = wizard;
        this.text = {
            step: config.step || DEFAULT_TEXT.step,
            previous: config.previous || DEFAULT_TEXT.previous,
            next: config.next || DEFAULT_TEXT.next,
            submit: config.submitLabel || config.submit || DEFAULT_TEXT.submit
        };
        this.showSubmit = config.showSubmit;
        this.submitButton = null;
        this.pending = false;
        this.init();
    }

    Stepper.prototype.tabs = function () {
        return Array.prototype.slice.call(this.wizard.querySelectorAll(SELECTORS.tab));
    };

    Stepper.prototype.panelOf = function (tab) {
        var id = tab.getAttribute('aria-controls');
        return id ? document.getElementById(id) : null;
    };

    Stepper.prototype.init = function () {
        var self = this;
        var widget = this.wizard.querySelector(SELECTORS.widget);
        if (!widget) {
            return;
        }
        this.widget = widget;
        this.prev = widget.querySelector(SELECTORS.prev);
        this.next = widget.querySelector(SELECTORS.next);
        this.decorateNav(this.prev, this.text.previous);
        this.decorateNav(this.next, this.text.next);
        this.createSubmit();

        widget.addEventListener('click', function (event) {
            var tab = event.target.closest && event.target.closest(SELECTORS.tab);
            if (tab && tab.getAttribute('aria-disabled') === 'true') {
                event.preventDefault();
                event.stopImmediatePropagation();
            }
        }, true);

        widget.addEventListener('keydown', function (event) {
            self.onKeyDown(event);
        }, true);

        new MutationObserver(function () {
            self.scheduleSync();
        }).observe(this.wizard, {
            subtree: true,
            childList: true,
            attributes: true,
            attributeFilter: ['class', 'data-cmp-visible']
        });
        this.sync();
    };

    Stepper.prototype.decorateNav = function (nav, label) {
        if (!nav) {
            return;
        }
        nav.setAttribute('role', 'button');
        nav.textContent = label;
    };

    Stepper.prototype.createSubmit = function () {
        if (!this.showSubmit) {
            return;
        }
        var button = document.createElement('button');
        button.type = 'button';
        button.className = 'test-wizard__submit';
        button.textContent = this.text.submit;
        button.addEventListener('click', submitForm);
        this.widget.appendChild(button);
        this.submitButton = button;
    };

    Stepper.prototype.hasAuthoredSubmit = function (panel) {
        return !!(panel && panel.querySelector('button[type="submit"], .cmp-adaptiveform-button__widget[type="submit"]'));
    };

    Stepper.prototype.scheduleSync = function () {
        var self = this;
        if (this.pending) {
            return;
        }
        this.pending = true;
        window.requestAnimationFrame(function () {
            self.pending = false;
            self.sync();
        });
    };

    Stepper.prototype.sync = function () {
        var visibleTabs = this.tabs().filter(isVisible);
        var total = visibleTabs.length;
        var activeIndex = -1;
        var self = this;

        visibleTabs.forEach(function (tab, index) {
            if (tab.classList.contains(ACTIVE_TAB)) {
                activeIndex = index;
            }
        });

        visibleTabs.forEach(function (tab, index) {
            var panel = self.panelOf(tab);
            var label = format(self.text.step, [index + 1, total]);
            var state = index < activeIndex ? 'completed' : (index === activeIndex ? 'active' : 'upcoming');

            tab.setAttribute('data-step-label', label);
            tab.setAttribute('data-step-state', state);
            tab.style.setProperty('--test-wizard-order', index * 3);
            tab.setAttribute('aria-expanded', state === 'active' ? 'true' : 'false');
            tab.setAttribute('aria-disabled', state === 'upcoming' ? 'true' : 'false');
            if (state === 'active') {
                tab.setAttribute('aria-current', 'step');
            } else {
                tab.removeAttribute('aria-current');
            }
            if (panel) {
                panel.setAttribute('data-step-label', label);
                panel.style.setProperty('--test-wizard-order', index * 3 + 1);
            }
        });

        var order = Math.max(activeIndex, 0) * 3 + 2;
        var isLast = activeIndex > -1 && activeIndex === total - 1;
        var activePanel = activeIndex > -1 ? this.panelOf(visibleTabs[activeIndex]) : null;
        var showSubmit = !!this.submitButton && isLast && !this.hasAuthoredSubmit(activePanel);

        [this.prev, this.next, this.submitButton].forEach(function (nav) {
            if (nav) {
                nav.style.setProperty('--test-wizard-order', order);
            }
        });
        if (this.submitButton) {
            this.submitButton.hidden = !showSubmit;
        }
    };

    Stepper.prototype.onKeyDown = function (event) {
        var tab = event.target.closest && event.target.closest(SELECTORS.tab);
        if (!tab || event.target !== tab) {
            return;
        }
        var key = event.key;
        if (key === 'Enter' || key === ' ' || key === 'Spacebar') {
            event.preventDefault();
            event.stopImmediatePropagation();
            if (tab.getAttribute('aria-disabled') !== 'true') {
                tab.click();
            }
        } else if ((key === 'ArrowDown' || key === 'ArrowRight') && tab.classList.contains(ACTIVE_TAB)) {
            // Forward navigation must go through the Core "Next" button so the current step is validated.
            event.preventDefault();
            event.stopImmediatePropagation();
            if (this.next && isVisible(this.next)) {
                this.next.click();
            }
        }
    };

    function readConfig(root) {
        var data = root.dataset || {};
        return {
            step: data.testWizardI18nStep,
            previous: data.testWizardI18nPrevious,
            next: data.testWizardI18nNext,
            submit: data.testWizardI18nSubmit,
            submitLabel: data.testWizardSubmitLabel,
            showSubmit: data.testWizardSubmit === 'true'
        };
    }

    function init() {
        Array.from(document.querySelectorAll('.cmp-adaptiveform-wizard')).forEach(function (wizard) {
            wizard.classList.add('test-adaptiveform-wizard');
            var variantRoot = wizard.closest('.' + VARIANT);
            if (!variantRoot || wizard.testStepper) {
                return;
            }
            wizard.classList.add(VARIANT);
            var root = wizard.closest('[data-test-wizard-i18n-step]') || variantRoot;
            wizard.testStepper = new Stepper(wizard, readConfig(root));
        });
    }

    document.addEventListener('DOMContentLoaded', init);
}(document, window));
