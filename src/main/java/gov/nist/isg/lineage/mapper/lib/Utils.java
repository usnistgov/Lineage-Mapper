// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package main.java.gov.nist.isg.lineage.mapper.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTable;

public class Utils {


  public static String getFileName(String filePattern, String regex, int index) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(filePattern);

    // Check if regex is correct. We expect 3 groups: (*)({iii})(*)
    if (!matcher.find() || matcher.groupCount() != 3)
      return null;

    // The matcher should fine at group: 0 - the entire string,
    // group 1 = prefix
    // group 2 = {i}
    // group 3 = suffix
    String prefix = matcher.group(1);
    int iCount = matcher.group(2).length() - 2;
    String suffix = matcher.group(3);

    String nameMatcher = prefix + "%0" + iCount + "d" + suffix;
    return String.format(nameMatcher, index);
  }

  /**
   * Get the highest pixel value contained within the array.
   *
   * @param data the short array to find the max value of.
   * @return a short containing the highest number in the array.
   */
  public static short getMaxValue(short[] data) {
    short maxval = Short.MIN_VALUE;
    for (int i = 0; i < data.length; i++) {
      maxval = (data[i] > maxval) ? data[i] : maxval;
    }
    return maxval;
  }

  public static void exportToCSVFile(JTable table, File fh) {

    try {
      FileWriter fw = new FileWriter(fh);
      PrintWriter pw = new PrintWriter(fw);
      int c = table.getColumnCount();
      int r = table.getRowCount();

      // print the column labels
      for (int j = 0; j < c; j++) {
        if (j > 0) pw.print(",");
        pw.print(table.getColumnName(j));
      }
      pw.print("\r\n");

      for (int i = 0; i < r; i++) {
        for (int j = 0; j < c; j++) {
          if (j > 0) pw.print(",");
          pw.print(table.getValueAt(i, j));
        }
        pw.print("\r\n");
      }
      //Flush the output to the file
      pw.flush();
      pw.close();
      fw.close();
    } catch (IOException e) {
      Log.error(e.getMessage());
    }
  }

  public static LinkedHashMap<Integer, Double> sortHashMapByValuesD(
      HashMap<Integer, Double> passedMap) {
    List<Integer> mapKeys = new ArrayList<Integer>(passedMap.keySet());
    List<Double> mapValues = new ArrayList<Double>(passedMap.values());
    Collections.sort(mapValues, Collections.reverseOrder());
    Collections.sort(mapKeys);

    LinkedHashMap<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();

    Iterator<Double> valueIt = mapValues.iterator();
    while (valueIt.hasNext()) {
      Double val = valueIt.next();
      Iterator<Integer> keyIt = mapKeys.iterator();

      while (keyIt.hasNext()) {
        Integer key = keyIt.next();
        Double comp1 = passedMap.get(key);
        Double comp2 = val;

        if (comp1.equals(comp2)) {
          passedMap.remove(key);
          mapKeys.remove(key);
          sortedMap.put((Integer) key, (Double) val);
          break;
        }
      }
    }
    return sortedMap;
  }


}
