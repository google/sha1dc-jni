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

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.eclipse.jgit.util.sha1.SHA1;
import org.junit.BeforeClass;
import org.junit.Test;

public class Sha1DcHasherTest {
  static {
    Path libraryPath =
        Paths.get(System.getenv("TEST_SRCDIR"))
            .resolve("__main__/src/main/java/com/google/devtools/sha1dc/sha1dc_jni_bridge.so");
    System.load(libraryPath.toString());
  }

  private static final int LOOP_COUNT = 100;
  private static final int ONE_BUFFER_SIZE = 2 * 1024 * 1024;
  private static final int BUFFER_COUNT = 20;
  private static List<byte[]> testInputs = new ArrayList<>();

  @BeforeClass
  public static void setUpClass() {
    Random random = new Random();
    for (int i = 0; i < BUFFER_COUNT; i++) {
      byte[] bs = new byte[ONE_BUFFER_SIZE];
      random.nextBytes(bs);
      testInputs.add(bs);
    }
  }

  @Test
  public void testValidity() {
    Hasher sha1dcHasher = new Sha1DcHasher();
    Hasher sha1Hasher = Hashing.sha1().newHasher();

    for (byte[] bs : testInputs) {
      sha1dcHasher.putBytes(bs);
      sha1Hasher.putBytes(bs);
    }
    assertEquals(sha1Hasher.hash(), sha1dcHasher.hash());
  }

  @Test
  public void testByteArray() {
    {
      Instant start = Instant.now();
      SHA1 hasher = SHA1.newInstance();
      for (int i = 0; i < LOOP_COUNT; i++) {
        hasher.reset();
        for (byte[] bs : testInputs) {
          hasher.update(bs);
        }
        hasher.digest();
      }
      Instant end = Instant.now();
      System.err.printf("%s: %dms\n", "JGit SHA1", end.toEpochMilli() - start.toEpochMilli());
    }

    {
      List<ByteBuffer> inputs =
          testInputs.stream()
              .map(
                  bs -> {
                    ByteBuffer buffer = ByteBuffer.allocateDirect(bs.length);
                    buffer.put(bs);
                    return buffer;
                  })
              .collect(Collectors.toList());

      Instant start = Instant.now();
      for (int i = 0; i < LOOP_COUNT; i++) {
        Hasher hasher = new Sha1DcHasher();
        for (ByteBuffer buffer : inputs) {
          buffer.rewind();
          hasher.putBytes(buffer);
        }
        hasher.hash();
      }
      Instant end = Instant.now();
      System.err.printf(
          "%s: %dms\n", "Sha1DcHasher (DirectBuffer)", end.toEpochMilli() - start.toEpochMilli());
    }

    BiConsumer<String, Supplier<Hasher>> f =
        (name, hasherSupplier) -> {
          Instant start = Instant.now();
          for (int i = 0; i < LOOP_COUNT; i++) {
            Hasher hasher = hasherSupplier.get();
            for (byte[] bs : testInputs) {
              hasher.putBytes(bs);
            }
            hasher.hash();
          }
          Instant end = Instant.now();
          System.err.printf("%s: %dms\n", name, end.toEpochMilli() - start.toEpochMilli());
        };

    f.accept("Sha1DcHasher (ByteArray, Copy)", () -> new Sha1DcHasher());
    f.accept("Sha1DcHasher (ByteArray, GCLock)", () -> new Sha1DcHasher(false));
    f.accept("Standard SHA-1 Hasher (ByteArray)", () -> Hashing.sha1().newHasher());
  }
}
