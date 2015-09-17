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
