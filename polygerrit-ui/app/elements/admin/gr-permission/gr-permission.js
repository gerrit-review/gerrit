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

  const MAX_AUTOCOMPLETE_RESULTS = 20;

  Polymer({
    is: 'gr-permission',

    properties: {
      name: String,
      labels: Object,

      _groupFilter: String,
      _query: {
        type: Function,
        value() {
          return this._getGroupSuggestions.bind(this);
        },
      },
      _rules: Array,
      _groupsWithRules: {
        type: Object,
        computed: '_computeGroupsWithRules(_rules)',
      },
    },

    behaviors: [
      Gerrit.BaseUrlBehavior,
      Gerrit.URLEncodingBehavior,
    ],

    ready() {
      this._rules = this._toSortedArray(this.permission.value.rules);
    },

    _computeLabel(permission) {
      if (!permission.value.label) {
        return;
      }

      const labelName = permission.value.label;
      const label = {
        name: labelName,
        values: this._computeLabelValues(this.labels[labelName].values),
      };
      return label;
    },

    _computeLabelValues(values) {
      const valuesArr = [];
      const keys = Object.keys(values).sort((a, b) => {
        return parseInt(a, 10) > parseInt(b, 10);
      });

      for (const key of keys) {
        if (!values[key]) { return; }
        // The value from the server being used to choose which itme is
        // selected is in integer form, so this must be converted.
        valuesArr.push({value: parseInt(key, 10), text: values[key]});
      }
      return valuesArr;
    },

    _computeGroupsWithRules(rules) {
      const groups = {};
      for (const rule of rules) {
        groups[rule.id] = true;
      }
      return groups;
    },

    _getGroupSuggestions() {
      return this.$.restAPI.getSuggestedGroups(
          this._groupFilter,
          MAX_AUTOCOMPLETE_RESULTS)
          .then(response => {
            const groups = [];
            for (const key in response) {
              if (!response.hasOwnProperty(key)) { continue; }
              groups.push({
                name: key,
                value: response[key],
              });
            }
            return groups.filter( group => {
              return !this._groupsWithRules[this._decodeName(group.value.id)];
            });
          });
    },

    _decodeName(name) {
      return decodeURIComponent(name);
    },

    /**
     * Handles firing an event so that the added item (with default values)
     * stored in a save-able state.
     */
    _handleAddRuleData() {
      console.log('TODO')
    },

    _handleRemoveRule(e) {
      const toDelete = Object.assign({}, this.permission.value.rules);
      // delete this.permission.value.rules[e.detail.rule];
      // this._rules = this._rules.filter(rule => {
      //   return rule.id !== e.detail.rule;
      // });

      this.fire('permission-updated', {
        id: this.permission.id,
        remove: {
          rules: toDelete,
        },
        add: {
          rules: this.permission.value.rules,
        },
      });
    },

    /**
     * Handles adding a skeleton item to the dom-repeat.
     * gr-rule-editor handles setting the default values.
     */
    _handleAddRuleItem(e) {
      this.set(['permission', 'value', 'rules', e.detail.value.id], {});

      // Purposely don't recompute _toSortedArray so that the newly added rule
      // is the last item of the array.
      this.push('_rules', {
        id: e.detail.value.id,
      });
    },

    _handleRuleUpdate(e) {
      this.permission.value.rules[e.detail.rule].action =
          e.detail.action;
      if (e.detail.max && e.detail.min) {
        this.permission.value.rules[e.detail.rule].max =
            e.detail.max;
        this.permission.value.rules[e.detail.rule].min =
            e.detail.min;
      }
      this.fire('permission-updated', {
        remove: {
          rules: this.permission.value.rules,
          id: this.permission.id,
        },
        add: {
          rules: this.permission.value.rules,
          id: this.permission.id,
        },
      });
    },

    _openParentProject() {
      page.show(this.getBaseUrl() + this.encodeURL(this._inheritsFrom.id) +
        ',access');
    },

    _toSortedArray(obj) {
      return this._toArray(obj).sort((a, b) => {
        return a.id > b.id;
      });
    },

    _toArray(obj) {
      return Object.keys(obj).map( key => {
        return {
          id: key,
          value: obj[key],
        };
      });
    },
  });
})();