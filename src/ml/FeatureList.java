/**
 * 
 */
package ml;

import java.util.ArrayList;

import parser.EssayInstance;
import features.*;
import java.io.*;
import java.util.*;

/**
  * Sorry Praveen, I changed the indentation cause it wasn't auto-working with this editor and I can't bring myself to set tab width 2.
  * @author semanticpc
  * 
  */
public class FeatureList
	{
	/** normalization type for the zscore function */
	public static enum NormType { BASIC, ABS, PROB };
	
	private static final boolean debug = false;
	public static void buildFeatures(ArrayList<EssayInstance> instances)
		{
		ArrayList<Features> featureInstances = new ArrayList<Features>();
		featureInstances.add(new LengthFeature());
		
		Features wordLengthFeature = new AverageWordLengthFeature();
		featureInstances.add(wordLengthFeature);

		Features idfFeature = new IDFFeatures(instances);
		featureInstances.add(idfFeature);
		
		Features coherenceFeature = new SentenceCoherenceFeatures();
		featureInstances.add(coherenceFeature);
		
		// be careful with using these style of % token match features and normalizing to (val-min)/(max-min) per task - there are some 4-10 word "essays" that will throw off the max on the scale
		// Use the zcore normalization instead if you want normalization.
		featureInstances.add(new PercentMatchesFeature(","));
		featureInstances.add(new PercentMatchesFeature("!"));
		featureInstances.add(new PercentMatchesFeature("?"));
		Features theFeature = new PercentMatchesFeature("the");
		featureInstances.add(theFeature);
		featureInstances.add(new PercentMatchesFeature("is"));
		featureInstances.add(new PercentMatchesFeature("@.*", true));
		
		// note:  this probably isn't platform safe; it should do something like take the path of RunPrediction classfile and do a relpath dynamically
		Features wordlistFeatures = null;
		try
			{
			wordlistFeatures = new WordListFeatures(app.RunPrediction.resourcesPath + "/scowl-7.1/american_50.latin1");
			featureInstances.add(wordlistFeatures);
			}
		catch (IOException exc)
			{
			System.err.println("Unable to load word list: " + exc);
			}

		try
			{
			featureInstances.add(new StopWordRatioFeature("Resources/stopwords.txt"));
			}
		catch (IOException exc)
			{
			System.err.println("Unable to load stopwords: " + exc);
			}
		
		// compute the primary features
		for (EssayInstance instance : instances)
			{
			for (Features feature : featureInstances)
				instance.setFeature(feature.getFeatureScores(instance));
			}
		
		ArrayList<Features> normalizationFeatures = new ArrayList<Features>();
		normalizationFeatures.add(new MinMaxNormalizer(instances, wordlistFeatures, "OOVs"));
		normalizationFeatures.add(new MinMaxNormalizer(instances, wordlistFeatures, "obvious_typos"));
		normalizationFeatures.add(new MinMaxNormalizer(instances, wordLengthFeature, "AverageWordLength"));
		normalizationFeatures.add(new MinMaxNormalizer(instances, idfFeature, "AverageIDF"));
		normalizationFeatures.add(new GaussianNormalizer(instances, idfFeature, "AverageIDF", GaussianNormalizer.Type.ZSCORE));
		normalizationFeatures.add(new GaussianNormalizer(instances, idfFeature, "AverageIDF", GaussianNormalizer.Type.ABS_ZSCORE));
		
		normalizationFeatures.add(new GaussianNormalizer(instances, coherenceFeature, "overlap_coherence", GaussianNormalizer.Type.ZSCORE));
		normalizationFeatures.add(new GaussianNormalizer(instances, coherenceFeature, "overlap_coherence", GaussianNormalizer.Type.NORMAL_PROB));

		normalizationFeatures.add(new GaussianNormalizer(instances, theFeature, "PercentMatches_\\Qthe\\E", GaussianNormalizer.Type.ZSCORE));
		normalizationFeatures.add(new GaussianNormalizer(instances, theFeature, "PercentMatches_\\Qthe\\E", GaussianNormalizer.Type.NORMAL_PROB));
		normalizationFeatures.add(new GaussianNormalizer(instances, theFeature, "PercentMatches_\\Qthe\\E", GaussianNormalizer.Type.ABS_ZSCORE));

		// compute the normalization features
		for (EssayInstance instance : instances)
			{
			for (Features feature : normalizationFeatures)
				instance.setFeature(feature.getFeatureScores(instance));
			}
		
		// some analysis for quick and dirty guessing whether features are good or bad
		correlationTests(instances, 1);
		correlationTests(instances, 2);
		correlationTests(instances, 3);
		
		// show the average score, etc
		analyseFeature(filter(instances, 1), "grade");
		
		try
			{
			// generate an ARFF with real valued output class (for regression if possible)
			saveARFFRealClass(filter(instances, 1), "training_essay1_real.arff");
			
			saveARFFDiscreteClass(filter(instances, 1), "training_essay1_discrete.arff");

			// generate an ARFF where grade is turned into a binary feature based on the threshold (in this case over/under 8.5)
			saveARFFThresholdClass(filter(instances, 1), "training_essay1_t8.5.arff", 8.5);
			}
		catch (IOException exc)
			{
			System.err.println("Error saving ARFF: " + exc);
			}
		}
	
	/**
	  * Save the data as an ARFF file where grade is specified as a real-valued feature.
	  * This type of output class doesn't work with most of Weka.
	  */
	public static void saveARFFRealClass(ArrayList<EssayInstance> instances, String filename) throws IOException
		{
		PrintWriter out = new PrintWriter(new FileWriter(filename));
		
		out.println("% Autogenerated by FeatureList.java");
		out.println("@relation training_essay_set_1");
		
		List<String> features = instances.get(0).listFeatures();
		for (String feature : features)
			{
			out.println("@attribute " + arffEscapeName(feature) + " real");
			}
		out.println("@attribute grade real");
		
		out.println("@data");
		for (EssayInstance instance : instances)
			{
			for (String feature : features)
				out.print(instance.getFeature(feature) + ",");
			out.println(instance.getFeature("grade"));
			}

		out.close();
		}

	/**
	  * Save the data as an ARFF file where grade is specified as a discrete feature.
	  * This type of output class works with Weka, but the machine learning won't
	  * take into account that it's really a continuous scale.
	  */
	public static void saveARFFDiscreteClass(ArrayList<EssayInstance> instances, String filename) throws IOException
		{
		PrintWriter out = new PrintWriter(new FileWriter(filename));
		
		out.println("% Autogenerated by FeatureList.java");
		out.println("@relation training_essay_set_1");
		
		List<String> features = instances.get(0).listFeatures();
		for (String feature : features)
			{
			out.println("@attribute " + arffEscapeName(feature) + " real");
			}
		
		// do the magic for discretization
		HashMap<Double,int[]> histogram = buildHistogram(instances, "grade");
		out.print("@attribute grade {");
		ArrayList<Double> values = new ArrayList<Double>(histogram.keySet());
		Collections.sort(values);
		for (int i = 0; i < values.size(); i++)
			{
			if (i > 0)
				out.print(",");
			out.print(values.get(i));
			}
		out.println("}");
		
		out.println("@data");
		for (EssayInstance instance : instances)
			{
			for (String feature : features)
				out.print(instance.getFeature(feature) + ",");
			out.println(instance.getFeature("grade"));
			}

		out.close();
		}
		
	/**
	  * Save the data as an ARFF file where grade is specified as a binary
	  * feature of less-than or greater-than-or-equal-to the threshold.
	  * This type of feature works nicely with Weka.
	  */
	public static void saveARFFThresholdClass(ArrayList<EssayInstance> instances, String filename, double gradeThreshold) throws IOException
		{
		PrintWriter out = new PrintWriter(new FileWriter(filename));
		
		out.println("% Autogenerated by FeatureList.java");
		out.println("@relation training_essay_set_1");
		
		List<String> features = instances.get(0).listFeatures();
		for (String feature : features)
			{
			out.println("@attribute " + arffEscapeName(feature) + " real");
			}
		out.println("@attribute grade {0,1}");
		
		out.println("@data");
		for (EssayInstance instance : instances)
			{
			for (String feature : features)
				out.print(instance.getFeature(feature) + ",");
			out.println(instance.getFeature("grade") < gradeThreshold ? 0 : 1);
			}

		out.close();
		}
	
	public static HashMap<Double,int[]> buildHistogram(ArrayList<EssayInstance> instances, String feature)
		{
		HashMap<Double,int[]> histogram = new HashMap<Double,int[]>();
		for (EssayInstance instance : instances)
			{
			Double value = Double.valueOf(instance.getFeature(feature));
			if (histogram.containsKey(value))
				histogram.get(value)[0]++;
			else
				histogram.put(value, new int[] { 1 });
			}
		return histogram;
		}

	public static void analyseFeature(ArrayList<EssayInstance> instances, String feature)
		{
		System.out.println("Analysis of feature " + feature);
		// count the number of unique values and/or decide if it's discrete
		HashMap<Double,int[]> histogram = buildHistogram(instances, feature);
		
		if (histogram.size() <= 20)
			{
			System.out.println("\tdiscrete, " + histogram.size() + " values");
			
			ArrayList<Double> values = new ArrayList<Double>(histogram.keySet());
			Collections.sort(values);
			for (Double value : values)
				System.out.println("\t\t" + value + ": " + histogram.get(value)[0]);
			}
		else
			{
			System.out.println("\tcontinuous");
			}
		
		System.out.println("\tmean: " + getMean(instances, feature));
		}
	
	public static double getMean(ArrayList<EssayInstance> instances, String feature)
		{
		double sum = 0;
		
		for (EssayInstance instance : instances)
			sum += instance.getFeature(feature);
		
		return sum / instances.size();
		}
	
	public static String arffEscapeName(String name)
		{
		name = name.replaceAll("\\\\Q|\\\\E", "");	// strip \\Q \\E
		name = name.replaceAll("!", "exclamation_mark");
		name = name.replaceAll("\\?", "question_mark");
		name = name.replaceAll("\\.\\*", "dot_star");
		name = name.replaceAll(",", "comma");
		name = name.replaceAll("@", "at_sign");
		
		return name;
		}
	
	public static void correlationTests(ArrayList<EssayInstance> instances, int essay_id)
		{
		ArrayList<EssayInstance> task = filter(instances, essay_id);
		assert(task.size() > 0);
		
		System.out.println("Pearson correlation coefficients with domain1_score for essay " + essay_id + ", " + task.size() + " instances");
		for (String feature : task.get(0).listFeatures())
			System.out.println("\tr for " + feature + ": " + pearson(task, feature));
		}
	
	/**
	  * Compute Pearson correlation coefficient between the domain_score1 and the specified feature.
	  * Note:  You shouldn't run this on the full set of instances, but filter by instance type.
	  *
	  * I originally tried Spearman correlation, but when a feature is undefined that makes it sort
	  * according to essay_id, which (ironically) is highly correlated for some reason.  The code is
	  * ugly as a result of coding Spearman first (but you can swap back and forth with 2 line changes).
	  *
	  * Note that because we're passing in a single task's instances, any of the normalizations are
	  * basically divide-by-invariant.  So they have minimal effect.
	  *
	  * @author Keith Trnka
	  */
	public static double pearson(ArrayList<EssayInstance> filteredInstances, final String feature)
		{
		// sort them according to domain_score
		Collections.sort(filteredInstances, new Comparator<EssayInstance>(){
			public int compare(EssayInstance a, EssayInstance b)
				{
				return a.domain1_score - b.domain1_score;
				}
			});
		
		// now store their ranks accoring to domain_score
		HashMap<Integer,Integer> scoreRanks = new HashMap<Integer,Integer>();
		for (int i = 0; i < filteredInstances.size(); i++)
			scoreRanks.put(filteredInstances.get(i).essay_id, filteredInstances.get(i).domain1_score);
		
		// sort them according to the feature
		Collections.sort(filteredInstances, new Comparator<EssayInstance>(){
			public int compare(EssayInstance a, EssayInstance b)
				{
				return (int)(10000 * (a.getFeature(feature) - b.getFeature(feature)));
				}
			});

		// now store their ranks accoring to domain_score
		HashMap<Integer,Integer> featureRanks = new HashMap<Integer,Integer>();
		for (int i = 0; i < filteredInstances.size(); i++)
			featureRanks.put(filteredInstances.get(i).essay_id, (int)(10000 * filteredInstances.get(i).getFeature(feature)));
		
		// compute the mean domain score and mean feature score
		double meanScoreRank = 0;
		for (int scoreRank : scoreRanks.values())
			meanScoreRank += scoreRank;
		meanScoreRank /= filteredInstances.size();
			
		double meanFeatureRank = 0;
		for (int featureRank : featureRanks.values())
			meanFeatureRank += featureRank;
		meanFeatureRank /= filteredInstances.size();

		// now compute the components
		double prod = 0;
		double score_square = 0;
		double feature_square = 0;
		for (EssayInstance instance : filteredInstances)
			{
			double scoreRank = scoreRanks.get(instance.essay_id);
			double featureRank = featureRanks.get(instance.essay_id);
			
			double scoreDiff = scoreRank - meanScoreRank;
			double featureDiff = featureRank - meanFeatureRank;
			
			prod += scoreDiff * featureDiff;
			score_square += scoreDiff * scoreDiff;
			feature_square += featureDiff * featureDiff;
			}
		
		double rs = prod / Math.sqrt(score_square * feature_square);
		
		return rs;
		}
	
	public static ArrayList<EssayInstance> filter(ArrayList<EssayInstance> instances, int essay_set)
		{
		ArrayList<EssayInstance> filtered = new ArrayList<EssayInstance>();
		for (EssayInstance instance : instances)
			if (instance.essay_set == essay_set)
				filtered.add(instance);
		
		return filtered;
		}
	}
