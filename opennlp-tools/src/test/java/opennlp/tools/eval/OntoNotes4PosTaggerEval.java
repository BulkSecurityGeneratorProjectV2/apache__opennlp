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

package opennlp.tools.eval;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import opennlp.tools.formats.DirectorySampleStream;
import opennlp.tools.formats.convert.FileToStringSampleStream;
import opennlp.tools.formats.convert.ParseToPOSSampleStream;
import opennlp.tools.formats.ontonotes.DocumentToLineStream;
import opennlp.tools.formats.ontonotes.OntoNotesParseSampleStream;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerCrossValidator;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;

public class OntoNotes4PosTaggerEval extends AbstractEvalTest {

  private static ObjectStream<POSSample> createPOSSampleStream() throws IOException {
    ObjectStream<File> documentStream = new DirectorySampleStream(new File(
        getOpennlpDataDir(), "ontonotes4/data/files/data/english"),
        file -> {
          if (file.isFile()) {
            return file.getName().endsWith(".parse");
          }

          return file.isDirectory();
        }, true);

    return new ParseToPOSSampleStream(new OntoNotesParseSampleStream(
        new DocumentToLineStream(
            new FileToStringSampleStream(documentStream, StandardCharsets.UTF_8))));
  }

  private void crossEval(TrainingParameters params, double expectedScore)
      throws IOException {
    try (ObjectStream<POSSample> samples = createPOSSampleStream()) {
      POSTaggerCrossValidator cv = new POSTaggerCrossValidator("eng", params, new POSTaggerFactory());
      cv.evaluate(samples, 5);

      Assertions.assertEquals(expectedScore, cv.getWordAccuracy(), 0.0001d);
    }
  }

  @BeforeAll
  static void verifyTrainingData() throws Exception {
    verifyTrainingData(createPOSSampleStream(), new BigInteger("300430765214895870888056958221353356972"));
  }

  @Test
  void evalEnglishMaxentTagger() throws IOException {
    TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
    params.put("Threads", "4");

    crossEval(params, 0.969345319453096d);
  }
}
