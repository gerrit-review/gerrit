// Copyright (C) 2008 The Android Open Source Project
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

package com.google.gerrit.httpd.rpc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gerrit.common.data.AccountInfo;
import com.google.gerrit.common.data.SuggestService;
import com.google.gerrit.reviewdb.Account;
import com.google.gerrit.reviewdb.AccountExternalId;
import com.google.gerrit.reviewdb.AccountGroupName;
import com.google.gerrit.reviewdb.Project;
import com.google.gerrit.reviewdb.ReviewDb;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.account.AccountCache;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectState;
import com.google.gerrit.server.util.FutureUtil;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwtorm.client.OrmException;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
<<<<<<< HEAD   (dfd895 Shorten the name of the group_agreements cache)
import java.util.concurrent.Future;
=======
>>>>>>> BRANCH (f855f3 Fix all of our pom.xml versions to be 2.1-SNAPSHOT)

class SuggestServiceImpl extends BaseServiceImplementation implements
    SuggestService {
  private static final String MAX_SUFFIX = "\u9fa5";

  private final ProjectCache projectCache;
  private final AccountCache accountCache;
  private final Provider<CurrentUser> currentUser;

  @Inject
  SuggestServiceImpl(final Provider<ReviewDb> schema,
      final ProjectCache projectCache, final AccountCache accountCache,
      final Provider<CurrentUser> currentUser) {
    super(schema, currentUser);
    this.projectCache = projectCache;
    this.accountCache = accountCache;
    this.currentUser = currentUser;
  }

  public void suggestProjectNameKey(final String query, final int limit,
      final AsyncCallback<List<Project.NameKey>> callback) {
    run(callback, new Action<List<Project.NameKey>>() {
      public List<Project.NameKey> run(final ReviewDb db) throws OrmException {
        final String a = query;
        final String b = a + MAX_SUFFIX;
        final int max = 10;
        final int n = limit <= 0 ? max : Math.min(limit, max);

        List<Future<ProjectState>> want = Lists.newArrayList();
        for (Project p : db.projects().suggestByName(a, b, n)) {
          want.add(projectCache.get(p.getNameKey()));
        }

        CurrentUser user = currentUser.get();
        List<Project.NameKey> res = Lists.newArrayList();
        for (Future<ProjectState> f : want) {
          ProjectState e = FutureUtil.getOrNull(f);
          if (e != null && e.controlFor(user).isVisible()) {
            res.add(e.getProject().getNameKey());
          }
        }
        return res;
      }
    });
  }

  public void suggestAccount(final String query, final Boolean active,
      final int limit, final AsyncCallback<List<AccountInfo>> callback) {
    run(callback, new Action<List<AccountInfo>>() {
      public List<AccountInfo> run(final ReviewDb db) throws OrmException {
        final String a = query;
        final String b = a + MAX_SUFFIX;
        final int max = 10;
        final int n = limit <= 0 ? max : Math.min(limit, max);

<<<<<<< HEAD   (dfd895 Shorten the name of the group_agreements cache)
        LinkedHashMap<Account.Id, AccountInfo> res = Maps.newLinkedHashMap();
        for (Account p : db.accounts().suggestByFullName(a, b, n)) {
          res.put(p.getId(), new AccountInfo(p));
=======
        final LinkedHashMap<Account.Id, AccountInfo> r =
            new LinkedHashMap<Account.Id, AccountInfo>();
        for (final Account p : db.accounts().suggestByFullName(a, b, n)) {
          addSuggestion(r, p, new AccountInfo(p), active);
>>>>>>> BRANCH (f855f3 Fix all of our pom.xml versions to be 2.1-SNAPSHOT)
        }
<<<<<<< HEAD   (dfd895 Shorten the name of the group_agreements cache)
        if (res.size() < n) {
          for (Account p : db.accounts().suggestByPreferredEmail(a, b,
              n - res.size())) {
            res.put(p.getId(), new AccountInfo(p));
=======
        if (r.size() < n) {
          for (final Account p : db.accounts().suggestByPreferredEmail(a, b,
              n - r.size())) {
            addSuggestion(r, p, new AccountInfo(p), active);
>>>>>>> BRANCH (f855f3 Fix all of our pom.xml versions to be 2.1-SNAPSHOT)
          }
        }
<<<<<<< HEAD   (dfd895 Shorten the name of the group_agreements cache)
        if (res.size() < n) {
          Map<String, Future<Account>> want = Maps.newHashMap();
          for (AccountExternalId e : db.accountExternalIds()
              .suggestByEmailAddress(a, b, n - res.size())) {
            if (!res.containsKey(e.getAccountId())) {
              want.put(e.getEmailAddress(), //
                  accountCache.getAccount(e.getAccountId()));
=======
        if (r.size() < n) {
          for (final AccountExternalId e : db.accountExternalIds()
              .suggestByEmailAddress(a, b, n - r.size())) {
            if (!r.containsKey(e.getAccountId())) {
              final Account p = accountCache.get(e.getAccountId()).getAccount();
              final AccountInfo info = new AccountInfo(p);
              info.setPreferredEmail(e.getEmailAddress());
              addSuggestion(r, p, info, active);
>>>>>>> BRANCH (f855f3 Fix all of our pom.xml versions to be 2.1-SNAPSHOT)
            }
          }

          for (Map.Entry<String, Future<Account>> ent : want.entrySet()) {
            Account p = FutureUtil.get(ent.getValue());
            AccountInfo info = new AccountInfo(p);
            info.setPreferredEmail(ent.getKey());
            res.put(p.getId(), info);
          }
        }
        return new ArrayList<AccountInfo>(res.values());
      }
    });
  }

  private void addSuggestion(Map map, Account account, AccountInfo info,
      Boolean active) {
    if (active == null || active == account.isActive()) {
      map.put(account.getId(), info);
    }
  }

  public void suggestAccountGroup(final String query, final int limit,
      final AsyncCallback<List<AccountGroupName>> callback) {
    run(callback, new Action<List<AccountGroupName>>() {
      public List<AccountGroupName> run(final ReviewDb db) throws OrmException {
        final String a = query;
        final String b = a + MAX_SUFFIX;
        final int max = 10;
        final int n = limit <= 0 ? max : Math.min(limit, max);
        return db.accountGroupNames().suggestByName(a, b, n).toList();
      }
    });
  }
}
