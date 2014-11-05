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

package com.google.gerrit.acceptance.git;

<<<<<<< HEAD   (c31a38 Fix rendering of links in license documentation)
=======
import org.eclipse.jgit.api.errors.GitAPIException;
>>>>>>> BRANCH (c303a3 Merge branch 'stable-2.9' into stable-2.10)
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Before;
<<<<<<< HEAD   (c31a38 Fix rendering of links in license documentation)
=======

import java.io.IOException;
import java.net.URISyntaxException;
>>>>>>> BRANCH (c303a3 Merge branch 'stable-2.9' into stable-2.10)

public class HttpPushForReviewIT extends AbstractPushForReview {
  @Before
<<<<<<< HEAD   (c31a38 Fix rendering of links in license documentation)
  public void selectHttpUrl() throws Exception {
=======
  public void selectHttpUrl() throws GitAPIException, IOException, URISyntaxException {
>>>>>>> BRANCH (c303a3 Merge branch 'stable-2.9' into stable-2.10)
    CredentialsProvider.setDefault(new UsernamePasswordCredentialsProvider(
        admin.username, admin.httpPassword));
    selectProtocol(Protocol.HTTP);
  }
}
