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

  Polymer({
    is: 'gr-project-access',

    properties: {
      params: Object,

      _capabilities: Object,
      /** @type {?} */
      _inheritsFrom: Object,
      _local: Object,
      _add: {
        type: Object,
        value: () => { return {}; },
      },
      _remove: {
        type: Object,
        value: () => { return {}; },
      },
      _sections: Array,
    },

    ready() {
      const promises = [];
      promises.push(this.$.restAPI.getProjectAccessRights(this.params.project)
          .then(res => {
            this._inheritsFrom = res.inherits_from;
            this._local = res.local;
            this._sections = util.toSortedArray(this._local);
          }
      ));

      promises.push(this.$.restAPI.getCapabilities().then(res => {
        this._capabilities = res;
      }));

      promises.push(this.$.restAPI.getProject(this.params.project).then(res => {
        this._labels = res.labels;
      }));

      return Promise.all(promises);
    },

    _handleSectionUpdated(e) {
      this._remove[e.detail.id] = e.detail.remove.value;
      this._add[e.detail.id] = e.detail.add.value;
    },

    _handleAddReference() {
      this.push('_sections', {
        id: '',
        value: {permissions: {}},
      });
    },

    _handleSave() {
      const toSave = {};
      for (const section of this._sections) {
        toSave[section.id] = section.value;
      }
      this.$.restAPI.setProjectAccessRights(this.params.project, {
        add: toSave,
      });
    },
  });
})();