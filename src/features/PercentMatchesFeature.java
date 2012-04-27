package features;

import java.util.*;
import java.util.regex.*;

import parser.EssayInstance;


/**
  * This lets you compute the percentage of tokens that match the
  * input word or regex.  Note that it matches the ENTIRE word or regex,
  * so you have to build the regex to do partial matching yourself.
  *
  * @author Keith Trnka
  *
  */
public class PercentMatchesFeature implements Features
	{
	private Pattern pattern = null;
	private static final boolean debug = false;

	public PercentMatchesFeature(String word)
		{
		this(word, false);
		}

	public PercentMatchesFeature(String regex, boolean isRegex)
		{
		if (isRegex)
			this.pattern = Pattern.compile(regex);
		else
			this.pattern = Pattern.compile(Pattern.quote(regex));
		}
		
	public HashMap<String, Double> getFeatureScores(EssayInstance instance)
		{
		int numWords = 0;
		int matches = 0;
		ArrayList<ArrayList<ArrayList<String>>> paragraphs = instance.getParagraphs();
		for (ArrayList<ArrayList<String>> paragraph : paragraphs)
			{
			for (ArrayList<String> sentence : paragraph)
				{
				for (String token : sentence)
					{
					if (pattern.matcher(token).matches())
						{
						matches++;
						}
					numWords++;
					}

				}

			}
		
		HashMap<String,Double> values = new HashMap<String,Double>();
		values.put("PercentMatches_" + pattern, new Double(matches / (double)numWords));
		
		if (debug)
			System.out.println("Percent matches(" + pattern + ") for ID(" + instance.essay_id + "): " + (matches / (double)numWords));
		return values;
		}
	}
