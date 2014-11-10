package edu.cmu.lti.oaqa.team04.annotators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.StringList;
import org.apache.uima.resource.ResourceInitializationException;

import util.Utils;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.LinkedLifeDataServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Document;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.kb.Triple;

public class Annotator extends JCasAnnotator_ImplBase {
  static GoPubMedService service = null;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    try {
      GoPubMedService service = new GoPubMedService("./project.properties");
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
//    StringList conceptStringList = null;
//    FSList mentionList = null;
    List<String> conceptList = null;
    List<Double> confidenceList = null;
    edu.cmu.lti.oaqa.type.retrieval.Document documentTypeSys = null;
 //   StringList documentStringList = null;
    Triple tripleTypeSys = null;
    
    try {
      OntologyServiceResponse.Result diseaseOntologyResult = service
              .findDiseaseOntologyEntitiesPaged(text, 0);
      conceptList = new ArrayList<String>();
      confidenceList = new ArrayList<Double>();
      conceptTypeSys = new Concept(aJCas);
      for (OntologyServiceResponse.Finding finding : diseaseOntologyResult.getFindings()) {
        conceptList.add(finding.getConcept().getUri());
        confidenceList.add(finding.getScore());
      }
      conceptTypeSys.setName("Disease Ontology");
      conceptTypeSys.setUris(Utils.createStringList(aJCas, conceptList));
      conceptTypeSys.setMentions(Utils.fromCollectionToFSList(aJCas, (Collection)confidenceList));
      conceptTypeSys.addToIndexes(aJCas);
      
      conceptList = new ArrayList<String>();
      confidenceList = new ArrayList<Double>();
      conceptTypeSys = new Concept(aJCas);
      OntologyServiceResponse.Result geneOntologyResult = service.findGeneOntologyEntitiesPaged(
              text, 0, 10);
      for (OntologyServiceResponse.Finding finding : geneOntologyResult.getFindings()) {
        conceptList.add(finding.getConcept().getUri());
        confidenceList.add(finding.getScore());
      }
      conceptTypeSys.setName("Gene Ontology");
      conceptTypeSys.setUris(Utils.createStringList(aJCas, conceptList));
      conceptTypeSys.setMentions(Utils.fromCollectionToFSList(aJCas, (Collection)confidenceList));
      conceptTypeSys.addToIndexes(aJCas);
      
      
      conceptList = new ArrayList<String>();
      confidenceList = new ArrayList<Double>();
      conceptTypeSys = new Concept(aJCas);
      OntologyServiceResponse.Result jochemResult = service.findJochemEntitiesPaged(text, 0);
      for (OntologyServiceResponse.Finding finding : jochemResult.getFindings()) {
        conceptList.add(finding.getConcept().getUri());
        confidenceList.add(finding.getScore());
      }
      conceptTypeSys.setName("Jochem");
      conceptTypeSys.setUris(Utils.createStringList(aJCas, conceptList));
      conceptTypeSys.setMentions(Utils.fromCollectionToFSList(aJCas, (Collection)confidenceList));
      conceptTypeSys.addToIndexes(aJCas);
      
      
      conceptList = new ArrayList<String>();
      confidenceList = new ArrayList<Double>();
      conceptTypeSys = new Concept(aJCas);
      OntologyServiceResponse.Result meshResult = service.findMeshEntitiesPaged(text, 0);
      for (OntologyServiceResponse.Finding finding : meshResult.getFindings()) {
        conceptList.add(finding.getConcept().getUri());
        confidenceList.add(finding.getScore());
      }
      conceptTypeSys.setName("MeSH");
      conceptTypeSys.setUris(Utils.createStringList(aJCas, conceptList));
      conceptTypeSys.setMentions(Utils.fromCollectionToFSList(aJCas, (Collection)confidenceList));
      conceptTypeSys.addToIndexes(aJCas);
      

      conceptList = new ArrayList<String>();
      confidenceList = new ArrayList<Double>();
      conceptTypeSys = new Concept(aJCas);
      OntologyServiceResponse.Result uniprotResult = service.findUniprotEntitiesPaged(text, 0);
      for (OntologyServiceResponse.Finding finding : uniprotResult.getFindings()) {
        conceptList.add(finding.getConcept().getUri());
        confidenceList.add(finding.getScore());
      }
      conceptTypeSys.setName("UniProt");
      conceptTypeSys.setUris(Utils.createStringList(aJCas, conceptList));
      conceptTypeSys.setMentions(Utils.fromCollectionToFSList(aJCas, (Collection)confidenceList));
      conceptTypeSys.addToIndexes(aJCas);
      
 //Triples
      LinkedLifeDataServiceResponse.Result linkedLifeDataResult = service
              .findLinkedLifeDataEntitiesPaged(text, 0);
   //   System.out.println("LinkedLifeData: " + linkedLifeDataResult.getEntities().size());
      for (LinkedLifeDataServiceResponse.Entity entity : linkedLifeDataResult.getEntities()) {
    //    System.out.println(" > " + entity.getEntity());
        for (LinkedLifeDataServiceResponse.Relation relation : entity.getRelations()) {
          tripleTypeSys = new Triple(aJCas);
          tripleTypeSys.setObject(relation.getObj());
          tripleTypeSys.setSubject(relation.getSubj());
          tripleTypeSys.setPredicate(relation.getPred());
          tripleTypeSys.addToIndexes(aJCas); 
        }
      }
      
      
      PubMedSearchServiceResponse.Result pubmedResult = service.findPubMedCitations(text, 0);
      List<Document> docList = pubmedResult.getDocuments();
    //  String[] pmids = new String[docList.size()];
  //    int i = 0;
      for(Document doc : docList){
        documentTypeSys = new edu.cmu.lti.oaqa.type.retrieval.Document(aJCas);
        documentTypeSys.setTitle("http://www.ncbi.nlm.nih.gov/pubmed/" + doc.getPmid());
        documentTypeSys.setDocId(doc.getPmid());
        documentTypeSys.addToIndexes(aJCas);
        
    //    documentTypeSys.setDocId(doc);
   //     pmids[i++] = "http://www.ncbi.nlm.nih.gov/pubmed/" + doc.getPmid();
       // System.out.println( pmids[i - 1]);
      }

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
