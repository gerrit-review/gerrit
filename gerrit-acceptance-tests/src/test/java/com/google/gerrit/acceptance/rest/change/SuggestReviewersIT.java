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

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.gerrit.acceptance.AbstractDaemonTest;
import com.google.gerrit.acceptance.GerritConfig;
import com.google.gerrit.acceptance.GerritConfigs;
import com.google.gerrit.acceptance.TestAccount;
import com.google.gerrit.common.data.GlobalCapability;
import com.google.gerrit.common.data.GroupDescription;
import com.google.gerrit.common.data.GroupDescriptions;
import com.google.gerrit.extensions.common.GroupInfo;
import com.google.gerrit.extensions.common.SuggestedReviewerInfo;
<<<<<<< HEAD   (3772eb Merge "On init of admin user use email from SSH key file as )
import com.google.gerrit.extensions.restapi.TopLevelResource;
=======
>>>>>>> BRANCH (040601 Merge "Use in-memory Lucene index for a better reviewer sugg)
import com.google.gerrit.extensions.restapi.Url;
import com.google.gerrit.reviewdb.client.AccountGroup;
import com.google.gerrit.server.group.CreateGroup;
import com.google.gerrit.server.group.GroupsCollection;
import com.google.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SuggestReviewersIT extends AbstractDaemonTest {
  @Inject
  private CreateGroup.Factory createGroupFactory;

  @Inject
  private GroupsCollection groups;

  private AccountGroup group1;
  private AccountGroup group2;
  private AccountGroup group3;

  private TestAccount user1;
  private TestAccount user2;
  private TestAccount user3;

  @Before
  public void setUp() throws Exception {
    group1 = group("users1");
    group2 = group("users2");
    group3 = group("users3");

<<<<<<< HEAD   (3772eb Merge "On init of admin user use email from SSH key file as )
    user1 = user("user1", group1);
    user2 = user("user2", group2);
    user3 = user("user3", group1, group2);
=======
    user1 = accounts.create("user1", "user1@example.com", "First1 Last1",
        "users1");
    user2 = accounts.create("user2", "user2@example.com", "First2 Last2",
        "users2");
    user3 = accounts.create("user3", "user3@example.com", "First3 Last3",
        "users1", "users2");
>>>>>>> BRANCH (040601 Merge "Use in-memory Lucene index for a better reviewer sugg)
  }

  @Test
  @GerritConfig(name = "suggest.accounts", value = "false")
  public void suggestReviewersNoResult1() throws Exception {
    String changeId = createChange().getChangeId();
    List<SuggestedReviewerInfo> reviewers =
        suggestReviewers(changeId, name("u"), 6);
    assertThat(reviewers).isEmpty();
  }

  @Test
  @GerritConfigs(
      {@GerritConfig(name = "suggest.accounts", value = "true"),
       @GerritConfig(name = "suggest.from", value = "1"),
       @GerritConfig(name = "accounts.visibility", value = "NONE")
      })
  public void suggestReviewersNoResult2() throws Exception {
    String changeId = createChange().getChangeId();
    List<SuggestedReviewerInfo> reviewers =
        suggestReviewers(changeId, name("u"), 6);
    assertThat(reviewers).isEmpty();
  }

  @Test
  @GerritConfig(name = "suggest.from", value = "2")
  public void suggestReviewersNoResult3() throws Exception {
    String changeId = createChange().getChangeId();
    List<SuggestedReviewerInfo> reviewers =
        suggestReviewers(changeId, name("").substring(0, 1), 6);
    assertThat(reviewers).isEmpty();
  }

  @Test
  public void suggestReviewersChange() throws Exception {
    String changeId = createChange().getChangeId();
    List<SuggestedReviewerInfo> reviewers =
        suggestReviewers(changeId, name("u"), 6);
    assertThat(reviewers).hasSize(6);
    reviewers = suggestReviewers(changeId, name("u"), 5);
    assertThat(reviewers).hasSize(5);
    reviewers = suggestReviewers(changeId, group3.getName(), 10);
    assertThat(reviewers).hasSize(1);
  }

  @Test
  @GerritConfig(name = "accounts.visibility", value = "SAME_GROUP")
  public void suggestReviewersSameGroupVisibility() throws Exception {
    String changeId = createChange().getChangeId();
    List<SuggestedReviewerInfo> reviewers;

    reviewers = suggestReviewers(changeId, user2.fullName, 2);
    assertThat(reviewers).hasSize(1);
<<<<<<< HEAD   (3772eb Merge "On init of admin user use email from SSH key file as )
    assertThat(Iterables.getOnlyElement(reviewers).account.name)
        .isEqualTo(user2.fullName);
=======
    assertThat(Iterables.getOnlyElement(reviewers).account.name).isEqualTo(
        "First2 Last2");
>>>>>>> BRANCH (040601 Merge "Use in-memory Lucene index for a better reviewer sugg)

    setApiUser(user1);
    reviewers = suggestReviewers(changeId, user2.fullName, 2);
    assertThat(reviewers).isEmpty();

    setApiUser(user2);
    reviewers = suggestReviewers(changeId, user2.fullName, 2);
    assertThat(reviewers).hasSize(1);
<<<<<<< HEAD   (3772eb Merge "On init of admin user use email from SSH key file as )
    assertThat(Iterables.getOnlyElement(reviewers).account.name)
        .isEqualTo(user2.fullName);
=======
    assertThat(Iterables.getOnlyElement(reviewers).account.name).isEqualTo(
        "First2 Last2");
>>>>>>> BRANCH (040601 Merge "Use in-memory Lucene index for a better reviewer sugg)

    setApiUser(user3);
    reviewers = suggestReviewers(changeId, user2.fullName, 2);
    assertThat(reviewers).hasSize(1);
<<<<<<< HEAD   (3772eb Merge "On init of admin user use email from SSH key file as )
    assertThat(Iterables.getOnlyElement(reviewers).account.name)
        .isEqualTo(user2.fullName);
=======
    assertThat(Iterables.getOnlyElement(reviewers).account.name).isEqualTo(
        "First2 Last2");
>>>>>>> BRANCH (040601 Merge "Use in-memory Lucene index for a better reviewer sugg)
  }

  @Test
  @GerritConfig(name = "accounts.visibility", value = "SAME_GROUP")
  public void suggestReviewersViewAllAccounts() throws Exception {
    String changeId = createChange().getChangeId();
    List<SuggestedReviewerInfo> reviewers;

    setApiUser(user1);
    reviewers = suggestReviewers(changeId, user2.fullName, 2);
    assertThat(reviewers).isEmpty();

    setApiUser(user1); // Clear cached group info.
    allowGlobalCapabilities(group1.getGroupUUID(),
        GlobalCapability.VIEW_ALL_ACCOUNTS);
    reviewers = suggestReviewers(changeId, user2.fullName, 2);
    assertThat(reviewers).hasSize(1);
<<<<<<< HEAD   (3772eb Merge "On init of admin user use email from SSH key file as )
    assertThat(Iterables.getOnlyElement(reviewers).account.name)
        .isEqualTo(user2.fullName);
=======
    assertThat(Iterables.getOnlyElement(reviewers).account.name).isEqualTo(
        "First2 Last2");
>>>>>>> BRANCH (040601 Merge "Use in-memory Lucene index for a better reviewer sugg)
  }

  @Test
  @GerritConfig(name = "suggest.maxSuggestedReviewers", value = "2")
  public void suggestReviewersMaxNbrSuggestions() throws Exception {
    String changeId = createChange().getChangeId();
    List<SuggestedReviewerInfo> reviewers =
        suggestReviewers(changeId, name("user"), 5);
    assertThat(reviewers).hasSize(2);
  }

  @Test
  @GerritConfig(name = "suggest.fullTextSearch", value = "true")
  public void suggestReviewersFullTextSearch() throws Exception {
    String changeId = createChange().getChangeId();
    List<SuggestedReviewerInfo> reviewers;

    reviewers = suggestReviewers(changeId, "first", 4);
    assertThat(reviewers).hasSize(3);

    reviewers = suggestReviewers(changeId, "first1", 2);
    assertThat(reviewers).hasSize(1);

    reviewers = suggestReviewers(changeId, "last", 4);
    assertThat(reviewers).hasSize(3);

    reviewers = suggestReviewers(changeId, "last1", 2);
    assertThat(reviewers).hasSize(1);

    reviewers = suggestReviewers(changeId, "fi la", 4);
    assertThat(reviewers).hasSize(3);

    reviewers = suggestReviewers(changeId, "la fi", 4);
    assertThat(reviewers).hasSize(3);

    reviewers = suggestReviewers(changeId, "first1 la", 2);
    assertThat(reviewers).hasSize(1);

    reviewers = suggestReviewers(changeId, "fi last1", 2);
    assertThat(reviewers).hasSize(1);

    reviewers = suggestReviewers(changeId, "first1 last2", 1);
    assertThat(reviewers).hasSize(0);

    reviewers = suggestReviewers(changeId, "user", 8);
    assertThat(reviewers).hasSize(7);

    reviewers = suggestReviewers(changeId, "user1", 2);
    assertThat(reviewers).hasSize(1);

    reviewers = suggestReviewers(changeId, "example.com", 6);
    assertThat(reviewers).hasSize(5);

    reviewers = suggestReviewers(changeId, "user1@example.com", 2);
    assertThat(reviewers).hasSize(1);

    reviewers = suggestReviewers(changeId, "user1 example", 2);
    assertThat(reviewers).hasSize(1);
  }

  @Test
  @GerritConfigs(
      {@GerritConfig(name = "suggest.fulltextsearch", value = "true"),
       @GerritConfig(name = "suggest.fullTextSearchMaxMatches", value = "2")
  })
  public void suggestReviewersFullTextSearchLimitMaxMatches() throws Exception {
    String changeId = createChange().getChangeId();
    List<SuggestedReviewerInfo> reviewers =
        suggestReviewers(changeId, "user", 3);
    assertThat(reviewers).hasSize(3);
  }

  @Test
  public void suggestReviewersWithoutLimitOptionSpecified() throws Exception {
    String changeId = createChange().getChangeId();
    String query = user3.fullName;
    List<SuggestedReviewerInfo> suggestedReviewerInfos = gApi.changes()
        .id(changeId)
        .suggestReviewers(query)
        .get();
    assertThat(suggestedReviewerInfos).hasSize(1);
  }

<<<<<<< HEAD   (3772eb Merge "On init of admin user use email from SSH key file as )
=======
  private List<SuggestedReviewerInfo> suggestReviewers(RestSession session,
      String changeId, String query, int n) throws IOException {
    return newGson().fromJson(
        session.get("/changes/"
            + changeId
            + "/suggest_reviewers?q="
            + Url.encode(query)
            + "&n="
            + n)
        .getReader(),
        new TypeToken<List<SuggestedReviewerInfo>>() {}
        .getType());
  }

>>>>>>> BRANCH (040601 Merge "Use in-memory Lucene index for a better reviewer sugg)
  private List<SuggestedReviewerInfo> suggestReviewers(String changeId,
      String query, int n) throws Exception {
    return gApi.changes()
        .id(changeId)
        .suggestReviewers(query)
        .withLimit(n)
        .get();
  }

  private AccountGroup group(String name) throws Exception {
    GroupInfo group = createGroupFactory.create(name(name))
        .apply(TopLevelResource.INSTANCE, null);
    GroupDescription.Basic d = groups.parseInternal(Url.decode(group.id));
    return GroupDescriptions.toAccountGroup(d);
  }

  private TestAccount user(String name, AccountGroup... groups) throws Exception {
    name = name(name);
    String[] groupNames = FluentIterable.from(Arrays.asList(groups))
        .transform(new Function<AccountGroup, String>() {
          @Override
          public String apply(AccountGroup in) {
            return in.getName();
          }
        }).toArray(String.class);
    return accounts.create(name, name + "@example.com", name, groupNames);
  }
}
