/**
 * 
 */
package ml;

import java.util.ArrayList;

import parser.EssayInstance;
import features.StopWordRatioFeature;

/**
 * @author semanticpc
 * 
 */
public class FeatureList {
  public static void buildFeatures(ArrayList<EssayInstance> instances) {
    StopWordRatioFeature stopwordratio = new StopWordRatioFeature();
    for (EssayInstance instance : instances) {
      instance.setFeature(stopwordratio.getFeatureScores(instance));
    }
    
  }
}
