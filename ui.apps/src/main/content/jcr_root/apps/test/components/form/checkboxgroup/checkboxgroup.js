use(function () {
    var displayStyle = String(properties.displayStyle || 'default');
    var selectionMode = String(properties.selectionMode || 'multiple');
    var classes = [];

    if (displayStyle === 'card') {
        classes.push('cmp-adaptiveform-checkboxgroup--card');
    } else if (displayStyle === 'card-tick') {
        classes.push('cmp-adaptiveform-checkboxgroup--card-tick');
    }

    return {
        // default settings: render the core component untouched
        needsWrapper: classes.length > 0 || selectionMode === 'single',
        classes: classes.join(' '),
        selectionMode: selectionMode === 'single' ? 'single' : 'multiple'
    };
});
