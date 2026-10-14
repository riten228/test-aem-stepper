use(function () {
    var displayStyle = String(properties.displayStyle || 'default');
    var selectionMode = String(properties.selectionMode || 'multiple');
    var classes = [];
    var tickOptions = [];
    var raw = properties.tickOptions;

    if (raw !== null && raw !== undefined) {
        // multi-value properties arrive as arrays, a single value as a string
        var list = (typeof raw === 'string') ? [raw] : raw;
        for (var i = 0; i < list.length; i++) {
            tickOptions.push(String(list[i]));
        }
    }

    if (displayStyle === 'card') {
        classes.push('cmp-adaptiveform-checkboxgroup--card');
    } else if (displayStyle === 'card-tick') {
        classes.push('cmp-adaptiveform-checkboxgroup--card-tick');
    }

    return {
        // default settings: render the core component untouched
        needsWrapper: classes.length > 0 || selectionMode === 'single',
        classes: classes.join(' '),
        tickOptions: (displayStyle === 'card-tick' && tickOptions.length > 0) ? JSON.stringify(tickOptions) : '',
        selectionMode: selectionMode === 'single' ? 'single' : 'multiple'
    };
});
