package edu.cmu.lti.oaqa.team04.annotators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import json.gson.Snippet;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.spell.TfIdfDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import util.SnippetComparator;
import util.SnippetWebService;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Document;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.Passage;

public class SnippetAnnotator extends JCasAnnotator_ImplBase {
  // private static GoPubMedService service = null;
  static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;

  static final SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();

  static final SentenceChunker SENTENCE_CHUNKER = new SentenceChunker(TOKENIZER_FACTORY,
          SENTENCE_MODEL);

   
  public void initialize(UimaContext aContext) throws ResourceInitializationException {

  }

   
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    // TODO Auto-generated method stub
    FSIterator questionsIt = aJCas.getAnnotationIndex(Question.type).iterator();
    Question question = new Question(aJCas);
    if (questionsIt.hasNext()) {
      question = (Question) questionsIt.next();
    }
    FSIterator documentsIt = aJCas.getJFSIndexRepository().getAllIndexedFS(
            edu.cmu.lti.oaqa.type.retrieval.Document.type);
    edu.cmu.lti.oaqa.type.retrieval.Document documents = null;
    SnippetWebService snippetService = new SnippetWebService();
    List<Snippet> snippetList = new ArrayList<Snippet>();
    while (documentsIt.hasNext()) {
      documents = (edu.cmu.lti.oaqa.type.retrieval.Document) documentsIt.next();
      JSONObject jsonObject = snippetService.getSnippets(documents.getUri());
      if (jsonObject == null) {
        continue;
      }
      JSONArray jsonArray = (JSONArray) jsonObject.get("sections");
      String passage = jsonArray.getString(0);
      passage = passage.replaceAll("\\2002", "");
      passage = passage.replaceAll("\\2003", "");
      passage = passage.replaceAll("\\2018", "");
      passage = passage.replaceAll("\\2019", "");
      Chunking chunking = SENTENCE_CHUNKER.chunk(passage.toCharArray(), 0, passage.length());
      Set<Chunk> sentences = chunking.chunkSet();
      if (sentences.size() < 1) {
        System.out.println("No sentence chunks found.");
        return;
      }
      String slice = chunking.charSequence().toString();
      TfIdfDistance tfIdf = new TfIdfDistance(TOKENIZER_FACTORY);
      tfIdf.handle(question.getText());
      for (Iterator<Chunk> it = sentences.iterator(); it.hasNext();) {
        Chunk sentence = it.next();
        int start = sentence.start();
        int end = sentence.end();
        Snippet snippet = new Snippet(snippetService.getUrl(documents.getUri()).toString(),
                slice.substring(start, end), start, end, "sections.0", "sections.0", 0);
        snippetList.add(snippet);
        tfIdf.handle(snippet.getText());
        // System.out.println(slice.substring(start, end));
      }
      for (Snippet s : snippetList) {
        s.setConfidence(tfIdf.proximity(question.getText(), s.getText()));
      }
    }
    snippetList.sort(new SnippetComparator());
    int count = Math.min(10, snippetList.size());
    for (int i = 0; i < count; i++) {
      Snippet snippetResult = snippetList.get(i);
      Passage snippets = new Passage(aJCas);
      snippets.setRank(i + 1);
      snippets.setTitle(snippetResult.getDocument());
      snippets.setText(snippetResult.getText());
      snippets.setScore(snippetResult.getConfidence());
      snippets.setOffsetInBeginSection(snippetResult.getOffsetInBeginSection());
      snippets.setOffsetInEndSection(snippetResult.getOffsetInEndSection());
      snippets.setBeginSection(snippetResult.getBeginSection());
      snippets.setEndSection(snippetResult.getEndSection());
      snippets.addToIndexes(aJCas);

    }

  }

}
