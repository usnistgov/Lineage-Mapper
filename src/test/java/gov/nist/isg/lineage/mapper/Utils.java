// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package gov.nist.isg.lineage.mapper;

import java.io.File;
import java.util.Scanner;

public class Utils {

  /**
   * recursively delete a folder
   * @param file root folder to delete
   */
  public static void recursiveDelete(File file) {
    if(!file.exists()) return;
    if(file.isDirectory()) {
      for(File f : file.listFiles()) recursiveDelete(f);
      file.delete();
    }else{
      file.delete();
    }
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
      textA = textA.replace("\r\n","\n"); // use unix line feed


      scannerB = new Scanner(b, "UTF-8");
      String textB = scannerB.useDelimiter("\\A").next();
      textB = textB.replace("\r\n","\n"); // use unix line feed

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
