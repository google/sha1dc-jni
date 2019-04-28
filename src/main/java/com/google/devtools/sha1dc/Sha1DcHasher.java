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

import com.google.common.hash.Funnel;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/** SHA-1 {@link Hasher} implementation with a collision detection. */
public final class Sha1DcHasher implements Hasher {
  private final Sha1DcBridge bridge;

  public Sha1DcHasher() {
    this(true);
  }

  public Sha1DcHasher(boolean preferCopyOverGcLocking) {
    this.bridge = new Sha1DcBridge(preferCopyOverGcLocking);
  }

  @Override
  public HashCode hash() {
    return HashCode.fromBytes(bridge.produceDigest());
  }

  @Override
  @Deprecated
  public int hashCode() {
    return hash().asInt();
  }

  @Override
  public Sha1DcHasher putByte(byte b) {
    bridge.consumeByte(b);
    return this;
  }

  @Override
  public Sha1DcHasher putBytes(byte[] bs) {
    bridge.consumeByteArray(bs, 0, bs.length);
    return this;
  }

  @Override
  public Sha1DcHasher putBytes(byte[] bs, int offset, int length) {
    bridge.consumeByteArray(bs, offset, length);
    return this;
  }

  @Override
  public Sha1DcHasher putBytes(ByteBuffer buffer) {
    bridge.consumeByteBuffer(buffer);
    return this;
  }

  @Override
  public Sha1DcHasher putBoolean(boolean b) {
    return putByte(b ? (byte) 1 : (byte) 0);
  }

  @Override
  public Sha1DcHasher putDouble(double d) {
    return putLong(Double.doubleToRawLongBits(d));
  }

  @Override
  public Sha1DcHasher putFloat(float f) {
    return putInt(Float.floatToRawIntBits(f));
  }

  @Override
  public Sha1DcHasher putUnencodedChars(CharSequence charSequence) {
    for (int i = 0, len = charSequence.length(); i < len; i++) {
      putChar(charSequence.charAt(i));
    }
    return this;
  }

  @Override
  public Sha1DcHasher putString(CharSequence charSequence, Charset charset) {
    return putBytes(charSequence.toString().getBytes(charset));
  }

  @Override
  public Sha1DcHasher putShort(short s) {
    putByte((byte) s);
    putByte((byte) (s >>> 8));
    return this;
  }

  @Override
  public Sha1DcHasher putInt(int i) {
    putByte((byte) i);
    putByte((byte) (i >>> 8));
    putByte((byte) (i >>> 16));
    putByte((byte) (i >>> 24));
    return this;
  }

  @Override
  public Sha1DcHasher putLong(long l) {
    for (int i = 0; i < 64; i += 8) {
      putByte((byte) (l >>> i));
    }
    return this;
  }

  @Override
  public Sha1DcHasher putChar(char c) {
    putByte((byte) c);
    putByte((byte) (c >>> 8));
    return this;
  }

  @Override
  public <T> Sha1DcHasher putObject(T instance, Funnel<? super T> funnel) {
    funnel.funnel(instance, this);
    return this;
  }
}
