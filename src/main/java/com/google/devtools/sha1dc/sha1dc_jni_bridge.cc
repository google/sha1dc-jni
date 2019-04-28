// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include <jni.h>

#include <cstdlib>

#include "external/cr-marcstevens_sha1collisiondetection/lib/sha1.h"

extern "C" {

static jfieldID part1_field;
static jfieldID part2_field;
static jfieldID part3_field;

JNIEXPORT jint JNICALL JNI_OnLoad_SHA1DC(JavaVM* vm, void* reserved) {
  JNIEnv* env;
  if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
    return JNI_ERR;
  }
  jclass result_class =
      env->FindClass("com/google/devtools/sha1dc/Sha1DcBridge$Result");
  if (result_class == nullptr) {
    return JNI_ERR;
  }
  part1_field = env->GetFieldID(result_class, "part1", "J");
  part2_field = env->GetFieldID(result_class, "part2", "J");
  part3_field = env->GetFieldID(result_class, "part3", "I");
  if (part1_field == nullptr || part2_field == nullptr || part3_field == nullptr) {
    return JNI_ERR;
  }
  return JNI_VERSION_1_8;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
  return JNI_OnLoad_SHA1DC(vm, reserved);
}

JNIEXPORT jlong JNICALL
Java_com_google_devtools_sha1dc_Sha1DcBridge_nativeAlloc(JNIEnv*, jclass) {
  SHA1_CTX* ctx = new SHA1_CTX();
  if (ctx == nullptr) {
    return 0L;
  }
  SHA1DCInit(ctx);
  return reinterpret_cast<jlong>(ctx);
}

JNIEXPORT void JNICALL Java_com_google_devtools_sha1dc_Sha1DcBridge_nativeFree(
    JNIEnv*, jclass, jlong handle) {
  SHA1_CTX* ctx = reinterpret_cast<SHA1_CTX*>(handle);
  delete ctx;
}

JNIEXPORT jlong JNICALL
Java_com_google_devtools_sha1dc_Sha1DcBridge_nativeClone(JNIEnv*, jclass,
                                                         jlong handle) {
  SHA1_CTX* new_ctx = new SHA1_CTX();
  if (new_ctx == nullptr) {
    return 0L;
  }
  SHA1_CTX* ctx = reinterpret_cast<SHA1_CTX*>(handle);
  *new_ctx = *ctx;
  return reinterpret_cast<jlong>(new_ctx);
}

JNIEXPORT void JNICALL Java_com_google_devtools_sha1dc_Sha1DcBridge_nativeReset(
    JNIEnv*, jclass, jlong handle) {
  SHA1_CTX* ctx = reinterpret_cast<SHA1_CTX*>(handle);
  SHA1DCInit(ctx);
}

JNIEXPORT void JNICALL
Java_com_google_devtools_sha1dc_Sha1DcBridge_nativeConsumeByte(JNIEnv*, jclass,
                                                               jlong handle,
                                                               jbyte b) {
  char bs[1];
  bs[0] = b;
  SHA1_CTX* ctx = reinterpret_cast<SHA1_CTX*>(handle);
  SHA1DCUpdate(ctx, bs, 1);
}

JNIEXPORT void JNICALL
Java_com_google_devtools_sha1dc_Sha1DcBridge_nativeConsumeByteArray(
    JNIEnv* env, jclass, jlong handle, jbyteArray bs, jint offset, jint length,
    jboolean prefer_copy_over_gc_locking) {
  jbyte* in;
  if (prefer_copy_over_gc_locking) {
    in = env->GetByteArrayElements(bs, nullptr);
  } else {
    in = reinterpret_cast<jbyte*>(env->GetPrimitiveArrayCritical(bs, nullptr));
  }
  if (in == nullptr) {
    return;
  }

  SHA1_CTX* ctx = reinterpret_cast<SHA1_CTX*>(handle);
  SHA1DCUpdate(ctx, reinterpret_cast<char*>(in) + offset, length);

  if (prefer_copy_over_gc_locking) {
    env->ReleaseByteArrayElements(bs, in, JNI_ABORT);
  } else {
    env->ReleasePrimitiveArrayCritical(bs, in, JNI_ABORT);
  }
}

JNIEXPORT jboolean JNICALL
Java_com_google_devtools_sha1dc_Sha1DcBridge_nativeConsumeDirectByteBuffer(
    JNIEnv* env, jclass, jlong handle, jobject buffer, jint offset,
    jint length) {
  char* in = reinterpret_cast<char*>(env->GetDirectBufferAddress(buffer));
  if (in == nullptr) {
    return false;
  }
  SHA1_CTX* ctx = reinterpret_cast<SHA1_CTX*>(handle);
  SHA1DCUpdate(ctx, in + offset, length);
  return true;
}

JNIEXPORT jboolean JNICALL
Java_com_google_devtools_sha1dc_Sha1DcBridge_nativeProduceDigest(
    JNIEnv* env, jclass, jlong handle, jobject result) {
  SHA1_CTX* ctx = reinterpret_cast<SHA1_CTX*>(handle);
  unsigned char hash[20];
  if (SHA1DCFinal(hash, ctx)) {
    return true;
  }
  long part1 = (long(hash[0]) << 56) | (long(hash[1]) << 48) |
               (long(hash[2]) << 40) | (long(hash[3]) << 32) |
               (long(hash[4]) << 24) | (long(hash[5]) << 16) |
               (long(hash[6]) << 8) | long(hash[7]);
  long part2 = (long(hash[8]) << 56) | (long(hash[9]) << 48) |
               (long(hash[10]) << 40) | (long(hash[11]) << 32) |
               (long(hash[12]) << 24) | (long(hash[13]) << 16) |
               (long(hash[14]) << 8) | long(hash[15]);
  int part3 = (long(hash[16]) << 24) | (long(hash[17]) << 16) |
              (long(hash[18]) << 8) | long(hash[19]);

  env->SetLongField(result, part1_field, part1);
  env->SetLongField(result, part2_field, part2);
  env->SetIntField(result, part3_field, part3);
  return false;
}
}
