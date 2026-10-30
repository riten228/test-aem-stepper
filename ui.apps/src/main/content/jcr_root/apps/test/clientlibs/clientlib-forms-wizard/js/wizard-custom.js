(function (document, window) {
  'use strict';
  console.log("in wizard-nav JS file");
  var VARIANT = 'cmp-adaptiveform-wizard--vertical-stepper';
  var SELECTORS = {
    tab: '.cmp-adaptiveform-wizard__tab',
    panel: '.cmp-adaptiveform-wizard__wizardpanel',
    prev: '.cmp-adaptiveform-wizard__previousNav',
    next: '.cmp-adaptiveform-wizard__nextNav',
    widget: '.cmp-adaptiveform-wizard__widget'
  };
  var ACTIVE_TAB = 'cmp-adaptiveform-wizard__tab--active';
  var DEFAULT_TEXT = {step: 'Step {0} of {1}', previous: 'Previous', next: 'Next', submit: 'Submit'};

  function format(template, values) {
    return template.replace(/\{(\d+)\}/g, function (match, index) {
      return values[index];
    });
  }

  function isVisible(element) {
    return element.getAttribute('data-cmp-visible') !== 'false';
  }

  function getFormModel() {
    var bridge = window.guideBridge;
    return bridge && bridge.getFormModel && bridge.getFormModel();
  }

  function submitForm() {
    var run = function () {
      var model = getFormModel();
      if (!model) {
        console.warn('[form-wizard] Form model not available; cannot submit.');
        return;
      }
      // Same action the "Submit Form" rule statement dispatches.
      // The default Submit action validates the form first.
      model.dispatch(new window.FormView.Actions.Submit());
    };
    if (window.guideBridge && window.guideBridge.connect) {
      window.guideBridge.connect(run);   // runs once the form is initialized
    } else {
      run();
    }
  }

  function parseSkipRules(root) {
    return Array.prototype.slice.call(root.querySelectorAll('[data-skip-rule]')).map(function (el) {
      var parts = el.getAttribute('data-skip-rule').split('|').map(function (part) {
        return part.trim();
      });
      return {checkboxGroup: parts[0], option: parts[1], stepName: parts[2]};
    }).filter(function (rule) {
      return rule.checkboxGroup && rule.option && rule.stepName;
    });
  }

  function whenFormReady(callback) {
    if (window.guideBridge && window.guideBridge.connect) {
      window.guideBridge.connect(callback);
    } else {
      window.addEventListener('bridgeInitializeStart', function (event) {
        event.detail.guideBridge.connect(callback);
      });
    }
  }

  // Hides each configured step while its checkbox group has the configured option selected.
  function bindSkipRules(rules) {
    if (!rules.length) {
      return;
    }
    whenFormReady(function () {
      var model = getFormModel();
      if (!model) {
        console.warn('[form-wizard] Form model not available; skip rules not applied.');
        return;
      }
      rules.forEach(function (rule) {
        var field = model.getElement(rule.checkboxGroup);
        if (!field) {
          console.warn('[form-wizard] Checkbox group not found for skip rule: ' + rule.checkboxGroup);
          return;
        }
        var step = model.getElement(rule.stepName);
        if (!step) {
          console.warn('[form-wizard] Step not found for skip rule: ' + rule.stepName);
          return;
        }
        var isSelected = function (value) {
          var selected = Array.isArray(value) ? value : [value];
          return selected.indexOf(rule.option) !== -1;
        };
        var apply = function (value) {
          step.visible = !isSelected(value);
        };
        field.subscribe('value', apply);
        apply(field.value);
      });
    });
  }

  function Stepper(wizard, config) {
    this.wizard = wizard;
    this.text = {
      step: config.step || DEFAULT_TEXT.step,
      previous: config.previous || DEFAULT_TEXT.previous,
      next: config.next || DEFAULT_TEXT.next,
      submit: config.submitLabel || config.submit || DEFAULT_TEXT.submit
    };
    this.showSubmit = config.showSubmit;
    this.submitButton = null;
    this.pending = false;
    this.init();
  }

  Stepper.prototype.tabs = function () {
    return Array.prototype.slice.call(this.wizard.querySelectorAll(SELECTORS.tab));
  };

  Stepper.prototype.panelOf = function (tab) {
    var id = tab.getAttribute('aria-controls');
    return id ? document.getElementById(id) : null;
  };

  Stepper.prototype.init = function () {
    var self = this;
    var widget = this.wizard.querySelector(SELECTORS.widget);
    if (!widget) {
      return;
    }
    this.widget = widget;
    this.prev = widget.querySelector(SELECTORS.prev);
    this.next = widget.querySelector(SELECTORS.next);
    this.decorateNav(this.prev, this.text.previous);
    this.decorateNav(this.next, this.text.next);
    this.createSubmit();

    widget.addEventListener('click', function (event) {
      var tab = event.target.closest && event.target.closest(SELECTORS.tab);
      if (tab && tab.getAttribute('aria-disabled') === 'true') {
        event.preventDefault();
        event.stopImmediatePropagation();
      }
    }, true);

    widget.addEventListener('keydown', function (event) {
      self.onKeyDown(event);
    }, true);

    new MutationObserver(function () {
      self.scheduleSync();
    }).observe(this.wizard, {
      subtree: true,
      childList: true,
      attributes: true,
      attributeFilter: ['class', 'data-cmp-visible']
    });
    this.sync();
  };

  Stepper.prototype.decorateNav = function (nav, label) {
    if (!nav) {
      return;
    }
    nav.setAttribute('role', 'button');
    if (nav.getAttribute('data-form-wizard-decorated') === 'true') {
      return;
    }
    nav.setAttribute('data-form-wizard-decorated', 'true');
    if (nav.childNodes.length !== 1 || nav.firstChild.nodeType !== 3 || nav.textContent !== label) {
      while (nav.firstChild) {
        nav.removeChild(nav.firstChild);
      }
      nav.appendChild(document.createTextNode(label));
    }
  };

  Stepper.prototype.createSubmit = function () {
    if (!this.showSubmit) {
      return;
    }
    var button = document.createElement('button');
    button.type = 'button';
    button.className = 'form-wizard__submit';
    button.textContent = this.text.submit;
    button.addEventListener('click', submitForm);
    this.widget.appendChild(button);
    this.submitButton = button;
  };

  Stepper.prototype.hasAuthoredSubmit = function (panel) {
    return !!(panel && panel.querySelector('button[type="submit"], .cmp-adaptiveform-button__widget[type="submit"]'));
  };

  Stepper.prototype.scheduleSync = function () {
    var self = this;
    if (this.pending) {
      return;
    }
    this.pending = true;
    window.requestAnimationFrame(function () {
      self.pending = false;
      self.sync();
    });
  };

  Stepper.prototype.sync = function () {
    // Steps hidden by rules are kept in the list as "skipped" so the stepper shows them.
    var tabs = this.tabs();
    var total = tabs.length;
    var activeIndex = -1;
    var lastVisibleIndex = -1;
    var self = this;

    tabs.forEach(function (tab, index) {
      if (tab.classList.contains(ACTIVE_TAB)) {
        activeIndex = index;
      }
      if (isVisible(tab)) {
        lastVisibleIndex = index;
      }
    });

    tabs.forEach(function (tab, index) {
      var panel = self.panelOf(tab);
      var label = format(self.text.step, [index + 1, total]);
      var state;
      if (!isVisible(tab)) {
        state = 'skipped';
      } else if (index < activeIndex) {
        state = 'completed';
      } else {
        state = index === activeIndex ? 'active' : 'upcoming';
      }

      tab.setAttribute('data-step-label', label);
      tab.setAttribute('data-step-state', state);
      tab.style.setProperty('--test-wizard-order', index * 3); //TODO
      tab.setAttribute('aria-expanded', state === 'active' ? 'true' : 'false');
      tab.setAttribute('aria-disabled', state === 'upcoming' || state === 'skipped' ? 'true' : 'false');
      if (state === 'active') {
        tab.setAttribute('aria-current', 'step');
      } else {
        tab.removeAttribute('aria-current');
      }
      if (panel) {
        panel.setAttribute('data-step-label', label);
        panel.style.setProperty('--test-wizard-order', index * 3 + 1); //TODO
      }
    });

    var isLast = activeIndex > -1 && activeIndex === lastVisibleIndex;
    var activePanel = activeIndex > -1 ? this.panelOf(tabs[activeIndex]) : null;
    var showSubmit = !!this.submitButton && isLast && !this.hasAuthoredSubmit(activePanel);

    this.placeActions(activePanel);
    if (this.submitButton) {
      this.submitButton.hidden = !showSubmit;
    }
    if (this.next) {
      this.next.classList.toggle('form-wizard__nav--replaced', isLast);
    }
  };

  Stepper.prototype.placeActions = function (activePanel) {
    if (!activePanel) {
      return;
    }
    var bar = activePanel.querySelector(':scope > .form-wizard__actions');
    if (!bar) {
      bar = document.createElement('div');
      bar.className = 'form-wizard__actions';
      activePanel.appendChild(bar);
    } else if (bar !== activePanel.lastElementChild) {
      activePanel.appendChild(bar);
    }
    [this.prev, this.next, this.submitButton].forEach(function (nav) {
      if (nav && nav.parentNode !== bar) {
        bar.appendChild(nav);
      }
    });
  };

  Stepper.prototype.onKeyDown = function (event) {
    var tab = event.target.closest && event.target.closest(SELECTORS.tab);
    if (!tab || event.target !== tab) {
      return;
    }
    var key = event.key;
    if (key === 'Enter' || key === ' ' || key === 'Spacebar') {
      event.preventDefault();
      event.stopImmediatePropagation();
      if (tab.getAttribute('aria-disabled') !== 'true') {
        tab.click();
      }
    } else if ((key === 'ArrowDown' || key === 'ArrowRight') && tab.classList.contains(ACTIVE_TAB)) {
      // Forward navigation must go through the Core "Next" button so the current step is validated.
      event.preventDefault();
      event.stopImmediatePropagation();
      if (this.next && isVisible(this.next)) {
        this.next.click();
      }
    }
  };

  function readConfig(root) {
    var data = root.dataset || {};
    return {
      step: data.formWizardI18nStep,
      previous: data.formWizardI18nPrevious,
      next: data.formWizardI18nNext,
      submit: data.formWizardI18nSubmit,
      submitLabel: data.formWizardSubmitLabel,
      showSubmit: data.formWizardSubmit === 'true'
    };
  }

  function init() {
    Array.from(document.querySelectorAll('.cmp-adaptiveform-wizard')).forEach(function (wizard) {
      wizard.classList.add('dvv-wizard');
      var variantRoot = wizard.closest('.' + VARIANT);
      if (!variantRoot || wizard.testStepper) {
        return;
      }
      wizard.classList.add(VARIANT);
      var root = wizard.closest('[data-form-wizard-i18n-step]') || variantRoot;
      wizard.testStepper = new Stepper(wizard, readConfig(root));
      bindSkipRules(parseSkipRules(root));
    });
  }

  document.addEventListener('DOMContentLoaded', init);
}(document, window));
