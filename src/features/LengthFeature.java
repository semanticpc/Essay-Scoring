package features;

import java.util.ArrayList;
import java.util.HashMap;

import parser.EssayInstance;


/**
 * 
 * @author burns
 *
 */
public class LengthFeature implements Features {

	HashMap<String,Double> featureScores;
	String essayString;
	private static int maxWordCount = -1;
	  
	public LengthFeature() {
		this.featureScores = new HashMap<String,Double>();
	}	
	
	public HashMap<String, Double> getFeatureScores(EssayInstance instance) {
	    this.featureScores.put("lengthratio", getScore(instance.essay));
	    return this.featureScores;
	}

	/**
	 * The length of this essay normalized against the length of the longest essay.
	 * 
	 * @param essay
	 * @return
	 */
	public Double getScore(String essay) {
		
		String[] words = essay.split("\\s");
		return Double.valueOf(((double) words.length) / getMaxWordCount());
	}
	
	/**
	 * Finds the essay with the greatest word count, and stores the count in a static class variable.
	 * 
	 * @return largest word count
	 */
	private int getMaxWordCount() {
		if (maxWordCount == -1) {
			ArrayList<EssayInstance> instances = app.RunPrediction.instances;
			
			// compute the word length of the longest essay
			for (EssayInstance instance : instances) {
				String thisEssay = instance.essay;
				String[] words = thisEssay.split("\\s");
				int thisEssayWordCount = words.length;
				if (thisEssayWordCount > maxWordCount)
					maxWordCount = thisEssayWordCount;
			}
		}
		
		return maxWordCount;
	}
	
}
