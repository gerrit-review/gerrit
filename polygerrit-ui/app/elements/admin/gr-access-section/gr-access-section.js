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

  const GLOBAL_NAME = 'GLOBAL_CAPABILITIES';

  // The name that gets automatically input when a new reference is added.
  const NEW_NAME = 'refs/heads/*';
  const REFS_NAME = 'refs/';
  const ON_BEHALF_OF = '(On Behalf Of)';
  const LABEL = 'Label';

  const PERMISSIONS = {
    abandon: {
      id: 'abandon',
      name: 'Abandon',
    },
    addPatchSet: {
      id: 'addPatchSet',
      name: 'Add Patch Set',
    },
    create: {
      id: 'create',
      name: 'Create Reference',
    },
    createTag: {
      id: 'createTag',
      name: 'Create Annotated Tag',
    },
    createSignedTag: {
      id: 'createSignedTag',
      name: 'Create Signed Tag',
    },
    delete: {
      id: 'delete',
      name: 'Delete Reference',
    },
    deleteDrafts: {
      id: 'deleteDrafts',
      name: 'Delete Drafts',
    },
    deleteOwnChanges: {
      id: 'deleteOwnChanges',
      name: 'Delete Own Changes',
    },
    editAssignee: {
      id: 'editAssignee',
      name: 'Edit Assignee',
    },
    editHashtags: {
      id: 'editHashtags',
      name: 'Edit Hashtags',
    },
    editTopicName: {
      id: 'editTopicName',
      name: 'Edit Topic Name',
    },
    forgeAuthor: {
      id: 'forgeAuthor',
      name: 'Forge Author Identity',
    },
    forgeCommitter: {
      id: 'forgeCommitter',
      name: 'Forge Committer Identity',
    },
    forgeServerAsCommitter: {
      id: 'forgeServerAsCommitter',
      name: 'Forge Server Identity',
    },
    owner: {
      id: 'owner',
      name: 'Owner',
    },
    publishDrafts: {
      id: 'publishDrafts',
      name: 'Publish Drafts',
    },
    push: {
      id: 'push',
      name: 'Push',
    },
    pushMerge: {
      id: 'pushMerge',
      name: 'Push Merge Commit',
    },
    read: {
      id: 'read',
      name: 'Read',
    },
    rebase: {
      id: 'rebase',
      name: 'Rebase',
    },
    removeReviewer: {
      id: 'removeReviewer',
      name: 'Remove Reviewer',
    },
    submit: {
      id: 'submit',
      name: 'Submit',
    },
    submitAs: {
      id: 'submitAs',
      name: 'Submit (On Behalf Of)',
    },
    viewDrafts: {
      id: 'viewDrafts',
      name: 'View Drafts',
    },
    viewPrivateChanges: {
      id: 'viewPrivateChanges',
      name: 'View Private Changes',
    },
  };

  Polymer({
    is: 'gr-access-section',

    properties: {
      capabilities: Object,
      section: Object,
      labels: Object,
      _editing: {
        type: Boolean,
        value: false,
      },
      _permissions: Array,
    },

    ready() {
      this._permissions = util.toSortedArray(this.section.value.permissions);
    },

    _computeGroupPath(groupId) {
      return this.getBaseUrl() + '/admin/groups/' +
          this.encodeURL(groupId, true);
    },

    _computePermissions(name, capabilities) {
      // TODO Make sure updated once we can add a section
      let allPermissions;
      if (name === GLOBAL_NAME) {
        allPermissions = util.toSortedArray(capabilities);
      } else {
        const labelOptions = this._computeLabelOptions();
        allPermissions = labelOptions.concat(util.toSortedArray(PERMISSIONS));
      }
      return allPermissions.filter( permission => {
        return !this.section.value.permissions[permission.id];
      });
    },

    _computeLabelOptions() {
      const labelOptions = [];
      for (const labelName of Object.keys(this.labels)) {
        labelOptions.push({
          id: 'label-' + labelName,
          value: {
            name: `${LABEL} ${labelName}`,
            id: 'label-' + labelName,
          },
        });
        labelOptions.push({
          id: 'labelAs-' + labelName,
          value: {
            name: `${LABEL} ${labelName} ${ON_BEHALF_OF}`,
            id: 'labelAs-' + labelName,
          },
        });
      }
      return labelOptions;
    },

    _computePermissionName(name, permission) {
      if (name === GLOBAL_NAME) {
        return this.capabilities[permission.id].name;
      } else if (PERMISSIONS[permission.id]) {
        return PERMISSIONS[permission.id].name;
      } else if (permission.value.label) {
        let behalfOf = '';
        if (permission.id.startsWith('labelAs-')) {
          behalfOf = ON_BEHALF_OF;
        }
        return `${LABEL} ${permission.value.label}${behalfOf}`;
      }
    },

    _computeSectionName(name) {
      // When a new section is created, it doesn't yet have a ref. Set into
      // edit mode so that the user can input one.
      if (!name) {
        this._editing = true;
        // Needed for the title value.
        name = NEW_NAME;
        // Needed for the input field value.
        this.set('section.id', name);
      }
      if (name === GLOBAL_NAME) {
        return 'Global Capabilities';
      } else if (name.startsWith(REFS_NAME)) {
        return `Reference: ${name}`;
      }
      return name;
    },

    _handlePermissionUpdated(e) {
      // Deep copy
      const toRemove = Object.assign({},
          JSON.parse(JSON.stringify(this.section)));
      toRemove.value.permissions[e.detail.id] = {
        rules: e.detail.remove.rules,
      };
      this.section.value.permissions[e.detail.id] = {
        rules: e.detail.add.rules,
      };
      this.fire('section-updated', {
        id: this.section.id,
        add: this.section,
        remove: toRemove,
      });
    },

    _handlePermissionRemoved(e) {
      this.section.value.permissions[e.detail.permission.id] = {
        rules: e.detail.add.rules,
      };

      this.fire('section-updated', {
        section: this.section,
        removal: true,
      });
    },

    _saveReference(e) {
      this._editing = false;
      console.log('TODO');
    },

    _handleEditReference() {
      this._editing = true;
    },

    _computeEditingClass() {
      return this._editing ? 'editing' : '';
    },

    _handleAddPermission() {
      const value = this.$.permissionSelect.value;
      const permissions = {
        id: value,
        value: {rules: {}},
      };
      if (value.startsWith('label')) {
        permissions.value.label =
            value.replace('label-', '').replace('labelAs-', '');
      }
      this.push('_permissions', permissions);
    },
  });
})();