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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class TrackerFileManager {

  /**
   * Finds all of the files contained within a directory.
   *
   * @param dir the File object to search for files as a directory.
   * @return List of files found.
   */
  public static List<File> findFilesInDirectory(File dir) {
    List<File> output = new ArrayList<File>();

    // if this is a directory, find the files within it that have the extension ext
    if (dir.isDirectory()) {
      File[] fn = dir.listFiles();
      for (int i = 0; i < fn.length; i++) {
        output.add(fn[i]);
      }
    }

    return output;
  }

  /**
   * Filter the Files in a List by their suffix. Only the last name in the path is compared to the
   * suffix.
   *
   * @param fileList List to filter, only the last name in the file path is checked.
   * @param suffix   String defining the suffix to match.
   * @return List containing the Files that match the suffix.
   */
  public static List<File> filterFilenamesBySuffix(List<File> fileList, String suffix) {
    List<File> output = new ArrayList<File>();

    suffix.trim().toLowerCase();
    Iterator<File> li = fileList.iterator();
    while (li.hasNext()) {
      File cur = li.next();
      // add the file if it ends with the suffix and has length
      if (cur != null && cur.getName().endsWith(suffix) && cur.getName().length() > 0) {
        output.add(cur);
      }
    }

    return output;
  }


  /**
   * Finds files within the input List of Files that contain the input common name.
   *
   * @param fileList   List to search, only the last name in the file path is checked.
   * @param commonName common name String to search for in the List.
   * @return List containing all elements of the input List that contain the common name. Empty
   * otherwise.
   */
  public static List<File> findFilesByCommonName(List<File> fileList, String commonName) {
    List<File> output = new ArrayList<File>();

    commonName.trim().toLowerCase();

    Iterator<File> li = fileList.iterator();
    while (li.hasNext()) {
      File cur = li.next();
      if (cur != null && cur.getName().contains(commonName)) {
        output.add(cur);
      }
    }

    return output;
  }

  /**
   * Finds files within the input List of Files that match the input regular expression.
   *
   * @param fileList List to search, only the last name in the file path is checked.
   * @param regex    Pattern containing the regular expression to use in searching the List of
   *                 Files.
   * @return List containing all elements of the input List that matches the regex. Empty otherwise.
   */
  public static List<File> findFilesByRegex(List<File> fileList, String regex) {
    List<File> output = new ArrayList<File>();

    Iterator<File> li = fileList.iterator();
    while (li.hasNext()) {
      File cur = li.next();
      if (cur != null && cur.getName().matches(regex)) {
        output.add(cur);
      }
    }

    return output;
  }

  /**
   * Filters a List of Files by their index location. Keeps only the Files at the indices in Integer
   * List.
   *
   * @param fileList  List of Files to filter.
   * @param indexList List of Integers specifying the index locations in fileList to keep.
   * @return List of File Objects found at the specified indices.
   */
  public static List<File> filterFileListByIndex(List<File> fileList, List<Integer> indexList) {

    List<File> output = new ArrayList<File>();
    for (int i = 0; i < indexList.size(); i++) {
      Integer idx = indexList.get(i);
      if (idx != null && idx < fileList.size()) {
        output.add(fileList.get(idx));
      }
    }

    return output;
  }

  /**
   * Parse a string input of numbers separated by delimiters (non numbers with the exception of '-'
   * and ':'). The ':' is used to denote a range of values, ex. '2:5' = '2,3,4,5'. Alternatively one
   * can specify the step used in computing the range of values. ex. '1:2:8' = '1,3,5,7'. The step
   * value can be negative, ex. '10:-2:1' = '10,8,6,4,2'.
   *
   * @param framesList of frames being filtered
   * @param inputString String to parse
   * @return sorted List of non negative Integers found.
   */
  public static List<Integer> parseStringFramesToTrack(List<File> framesList, String inputString) {

    String framesToTrack = inputString;
    boolean populateAll = false;
    if (framesToTrack.isEmpty()) {
      populateAll = true;
    }
    if (framesToTrack.equalsIgnoreCase("all")) {
      populateAll = true;
    }
    if (framesToTrack.equalsIgnoreCase("0")) {
      populateAll = true;
    }

    List<Integer> output;
    if (populateAll) {
      output = new ArrayList<Integer>();
      // populate output list with all of the frame indexes
      for (int i = 0; i < framesList.size(); i++) {
        output.add(i);
      }

    } else {

      // there is a subset specified in framesToTrack string
      String temp = "";
      for (int i = 0; i < framesToTrack.length(); i++) {
        char ch = framesToTrack.charAt(i);
        if (!Character.isDigit(ch)) {
          if (ch != ':' && ch != '-') {
            ch = ',';
          }
        }
        temp = temp + ch;
      }
      framesToTrack = temp;

      String[] tokenList = framesToTrack.split(",");

      Set<Integer> framesSet = new HashSet<Integer>();
      for (int i = 0; i < tokenList.length; i++) {
        if (tokenList[i].isEmpty()) {
          continue;
        }
        if (tokenList[i].contains(":")) {
          try {
            // this is a range of values, expand it out adding them to framesList
            String str = tokenList[i];

            int count = 0;
            for (int k = 0; k < str.length(); k++) {
              if (str.charAt(k) == ':') {
                count++;
              }
            }

            int step = 0;
            if (count == 1) {
              step = 1;
            } else if (count == 2) {
              // this is of the format #:#:# and specifies a range with the step being the middle number
              step = Integer.parseInt(str.substring(str.indexOf(':') + 1, str.lastIndexOf(':')));
            }

            if (step == 0) {
              continue; // skip this segment of the frames to track string
            }

            int startNb = Integer.parseInt(str.substring(0, str.indexOf(':')));
            int endNb = Integer.parseInt(str.substring(str.lastIndexOf(':') + 1));

            // correct the directions
            if (step > 0 && endNb < startNb) {
              continue; // skip this segment of the frames to track string
            }
            if (step < 0 && startNb < endNb) {
              continue; // skip this segment of the frames to track string
            }

            // insert into the framesSet based on direction of step
            if (step > 0) {
              // add to framesList the numbers startNb:step:endNb
              for (int k = startNb; k <= endNb; k += step) {
                framesSet.add(k);
              }
            } else {
              // add to framesList the numbers startNb:step:endNb
              for (int k = startNb; k >= endNb; k += step) {
                framesSet.add(k);
              }
            }
          } catch (NumberFormatException e) {
            // do nothing
          }
          continue;
        }

        try {
          int tempI = Integer.parseInt(tokenList[i]);
          // this must be just a number
          framesSet.add(tempI);
        } catch (NumberFormatException e) {
          // do nothing
        }
      }

      Iterator<Integer> li = framesSet.iterator();
      while (li.hasNext()) {
        Integer val = li.next();
        if (val < 0) {
          li.remove();
        }
      }

      output = new ArrayList<Integer>(framesSet);
      for (int i = 0; i < output.size(); i++) {
        output.set(i, output.get(i) - 1);
      }
    }

    // sort the output list
    Collections.sort(output);

    return output;
  }


  public static List<File> generateOutputFilesByCommonName(List<File> inputFiles,
                                                           File outputDirectory,
                                                           String commonName) {

    List<File> output = new ArrayList<File>();

    // if the output directory is not a directory, return an empty list
    if (!outputDirectory.isDirectory()) {
      return output;
    }

    // if the output directory does not exist, attempt to create it, return empty list if failed
    if (!outputDirectory.exists()) {
      if (!outputDirectory.mkdirs()) {
        return output;
      }
    }

    // create an output file for each file in inputFiles numbered sequentially
    Iterator<File> li = inputFiles.iterator();

    Integer nbFile = inputFiles.size();
    String nbStr = "%0" + nbFile.toString().length() + "d";

    // output file numbering starts with 1 instead of 0,
    int outputNb = 1;
    while (li.hasNext()) {
      File cur = li.next();
      if (cur != null) {
        // get the image format extension from the input files
        String fn = cur.getName();
        int idx = fn.lastIndexOf('.');
        String ext = fn.substring(idx);

        String outputName = commonName + String.format(nbStr, outputNb++) + ext;
        output.add(new File(outputDirectory + File.separator + outputName));
      }
    }

    return output;
  }

}
