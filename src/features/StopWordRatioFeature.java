/**
 * 
 */
package features;

import java.util.HashMap;

import parser.EssayInstance;

/**
 * @author semanticpc
 * 
 */
public class StopWordRatioFeature implements Features {
  HashMap<String,Double> featureScores;
  String essayString;
  
  public StopWordRatioFeature() {
    this.featureScores = new HashMap<String,Double>();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see features.Features#getFeatureScores()
   */
  @Override
  public HashMap<String,Double> getFeatureScores(EssayInstance instance) {
    this.featureScores.put("stratio", getScore(instance.essay));
    return this.featureScores;
  }
  
  /**
   * @param essay
   * @return
   */
  public Double getScore(String essay) {
    // TODO Implement the Stop Word Ratio Scorer here
    return 0.0;
  }
  
}
