# Maven style API version (e.g. '2.x-SNAPSHOT').
# Used by :api_install and :api_deploy targets
# when talking to the destination repository.
#
<<<<<<< HEAD   (7af1d0 Merge "Split Lucene & Elastic query tests into separate rule)
GERRIT_VERSION = "2.16-SNAPSHOT"

def check_version(x):
    if native.bazel_version == "":
        # experimental / unreleased Bazel.
        return
    if native.bazel_version < x:
        fail("\nERROR: Current Bazel version is {}, expected at least {}\n".format(native.bazel_version, x))
=======
GERRIT_VERSION = "2.15-rc2"
>>>>>>> BRANCH (6f1730 Merge "Allow user selection of file path in diff view" into )
