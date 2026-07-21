(function (document) {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        Array.from(document.querySelectorAll('.cmp-adaptiveform-wizard')).forEach(function (wizard) {
            wizard.classList.add('test-adaptiveform-wizard');
        });
    });
}(document));
