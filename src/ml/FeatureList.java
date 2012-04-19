/**
 * 
 */
package ml;

import java.util.ArrayList;

import parser.EssayInstance;
import features.StopWordRatioFeature;
import features.LengthFeature;

/**
 * @author semanticpc
 * 
 */
public class FeatureList {
  public static void buildFeatures(ArrayList<EssayInstance> instances) {
    StopWordRatioFeature stopwordratio = new StopWordRatioFeature();
    LengthFeature lengthratio = new LengthFeature();
    for (EssayInstance instance : instances) {
      instance.setFeature(stopwordratio.getFeatureScores(instance));
      instance.setFeature(lengthratio.getFeatureScores(instance));
    }
    
  }
}
