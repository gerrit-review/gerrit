// Copyright (C) 2013 The Android Open Source Project
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

package com.google.gerrit.acceptance.rest.change;

import static com.google.common.truth.Truth.assertThat;
<<<<<<< HEAD   (f7fc14 Format Java files with google-java-format)
=======
import static com.google.gerrit.acceptance.GitUtil.getChangeId;
import static com.google.gerrit.acceptance.GitUtil.pushHead;
import static com.google.gerrit.server.group.SystemGroupBackend.REGISTERED_USERS;
>>>>>>> BRANCH (43fe97 Don't require Add Patch Set permission for submit by rebase)

import com.google.gerrit.acceptance.PushOneCommit;
import com.google.gerrit.acceptance.TestAccount;
import com.google.gerrit.acceptance.TestProjectInput;
<<<<<<< HEAD   (f7fc14 Format Java files with google-java-format)
=======
import com.google.gerrit.common.data.Permission;
import com.google.gerrit.extensions.api.changes.SubmitInput;
import com.google.gerrit.extensions.client.ChangeStatus;
>>>>>>> BRANCH (43fe97 Don't require Add Patch Set permission for submit by rebase)
import com.google.gerrit.extensions.client.InheritableBoolean;
import com.google.gerrit.extensions.client.SubmitType;
<<<<<<< HEAD   (f7fc14 Format Java files with google-java-format)
=======
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.restapi.ResourceConflictException;
import com.google.gerrit.reviewdb.client.Branch;
import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.reviewdb.client.PatchSet;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.change.Submit.TestSubmitInput;
import com.google.gerrit.server.git.ProjectConfig;
import com.google.gerrit.server.project.Util;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
>>>>>>> BRANCH (43fe97 Don't require Add Patch Set permission for submit by rebase)
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

public class SubmitByRebaseIfNecessaryIT extends AbstractSubmitByRebase {

  @Override
  protected SubmitType getSubmitType() {
    return SubmitType.REBASE_IF_NECESSARY;
  }

  @Test
  @TestProjectInput(useContentMerge = InheritableBoolean.TRUE)
  public void submitWithFastForward() throws Exception {
    RevCommit oldHead = getRemoteHead();
    PushOneCommit.Result change = createChange();
    submit(change.getChangeId());
    RevCommit head = getRemoteHead();
    assertThat(head.getId()).isEqualTo(change.getCommit());
    assertThat(head.getParent(0)).isEqualTo(oldHead);
    assertApproved(change.getChangeId());
    assertCurrentRevision(change.getChangeId(), 1, head);
    assertSubmitter(change.getChangeId(), 1);
    assertPersonEquals(admin.getIdent(), head.getAuthorIdent());
    assertPersonEquals(admin.getIdent(), head.getCommitterIdent());
    assertRefUpdatedEvents(oldHead, head);
    assertChangeMergedEvents(change.getChangeId(), head.name());
  }

  @Test
  @TestProjectInput(useContentMerge = InheritableBoolean.TRUE)
<<<<<<< HEAD   (f7fc14 Format Java files with google-java-format)
=======
  public void submitWithRebase() throws Exception {
    submitWithRebase(admin);
  }

  @Test
  @TestProjectInput(useContentMerge = InheritableBoolean.TRUE)
  public void submitWithRebaseWithoutAddPatchSetPermission() throws Exception {
    ProjectConfig cfg = projectCache.checkedGet(project).getConfig();
    Util.block(cfg, Permission.ADD_PATCH_SET, REGISTERED_USERS, "refs/*");
    Util.allow(cfg, Permission.SUBMIT, REGISTERED_USERS, "refs/heads/*");
    Util.allow(cfg, Permission.forLabel(Util.codeReview().getName()), -2, 2,
        REGISTERED_USERS, "refs/heads/*");
    saveProjectConfig(project, cfg);

    submitWithRebase(user);
  }

  private void submitWithRebase(TestAccount submitter) throws Exception {
    setApiUser(submitter);
    RevCommit initialHead = getRemoteHead();
    PushOneCommit.Result change =
        createChange("Change 1", "a.txt", "content");
    submit(change.getChangeId());

    RevCommit headAfterFirstSubmit = getRemoteHead();
    testRepo.reset(initialHead);
    PushOneCommit.Result change2 =
        createChange("Change 2", "b.txt", "other content");
    submit(change2.getChangeId());
    assertRebase(testRepo, false);
    RevCommit headAfterSecondSubmit = getRemoteHead();
    assertThat(headAfterSecondSubmit.getParent(0))
        .isEqualTo(headAfterFirstSubmit);
    assertApproved(change2.getChangeId(), submitter);
    assertCurrentRevision(change2.getChangeId(), 2, headAfterSecondSubmit);
    assertSubmitter(change2.getChangeId(), 1, submitter);
    assertSubmitter(change2.getChangeId(), 2, submitter);
    assertPersonEquals(admin.getIdent(),
        headAfterSecondSubmit.getAuthorIdent());
    assertPersonEquals(submitter.getIdent(),
        headAfterSecondSubmit.getCommitterIdent());

    assertRefUpdatedEvents(initialHead, headAfterFirstSubmit,
        headAfterFirstSubmit, headAfterSecondSubmit);
    assertChangeMergedEvents(change.getChangeId(), headAfterFirstSubmit.name(),
        change2.getChangeId(), headAfterSecondSubmit.name());
  }

  @Test
  public void submitWithRebaseMultipleChanges() throws Exception {
    RevCommit initialHead = getRemoteHead();
    PushOneCommit.Result change1 =
        createChange("Change 1", "a.txt", "content");
    submit(change1.getChangeId());
    RevCommit headAfterFirstSubmit = getRemoteHead();
    assertThat(headAfterFirstSubmit.name())
        .isEqualTo(change1.getCommit().name());

    testRepo.reset(initialHead);
    PushOneCommit.Result change2 =
        createChange("Change 2", "b.txt", "other content");
    assertThat(change2.getCommit().getParent(0))
        .isNotEqualTo(change1.getCommit());
    PushOneCommit.Result change3 =
        createChange("Change 3", "c.txt", "third content");
    PushOneCommit.Result change4 =
        createChange("Change 4", "d.txt", "fourth content");
    approve(change2.getChangeId());
    approve(change3.getChangeId());
    submit(change4.getChangeId());

    assertRebase(testRepo, false);
    assertApproved(change2.getChangeId());
    assertApproved(change3.getChangeId());
    assertApproved(change4.getChangeId());

    RevCommit headAfterSecondSubmit = parse(getRemoteHead());
    assertThat(headAfterSecondSubmit.getShortMessage()).isEqualTo("Change 4");
    assertThat(headAfterSecondSubmit).isNotEqualTo(change4.getCommit());
    assertCurrentRevision(change4.getChangeId(), 2, headAfterSecondSubmit);

    RevCommit parent = parse(headAfterSecondSubmit.getParent(0));
    assertThat(parent.getShortMessage()).isEqualTo("Change 3");
    assertThat(parent).isNotEqualTo(change3.getCommit());
    assertCurrentRevision(change3.getChangeId(), 2, parent);

    RevCommit grandparent = parse(parent.getParent(0));
    assertThat(grandparent).isNotEqualTo(change2.getCommit());
    assertCurrentRevision(change2.getChangeId(), 2, grandparent);

    RevCommit greatgrandparent = parse(grandparent.getParent(0));
    assertThat(greatgrandparent).isEqualTo(change1.getCommit());
    assertCurrentRevision(change1.getChangeId(), 1, greatgrandparent);

    assertRefUpdatedEvents(initialHead, headAfterFirstSubmit,
        headAfterFirstSubmit, headAfterSecondSubmit);
    assertChangeMergedEvents(change1.getChangeId(), headAfterFirstSubmit.name(),
        change2.getChangeId(), headAfterSecondSubmit.name(),
        change3.getChangeId(), headAfterSecondSubmit.name(),
        change4.getChangeId(), headAfterSecondSubmit.name());
  }

  @Test
  public void submitWithRebaseMergeCommit() throws Exception {
    /*
        *  (HEAD, origin/master, origin/HEAD) Merge changes X,Y
        |\
        | *   Merge branch 'master' into origin/master
        | |\
        | | * SHA Added a
        | |/
        * | Before
        |/
        * Initial empty repository
     */
    RevCommit initialHead = getRemoteHead();
    PushOneCommit.Result change1 = createChange("Added a", "a.txt", "");

    PushOneCommit change2Push = pushFactory.create(db, admin.getIdent(), testRepo,
        "Merge to master", "m.txt", "");
    change2Push.setParents(ImmutableList.of(initialHead, change1.getCommit()));
    PushOneCommit.Result change2 = change2Push.to("refs/for/master");

    testRepo.reset(initialHead);
    PushOneCommit.Result change3 = createChange("Before", "b.txt", "");

    approve(change3.getChangeId());
    submit(change3.getChangeId());

    approve(change1.getChangeId());
    approve(change2.getChangeId());
    submit(change2.getChangeId());

    RevCommit newHead = getRemoteHead();
    assertThat(newHead.getParentCount()).isEqualTo(2);

    RevCommit headParent1 = parse(newHead.getParent(0).getId());
    RevCommit headParent2 = parse(newHead.getParent(1).getId());

    assertThat(headParent1.getId()).isEqualTo(change3.getCommit().getId());
    assertThat(headParent1.getParentCount()).isEqualTo(1);
    assertThat(headParent1.getParent(0)).isEqualTo(initialHead);

    assertThat(headParent2.getId()).isEqualTo(change2.getCommit().getId());
    assertThat(headParent2.getParentCount()).isEqualTo(2);

    RevCommit headGrandparent1 = parse(headParent2.getParent(0).getId());
    RevCommit headGrandparent2 = parse(headParent2.getParent(1).getId());

    assertThat(headGrandparent1.getId()).isEqualTo(initialHead.getId());
    assertThat(headGrandparent2.getId()).isEqualTo(change1.getCommit().getId());
  }

  @Test
  @TestProjectInput(useContentMerge = InheritableBoolean.TRUE)
>>>>>>> BRANCH (43fe97 Don't require Add Patch Set permission for submit by rebase)
  public void submitWithContentMerge() throws Exception {
    RevCommit initialHead = getRemoteHead();
    PushOneCommit.Result change = createChange("Change 1", "a.txt", "aaa\nbbb\nccc\n");
    submit(change.getChangeId());
    RevCommit headAfterFirstSubmit = getRemoteHead();
    PushOneCommit.Result change2 = createChange("Change 2", "a.txt", "aaa\nbbb\nccc\nddd\n");
    submit(change2.getChangeId());

    RevCommit headAfterSecondSubmit = getRemoteHead();
    testRepo.reset(change.getCommit());
    PushOneCommit.Result change3 = createChange("Change 3", "a.txt", "bbb\nccc\n");
    submit(change3.getChangeId());
    assertRebase(testRepo, true);
    RevCommit headAfterThirdSubmit = getRemoteHead();
    assertThat(headAfterThirdSubmit.getParent(0)).isEqualTo(headAfterSecondSubmit);
    assertApproved(change3.getChangeId());
    assertCurrentRevision(change3.getChangeId(), 2, headAfterThirdSubmit);
    assertSubmitter(change3.getChangeId(), 1);
    assertSubmitter(change3.getChangeId(), 2);

    assertRefUpdatedEvents(
        initialHead,
        headAfterFirstSubmit,
        headAfterFirstSubmit,
        headAfterSecondSubmit,
        headAfterSecondSubmit,
        headAfterThirdSubmit);
    assertChangeMergedEvents(
        change.getChangeId(),
        headAfterFirstSubmit.name(),
        change2.getChangeId(),
        headAfterSecondSubmit.name(),
        change3.getChangeId(),
        headAfterThirdSubmit.name());
  }
}
