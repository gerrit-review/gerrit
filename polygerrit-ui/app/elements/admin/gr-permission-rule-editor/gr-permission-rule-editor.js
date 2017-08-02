// Copyright (C) 2017 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
(function() {
  'use strict';

  const PRIORITY_OPTIONS = [
    'BATCH',
    'INTERACTIVE',
  ];

  const DROPDOWN_OPTIONS = [
    'ALLOW',
    'DENY',
    'BLOCK',
  ];

  Polymer({
    is: 'gr-permission-rule-editor',

    properties: {
      label: Object,
      group: String,
      permission: String,
      rule: Object,
      section: String,
      _modified: {
        type: Boolean,
        value: false,
      },
      _deleted: {
        type: Boolean,
        value: false,
      },
    },

    observers: [
      '_handleValueChange(_ruleValues.*)',
    ],

    ready() {
      this._setRules();
    },

    _computeTempGroupName(group) {
      // return group.slice(-5);
      return group;
    },

    _setRules() {
      this._ruleValues = Object.assign({}, this.rule.value);
      this._originalRuleValues = Object.assign({}, this.rule.value);
    },

    _computeRuleValue() {
      if (this.rule.value) { return this.rule; }
    },

    _setDefaultRuleValues() {
      if (this.permission === 'priority') {
        this.set('rule.value.action', PRIORITY_OPTIONS[0]);
      } else if (this.label) {
        // this.rule.value.max =
        // this.rule.value.min =
      }
      this.set('rule', {
        id: this.rule.id,
        value: {
          action: DROPDOWN_OPTIONS[0],
        },
      });
      this._setRules();
    },

    _computeOptions(permission) {
      if (permission === 'priority') {
        return PRIORITY_OPTIONS;
      }
      return DROPDOWN_OPTIONS;
    },

    _handleRemoveRule(e) {
      this._deleted = true;
      this.fire('remove-rule', {rule: this.rule.id});
    },

    _handleUndoRemove(e) {
      this._deleted = false;
      this._updateValue();
    },

    _handleUndoChange(e) {
      this._modified = false;
      this._ruleValues = Object.assign({}, this._originalRuleValues);
      this._modified = false;
      this._handleValueChange();
    },

    _handleValueChange() {
      if (!this.rule.value) {
        this._setDefaultRuleValues();
        this.fire('add-rule', {rule: this.rule.id});
      }
      if (this._ruleValues.max === this.rule.value.max &&
          this._ruleValues.min === this.rule.value.min &&
          this._ruleValues.action === this.rule.value.action) {
        return;
      }
      this._updateValue();
      this._modified = true;
    },
    _updateValue() {
      const obj = {
        action: this._ruleValues.action,
        permission: this.permission,
        section: this.section,
        rule: this.rule.id,
      };
      if (this.label) {
        obj.min = this._ruleValues.min;
        obj.max = this._ruleValues.max;
      }
      this.fire('value-changed', obj);
    },
  });
})();