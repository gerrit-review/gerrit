// Copyright (C) 2014 The Android Open Source Project
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

package com.google.gerrit.acceptance.server.change;

import static com.google.common.truth.Truth.assertThat;
import static com.google.gerrit.acceptance.PushOneCommit.FILE_NAME;
import static com.google.gerrit.acceptance.PushOneCommit.SUBJECT;
import static com.google.gerrit.server.group.SystemGroupBackend.ANONYMOUS_USERS;
import static com.google.gerrit.server.project.Util.category;
import static com.google.gerrit.server.project.Util.value;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gerrit.acceptance.AbstractDaemonTest;
import com.google.gerrit.acceptance.GitUtil;
import com.google.gerrit.acceptance.NoHttpd;
import com.google.gerrit.acceptance.PushOneCommit;
import com.google.gerrit.common.data.LabelType;
import com.google.gerrit.common.data.Permission;
import com.google.gerrit.extensions.api.changes.DraftInput;
import com.google.gerrit.extensions.api.changes.ReviewInput;
import com.google.gerrit.extensions.api.changes.ReviewInput.CommentInput;
import com.google.gerrit.extensions.api.changes.ReviewInput.DraftHandling;
import com.google.gerrit.extensions.client.ChangeStatus;
import com.google.gerrit.extensions.client.Comment;
import com.google.gerrit.extensions.client.Side;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gerrit.extensions.restapi.BadRequestException;
import com.google.gerrit.extensions.restapi.IdString;
import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.extensions.restapi.TopLevelResource;
import com.google.gerrit.reviewdb.client.AccountGroup;
import com.google.gerrit.reviewdb.client.Patch;
import com.google.gerrit.reviewdb.client.RefNames;
import com.google.gerrit.server.change.ChangeResource;
import com.google.gerrit.server.change.ChangesCollection;
import com.google.gerrit.server.change.PostReview;
import com.google.gerrit.server.change.RevisionResource;
import com.google.gerrit.server.git.ProjectConfig;
import com.google.gerrit.server.group.SystemGroupBackend;
import com.google.gerrit.server.project.Util;
import com.google.gerrit.testutil.FakeEmailSender;
import com.google.gerrit.testutil.FakeEmailSender.Message;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoHttpd
public class CommentsIT extends AbstractDaemonTest {

  @Inject
  private Provider<ChangesCollection> changes;

  @Inject
  private Provider<PostReview> postReview;

  @Inject
  private FakeEmailSender email;

  private final Integer[] lines = {0, 1};

  @Before
  public void setUp() {
    setApiUser(user);
  }

  @Test
  public void getNonExistingComment() throws Exception {
    PushOneCommit.Result r = createChange();
    String changeId = r.getChangeId();
    String revId = r.getCommit().getName();
    exception.expect(ResourceNotFoundException.class);
    getPublishedComment(changeId, revId, "non-existing");
  }

  @Test
  public void createDraft() throws Exception {
    for (Integer line : lines) {
      PushOneCommit.Result r = createChange();
      String changeId = r.getChangeId();
      String revId = r.getCommit().getName();
      String path = "file1";
      DraftInput comment = newDraft(path, Side.REVISION, line, "comment 1");
      addDraft(changeId, revId, comment);
      Map<String, List<CommentInfo>> result = getDraftComments(changeId, revId);
      assertThat(result).hasSize(1);
      CommentInfo actual = Iterables.getOnlyElement(result.get(comment.path));
      assertThat(comment).isEqualTo(infoToDraft(path).apply(actual));
    }
  }

  @Test
  public void createDraftOnMergeCommitChange() throws Exception {
    for (Integer line : lines) {
      PushOneCommit.Result r = createMergeCommitChange("refs/for/master");
      String changeId = r.getChangeId();
      String revId = r.getCommit().getName();
      String path = "file1";
      DraftInput c1 = newDraft(path, Side.REVISION, line, "ps-1");
      DraftInput c2 = newDraft(path, Side.PARENT, line, "auto-merge of ps-1");
      DraftInput c3 = newDraftOnParent(path, 1, line, "parent-1 of ps-1");
      DraftInput c4 = newDraftOnParent(path, 2, line, "parent-2 of ps-1");
      addDraft(changeId, revId, c1);
      addDraft(changeId, revId, c2);
      addDraft(changeId, revId, c3);
      addDraft(changeId, revId, c4);
      Map<String, List<CommentInfo>> result = getDraftComments(changeId, revId);
      assertThat(result).hasSize(1);
      assertThat(Lists.transform(result.get(path), infoToDraft(path)))
          .containsExactly(c1, c2, c3, c4);
    }
  }

