package edu.cmu.lti.oaqa.team04.annotators;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.type.input.Question;

public class StemAnnotator extends JCasAnnotator_ImplBase {
  static private HashSet<String> stopwordsSet;

   
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    stopwordsSet = new HashSet<String>();
    String sLine;
    URL docUrl = StemAnnotator.class.getResource("/stopwordsenhancedforyesno");
    if (docUrl == null) {
      throw new IllegalArgumentException("Error opening /stopwords.txt");
    }
    BufferedReader br = null;
    try {
      br = new BufferedReader(new InputStreamReader(docUrl.openStream()));
      while ((sLine = br.readLine()) != null) {
        stopwordsSet.add(sLine);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      // }

    }

    br = null;

  }

   
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    // TODO Auto-generated method stub
    FSIterator it = aJCas.getAnnotationIndex(Question.type).iterator();
    // String Doc = aJCas.getDocumentText();
    Question questionTypeSys = null;
    if (it.hasNext()) {
      questionTypeSys = (Question) it.next();
    }
    String text = questionTypeSys.getText();
    // System.out.println(text);
    String finaltext = "";
    text = text.replace(",", " ");
    text = text.replace(".", " ");
    text = text.replace("!", " ");
    text = text.replace(";", " ");
    text = text.replace("?", " ");
    text = text.replace("-", " ");
    text = text.replace("'s ", " ");
    // System.out.println(text);
    List<String> res = new ArrayList<String>();
    for (String s : text.replaceAll("[^0-9a-zA-Z ]", "").toLowerCase().split("\\s+"))
      if (!stopwordsSet.contains(s))
        res.add(s);
    for (String s : res) {
      finaltext += s + " " + "AND" + " ";
    }
    System.out.println(finaltext.substring(0, finaltext.lastIndexOf("AND")).trim());
    questionTypeSys.setText(finaltext.substring(0, finaltext.lastIndexOf("AND")).trim());
  }

}
