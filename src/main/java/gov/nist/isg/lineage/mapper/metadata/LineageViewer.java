// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package main.java.gov.nist.isg.lineage.mapper.metadata;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.swing.JTable;

import main.java.gov.nist.isg.lineage.mapper.app.TrackingAppParams;
import main.java.gov.nist.isg.lineage.mapper.lib.Log;


public class LineageViewer {

  private TrackingAppParams params;

  public LineageViewer(TrackingAppParams params) {
    this.params = params;
  }


  public boolean generateDataJS(File tgtIndex) {
    Log.debug("Starting generation of data js file");

    try {

      File tgtDir = tgtIndex.getParentFile();
      File fh = new File(tgtDir.getCanonicalPath() + File.separator + "js" + File.separator + "data.js");
      FileWriter fw = new FileWriter(fh);
      PrintWriter pw = new PrintWriter(fw);


      Log.debug("Generating birth death variable");
      BirthDeathMetadata bdm = params.getBirthDeathMetadata();
      bdm.buildMetadataTable();
      pw.print("var birthDeathText = '");
      if (bdm != null && bdm.getTable() != null) {
        JTable table = bdm.getTable();
        int c = table.getColumnCount();
        int r = table.getRowCount();

        for (int i = 0; i < r; i++) {
          for (int j = 0; j < c; j++) {
            if (j > 0) pw.print(",");
            pw.print(table.getValueAt(i, j));
          }
          pw.print("\\n");
        }
      }
      pw.print("';\n\n");


      Log.debug("Generating division variable");
      DivisionMetadata dvm = params.getDivisionMetadata();
      dvm.buildMetadataTable();
      pw.print("var divisionText = '");
      if (dvm != null && dvm.getTable() != null) {
        JTable table = dvm.getTable();
        int c = table.getColumnCount();
        int r = table.getRowCount();

        for (int i = 0; i < r; i++) {
          for (int j = 0; j < c; j++) {
            if (j > 0) pw.print(",");
            pw.print(table.getValueAt(i, j));
          }
          pw.print("\\n");
        }
      }
      pw.print("';\n\n");


      Log.debug("Generating fusion variable");
      FusionMetadata fsm = params.getFusionMetadata();
      fsm.buildMetadataTable();
      pw.print("var fusionText = '");
      if (fsm != null && fsm.getTable() != null) {
        JTable table = fsm.getTable();
        int c = table.getColumnCount();
        int r = table.getRowCount();

        for (int i = 0; i < r; i++) {
          for (int j = 0; j < c; j++) {
            if (j > 0) pw.print(",");
            pw.print(table.getValueAt(i, j));
          }
          pw.print("\\n");
        }
      }
      pw.print("';\n\n");

      //Flush the output to the file
      pw.flush();
      pw.close();
      fw.close();

    } catch (FileNotFoundException e) {
      Log.error(e);
    } catch (IOException e) {
      Log.error(e);
    }

    Log.debug("data.js generation successful");
    return true;
  }


  private void copyFile(String s, File tgtDir) {
    InputStream is = null;
    OutputStream os = null;
    try {
      File f = new File(s);

      if (Desktop.isDesktopSupported()) {

        is = getClass().getResourceAsStream(s);
        if (!tgtDir.exists())
          tgtDir.mkdir();

        File out = new File(tgtDir.getCanonicalPath() + File.separator + f.getName());
//        out.deleteOnExit();

        os = new FileOutputStream(out);

        final byte[] buf = new byte[1024];
        int len = 0;
        while ((len = is.read(buf)) > 0) {
          os.write(buf, 0, len);
        }
      }


    } catch (Exception err) {
      Log.error(err);
    } finally {
      try {
        if (is != null)
          is.close();
        if (os != null)
          os.close();
      } catch (IOException e1) {
        Log.error(e1);
      }


    }
  }


  public File generateLineageViewerHtmlPage() {
    Log.debug("Copying Webpage files to output directory from jar");

    File filesDir = new File(params.getOutputDirectory() + params.getOutputPrefix() +
        "lineage-viewer" + File.separator);
    if (!filesDir.exists())
      filesDir.mkdir();

    File index;
    Log.debug("getting local file path");
    java.net.URL url = LineageViewer.class.getResource(
        "/main/java/gov/nist/isg/lineage/viewer/lineage-viewer.html");
    File srcDir = new File(url.getPath()).getParentFile();

    Log.debug("attempting copy");
    Log.debug("src: " + srcDir.toString());
    Log.debug("tgt: " + filesDir.toString());
    File jsDir = new File(filesDir.getAbsolutePath() + File.separator + "js");


    copyFile("/main/java/gov/nist/isg/lineage/viewer/lineage-viewer.html", filesDir);
    copyFile("/main/java/gov/nist/isg/lineage/viewer/js/d3.min.js", jsDir);
    copyFile("/main/java/gov/nist/isg/lineage/viewer/js/jquery-1.10.2.js", jsDir);
    copyFile("/main/java/gov/nist/isg/lineage/viewer/js/jquery-ui-1.10.4.custom.min.js", jsDir);
    copyFile("/main/java/gov/nist/isg/lineage/viewer/js/lineageMapper.js", jsDir);
    copyFile("/main/java/gov/nist/isg/lineage/viewer/js/queue.v1.min.js", jsDir);

    File cssDir = new File(filesDir.getAbsolutePath() + File.separator + "css");
    copyFile("/main/java/gov/nist/isg/lineage/viewer/css/bootstrap.min.css", cssDir);
    copyFile("/main/java/gov/nist/isg/lineage/viewer/css/colony.css", cssDir);
    copyFile("/main/java/gov/nist/isg/lineage/viewer/css/layout.css", cssDir);
    copyFile("/main/java/gov/nist/isg/lineage/viewer/css/main_style.css", cssDir);
    copyFile("/main/java/gov/nist/isg/lineage/viewer/css/reset.css", cssDir);
    copyFile("/main/java/gov/nist/isg/lineage/viewer/css/style.css", cssDir);

    cssDir = new File(filesDir.getAbsolutePath() + File.separator + "css" + File.separator + "ui-lightness");
    copyFile("/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/jquery-ui.min.css", cssDir);
    copyFile(
        "/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/jquery-ui-1.10.4.custom.min.css", cssDir);

    File imgsDir = new File(filesDir.getAbsolutePath() + File.separator + "css" + File.separator + "ui-lightness" + File.separator + "images");
    copyFile("/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/images/animated-overlay.gif", imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/images/ui-bg_diagonals-thick_18_b81900_40x40.png", imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/images/ui-bg_diagonals-thick_20_666666_40x40.png", imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/images/ui-bg_flat_10_000000_40x100.png", imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/images/ui-bg_glass_65_ffffff_1x400.png", imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/images/ui-bg_glass_100_f6f6f6_1x400.png", imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/images/ui-bg_glass_100_fdf5ce_1x400.png", imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/images/ui-bg_gloss-wave_35_f6a828_500x100.png", imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/images/ui-bg_highlight-soft_75_ffe45c_1x100.png", imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/images/ui-bg_highlight-soft_100_eeeeee_1x100.png", imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/images/ui-icons_228ef1_256x240.png", imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/images/ui-icons_222222_256x240.png", imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/images/ui-icons_ef8c08_256x240.png", imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/images/ui-icons_ffd27a_256x240.png", imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineage/viewer/css/ui-lightness/images/ui-icons_ffffff_256x240.png", imgsDir);

    index = new File(filesDir.getAbsolutePath() + File.separator + "lineage-viewer.html");


    Log.debug("File copy successful");

    return index;
  }

}