  @Test
  public void postComment() throws Exception {
    for (Integer line : lines) {
      String file = "file";
      String contents = "contents " + line;
      PushOneCommit push = pushFactory.create(db, admin.getIdent(), testRepo,
          "first subject", file, contents);
      PushOneCommit.Result r = push.to("refs/for/master");
      String changeId = r.getChangeId();
      String revId = r.getCommit().getName();
      ReviewInput input = new ReviewInput();
      CommentInput comment =
          newComment(file, Side.REVISION, line, "comment 1", false);
      input.comments = new HashMap<>();
      input.comments.put(comment.path, Lists.newArrayList(comment));
      revision(r).review(input);
      Map<String, List<CommentInfo>> result = getPublishedComments(changeId, revId);
      assertThat(result).isNotEmpty();
      CommentInfo actual = Iterables.getOnlyElement(result.get(comment.path));
      assertThat(comment).isEqualTo(infoToInput(file).apply(actual));
      assertThat(comment).isEqualTo(infoToInput(file).apply(
          getPublishedComment(changeId, revId, actual.id)));
    }
  }

  @Test
  public void postCommentWithUnresolved() throws Exception {
    for (Integer line : lines) {
      String file = "file";
      String contents = "contents " + line;
      PushOneCommit push = pushFactory.create(db, admin.getIdent(), testRepo,
          "first subject", file, contents);
      PushOneCommit.Result r = push.to("refs/for/master");
      String changeId = r.getChangeId();
      String revId = r.getCommit().getName();
      ReviewInput input = new ReviewInput();
      CommentInput comment =
          newComment(file, Side.REVISION, line, "comment 1", true);
      input.comments = new HashMap<>();
      input.comments.put(comment.path, Lists.newArrayList(comment));
      revision(r).review(input);
      Map<String, List<CommentInfo>> result = getPublishedComments(changeId, revId);
      assertThat(result).isNotEmpty();
      CommentInfo actual = Iterables.getOnlyElement(result.get(comment.path));
      assertThat(comment).isEqualTo(infoToInput(file).apply(actual));
      assertThat(comment).isEqualTo(infoToInput(file).apply(
          getPublishedComment(changeId, revId, actual.id)));
    }
  }

  @Test
  public void postCommentOnMergeCommitChange() throws Exception {
    for (Integer line : lines) {
      String file = "foo";
      PushOneCommit.Result r = createMergeCommitChange("refs/for/master", file);
      String changeId = r.getChangeId();
      String revId = r.getCommit().getName();
      ReviewInput input = new ReviewInput();
      CommentInput c1 = newComment(file, Side.REVISION, line, "ps-1", false);
      CommentInput c2 =
          newComment(file, Side.PARENT, line, "auto-merge of ps-1", false);
      CommentInput c3 = newCommentOnParent(file, 1, line, "parent-1 of ps-1");
      CommentInput c4 = newCommentOnParent(file, 2, line, "parent-2 of ps-1");
      input.comments = new HashMap<>();
      input.comments.put(file, ImmutableList.of(c1, c2, c3, c4));
      revision(r).review(input);
      Map<String, List<CommentInfo>> result = getPublishedComments(changeId, revId);
      assertThat(result).isNotEmpty();
      assertThat(Lists.transform(result.get(file), infoToInput(file)))
          .containsExactly(c1, c2, c3, c4);
    }

    // for the commit message comments on the auto-merge are not possible
    for (Integer line : lines) {
      String file = Patch.COMMIT_MSG;
      PushOneCommit.Result r = createMergeCommitChange("refs/for/master");
      String changeId = r.getChangeId();
      String revId = r.getCommit().getName();
      ReviewInput input = new ReviewInput();
      CommentInput c1 = newComment(file, Side.REVISION, line, "ps-1", false);
      CommentInput c2 = newCommentOnParent(file, 1, line, "parent-1 of ps-1");
      CommentInput c3 = newCommentOnParent(file, 2, line, "parent-2 of ps-1");
      input.comments = new HashMap<>();
      input.comments.put(file, ImmutableList.of(c1, c2, c3));
      revision(r).review(input);
      Map<String, List<CommentInfo>> result = getPublishedComments(changeId, revId);
      assertThat(result).isNotEmpty();
      assertThat(Lists.transform(result.get(file), infoToInput(file)))
          .containsExactly(c1, c2, c3);
    }
  }

