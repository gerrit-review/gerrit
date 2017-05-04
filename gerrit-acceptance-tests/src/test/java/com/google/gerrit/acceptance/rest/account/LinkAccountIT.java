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

package com.google.gerrit.acceptance.rest.account;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.gerrit.acceptance.AbstractDaemonTest;
import com.google.gerrit.acceptance.Sandboxed;
import com.google.gerrit.extensions.common.AccountExternalIdInfo;
import com.google.gerrit.extensions.common.EmailInfo;
import com.google.gerrit.server.config.ConfirmEmail;
import com.google.gerrit.server.mail.EmailTokenVerifier;
import com.google.gerrit.testutil.ConfigSuite;
import com.google.gwtjsonrpc.server.SignedToken;
import com.google.inject.Inject;
import org.eclipse.jgit.lib.Config;
import org.junit.Test;

public class LinkAccountIT extends AbstractDaemonTest {
  @ConfigSuite.Default
  public static Config defaultConfig() {
    Config cfg = new Config();
    cfg.setString("auth", null, "registerEmailPrivateKey", SignedToken.generateRandomKey());
    // TODO setting
    return cfg;
  }

  @Inject private EmailTokenVerifier emailTokenVerifier;

  @Test
  @Sandboxed
  public void linkAccounts() throws Exception {
    // Remove user's "username:user" external ID so that they only have the "mailto:" one remaining
    //setApiUser(user);
    //gApi.accounts().self().deleteExternalIds(ImmutableList.of("username:" + user.username));
    //assertThat(gApi.accounts().id(user.id.get()).getExternalIds()).hasSize(1);
    //setApiUser(admin);

    // Confirm user.email from admin account
    ConfirmEmail.Input in = new ConfirmEmail.Input();
    in.token = emailTokenVerifier.encode(admin.getId(), user.email);
    adminRestSession.put("/config/server/email.confirm", in).assertNoContent();

    // Check that admin now has three external IDs: one with their username and two for
    // admin@example.com and user@example.com
    assertThat(gApi.accounts().self().getExternalIds())
        .containsExactly(
            new AccountExternalIdInfo("username:admin", null, true, null),
            new AccountExternalIdInfo("mailto:" + user.email, user.email, true, true),
            new AccountExternalIdInfo("mailto:" + admin.email, admin.email, true, true));

    // Check that the new external ID is also reflected when getting the admin's email addresses
    assertThat(gApi.accounts().id(admin.id.get()).getEmails())
        .containsExactly(
            new EmailInfo(admin.email, true, null), new EmailInfo(user.email, null, null));

    // Since this was user's only external ID, the account must be disabled now
    assertThat(gApi.accounts().id(user.id.get()).getActive()).isFalse();
  }

  //  @Test
  //  @GerritConfig(name = "account.linkAccounts", value = "true")
  //  public void linkAccounts_changePrimaryEmailAddressOnLeftoverAccount() throws Exception {
  //    ConfirmEmail.Input in = new ConfirmEmail.Input();
  //    in.token = emailTokenVerifier.encode(admin.getId(), user.email);
  //    adminRestSession.put("/config/server/email.confirm", in).assertNoContent();
  //    EmailInfo adminInfo = new EmailInfo();
  //    adminInfo.email = admin.email;
  //    adminInfo.preferred = true;
  //    EmailInfo userInfo = new EmailInfo();
  //    userInfo.email = user.email;
  //
  //    List<AccountExternalIdInfo> extIds = gApi.accounts().self().getExternalIds();
  //
  //    assertThat(gApi.accounts().self().getExternalIds().stream().map(e -> e.emailAddress).collect(toList())).containsExactly(admin.email, user.email, null);
  //    //assertThat(gApi.accounts().self().getEmails()).containsExactly(adminInfo, userInfo);
  //    //adminRestSession.put("/config/server/email.confirm", in).assertUnprocessableEntity();
  //  }
}
