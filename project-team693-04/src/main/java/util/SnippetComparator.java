package util;

import java.util.Comparator;

import json.gson.Snippet;



/**
 * Description: Compare cosine similarity in the list.
 * 
 * @author Xuwei Zou
 *
 */
public class SnippetComparator implements Comparator<Snippet> {
/**
 * Override the compare function.
 */
  @Override
  public int compare(Snippet o1, Snippet o2) {
    // TODO Auto-generated method stub
    if(o1.getConfidence()>o2.getConfidence()){
      return -1;
    }
    else if(o1.getConfidence()<o2.getConfidence()){
      return 1;
    }
    else{
      return 0;
    }
  }

}
