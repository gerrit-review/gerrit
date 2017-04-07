load("//tools/bzl:maven_jar.bzl", "GERRIT", "MAVEN_LOCAL", "MAVEN_CENTRAL", "maven_jar")

<<<<<<< HEAD   (cb9a59 Merge "Consistenly format all Java labels as UPPERCASE_UNDER)
_JGIT_VERS = "4.7.0.201704051617-r.15-gc4e952109"
=======
_JGIT_VERS = "4.7.0.201704051617-r"
>>>>>>> BRANCH (c0c019 Merge "Upgrade JGit to 4.7.0.201704051617-r" into stable-2.1)

<<<<<<< HEAD   (cb9a59 Merge "Consistenly format all Java labels as UPPERCASE_UNDER)
_DOC_VERS = "4.7.0.201704051617-r" # Set to _JGIT_VERS unless using a snapshot
=======
_DOC_VERS = _JGIT_VERS # Set to _JGIT_VERS unless using a snapshot
>>>>>>> BRANCH (c0c019 Merge "Upgrade JGit to 4.7.0.201704051617-r" into stable-2.1)

JGIT_DOC_URL = "http://download.eclipse.org/jgit/site/" + _DOC_VERS + "/apidocs"

_JGIT_REPO = MAVEN_CENTRAL # Leave here even if set to MAVEN_CENTRAL.

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
<<<<<<< HEAD   (cb9a59 Merge "Consistenly format all Java labels as UPPERCASE_UNDER)
        sha1 = "875277521153030e2bdab12bf602b740232b2b28",
        src_sha1 = "f9adcd3ef0f77c5db16569771f95bc0142c36f46",
=======
        sha1 = "99be65d1827276b97d4f51668b60f4a38f282bda",
        src_sha1 = "de519d6f352aaf12e4c65f7590591326ac24d2e8",
>>>>>>> BRANCH (c0c019 Merge "Upgrade JGit to 4.7.0.201704051617-r" into stable-2.1)
        unsign = True,
    )
    maven_jar(
        name = "jgit_servlet",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.http.server:" + _JGIT_VERS,
        repository = _JGIT_REPO,
<<<<<<< HEAD   (cb9a59 Merge "Consistenly format all Java labels as UPPERCASE_UNDER)
        sha1 = "e1037f50696a6e19fb5d30f9d44cb31e3c5fe8b0",
=======
        sha1 = "72fa98ebf001aadd3dcb99ca8f7fcd90983da56b",
>>>>>>> BRANCH (c0c019 Merge "Upgrade JGit to 4.7.0.201704051617-r" into stable-2.1)
        unsign = True,
    )
    maven_jar(
        name = "jgit_archive",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.archive:" + _JGIT_VERS,
        repository = _JGIT_REPO,
<<<<<<< HEAD   (cb9a59 Merge "Consistenly format all Java labels as UPPERCASE_UNDER)
        sha1 = "660bc82c9ff3c33249d269860d9793e830d6c374",
=======
        sha1 = "f825504a903dfe8d3daa61d6ab5c26fbad92c954",
>>>>>>> BRANCH (c0c019 Merge "Upgrade JGit to 4.7.0.201704051617-r" into stable-2.1)
    )
    maven_jar(
        name = "jgit_junit",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.junit:" + _JGIT_VERS,
        repository = _JGIT_REPO,
<<<<<<< HEAD   (cb9a59 Merge "Consistenly format all Java labels as UPPERCASE_UNDER)
        sha1 = "c2b28646cc2531df947a9e0f73fa9c415567b05e",
=======
        sha1 = "e0dbc6d3568b2ba65c9421af2f06e4158a624bcb",
>>>>>>> BRANCH (c0c019 Merge "Upgrade JGit to 4.7.0.201704051617-r" into stable-2.1)
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
