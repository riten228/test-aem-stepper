(function (document) {
    'use strict';

    var STEP_LABEL = 'Step {0} of {1}';
    var MOBILE_QUERY = window.matchMedia('(max-width: 767px)');

    function decorate(wizard) {
        var tabs = Array.from(wizard.querySelectorAll('.cmp-adaptiveform-wizard__tab'));
        var total = tabs.length;

        function update() {
            var activeIndex = tabs.findIndex(function (tab) {
                return tab.classList.contains('cmp-adaptiveform-wizard__tab--active');
            });
            tabs.forEach(function (tab, index) {
                tab.setAttribute('data-step-label', STEP_LABEL.replace('{0}', index + 1).replace('{1}', total));
                tab.classList.toggle('test-wizard-tab--completed', activeIndex > -1 && index < activeIndex);
                tab.classList.toggle('test-wizard-tab--upcoming', activeIndex > -1 && index > activeIndex);
            });
            Array.from(wizard.querySelectorAll('.cmp-adaptiveform-wizard__wizardpanel')).forEach(function (panel) {
                if (activeIndex > -1) {
                    panel.setAttribute('data-step-label', STEP_LABEL.replace('{0}', activeIndex + 1).replace('{1}', total));
                }
            });
        }

        var observer = new MutationObserver(update);
        tabs.forEach(function (tab) {
            observer.observe(tab, {attributes: true, attributeFilter: ['class']});
        });
        update();
    }

    document.addEventListener('DOMContentLoaded', function () {
        Array.from(document.querySelectorAll('.cmp-adaptiveform-wizard')).forEach(function (wizard) {
            wizard.classList.add('test-adaptiveform-wizard');
            decorate(wizard);
        });
    });
}(document));
