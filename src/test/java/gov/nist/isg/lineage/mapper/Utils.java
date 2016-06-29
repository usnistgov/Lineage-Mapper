package test.java.gov.nist.isg.lineage.mapper;

import java.io.File;
import java.util.Scanner;

public class Utils {

  /**
   * recursively delete a folder
   * @param folder root folder to delete
   */
  public static void deleteFolder(File folder) {
    File[] files = folder.listFiles();
    if(files != null) {
      for(File f : files) {
        if(f.isDirectory()) {
          deleteFolder(f);
        }else{
          f.delete();
        }
      }
    }
    folder.delete();
  }

  /**
   * Determine if two text files have equal contents.
   * @param a File pointing to the first file to be compared.
   * @param b File pointing to the second file to be compared.
   * @return whether the two files contain equal contents.
   */
  public static boolean isEqualContents(File a, File b) {
    // if either file does not exist, they do not have equal contents
    if(!a.exists())
      return false;
    if(!b.exists())
      return false;

    boolean same = false;
    Scanner scannerA = null;
    Scanner scannerB = null;
    try {
      scannerA = new Scanner(a, "UTF-8");
      String textA = scannerA.useDelimiter("\\A").next();

      scannerB = new Scanner(b, "UTF-8");
      String textB = scannerB.useDelimiter("\\A").next();

      if(textA.contentEquals(textB))
        same = true;

    } catch (Exception e) {
      same = false;
    } finally {
      scannerA.close();
      scannerB.close();
    }

    return same;
  }
}
