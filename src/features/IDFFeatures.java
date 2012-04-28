package features;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.nio.charset.*;

import parser.EssayInstance;


/**
  * This computes the average IDF using lowercased tokens and ignoring the @TAGs.
  *
  * @author Keith Trnka
  */
public class IDFFeatures implements Features
	{
	private static final boolean debug = false;
	
	HashMap<String,double[]> idf;

	/**
	  * @param documents The set of instances so that we can compute IDF.  The method shouldn't actually do scoring here.
	  */
	public IDFFeatures(ArrayList<EssayInstance> documents)
		{
		idf = new HashMap<String,double[]>();

		// step 1: compute doc frequency
		for (EssayInstance instance : documents)
			{
			HashSet<String> words = new HashSet<String>();
			
			ArrayList<ArrayList<ArrayList<String>>> paragraphs = instance.getParagraphs();
			for (ArrayList<ArrayList<String>> paragraph : paragraphs)
				{
				for (ArrayList<String> sentence : paragraph)
					{
					for (String token : sentence)
						{
						if (token.charAt(0) == '@')
							continue;
						
						words.add(token.toLowerCase());
						}
					}
				}
			
			// merge them in
			for (String word : words)
				{
				if (idf.containsKey(word))
					idf.get(word)[0]++;
				else
					idf.put(word, new double[] { 1 });
				}
			}
		
		// step 2: invert it
		for (String word : idf.keySet())
			idf.get(word)[0] = Math.log(documents.size() / (double)idf.get(word)[0]);
		}
		
	public HashMap<String, Double> getFeatureScores(EssayInstance instance)
		{
		int numWords = 0;
		
		double sumIdf = 0;
		
		ArrayList<ArrayList<ArrayList<String>>> paragraphs = instance.getParagraphs();
		for (ArrayList<ArrayList<String>> paragraph : paragraphs)
			{
			for (ArrayList<String> sentence : paragraph)
				{
				boolean initialCaps = true;
				for (String token : sentence)
					{
					if (token.charAt(0) == '@')
						continue;
						
					sumIdf += idf.get(token.toLowerCase())[0];
					numWords++;
					}

				}

			}
		
		HashMap<String,Double> values = new HashMap<String,Double>();
		values.put("AverageIDF", new Double(sumIdf / (double)numWords));
		
		if (debug)
			{
			System.out.println("AverageIDF for ID(" + instance.essay_id + "): " + values.get("AverageIDF"));
			}
		return values;
		}
	}
