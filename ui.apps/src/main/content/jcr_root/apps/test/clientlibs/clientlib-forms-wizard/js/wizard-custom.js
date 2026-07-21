(function (document) {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        Array.prototype.forEach.call(document.querySelectorAll('.cmp-adaptiveform-wizard'), function (wizard) {
            wizard.classList.add('test-adaptiveform-wizard');
        });
    });
}(document));
