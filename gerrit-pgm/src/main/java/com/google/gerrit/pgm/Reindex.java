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

package com.google.gerrit.pgm;

import static com.google.gerrit.server.schema.DataSourceProvider.Context.MULTI_USER;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gerrit.common.Die;
import com.google.gerrit.lifecycle.LifecycleManager;
import com.google.gerrit.lucene.LuceneIndexModule;
import com.google.gerrit.pgm.util.BatchProgramModule;
import com.google.gerrit.pgm.util.SiteProgram;
<<<<<<< HEAD   (4512fe Enable configurable 'maxBatchChanges' limit)
import com.google.gerrit.pgm.util.ThreadLimiter;
=======
import com.google.gerrit.reviewdb.client.AccountGroup;
>>>>>>> BRANCH (18a62d Update 2.10 release notes with information from 2.9.2)
import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.reviewdb.server.ReviewDb;
import com.google.gerrit.server.config.GerritServerConfig;
<<<<<<< HEAD   (4512fe Enable configurable 'maxBatchChanges' limit)
=======
import com.google.gerrit.server.config.GitReceivePackGroups;
import com.google.gerrit.server.config.GitUploadPackGroups;
import com.google.gerrit.server.git.GitModule;
import com.google.gerrit.server.git.MergeUtil;
import com.google.gerrit.server.git.WorkQueue;
import com.google.gerrit.server.git.validators.CommitValidationListener;
import com.google.gerrit.server.git.validators.CommitValidators;
import com.google.gerrit.server.group.GroupModule;
import com.google.gerrit.server.index.ChangeBatchIndexer;
>>>>>>> BRANCH (18a62d Update 2.10 release notes with information from 2.9.2)
import com.google.gerrit.server.index.ChangeIndex;
import com.google.gerrit.server.index.ChangeSchemas;
import com.google.gerrit.server.index.IndexCollection;
import com.google.gerrit.server.index.IndexModule;
import com.google.gerrit.server.index.IndexModule.IndexType;
<<<<<<< HEAD   (4512fe Enable configurable 'maxBatchChanges' limit)
import com.google.gerrit.server.index.SiteIndexer;
=======
import com.google.gerrit.server.mail.ReplacePatchSetSender;
import com.google.gerrit.server.notedb.NoteDbModule;
import com.google.gerrit.server.patch.PatchListCacheImpl;
import com.google.gerrit.server.project.ChangeControl;
import com.google.gerrit.server.project.CommentLinkInfo;
import com.google.gerrit.server.project.CommentLinkProvider;
import com.google.gerrit.server.project.ProjectCacheImpl;
import com.google.gerrit.server.project.ProjectControl;
import com.google.gerrit.server.project.ProjectState;
import com.google.gerrit.server.project.SectionSortCache;
import com.google.gerrit.server.query.change.ChangeData;
import com.google.gerrit.server.schema.DataSourceProvider;
import com.google.gerrit.server.schema.DataSourceType;
>>>>>>> BRANCH (18a62d Update 2.10 release notes with information from 2.9.2)
import com.google.gerrit.solr.SolrIndexModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.util.io.NullOutputStream;
import org.kohsuke.args4j.Option;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Reindex extends SiteProgram {
  @Option(name = "--threads", usage = "Number of threads to use for indexing")
  private int threads = Runtime.getRuntime().availableProcessors();

  @Option(name = "--schema-version",
      usage = "Schema version to reindex; default is most recent version")
  private Integer version;

  @Option(name = "--output", usage = "Prefix for output; path for local disk index, or prefix for remote index")
  private String outputBase;

  @Option(name = "--verbose", usage = "Output debug information for each change")
  private boolean verbose;

  @Option(name = "--dry-run", usage = "Dry run: don't write anything to index")
  private boolean dryRun;

  private Injector dbInjector;
  private Injector sysInjector;
  private ChangeIndex index;

  @Override
  public int run() throws Exception {
    mustHaveValidSite();
    dbInjector = createDbInjector(MULTI_USER);
    threads = ThreadLimiter.limitThreads(dbInjector, threads);
    checkNotSlaveMode();
    disableLuceneAutomaticCommit();
    if (version == null) {
      version = ChangeSchemas.getLatest().getVersion();
    }
    LifecycleManager dbManager = new LifecycleManager();
    dbManager.add(dbInjector);
    dbManager.start();

    sysInjector = createSysInjector();
    LifecycleManager sysManager = new LifecycleManager();
    sysManager.add(sysInjector);
    sysManager.start();

    index = sysInjector.getInstance(IndexCollection.class).getSearchIndex();
    int result = 0;
    try {
      index.markReady(false);
      index.deleteAll();
      result = indexAll();
      index.markReady(true);
    } catch (Exception e) {
      throw die(e.getMessage(), e);
    }
    sysManager.stop();
    dbManager.stop();
    return result;
  }

  private void checkNotSlaveMode() throws Die {
    Config cfg = dbInjector.getInstance(
        Key.get(Config.class, GerritServerConfig.class));
    if (cfg.getBoolean("container", "slave", false)) {
      throw die("Cannot run reindex in slave mode");
    }
  }

  private Injector createSysInjector() {
    List<Module> modules = Lists.newArrayList();
    Module changeIndexModule;
    switch (IndexModule.getIndexType(dbInjector)) {
      case LUCENE:
        changeIndexModule = new LuceneIndexModule(version, threads, outputBase);
        break;
      case SOLR:
        changeIndexModule = new SolrIndexModule(false, threads, outputBase);
        break;
      default:
        throw new IllegalStateException("unsupported index.type");
    }
    modules.add(changeIndexModule);
<<<<<<< HEAD   (4512fe Enable configurable 'maxBatchChanges' limit)
    modules.add(dbInjector.getInstance(BatchProgramModule.class));
=======
    modules.add(new ReviewDbModule());
    modules.add(new FactoryModule() {
      @SuppressWarnings("rawtypes")
      @Override
      protected void configure() {
        // Plugins are not loaded and we're just running through each change
        // once, so don't worry about cache removal.
        bind(new TypeLiteral<DynamicSet<CacheRemovalListener>>() {})
            .toInstance(DynamicSet.<CacheRemovalListener> emptySet());
        bind(new TypeLiteral<DynamicMap<Cache<?, ?>>>() {})
            .toInstance(DynamicMap.<Cache<?, ?>> emptyMap());
        bind(new TypeLiteral<List<CommentLinkInfo>>() {})
            .toProvider(CommentLinkProvider.class).in(SINGLETON);
        bind(String.class).annotatedWith(CanonicalWebUrl.class)
            .toProvider(CanonicalWebUrlProvider.class);
        bind(IdentifiedUser.class)
          .toProvider(Providers. <IdentifiedUser>of(null));
        bind(CurrentUser.class).to(IdentifiedUser.class);

        bind(new TypeLiteral<Set<AccountGroup.UUID>>() {})
            .annotatedWith(GitUploadPackGroups.class)
            .toInstance(Collections.<AccountGroup.UUID> emptySet());
        bind(new TypeLiteral<Set<AccountGroup.UUID>>() {})
            .annotatedWith(GitReceivePackGroups.class)
            .toInstance(Collections.<AccountGroup.UUID> emptySet());
        factory(ChangeControl.AssistedFactory.class);
        factory(ProjectControl.AssistedFactory.class);

        install(new DefaultCacheFactory.Module());
        install(new GroupModule());
        install(new PrologModule());
        install(AccountByEmailCacheImpl.module());
        install(AccountCacheImpl.module());
        install(GroupCacheImpl.module());
        install(GroupIncludeCacheImpl.module());
        install(ProjectCacheImpl.module());
        install(SectionSortCache.module());
        install(ChangeKindCacheImpl.module());
        factory(CapabilityControl.Factory.class);
        factory(ChangeData.Factory.class);
        factory(ProjectState.Factory.class);

        if (recheckMergeable) {
          install(new MergeabilityModule());
        } else {
          bind(MergeabilityChecker.class)
              .toProvider(Providers.<MergeabilityChecker> of(null));
        }
      }
    });

>>>>>>> BRANCH (18a62d Update 2.10 release notes with information from 2.9.2)
    return dbInjector.createChildInjector(modules);
  }

  private void disableLuceneAutomaticCommit() {
    Config cfg =
        dbInjector.getInstance(Key.get(Config.class, GerritServerConfig.class));
    if (IndexModule.getIndexType(dbInjector) == IndexType.LUCENE) {
      cfg.setLong("index", "changes_open", "commitWithin", -1);
      cfg.setLong("index", "changes_closed", "commitWithin", -1);
    }
  }

  private int indexAll() throws Exception {
    ReviewDb db = sysInjector.getInstance(ReviewDb.class);
    ProgressMonitor pm = new TextProgressMonitor();
    pm.start(1);
    pm.beginTask("Collecting projects", ProgressMonitor.UNKNOWN);
    Set<Project.NameKey> projects = Sets.newTreeSet();
    int changeCount = 0;
    try {
      for (Change change : db.changes().all()) {
        changeCount++;
        if (projects.add(change.getProject())) {
          pm.update(1);
        }
      }
    } finally {
      db.close();
    }
    pm.endTask();

    SiteIndexer batchIndexer =
        sysInjector.getInstance(SiteIndexer.class);
    SiteIndexer.Result result = batchIndexer.setNumChanges(changeCount)
        .setProgressOut(System.err)
        .setVerboseOut(verbose ? System.out : NullOutputStream.INSTANCE)
        .indexAll(index, projects);
    int n = result.doneCount() + result.failedCount();
    double t = result.elapsed(TimeUnit.MILLISECONDS) / 1000d;
    System.out.format("Reindexed %d changes in %.01fs (%.01f/s)\n", n, t, n/t);
    return result.success() ? 0 : 1;
  }
}
