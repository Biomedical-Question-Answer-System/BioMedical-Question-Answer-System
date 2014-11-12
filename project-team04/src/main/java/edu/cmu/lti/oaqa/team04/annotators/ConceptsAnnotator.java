package edu.cmu.lti.oaqa.team04.annotators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import util.FindingComparator;
import util.Utils;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse.Finding;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;

public class ConceptsAnnotator extends JCasAnnotator_ImplBase {
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
    Concept conceptTypeSys = null;
    ConceptSearchResult result = null;
    List<Finding> findingList = new ArrayList<Finding>();

    try {
      OntologyServiceResponse.Result diseaseOntologyResult = service
              .findDiseaseOntologyEntitiesPaged(text, 0);
      for (OntologyServiceResponse.Finding finding : diseaseOntologyResult.getFindings()) {
        if (finding.getScore() > 0.15) {
          findingList.add(finding);
        } else {
          break;
        }
      }

      conceptTypeSys = new Concept(aJCas);
      OntologyServiceResponse.Result geneOntologyResult = service.findGeneOntologyEntitiesPaged(
              text, 0, 10);
      for (OntologyServiceResponse.Finding finding : geneOntologyResult.getFindings()) {
        if (finding.getScore() > 0.15) {
          findingList.add(finding);
        } else {
          break;
        }
      }

      OntologyServiceResponse.Result jochemResult = service.findJochemEntitiesPaged(text, 0);
      for (OntologyServiceResponse.Finding finding : jochemResult.getFindings()) {
        if (finding.getScore() > 0.15) {
          findingList.add(finding);
        } else {
          break;
        }
      }

      OntologyServiceResponse.Result meshResult = service.findMeshEntitiesPaged(text, 0);
      for (OntologyServiceResponse.Finding finding : meshResult.getFindings()) {
        if (finding.getScore() > 0.15) {
          findingList.add(finding);
        } else {
          break;
        }
      }

      OntologyServiceResponse.Result uniprotResult = service.findUniprotEntitiesPaged(text, 0);
      for (OntologyServiceResponse.Finding finding : uniprotResult.getFindings()) {
        if (finding.getScore() > 0.15) {
          findingList.add(finding);
        } else {
          break;
        }
      }
      FindingComparator findingComparator = new FindingComparator();
      findingList.sort(findingComparator);
      int count = 0;
      for (Finding f : findingList) {
        conceptTypeSys = new Concept(aJCas);
        conceptTypeSys.setName(f.getConcept().getLabel());
        conceptTypeSys.addToIndexes();
        result = new ConceptSearchResult(aJCas);
        result.setConcept(conceptTypeSys);
        result.setUri(f.getConcept().getUri());
        result.setScore(f.getScore());
        result.addToIndexes(aJCas);
        count++;
        if(count>=10){
          break;
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