  @Test
  public void postCommentOnCommitMessageOnAutoMerge() throws Exception {
    PushOneCommit.Result r = createMergeCommitChange("refs/for/master");
    ReviewInput input = new ReviewInput();
    CommentInput c = newComment(Patch.COMMIT_MSG, Side.PARENT, 0,
        "comment on auto-merge", false);
    input.comments = new HashMap<>();
    input.comments.put(Patch.COMMIT_MSG, ImmutableList.of(c));
    exception.expect(BadRequestException.class);
    exception.expectMessage(
        "cannot comment on " + Patch.COMMIT_MSG + " on auto-merge");
    revision(r).review(input);
  }

  @Test
  public void listComments() throws Exception {
    String file = "file";
    PushOneCommit push = pushFactory.create(db, admin.getIdent(), testRepo,
        "first subject", file, "contents");
    PushOneCommit.Result r = push.to("refs/for/master");
    String changeId = r.getChangeId();
    String revId = r.getCommit().getName();
    assertThat(getPublishedComments(changeId, revId)).isEmpty();

    List<CommentInput> expectedComments = new ArrayList<>();
    for (Integer line : lines) {
      ReviewInput input = new ReviewInput();
      CommentInput comment =
          newComment(file, Side.REVISION, line, "comment " + line, false);
      expectedComments.add(comment);
      input.comments = new HashMap<>();
      input.comments.put(comment.path, Lists.newArrayList(comment));
      revision(r).review(input);
    }

    Map<String, List<CommentInfo>> result = getPublishedComments(changeId, revId);
    assertThat(result).isNotEmpty();
    List<CommentInfo> actualComments = result.get(file);
    assertThat(Lists.transform(actualComments, infoToInput(file)))
        .containsExactlyElementsIn(expectedComments);
  }

  @Test
  public void putDraft() throws Exception {
    for (Integer line : lines) {
      PushOneCommit.Result r = createChange();
      Timestamp origLastUpdated = r.getChange().change().getLastUpdatedOn();
      String changeId = r.getChangeId();
      String revId = r.getCommit().getName();
      String path = "file1";
      DraftInput comment = newDraft(path, Side.REVISION, line, "comment 1");
      addDraft(changeId, revId, comment);
      Map<String, List<CommentInfo>> result = getDraftComments(changeId, revId);
      CommentInfo actual = Iterables.getOnlyElement(result.get(comment.path));
      assertThat(comment).isEqualTo(infoToDraft(path).apply(actual));
      String uuid = actual.id;
      comment.message = "updated comment 1";
      updateDraft(changeId, revId, comment, uuid);
      result = getDraftComments(changeId, revId);
      actual = Iterables.getOnlyElement(result.get(comment.path));
      assertThat(comment).isEqualTo(infoToDraft(path).apply(actual));

      // Posting a draft comment doesn't cause lastUpdatedOn to change.
      assertThat(r.getChange().change().getLastUpdatedOn())
          .isEqualTo(origLastUpdated);
    }
  }

  @Test
  public void listDrafts() throws Exception {
    String file = "file";
    PushOneCommit.Result r = createChange();
    String changeId = r.getChangeId();
    String revId = r.getCommit().getName();
    assertThat(getDraftComments(changeId, revId)).isEmpty();

    List<DraftInput> expectedDrafts = new ArrayList<>();
    for (Integer line : lines) {
      DraftInput comment = newDraft(file, Side.REVISION, line, "comment " + line);
      expectedDrafts.add(comment);
      addDraft(changeId, revId, comment);
    }

    Map<String, List<CommentInfo>> result = getDraftComments(changeId, revId);
    assertThat(result).isNotEmpty();
    List<CommentInfo> actualComments = result.get(file);
    assertThat(Lists.transform(actualComments, infoToDraft(file)))
        .containsExactlyElementsIn(expectedDrafts);
  }

  @Test
  public void getDraft() throws Exception {
    for (Integer line : lines) {
      PushOneCommit.Result r = createChange();
      String changeId = r.getChangeId();
      String revId = r.getCommit().getName();
      String path = "file1";
      DraftInput comment = newDraft(
          path, Side.REVISION, line, "comment 1");
      CommentInfo returned = addDraft(changeId, revId, comment);
      CommentInfo actual = getDraftComment(changeId, revId, returned.id);
      assertThat(comment).isEqualTo(infoToDraft(path).apply(actual));
    }
  }

  @Test
  public void deleteDraft() throws Exception {
    for (Integer line : lines) {
      PushOneCommit.Result r = createChange();
      Timestamp origLastUpdated = r.getChange().change().getLastUpdatedOn();
      String changeId = r.getChangeId();
      String revId = r.getCommit().getName();
      DraftInput draft = newDraft("file1", Side.REVISION, line, "comment 1");
      CommentInfo returned = addDraft(changeId, revId, draft);
      deleteDraft(changeId, revId, returned.id);
      Map<String, List<CommentInfo>> drafts = getDraftComments(changeId, revId);
      assertThat(drafts).isEmpty();

      // Deleting a draft comment doesn't cause lastUpdatedOn to change.
      assertThat(r.getChange().change().getLastUpdatedOn())
          .isEqualTo(origLastUpdated);
    }
  }

