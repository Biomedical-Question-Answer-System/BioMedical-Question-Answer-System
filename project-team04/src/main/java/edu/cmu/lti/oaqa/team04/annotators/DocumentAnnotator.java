package edu.cmu.lti.oaqa.team04.annotators;


import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Document;
import edu.cmu.lti.oaqa.type.input.Question;

public class DocumentAnnotator extends JCasAnnotator_ImplBase {
  private static GoPubMedService service = null;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    try {
      service = new GoPubMedService("./project.properties");
    } catch (ConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    // TODO Auto-generated method stub
    FSIterator it = aJCas.getAnnotationIndex(Question.type).iterator();
    // String Doc = aJCas.getDocumentText();
    Question questionTypeSys = null;
    if (it.hasNext()) {
      questionTypeSys = (Question) it.next();
    }
    String text = questionTypeSys.getText();
    edu.cmu.lti.oaqa.type.retrieval.Document documentTypeSys = null;
    try {
      PubMedSearchServiceResponse.Result pubmedResult = service.findPubMedCitations(text, 0);
      List<Document> docList = pubmedResult.getDocuments();
      // String[] pmids = new String[docList.size()];
      // int i = 0;
      int count = 1;
      for (Document doc : docList) {
        documentTypeSys = new edu.cmu.lti.oaqa.type.retrieval.Document(aJCas);
        documentTypeSys.setTitle("http://www.ncbi.nlm.nih.gov/pubmed/" + doc.getPmid());
        documentTypeSys.setDocId(doc.getPmid());
   //     System.out.println(doc.getPmid()+"********");
        documentTypeSys.setUri(doc.getPmid());
        documentTypeSys.setRank(count++);
        documentTypeSys.addToIndexes(aJCas);
        if(count>=30){

          break;
        }
      }

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}


