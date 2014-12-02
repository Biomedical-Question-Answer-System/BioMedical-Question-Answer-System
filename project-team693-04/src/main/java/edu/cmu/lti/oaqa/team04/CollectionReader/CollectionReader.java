package edu.cmu.lti.oaqa.team04.CollectionReader;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import json.JsonCollectionReaderHelper;
import json.gson.Question;
import json.gson.TrainingSet;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

public class CollectionReader extends CollectionReader_ImplBase {

  public static final String PARAM_INPUTPATH = "InputFile";

  private List<Question> inputs;

  private int NumofQues;

  private String oPath;

  private int CurrPos;

  public void initialize() throws ResourceInitializationException {
    oPath = ((String) getConfigParameterValue(PARAM_INPUTPATH)).trim();
    CurrPos = 0;
    // extract the input questions from file
    System.out.println(oPath);

    Object value = oPath;

    if (String.class.isAssignableFrom(value.getClass()))
      inputs = TrainingSet.load(getClass().getResourceAsStream(String.class.cast(value))).stream()
              .collect(toList());
    else if (String[].class.isAssignableFrom(value.getClass()))
      inputs = Arrays.stream(String[].class.cast(value))
              .flatMap(path -> TrainingSet.load(getClass().getResourceAsStream(oPath)).stream())
              .collect(toList());

    // trim question texts
    inputs.stream().filter(input -> input.getBody() != null)
            .forEach(input -> input.setBody(input.getBody().trim().replaceAll("\\s+", " ")));

    NumofQues = inputs.size();
  }

  @Override
  public void getNext(CAS aCAS) throws IOException, CollectionException {
    JCas jcas;
    try {
      jcas = aCAS.getJCas();
    } catch (CASException e) {
      throw new CollectionException(e);
    }
    // add all the information to CAS using helper
    Question question = inputs.get(CurrPos++);
    JsonCollectionReaderHelper.addQuestionToIndex(question, "", jcas);

    jcas.setDocumentText(question.getBody());
  }

  @Override
  public boolean hasNext() throws IOException, CollectionException {
    return CurrPos < inputs.size();
  }

  @Override
  public Progress[] getProgress() {
    return new Progress[] { new ProgressImpl(CurrPos, inputs.size(), Progress.ENTITIES) };
  }

  @Override
  public void close() throws IOException {
  }

}
