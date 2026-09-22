# Adaptive Form radiobutton variants

## Component path
- Proxy component: `/apps/test/components/form/radiobutton`
- Resource super type: `core/fd/components/form/radiobutton/v1/radiobutton`

## Authoring
In the component dialog, use:
- **Selection mode** to switch between **Single choice** (radio button behavior) and **Multiple choice** (checkbox group behavior).
- **Rendering variant** to choose **Default**, **Standard**, or **Card with Icon**.
- **Options** to define each value/text pair, and optionally set an **Icon** per option for the **Card with Icon** variant.

Icons accept either:
- an asset/path reference (rendered as an image), or
- a CSS class name (rendered on a span for font-icon usage).

## Implementation notes
- The proxy keeps the OOTB v1 radio button model/data contract for single-select rendering.
- When **Multiple choice** is selected, the same proxy renders against the OOTB `checkboxgroup` Sling Model/runtime contract so submission and client-side behavior follow the Core Components checkbox-group pattern.
- Only the markup/CSS presentation changes per variant; the underlying field name, values, validation hooks, and adaptive-form runtime attributes stay aligned with the Core Components implementation.
