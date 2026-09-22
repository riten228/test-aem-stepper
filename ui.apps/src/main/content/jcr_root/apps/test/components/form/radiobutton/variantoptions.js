use(function () {
    function toArray(value) {
        var className = value && value.getClass ? String(value.getClass().getName()) : '';
        if (value === null || value === undefined) {
            return [];
        }
        if (typeof value === 'string' || className === 'java.lang.String') {
            return [String(value)];
        }
        if (typeof value.length === 'number') {
            var values = [];
            for (var i = 0; i < value.length; i++) {
                values.push(String(value[i]));
            }
            return values;
        }
        return [String(value)];
    }

    function normalizeVariant(value) {
        if (value === 'standard' || value === 'cardicon') {
            return value;
        }
        return 'default';
    }

    function isImageReference(value) {
        return /^(?:\/|https?:\/\/|data:image\/)/.test(value) || /\.(?:svg|png|jpe?g|gif|webp)$/i.test(value);
    }

    var selectionMode = properties.selectionMode === 'multi' ? 'multi' : 'single';
    var variant = normalizeVariant(String(properties.variant || 'default'));
    var icons = toArray(properties.icons || properties.icon);
    var iconIsImage = [];
    var i;

    for (i = 0; i < icons.length; i++) {
        iconIsImage.push(isImageReference(icons[i]));
    }

    return {
        selectionMode: selectionMode,
        isMulti: selectionMode === 'multi',
        variant: variant,
        isDefaultVariant: variant === 'default',
        isCustomVariant: variant !== 'default',
        variantClass: variant === 'default' ? '' : ' cmp-adaptiveform-radiobutton--' + variant,
        icons: icons,
        iconIsImage: iconIsImage
    };
});
