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

**Clientlib scoping:** `test.forms.wizard` is **not** embedded in the global `test.base` clientlib. Instead it is loaded through `customheaderlibs.html` inside the wizard proxy component (`apps/test/components/forms/wizard/customheaderlibs.html`) and only when that component is rendered. This keeps stepper styles/scripts isolated to pages/forms that actually include the wizard.

All CSS rules in `wizard-custom.css` are scoped under the `.test-adaptiveform-wizard` wrapper class, which `wizard-custom.js` adds to each `.cmp-adaptiveform-wizard` element on `DOMContentLoaded`. This double-scoping means the styles cannot bleed onto other components even in edge cases where the clientlib is loaded on a non-wizard page.

Override `wizard-custom.css` to change left navigation layout, spacing, colors, and responsive behavior without touching Adobe code.

## Fragment notes
The example fragments are reusable starter structures for common sections. Copy them or reference them from the Adaptive Form Fragment component to accelerate authoring.

## HTL-rendered wizard variant

`test/components/forms/wizard-htl` is a separate proxy component; the existing `test/components/forms/wizard` remains unchanged. The adaptive-form-stepper template uses the new proxy by default, while both variants remain in the same Forms component group.

The HTL renders the Core wizard's tabs, panels, i18n step labels, and Prev/Next/Submit buttons directly. CSS counters and the active/stepped classes maintained by the Core wizard provide numbered desktop steps and the mobile tab/panel accordion; no current-step state is calculated in HTL. The component-scoped clientlib keeps the CSS isolated. Its small JavaScript forwards clicks from the per-panel buttons to Core's single navigation handlers and dispatches the configured Submit action, avoiding DOM construction or node relocation.

## Vertical stepper

### How it works
The stepper is a presentation layer on top of the Core Forms wizard runtime (`core/fd/components/form/wizard/v1/wizard`). Navigation, per-step validation and runtime events are untouched; no Java was added.

- `apps/test/components/forms/wizard/wizard.html` wraps the Core wizard markup (it includes the Core script via `data-sly-resource` with the Core `resourceType`) in a `div.form-wizard.cmp-adaptiveform-wizard--vertical-stepper`. The wrapper also carries the translated texts and the Submit settings as `data-form-wizard-*` attributes.
- `clientlib-forms-wizard/js/wizard-custom.js` adds `test-adaptiveform-wizard` and the variant class to the wizard element, then keeps the stepper in sync with a `MutationObserver`: "Step X of N" (`data-step-label`), `data-step-state` (`completed`/`active`/`upcoming`), `aria-expanded`, `aria-current="step"`, `aria-disabled`, and the CSS `order` variable (tab + panel only) used by the mobile accordion. It also moves the existing Previous/Next/Submit nodes (not clones, so Core listeners are preserved) into a `div.form-wizard__actions` appended to the active panel; Next gets `form-wizard__nav--replaced` on the last step.
- `css/wizard-custom.css` contains all styling, scoped under `.test-adaptiveform-wizard.cmp-adaptiveform-wizard--vertical-stepper`, and driven by CSS variables (`--test-wizard-primary: #0d2c7c`, `--test-wizard-success: #4caf50`, ...). No new files, so `css.txt`/`js.txt` are unchanged; the filevault filter already covers `/apps/test/clientlibs`, `/apps/test/components`, `/apps/test/i18n` (ui.apps) and `/conf/test` (ui.content).

### Enabling it
The variant is on by default for every wizard rendered with `test/components/forms/wizard` (including the `adaptive-form-stepper` template). The `stepper-policy` also exposes it as a style (`cmp-adaptiveform-wizard--vertical-stepper`), and any wizard with that class on itself or an ancestor gets the stepper. Wizards that don't use the proxy keep the classic layout.

### Desktop vs. mobile
- `>= 768px`: step cards in the left column, active panel in the right column; the button row lives inside the panel card, after the step content (outlined Previous, filled Next/Submit). Previous is hidden on step 1 (Core sets `data-cmp-visible="false"`).
- `< 768px` (`max-width: 767px`): `display: contents` on the tab container and CSS `order` interleave each tab and its panel in one column, forming an accordion: the active tab is the header, the active panel sits beneath it, upcoming tabs stay collapsed.
- Keyboard: Enter/Space activate a tab (not for upcoming ones); Core's arrow-key handling still applies, except forward arrows which are routed through "Next" so the current step is validated.

### Submit on the last step
1. **Authored**: add the Core Forms **Button** (type = submit) in the last step panel; the step policy allows it.
2. **Hardcoded**: in the wizard dialog (Basic tab) enable **Show submit button on last step** and optionally set **Submit button label**. The dialog is merged onto the Core wizard dialog through the proxy.
3. No duplicate: the hardcoded button is not shown when the last step contains a `button[type="submit"]`.

### Skipped steps
Steps hidden by a rule (`visible = false` on the step panel) are not removed from the stepper. `wizard-custom.js` keeps them, numbers them in "Step X of N", and sets `data-step-state="skipped"`. The Core wizard navigates past them, and the stepper shows them in yellow (`--test-wizard-skipped-*` variables) with `aria-disabled="true"`.

Example: a rule on a checkbox group's change event sets the panels for Step 2 and Step 3 to hidden when a given option is selected, so Next goes from Step 1 to Step 4.

### i18n
Keys "Step {0} of {1}", "Previous", "Next" and "Submit" are in `apps/test/i18n` (`fr.json`, `nl.json`).

### CFC 1.1.79 wizard markup (findings)
Source: Core Forms Components wizard v1 (HTL + `wizardview.js`); the 1.1.79 artifact is not available offline, so the upstream sources were inspected and these assumptions apply:
- Root `div.cmp-adaptiveform-wizard[data-cmp-is="adaptiveFormWizard"]` > `.cmp-adaptiveform-wizard__widget` > `.cmp-adaptiveform-wizard__tabs-container > ol.cmp-adaptiveform-wizard__tabList > li.cmp-adaptiveform-wizard__tab`.
- Panels: `.cmp-adaptiveform-wizard__wizardpanel[role=tabpanel]`; active/stepped modifiers `--active` / `--stepped` on both tab and panel.
- Navigation: `.cmp-adaptiveform-wizard__previousNav` / `__nextNav` are empty `div`s (icon-only) toggled with `data-cmp-visible`; the JS adds their text.
- Limitations/workarounds: the Core wizard has no "completed" class (derived from the index relative to the active tab); the nav elements are not real buttons (given `role="button"`); Core arrow-key behavior can skip validation, so forward arrows are redirected to Next in this variant.

### Example (matches the designs)
Create a form from the **Adaptive Form Stepper** template and add five panels titled **Activiteit**, **Werknemers**, **Mobiliteit**, **Gebouwen en goederen**, **Gegevens**. Use Radio Button (card style) for single-choice, Checkbox for multi-choice, Date Picker where needed, and a submit button in the final panel if you prefer authored submit over the hardcoded toggle.
