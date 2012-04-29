/**
 * 
 */
package features;

import java.util.*;
import java.io.*;

import parser.EssayInstance;

/**
 * @author semanticpc
 * 
 */
public class StopWordRatioFeature implements Features {
  HashSet<String> stopwords;

  public StopWordRatioFeature(String stopwordPath) throws IOException {
	stopwords = new HashSet<String>();
	
	BufferedReader in = new BufferedReader(new FileReader(stopwordPath));
	String line;
	while ( (line = in.readLine()) != null)
		{
		line.trim();
		if (line.length() == 0 || line.charAt(0) == '#')
			continue;
		stopwords.add(line.toLowerCase());
		}
	in.close();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see features.Features#getFeatureScores()
   */
  //@Override
  public HashMap<String,Double> getFeatureScores(EssayInstance instance) {
	int numStopwords = 0, numWords = 0;
	
	ArrayList<ArrayList<ArrayList<String>>> paragraphs = instance.getParagraphs();
	for (ArrayList<ArrayList<String>> paragraph : paragraphs)
		{
		for (ArrayList<String> sentence : paragraph)
			{
			for (String token : sentence)
				{
				// filter for words that don't make sense (punctuation, for instance)
				if (token.length() == 1 && !token.matches("\\w"))
					continue;
				
				if (stopwords.contains(token.toLowerCase()))
					numStopwords++;
					
				numWords++;
				}
			}
		}
				
	HashMap<String,Double> featureScores = new HashMap<String,Double>();
    featureScores.put("stopword_ratio", numStopwords / (double)numWords);
    return featureScores;
  }
  
}
