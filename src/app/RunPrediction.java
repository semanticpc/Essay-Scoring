/**
 * 
 */
package app;

import java.util.ArrayList;

import ml.FeatureList;
import parser.EssayInstance;
import parser.EssayInstanceParser;

/**
 * @author semanticpc
 * 
 */
public class RunPrediction {
	public static ArrayList<EssayInstance> instances;
  	
  public static void main(String[] args) {
    EssayInstanceParser parser = new EssayInstanceParser();
    // Parse the input training file
    instances = parser.parse(
        "DataSet/training_set_rel3.tsv", true);
    
    // Get feature Scores for each instance
    FeatureList.buildFeatures(instances);
    
    // Now we have all the instances and features
    // use any Machine Learning Tools (such as Weka)
    
  }
}
