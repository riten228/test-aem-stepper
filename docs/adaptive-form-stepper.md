# Adaptive Form Stepper Implementation Guide

## What was added
- Template: `/conf/test/settings/wcm/templates/adaptive-form-stepper`
- Wizard proxy: `/apps/test/components/forms/wizard`
- Policies:
  - `/conf/test/settings/wcm/policies/test/components/forms/container/stepper-form-container`
  - `/conf/test/settings/wcm/policies/test/components/forms/wizard/stepper-policy`
  - `/conf/test/settings/wcm/policies/test/components/forms/wizard/wizard-step-policy`
- Client library: `/apps/test/clientlibs/clientlib-forms-wizard`
- Example fragments:
  - `/content/forms/af/test/fragments/personal-information`
  - `/content/forms/af/test/fragments/address-details`

## Template structure
The editable template preconfigures this hierarchy:
1. Page (`test/components/page`)
2. Root container (`test/components/container`)
3. Adaptive form container (`core/fd/components/form/container/v1/container`)
4. Wizard proxy (`test/components/forms/wizard`)

Authors add step panels directly inside the wizard. Each step should use the Adaptive Form Panel Container so the step-level policy applies cleanly.

## Author workflow
1. Create a form under `/content/forms/af` with the **Adaptive Form Stepper** template.
2. Open the form and select the wizard.
3. Add a new **Adaptive Form Panel** for each step.
4. Inside each step, drag allowed fields such as Text Input, Email Input, Checkbox, Radio Button, Date Picker, File Attachment, nested Panel, or Fragment.
5. Reuse fragments by adding the **Adaptive Form Fragment** component and pointing it to a fragment path such as:
   - `/content/forms/af/test/fragments/personal-information`
   - `/content/forms/af/test/fragments/address-details`

## Policy intent
- **Form container policy** restricts the form shell to the project wizard proxy.
- **Wizard policy** allows step panels plus key adaptive form fields and fragments.
- **Wizard step policy** allows form fields and fragments inside each step panel.

## Styling customization
`test.forms.wizard` embeds the Core Components wizard runtime clientlib and layers project CSS/JS on top.

**Clientlib scoping:** `test.forms.wizard` is **not** embedded in the global `test.base` clientlib. Instead it is loaded through `customheaderlibs.html` inside the wizard proxy component (`apps/test/components/forms/wizard`), so the CSS and JS are only included on pages that contain the wizard component.

All CSS rules in `wizard-custom.css` are scoped under the `.test-adaptiveform-wizard` wrapper class, which `wizard-custom.js` adds to each `.cmp-adaptiveform-wizard` element on `DOMContentLoaded`. This double-scoping means the styles cannot bleed onto other components even in edge cases where the clientlib is loaded on a non-wizard page.

Override `wizard-custom.css` to change left navigation layout, spacing, colors, and responsive behavior without touching Adobe code.

## Fragment notes
The example fragments are reusable starter structures for common sections. Copy them or reference them from the Adaptive Form Fragment component to accelerate authoring.
