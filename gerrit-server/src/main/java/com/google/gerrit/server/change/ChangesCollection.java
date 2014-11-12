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

<<<<<<< HEAD   (3e7054 Convert project REST API acceptance tests to use Google Trut)
=======
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
>>>>>>> BRANCH (fdb099 Merge branch 'stable-2.9' into stable-2.10)
import com.google.gerrit.extensions.registration.DynamicMap;
import com.google.gerrit.extensions.restapi.AcceptsPost;
import com.google.gerrit.extensions.restapi.IdString;
import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.RestCollection;
import com.google.gerrit.extensions.restapi.RestView;
import com.google.gerrit.extensions.restapi.TopLevelResource;
import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.server.ChangeUtil;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.index.ChangeIndexer;
import com.google.gerrit.server.project.ChangeControl;
import com.google.gerrit.server.project.NoSuchChangeException;
import com.google.gerrit.server.query.change.QueryChanges;
import com.google.gwtorm.server.OrmException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

<<<<<<< HEAD   (3e7054 Convert project REST API acceptance tests to use Google Trut)
=======
import java.io.IOException;
import java.util.Collections;
>>>>>>> BRANCH (fdb099 Merge branch 'stable-2.9' into stable-2.10)
import java.util.List;

@Singleton
public class ChangesCollection implements
    RestCollection<TopLevelResource, ChangeResource>,
    AcceptsPost<TopLevelResource> {
  private final Provider<CurrentUser> user;
  private final ChangeControl.GenericFactory changeControlFactory;
  private final Provider<QueryChanges> queryFactory;
  private final DynamicMap<RestView<ChangeResource>> views;
  private final ChangeUtil changeUtil;
  private final CreateChange createChange;
  private final ChangeIndexer changeIndexer;

  @Inject
  ChangesCollection(
      Provider<CurrentUser> user,
      ChangeControl.GenericFactory changeControlFactory,
      Provider<QueryChanges> queryFactory,
      DynamicMap<RestView<ChangeResource>> views,
<<<<<<< HEAD   (3e7054 Convert project REST API acceptance tests to use Google Trut)
      ChangeUtil changeUtil,
      CreateChange createChange) {
=======
      CreateChange createChange,
      ChangeIndexer changeIndexer) {
    this.db = dbProvider;
>>>>>>> BRANCH (fdb099 Merge branch 'stable-2.9' into stable-2.10)
    this.user = user;
    this.changeControlFactory = changeControlFactory;
    this.queryFactory = queryFactory;
    this.views = views;
    this.changeUtil = changeUtil;
    this.createChange = createChange;
    this.changeIndexer = changeIndexer;
  }

  @Override
  public QueryChanges list() {
    return queryFactory.get();
  }

  @Override
  public DynamicMap<RestView<ChangeResource>> views() {
    return views;
  }

  @Override
  public ChangeResource parse(TopLevelResource root, IdString id)
      throws ResourceNotFoundException, OrmException {
<<<<<<< HEAD   (3e7054 Convert project REST API acceptance tests to use Google Trut)
    List<Change> changes = changeUtil.findChanges(id.encoded());
=======
    List<Change> changes = findChanges(id.encoded());
    if (changes.isEmpty()) {
      Integer changeId = Ints.tryParse(id.get());
      if (changeId != null) {
        try {
          changeIndexer.delete(changeId);
        } catch (IOException e) {
          throw new ResourceNotFoundException(id.get(), e);
        }
      }
    }
>>>>>>> BRANCH (fdb099 Merge branch 'stable-2.9' into stable-2.10)
    if (changes.size() != 1) {
      throw new ResourceNotFoundException(id);
    }

    ChangeControl control;
    try {
      control = changeControlFactory.validateFor(changes.get(0), user.get());
    } catch (NoSuchChangeException e) {
      throw new ResourceNotFoundException(id);
    }
    return new ChangeResource(control);
  }

  public ChangeResource parse(Change.Id id)
      throws ResourceNotFoundException, OrmException {
    return parse(TopLevelResource.INSTANCE,
        IdString.fromUrl(Integer.toString(id.get())));
  }

  public ChangeResource parse(ChangeControl control) {
    return new ChangeResource(control);
  }

  @SuppressWarnings("unchecked")
  @Override
  public CreateChange post(TopLevelResource parent) throws RestApiException {
    return createChange;
  }
}
