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

package com.google.gerrit.server.project;

import com.google.common.collect.ImmutableMap;
import com.google.gerrit.common.Nullable;
import com.google.gerrit.extensions.api.projects.TagInfo;
import com.google.gerrit.extensions.common.WebLinkInfo;
import com.google.gerrit.extensions.restapi.BadRequestException;
import com.google.gerrit.extensions.restapi.IdString;
import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.extensions.restapi.RestReadView;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.CommonConverters;
<<<<<<< HEAD   (34da15 Add support for tag web links)
import com.google.gerrit.server.CurrentUser;
=======
>>>>>>> BRANCH (433e1a Add support for tag web links)
import com.google.gerrit.server.WebLinks;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.git.SearchingChangeCacheImpl;
import com.google.gerrit.server.git.VisibleRefFilter;
import com.google.gerrit.server.permissions.PermissionBackend;
import com.google.gerrit.server.permissions.RefPermission;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.kohsuke.args4j.Option;

public class ListTags implements RestReadView<ProjectResource> {
  private final GitRepositoryManager repoManager;
  private final PermissionBackend permissionBackend;
  private final Provider<CurrentUser> user;
  private final VisibleRefFilter.Factory refFilterFactory;
  @Nullable private final SearchingChangeCacheImpl changeCache;
  private final WebLinks links;

  @Option(
    name = "--limit",
    aliases = {"-n"},
    metaVar = "CNT",
    usage = "maximum number of tags to list"
  )
  public void setLimit(int limit) {
    this.limit = limit;
  }

  @Option(
    name = "--start",
    aliases = {"-s"},
    metaVar = "CNT",
    usage = "number of tags to skip"
  )
  public void setStart(int start) {
    this.start = start;
  }

  @Option(
    name = "--match",
    aliases = {"-m"},
    metaVar = "MATCH",
    usage = "match tags substring"
  )
  public void setMatchSubstring(String matchSubstring) {
    this.matchSubstring = matchSubstring;
  }

  @Option(
    name = "--regex",
    aliases = {"-r"},
    metaVar = "REGEX",
    usage = "match tags regex"
  )
  public void setMatchRegex(String matchRegex) {
    this.matchRegex = matchRegex;
  }

  private int limit;
  private int start;
  private String matchSubstring;
  private String matchRegex;

  @Inject
  public ListTags(
      GitRepositoryManager repoManager,
<<<<<<< HEAD   (34da15 Add support for tag web links)
      PermissionBackend permissionBackend,
      Provider<CurrentUser> user,
      VisibleRefFilter.Factory refFilterFactory,
=======
      Provider<ReviewDb> dbProvider,
      TagCache tagCache,
      ChangeNotes.Factory changeNotesFactory,
>>>>>>> BRANCH (433e1a Add support for tag web links)
      @Nullable SearchingChangeCacheImpl changeCache,
      WebLinks webLinks) {
    this.repoManager = repoManager;
    this.permissionBackend = permissionBackend;
    this.user = user;
    this.refFilterFactory = refFilterFactory;
    this.changeCache = changeCache;
    this.links = webLinks;
  }

  @Override
  public List<TagInfo> apply(ProjectResource resource)
      throws IOException, ResourceNotFoundException, BadRequestException {
    List<TagInfo> tags = new ArrayList<>();

    PermissionBackend.ForProject perm = permissionBackend.user(user).project(resource.getNameKey());
    try (Repository repo = getRepository(resource.getNameKey());
        RevWalk rw = new RevWalk(repo)) {
      ProjectControl pctl = resource.getControl();
      Map<String, Ref> all =
          visibleTags(pctl, repo, repo.getRefDatabase().getRefs(Constants.R_TAGS));
      for (Ref ref : all.values()) {
<<<<<<< HEAD   (34da15 Add support for tag web links)
        tags.add(
            createTagInfo(perm.ref(ref.getName()), ref, rw, pctl.getProject().getNameKey(), links));
=======
        tags.add(createTagInfo(ref, rw, pctl.controlForRef(ref.getName()), pctl, links));
>>>>>>> BRANCH (433e1a Add support for tag web links)
      }
    }

    Collections.sort(
        tags,
        new Comparator<TagInfo>() {
          @Override
          public int compare(TagInfo a, TagInfo b) {
            return a.ref.compareTo(b.ref);
          }
        });

    return new RefFilter<TagInfo>(Constants.R_TAGS)
        .start(start)
        .limit(limit)
        .subString(matchSubstring)
        .regex(matchRegex)
        .filter(tags);
  }

