java_test(
    name = "Sha1DcBridgeTest",
    srcs = ["Sha1DcBridgeTest.java"],
    data = [
        "testdata/shattered-1.pdf",
        "//src/main/java/com/google/devtools/sha1dc:sha1dc_jni_bridge.so",
    ],
    deps = [
        "//src/main/java/com/google/devtools/sha1dc:jni_bridge_java",
        "//third_party:junit",
    ],
)

java_test(
    name = "Sha1DcHasherTest",
    srcs = ["Sha1DcHasherTest.java"],
    data = [
        "//src/main/java/com/google/devtools/sha1dc:sha1dc_jni_bridge.so",
    ],
    deps = [
        "//src/main/java/com/google/devtools/sha1dc:sha1dc_guava",
        "//third_party:guava",
        "//third_party:jgit",
        "//third_party:junit",
    ],
)
