load("//tools/bzl:maven_jar.bzl", "GERRIT", "MAVEN_LOCAL", "MAVEN_CENTRAL", "maven_jar")

<<<<<<< HEAD   (aae6da Upgrade commons-compress to 1.13)
_JGIT_VERS = "4.6.0.201612231935-r.30-gd3148f300"
=======
_JGIT_VERS = "4.6.1.201703071140-r.149-g61f830d3a"
>>>>>>> BRANCH (42a514 Merge changes Ic4f730d5,I4c3642ea into stable-2.14)

_DOC_VERS = "4.6.0.201612231935-r" # Set to _JGIT_VERS unless using a snapshot

JGIT_DOC_URL = "http://download.eclipse.org/jgit/site/" + _DOC_VERS + "/apidocs"

_JGIT_REPO = GERRIT # Leave here even if set to MAVEN_CENTRAL.

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
<<<<<<< HEAD   (aae6da Upgrade commons-compress to 1.13)
        sha1 = "a2b5970b853f8fee64589fc1103c0ceb7677ba63",
        src_sha1 = "765f955774c36c226aa41fba7c20119451de2db7",
        unsign = True,
    )
    maven_jar(
        name = "jgit_servlet",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.http.server:" + _JGIT_VERS,
        repository = _JGIT_REPO,
        sha1 = "d3aa54bd610db9a5c246aa8fef13989982c98628",
        unsign = True,
    )
    maven_jar(
        name = "jgit_archive",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.archive:" + _JGIT_VERS,
        repository = _JGIT_REPO,
        sha1 = "a728cf277396f1227c5a8dffcf5dee0188fc0821",
    )
    maven_jar(
        name = "jgit_junit",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.junit:" + _JGIT_VERS,
        repository = _JGIT_REPO,
        sha1 = "6c2b2f192c95d25a2e1576aee5d1169dd8bd2266",
=======
        sha1 = "dbb390b827b968558342e882e0c9b90e1ed037a2",
        src_sha1 = "05d8939d08fe75a080fbf84f3163df5127950985",
        unsign = True,
    )
    maven_jar(
        name = "jgit_servlet",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.http.server:" + _JGIT_VERS,
        repository = _JGIT_REPO,
        sha1 = "136026aa28b065d04194cadcb3371b5a3f6c7235",
        unsign = True,
    )
    maven_jar(
        name = "jgit_archive",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.archive:" + _JGIT_VERS,
        repository = _JGIT_REPO,
        sha1 = "8be5fa1000cf66ff3deae257bb29870c93f83363",
    )
    maven_jar(
        name = "jgit_junit",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.junit:" + _JGIT_VERS,
        repository = _JGIT_REPO,
        sha1 = "d6a6c49b137a7f1a30ec55a228826d9146c0eba4",
>>>>>>> BRANCH (42a514 Merge changes Ic4f730d5,I4c3642ea into stable-2.14)
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
