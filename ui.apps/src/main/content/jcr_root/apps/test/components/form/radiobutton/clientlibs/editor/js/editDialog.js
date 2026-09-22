(function ($) {
    'use strict';

    var EDIT_DIALOG = '.cmp-adaptiveform-radiobutton__editdialog',
        RADIOBUTTON_ASSISTPRIORITY = EDIT_DIALOG + ' .cmp-adaptiveform-radiobutton__assistprioritycustom',
        RADIOBUTTON_CUSTOMTEXT = EDIT_DIALOG + ' .cmp-adaptiveform-radiobutton__customtext',
        RADIOBUTTON_SELECTION_MODE = EDIT_DIALOG + ' .cmp-adaptiveform-radiobutton__selectionmode coral-select',
        RADIOBUTTON_SINGLE_TYPE = EDIT_DIALOG + ' .cmp-adaptiveform-radiobutton__type--single coral-select',
        RADIOBUTTON_MULTI_TYPE = EDIT_DIALOG + ' .cmp-adaptiveform-radiobutton__type--multi coral-select',
        RADIOBUTTON_SINGLE_TYPE_WRAPPER = EDIT_DIALOG + ' .cmp-adaptiveform-radiobutton__typefield--single',
        RADIOBUTTON_MULTI_TYPE_WRAPPER = EDIT_DIALOG + ' .cmp-adaptiveform-radiobutton__typefield--multi',
        RADIOBUTTON_SINGLE_DEFAULT_WRAPPER = EDIT_DIALOG + ' .cmp-adaptiveform-radiobutton__defaultvalue--single',
        RADIOBUTTON_MULTI_DEFAULT_WRAPPER = EDIT_DIALOG + ' .cmp-adaptiveform-radiobutton__defaultvalue--multi',
        RADIOBUTTON_ICON_VISIBLE = EDIT_DIALOG + ' .cmp-adaptiveform-radiobutton__icon',
        RADIOBUTTON_ICON_HIDDEN = EDIT_DIALOG + ' .cmp-adaptiveform-radiobutton__iconsHidden',
        RADIOBUTTON_DEFAULT_INPUTS = EDIT_DIALOG + ' .cmp-adaptiveform-radiobutton__defaultvalue input',
        RADIOBUTTON_ENUM = EDIT_DIALOG + ' .cmp-adaptiveform-base__enum',
        Utils = window.CQ.FormsCoreComponents.Utils.v1;

    function handleAssistPriorityChange(dialog) {
        var assistpriority = dialog.find(RADIOBUTTON_ASSISTPRIORITY);
        var customtext = dialog.find(RADIOBUTTON_CUSTOMTEXT);
        var hideAndShowElements = function () {
            if (assistpriority[0].value === 'custom') {
                customtext.show();
            } else {
                customtext.hide();
            }
        };
        hideAndShowElements();
        dialog.on('change', RADIOBUTTON_ASSISTPRIORITY, function () {
            hideAndShowElements();
        });
    }

    function setDisabledState(wrapper, disabled) {
        wrapper.find('input, button, coral-select, coral-multifield').prop('disabled', disabled);
        wrapper.find('input, button, select, textarea').prop('disabled', disabled);
    }

    function syncSelectionMode(dialog) {
        var selectionMode = dialog.find(RADIOBUTTON_SELECTION_MODE),
            singleTypeWrapper = dialog.find(RADIOBUTTON_SINGLE_TYPE_WRAPPER),
            multiTypeWrapper = dialog.find(RADIOBUTTON_MULTI_TYPE_WRAPPER),
            singleDefaultWrapper = dialog.find(RADIOBUTTON_SINGLE_DEFAULT_WRAPPER),
            multiDefaultWrapper = dialog.find(RADIOBUTTON_MULTI_DEFAULT_WRAPPER),
            singleType = dialog.find(RADIOBUTTON_SINGLE_TYPE)[0],
            multiType = dialog.find(RADIOBUTTON_MULTI_TYPE)[0],
            isMulti;

        if (!selectionMode.length) {
            return;
        }

        isMulti = selectionMode[0].value === 'multi';

        singleTypeWrapper.toggle(!isMulti);
        multiTypeWrapper.toggle(isMulti);
        singleDefaultWrapper.toggle(!isMulti);
        multiDefaultWrapper.toggle(isMulti);

        setDisabledState(singleTypeWrapper, isMulti);
        setDisabledState(multiTypeWrapper, !isMulti);
        setDisabledState(singleDefaultWrapper, isMulti);
        setDisabledState(multiDefaultWrapper, !isMulti);

        if (isMulti && singleType && multiType && singleType.value && multiType.value !== singleType.value + '[]') {
            multiType.value = singleType.value + '[]';
        }

        if (!isMulti && singleType && multiType && multiType.value) {
            singleType.value = multiType.value.replace(/\[\]$/, '');
        }
    }

    function initialiseSelectionMode(dialog) {
        syncSelectionMode(dialog);
        dialog.on('change', RADIOBUTTON_SELECTION_MODE, function () {
            syncSelectionMode(dialog);
        });
    }

    var registerDialogValidator = Utils.registerDialogDataTypeValidators(
        RADIOBUTTON_DEFAULT_INPUTS,
        RADIOBUTTON_ENUM,
        function (dialog) {
            var selectionMode = dialog.find(RADIOBUTTON_SELECTION_MODE),
                selectedValue = '',
                selector;

            if (selectionMode.length && selectionMode[0].value === 'multi') {
                selector = dialog.find(RADIOBUTTON_MULTI_TYPE);
            } else {
                selector = dialog.find(RADIOBUTTON_SINGLE_TYPE);
            }

            if (selector && selector.length > 0) {
                selectedValue = selector[0].selectedItem ? selector[0].selectedItem.value : selector[0].value;
            }

            return selectedValue.toLowerCase().replace(/\[\]$/, '');
        }
    );

    function initialiseIcons(dialog) {
        Utils.prefillMultifieldValues(dialog, RADIOBUTTON_ICON_VISIBLE, RADIOBUTTON_ICON_HIDDEN);
    }

    Utils.initializeEditDialog(EDIT_DIALOG)(handleAssistPriorityChange, registerDialogValidator, initialiseSelectionMode, initialiseIcons);
})(jQuery);
