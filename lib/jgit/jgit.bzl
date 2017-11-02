load("//tools/bzl:maven_jar.bzl", "GERRIT", "MAVEN_LOCAL", "MAVEN_CENTRAL", "maven_jar")

<<<<<<< HEAD   (7ea0ed Update JGit to 4.9.0.201710071750-r.30-g651e17bac)
_JGIT_VERS = "4.9.0.201710071750-r.30-g651e17bac"
=======
_JGIT_VERS = "4.9.0.201710071750-r.8-g678c99c05"
>>>>>>> BRANCH (884301 Merge changes from topic "notedb-pack-inserter" into stable-)

_DOC_VERS = "4.9.0.201710071750-r"  # Set to _JGIT_VERS unless using a snapshot

JGIT_DOC_URL = "http://download.eclipse.org/jgit/site/" + _DOC_VERS + "/apidocs"

_JGIT_REPO = GERRIT  # Leave here even if set to MAVEN_CENTRAL.

# set this to use a local version.
# "/home/<user>/projects/jgit"
LOCAL_JGIT_REPO = ""

def jgit_repos():
  if LOCAL_JGIT_REPO:
    native.local_repository(
        name = "jgit",
        path = LOCAL_JGIT_REPO,
    )
  else:
    jgit_maven_repos()

def jgit_maven_repos():
    maven_jar(
        name = "jgit_lib",
        artifact = "org.eclipse.jgit:org.eclipse.jgit:" + _JGIT_VERS,
        repository = _JGIT_REPO,
<<<<<<< HEAD   (7ea0ed Update JGit to 4.9.0.201710071750-r.30-g651e17bac)
        sha1 = "5f0ab69e6dd369c9efcf2fc23f7ae923113d6d3d",
        src_sha1 = "8dcfff9612815fc1b2bb0e8d032c9b99a6ed89e4",
=======
        sha1 = "845fcaabaaf84f0924f78971def3adc962d3a69e",
        src_sha1 = "8cc0627939d1bcd8fb313fd6bed03edfecc5011f",
>>>>>>> BRANCH (884301 Merge changes from topic "notedb-pack-inserter" into stable-)
        unsign = True,
    )
    maven_jar(
        name = "jgit_servlet",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.http.server:" + _JGIT_VERS,
        repository = _JGIT_REPO,
<<<<<<< HEAD   (7ea0ed Update JGit to 4.9.0.201710071750-r.30-g651e17bac)
        sha1 = "58629a74278c502f7fb6926b9a4b483da46072b5",
=======
        sha1 = "a0cb65ef0d9db387b010337b44ac4710be52b21e",
>>>>>>> BRANCH (884301 Merge changes from topic "notedb-pack-inserter" into stable-)
        unsign = True,
    )
    maven_jar(
        name = "jgit_archive",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.archive:" + _JGIT_VERS,
        repository = _JGIT_REPO,
<<<<<<< HEAD   (7ea0ed Update JGit to 4.9.0.201710071750-r.30-g651e17bac)
        sha1 = "a31a98bac30977bbad867aa89a0338d3260eb46a",
=======
        sha1 = "32c85ea349d19a0129d43ceda021a3bdd41a8242",
>>>>>>> BRANCH (884301 Merge changes from topic "notedb-pack-inserter" into stable-)
    )
    maven_jar(
        name = "jgit_junit",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.junit:" + _JGIT_VERS,
        repository = _JGIT_REPO,
<<<<<<< HEAD   (7ea0ed Update JGit to 4.9.0.201710071750-r.30-g651e17bac)
        sha1 = "358989980e6faf77c3af955f5eea268020a6407d",
=======
        sha1 = "549c3b338d66c2f8575394252a39e69d7fe71969",
>>>>>>> BRANCH (884301 Merge changes from topic "notedb-pack-inserter" into stable-)
        unsign = True,
    )

def jgit_dep(name):
  mapping = {
      "@jgit_junit//jar": "@jgit//org.eclipse.jgit.junit:junit",
      "@jgit_lib//jar:src": "@jgit//org.eclipse.jgit:libjgit-src.jar",
      "@jgit_lib//jar": "@jgit//org.eclipse.jgit:jgit",
      "@jgit_servlet//jar":"@jgit//org.eclipse.jgit.http.server:jgit-servlet",
      "@jgit_archive//jar": "@jgit//org.eclipse.jgit.archive:jgit-archive",
  }

  if LOCAL_JGIT_REPO:
    return mapping[name]
  else:
    return name
