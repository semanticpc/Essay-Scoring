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
	private static final boolean debug = false;
	public static void buildFeatures(ArrayList<EssayInstance> instances)
		{
		ArrayList<Features> featureInstances = new ArrayList<Features>();
		featureInstances.add(new StopWordRatioFeature());
		featureInstances.add(new LengthFeature());
		featureInstances.add(new AverageWordLengthFeature());

		featureInstances.add(new IDFFeatures(instances));
		featureInstances.add(new SentenceCoherenceFeatures());
		
		// be careful with using these style of % token match features and normalizing to (val-min)/(max-min) per task - there are some 4-10 word "essays" that will throw off the max on the scale
		// Instead of normalizing with min/max, it might be better to normalize to the number of stddev away from the mean, which is a little less sensitive to those outliers
		featureInstances.add(new PercentMatchesFeature(","));
		featureInstances.add(new PercentMatchesFeature("!"));
		featureInstances.add(new PercentMatchesFeature("?"));
		featureInstances.add(new PercentMatchesFeature("the"));
		featureInstances.add(new PercentMatchesFeature("is"));
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
		
		// compute the primary features
		for (EssayInstance instance : instances)
			{
			for (Features feature : featureInstances)
				instance.setFeature(feature.getFeatureScores(instance));
			}
		
		// normalize to create derivative features
		minmaxNormalizePerTask(instances, "AverageWordLength");
		minmaxNormalizePerTask(instances, "OOVs");
		minmaxNormalizePerTask(instances, "obvious_typos");
		minmaxNormalizePerTask(instances, "AverageIDF");
		zscoreNormalizePerTask(instances, "AverageIDF");
		zscoreNormalizePerTask(instances, "overlap_coherence");
		
		// some analysis for feature selection
		correlationTests(instances, 1);
		}
	
	public static void correlationTests(ArrayList<EssayInstance> instances, int essay_id)
		{
		ArrayList<EssayInstance> task = filter(instances, essay_id);
		assert(task.size() > 0);
		
		System.out.println("Correlation for essay " + essay_id + ", " + task.size() + " instances");
		for (String feature : task.get(0).listFeatures())
			System.out.println("\tr for " + feature + ": " + pearson(task, feature));
		}

	/**
	  * Take the filled out instances and for each essay task/set, normalize the feature values to (val-min)/(max-min) aka 0-1.
	  * Stores the value in normalizedFeatureName (can be the same as featureName).
	  *
	  * @author Keith Trnka
	  */
	private static void minmaxNormalizePerTask(ArrayList<EssayInstance> instances, String featureName, String normalizedFeatureName)
		{
		// KT: Yes, using the singleton array is ugly, but it'd be a sin to constantly create new Doubles and re-store them.
		HashMap<Integer,double[]> min = new HashMap<Integer,double[]>();
		HashMap<Integer,double[]> max = new HashMap<Integer,double[]>();
		
		// compute min and max
		for (EssayInstance instance : instances)
			{
			double value = instance.getFeature(featureName);
			
			// update min
			if (!min.containsKey(instance.essay_set))
				min.put(instance.essay_set, new double[] { value });
			else if (min.get(instance.essay_set)[0] > value)
				min.get(instance.essay_set)[0] = value;
			
			// update max
			if (!max.containsKey(instance.essay_set))
				max.put(instance.essay_set, new double[] { value });
			else if (max.get(instance.essay_set)[0] < value)
				max.get(instance.essay_set)[0] = value;
			}
		
		// debugging info
		if (debug)
			{
			List<Integer> tasks = new ArrayList<Integer>(min.keySet());
			Collections.sort(tasks);
			for (Integer task : tasks)
				{
				System.out.println("Feature " + featureName + " for task/essay_set " + task + ":");
				System.out.println("\tmin: " + min.get(task)[0]);
				System.out.println("\tmax: " + max.get(task)[0]);
				}
			}

		// now normalize to min and max
		for (EssayInstance instance : instances)
			{
			double value = instance.getFeature(featureName);
			
			double normalized = value;
			if ( (max.get(instance.essay_set)[0] - min.get(instance.essay_set)[0]) != 0)
				normalized = (value - min.get(instance.essay_set)[0]) / (max.get(instance.essay_set)[0] - min.get(instance.essay_set)[0]);
			
			instance.setFeature(normalizedFeatureName, normalized);
			}
		}
    
	/**
	  * Convenience method to append a _NormTask to the name
	  * @author Keith Trnka
	  */
	private static void minmaxNormalizePerTask(ArrayList<EssayInstance> instances, String featureName)
		{
		minmaxNormalizePerTask(instances, featureName, featureName + "_MinMaxNormTask");
		}

	/**
	  * Measure a normal distribution for each essay, convert values to z-score or whatever it's called.
	  * @author Keith Trnka
	  */
	private static void zscoreNormalizePerTask(ArrayList<EssayInstance> instances, String featureName, String normalizedFeatureName)
		{
		// KT: Yes, using the singleton array is ugly, but it'd be a sin to constantly create new Doubles and re-store them.
		HashMap<Integer,double[]> means = new HashMap<Integer,double[]>();
		HashMap<Integer,int[]> docs = new HashMap<Integer,int[]>();
		
		// compute min and max
		for (EssayInstance instance : instances)
			{
			double value = instance.getFeature(featureName);
			
			if (!means.containsKey(instance.essay_set))
				means.put(instance.essay_set, new double[] { value });
			else
				means.get(instance.essay_set)[0] += value;
			
			if (!docs.containsKey(instance.essay_set))
				docs.put(instance.essay_set, new int[] { 1 });
			else
				docs.get(instance.essay_set)[0]++;
			}
		
		// now convert to means
		for (Integer task : means.keySet())
			means.get(task)[0] /= docs.get(task)[0];
			
		// now compute standard deviation
		HashMap<Integer,double[]> stddev = new HashMap<Integer,double[]>();
		for (EssayInstance instance : instances)
			{
			double value = instance.getFeature(featureName);
			
			double dev = means.get(instance.essay_set)[0] - value;
			dev *= dev;
			
			if (!stddev.containsKey(instance.essay_set))
				stddev.put(instance.essay_set, new double[] { dev });
			else
				stddev.get(instance.essay_set)[0] += dev;
			}
		
		// normalize the stddev, root it
		for (Integer task : stddev.keySet())
			stddev.get(task)[0] = Math.sqrt(stddev.get(task)[0] / (docs.get(task)[0] - 1));
			
		// debugging info
		if (debug)
			{
			List<Integer> tasks = new ArrayList<Integer>(means.keySet());
			Collections.sort(tasks);
			for (Integer task : tasks)
				{
				System.out.println("Feature " + featureName + " for task/essay_set " + task + ":");
				System.out.println("\tx: " + means.get(task)[0]);
				System.out.println("\ts: " + stddev.get(task)[0]);
				}
			}

		// now normalize to min and max
		for (EssayInstance instance : instances)
			{
			double value = instance.getFeature(featureName);
			
			double normalized = value;
			if ( stddev.get(instance.essay_set)[0] != 0)
				normalized = (value - means.get(instance.essay_set)[0]) / stddev.get(instance.essay_set)[0];
			
			instance.setFeature(normalizedFeatureName, normalized);
			}
		}

	/**
	  * Convenience function
	  */
	private static void zscoreNormalizePerTask(ArrayList<EssayInstance> instances, String featureName)
		{
		zscoreNormalizePerTask(instances, featureName, featureName + "_GauNormTask");
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
