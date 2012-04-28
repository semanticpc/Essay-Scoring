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
public interface Features {
  
  /**
   * @return The feature scores in a HashMap for each essay instance For
   *         example: <SpellingScore1, 0.9>, <SpellingScore2,0.6> for spelling
   *         features
   */
  public HashMap<String,Double> getFeatureScores(EssayInstance instance);

}
