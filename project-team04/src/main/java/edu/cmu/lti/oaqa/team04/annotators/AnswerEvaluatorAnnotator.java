package edu.cmu.lti.oaqa.team04.annotators;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import json.gson.Snippet;
import json.gson.TestSet;
import json.gson.TestYesNoQuestion;
import json.gson.TrainingSet;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.collect.Lists;

import util.FindingComparator;
import util.Utils;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse.Finding;
import edu.cmu.lti.oaqa.type.answer.Answer;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.Passage;

public class AnswerEvaluatorAnnotator extends JCasAnnotator_ImplBase {
  private HashMap<String, String> yesnoMap = new HashMap<String, String>();

  private int correct_num = 0;

  private int answer_num = 0;

  private int supposed_num = 0;

  // private int questionnum =0;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    String filePath = "/BioASQ-SampleData1B.json";
    Object value = filePath;
    List<json.gson.Question> inputs = Lists.newArrayList();
    if (String.class.isAssignableFrom(value.getClass())) {
      inputs = TestSet.load(getClass().getResourceAsStream(String.class.cast(value))).stream()
              .collect(toList());
    } else if (String[].class.isAssignableFrom(value.getClass())) {
      inputs = Arrays.stream(String[].class.cast(value))
              .flatMap(path -> TestSet.load(getClass().getResourceAsStream(path)).stream())
              .collect(toList());
    }
    inputs.stream().filter(input -> input.getBody() != null)
            .forEach(input -> input.setBody(input.getBody().trim().replaceAll("\\s+", " ")));

    for (json.gson.Question q : inputs) {
      if (q instanceof TestYesNoQuestion) {
        yesnoMap.put(q.getId(), ((TestYesNoQuestion)q).getExactAnswer());
    
      }
     
    }
  }

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    // TODO Auto-generated method stub
    boolean flag = true;
    int max = 0;
    Question question = new Question(aJCas);
    FSIterator<?> questionIt = aJCas.getJFSIndexRepository().getAllIndexedFS(Question.type);
    if (questionIt.hasNext()) {
      question = (Question) questionIt.next();
    }
    if(!question.getType().equals("YES_NO")){
      return;
    }
    if (yesnoMap.containsKey(question.getId())) {
      String goldAnswer = yesnoMap.get(question.getId());
      answer_num++;
      FSIterator<?> answerIt = aJCas.getJFSIndexRepository().getAllIndexedFS(Answer.type);
      Answer resultAnswer = new Answer(aJCas);
      if(answerIt.hasNext()){
        resultAnswer =  (Answer) answerIt.next();
      }
        if(resultAnswer.equals(goldAnswer)){
          correct_num++;
      }

    }
    // System.out.println("MAP: " + map / conceptList.size() + "\n");
    // System.out.println("GMAP: " + Math.pow(gmap, 1.0 / conceptList.size()) + "\n");
    // totalMap += map;
    // totalGmap *= gmap;
    System.out.println(question.getText());
    printReport();

  }

  public double getfScore() {
    double precision = getPrecision();
    double recall = getRecall();
    return 2.0 * precision * recall / (precision + recall);
  }

  public double getPrecision() {
    return (double) correct_num / answer_num;
  }

  public double getRecall() {
    return (double) correct_num / supposed_num;
  }

  public int getintersection(int start1, int end1, int start2, int end2) {
    if (start1 > start2) {
      if (end2 < start1) {
        return 0;
      } else if (end2 < end1) {
        return end2 - start1;
      } else {
        return end1 - start1;
      }
    } else {
      if (end1 < start2) {
        return 0;
      } else if (end1 < end2) {
        return end1 - start2;
      } else {
        return end2 - start2;
      }
    }
  }

  public void printReport() {
    System.out.println();
    System.out.println("Correct Num:" + correct_num);
    System.out.println("Total Returned Answer:" + answer_num);
//    System.out.println("Gold Answer Num:" + supposed_num);
    System.out.println("Precision:" + getPrecision());
 //   System.out.println("Recall:" + getRecall());
//    System.out.println("F-socre:" + getfScore());
    // System.out.println("MAP:" + getMap());
    // System.out.println("GMAP:" + getGmap());
  }
}
