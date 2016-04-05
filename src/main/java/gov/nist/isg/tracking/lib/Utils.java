// Disclaimer: IMPORTANT: This software was developed at the National
// Institute of Standards and Technology by employees of the Federal
// Government in the course of their official duties. Pursuant to
// title 17 Section 105 of the United States Code this software is not
// subject to copyright protection and is in the public domain. This
// is an experimental system. NIST assumes no responsibility
// whatsoever for its use by other parties, and makes no guarantees,
// expressed or implied, about its quality, reliability, or any other
// characteristic. We would appreciate acknowledgement if the software
// is used. This software can be redistributed and/or modified freely
// provided that any derivative works bear some notice that they are
// derived from it, and any modified versions bear some notice that
// they have been modified.

package main.java.gov.nist.isg.tracking.lib;

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

import javax.swing.*;

/**
 * Created by mmajurski on 5/19/14.
 */
public class Utils {

  /**
   * Get the highest pixel value contained within the array.
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
      for(int j = 0; j < c; j++) {
        if(j>0)  pw.print(",");
        pw.print(table.getColumnName(j));
      }
      pw.print("\r\n");

      for(int i = 0; i < r; i++) {
        for(int j = 0; j < c; j++) {
          if(j>0)  pw.print(",");
          pw.print(table.getValueAt(i,j));
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
