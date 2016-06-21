// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package main.java.gov.nist.isg.lineage.mapper.app.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import main.java.gov.nist.isg.lineage.mapper.lib.Log;


public class FileUtils {


  private static String documentationLoc = "/docs/";


  /**
   * Loads a compressed resource (handles files inside jar such as html or pdf) The result is a
   * temporary file that will be deleted once the jvm exits
   *
   * @param file      the file to load from resource
   * @param extension the extension of the temporary file
   * @return a temporary file that is deleted on jvm exit (or null if error)
   */
  public static File loadCompressedResource(String file, String extension) {
    InputStream is = null;
    OutputStream os = null;
    File out = null;

    try {
      is = HelpDocumentationViewer.class.getResourceAsStream(documentationLoc + file);

      out = File.createTempFile(file, extension);
      out.deleteOnExit();
      os = new FileOutputStream(out);

      final byte[] buf = new byte[1024];
      int len = 0;
      while ((len = is.read(buf)) > 0) {
        os.write(buf, 0, len);
      }

      os.flush();

    } catch (Exception e) {
      Log.mandatory("Error opening help file: " + e.getMessage());
    } finally {
      try {
        if (os != null)
          os.close();

        if (is != null)
          is.close();


      } catch (IOException e) {
        Log.mandatory("Error closing help file");
      }
    }

    return out;
  }


}
