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

import opennlp.tools.chunker.ChunkerCrossValidator;
import opennlp.tools.chunker.ChunkerFactory;
import opennlp.tools.formats.ad.ADChunkSampleStream;
import opennlp.tools.formats.ad.ADNameSampleStream;
import opennlp.tools.formats.ad.ADSentenceSampleStream;
import opennlp.tools.formats.convert.NameToTokenSampleStream;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.sentdetect.SDCrossValidator;
import opennlp.tools.sentdetect.SentenceDetectorFactory;
import opennlp.tools.sentdetect.lang.Factory;
import opennlp.tools.tokenize.DetokenizationDictionary;
import opennlp.tools.tokenize.DictionaryDetokenizer;
import opennlp.tools.tokenize.TokenSample;
import opennlp.tools.tokenize.TokenizerCrossValidator;
import opennlp.tools.tokenize.TokenizerFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;

/**
 * Cross validation of Sentence Detector, Tokenizer and Chunker against the
 * Portugues corpus.
 * <p>
 * Download the gz files from the Floresta Sintactica project <a
 * href="http://www.linguateca.pt/floresta/corpus.html"> site </a> and
 * decompress it into this directory: $OPENNLP_DATA_DIR/ad.
 * <ul>
 * <li><a href=
 * "http://www.linguateca.pt/floresta/ficheiros/gz/FlorestaVirgem_CF_3.0_ad.txt.gz"
 * > FlorestaVirgem_CF_3.0_ad.txt.gz </a></li>
 * <li><a href=
 * "http://www.linguateca.pt/floresta/ficheiros/gz/Bosque_CF_8.0.ad.txt.gz">
 * Bosque_CF_8.0.ad.txt.gz </a></li>
 * </ul>
 */
public class ArvoresDeitadasEval extends AbstractEvalTest {

  private static final String BOSQUE = "ad/Bosque_CF_8.0.ad.txt";
  private static final String FLORESTA_VIRGEM = "ad/FlorestaVirgem_CF_3.0_ad.txt";

  private static final String LANG = "por";

  private static ObjectStream<String> getLineSample(String corpus)
      throws IOException {
    return new PlainTextByLineStream(new MarkableFileInputStreamFactory(
        new File(getOpennlpDataDir(), corpus)), StandardCharsets.ISO_8859_1);
  }

  @BeforeAll
  static void verifyTrainingData() throws Exception {

    verifyTrainingData(new ADSentenceSampleStream(getLineSample(BOSQUE), false),
        new BigInteger("140568367548727787313497336739085858596"));

    verifyTrainingData(new ADSentenceSampleStream(getLineSample(FLORESTA_VIRGEM), false),
        new BigInteger("2614161133949079191933514776652602918"));

  }

  private void sentenceCrossEval(TrainingParameters params,
                                 double expectedScore) throws IOException {

    ADSentenceSampleStream samples = new ADSentenceSampleStream(
        getLineSample(FLORESTA_VIRGEM), false);

    SDCrossValidator cv = new SDCrossValidator(LANG, params,
        new SentenceDetectorFactory(LANG, true, null,
            new Factory().getEOSCharacters(LANG)));

    cv.evaluate(samples, 10);

    System.out.println(cv.getFMeasure());
    Assertions.assertEquals(expectedScore, cv.getFMeasure().getFMeasure(), 0.0001d);
  }

  private void tokenizerCrossEval(TrainingParameters params,
                                  double expectedScore) throws IOException {

    ObjectStream<NameSample> nameSamples = new ADNameSampleStream(
        getLineSample(FLORESTA_VIRGEM), true);

    DictionaryDetokenizer detokenizer = new DictionaryDetokenizer(
        new DetokenizationDictionary(new File("lang/pt/tokenizer/pt-detokenizer.xml")));

    ObjectStream<TokenSample> samples = new NameToTokenSampleStream(
        detokenizer, nameSamples);

    TokenizerCrossValidator validator;

    TokenizerFactory tokFactory = TokenizerFactory.create(null, LANG, null,
        true, null);
    validator = new opennlp.tools.tokenize.TokenizerCrossValidator(params,
        tokFactory);

    validator.evaluate(samples, 10);

    System.out.println(validator.getFMeasure());
    Assertions.assertEquals(expectedScore, validator.getFMeasure().getFMeasure(), 0.0001d);
  }

