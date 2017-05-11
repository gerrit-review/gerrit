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
    is: 'gr-block-user',

    properties: {
      /** @type {?} */
      account: {
        type: Object,
        observer: '_accountChanged',
      },
      _isLoading: {
        type: Boolean,
        value: false,
      },
      _isBlocked: Boolean,
    },

    _computeHideBlockActions(account, _isLoading) {
      return !account || _isLoading;
    },

    _handleAccountSearch() {
      this.$.restAPI.getSuggestedAccounts(this.$.entry.getText()).then(
          account => {
            this.account = account;
          });
    },

    _accountChanged() {
      if (!this.account) { return; }
      this._isLoading = true;
      let accountId;
      //for (let i = 0; i < this.account.length; i++) {
      //  accountId = this.account[i]._account_id;
      //}
      //let account = accountId ? accountId : this.$.entry.getText();
      this.$.restAPI.getUserBlocked(this.$.entry.getText()).then(isBlocked => {
        console.log(isBlocked);
        if (!this.account) {
          // Late response.
          return;
        }
        this._isLoading = false;
        this._isBlocked = isBlocked;
      });
    },

    _setAccount(e) {
      this.account = e.detail.value.account;
    },

    _clearAccount(e) {
      this.account = null;
      this._isLoading = false;
    },

    _handleUnblockUserTap(e) {
      let account;
      const btnId = e.model.get('item._account_id');
      account = btnId ? btnId : this.$.entry.getText();
      this.$.restAPI.unblockUser(account).then(
          isUnblocked => {
            this._isBlocked = !isUnblocked;
          });
    },

    _handleBlockUserTap() {
      this.$.overlay.open();
    },

    _handleConfirmDialogCancel() {
      this.$.overlay.close();
    },

    _handleBlockUserConfirm(e) {
      const btnId = e.model.get('item._account_id');
      let account = btnId ? btnId : this.$.entry.getText();
      this.$.restAPI.blockUser(account)
          .then(isBlocked => {
            this._isBlocked = isBlocked;
            this._handleConfirmDialogCancel();
          });
    },
  });
})();