  @Test
  public void insertCommentsWithHistoricTimestamp() throws Exception {
    Timestamp timestamp = new Timestamp(0);
    for (Integer line : lines) {
      String file = "file";
      String contents = "contents " + line;
      PushOneCommit push = pushFactory.create(db, admin.getIdent(), testRepo,
          "first subject", file, contents);
      PushOneCommit.Result r = push.to("refs/for/master");
      String changeId = r.getChangeId();
      String revId = r.getCommit().getName();
      Timestamp origLastUpdated = r.getChange().change().getLastUpdatedOn();

      ReviewInput input = new ReviewInput();
      CommentInput comment =
          newComment(file, Side.REVISION, line, "comment 1", false);
      comment.updated = timestamp;
      input.comments = new HashMap<>();
      input.comments.put(comment.path, Lists.newArrayList(comment));
      ChangeResource changeRsrc =
          changes.get().parse(TopLevelResource.INSTANCE,
              IdString.fromDecoded(changeId));
      RevisionResource revRsrc =
          revisions.parse(changeRsrc, IdString.fromDecoded(revId));
      postReview.get().apply(revRsrc, input, timestamp);
      Map<String, List<CommentInfo>> result = getPublishedComments(changeId, revId);
      assertThat(result).isNotEmpty();
      CommentInfo actual = Iterables.getOnlyElement(result.get(comment.path));
      CommentInput ci = infoToInput(file).apply(actual);
      ci.updated = comment.updated;
      assertThat(comment).isEqualTo(ci);
      assertThat(actual.updated)
          .isEqualTo(gApi.changes().id(r.getChangeId()).info().created);

      // Updating historic comments doesn't cause lastUpdatedOn to regress.
      assertThat(r.getChange().change().getLastUpdatedOn())
          .isEqualTo(origLastUpdated);
    }
  }

  @Test
  public void addDuplicateComments() throws Exception {
    PushOneCommit.Result r1 = createChange();
    String changeId = r1.getChangeId();
    String revId = r1.getCommit().getName();
    addComment(r1, "nit: trailing whitespace");
    addComment(r1, "nit: trailing whitespace");
    Map<String, List<CommentInfo>> result = getPublishedComments(changeId, revId);
    assertThat(result.get(FILE_NAME)).hasSize(2);
    addComment(r1, "nit: trailing whitespace", true, false);
    result = getPublishedComments(changeId, revId);
    assertThat(result.get(FILE_NAME)).hasSize(2);

    PushOneCommit.Result r2 = pushFactory.create(
          db, admin.getIdent(), testRepo, SUBJECT, FILE_NAME, "content")
        .to("refs/for/master");
    changeId = r2.getChangeId();
    revId = r2.getCommit().getName();
    addComment(r2, "nit: trailing whitespace", true, false);
    result = getPublishedComments(changeId, revId);
    assertThat(result.get(FILE_NAME)).hasSize(1);
  }

  @Test
  public void listChangeDrafts() throws Exception {
    PushOneCommit.Result r1 = createChange();

    PushOneCommit.Result r2 = pushFactory.create(
          db, admin.getIdent(), testRepo, SUBJECT, FILE_NAME, "new content",
          r1.getChangeId())
        .to("refs/for/master");


    setApiUser(admin);
    addDraft(r1.getChangeId(), r1.getCommit().getName(),
        newDraft(FILE_NAME, Side.REVISION, 1, "nit: trailing whitespace"));
    addDraft(r2.getChangeId(), r2.getCommit().getName(),
        newDraft(FILE_NAME, Side.REVISION, 1, "typo: content"));

    setApiUser(user);
    addDraft(r2.getChangeId(), r2.getCommit().getName(),
        newDraft(FILE_NAME, Side.REVISION, 1, "+1, please fix"));

    setApiUser(admin);
    Map<String, List<CommentInfo>> actual =
        gApi.changes().id(r1.getChangeId()).drafts();
    assertThat(actual.keySet()).containsExactly(FILE_NAME);
    List<CommentInfo> comments = actual.get(FILE_NAME);
    assertThat(comments).hasSize(2);

    CommentInfo c1 = comments.get(0);
    assertThat(c1.author).isNull();
    assertThat(c1.patchSet).isEqualTo(1);
    assertThat(c1.message).isEqualTo("nit: trailing whitespace");
    assertThat(c1.side).isNull();
    assertThat(c1.line).isEqualTo(1);

    CommentInfo c2 = comments.get(1);
    assertThat(c2.author).isNull();
    assertThat(c2.patchSet).isEqualTo(2);
    assertThat(c2.message).isEqualTo("typo: content");
    assertThat(c2.side).isNull();
    assertThat(c2.line).isEqualTo(1);
  }

