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

package main.java.gov.nist.isg.tracking.app.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import main.java.gov.nist.isg.tracking.lib.Log;

/**
 * Created by mmajursk on 9/16/2015.
 */
public class FileUtils {


  private static String documentationLoc = "/docs/";


  /**
   * Loads a compressed resource (handles files inside jar such as html or pdf)
   * The result is a temporary file that will be deleted once the jvm exits
   * @param file the file to load from resource
   * @param extension the extension of the temporary file
   * @return a temporary file that is deleted on jvm exit (or null if error)
   */
  public static File loadCompressedResource(String file, String extension)
  {
    InputStream is = null;
    OutputStream os = null;
    File out = null;

    try
    {
      is = HelpDocumentationViewer.class.getResourceAsStream(documentationLoc + file);

      out = File.createTempFile(file, extension);
      out.deleteOnExit();
      os = new FileOutputStream(out);

      final byte [] buf = new byte[1024];
      int len = 0;
      while ((len = is.read(buf)) > 0) {
        os.write(buf, 0, len);
      }

      os.flush();

    } catch (Exception e)
    {
      Log.mandatory("Error opening help file: " + e.getMessage());
    } finally {
      try
      {
        if (os != null)
          os.close();

        if (is != null)
          is.close();


      } catch (IOException e)
      {
        Log.mandatory("Error closing help file");
      }
    }

    return out;

  }

}
