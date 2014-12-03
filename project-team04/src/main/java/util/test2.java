package util;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import json.gson.Snippet;
import json.gson.TestSet;
import json.gson.TestYesNoQuestion;
import json.gson.TrainingSet;

import com.aliasi.classify.Classification;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class test2 {
  int count = 0;
  public void test1(){
    
    String filePath = "/BioASQ-trainingDataset2b.json";
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
    List<json.gson.Question> outputs = Lists.newArrayList();
    for (json.gson.Question q : inputs) {
//      if (q instanceof TestYesNoQuestion) {
//        List<Snippet> snippetList = q.getSnippets();
//        String answer = ((TestYesNoQuestion) q).getExactAnswer();
//        for(Snippet s:snippetList){
//          PrintWriter writer = null;
//          try {
//            writer = new PrintWriter("./src/main/resources/txt_sentoken/"+answer+"/"+count+".txt", "UTF-8");
//            writer.printf(s.getText());
//          } catch (IOException e) {
//            e.printStackTrace();
//          }
//          writer.close();
//          count++;
//        }
//
//      }

      if(q.getType().toString().equals("yesno")){
        json.gson.Question newquestion = new json.gson.Question(q.getId(),q.getBody(),q.getType(), null, null, null, null);
        outputs.add(newquestion);
      }
    }
    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
            .create();
    String Output = gson.toJson(outputs);
    PrintWriter fw = null;
    try {
      fw = new PrintWriter("yesnoquestion.json", "UTF-8");
    } catch (IOException e) {
      e.printStackTrace();
    }
    fw.println("{\"questions\":  " + Output+"}");
    fw.close();
  }
  public static void main(String[] args){
    test2 t = new test2();
    t.test1();
  }
}