  public TagInfo get(ProjectResource resource, IdString id)
      throws ResourceNotFoundException, IOException {
    try (Repository repo = getRepository(resource.getNameKey());
        RevWalk rw = new RevWalk(repo)) {
      String tagName = id.get();
      if (!tagName.startsWith(Constants.R_TAGS)) {
        tagName = Constants.R_TAGS + tagName;
      }
      Ref ref = repo.getRefDatabase().exactRef(tagName);
      ProjectControl pctl = resource.getControl();
      if (ref != null && !visibleTags(pctl, repo, ImmutableMap.of(ref.getName(), ref)).isEmpty()) {
<<<<<<< HEAD   (34da15 Add support for tag web links)
        return createTagInfo(
            permissionBackend
                .user(pctl.getUser())
                .project(resource.getNameKey())
                .ref(ref.getName()),
            ref,
            rw,
            pctl.getProject().getNameKey(),
            links);
=======
        return createTagInfo(ref, rw, pctl.controlForRef(ref.getName()), pctl, links);
>>>>>>> BRANCH (433e1a Add support for tag web links)
      }
    }
    throw new ResourceNotFoundException(id);
  }

  public static TagInfo createTagInfo(
<<<<<<< HEAD   (34da15 Add support for tag web links)
      PermissionBackend.ForRef perm,
      Ref ref,
      RevWalk rw,
      Project.NameKey projectName,
      WebLinks links)
=======
      Ref ref, RevWalk rw, RefControl control, ProjectControl pctl, WebLinks links)
>>>>>>> BRANCH (433e1a Add support for tag web links)
      throws MissingObjectException, IOException {
    RevObject object = rw.parseAny(ref.getObjectId());
<<<<<<< HEAD   (34da15 Add support for tag web links)
    boolean canDelete = perm.testOrFalse(RefPermission.DELETE);
    List<WebLinkInfo> webLinks = links.getTagLinks(projectName.get(), ref.getName());
=======
    List<WebLinkInfo> webLinks = links.getTagLinks(pctl.getProject().getName(), ref.getName());
>>>>>>> BRANCH (433e1a Add support for tag web links)
    if (object instanceof RevTag) {
      // Annotated or signed tag
      RevTag tag = (RevTag) object;
      PersonIdent tagger = tag.getTaggerIdent();
      return new TagInfo(
          ref.getName(),
          tag.getName(),
          tag.getObject().getName(),
          tag.getFullMessage().trim(),
          tagger != null ? CommonConverters.toGitPerson(tag.getTaggerIdent()) : null,
<<<<<<< HEAD   (34da15 Add support for tag web links)
          canDelete,
=======
          control.canDelete(),
>>>>>>> BRANCH (433e1a Add support for tag web links)
          webLinks.isEmpty() ? null : webLinks);
    }
    // Lightweight tag
    return new TagInfo(
        ref.getName(),
        ref.getObjectId().getName(),
<<<<<<< HEAD   (34da15 Add support for tag web links)
        canDelete,
=======
        control.canDelete(),
>>>>>>> BRANCH (433e1a Add support for tag web links)
        webLinks.isEmpty() ? null : webLinks);
  }

  private Repository getRepository(Project.NameKey project)
      throws ResourceNotFoundException, IOException {
    try {
      return repoManager.openRepository(project);
    } catch (RepositoryNotFoundException noGitRepository) {
      throw new ResourceNotFoundException();
    }
  }

  private Map<String, Ref> visibleTags(
      ProjectControl pctl, Repository repo, Map<String, Ref> tags) {
<<<<<<< HEAD   (34da15 Add support for tag web links)
    return refFilterFactory
        .create(pctl.getProjectState(), repo)
        .setShowMetadata(false)
=======
    return new VisibleRefFilter(
            tagCache, changeNotesFactory, changeCache, repo, pctl, dbProvider.get(), false)
>>>>>>> BRANCH (433e1a Add support for tag web links)
        .filter(tags, true);
  }
}
