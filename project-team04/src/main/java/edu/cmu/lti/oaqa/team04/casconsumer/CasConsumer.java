package edu.cmu.lti.oaqa.team04.casconsumer;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;
import org.uimafit.util.CasUtil;
import org.uimafit.util.FSCollectionFactory;

import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.kb.Triple;

/**
 * @author alfie
 *
 */
public class CasConsumer extends CasConsumer_ImplBase {

  private PrintWriter writer = null;

  public void initialize() throws ResourceInitializationException {
    try {
      writer = new PrintWriter("answer.txt", "UTF-8");
    } catch (IOException e) {
      e.printStackTrace();
    }
    writer.println("\"questions\": [");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.uima.collection.base_cpm.CasObjectProcessor#processCas(org.apache.uima.cas.CAS)
   */
  @Override
	public void processCas(CAS aCAS) throws ResourceProcessException {
		// TODO Auto-generated method stub
		JCas jcas;
		try {
			jcas =aCAS.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}
		writer.printf("\t{\n");
		String body = jcas.getDocumentText();
		writer.printf("\t\t\"body\": \"%s\",\n", body);
		// concepts
		writer.printf("\t\t\"concepts\": [");
	//	FSIterator it = jcas.getAnnotationIndex(Question.type).iterator();
		FSIterator<?> conceptIt = jcas.getJFSIndexRepository().getAllIndexedFS(Concept.type);//jcas.getAnnotationIndex(Concept.type).iterator();
		while(conceptIt.hasNext()){
			Concept concept = (Concept)conceptIt.next();
			StringList conceptList = concept.getUris();
			Collection<String> conceptCollection =FSCollectionFactory
			        .create(conceptList);
	//		ArrayList<String> conceptArrayList =(ArrayList<String>) conceptCollection;
			Iterator<String> conceptIterator= conceptCollection.iterator();
			while(conceptIterator.hasNext()){
			  writer.printf("\n\t\t\t\"%s\"", conceptIterator.next());
			}
			if(conceptIt.hasNext())
				writer.printf(",");
		}
		writer.printf("\n\t\t],\n");
		// documents
		writer.printf("\t\t\"documents\": [");
		FSIterator docIt =  jcas.getJFSIndexRepository().getAllIndexedFS(Document.type);
		while(docIt.hasNext()){
			Document doc = (Document)docIt.next();
			writer.printf("\n\t\t\t\"%s\"", doc.getDocId());
			if(conceptIt.hasNext())
				writer.printf(",");
		}
		writer.printf("\n\t\t],\n");
		// triples
		writer.printf("\t\t\"triples\": [");
		FSIterator tripleIt =  jcas.getJFSIndexRepository().getAllIndexedFS(Triple.type);
		while(docIt.hasNext()){
			Triple triple = (Triple)tripleIt.next();
			writer.printf("\n\t\t\t{\n\t\t\t\t\"o\": \"%s\"\n\t\t\t\t\"p\": \"%s\"\n\t\t\t\t\"s\": \"%s\"\n\t\t\t}", triple.getObject(), triple.getPredicate(), triple.getSubject());
			if(conceptIt.hasNext())
				writer.printf(",");
		}
		writer.printf("\n\t\t],\n");
		// the end of the question
		writer.printf("\t},\n");
	}

  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {

    super.collectionProcessComplete(arg0);
    writer.println("]}");
    writer.close();
  }

}