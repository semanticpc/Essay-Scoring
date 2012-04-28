package features;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.nio.charset.*;

import parser.EssayInstance;


/**
  * Feature for sentence-transition coherence.  More or less average keyword overlap.
  *
  * Doesn't filter on @TAGs, because it looks like the @TAG numbering indicates word reuse.
  *
  * @author Keith Trnka
  *
  */
public class SentenceCoherenceFeatures implements Features
	{
	private static final boolean debug = false;
	

	public SentenceCoherenceFeatures() 
		{
		}
		
	public HashMap<String, Double> getFeatureScores(EssayInstance instance)
		{
		int numWords = 0;
		int overlap = 0;
		
		ArrayList<ArrayList<ArrayList<String>>> paragraphs = instance.getParagraphs();
		for (ArrayList<ArrayList<String>> paragraph : paragraphs)
			{
			HashSet<String> partialParagraph = new HashSet<String>();
			
			for (ArrayList<String> sentence : paragraph)
				{
				HashSet<String> words = new HashSet<String>();
				
				for (String token : sentence)
					{
					token = token.toLowerCase();
					
					// This check reduces correlation a bit in my test,
					// but I have a feeling it's picked up by number of 
					// punctuation counts anyway.
					if (token.length() == 1 && !token.matches("\\w"))
						continue;
					
					if (partialParagraph.contains(token))
						overlap++;
					
					// potential fallback rules:
					// @CAPS1's should overlap with @CAPS1
					// the should overlap with a/an
					// he/she/it should overlap with anything
					
					// exclude overlap with commas, periods, etc
					
					numWords++;
					
					words.add(token);
					}

				// merge the words in
				partialParagraph.addAll(words);
				}

			}
		
		HashMap<String,Double> values = new HashMap<String,Double>();
		values.put("overlap_coherence", new Double(overlap / (double)numWords));
		
		if (debug)
			{
			System.out.println("Overlap Coherence for ID(" + instance.essay_id + ") @ score(" + instance.domain1_score + "): " + values.get("overlap_coherence"));
			}
		return values;
		}
	}
