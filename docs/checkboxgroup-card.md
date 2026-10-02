# Checkbox Group: card display and selection mode

`test/components/form/checkboxgroup` is a proxy of
`core/fd/components/form/checkboxgroup/v1/checkboxgroup`. Dialog options (Basic tab):

| Option | Property | Values |
| --- | --- | --- |
| Display style | `displayStyle` | `default` (core UI), `card`, `card-tick` |
| Selection mode | `selectionMode` | `multiple` (default), `single` |

With defaults the core markup is rendered untouched. Otherwise the core output is wrapped in a
`div.cmp-adaptiveform-checkboxgroup-wrapper` carrying `cmp-adaptiveform-checkboxgroup--card` /
`--card-tick` and `data-selection-mode`. The field value stays an array.

`single` is radio-like: selecting an item unchecks the others, but clicking the selected item
deselects it. Implemented in `ui.frontend/.../components/_form-checkboxgroup.js`; styles in `_form-checkboxgroup.scss`.

Usage: add the component from the "Test Sites Project - Forms" group (allowed in the forms
policies) and choose the options in its dialog.
