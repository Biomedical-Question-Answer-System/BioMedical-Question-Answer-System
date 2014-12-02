package edu.cmu.lti.oaqa.team04;

import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import json.gson.TestQuestion;
import json.gson.TestSet;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.util.XMLInputSource;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;

import com.google.common.collect.Lists;

import json.gson.Question;
import json.gson.QuestionType;
import json.gson.TestQuestion;
import json.gson.TestSet;
import json.gson.TestSummaryQuestion;
import json.gson.TrainingFactoidQuestion;
import json.gson.TrainingListQuestion;
import json.gson.TrainingQuestion;
import json.gson.TrainingYesNoQuestion;
import edu.cmu.lti.oaqa.type.answer.Answer;
import static java.util.stream.Collectors.toList;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.Passage;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;
import util.TypeFactory;

public class AaeBIOQA {
  public static void main(String [] args) 
          throws Exception {
          
        String sLine;
        long startTime=System.currentTimeMillis();
        
        URL descUrl = AaeBIOQA.class.getResource("/descriptors/AaeDescriptor.xml");
         if (descUrl == null) {
            throw new IllegalArgumentException("Error opening AaeDescriptor.xml");
         }
        // create AnalysisEngine    
        XMLInputSource input = new XMLInputSource(descUrl);
        AnalysisEngineDescription desc = UIMAFramework.getXMLParser().parseAnalysisEngineDescription(input);
        AnalysisEngine anAnalysisEngine = UIMAFramework.produceAnalysisEngine(desc);
        CAS aCas = anAnalysisEngine.newCAS();

        
        String filePath = "/questions.json";
        List<TestQuestion> inputs;
        inputs = Lists.newArrayList();
        /*  InputStream stream = getClass().getResourceAsStream(filePath);
        try {
          System.out.println("stream " + stream.read());
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }*/
        Object value = filePath;
        if (String.class.isAssignableFrom(value.getClass())) {
          inputs = TestSet.load(value.getClass().getResourceAsStream(
                  String.class.cast(value))).stream()
              .collect(toList());
        } else if (String[].class.isAssignableFrom(value.getClass())) {
          inputs = Arrays
              .stream(String[].class.cast(value))
              .flatMap(
                  path -> TestSet.load(value.getClass().getResourceAsStream(path))
                      .stream()).collect(toList());
        }
     
        
        URL docUrl = AaeBIOQA.class.getResource("/questions.json");
        if (docUrl == null) {
           throw new IllegalArgumentException("Error opening data/questions.json");
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(docUrl.openStream()));
        while ((sLine = br.readLine()) != null)   {
          System.out.println(sLine);
          aCas.setDocumentText(sLine);
          anAnalysisEngine.process(aCas);
          aCas.reset();
        }
        br.close();
        br=null;
        anAnalysisEngine.collectionProcessComplete();
        anAnalysisEngine.destroy(); 
        long endTime=System.currentTimeMillis();
        
        double totalTime=(endTime-startTime)/1000.0;
        System.out.println("Total time taken: "+totalTime);
        

      }
}
