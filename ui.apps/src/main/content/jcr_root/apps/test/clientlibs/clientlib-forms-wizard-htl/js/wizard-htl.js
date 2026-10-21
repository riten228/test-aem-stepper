(function (document, window) {
    'use strict';

    var BUTTONS = '.cmp-adaptiveform-wizard__previousNav, .cmp-adaptiveform-wizard__nextNav';

    function submitForm() {
        var bridge = window.guideBridge;
        var model = bridge && bridge.getFormModel && bridge.getFormModel();
        if (model && window.FormView && window.FormView.Actions) {
            model.dispatch(new window.FormView.Actions.Submit());
        }
    }

    function initialize(root) {
        var wizard = root.matches('.cmp-adaptiveform-wizard') ? root : root.querySelector('.cmp-adaptiveform-wizard');
        if (!wizard || wizard.testWizardHtl) {
            return;
        }
        wizard.testWizardHtl = true;

        wizard.addEventListener('click', function (event) {
            var button = event.target.closest && event.target.closest(BUTTONS);
            if (!button || !wizard.contains(button)) {
                return;
            }
            var coreButton = wizard.querySelector(button.classList.contains('cmp-adaptiveform-wizard__previousNav')
                ? '.cmp-adaptiveform-wizard__previousNav'
                : '.cmp-adaptiveform-wizard__nextNav');
            if (button !== coreButton && coreButton) {
                event.preventDefault();
                coreButton.click();
            }
        }, true);

        wizard.addEventListener('keydown', function (event) {
            var button = event.target.closest && event.target.closest(BUTTONS);
            if (!button || !wizard.contains(button) || !['Enter', ' ', 'Spacebar', 'Space'].includes(event.key)) {
                return;
            }
            event.preventDefault();
            var coreButton = wizard.querySelector(button.classList.contains('cmp-adaptiveform-wizard__previousNav')
                ? '.cmp-adaptiveform-wizard__previousNav'
                : '.cmp-adaptiveform-wizard__nextNav');
            if (button !== coreButton && coreButton) {
                coreButton.click();
            }
        }, true);

        wizard.addEventListener('click', function (event) {
            if (event.target.closest('.test-wizard-htl__submit')) {
                submitForm();
            }
        });
    }

    function init() {
        document.querySelectorAll('.test-wizard-htl.cmp-adaptiveform-wizard').forEach(initialize);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
}(document, window));
