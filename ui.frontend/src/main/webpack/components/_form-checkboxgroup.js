(function () {
    'use strict';

    var SELECTOR = '[data-selection-mode="single"] input[type="checkbox"]';
    var syncing = false;

    // Not active while authoring.
    function isEditor() {
        return !!(window.Granite && window.Granite.author);
    }

    // Single mode: checking an item unchecks the others (radio-like), but
    // unchecking the selected item is still allowed. Delegated and in the
    // capture phase so it works for dynamically rendered fields and runs
    // before the Adaptive Forms runtime handler of the clicked checkbox,
    // which then writes the final array value to the field model.
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
    }, true);
})();