  @Test
  public void listChangeComments() throws Exception {
    PushOneCommit.Result r1 = createChange();

    PushOneCommit.Result r2 = pushFactory.create(
          db, admin.getIdent(), testRepo, SUBJECT, FILE_NAME, "new cntent",
          r1.getChangeId())
        .to("refs/for/master");

    addComment(r1, "nit: trailing whitespace");
    addComment(r2, "typo: content");

    Map<String, List<CommentInfo>> actual = gApi.changes()
        .id(r2.getChangeId())
        .comments();
    assertThat(actual.keySet()).containsExactly(FILE_NAME);

    List<CommentInfo> comments = actual.get(FILE_NAME);
    assertThat(comments).hasSize(2);

    CommentInfo c1 = comments.get(0);
    assertThat(c1.author._accountId).isEqualTo(user.getId().get());
    assertThat(c1.patchSet).isEqualTo(1);
    assertThat(c1.message).isEqualTo("nit: trailing whitespace");
    assertThat(c1.side).isNull();
    assertThat(c1.line).isEqualTo(1);

    CommentInfo c2 = comments.get(1);
    assertThat(c2.author._accountId).isEqualTo(user.getId().get());
    assertThat(c2.patchSet).isEqualTo(2);
    assertThat(c2.message).isEqualTo("typo: content");
    assertThat(c2.side).isNull();
    assertThat(c2.line).isEqualTo(1);
  }

  @Test
  public void listChangeWithDrafts() throws Exception {
    for (Integer line : lines) {
      PushOneCommit.Result r = createChange();
      String changeId = r.getChangeId();
      String revId = r.getCommit().getName();
      DraftInput comment = newDraft(
          "file1", Side.REVISION, line, "comment 1");
      addDraft(changeId, revId, comment);
      assertThat(gApi.changes().query(
          "change:" + changeId + " has:draft").get()).hasSize(1);
    }
  }

