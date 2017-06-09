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

package com.google.gerrit.server.account;

import static com.google.gerrit.server.account.externalids.ExternalId.SCHEME_USERNAME;

import com.google.common.base.Strings;
import com.google.gerrit.extensions.restapi.AuthException;
import com.google.gerrit.extensions.restapi.ResourceConflictException;
import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.extensions.restapi.Response;
import com.google.gerrit.extensions.restapi.RestModifyView;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.account.PutHttpPassword.Input;
import com.google.gerrit.server.account.externalids.ExternalId;
import com.google.gerrit.server.account.externalids.ExternalIds;
import com.google.gerrit.server.account.externalids.ExternalIdsUpdate;
import com.google.gerrit.server.permissions.GlobalPermission;
import com.google.gerrit.server.permissions.PermissionBackend;
import com.google.gerrit.server.permissions.PermissionBackendException;
import com.google.gwtorm.server.OrmException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.jgit.errors.ConfigInvalidException;

public class PutHttpPassword implements RestModifyView<AccountResource, Input> {
  public static class Input {
    public String httpPassword;
    public boolean generate;
  }

  private static final int LEN = 31;
  private static final SecureRandom rng;

  static {
    try {
      rng = SecureRandom.getInstance("SHA1PRNG");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Cannot create RNG for password generator", e);
    }
  }

  private final Provider<CurrentUser> self;
<<<<<<< HEAD   (c93e48 Merge "Make it configurable whether the first user should be)
  private final PermissionBackend permissionBackend;
  private final ExternalIds externalIds;
=======
  private final Provider<ReviewDb> dbProvider;
>>>>>>> BRANCH (4847c3 Let ExternalIdsUpdate take care to evict accounts from the a)
  private final ExternalIdsUpdate.User externalIdsUpdate;

  @Inject
  PutHttpPassword(
      Provider<CurrentUser> self,
<<<<<<< HEAD   (c93e48 Merge "Make it configurable whether the first user should be)
      PermissionBackend permissionBackend,
      ExternalIds externalIds,
=======
      Provider<ReviewDb> dbProvider,
>>>>>>> BRANCH (4847c3 Let ExternalIdsUpdate take care to evict accounts from the a)
      ExternalIdsUpdate.User externalIdsUpdate) {
    this.self = self;
<<<<<<< HEAD   (c93e48 Merge "Make it configurable whether the first user should be)
    this.permissionBackend = permissionBackend;
    this.externalIds = externalIds;
=======
    this.dbProvider = dbProvider;
>>>>>>> BRANCH (4847c3 Let ExternalIdsUpdate take care to evict accounts from the a)
    this.externalIdsUpdate = externalIdsUpdate;
  }

  @Override
  public Response<String> apply(AccountResource rsrc, Input input)
      throws AuthException, ResourceNotFoundException, ResourceConflictException, OrmException,
          IOException, ConfigInvalidException, PermissionBackendException {
    if (self.get() != rsrc.getUser()) {
      permissionBackend.user(self).check(GlobalPermission.ADMINISTRATE_SERVER);
    }

    if (input == null) {
      input = new Input();
    }
    input.httpPassword = Strings.emptyToNull(input.httpPassword);

    String newPassword;
    if (input.generate) {
      newPassword = generate();
    } else if (input.httpPassword == null) {
      newPassword = null;
    } else {
      // Only administrators can explicitly set the password.
      permissionBackend.user(self).check(GlobalPermission.ADMINISTRATE_SERVER);
      newPassword = input.httpPassword;
    }
    return apply(rsrc.getUser(), newPassword);
  }

  public Response<String> apply(IdentifiedUser user, String newPassword)
      throws ResourceNotFoundException, ResourceConflictException, OrmException, IOException,
          ConfigInvalidException {
    if (user.getUserName() == null) {
      throw new ResourceConflictException("username must be set");
    }

    ExternalId extId = externalIds.get(ExternalId.Key.create(SCHEME_USERNAME, user.getUserName()));
    if (extId == null) {
      throw new ResourceNotFoundException();
    }
    ExternalId newExtId =
        ExternalId.createWithPassword(extId.key(), extId.accountId(), extId.email(), newPassword);
<<<<<<< HEAD   (c93e48 Merge "Make it configurable whether the first user should be)
    externalIdsUpdate.create().upsert(newExtId);
=======
    externalIdsUpdate.create().upsert(dbProvider.get(), newExtId);
>>>>>>> BRANCH (4847c3 Let ExternalIdsUpdate take care to evict accounts from the a)

    return Strings.isNullOrEmpty(newPassword) ? Response.<String>none() : Response.ok(newPassword);
  }

  public static String generate() {
    byte[] rand = new byte[LEN];
    rng.nextBytes(rand);

    byte[] enc = Base64.encodeBase64(rand, false);
    StringBuilder r = new StringBuilder(enc.length);
    for (int i = 0; i < enc.length; i++) {
      if (enc[i] == '=') {
        break;
      }
      r.append((char) enc[i]);
    }
    return r.toString();
  }
}
