/**
 * 
 */
package app;

import java.util.ArrayList;

import ml.FeatureList;
import parser.EssayInstance;
import parser.EssayInstanceParser;
import java.io.*;
import java.nio.charset.*;

/**
 * @author semanticpc
 * 
 */
public class RunPrediction {
	public static final boolean debug = true;
	public static ArrayList<EssayInstance> instances;

  public static void main(String[] args) {
    EssayInstanceParser parser = new EssayInstanceParser();
    // Parse the input training file
    instances = parser.parse(
        "DataSet/training_set_rel3.tsv", true);
	
	if (debug)
		{
		try
			{
			PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream("examples.utf8"), Charset.forName("UTF-8")));
			for (EssayInstance essay : instances)
				out.println(essay + "\n");
			out.close();
			}
		catch (IOException e)
			{
			System.err.println("Failure to write to outfile: " + e);
			}
		}
    
    // Get feature Scores for each instance
    FeatureList.buildFeatures(instances);
    
    // Now we have all the instances and features
    // use any Machine Learning Tools (such as Weka)
	System.out.println("Complete!");
    
  }
}
