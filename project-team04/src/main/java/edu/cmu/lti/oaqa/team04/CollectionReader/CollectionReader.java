package edu.cmu.lti.oaqa.team04.CollectionReader;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import json.JsonCollectionReaderHelper;
import json.gson.TestQuestion;
import json.gson.TestSet;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import com.google.common.collect.Lists;

public class CollectionReader extends CollectionReader_ImplBase {

  private List<TestQuestion> inputs;

  private int numberOfQuestions;

  @Override
  public void initialize() throws ResourceInitializationException {
    super.initialize();
    String filePath = "/BioASQ-SampleData1B.json";
    inputs = Lists.newArrayList();
    Object value = filePath;
    if (String.class.isAssignableFrom(value.getClass())) {
      inputs = TestSet.load(getClass().getResourceAsStream(String.class.cast(value))).stream()
              .collect(toList());
    } else if (String[].class.isAssignableFrom(value.getClass())) {
      inputs = Arrays.stream(String[].class.cast(value))
              .flatMap(path -> TestSet.load(getClass().getResourceAsStream(path)).stream())
              .collect(toList());
    }
    // trim question texts
    inputs.stream().filter(input -> input.getBody() != null)
            .forEach(input -> input.setBody(input.getBody().trim().replaceAll("\\s+", " ")));
    numberOfQuestions = inputs.size();
    System.out.println("concepts");
    System.out.println(inputs.get(0).getConcepts());
  }

  @Override
  public void getNext(CAS aCAS) throws IOException, CollectionException {
    // TODO Auto-generated method stub

    JCas jcas;
    try {
      jcas = aCAS.getJCas();
    } catch (CASException e) {
      throw new CollectionException(e);
    }
    TestQuestion question = inputs.remove(0);
    jcas.setDocumentText(question.getBody());
    JsonCollectionReaderHelper.addQuestionToIndex(question, "", jcas);
  }

  @Override
  public boolean hasNext() throws IOException, CollectionException {
    // TODO Auto-generated method stub
    return inputs.size() > 0;
  }

  @Override
  public Progress[] getProgress() {
    // TODO Auto-generated method stub
    return new Progress[] { new ProgressImpl(numberOfQuestions - inputs.size(), numberOfQuestions,
            Progress.ENTITIES) };
  }

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub

  }

}
