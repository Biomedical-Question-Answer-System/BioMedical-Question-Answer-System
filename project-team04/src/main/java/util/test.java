package util;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import json.gson.Snippet;
import json.gson.TestSet;
import json.gson.TestYesNoQuestion;
import json.gson.TrainingSet;
import json.gson.TrainingYesNoQuestion;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.util.Files;
import com.google.common.collect.Lists;


public class test {
  File mPolarityDir;

  String[] mCategories;

  DynamicLMClassifier<NGramProcessLM> mClassifier;
  
  int answernum = 0;
  int correctnum = 0;
  
  void train() throws IOException {
    int numTrainingCases = 0;
    int numTrainingChars = 0;
    System.out.println("\nTraining.");
    for (int i = 0; i < mCategories.length; ++i) {
      String category = mCategories[i];
      Classification classification = new Classification(category);
      File file = new File(mPolarityDir, mCategories[i]);
      File[] trainFiles = file.listFiles();
      for (int j = 0; j < trainFiles.length; ++j) {
        File trainFile = trainFiles[j];
        ++numTrainingCases;
        String review = Files.readFromFile(trainFile, "ISO-8859-1");
        numTrainingChars += review.length();
        Classified<CharSequence> classified = new Classified<CharSequence>(review, classification);
        mClassifier.handle(classified);

      }
    }
    System.out.println("  # Training Cases=" + numTrainingCases);
    System.out.println("  # Training Chars=" + numTrainingChars);
  }
  public void test(){
    mPolarityDir = new File("./src/main/resources/txt_sentoken");
    // System.out.println("\nData Directory=" + mPolarityDir);
    mCategories = mPolarityDir.list();
    int nGram = 8;
    mClassifier = DynamicLMClassifier.createNGramProcess(mCategories, nGram);
    try {
      train();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    String filePath = "/NewAnswer.json";
    Object value = filePath;
    List<json.gson.Question> inputs = Lists.newArrayList();
    if (String.class.isAssignableFrom(value.getClass())) {
      inputs = TrainingSet.load(getClass().getResourceAsStream(String.class.cast(value))).stream()
              .collect(toList());
    } else if (String[].class.isAssignableFrom(value.getClass())) {
      inputs = Arrays.stream(String[].class.cast(value))
              .flatMap(path -> TrainingSet.load(getClass().getResourceAsStream(path)).stream())
              .collect(toList());
    }
    inputs.stream().filter(input -> input.getBody() != null)
            .forEach(input -> input.setBody(input.getBody().trim().replaceAll("\\s+", " ")));

    for (json.gson.Question q : inputs) {
      answernum++;
      if (q instanceof TrainingYesNoQuestion) {
        List<Snippet> snippetList = q.getSnippets();
       String answer= ((TrainingYesNoQuestion)q).getExactAnswer();
       int yes = 0;
       int no = 0;
       if(snippetList != null)
       for(Snippet s :snippetList){
         Classification classification = mClassifier.classify(s.getText());
         System.out.println(classification.bestCategory());
         if (classification.bestCategory().equals("n")){
           no++;
         }
         else{
           yes++;
         }
       }
       if(no>yes){
         if(answer.toLowerCase().equals("no")){
           correctnum++;
         }
       }
       else{
         if(answer.toLowerCase().equals("yes")){
           correctnum++;
         }
       }
      }
     System.out.println("answer:"+answernum+" correct:"+correctnum+" precision:"+(double)correctnum/answernum);
    }
  }
  public static void main(String[] args){
    test t = new test();
    t.test();
  }
}
