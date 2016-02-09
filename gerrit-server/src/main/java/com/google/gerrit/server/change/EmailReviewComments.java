// Copyright (C) 2012 The Android Open Source Project
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

package com.google.gerrit.server.change;

import static com.google.gerrit.server.PatchLineCommentsUtil.PLC_ORDER;

import com.google.gerrit.extensions.api.changes.ReviewInput.NotifyHandling;
import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.reviewdb.client.ChangeMessage;
import com.google.gerrit.reviewdb.client.PatchLineComment;
import com.google.gerrit.reviewdb.client.PatchSet;
import com.google.gerrit.reviewdb.server.ReviewDb;
import com.google.gerrit.server.CurrentUser;
<<<<<<< HEAD   (6da228 Remove index defaultMaxClauseCount config setting while reus)
import com.google.gerrit.server.git.SendEmailExecutor;
=======
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.git.EmailReviewCommentsExecutor;
import com.google.gerrit.server.git.WorkQueue.Executor;
>>>>>>> BRANCH (e39cb5 EmailReviewComments: Provide the current user instead of exc)
import com.google.gerrit.server.mail.CommentSender;
import com.google.gerrit.server.patch.PatchSetInfoFactory;
import com.google.gerrit.server.util.RequestContext;
import com.google.gerrit.server.util.ThreadLocalRequestContext;
import com.google.gwtorm.server.OrmException;
import com.google.gwtorm.server.SchemaFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class EmailReviewComments implements Runnable, RequestContext {
  private static final Logger log = LoggerFactory.getLogger(EmailReviewComments.class);

  interface Factory {
    EmailReviewComments create(
        NotifyHandling notify,
        Change change,
        PatchSet patchSet,
        IdentifiedUser user,
        ChangeMessage message,
        List<PatchLineComment> comments);
  }

  private final ExecutorService sendEmailsExecutor;
  private final PatchSetInfoFactory patchSetInfoFactory;
  private final CommentSender.Factory commentSenderFactory;
  private final SchemaFactory<ReviewDb> schemaFactory;
  private final ThreadLocalRequestContext requestContext;

  private final NotifyHandling notify;
  private final Change change;
  private final PatchSet patchSet;
  private final IdentifiedUser user;
  private final ChangeMessage message;
  private List<PatchLineComment> comments;
  private ReviewDb db;

  @Inject
<<<<<<< HEAD   (6da228 Remove index defaultMaxClauseCount config setting while reus)
  EmailReviewComments (
      @SendEmailExecutor ExecutorService executor,
=======
  EmailReviewComments(
      @EmailReviewCommentsExecutor final Executor executor,
>>>>>>> BRANCH (e39cb5 EmailReviewComments: Provide the current user instead of exc)
      PatchSetInfoFactory patchSetInfoFactory,
      CommentSender.Factory commentSenderFactory,
      SchemaFactory<ReviewDb> schemaFactory,
      ThreadLocalRequestContext requestContext,
      @Assisted NotifyHandling notify,
      @Assisted Change change,
      @Assisted PatchSet patchSet,
      @Assisted IdentifiedUser user,
      @Assisted ChangeMessage message,
      @Assisted List<PatchLineComment> comments) {
    this.sendEmailsExecutor = executor;
    this.patchSetInfoFactory = patchSetInfoFactory;
    this.commentSenderFactory = commentSenderFactory;
    this.schemaFactory = schemaFactory;
    this.requestContext = requestContext;
    this.notify = notify;
    this.change = change;
    this.patchSet = patchSet;
    this.user = user;
    this.message = message;
    this.comments = PLC_ORDER.sortedCopy(comments);
  }

  void sendAsync() {
    sendEmailsExecutor.submit(this);
  }

  @Override
  public void run() {
    RequestContext old = requestContext.setContext(this);
    try {

<<<<<<< HEAD   (6da228 Remove index defaultMaxClauseCount config setting while reus)
      CommentSender cm = commentSenderFactory.create(notify, change.getId());
      cm.setFrom(authorId);
=======
      comments = Lists.newArrayList(comments);
      Collections.sort(comments, new Comparator<PatchLineComment>() {
        @Override
        public int compare(PatchLineComment a, PatchLineComment b) {
          int cmp = path(a).compareTo(path(b));
          if (cmp != 0) {
            return cmp;
          }

          // 0 is ancestor, 1 is revision. Sort ancestor first.
          cmp = a.getSide() - b.getSide();
          if (cmp != 0) {
            return cmp;
          }

          return a.getLine() - b.getLine();
        }

        private String path(PatchLineComment c) {
          return c.getKey().getParentKey().getFileName();
        }
      });

      CommentSender cm = commentSenderFactory.create(notify, change);
      cm.setFrom(user.getAccountId());
>>>>>>> BRANCH (e39cb5 EmailReviewComments: Provide the current user instead of exc)
      cm.setPatchSet(patchSet, patchSetInfoFactory.get(change, patchSet));
      cm.setChangeMessage(message);
      cm.setPatchLineComments(comments);
      cm.send();
    } catch (Exception e) {
      log.error("Cannot email comments for " + patchSet.getId(), e);
    } finally {
      requestContext.setContext(old);
      if (db != null) {
        db.close();
        db = null;
      }
    }
  }

  @Override
  public String toString() {
    return "send-email comments";
  }

  @Override
<<<<<<< HEAD   (6da228 Remove index defaultMaxClauseCount config setting while reus)
  public CurrentUser getUser() {
    throw new OutOfScopeException("No user on email thread");
=======
  public CurrentUser getCurrentUser() {
    return user.getRealUser();
>>>>>>> BRANCH (e39cb5 EmailReviewComments: Provide the current user instead of exc)
  }

  @Override
  public Provider<ReviewDb> getReviewDbProvider() {
    return new Provider<ReviewDb>() {
      @Override
      public ReviewDb get() {
        if (db == null) {
          try {
            db = schemaFactory.open();
          } catch (OrmException e) {
            throw new ProvisionException("Cannot open ReviewDb", e);
          }
        }
        return db;
      }
    };
  }
}
