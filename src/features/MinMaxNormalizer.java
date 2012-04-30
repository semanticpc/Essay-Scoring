package features;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.nio.charset.*;

import parser.EssayInstance;


/**
  * Normalize the specified feature to (val-min)/(max-min) on a task-by-task basis.
  * This code was moved from FeatureList for compatibility with held-out data.
  *
  * @author Keith Trnka
  *
  */
public class MinMaxNormalizer implements Features
	{
	private static final boolean debug = false;
	
	private HashMap<Integer,double[]> min;
	private HashMap<Integer,double[]> max;
	private Features baseFeature;
	private String baseName;
	private String name;
	
	/**
	  * Learns the min-max range of the feature for each task.  For ideal efficiency, run this
	  * after the base feature has been computed.
	  * @param trainingSample The instances to learn the range from (doesn't have to be the full training set).
	  * @param base The base feature instance to use.
	  * @param baseName The name of the base feature
	  */
	public MinMaxNormalizer(ArrayList<EssayInstance> trainingSample, Features base, String baseName)
		{
		min = new HashMap<Integer,double[]>();
		max = new HashMap<Integer,double[]>();
		baseFeature = base;
		this.baseName = baseName;
		
		name = baseName + "_MinMaxNormTask";
		
		for (EssayInstance instance : trainingSample)
			{
			double value = getBaseValue(instance);
			
			if (!min.containsKey(instance.essay_set))
				min.put(instance.essay_set, new double[] { value });
			else if (min.get(instance.essay_set)[0] > value)
				min.get(instance.essay_set)[0] = value;

			if (!max.containsKey(instance.essay_set))
				max.put(instance.essay_set, new double[] { value });
			else if (max.get(instance.essay_set)[0] < value)
				max.get(instance.essay_set)[0] = value;
			}
		}
		
	public HashMap<String,Double> getFeatureScores(EssayInstance instance)
		{
		double value = getBaseValue(instance);

		// KT:  I'm not sure if the testing set will ever have instances of an unseen
		// essay_set.  If so, this is here for safety.
		// If it's the case, we need to somehow disable this feature if it's
		// the kind of classifier that can have missing features.
		assert(min.containsKey(instance.essay_set) && max.containsKey(instance.essay_set));

		// scale it
		double taskMin = min.get(instance.essay_set)[0];
		double taskMax = max.get(instance.essay_set)[0];
		
		// the min and max should never be the same unless it's a dummy feature maybe?
		assert(taskMin != taskMax);
		double scaled = (value - taskMin) / (taskMax - taskMin);
		
		// return it
		HashMap<String,Double> values = new HashMap<String,Double>();
		values.put(name, scaled);
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
			// KT:  I commented this out because I felt like it'd lead to accidental creation of extra duplicate features
			// if the normalization feature were constructed before the base feature were run.
			//instance.setFeature(baseName, values.get(baseName));
			value = values.get(baseName);
			}
		
		return value.doubleValue();
		}
	}
