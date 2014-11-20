package edu.cmu.lti.oaqa.team04.annotators;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import json.gson.TestSet;
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
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;

public class DocumentEvaluatorAnnotator extends JCasAnnotator_ImplBase {
  private HashMap<String, ArrayList<String>> documentMap = new HashMap<String, ArrayList<String>>();

  private int correct_num = 0;

  private int answer_num = 0;

  private int supposed_num = 0;
  
  private double  totalMap =0;
  
  private double totalGmap = 1;
  
//  private int questionnum =0;

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
    // if (String.class.isAssignableFrom(value.getClass()))
    // inputs = TrainingSet.load(getClass().getResourceAsStream(String.class.cast(value))).stream()
    // .collect(toList());
    // else if (String[].class.isAssignableFrom(value.getClass()))
    // inputs = Arrays.stream(String[].class.cast(value))
    // .flatMap(path -> TrainingSet.load(getClass().getResourceAsStream(filePath)).stream())
    // .collect(toList());
    // inputs.stream().filter(input -> input.getBody() != null)
    // .forEach(input -> input.setBody(input.getBody().trim().replaceAll("\\s+", " ")));
    for (json.gson.Question q : inputs) {
      List<String> golddocuments = q.getDocuments();
      ArrayList<String> individualDocList = new ArrayList<String>();
      for (String s : golddocuments) {
        individualDocList.add(s);
      }
      documentMap.put(q.getId(), individualDocList);
    }
  }

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    // TODO Auto-generated method stub
 //   double map = 0;
//    double gmap = 1;
    boolean flag = true;
    int max = 0;
    Question question = new Question(aJCas);
    FSIterator<?> questionIt = aJCas.getJFSIndexRepository().getAllIndexedFS(Question.type);
    if (questionIt.hasNext()) {
      question = (Question) questionIt.next();
    }
    if (documentMap.containsKey(question.getId())) {
      ArrayList<String> golddocumentList = documentMap.get(question.getId());
      supposed_num += golddocumentList.size();
      FSIterator<?> docResultIt = aJCas.getJFSIndexRepository().getAllIndexedFS(
              Document.type);
      HashMap<String, Integer> documentResultMap = new HashMap<String, Integer>();
      while (docResultIt.hasNext()) {
        Document document = (Document) docResultIt.next();
        documentResultMap.put(document.getTitle(), document.getRank());
        answer_num++;
      }
      for (int i = 0; i < golddocumentList.size(); i++) {
        String goldDocument = golddocumentList.get(i);
        if (flag) {
          if (documentResultMap.containsKey(goldDocument)) {
            correct_num++;
            if (max >= documentResultMap.get(goldDocument)) {
              totalMap += (i + 1.0) / max;
              totalGmap *= (i + 1.0) / max + 0.01;
            } else {
              max = documentResultMap.get(goldDocument);
              totalMap += (i + 1.0) / max;
              totalGmap *= (i + 1.0) / max + 0.01;
            }
          } else {
            flag = false;
            totalGmap *= 0.01;
          }
        } else {
          totalGmap *= 0.01;
          if (documentResultMap.containsKey(goldDocument)) {
            correct_num++;
          }
        }

      }
 //     System.out.println("MAP: " + map / conceptList.size() + "\n");
  //    System.out.println("GMAP: " + Math.pow(gmap, 1.0 / conceptList.size()) + "\n");
 //     totalMap += map;
  //    totalGmap *= gmap;
      printReport();
    }
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
  public double getMap() {
    return (double) totalMap / supposed_num;
  }

  public double getGmap() {
    return (double) Math.pow(totalGmap, 1.0/supposed_num);
  }
  public void printReport() {
    System.out.println();
    System.out.println("Correct Num:" + correct_num);
    System.out.println("Total Returned Answer:" + answer_num);
    System.out.println("Gold Answer Num:" + supposed_num);
    System.out.println("Precision:" + getPrecision());
    System.out.println("Recall:" + getRecall());
    System.out.println("F-socre:" + getfScore());
    System.out.println("MAP:" + getMap());
    System.out.println("GMAP:" + getGmap());
  }
}
