import static java.util.stream.Collectors.toList;

import com.aliasi.util.Files;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.lm.NGramProcessLM;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import json.gson.TestSet;
import json.gson.TestYesNoQuestion;

public class test {

    File mPolarityDir;
    String[] mCategories;
    DynamicLMClassifier<NGramProcessLM> mClassifier;

    test(String[] args) {
        System.out.println("\nBASIC POLARITY DEMO");
        mPolarityDir = new File("./src/main/resources/txt_sentoken");
        System.out.println("\nData Directory=" + mPolarityDir);
        mCategories = mPolarityDir.list();
        int nGram = 8;
        mClassifier 
            = DynamicLMClassifier
            .createNGramProcess(mCategories,nGram);
    }

    public test() {
      // TODO Auto-generated constructor stub
    }

    void run() throws ClassNotFoundException, IOException {
        train();
        evaluate();
    }

    boolean isTrainingFile(File file) {
        return file.getName().charAt(2) != '9';  // test on fold 9
    }

    void train() throws IOException {
        int numTrainingCases = 0;
        int numTrainingChars = 0;
        System.out.println("\nTraining.");
        for (int i = 0; i < mCategories.length; ++i) {
            String category = mCategories[i];
            Classification classification
                = new Classification(category);
            File file = new File(mPolarityDir,mCategories[i]);
            File[] trainFiles = file.listFiles();
            for (int j = 0; j < trainFiles.length; ++j) {
                File trainFile = trainFiles[j];
                if (isTrainingFile(trainFile)) {
                    ++numTrainingCases;
                    String review = Files.readFromFile(trainFile,"ISO-8859-1");
                    numTrainingChars += review.length();
                    Classified<CharSequence> classified
                        = new Classified<CharSequence>(review,classification);
                    mClassifier.handle(classified);
                }
            }
        }
        System.out.println("  # Training Cases=" + numTrainingCases);
        System.out.println("  # Training Chars=" + numTrainingChars);
    }

    void evaluate() throws IOException {
        System.out.println("\nEvaluating.");
        int numTests = 0;
        int numCorrect = 0;
        for (int i = 0; i < mCategories.length; ++i) {
            String category = mCategories[i];
            File file = new File(mPolarityDir,mCategories[i]);
            File[] trainFiles = file.listFiles();
            for (int j = 0; j < trainFiles.length; ++j) {
                File trainFile = trainFiles[j];
                if (!isTrainingFile(trainFile)) {
                    String review = Files.readFromFile(trainFile,"ISO-8859-1");
                    ++numTests;
                    Classification classification
                        = mClassifier.classify(review);
                    if (classification.bestCategory().equals(category))
                        ++numCorrect;
                }
            }
        }
        System.out.println("  # Test Cases=" + numTests);
        System.out.println("  # Correct=" + numCorrect);
        System.out.println("  % Correct=" 
                           + ((double)numCorrect)/(double)numTests);
    }

    public void test(){
      String filePath = "/BioASQ-SampleData1B.json";
      Object value = filePath;
      List<json.gson.TestYesNoQuestion> inputs = Lists.newArrayList();
      if (String.class.isAssignableFrom(value.getClass())) {
        inputs = (List<TestYesNoQuestion>) TestSet.load(getClass().getResourceAsStream(String.class.cast(value))).stream()
                .collect(toList());
      } else if (String[].class.isAssignableFrom(value.getClass())) {
        inputs = (List<TestYesNoQuestion>) Arrays.stream(String[].class.cast(value))
                .flatMap(path -> TestSet.load(getClass().getResourceAsStream(path)).stream())
                .collect(toList());
      }
      inputs.stream().filter(input -> input.getBody() != null)
              .forEach(input -> input.setBody(input.getBody().trim().replaceAll("\\s+", " ")));

      for (json.gson.TestYesNoQuestion  q : inputs) {
     
     //   yesnoMap.put(q.getId(), q.getExactAnswer());
      }
    }
    
    public static void main(String[] args) {
       test t = new test();
       t.test();
       
    }

}
