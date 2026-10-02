(function () {
    'use strict';

    var SELECTOR = '[data-selection-mode="single"] input[type="checkbox"]';
    var TICK_CLASS = 'cmp-adaptiveform-checkboxgroup__option--tick';
    var syncing = false;

    // Not active while authoring.
    function isEditor() {
        return !!(window.Granite && window.Granite.author);
    }

    function getFieldModel(group) {
        var field = group.querySelector('[data-cmp-is="adaptiveFormCheckBoxGroup"]');
        var id = field && field.id;
        var bridge = window.guideBridge;
        try {
            if (id && bridge && bridge.getFormModel) {
                var form = bridge.getFormModel();
                return form && form.getElement ? form.getElement(id) : null;
            }
        } catch (e) {
            // model not available, DOM sync only
        }
        return null;
    }

    function syncDom(group, selected) {
        group.querySelectorAll('input[type="checkbox"]').forEach(function (input) {
            var on = input === selected;
            input.checked = on;
            input.setAttribute('aria-checked', on ? 'true' : 'false');
        });
    }

    // Single mode: checking an item unchecks the others (radio-like), but
    // unchecking the selected item is still allowed. Delegated and in the
    // capture phase so it works for dynamically rendered fields.
    document.addEventListener('change', function (event) {
        var target = event.target;
        if (syncing || isEditor() || !target.matches || !target.matches(SELECTOR) || !target.checked) {
            return;
        }
        var group = target.closest('[data-selection-mode="single"]');
        syncing = true;
        try {
            group.querySelectorAll('input[type="checkbox"]').forEach(function (input) {
                if (input !== target && input.checked) {
                    input.checked = false;
                    input.setAttribute('aria-checked', 'false');
                    // lets the runtime update the model
                    input.dispatchEvent(new Event('change', { bubbles: true }));
                }
            });
        } finally {
            syncing = false;
        }

        // The runtime handler of the clicked item runs after this one and may
        // re-apply the previous array value, so enforce the result afterwards.
        setTimeout(function () {
            if (!target.checked && target.getAttribute('aria-checked') !== 'true') {
                return;
            }
            var model = getFieldModel(group);
            if (model) {
                try {
                    syncing = true;
                    model.value = [target.value];
                } catch (e) {
                    // ignore, DOM sync below still applies
                } finally {
                    syncing = false;
                }
            }
            syncDom(group, target);
        }, 0);
    }, true);

    // Flags the options configured in data-tick-options (JSON array of values).
    function applyTicks(root) {
        (root || document).querySelectorAll('[data-tick-options]').forEach(function (group) {
            var values;
            try {
                values = JSON.parse(group.getAttribute('data-tick-options'));
            } catch (e) {
                return;
            }
            if (!Array.isArray(values)) {
                return;
            }
            group.querySelectorAll('input[type="checkbox"]').forEach(function (input) {
                var label = input.closest('.cmp-adaptiveform-checkboxgroup__option-label');
                if (label) {
                    var flagged = values.indexOf(input.value) !== -1;
                    if (label.classList.contains(TICK_CLASS) !== flagged) {
                        label.classList.toggle(TICK_CLASS, flagged);
                    }
                }
            });
        });
    }

    function init() {
        applyTicks();
        // re-apply after the runtime re-renders the options
        new MutationObserver(function () {
            applyTicks();
        }).observe(document.body, { childList: true, subtree: true });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
