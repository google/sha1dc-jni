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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class Sha1DcBridgeTest {
  // SHA-1 for "test".
  private static final String HASH_TEST = "A94A8FE5CCB19BA61C4C0873D391E987982FBBD3";
  // SHA-1 for "test_hash".
  private static final String HASH_TEST_HASH = "327D106BF608B1F63BF5CBC5D1B6EA2D6836B446";
  private final Sha1DcBridge bridge = new Sha1DcBridge(true);

  static {
    Path libraryPath =
        Paths.get(System.getenv("TEST_SRCDIR"))
            .resolve("__main__/src/main/java/com/google/devtools/sha1dc/sha1dc_jni_bridge.so");
    System.load(libraryPath.toString());
  }

  @Test
  public void testByte() {
    bridge.consumeByte((byte) 't');
    bridge.consumeByte((byte) 'e');
    bridge.consumeByte((byte) 's');
    bridge.consumeByte((byte) 't');
    assertEquals(HASH_TEST, toHexString(bridge.produceDigest()));
  }

  @Test
  public void testByteArray() {
    byte[] bs = "test".getBytes(StandardCharsets.UTF_8);
    bridge.consumeByteArray(bs, 0, bs.length);
    assertEquals(HASH_TEST, toHexString(bridge.produceDigest()));
  }

  @Test
  public void testByteArrayWithLock() {
    bridge.setPreferCopyOverGcLocking(false);

    byte[] bs = "test".getBytes(StandardCharsets.UTF_8);
    bridge.consumeByteArray(bs, 0, bs.length);
    assertEquals(HASH_TEST, toHexString(bridge.produceDigest()));
  }

  @Test
  public void testByteArrayWithOffset() {
    byte[] bs = "123test456".getBytes(StandardCharsets.UTF_8);
    bridge.consumeByteArray(bs, 3, 4);
    assertEquals(HASH_TEST, toHexString(bridge.produceDigest()));
  }

  @Test
  public void testHeapBuffer() {
    ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.put("test".getBytes(StandardCharsets.UTF_8));
    buffer.rewind();

    bridge.consumeByteBuffer(buffer);
    assertEquals(HASH_TEST, toHexString(bridge.produceDigest()));
  }

  @Test
  public void testHeapBufferWithOffset() {
    ByteBuffer buffer = ByteBuffer.allocate(10);
    buffer.put("123test456".getBytes(StandardCharsets.UTF_8));
    buffer.rewind();
    buffer.position(3).limit(7);

    bridge.consumeByteBuffer(buffer);
    assertEquals(HASH_TEST, toHexString(bridge.produceDigest()));
  }

  @Test
  public void testDirectBuffer() {
    ByteBuffer buffer = ByteBuffer.allocateDirect(4);
    buffer.put("test".getBytes(StandardCharsets.UTF_8));
    buffer.rewind();

    bridge.consumeByteBuffer(buffer);
    assertEquals(HASH_TEST, toHexString(bridge.produceDigest()));
  }

  @Test
  public void testDirectBufferWithOffset() {
    ByteBuffer buffer = ByteBuffer.allocateDirect(10);
    buffer.put("123test456".getBytes(StandardCharsets.UTF_8));
    buffer.rewind();
    buffer.position(3).limit(7);

    bridge.consumeByteBuffer(buffer);
    assertEquals(HASH_TEST, toHexString(bridge.produceDigest()));
  }

  @Test
  public void testReset() {
    byte[] bs = "test".getBytes(StandardCharsets.UTF_8);
    bridge.consumeByteArray(bs, 0, bs.length);
    bridge.reset();

    bs = "test_hash".getBytes(StandardCharsets.UTF_8);
    bridge.consumeByteArray(bs, 0, bs.length);
    assertEquals(HASH_TEST_HASH, toHexString(bridge.produceDigest()));
  }

  @Test
  public void testClone() {
    byte[] bs = "te".getBytes(StandardCharsets.UTF_8);
    bridge.consumeByteArray(bs, 0, bs.length);

    Sha1DcBridge clonedBridge = (Sha1DcBridge) bridge.clone();
    bs = "st_hash".getBytes(StandardCharsets.UTF_8);
    clonedBridge.consumeByteArray(bs, 0, bs.length);
    assertEquals(HASH_TEST_HASH, toHexString(clonedBridge.produceDigest()));

    bs = "st".getBytes(StandardCharsets.UTF_8);
    bridge.consumeByteArray(bs, 0, bs.length);
    assertEquals(HASH_TEST, toHexString(bridge.produceDigest()));
  }

  @Test
  public void testShatteredPdf() throws Exception {
    byte[] bs =
        Files.readAllBytes(
            Paths.get(System.getenv("TEST_SRCDIR"))
                .resolve(
                    "__main__/src/test/java/com/google/devtools/sha1dc/testdata/shattered-1.pdf"));
    bridge.consumeByteArray(bs, 0, bs.length);
    assertThrows(CollisionDetectedException.class, () -> bridge.produceDigest());
  }

  private static String toHexString(byte[] bs) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bs) {
      sb.append(String.format("%02X", b));
    }
    return sb.toString();
  }
}
