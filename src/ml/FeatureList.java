/**
 * 
 */
package ml;

import java.util.ArrayList;

import parser.EssayInstance;
import features.*;

/**
 * @author semanticpc
 * 
 */
public class FeatureList {
  public static void buildFeatures(ArrayList<EssayInstance> instances) {
	ArrayList<Features> featureInstances = new ArrayList<Features>();
	featureInstances.add(new StopWordRatioFeature());
    featureInstances.add(new LengthFeature());
	featureInstances.add(new AverageWordLengthFeature());
	featureInstances.add(new PercentMatchesFeature(","));
	featureInstances.add(new PercentMatchesFeature("!"));
	featureInstances.add(new PercentMatchesFeature("?"));
	featureInstances.add(new PercentMatchesFeature("@.*", true));
    for (EssayInstance instance : instances) {
	  for (Features feature : featureInstances)
		instance.setFeature(feature.getFeatureScores(instance));
    }
    
  }
}
