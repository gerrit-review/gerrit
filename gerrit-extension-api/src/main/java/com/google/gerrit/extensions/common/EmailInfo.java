// Copyright (C) 2017 The Android Open Source Project
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

package com.google.gerrit.extensions.common;

import com.google.common.base.MoreObjects;
import java.util.Objects;

public class EmailInfo {
  public String email;
  public Boolean preferred;
  public Boolean pendingConfirmation;

  public EmailInfo() {}

  public EmailInfo(String email, Boolean preferred, Boolean pendingConfirmation) {
    this.email = email;
    this.preferred = preferred;
    this.pendingConfirmation = pendingConfirmation;
  }

  public void preferred(String e) {
    this.preferred = e != null && e.equals(email) ? true : null;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof EmailInfo) {
      EmailInfo a = (EmailInfo) o;
      return (Objects.equals(a.email, email))
          && (Objects.equals(a.preferred, preferred))
          && (Objects.equals(a.pendingConfirmation, pendingConfirmation));
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(email, preferred, pendingConfirmation);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("email", email)
        .add("preferred", preferred)
        .add("pendingConfirmation", pendingConfirmation)
        .toString();
  }
}
