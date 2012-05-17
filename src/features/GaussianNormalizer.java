package features;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.nio.charset.*;

import parser.EssayInstance;


/**
  * Normalizations based on normal distributions or gaussians.  There are three
  * possible normalizations, specified by the parameters.  The first is the
  * z-score, which is (x-mean)/stddev.  The second is the absolute value of z-score.
  * The third is the actual probability of the z-score.
  *
  * <p>This code was moved from FeatureList for compatibility with held-out data.
  *
  * @author Keith Trnka
  *
  */
public class GaussianNormalizer implements Features
	{
	public static enum Type { ZSCORE, ABS_ZSCORE, NORMAL_PROB };
	
	private static final boolean debug = false;
	
	private HashMap<Integer,double[]> means;
	private HashMap<Integer,double[]> stddev;
	
	private Features baseFeature;
	private String baseName;
	private String name;
	private Type type;
	
	/**
	  * Learns the mean and stddec of the feature for each task.  Run this
	  * after the base feature has been computed.
	  * @param trainingSample The instances to learn the range from (doesn't have to be the full training set).
	  * @param base The base feature instance to use.
	  * @param baseName The name of the base feature
	  */
	public GaussianNormalizer(ArrayList<EssayInstance> trainingSample, Features base, String baseName, Type type)
		{
		means = new HashMap<Integer,double[]>();
		stddev = new HashMap<Integer,double[]>();
		baseFeature = base;
		this.baseName = baseName;
		
		name = baseName + "_" + type.toString().toLowerCase() + "_norm_task";
		this.type = type;
		
		HashMap<Integer,int[]> docs = new HashMap<Integer,int[]>();
		
		// compute the sums
		for (EssayInstance instance : trainingSample)
			{
			double value = getBaseValue(instance);
			
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
		for (EssayInstance instance : trainingSample)
			{
			double value = getBaseValue(instance);
			
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
				System.out.println("Feature " + baseName + " for task/essay_set " + task + ":");
				System.out.println("\tx: " + means.get(task)[0]);
				System.out.println("\ts: " + stddev.get(task)[0]);
				}
			}
		}
		
	public HashMap<String,Double> getFeatureScores(EssayInstance instance)
		{
		double value = getBaseValue(instance);

		// KT:  I'm not sure if the testing set will ever have instances of an unseen
		// essay_set.  If so, this is here for safety.
		// If it's the case, we need to somehow disable this feature if it's
		// the kind of classifier that can have missing features.
		assert(means.containsKey(instance.essay_set) && stddev.containsKey(instance.essay_set));

		// scale it
		double taskMean = means.get(instance.essay_set)[0];
		double taskStddev = stddev.get(instance.essay_set)[0];
		
		// stddev should never be zero - if it is, don't use this normalization.
		assert(taskStddev != 0);
		
		double zscore = (value - taskMean) / taskStddev;
		
		if (type == Type.ABS_ZSCORE)
			zscore = Math.abs(zscore);
		else if (type == Type.NORMAL_PROB)
			zscore = (Math.exp(-Math.pow(zscore, 2)/2)) / (taskStddev * Math.sqrt(2 * Math.PI));
		
		// return it
		HashMap<String,Double> values = new HashMap<String,Double>();
		values.put(name, zscore);
		return values;
		}
	
	/**
	  * Checks to see if base value is already computed.  If so, returns that.
	  * Otherwise computes the value and stores it.
	  */
	private double getBaseValue(EssayInstance instance)
		{
		Double value = instance.getFeature(baseName);
		if (value == null)
			{
			// KT:  You'll end up with duplicate features if it goes through this code path
			// because it'll be computed and stored, then when the normal feature is called
			// then that will also try to store.
			assert(false);
			
			HashMap<String,Double> values = baseFeature.getFeatureScores(instance);
			instance.setFeature(baseName, values.get(baseName));
			value = values.get(baseName);
			}
		
		return value.doubleValue();
		}
	}
