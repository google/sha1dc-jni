# sha1collisiondetection JNI bridge

[cr-marcstevens/sha1collisiondetection](https://github.com/cr-marcstevens/sha1collisiondetection)
is the library for calculating SHA-1 hash as well as detecting a hash collision.
This is a JNI bridge of this library.

This is not an official Google product. This is the author's hobby project. Not
for production use.

## Build & Test

Use [Bazel](https://bazel.build). I'm not sure how I can create a distributable
package with JNI. You would need to change the BUILD files in order to build
non-Linux environment.

Note that `Sha1DcHasher` itself doesn't load the native library. You would need
to load `sha1dc_jni_bridge.so` somewhere else.

## Speed comparison

This is not a serious benchmark. It's better to do your own benchmarking if
you're looking into this. With `bazel test ... -c opt` in the author's
workstation:

    Hashing 2MiB chunked 40MiB file 100 times (== hashing ~ 4GiB)

    JGit SHA1: 18152ms
    Sha1DcHasher (DirectBuffer): 9812ms
    Sha1DcHasher (ByteArray, Copy): 10937ms
    Sha1DcHasher (ByteArray, GCLock): 9831ms
    Standard SHA-1 Hasher (ByteArray): 12160ms

JGit's `SHA1` class and `Sha1DcHasher` class are collision detection enabled
implementations.
