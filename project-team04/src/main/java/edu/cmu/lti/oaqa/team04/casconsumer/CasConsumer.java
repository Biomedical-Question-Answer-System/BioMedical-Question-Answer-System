package edu.cmu.lti.oaqa.team04.casconsumer;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.Passage;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;

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
      jcas = aCAS.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }
    Question question = new Question(jcas);
    FSIterator<?> questionIt = jcas.getJFSIndexRepository().getAllIndexedFS(Question.type);
    if (questionIt.hasNext()) {
      question = (Question) questionIt.next();
    }
    writer.printf("\t{\n");
    String body = jcas.getDocumentText();
    writer.printf("\t\t\"body\": \"%s\",\n", body);
    // concepts
    writer.printf("\t\t\"concepts\": [");
    // FSIterator it = jcas.getAnnotationIndex(Question.type).iterator();
    FSIterator<?> conceptIt = jcas.getJFSIndexRepository()
            .getAllIndexedFS(ConceptSearchResult.type);// jcas.getAnnotationIndex(Concept.type).iterator();
    while (conceptIt.hasNext()) {
      ConceptSearchResult concept = (ConceptSearchResult) conceptIt.next();
      writer.printf("\n\t\t\t\"%s\"", concept.getUri());
      if (conceptIt.hasNext())
        writer.printf(",");
    }
    writer.printf("\n\t\t],\n");
    // documents
    writer.printf("\t\t\"documents\": [");
    FSIterator docIt = jcas.getJFSIndexRepository().getAllIndexedFS(Document.type);
    while (docIt.hasNext()) {
      Document doc = (Document) docIt.next();
      writer.printf("\n\t\t\t\"%s\"", doc.getTitle());
      if (conceptIt.hasNext())
        writer.printf(",");
    }
    writer.printf("\n\t\t],\n");
    // triples
    writer.printf("\t\t\"triples\": [");
    FSIterator tripleIt = jcas.getJFSIndexRepository().getAllIndexedFS(TripleSearchResult.type);
    while (tripleIt.hasNext()) {
      TripleSearchResult triple = (TripleSearchResult) tripleIt.next();
      writer.printf(
              "\n\t\t\t{\n\t\t\t\t\"o\": \"%s\"\n\t\t\t\t\"p\": \"%s\"\n\t\t\t\t\"s\": \"%s\"\n\t\t\t}",
              triple.getTriple().getObject(), triple.getTriple().getPredicate(), triple.getTriple()
                      .getSubject());
      if (conceptIt.hasNext())
        writer.printf(",");
    }
    writer.printf("\n\t\t],\n");
    // question id
    writer.printf("\t\t\"id\": \"" + question.getId() + "\",\n");
    // the end of the question
    writer.printf("\t},\n");
    // snippets
    writer.printf("\t\t\"snippets\": [");
    FSIterator snippetIt = jcas.getJFSIndexRepository().getAllIndexedFS(Passage.type);
    while (snippetIt.hasNext()) {
      Passage snippet = (Passage) snippetIt.next();
      writer.printf(
              "\n\t\t\t{\n\t\t\t\t\"beginSection\": \"%s\"\n\t\t\t\t\"document\": \"%s\"\n\t\t\t\t\"endSection\": \"%s\"\n\t\t\t\t\"offsetInBeginSection\": \"%s\"\n\t\t\t\t\"offsetInEndSection\": \"%s\"\n\t\t\t\t\"text\": \"%s\"\n\t\t\t}",
              snippet.getBeginSection(),snippet.getTitle(),snippet.getEndSection(),snippet.getOffsetInBeginSection(),snippet.getOffsetInEndSection(),snippet.getText());
      if (conceptIt.hasNext())
        writer.printf(",");
    }
    writer.printf("\n\t\t],\n");
    // question id
    writer.printf("\t\t\"id\": \"" + question.getId() + "\",\n");
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
