// Copyright (C) 2016 The Android Open Source Project
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
    is: 'gr-confirm-revert-dialog',

    /**
     * Fired when the confirm button is pressed.
     *
     * @event confirm
     */

    /**
     * Fired when the cancel button is pressed.
     *
     * @event cancel
     */

    properties: {
      message: String,
    },

    populateRevertMessage: function(message, commitHash) {
      // Figure out what the revert title should be.
      var originalTitle = message.split('\n')[0];
      var revertTitle = 'Revert "' + originalTitle + '"';
      if (!commitHash) {
<<<<<<< HEAD   (48910a Do not add original description to reverts. Leave that for p)
        alert('Unable to find the commit hash of this issue.');
=======
        alert('Unable to find the commit hash of this change.');
>>>>>>> BRANCH (95b96f Use commit hash instead of Change-ID in the revert message)
        return;
      }
      var revertCommitText = 'This reverts commit ' + commitHash + '.';
<<<<<<< HEAD   (48910a Do not add original description to reverts. Leave that for p)
=======
      // Add '> ' in front of the original commit text.
      var originalCommitText = message.replace(/^/gm, '> ');
>>>>>>> BRANCH (95b96f Use commit hash instead of Change-ID in the revert message)

      this.message = revertTitle + '\n\n' +
                     revertCommitText + '\n\n' +
                     'Reason for revert: <INSERT REASONING HERE>\n';
    },

    _handleConfirmTap: function(e) {
      e.preventDefault();
      this.fire('confirm', null, {bubbles: false});
    },

    _handleCancelTap: function(e) {
      e.preventDefault();
      this.fire('cancel', null, {bubbles: false});
    },
  });
})();
