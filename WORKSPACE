load("@bazel_tools//tools/build_defs/repo:git.bzl", "new_git_repository")

maven_jar(
    name = "com_google_code_findbugs_jsr305",
    artifact = "com.google.code.findbugs:jsr305:1.3.9",
)

maven_jar(
    name = "com_google_guava_guava",
    artifact = "com.google.guava:guava:27.1-jre",
)

maven_jar(
    name = "org_eclipse_jgit_org_eclipse_jgit",
    artifact = "org.eclipse.jgit:org.eclipse.jgit:5.3.0.201903130848-r",
)

maven_jar(
    name = "junit_junit",
    artifact = "junit:junit:4.13-beta-2",
)

new_git_repository(
    name = "cr-marcstevens_sha1collisiondetection",
    build_file_content = """
cc_library(
    name = "sha1collisiondetection",
    visibility = ["//visibility:public"],
    srcs = glob(["lib/*.c"]),
    hdrs = glob(["lib/*.h"]),
)
""",
    commit = "16033998da4b273aebd92c84b1e1b12e4aaf7009",
    remote = "https://github.com/cr-marcstevens/sha1collisiondetection",
)

maven_jar(
    name = "org_slf4j_slf4j_api",
    artifact = "org.slf4j:slf4j-api:1.7.2",
)
