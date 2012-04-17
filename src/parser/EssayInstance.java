/**
 * 
 */
package parser;

import java.util.HashMap;

/**
 * @author semanticpc
 * 
 */
public class EssayInstance {
  // Not sure if we need all these fields.
  // Storing all the fields available in the Training Set File for now
  public int essay_id;
  public int essay_set;
  public String essay;
  public int rater1_domain1 = -1;
  public int rater2_domain1 = -1;
  public int rater3_domain1 = -1;
  
  public int domain1_score = -1;
  
  public int rater1_domain2 = -1;
  public int rater2_domain2 = -1;
  
  public int domain2_score = -1;
  
  public int rater1_trait1 = -1;
  public int rater1_trait2 = -1;
  public int rater1_trait3 = -1;
  public int rater1_trait4 = -1;
  public int rater1_trait5 = -1;
  public int rater1_trait6 = -1;
  public int rater2_trait1 = -1;
  public int rater2_trait2 = -1;
  public int rater2_trait3 = -1;
  public int rater2_trait4 = -1;
  public int rater2_trait5 = -1;
  public int rater2_trait6 = -1;
  public int rater3_trait1 = -1;
  public int rater3_trait2 = -1;
  public int rater3_trait3 = -1;
  public int rater3_trait4 = -1;
  public int rater3_trait5 = -1;
  public int rater3_trait6 = -1;
  
  HashMap<String,Double> features;
  
  public EssayInstance() {
    this.features = new HashMap<String,Double>();
  }
  
  /**
   * @param featureScores
   */
  public void setFeature(HashMap<String,Double> featureScores) {
    for (String featureKey : featureScores.keySet()) {
      if (this.features.containsKey(featureKey)) this.features.put(
          featureKey.concat("1"), featureScores.get(featureKey));
      else this.features.put(featureKey, featureScores.get(featureKey));
    }
  }
  
  public HashMap<String,Double> getFeatures() {
    return this.features;
  }
}
