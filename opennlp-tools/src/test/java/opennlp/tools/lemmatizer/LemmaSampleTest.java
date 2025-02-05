/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.tools.lemmatizer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StringReader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LemmaSampleTest {

  @Test
  void testParameterValidation() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new LemmaSample(new String[] {""}, new String[] {""},
          new String[] {"test", "one element to much"});
    });
  }

  private static String[] createSentence() {
    return new String[] {"Forecasts", "for", "the", "trade", "figures",
        "range", "widely", "."};
  }

  private static String[] createTags() {

    return new String[] {"NNS", "IN", "DT", "NN", "NNS", "VBP", "RB", "."};
  }

  private static String[] createLemmas() {
    return new String[] {"Forecast", "for", "the", "trade", "figure", "range",
        "widely", "."};
  }

  @Test
  void testLemmaSampleSerDe() throws IOException {
    LemmaSample lemmaSample = createGoldSample();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutput out = new ObjectOutputStream(byteArrayOutputStream);
    out.writeObject(lemmaSample);
    out.flush();
    byte[] bytes = byteArrayOutputStream.toByteArray();

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
    ObjectInput objectInput = new ObjectInputStream(byteArrayInputStream);

    LemmaSample deSerializedLemmaSample = null;
    try {
      deSerializedLemmaSample = (LemmaSample) objectInput.readObject();
    } catch (ClassNotFoundException e) {
      // do nothing
    }

    Assertions.assertNotNull(deSerializedLemmaSample);
    Assertions.assertArrayEquals(lemmaSample.getLemmas(), deSerializedLemmaSample.getLemmas());
    Assertions.assertArrayEquals(lemmaSample.getTokens(), deSerializedLemmaSample.getTokens());
    Assertions.assertArrayEquals(lemmaSample.getTags(), deSerializedLemmaSample.getTags());
  }

  @Test
  void testRetrievingContent() {
    LemmaSample sample = new LemmaSample(createSentence(), createTags(), createLemmas());

    Assertions.assertArrayEquals(createSentence(), sample.getTokens());
    Assertions.assertArrayEquals(createTags(), sample.getTags());
    Assertions.assertArrayEquals(createLemmas(), sample.getLemmas());
  }

  @Test
  void testToString() throws IOException {

    LemmaSample sample = new LemmaSample(createSentence(), createTags(),
        createLemmas());
    String[] sentence = createSentence();
    String[] tags = createTags();
    String[] lemmas = createLemmas();

    StringReader sr = new StringReader(sample.toString());
    BufferedReader reader = new BufferedReader(sr);
    for (int i = 0; i < sentence.length; i++) {
      String line = reader.readLine();
      String[] parts = line.split("\t");
      Assertions.assertEquals(3, parts.length);
      Assertions.assertEquals(sentence[i], parts[0]);
      Assertions.assertEquals(tags[i], parts[1]);
      Assertions.assertEquals(lemmas[i], parts[2]);
    }
  }

  @Test
  void testEquals() {
    Assertions.assertFalse(createGoldSample() == createGoldSample());
    Assertions.assertTrue(createGoldSample().equals(createGoldSample()));
    Assertions.assertFalse(createPredSample().equals(createGoldSample()));
    Assertions.assertFalse(createPredSample().equals(new Object()));
  }

  public static LemmaSample createGoldSample() {
    return new LemmaSample(createSentence(), createTags(), createLemmas());
  }

  public static LemmaSample createPredSample() {
    String[] lemmas = createLemmas();
    lemmas[5] = "figure";
    return new LemmaSample(createSentence(), createTags(), lemmas);
  }

}
