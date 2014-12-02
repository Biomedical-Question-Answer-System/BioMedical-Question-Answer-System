package edu.cmu.lti.oaqa.team04.annotators;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.LinkedLifeDataServiceResponse;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Triple;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;

public class TriplesAnnotator extends JCasAnnotator_ImplBase {
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
    Triple tripleTypeSys = null;
    TripleSearchResult tripleSearchRe = null;
    try {
      LinkedLifeDataServiceResponse.Result linkedLifeDataResult = service
              .findLinkedLifeDataEntitiesPaged(text, 0);
      // System.out.println("LinkedLifeData: " + linkedLifeDataResult.getEntities().size());
      for (LinkedLifeDataServiceResponse.Entity entity : linkedLifeDataResult.getEntities()) {
        if (entity.getScore() > 0.5) {
          int count = 1;
          for (LinkedLifeDataServiceResponse.Relation relation : entity.getRelations()) {
            tripleTypeSys = new Triple(aJCas);
            tripleTypeSys.setObject(relation.getObj());
            tripleTypeSys.setSubject(relation.getSubj());
            tripleTypeSys.setPredicate(relation.getPred());
            tripleTypeSys.addToIndexes();
            tripleSearchRe = new TripleSearchResult(aJCas);
            tripleSearchRe.setTriple(tripleTypeSys);
            tripleSearchRe.setScore(entity.getScore());
            tripleSearchRe.setRank(count++);
            tripleSearchRe.addToIndexes(aJCas);
     //       count++;
            if (count >= 10) {
              break;
            }
          }
        }
        else{
          continue;
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
