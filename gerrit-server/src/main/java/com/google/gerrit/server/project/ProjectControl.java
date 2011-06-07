// Copyright (C) 2009 The Android Open Source Project
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

package com.google.gerrit.server.project;

import static com.google.gerrit.common.CollectionsUtil.isAnyIncludedIn;

import com.google.gerrit.common.data.AccessSection;
import com.google.gerrit.common.data.Permission;
import com.google.gerrit.common.data.PermissionRule;
import com.google.gerrit.reviewdb.AccountGroup;
import com.google.gerrit.reviewdb.Branch;
import com.google.gerrit.reviewdb.Change;
import com.google.gerrit.reviewdb.Project;
<<<<<<< HEAD   (1118da Correct usage of @Nullable annotation in EventFactory)
=======
import com.google.gerrit.reviewdb.RefRight;
import com.google.gerrit.reviewdb.SystemConfig;
>>>>>>> BRANCH (019d2c Fix ChangeDetailFactory's invocation of PatchSetDetailFactor)
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.ReplicationUser;
import com.google.gerrit.server.config.GitReceivePackGroups;
import com.google.gerrit.server.config.GitUploadPackGroups;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

<<<<<<< HEAD   (1118da Correct usage of @Nullable annotation in EventFactory)
import java.util.Collection;
=======
import java.util.Collections;
>>>>>>> BRANCH (019d2c Fix ChangeDetailFactory's invocation of PatchSetDetailFactor)
import java.util.HashSet;
import java.util.Set;

/** Access control management for a user accessing a project's data. */
public class ProjectControl {
  public static final int VISIBLE = 1 << 0;
  public static final int OWNER = 1 << 1;

  public static class GenericFactory {
    private final ProjectCache projectCache;

    @Inject
    GenericFactory(final ProjectCache pc) {
      projectCache = pc;
    }

    public ProjectControl controlFor(Project.NameKey nameKey, CurrentUser user)
        throws NoSuchProjectException {
      final ProjectState p = projectCache.get(nameKey);
      if (p == null) {
        throw new NoSuchProjectException(nameKey);
      }
      return p.controlFor(user);
    }
  }

  public static class Factory {
    private final ProjectCache projectCache;
    private final Provider<CurrentUser> user;

    @Inject
    Factory(final ProjectCache pc, final Provider<CurrentUser> cu) {
      projectCache = pc;
      user = cu;
    }

    public ProjectControl controlFor(final Project.NameKey nameKey)
        throws NoSuchProjectException {
      final ProjectState p = projectCache.get(nameKey);
      if (p == null) {
        throw new NoSuchProjectException(nameKey);
      }
      return p.controlFor(user.get());
    }

    public ProjectControl validateFor(final Project.NameKey nameKey)
        throws NoSuchProjectException {
      return validateFor(nameKey, VISIBLE);
    }

    public ProjectControl ownerFor(final Project.NameKey nameKey)
        throws NoSuchProjectException {
      return validateFor(nameKey, OWNER);
    }

    public ProjectControl validateFor(final Project.NameKey nameKey,
        final int need) throws NoSuchProjectException {
      final ProjectControl c = controlFor(nameKey);
      if ((need & VISIBLE) == VISIBLE && c.isVisible()) {
        return c;
      }
      if ((need & OWNER) == OWNER && c.isOwner()) {
        return c;
      }
      throw new NoSuchProjectException(nameKey);
    }
  }

  interface AssistedFactory {
    ProjectControl create(CurrentUser who, ProjectState ps);
  }

<<<<<<< HEAD   (1118da Correct usage of @Nullable annotation in EventFactory)
  private final Set<AccountGroup.UUID> uploadGroups;
  private final Set<AccountGroup.UUID> receiveGroups;
=======
  private final SystemConfig systemConfig;
  private final Set<AccountGroup.Id> uploadGroups;
  private final Set<AccountGroup.Id> receiveGroups;
>>>>>>> BRANCH (019d2c Fix ChangeDetailFactory's invocation of PatchSetDetailFactor)

  private final RefControl.Factory refControlFactory;
  private final CurrentUser user;
  private final ProjectState state;

  private Collection<AccessSection> access;

  @Inject
<<<<<<< HEAD   (1118da Correct usage of @Nullable annotation in EventFactory)
  ProjectControl(@GitUploadPackGroups Set<AccountGroup.UUID> uploadGroups,
      @GitReceivePackGroups Set<AccountGroup.UUID> receiveGroups,
=======
  ProjectControl(final SystemConfig systemConfig,
      @GitUploadPackGroups Set<AccountGroup.Id> uploadGroups,
      @GitReceivePackGroups Set<AccountGroup.Id> receiveGroups,
>>>>>>> BRANCH (019d2c Fix ChangeDetailFactory's invocation of PatchSetDetailFactor)
      final RefControl.Factory refControlFactory,
      @Assisted CurrentUser who, @Assisted ProjectState ps) {
    this.systemConfig = systemConfig;
    this.uploadGroups = uploadGroups;
    this.receiveGroups = receiveGroups;
    this.refControlFactory = refControlFactory;
    user = who;
    state = ps;
  }

  public ProjectControl forAnonymousUser() {
    return state.controlForAnonymousUser();
  }

  public ProjectControl forUser(final CurrentUser who) {
    return state.controlFor(who);
  }

  public ChangeControl controlFor(final Change change) {
    return new ChangeControl(controlForRef(change.getDest()), change);
  }

  public RefControl controlForRef(Branch.NameKey ref) {
    return controlForRef(ref.get());
  }

  public RefControl controlForRef(String refName) {
    return refControlFactory.create(this, refName);
  }

  public CurrentUser getCurrentUser() {
    return user;
  }

  public ProjectState getProjectState() {
    return state;
  }

  public Project getProject() {
    return getProjectState().getProject();
  }

  /** Can this user see this project exists? */
  public boolean isVisible() {
    return visibleForReplication()
        || canPerformOnAnyRef(Permission.READ);
  }

  public boolean canAddRefs() {
    return (canPerformOnAnyRef(Permission.CREATE)
        || isOwnerAnyRef());
  }

  /** Can this user see all the refs in this projects? */
  public boolean allRefsAreVisible() {
    return visibleForReplication()
        || canPerformOnAllRefs(Permission.READ);
  }

  /** Is this project completely visible for replication? */
  boolean visibleForReplication() {
    return getCurrentUser() instanceof ReplicationUser
        && ((ReplicationUser) getCurrentUser()).isEverythingVisible();
  }

  /** Is this user a project owner? Ownership does not imply {@link #isVisible()} */
  public boolean isOwner() {
    return controlForRef(AccessSection.ALL).isOwner()
        || getCurrentUser().isAdministrator();
  }

  /** Does this user have ownership on at least one reference name? */
  public boolean isOwnerAnyRef() {
    return canPerformOnAnyRef(Permission.OWNER)
        || getCurrentUser().isAdministrator();
  }

  /** @return true if the user can upload to at least one reference */
  public boolean canPushToAtLeastOneRef() {
    return canPerformOnAnyRef(Permission.PUSH)
        || canPerformOnAnyRef(Permission.PUSH_TAG);
  }

<<<<<<< HEAD   (1118da Correct usage of @Nullable annotation in EventFactory)
  private boolean canPerformOnAnyRef(String permissionName) {
    final Set<AccountGroup.UUID> groups = user.getEffectiveGroups();
=======
  // TODO (anatol.pomazau): Try to merge this method with similar RefRightsForPattern#canPerform
  private boolean canPerformOnAnyRef(ApprovalCategory.Id actionId,
      short requireValue) {
    final Set<AccountGroup.Id> groups = getEffectiveUserGroups();
>>>>>>> BRANCH (019d2c Fix ChangeDetailFactory's invocation of PatchSetDetailFactor)

    for (AccessSection section : access()) {
      Permission permission = section.getPermission(permissionName);
      if (permission == null) {
        continue;
      }

      for (PermissionRule rule : permission.getRules()) {
        if (rule.getDeny()) {
          continue;
        }

        // Being in a group that was granted this permission is only an
        // approximation.  There might be overrides and doNotInherit
        // that would render this to be false.
        //
        if (groups.contains(rule.getGroup().getUUID())
            && controlForRef(section.getRefPattern()).canPerform(permissionName)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * @return the effective groups of the current user for this project
   */
  private Set<AccountGroup.Id> getEffectiveUserGroups() {
    final Set<AccountGroup.Id> userGroups = user.getEffectiveGroups();
    if (isOwner()) {
      final Set<AccountGroup.Id> userGroupsOnProject =
          new HashSet<AccountGroup.Id>(userGroups.size() + 1);
      userGroupsOnProject.addAll(userGroups);
      userGroupsOnProject.add(systemConfig.ownerGroupId);
      return Collections.unmodifiableSet(userGroupsOnProject);
    } else {
      return userGroups;
    }
  }

  private boolean canPerformOnAllRefs(String permission) {
    boolean canPerform = false;
    Set<String> patterns = allRefPatterns(permission);
    if (patterns.contains(AccessSection.ALL)) {
      // Only possible if granted on the pattern that
      // matches every possible reference.  Check all
      // patterns also have the permission.
      //
      for (final String pattern : patterns) {
        if (controlForRef(pattern).canPerform(permission)) {
          canPerform = true;
        } else {
          return false;
        }
      }
    }
    return canPerform;
  }

  private Set<String> allRefPatterns(String permissionName) {
    Set<String> all = new HashSet<String>();
    for (AccessSection section : access()) {
      Permission permission = section.getPermission(permissionName);
      if (permission != null) {
        all.add(section.getRefPattern());
      }
    }
    return all;
  }

  Collection<AccessSection> access() {
    if (access == null) {
      access = state.getAllAccessSections();
    }
    return access;
  }

  public boolean canRunUploadPack() {
    return isAnyIncludedIn(uploadGroups, getEffectiveUserGroups());
  }

  public boolean canRunReceivePack() {
    return isAnyIncludedIn(receiveGroups, getEffectiveUserGroups());
  }
}
