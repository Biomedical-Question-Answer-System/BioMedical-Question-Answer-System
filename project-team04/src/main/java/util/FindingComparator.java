package util;

import java.util.Comparator;

import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse.Finding;

/**
 * Description: Compare cosine similarity in the list.
 * 
 * @author Xuwei Zou
 *
 */
public class FindingComparator implements Comparator<Finding> {
/**
 * Override the compare function.
 */
  @Override
  public int compare(Finding o1, Finding o2) {
    // TODO Auto-generated method stub
    if(o1.getScore()>o2.getScore()){
      return -1;
    }
    else if(o1.getScore()<o2.getScore()){
      return 1;
    }
    else{
      return 0;
    }
  }

}
