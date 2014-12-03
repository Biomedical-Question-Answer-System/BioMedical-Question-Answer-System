package util;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import json.gson.Snippet;
import json.gson.TestSet;
import json.gson.TestYesNoQuestion;
import json.gson.TrainingSet;
import json.gson.TrainingYesNoQuestion;

import com.aliasi.classify.Classification;
import com.google.common.collect.Lists;

public class test1 {
  int count = 0;

  public void test1() {

    String filePath = "/yesno.json";
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
      if (true) {
        List<Snippet> snippetList = q.getSnippets();
        String answer = ((TrainingYesNoQuestion) q).getExactAnswer();
        if (snippetList!=null) {
          for (Snippet s : snippetList) {
            PrintWriter writer = null;
            File afile = new File("./src/main/resources/txt_sentoken/"
                    + answer.substring(0, 1).toLowerCase(), count + ".txt");
            try {
              writer = new PrintWriter(afile);
              System.out.println(s.getText());
              writer.print(s.getText());
            } catch (IOException e) {
              e.printStackTrace();
            }
            writer.close();
            count++;
          }
        }
      }
    }
  }

  public static void main(String[] args) {
    test1 t = new test1();
    t.test1();
  }
}
