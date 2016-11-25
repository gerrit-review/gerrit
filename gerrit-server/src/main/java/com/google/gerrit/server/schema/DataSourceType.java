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

package com.google.gerrit.server.schema;

import java.io.IOException;


/** Abstraction of a supported database platform */
public interface DataSourceType {

  String getDriver();

  String getUrl();

<<<<<<< HEAD   (4d8ec4 Git-ignore .primary_build_tool and .gwt_work_dir)
  boolean usePool();
=======
  public String getValidationQuery();

  public boolean usePool();
>>>>>>> BRANCH (96bbab Fix DB connection pool verification)

  /**
   * Return a ScriptRunner that runs the index script. Must not return
   * {@code null}, but may return a ScriptRunner that does nothing.
   *
   * @throws IOException
   */
  ScriptRunner getIndexScript() throws IOException;
}