  private void chunkerCrossEval(TrainingParameters params,
                                double expectedScore) throws IOException {

    ADChunkSampleStream samples = new ADChunkSampleStream(getLineSample(BOSQUE));

    ChunkerCrossValidator cv = new ChunkerCrossValidator(LANG, params,
        new ChunkerFactory());

    cv.evaluate(samples, 10);
    Assertions.assertEquals(expectedScore, cv.getFMeasure().getFMeasure(), 0.0001d);
  }

  @Test
  void evalPortugueseSentenceDetectorPerceptron() throws IOException {
    sentenceCrossEval(createPerceptronParams(), 0.9892778840089301d);
  }

  @Test
  void evalPortugueseSentenceDetectorGis() throws IOException {
    sentenceCrossEval(ModelUtil.createDefaultTrainingParameters(), 0.987270070655111d);
  }

  @Test
  void evalPortugueseSentenceDetectorMaxentQn() throws IOException {
    sentenceCrossEval(createMaxentQnParams(), 0.9924715809679968d);
  }

  @Test
  void evalPortugueseSentenceDetectorNaiveBayes() throws IOException {
    sentenceCrossEval(createNaiveBayesParams(), 0.9672196206048099d);
  }

  @Test
  void evalPortugueseTokenizerPerceptron() throws IOException {
    tokenizerCrossEval(createPerceptronParams(), 0.9994887308380267d);
  }

  @Test
  void evalPortugueseTokenizerGis() throws IOException {
    tokenizerCrossEval(ModelUtil.createDefaultTrainingParameters(), 0.9992539405481062d);
  }

  @Test
  void evalPortugueseTokenizerMaxentQn() throws IOException {
    tokenizerCrossEval(createMaxentQnParams(), 0.9996017148748251d);
  }

  @Test
  void evalPortugueseTokenizerNaiveBayes() throws IOException {
    tokenizerCrossEval(createNaiveBayesParams(), 0.9962358244502717d);
  }

  @Test
  void evalPortugueseTokenizerMaxentQnMultipleThreads() throws IOException {
    TrainingParameters params = createMaxentQnParams();
    params.put("Threads", 4);
    tokenizerCrossEval(params, 0.9996017148748251d);
  }

  @Test
  void evalPortugueseChunkerPerceptron() throws IOException {
    chunkerCrossEval(createPerceptronParams(),
        0.9638122825015589d);
  }

  @Test
  void evalPortugueseChunkerGis() throws IOException {
    chunkerCrossEval(ModelUtil.createDefaultTrainingParameters(),
        0.9573860781121228d);
  }

  @Test
  void evalPortugueseChunkerGisMultipleThreads() throws IOException {
    TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
    params.put("Threads", 4);
    chunkerCrossEval(params, 0.9573860781121228d);
  }

  @Test
  void evalPortugueseChunkerQn() throws IOException {
    chunkerCrossEval(createMaxentQnParams(),
        0.9648211936491359d);
  }

  @Test
  void evalPortugueseChunkerQnMultipleThreads() throws IOException {
    TrainingParameters params = createMaxentQnParams();
    params.put("Threads", 4);

    // NOTE: Should be the same as without multiple threads!!!
    chunkerCrossEval(params, 0.9649180953528779d);
  }

  @Test
  void evalPortugueseChunkerNaiveBayes() throws IOException {
    chunkerCrossEval(createNaiveBayesParams(), 0.9041507736043933d);
  }
}