  @Test
  public void publishCommentsAllRevisions() throws Exception {
    PushOneCommit.Result r1 = createChange();

    PushOneCommit.Result r2 = pushFactory.create(
          db, admin.getIdent(), testRepo, SUBJECT, FILE_NAME, "new\ncntent\n",
          r1.getChangeId())
        .to("refs/for/master");

    addDraft(r1.getChangeId(), r1.getCommit().getName(),
        newDraft(FILE_NAME, Side.REVISION, 1, "nit: trailing whitespace"));
    addDraft(r1.getChangeId(), r1.getCommit().getName(),
        newDraft(FILE_NAME, Side.PARENT, 2, "what happened to this?"));
    addDraft(r2.getChangeId(), r2.getCommit().getName(),
        newDraft(FILE_NAME, Side.REVISION, 1, "join lines"));
    addDraft(r2.getChangeId(), r2.getCommit().getName(),
        newDraft(FILE_NAME, Side.REVISION, 2, "typo: content"));
    addDraft(r2.getChangeId(), r2.getCommit().getName(),
        newDraft(FILE_NAME, Side.PARENT, 1, "comment 1 on base"));
    addDraft(r2.getChangeId(), r2.getCommit().getName(),
        newDraft(FILE_NAME, Side.PARENT, 2, "comment 2 on base"));

    PushOneCommit.Result other = createChange();
    // Drafts on other changes aren't returned.
    addDraft(other.getChangeId(), other.getCommit().getName(),
        newDraft(FILE_NAME, Side.REVISION, 1, "unrelated comment"));

    setApiUser(admin);
    // Drafts by other users aren't returned.
    addDraft(r2.getChangeId(), r2.getCommit().getName(),
        newDraft(FILE_NAME, Side.REVISION, 2, "oops"));
    setApiUser(user);

    ReviewInput reviewInput = new ReviewInput();
    reviewInput.drafts = DraftHandling.PUBLISH_ALL_REVISIONS;
    reviewInput.message = "comments";
    gApi.changes()
       .id(r2.getChangeId())
       .current()
       .review(reviewInput);

    assertThat(gApi.changes()
          .id(r1.getChangeId())
          .revision(r1.getCommit().name())
          .drafts())
        .isEmpty();
    Map<String, List<CommentInfo>> ps1Map = gApi.changes()
        .id(r1.getChangeId())
        .revision(r1.getCommit().name())
        .comments();
    assertThat(ps1Map.keySet()).containsExactly(FILE_NAME);
    List<CommentInfo> ps1List = ps1Map.get(FILE_NAME);
    assertThat(ps1List).hasSize(2);
    assertThat(ps1List.get(0).message).isEqualTo("what happened to this?");
    assertThat(ps1List.get(0).side).isEqualTo(Side.PARENT);
    assertThat(ps1List.get(1).message).isEqualTo("nit: trailing whitespace");
    assertThat(ps1List.get(1).side).isNull();

    assertThat(gApi.changes()
          .id(r2.getChangeId())
          .revision(r2.getCommit().name())
          .drafts())
        .isEmpty();
    Map<String, List<CommentInfo>> ps2Map = gApi.changes()
        .id(r2.getChangeId())
        .revision(r2.getCommit().name())
        .comments();
    assertThat(ps2Map.keySet()).containsExactly(FILE_NAME);
    List<CommentInfo> ps2List = ps2Map.get(FILE_NAME);
    assertThat(ps2List).hasSize(4);
    assertThat(ps2List.get(0).message).isEqualTo("comment 1 on base");
    assertThat(ps2List.get(1).message).isEqualTo("comment 2 on base");
    assertThat(ps2List.get(2).message).isEqualTo("join lines");
    assertThat(ps2List.get(3).message).isEqualTo("typo: content");

    List<Message> messages = email.getMessages(r2.getChangeId(), "comment");
    assertThat(messages).hasSize(1);
    String url = canonicalWebUrl.get();
    int c = r1.getChange().getId().get();
    assertThat(extractComments(messages.get(0).body())).isEqualTo(
        "Patch Set 2:\n"
        + "\n"
        + "(6 comments)\n"
        + "\n"
        + "comments\n"
        + "\n"
        + url + "#/c/" + c + "/1/a.txt\n"
        + "File a.txt:\n"
        + "\n"
        + url + "#/c/" + c + "/1/a.txt@a2\n"
        + "PS1, Line 2: \n"
        + "what happened to this?\n"
        + "\n"
        + "\n"
        + url + "#/c/" + c + "/1/a.txt@1\n"
        + "PS1, Line 1: ew\n"
        + "nit: trailing whitespace\n"
        + "\n"
        + "\n"
        + url + "#/c/" + c + "/2/a.txt\n"
        + "File a.txt:\n"
        + "\n"
        + url + "#/c/" + c + "/2/a.txt@a1\n"
        + "PS2, Line 1: \n"
        + "comment 1 on base\n"
        + "\n"
        + "\n"
        + url + "#/c/" + c + "/2/a.txt@a2\n"
        + "PS2, Line 2: \n"
        + "comment 2 on base\n"
        + "\n"
        + "\n"
        + url + "#/c/" + c + "/2/a.txt@1\n"
        + "PS2, Line 1: ew\n"
        + "join lines\n"
        + "\n"
        + "\n"
        + url + "#/c/" + c + "/2/a.txt@2\n"
        + "PS2, Line 2: nten\n"
        + "typo: content\n"
        + "\n"
        + "\n");
  }

  @Test
  public void commentTags() throws Exception {
    PushOneCommit.Result r = createChange();

    CommentInput pub = new CommentInput();
    pub.line = 1;
    pub.message = "published comment";
    pub.path = FILE_NAME;
    ReviewInput rin = newInput(pub);
    rin.tag = "tag1";
    gApi.changes().id(r.getChangeId()).current().review(rin);

    List<CommentInfo> comments =
        gApi.changes().id(r.getChangeId()).current().commentsAsList();
    assertThat(comments).hasSize(1);
    assertThat(comments.get(0).tag).isEqualTo("tag1");

    DraftInput draft = new DraftInput();
    draft.line = 2;
    draft.message = "draft comment";
    draft.path = FILE_NAME;
    draft.tag = "tag2";
    addDraft(r.getChangeId(), r.getCommit().name(), draft);

    List<CommentInfo> drafts =
        gApi.changes().id(r.getChangeId()).current().draftsAsList();
    assertThat(drafts).hasSize(1);
    assertThat(drafts.get(0).tag).isEqualTo("tag2");
  }

