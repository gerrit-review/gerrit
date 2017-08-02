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
      permission: {
        type: Object,
        observer: '_sortPermission',
      },
      section: String,

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
      _deleted: {
        type: Boolean,
        value: false,
      },
    },

    _sortPermission(permission) {
      this._rules = util.toSortedArray(permission.value.rules);
    },

    _handleRemovePermission() {
      this._deleted = true;
    },

    _computeDeletedClass(deleted) {
      return deleted ? 'deleted' : '';
    },

    _handleUndoRemove() {
      this._deleted = false;
    },

    _computeLabel(permission) {
      if (!permission.value.label) { return; }

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

            // Does not return groups in which we already have rules for.
            return groups.filter( group => {
              return !this._groupsWithRules[this._decodeName(group.value.id)];
            });
          });
    },

    _decodeName(name) {
      return decodeURIComponent(name);
    },

    /**
     * Handles adding a skeleton item to the dom-repeat.
     * gr-rule-editor handles setting the default values.
     */
    _handleAddRuleItem(e) {
      this.set(['permission', 'value', 'rules', e.detail.value.id], {});

      // Purposely don't recompute sorted array so that the newly added rule
      // is the last item of the array.
      this.push('_rules', {
        id: e.detail.value.id,
      });
    },
  });
})();