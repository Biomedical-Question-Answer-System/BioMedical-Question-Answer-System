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

import util.Utils;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Concept;

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
    List<String> conceptList = null;
    try {
      OntologyServiceResponse.Result diseaseOntologyResult = service
              .findDiseaseOntologyEntitiesPaged(text, 0);
      conceptList = new ArrayList<String>();
      // confidenceList = new ArrayList<Double>();
      conceptTypeSys = new Concept(aJCas);
      for (OntologyServiceResponse.Finding finding : diseaseOntologyResult.getFindings()) {
        conceptList.add(finding.getConcept().getUri());
        // confidenceList.add(finding.getScore());
      }
      conceptTypeSys.setName("Disease Ontology");
      conceptTypeSys.setUris(Utils.createStringList(aJCas, conceptList));
      // conceptTypeSys.setMentions(Utils.fromCollectionToFSList(aJCas,
      // (Collection)confidenceList));
      conceptTypeSys.addToIndexes(aJCas);

      conceptList = new ArrayList<String>();
      // confidenceList = new ArrayList<Double>();
      conceptTypeSys = new Concept(aJCas);
      OntologyServiceResponse.Result geneOntologyResult = service.findGeneOntologyEntitiesPaged(
              text, 0, 10);
      for (OntologyServiceResponse.Finding finding : geneOntologyResult.getFindings()) {
        conceptList.add(finding.getConcept().getUri());
        // confidenceList.add(finding.getScore());
      }
      conceptTypeSys.setName("Gene Ontology");
      conceptTypeSys.setUris(Utils.createStringList(aJCas, conceptList));
      // conceptTypeSys.setMentions(Utils.fromCollectionToFSList(aJCas,
      // (Collection)confidenceList));
      conceptTypeSys.addToIndexes(aJCas);

      conceptList = new ArrayList<String>();
      // confidenceList = new ArrayList<Double>();
      conceptTypeSys = new Concept(aJCas);
      OntologyServiceResponse.Result jochemResult = service.findJochemEntitiesPaged(text, 0);
      for (OntologyServiceResponse.Finding finding : jochemResult.getFindings()) {
        conceptList.add(finding.getConcept().getUri());
        // confidenceList.add(finding.getScore());
      }
      conceptTypeSys.setName("Jochem");
      conceptTypeSys.setUris(Utils.createStringList(aJCas, conceptList));
      // conceptTypeSys.setMentions(Utils.fromCollectionToFSList(aJCas,
      // (Collection)confidenceList));
      conceptTypeSys.addToIndexes(aJCas);

      conceptList = new ArrayList<String>();
      // confidenceList = new ArrayList<Double>();
      conceptTypeSys = new Concept(aJCas);
      OntologyServiceResponse.Result meshResult = service.findMeshEntitiesPaged(text, 0);
      for (OntologyServiceResponse.Finding finding : meshResult.getFindings()) {
        conceptList.add(finding.getConcept().getUri());
        // confidenceList.add(finding.getScore());
      }
      conceptTypeSys.setName("MeSH");
      conceptTypeSys.setUris(Utils.createStringList(aJCas, conceptList));
      // conceptTypeSys.setMentions(Utils.fromCollectionToFSList(aJCas,
      // (Collection)confidenceList));
      conceptTypeSys.addToIndexes(aJCas);

      conceptList = new ArrayList<String>();
      // confidenceList = new ArrayList<Double>();
      conceptTypeSys = new Concept(aJCas);
      OntologyServiceResponse.Result uniprotResult = service.findUniprotEntitiesPaged(text, 0);
      for (OntologyServiceResponse.Finding finding : uniprotResult.getFindings()) {
        conceptList.add(finding.getConcept().getUri());
        // confidenceList.add(finding.getScore());
      }
      conceptTypeSys.setName("UniProt");
      conceptTypeSys.setUris(Utils.createStringList(aJCas, conceptList));
      // conceptTypeSys.setMentions(Utils.fromCollectionToFSList(aJCas,
      // (Collection)confidenceList));
      conceptTypeSys.addToIndexes(aJCas);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