  @Test
  public void unresolvedCommentsBlocked()
      throws Exception {
    PushOneCommit.Result r = createChange();

    setApiUser(admin);
    gApi.changes()
        .id(r.getChangeId())
        .revision(r.getCommit().name())
        .review(ReviewInput.approve());

    GitUtil.fetch(testRepo, RefNames.REFS_CONFIG + ":config");
    testRepo.reset("config");
//    PushOneCommit push2 = pushFactory.create(db, admin.getIdent(), testRepo,
//        "Configure",
//        "rules.pl",
//        "submit_rule(submit(CR)) :-\n"
//            + "  gerrit:unresolved_comments(0),\n"
//            + "  gerrit:default_submit(X).");
//
    PushOneCommit push2 = pushFactory.create(db, admin.getIdent(), testRepo,
        "Configure",
        "rules.pl",
        "submit_rule(submit(CR)) :- \n"
            + "gerrit:change_branch('refs/meta/config'),\n"
            + "!,\n"
            + "gerrit:max_with_block(-2, 2, 'Code-Review', CR).\n\n"

            + "submit_rule(submit(CR, V, L)) :- \n"
            + "needs_library_compliance,\n"
            + "!,\n"
            + "base(CR, V),\n"
            + "gerrit:max_with_block(-1, 1, 'Library-Compliance', L).\n\n"

            + "submit_rule(submit(CR, V)) :- \n"
            + "is_no_polygerrit_change, \n"
            + "!,\n"
            + "base(CR, V).\n\n"

            + "submit_rule(submit(P, V)) :- \n"
            + "gerrit:max_with_block(-2, 2, 'PolyGerrit-Review', P),\n"
            + "gerrit:max_with_block(-1, 1, 'Verified', V). \n\n"

            + "base(CR, V) :- \n"
            + "gerrit:unresolved_comments(0), \n"
            + "gerrit:max_with_block(-2, 2, 'Code-Review', CR), \n"
            + "gerrit:max_with_block(-1, 1, 'Verified', V). \n\n"

            + "needs_library_compliance :- "
            + "gerrit:commit_delta('^lib/'), !.\n"

            + "needs_library_compliance :- "
            + "gerrit:commit_delta('^[.]buckversion$'), !. \n"

            + "nneeds_library_compliance :- "
            + "gerrit:commit_delta('^WORKSPACE$'), !. \n\n"

            + "is_no_polygerrit_change :- \n"
            + "gerrit:commit_delta('^(?!polygerrit-ui/).*$').");

    push2.to(RefNames.REFS_CONFIG);

    // Allow user to approve
    //    ProjectConfig cfg = projectCache.checkedGet(project).getConfig();
    //    AccountGroup.UUID registeredUsers =
    //        SystemGroupBackend.getGroup(REGISTERED_USERS).getUUID();
    //    String heads = RefNames.REFS_HEADS + "*";
    //    Util.allow(cfg, Permission.forLabel(Util.codeReview().getName()), -2, 2,
    //        registeredUsers, heads);
    //    saveProjectConfig(project, cfg);



    addComment(r, "nit: trailing whitespace", true, false);

    // print out number of unresolved comments
    Map<String, List<CommentInfo>> comments =
        gApi.changes().id(r.getChangeId()).comments();
    for (Map.Entry<String, List<CommentInfo>> entry : comments.entrySet()) {
      for (CommentInfo c : entry.getValue()) {
        System.out.println(c.message + " --> unresolved: " + c.unresolved);
      }
    }

    LabelType verified = category("Verified",
        value(1, "Passes"), value(0, "No score"), value(-1, "Failed"));
    LabelType resolved = category("Resolved",
        value(1, "Passes"), value(0, "No score"), value(-1, "Failed"));
    ProjectConfig cfg = projectCache.checkedGet(project).getConfig();
    cfg.getLabelSections().put(verified.getName(), verified);
    cfg.getLabelSections().put(resolved.getName(), resolved);
    String heads = "refs/heads/*";
    AccountGroup.UUID anon =
        SystemGroupBackend.getGroup(ANONYMOUS_USERS).getUUID();
    Util.allow(cfg, Permission.forLabel("Verified"), -1, 1, anon, heads);
    Util.allow(cfg, Permission.forLabel("Resolved"), -1, 1, anon, heads);
    saveProjectConfig(project, cfg);

    setApiUser(admin);
    ReviewInput in = new ReviewInput();
    in.label("Code-Review", 2);
    in.label("Verified", 0);
    in.label("Resolved", 1);
    gApi.changes().id(r.getChangeId()).current().review(in);

    gApi.changes()
        .id(r.getChangeId())
        .revision(r.getCommit().name())
        .submit();

    ChangeInfo change = gApi.changes()
        .id(r.getChangeId())
        .get();
    assertThat(change.status).isEqualTo(ChangeStatus.MERGED);
    //    assertThat(change.labels.keySet()).containsExactly("Code-Review",
    //        "Non-Author-Code-Review");
    //    assertThat(change.permittedLabels.keySet()).containsExactly("Code-Review");
    //    assertPermitted(change, "Code-Review", 0, 1, 2);
  }


