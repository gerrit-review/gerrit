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

package com.google.gerrit.server.change;

import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.extensions.restapi.RestReadView;
import com.google.gerrit.reviewdb.client.Patch;
import com.google.gerrit.server.ChangeUtil;
import com.google.gerrit.server.project.NoSuchChangeException;
import com.google.gwtorm.server.OrmException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;

import java.io.IOException;

@Singleton
public class GetContent implements RestReadView<FileResource> {
  private final FileContentUtil fileContentUtil;
  private final ChangeUtil changeUtil;

  @Inject
  GetContent(FileContentUtil fileContentUtil,
      ChangeUtil changeUtil) {
    this.fileContentUtil = fileContentUtil;
    this.changeUtil = changeUtil;
  }

  @Override
  public BinaryResult apply(FileResource rsrc)
<<<<<<< HEAD   (14422a Update reviewnotes plugin to latest revision)
      throws ResourceNotFoundException, IOException, NoSuchChangeException,
      OrmException {
    String path = rsrc.getPatchKey().get();
    if (Patch.COMMIT_MSG.equals(path)) {
      String msg = changeUtil.getMessage(rsrc.getRevision().getChange());
      return BinaryResult.create(msg)
          .setContentType(FileContentUtil.TEXT_X_GERRIT_COMMIT_MESSAGE)
          .base64();
=======
      throws ResourceNotFoundException, IOException {
    return apply(rsrc.getRevision().getControl().getProject().getNameKey(),
        rsrc.getRevision().getPatchSet().getRevision().get(),
        rsrc.getPatchKey().get());
  }

  public BinaryResult apply(Project.NameKey project, String revstr, String path)
      throws ResourceNotFoundException, IOException {
    Repository repo = repoManager.openRepository(project);
    try {
      RevWalk rw = new RevWalk(repo);
      try {
        RevCommit commit =
            rw.parseCommit(repo.resolve(revstr));
        TreeWalk tw =
            TreeWalk.forPath(rw.getObjectReader(), path,
                commit.getTree().getId());
        if (tw == null) {
          throw new ResourceNotFoundException();
        }
        try {
          final ObjectLoader object = repo.open(tw.getObjectId(0));
          @SuppressWarnings("resource")
          BinaryResult result = new BinaryResult() {
            @Override
            public void writeTo(OutputStream os) throws IOException {
              object.copyTo(os);
            }
          };
          return result.setContentLength(object.getSize()).base64();
        } finally {
          tw.close();
        }
      } finally {
        rw.close();
      }
    } finally {
      repo.close();
>>>>>>> BRANCH (6b870d Bump JGit to v4.0.0.201506090130-r)
    }
    return fileContentUtil.getContent(
        rsrc.getRevision().getControl().getProjectControl().getProjectState(),
        ObjectId.fromString(rsrc.getRevision().getPatchSet().getRevision().get()),
        path);
  }
}
