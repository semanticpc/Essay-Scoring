package features;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.nio.charset.*;

import parser.EssayInstance;


/**
  * This does various computations that require a word list to spellcheck against.
  * It computes multiple feature values:
  * <ul>
  * <li>Percent OOV (case-sensitive except for start-of-sentence)
  * <li>Percent obvious typos<br>This is very accurate but for some reason it thinks "Some" is a typo, probably from Somme.  Maybe it's a case where sentence-final punctuation was missing.  Coverage could be better for multiple errors like how to spell vacuum.
  * </ul>
  *
  * The features ignore the @TAG types of things and punctuation is considered in-vocabulary.
  *
  * <p>To do:  Something like the percentage of tokens with capitalization errors.  Make it play nice with spacing errors like "time?Only".  I think those are
  * actually OCR errors or something.  But I don't want to split amazon.com or anything like that.
  *
  * @author Keith Trnka
  *
  */
public class WordListFeatures implements Features
	{
	private static final boolean debug = false;
	HashSet<String> lexicon = null;
	HashSet<String> lexiconLC = null;
	HashSet<String> typos = null;
	HashSet<String> typosLC = null;

	public WordListFeatures(String wordlistPath) throws IOException
		{
		lexicon = new HashSet<String>();
		lexiconLC = new HashSet<String>();
		typos = new HashSet<String>();
		typosLC = new HashSet<String>();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(wordlistPath), Charset.forName("ISO-8859-1")));
		String line;
		while ( (line = in.readLine()) != null )
			{
			line = line.trim();
			lexicon.add(line);
			lexiconLC.add(line.toLowerCase());
			}
		in.close();
		
		// generate typo lists
		
		// some patterns used to generate typos from the real-word list
		Pattern iePattern = Pattern.compile("ie");
		Pattern eiPattern = Pattern.compile("ie");
		Pattern doubleLetterPattern = Pattern.compile("(\\w)\\1");
		
		// The apostrophe error might be dubious cause it might actually be an OCR error.
		// Also, it's deliberately ignoring 's cause I had too many cases where X's was in lexicon by Xs wasn't (even though it was legit)
		Pattern aposPattern = Pattern.compile("'(?!s)");
		
		
		for (String word : lexicon)
			{
			Matcher m = iePattern.matcher(word);
			if (m.find())
				{
				String candidate = m.replaceFirst("ei");
				if (!lexicon.contains(candidate))
					{
					typos.add(candidate);
//					System.out.println("Potential typo: " + candidate);
					}
				if (!lexiconLC.contains(candidate.toLowerCase()))
					typosLC.add(candidate.toLowerCase());
				}
			m = eiPattern.matcher(word);
			if (m.find())
				{
				String candidate = m.replaceFirst("ie");
				if (!lexicon.contains(candidate))
					{
					typos.add(candidate);
//					System.out.println("Potential typo: " + candidate);
					}
				if (!lexiconLC.contains(candidate.toLowerCase()))
					typosLC.add(candidate.toLowerCase());
				}
			
			m = doubleLetterPattern.matcher(word);
			if (m.find())
				{
				String candidate = m.replaceFirst(m.group(1));
				if (!lexicon.contains(candidate))
					{
					typos.add(candidate);
//					System.out.println("Potential typo: " + candidate);
					}
				if (!lexiconLC.contains(candidate.toLowerCase()))
					typosLC.add(candidate.toLowerCase());
				}
			m = aposPattern.matcher(word);
			if (m.find())
				{
				String candidate = m.replaceFirst("");
				if (!lexicon.contains(candidate))
					{
					typos.add(candidate);
//					System.out.println("Potential typo: " + candidate);
					}
				if (!lexiconLC.contains(candidate.toLowerCase()))
					typosLC.add(candidate.toLowerCase());
				}
			}
		
		// manually add punctuation symbols
		String[] punctuation = new String [] {",", ".", "?", "-", "!", "'", "\"", "(", ")", "$", ":", ";"};
		for (String punct : punctuation)
			{
			lexicon.add(punct);
			lexiconLC.add(punct);
			}
		}
		
	public HashMap<String, Double> getFeatureScores(EssayInstance instance)
		{
		int numWords = 0;
		
		int matches = 0;
		int numTypos = 0;
		
		ArrayList<ArrayList<ArrayList<String>>> paragraphs = instance.getParagraphs();
		for (ArrayList<ArrayList<String>> paragraph : paragraphs)
			{
			for (ArrayList<String> sentence : paragraph)
				{
				boolean initialCaps = true;
				for (String token : sentence)
					{
					// ignore those darn @tags
					if (token.charAt(0) == '@')
						{
						initialCaps = false;
						continue;
						}

					if (initialCaps)
						{
						if (lexiconLC.contains(token.toLowerCase()))
							matches++;
						else if (typosLC.contains(token.toLowerCase()))
							{
							numTypos++;
//							System.out.println("Typo: " + token);
							}
						}
					else 
						{
						if (lexicon.contains(token))
							{
							matches++;
							}
						else if (typos.contains(token))
							{
							numTypos++;
//							System.out.println("Typo: " + token);
							}
						}

					numWords++;
					initialCaps = false;
					
					// quotations might have initial caps after them
					// This could be improved to be in effect for only the opening quote.
					if (token.equals("\""))
						initialCaps = true;
					}

				}

			}
		
		HashMap<String,Double> values = new HashMap<String,Double>();
		values.put("OOVs", new Double(1 - matches / (double)numWords));
		values.put("obvious_typos", new Double(numTypos / (double)numWords));
		
		if (debug)
			{
			System.out.println("OOVs for ID(" + instance.essay_id + "): " + values.get("OOVs"));
			System.out.println("Obvious typos for ID(" + instance.essay_id + "): " + values.get("obvious_typos"));
			}
		return values;
		}
	}