  private static String extractComments(String msg) {
    // Extract lines between start "....." and end "-- ".
    Pattern p = Pattern.compile(".*[.]{5}\n+(.*)\\n+-- \n.*", Pattern.DOTALL);
    Matcher m = p.matcher(msg);
    return m.matches() ? m.group(1) : msg;
  }

  private ReviewInput newInput(CommentInput c) {
    ReviewInput in = new ReviewInput();
    in.comments = new HashMap<>();
    in.comments.put(c.path, Lists.newArrayList(c));
    return in;
  }

  private void addComment(PushOneCommit.Result r, String message)
      throws Exception {
    addComment(r, message, false, false);
  }

  private void addComment(PushOneCommit.Result r, String message,
      boolean omitDuplicateComments, boolean unresolved) throws Exception {
    CommentInput c = new CommentInput();
    c.line = 1;
    c.message = message;
    c.path = FILE_NAME;
    c.unresolved = unresolved;
    ReviewInput in = newInput(c);
    in.omitDuplicateComments = omitDuplicateComments;
    gApi.changes()
        .id(r.getChangeId())
        .revision(r.getCommit().name())
        .review(in);
  }

  private CommentInfo addDraft(String changeId, String revId, DraftInput in)
      throws Exception {
    return gApi.changes().id(changeId).revision(revId).createDraft(in).get();
  }

  private void updateDraft(String changeId, String revId, DraftInput in,
      String uuid) throws Exception {
    gApi.changes().id(changeId).revision(revId).draft(uuid).update(in);
  }

  private void deleteDraft(String changeId, String revId, String uuid)
      throws Exception {
    gApi.changes().id(changeId).revision(revId).draft(uuid).delete();
  }

  private CommentInfo getPublishedComment(String changeId, String revId,
      String uuid) throws Exception {
    return gApi.changes().id(changeId).revision(revId).comment(uuid).get();
  }

  private Map<String, List<CommentInfo>> getPublishedComments(String changeId,
      String revId) throws Exception {
    return gApi.changes().id(changeId).revision(revId).comments();
  }

  private Map<String, List<CommentInfo>> getDraftComments(String changeId,
      String revId) throws Exception {
    return gApi.changes().id(changeId).revision(revId).drafts();
  }

  private CommentInfo getDraftComment(String changeId, String revId,
      String uuid) throws Exception {
    return gApi.changes().id(changeId).revision(revId).draft(uuid).get();
  }

  private static CommentInput newComment(String path, Side side, int line,
      String message, Boolean unresolved) {
    CommentInput c = new CommentInput();
    return populate(c, path, side, null, line, message, unresolved);
  }

  private static CommentInput newCommentOnParent(String path, int parent,
      int line, String message) {
    CommentInput c = new CommentInput();
    return populate(c, path, Side.PARENT, Integer.valueOf(parent), line,
        message, false);
  }

  private DraftInput newDraft(String path, Side side, int line,
      String message) {
    DraftInput d = new DraftInput();
    return populate(d, path, side, null, line, message, false);
  }

  private DraftInput newDraftOnParent(String path, int parent, int line,
      String message) {
    DraftInput d = new DraftInput();
    return populate(d, path, Side.PARENT, Integer.valueOf(parent), line,
        message, false);
  }

  private static <C extends Comment> C populate(C c, String path, Side side,
      Integer parent, int line, String message, Boolean unresolved) {
    c.path = path;
    c.side = side;
    c.parent = parent;
    c.line = line != 0 ? line : null;
    c.message = message;
    c.unresolved = unresolved;
    if (line != 0) {
      Comment.Range range = new Comment.Range();
      range.startLine = line;
      range.startCharacter = 1;
      range.endLine = line;
      range.endCharacter = 5;
      c.range = range;
    }
    return c;
  }

  private static Function<CommentInfo, CommentInput> infoToInput(String path) {
    return infoToInput(path, CommentInput::new);
  }

  private static Function<CommentInfo, DraftInput> infoToDraft(String path) {
    return infoToInput(path, DraftInput::new);
  }

  private static <I extends Comment> Function<CommentInfo, I> infoToInput(
      String path, Supplier<I> supplier) {
    return info -> {
      I i = supplier.get();
      i.path = path;
      copy(info, i);
      return i;
    };
  }

  private static void copy(Comment from, Comment to) {
    to.side = from.side == null ? Side.REVISION : from.side;
    to.parent = from.parent;
    to.line = from.line;
    to.message = from.message;
    to.range = from.range;
    to.unresolved = from.unresolved;
  }
}
