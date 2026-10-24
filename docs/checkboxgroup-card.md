# Checkbox Group: card display and selection mode

`test/components/form/checkboxgroup` is a proxy of
`core/fd/components/form/checkboxgroup/v1/checkboxgroup`. Dialog options (Basic tab):

| Option | Property | Values |
| --- | --- | --- |
| Display style | `displayStyle` | `default` (core UI), `card`, `card-tick` |
| Selection mode | `selectionMode` | `multiple` (default), `single` |
| Options showing tick mark | `tickOptions` | multifield of option values (the `enum` values) |

With defaults the core markup is rendered untouched. Otherwise the core output is wrapped in a
`div.cmp-adaptiveform-checkboxgroup-wrapper` carrying `cmp-adaptiveform-checkboxgroup--card` /
`--card-tick` and `data-selection-mode`. The field value stays an array.

`single` is radio-like: selecting an item unchecks the others, but clicking the selected item
deselects it. Implemented in `ui.frontend/.../components/_form-checkboxgroup.js`; styles in `_form-checkboxgroup.scss`.

`tickOptions` only applies to `card-tick`. When set, the wrapper gets `data-tick-options` (JSON array)
and the frontend adds `cmp-adaptiveform-checkboxgroup__option--tick` to the matching option labels
(re-applied via MutationObserver after runtime re-renders); only those show the tick. When empty,
all options show the tick. `card` never shows ticks.

In `single` mode the frontend also sets the field model value (via `guideBridge`, when available)
and the DOM checked/`aria-checked` states after the runtime handler, so only one item stays selected.

Usage: add the component from the "Test Sites Project - Forms" group (allowed in the forms
policies) and choose the options in its dialog.
