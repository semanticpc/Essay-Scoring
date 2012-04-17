/**
 * 
 */
package parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author semanticpc
 * 
 */
public class EssayInstanceParser {
  public ArrayList<EssayInstance> parse(String filename, boolean header) {
    ArrayList<EssayInstance> eassyInstances = new ArrayList<EssayInstance>();
    BufferedReader bReader;
    try {
      bReader = new BufferedReader(new FileReader(filename));
      
      String line;
      while ((line = bReader.readLine()) != null) {
        if (header == true) {
          header = false;
          continue;
        }
        
        EssayInstance essay = parseFields(line);
        
        eassyInstances.add(essay);
      }
      bReader.close();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NumberFormatException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return eassyInstances;
  }
  
  /**
   * @param line
   * @return
   */
  private EssayInstance parseFields(String line) {
    String[] fields = line.split("\t");
    EssayInstance essay = new EssayInstance();
    essay.essay_id = Integer.parseInt(fields[0]);
    essay.essay_set = Integer.parseInt(fields[1]);
    essay.essay = fields[2];
    essay.rater1_domain1 = Integer.parseInt(fields[3]);
    essay.rater2_domain1 = Integer.parseInt(fields[4]);
    essay.rater3_domain1 = parseInt(fields[5]);
    
    essay.domain1_score = Integer.parseInt(fields[6]);
    if (fields.length <= 7) return essay;
    essay.rater1_domain2 = parseInt(fields[7]);
    if (fields.length <= 8) return essay;
    essay.rater2_domain2 = parseInt(fields[8]);
    
    if (fields.length <= 9) return essay;
    essay.domain2_score = parseInt(fields[9]);
    
    if (fields.length <= 10) return essay;
    essay.rater1_trait1 = parseInt(fields[10]);
    
    if (fields.length <= 11) return essay;
    essay.rater1_trait2 = parseInt(fields[11]);
    if (fields.length <= 12) return essay;
    essay.rater1_trait3 = parseInt(fields[12]);
    if (fields.length <= 13) return essay;
    essay.rater1_trait4 = parseInt(fields[13]);
    if (fields.length <= 14) return essay;
    essay.rater1_trait5 = parseInt(fields[14]);
    if (fields.length <= 15) return essay;
    essay.rater1_trait6 = parseInt(fields[15]);
    if (fields.length <= 16) return essay;
    essay.rater2_trait1 = parseInt(fields[16]);
    if (fields.length <= 17) return essay;
    essay.rater2_trait2 = parseInt(fields[17]);
    if (fields.length <= 18) return essay;
    essay.rater2_trait3 = parseInt(fields[18]);
    if (fields.length <= 19) return essay;
    essay.rater2_trait4 = parseInt(fields[19]);
    if (fields.length <= 20) return essay;
    essay.rater2_trait5 = parseInt(fields[20]);
    if (fields.length <= 21) return essay;
    essay.rater2_trait6 = parseInt(fields[21]);
    if (fields.length <= 22) return essay;
    essay.rater3_trait1 = parseInt(fields[22]);
    if (fields.length <= 23) return essay;
    essay.rater3_trait2 = parseInt(fields[23]);
    if (fields.length <= 24) return essay;
    essay.rater3_trait3 = parseInt(fields[24]);
    if (fields.length <= 25) return essay;
    essay.rater3_trait4 = parseInt(fields[25]);
    if (fields.length <= 26) return essay;
    essay.rater3_trait5 = parseInt(fields[26]);
    if (fields.length <= 27) return essay;
    essay.rater3_trait6 = parseInt(fields[27]);
    return essay;
  }
  
  /**
   * @param string
   * @return
   */
  private int parseInt(String input) {
    if (input.equals("")) return -1;
    else return Integer.parseInt(input);
  }
}
