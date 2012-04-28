/**
 * 
 */
package parser;

import java.util.regex.*;
import java.util.*;

/**
 * @author semanticpc
 * 
 */
public class EssayInstance {
  // Not sure if we need all these fields.
  // Storing all the fields available in the Training Set File for now
  public int essay_id;
  public int essay_set;
  public String essay;
  public int rater1_domain1 = -1;
  public int rater2_domain1 = -1;
  public int rater3_domain1 = -1;
  
  public int domain1_score = -1;
  
  public int rater1_domain2 = -1;
  public int rater2_domain2 = -1;
  
  public int domain2_score = -1;
  
  public int rater1_trait1 = -1;
  public int rater1_trait2 = -1;
  public int rater1_trait3 = -1;
  public int rater1_trait4 = -1;
  public int rater1_trait5 = -1;
  public int rater1_trait6 = -1;
  public int rater2_trait1 = -1;
  public int rater2_trait2 = -1;
  public int rater2_trait3 = -1;
  public int rater2_trait4 = -1;
  public int rater2_trait5 = -1;
  public int rater2_trait6 = -1;
  public int rater3_trait1 = -1;
  public int rater3_trait2 = -1;
  public int rater3_trait3 = -1;
  public int rater3_trait4 = -1;
  public int rater3_trait5 = -1;
  public int rater3_trait6 = -1;
  
	public static final Pattern paragraphPattern = Pattern.compile("\\s{3,}");
	public static final Pattern sentencePattern = Pattern.compile("(?<=[\\.?!][^\\w\\s]?)\\s+(?![a-z])");
	public static final Pattern wordPattern = Pattern.compile("\\s+");
	public static final Pattern frontMatter = Pattern.compile("([^\\w@]+)([\\w@].*)");
	public static final Pattern backMatter = Pattern.compile("(.*\\w)(\\W+)");
	
	/** This list is partially from Perl Lingua::EN::Sentence */
	public static final Pattern endsInAbbreviation = Pattern.compile(".*(Mr|Mrs|Dr|Jr|Ms|Prof|Sr|dept|Univ|Inc|Ltd|Co|Corp|Mt|Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Oct|Nov|Dec|Sept|vs|etc|no)\\.");

	private ArrayList<ArrayList<ArrayList<String>>> cachedParse = null;
  
  HashMap<String,Double> features;
  
  public EssayInstance() {
    this.features = new HashMap<String,Double>();
  }
  
  /**
   * @param featureScores
   */
  public void setFeature(HashMap<String,Double> featureScores) {
    for (String featureKey : featureScores.keySet()) {
      if (this.features.containsKey(featureKey)) this.features.put(
          featureKey.concat("1"), featureScores.get(featureKey));
      else this.features.put(featureKey, featureScores.get(featureKey));
    }
  }
  
  public HashMap<String,Double> getFeatures() {
    return this.features;
  }
  
  /**
    * I'm not sure if features was supposed to be package private, but if so, then this is an accessor.
    * @author Keith Trnka
	*/
  public double getFeature(String feature)
	{
	return features.get(feature);
	}

  public List<String> listFeatures()
	{
	ArrayList<String> fList = new ArrayList<String>(features.keySet());
	Collections.sort(fList);
	return fList;
	}

  /**
    * I'm not sure if features was supposed to be package private, but if so, then this is a mutator.
    * @author Keith Trnka
	*/
  public void setFeature(String feature, double value)
	{
	features.put(feature, value);
	}
  
  /**
    * Shows the ID and parsed contents.
	*/
  public String toString()
	{
	StringBuilder b = new StringBuilder("ID(" + essay_id + "): ");
	
	ArrayList<ArrayList<ArrayList<String>>> paragraphs = getParagraphs();
	for (ArrayList<ArrayList<String>> paragraph : paragraphs)
		{
		for (ArrayList<String> sentence : paragraph)
			{
			for (String token : sentence)
				{
				b.append(token);
				b.append(" ");
				}
			b.append(" ");
			}
		b.append("\n");
		}
	return b.toString();
	}

	/**
	  * See the toString method for an example of how to iterate over the return value.
	  *
	  * @returns the parsed structure of the text at the paragraph, sentence, word levels
	  */
	public ArrayList<ArrayList<ArrayList<String>>> getParagraphs()
		{
		if (cachedParse != null)
			return cachedParse;
			
		cachedParse = new ArrayList<ArrayList<ArrayList<String>>>();
		
		String[] paragraphs = paragraphPattern.split(essay);
		for (String paragraph : paragraphs)
			{
			ArrayList<ArrayList<String>> sentenceList = new ArrayList<ArrayList<String>>();
			cachedParse.add(sentenceList);
			
			// boring sentence splitter
			String[] sentences = sentencePattern.split(paragraph);
			
			// to do: (static) load an abbreviation list and merge sentences back ending in abbrevs
			// I'd do it in regex, but you can't do variable width negative lookbehinds (I can't believe I know that)
			boolean mergeIntoPrevious = false;
			for (String sentence : sentences)
				{
				ArrayList<String> wordList;
				if (mergeIntoPrevious)
					wordList = sentenceList.get(sentenceList.size() - 1);
				else
					{
					wordList = new ArrayList<String>();
					sentenceList.add(wordList);
					}
				
				// decide what the next sentence should do
				if (endsInAbbreviation.matcher(sentence).matches())
					mergeIntoPrevious = true;
				else
					mergeIntoPrevious = false;
				
				// split on spaces first, then strip off leading and trailing punctuation
				String[] tokens = wordPattern.split(sentence);
				for (String token : tokens)
					{
					// sometimes we get an empty token somehow, probably an extra space at the start or end?
					if (token.length() == 0)
						continue;

					Matcher m = frontMatter.matcher(token);
					if (m.matches())
						{
						// explode group 1
						String front = m.group(1);
						for (int i = 0; i < front.length(); i++)
							wordList.add(String.valueOf(front.charAt(i)));
						
						// process group 2
						token = m.group(2);
						}
					
					m = backMatter.matcher(token);
					if (m.matches())
						{
						// save group 1
						wordList.add(m.group(1));
						
						// explode group 2
						String back = m.group(2);
						for (int i = 0; i < back.length(); i++)
							wordList.add(String.valueOf(back.charAt(i)));

						}
					else 
						{
						wordList.add(token);
						}
					}
				}
			}
		return cachedParse;
		}
		

}
