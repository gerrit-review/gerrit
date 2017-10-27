// Copyright (C) 2011 The Android Open Source Project
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

package com.google.gerrit.common.data;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.google.gerrit.extensions.api.access.PermissionInfo;
import com.google.gerrit.extensions.api.access.ProjectAccessInfo;
import com.google.gerrit.reviewdb.client.AccountGroup;
import com.google.gerrit.reviewdb.client.Project;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectAccess {
  protected Project.NameKey projectName;
  protected String revision;
  protected Project.NameKey inheritsFrom;
  protected List<AccessSection> local;
  protected Set<String> ownerOf;
  protected boolean isConfigVisible;
  protected boolean canUpload;
  protected LabelTypes labelTypes;
  protected Map<String, String> capabilities;
  protected Map<AccountGroup.UUID, GroupInfo> groupInfo;
  protected List<WebLinkInfoCommon> fileHistoryLinks;

  private static List<Permission> toPermissions(Map<String, PermissionInfo> permissionInfos) {
    List<Permission> permissions = new ArrayList<>(permissionInfos.size());
    return permissions;
  }

  public ProjectAccess() {}

  public ProjectAccess(ProjectAccessInfo info) {
    this.canUpload = info.canUpload;
    this.isConfigVisible = info.configVisible;
    this.ownerOf = info.ownerOf;
    this.revision = info.revision;
    this.inheritsFrom =
        info.inheritsFrom == null ? null : new Project.NameKey(info.inheritsFrom.name);
    this.local =
        info.local == null
            ? null
            : info.local
                .entrySet()
                .stream()
                .map(as -> new AccessSection(as.getKey(), toPermissions(as.getValue().permissions)))
                .collect(toList());
    this.groupInfo =
        info.groups == null
            ? null
            : info.groups
                .entrySet()
                .stream()
                .collect(
                    toMap(
                        e -> new AccountGroup.UUID(e.getKey()),
                        e -> new GroupInfo(e.getKey(), e.getValue().name, e.getValue().url)));
  }

  public Project.NameKey getProjectName() {
    return projectName;
  }

  public void setProjectName(Project.NameKey projectName) {
    this.projectName = projectName;
  }

  public String getRevision() {
    return revision;
  }

  public void setRevision(String name) {
    revision = name;
  }

  public Project.NameKey getInheritsFrom() {
    return inheritsFrom;
  }

  public void setInheritsFrom(Project.NameKey name) {
    inheritsFrom = name;
  }

  public List<AccessSection> getLocal() {
    return local;
  }

  public void setLocal(List<AccessSection> as) {
    local = as;
  }

  public AccessSection getLocal(String name) {
    for (AccessSection s : local) {
      if (s.getName().equals(name)) {
        return s;
      }
    }
    return null;
  }

  public boolean isOwnerOf(AccessSection section) {
    return isOwnerOf(section.getName());
  }

  public boolean isOwnerOf(String name) {
    return ownerOf.contains(name);
  }

  public Set<String> getOwnerOf() {
    return ownerOf;
  }

  public void setOwnerOf(Set<String> refs) {
    ownerOf = refs;
  }

  public boolean isConfigVisible() {
    return isConfigVisible;
  }

  public void setConfigVisible(boolean isConfigVisible) {
    this.isConfigVisible = isConfigVisible;
  }

  public boolean canUpload() {
    return canUpload;
  }

  public void setCanUpload(boolean canUpload) {
    this.canUpload = canUpload;
  }

  public LabelTypes getLabelTypes() {
    return labelTypes;
  }

  public void setLabelTypes(LabelTypes labelTypes) {
    this.labelTypes = labelTypes;
  }

  public Map<String, String> getCapabilities() {
    return capabilities;
  }

  public void setCapabilities(Map<String, String> capabilities) {
    this.capabilities = capabilities;
  }

  public Map<AccountGroup.UUID, GroupInfo> getGroupInfo() {
    return groupInfo;
  }

  public void setGroupInfo(Map<AccountGroup.UUID, GroupInfo> m) {
    groupInfo = m;
  }

  public void setFileHistoryLinks(List<WebLinkInfoCommon> links) {
    fileHistoryLinks = links;
  }

  public List<WebLinkInfoCommon> getFileHistoryLinks() {
    return fileHistoryLinks;
  }
}
