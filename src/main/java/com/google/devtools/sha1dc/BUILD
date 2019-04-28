java_library(
    name = "sha1dc_guava",
    srcs = [
        "Sha1DcHasher.java",
    ],
    visibility = ["//visibility:public"],
    exports = [
        ":jni_bridge_java",
    ],
    deps = [
        ":jni_bridge_java",
        "//third_party:guava",
    ],
)

cc_binary(
    name = "sha1dc_jni_bridge.so",
    linkshared = 1,
    visibility = ["//visibility:public"],
    deps = [":sha1dc_jni_bridge_lib"],
)

java_library(
    name = "jni_bridge_java",
    srcs = [
        "CollisionDetectedException.java",
        "Sha1DcBridge.java",
    ],
    visibility = ["//src/test/java/com/google/devtools/sha1dc:__pkg__"],
)

cc_library(
    name = "sha1dc_jni_bridge_lib",
    srcs = [
        "sha1dc_jni_bridge.cc",
    ],
    deps = [
        "//:jni",
        "@cr-marcstevens_sha1collisiondetection//:sha1collisiondetection",
    ],
)
