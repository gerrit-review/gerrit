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

package com.google.gerrit.server.git.strategy;

import com.google.common.collect.ImmutableSet;
import com.google.gerrit.server.git.CodeReviewCommit;
import com.google.gerrit.server.git.IntegrationException;

<<<<<<< HEAD   (0ff3e3 AbstractSubmit: Add more assertions in submitWholeTopic)
import java.util.ArrayList;
=======
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

>>>>>>> BRANCH (3f1de3 Set version to 2.12.6)
import java.util.Collection;
import java.util.List;

public class MergeIfNecessary extends SubmitStrategy {
  MergeIfNecessary(SubmitStrategy.Arguments args) {
    super(args);
  }

  @Override
  public List<SubmitStrategyOp> buildOps(
      Collection<CodeReviewCommit> toMerge) throws IntegrationException {
    List<CodeReviewCommit> sorted =
        args.mergeUtil.reduceToMinimalMerge(args.mergeSorter, toMerge);
    List<SubmitStrategyOp> ops = new ArrayList<>(sorted.size());
    CodeReviewCommit firstFastForward = args.mergeUtil.getFirstFastForward(
          args.mergeTip.getInitialTip(), args.rw, sorted);
    if (firstFastForward != null &&
        !firstFastForward.equals(args.mergeTip.getInitialTip())) {
      ops.add(new FastForwardOp(args, firstFastForward));
    }

    // For every other commit do a pair-wise merge.
    while (!sorted.isEmpty()) {
      CodeReviewCommit n = sorted.remove(0);
      ops.add(new MergeOneOp(args, n));
    }
<<<<<<< HEAD   (0ff3e3 AbstractSubmit: Add more assertions in submitWholeTopic)
    return ops;
=======
    RevCommit initialTip = mergeTip.getInitialTip();
    args.mergeUtil.markCleanMerges(args.rw, args.canMergeFlag, branchTip,
        initialTip == null ? ImmutableSet.<RevCommit> of()
            : ImmutableSet.of(initialTip));
    setRefLogIdent();
    return mergeTip;
>>>>>>> BRANCH (3f1de3 Set version to 2.12.6)
  }

  static boolean dryRun(SubmitDryRun.Arguments args,
      CodeReviewCommit mergeTip, CodeReviewCommit toMerge)
      throws IntegrationException {
    return args.mergeUtil.canFastForward(
          args.mergeSorter, mergeTip, args.rw, toMerge)
        || args.mergeUtil.canMerge(
          args.mergeSorter, args.repo, mergeTip, toMerge);
  }
}
