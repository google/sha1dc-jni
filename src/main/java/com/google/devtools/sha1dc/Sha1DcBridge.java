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

package com.google.devtools.sha1dc;

import java.nio.ByteBuffer;

final class Sha1DcBridge {
  private long handle;
  private boolean preferCopyOverGcLocking;

  Sha1DcBridge(boolean preferCopyOverGcLocking) {
    this(nativeAlloc(), preferCopyOverGcLocking);
  }

  private Sha1DcBridge(long handle, boolean preferCopyOverGcLocking) {
    this.handle = handle;
    this.preferCopyOverGcLocking = preferCopyOverGcLocking;
  }

  @Override
  protected final void finalize() {
    if (handle != 0) {
      nativeFree(handle);
      handle = 0;
    }
  }

  @Override
  public Object clone() {
    return new Sha1DcBridge(nativeClone(handle), preferCopyOverGcLocking);
  }

  void setPreferCopyOverGcLocking(boolean preferCopyOverGcLocking) {
    this.preferCopyOverGcLocking = preferCopyOverGcLocking;
  }

  void reset() {
    nativeReset(handle);
  }

  void consumeByte(byte b) {
    nativeConsumeByte(handle, b);
  }

  void consumeByteArray(byte[] bs, int offset, int length) {
    nativeConsumeByteArray(handle, bs, offset, length, preferCopyOverGcLocking);
  }

  void consumeByteBuffer(ByteBuffer buffer) {
    if (buffer.isDirect()) {
      if (nativeConsumeDirectByteBuffer(handle, buffer, buffer.position(), buffer.remaining())) {
        return;
      }
      throw new AssertionError("SHA1DC JNI code cannot process the DirectBuffer");
    }
    nativeConsumeByteArray(
        handle,
        buffer.array(),
        buffer.arrayOffset() + buffer.position(),
        buffer.remaining(),
        preferCopyOverGcLocking);
  }

  byte[] produceDigest() {
    Result result = new Result();
    if (nativeProduceDigest(handle, result)) {
      throw new CollisionDetectedException();
    }
    byte[] ret = new byte[20];
    for (int i = 7; i >= 0; i--) {
      ret[i] = (byte) (result.part1 & 0xffL);
      result.part1 >>= 8;
    }
    for (int i = 7; i >= 0; i--) {
      ret[i + 8] = (byte) (result.part2 & 0xffL);
      result.part2 >>= 8;
    }
    for (int i = 3; i >= 0; i--) {
      ret[i + 16] = (byte) (result.part3 & 0xff);
      result.part3 >>= 8;
    }
    return ret;
  }

  private static native long nativeAlloc();

  private static native void nativeFree(long handle);

  private static native long nativeClone(long handle);

  private static native void nativeReset(long handle);

  private static native void nativeConsumeByte(long handle, byte b);

  private static native void nativeConsumeByteArray(
      long handle, byte[] bs, int offset, int length, boolean preferCopyOverGcLocking);

  private static native boolean nativeConsumeDirectByteBuffer(
      long handle, ByteBuffer buffer, int offset, int length);

  /** Returns true if the collision found. */
  private static native boolean nativeProduceDigest(long handle, Result result);

  static class Result {
    long part1;
    long part2;
    int part3;
  }
}
