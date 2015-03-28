package edu.cmu.lti.oaqa.team04.annotators;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import json.gson.Snippet;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.spell.TfIdfDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Files;

import util.SnippetComparator;
import util.SnippetWebService;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Document;
import edu.cmu.lti.oaqa.type.answer.Answer;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.Passage;

public class AnswerAnnotator extends JCasAnnotator_ImplBase {
  // private static GoPubMedService service = null;
  File mPolarityDir;

  String[] mCategories;

  DynamicLMClassifier<NGramProcessLM> mClassifier;

  private int question_count = 0;

  void train() throws IOException {
    int numTrainingCases = 0;
    int numTrainingChars = 0;
    System.out.println("\nTraining.");
    for (int i = 0; i < mCategories.length; ++i) {
      String category = mCategories[i];
      Classification classification = new Classification(category);
      File file = new File(mPolarityDir, mCategories[i]);
      File[] trainFiles = file.listFiles();
      for (int j = 0; j < trainFiles.length; ++j) {
        File trainFile = trainFiles[j];
        ++numTrainingCases;
        String review = Files.readFromFile(trainFile, "ISO-8859-1");
        numTrainingChars += review.length();
        Classified<CharSequence> classified = new Classified<CharSequence>(review, classification);
        mClassifier.handle(classified);

      }
    }
    System.out.println("  # Training Cases=" + numTrainingCases);
    System.out.println("  # Training Chars=" + numTrainingChars);
  }

   
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    mPolarityDir = new File("./src/main/resources/txt_sentoken");
    // System.out.println("\nData Directory=" + mPolarityDir);
    mCategories = mPolarityDir.list();
    int nGram = 8;
    mClassifier = DynamicLMClassifier.createNGramProcess(mCategories, nGram);
    try {
      train();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

   
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    // TODO Auto-generated method stub
    FSIterator questionsIt = aJCas.getAnnotationIndex(Question.type).iterator();
    int yes = 0;
    int no = 0;
    Question question = new Question(aJCas);
    if (questionsIt.hasNext()) {
      question = (Question) questionsIt.next();
    }
    if (!question.getQuestionType().equals("YES_NO")) {
      return;
    }
    FSIterator snippetsIt = aJCas.getJFSIndexRepository().getAllIndexedFS(Passage.type);
    while (snippetsIt.hasNext()) {
      Passage snippetResult = (Passage) snippetsIt.next();
      Classification classification = mClassifier.classify(snippetResult.getText());
      if (classification.bestCategory().equals("n")) {
        no++;
      } else {
        yes++;
      }

    }
    Answer finalAnswer = new Answer(aJCas);
    if (no > 0) {
      finalAnswer.setText("No");
    } else {
      finalAnswer.setText("Yes");
    }
    System.out.println("Question :" + question_count + "  Yes:" + yes + "  No:" + no);
    finalAnswer.addToIndexes(aJCas);
  }

}
