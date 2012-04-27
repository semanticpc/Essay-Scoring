/**
 * 
 */
package ml;

import java.util.ArrayList;

import parser.EssayInstance;
import features.*;
import java.io.*;

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
	
	// be careful with using these style of % token match features and normalizing to (val-min)/(max-min) per task - there are some 4-10 word "essays" that will throw off the max on the scale
	// Instead of normalizing with min/max, it might be better to normalize to the number of stddev away from the mean, which is a little less sensitive to those outliers
	featureInstances.add(new PercentMatchesFeature(","));
	featureInstances.add(new PercentMatchesFeature("!"));
	featureInstances.add(new PercentMatchesFeature("?"));
	featureInstances.add(new PercentMatchesFeature("@.*", true));
	
	// note:  this probably isn't platform safe; it should do something like take the path of RunPrediction classfile and do a relpath dynamically
	try
		{
		featureInstances.add(new WordListFeatures(app.RunPrediction.resourcesPath + "/scowl-7.1/american_50.latin1"));
		}
	catch (IOException exc)
		{
		System.err.println("Unable to load word list: " + exc);
		}
	
    for (EssayInstance instance : instances) {
	  for (Features feature : featureInstances)
		instance.setFeature(feature.getFeatureScores(instance));
    }
    
  }
}
