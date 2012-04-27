package features;

import java.util.*;
import java.util.regex.*;

import parser.EssayInstance;


/**
  * Average word length, often used in readability scores.  Note that this partially
  * filters the tokens.
  *
  * @author Keith Trnka
  *
  */
public class AverageWordLengthFeature implements Features
	{
	public static final Pattern validWord = Pattern.compile("\\w");
	private static final boolean debug = false;
	
	public AverageWordLengthFeature()
		{
		}	
	
	public HashMap<String, Double> getFeatureScores(EssayInstance instance)
		{
		int numWords = 0;
		int sumLength = 0;
		ArrayList<ArrayList<ArrayList<String>>> paragraphs = instance.getParagraphs();
		for (ArrayList<ArrayList<String>> paragraph : paragraphs)
			{
			for (ArrayList<String> sentence : paragraph)
				{
				for (String token : sentence)
					{
					if (token.charAt(0) != '@' && validWord.matcher(token).find())
						{
						numWords++;
						sumLength += token.length();
						}
					}

				}

			}
		
		HashMap<String,Double> values = new HashMap<String,Double>();
		values.put("AverageWordLength", new Double(sumLength / (double)numWords));
		
		if (debug)
			System.out.println("Average word length for ID(" + instance.essay_id + "): " + (sumLength / (double)numWords));
		return values;
		}
	}
