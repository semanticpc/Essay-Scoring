package features;

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
	  
	public LengthFeature() {
		this.featureScores = new HashMap<String,Double>();
	}	
	
	public HashMap<String, Double> getFeatureScores(EssayInstance instance) {
	    this.featureScores.put("lengthratio", getScore(instance.essay));
	    return this.featureScores;
	}

	/**
	 * 
	 * @param essay
	 * @return
	 */
	public Double getScore(String essay) {
		// TODO Implement the Length Ratio Scorer here
		return 0.0;
	}	
	
}
