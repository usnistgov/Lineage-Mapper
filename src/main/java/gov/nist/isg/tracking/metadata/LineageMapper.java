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

package main.java.gov.nist.isg.tracking.metadata;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.swing.*;

import main.java.gov.nist.isg.tracking.app.TrackingAppParams;
import main.java.gov.nist.isg.tracking.lib.Log;

/**
 * Created by mmajursk on 8/4/2014.
 */
public class LineageMapper implements ActionListener {

  @Override
  public void actionPerformed(ActionEvent e) {

    Log.debug("Starting Lineage Mapper");
    File f = copyLineageMapperToTempDir();
    if (f == null) {
      Log.debug("Copying files failed");
      return;
    }

    if (!generateDataJS(f)) {
      Log.debug("Generating data.js failed");
      return;
    }



    Log.debug("attempting to hand the newly create index.html to a web browser");
    if (Desktop.isDesktopSupported()) {
      Log.debug("Java Desktop is supported");
      try {
        Log.debug("Asking Desktop to open index.html");
        Desktop.getDesktop().open(f);
      } catch (IOException ex) {
        Log.mandatory("Opening Lineage Mapper tree in web browser failed.");
        Log.error(ex);
      }
    }
  }




  private static boolean generateDataJS(File tgtIndex) {
    Log.debug("Starting generation of data js file");
    TrackingAppParams params = TrackingAppParams.getInstance();

    try {

      File tgtDir = tgtIndex.getParentFile();
      File fh = new File(tgtDir.getCanonicalPath() + File.separator + "js" + File.separator + "data.js");
      FileWriter fw = new FileWriter(fh);
      PrintWriter pw = new PrintWriter(fw);


      Log.debug("Generating birth death variable");
      BirthDeathMetadata bdm = params.getBirthDeathMetadata();
      bdm.buildMetadataTable();
      pw.print("var birthDeathText = '");
      if(bdm != null && bdm.getTable() != null) {
        JTable table = bdm.getTable();
        int c = table.getColumnCount();
        int r = table.getRowCount();

        for(int i = 0; i < r; i++) {
          for(int j = 0; j < c; j++) {
            if(j>0)  pw.print(",");
            pw.print(table.getValueAt(i,j));
          }
          pw.print("\\n");
        }
      }
      pw.print("';\n\n");


      Log.debug("Generating division variable");
      DivisionMetadata dvm = params.getDivisionMetadata();
      dvm.buildMetadataTable();
      pw.print("var divisionText = '");
      if(dvm != null && dvm.getTable() != null) {
        JTable table = dvm.getTable();
        int c = table.getColumnCount();
        int r = table.getRowCount();

        for(int i = 0; i < r; i++) {
          for(int j = 0; j < c; j++) {
            if(j>0)  pw.print(",");
            pw.print(table.getValueAt(i,j));
          }
          pw.print("\\n");
        }
      }
      pw.print("';\n\n");


      Log.debug("Generating fusion variable");
      FusionMetadata fsm = params.getFusionMetadata();
      fsm.buildMetadataTable();
      pw.print("var fusionText = '");
      if(fsm != null && fsm.getTable() != null) {
        JTable table = fsm.getTable();
        int c = table.getColumnCount();
        int r = table.getRowCount();

        for(int i = 0; i < r; i++) {
          for(int j = 0; j < c; j++) {
            if(j>0)  pw.print(",");
            pw.print(table.getValueAt(i,j));
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
        if(!tgtDir.exists())
          tgtDir.mkdir();

        File out = new File(tgtDir.getCanonicalPath() + File.separator + f.getName());
        out.deleteOnExit();

        os = new FileOutputStream(out);

        final byte[] buf = new byte[1024];
        int len = 0;
        while ((len = is.read(buf)) > 0) {
          os.write(buf, 0, len);
        }
      }


    } catch (Exception err) {
      Log.error(err);
    }finally{
      try {
        if(is != null)
          is.close();
        if(os != null)
          os.close();
      } catch (IOException e1) {
        Log.error(e1);
      }


    }
  }


  private File copyLineageMapperToTempDir() {
    Log.debug("Copying Webpage files to local tmp directory from jar");
    String baseTempPath = System.getProperty("java.io.tmpdir");


    File tempDir = new File(baseTempPath + File.separator + "lineageTempDir");
    if (!tempDir.exists()) {
      Log.debug("creating temp dir");
      tempDir.mkdir();
    }

    tempDir.deleteOnExit();
    File index;

    Log.debug("getting local file path");
    java.net.URL url = LineageMapper.class.getResource(
        "/main/java/gov/nist/isg/lineageMapper/index.html");
    File srcDir = new File(url.getPath()).getParentFile();


    Log.debug("attempting copy");
    Log.debug("src: " + srcDir.toString());
    Log.debug("tgt: " + tempDir.toString());

    File jsDir = new File(tempDir.getAbsolutePath() + File.separator + "js");

    copyFile("/main/java/gov/nist/isg/lineageMapper/index.html",tempDir);
    copyFile("/main/java/gov/nist/isg/lineageMapper/js/d3.min.js",jsDir);
    copyFile("/main/java/gov/nist/isg/lineageMapper/js/jquery-1.10.2.js",jsDir);
    copyFile("/main/java/gov/nist/isg/lineageMapper/js/jquery-ui-1.10.4.custom.min.js",jsDir);
    copyFile("/main/java/gov/nist/isg/lineageMapper/js/lineageMapper.js",jsDir);
    copyFile("/main/java/gov/nist/isg/lineageMapper/js/queue.v1.min.js",jsDir);

    File cssDir = new File(tempDir.getAbsolutePath() + File.separator + "css");
    copyFile("/main/java/gov/nist/isg/lineageMapper/css/bootstrap.min.css",cssDir);
    copyFile("/main/java/gov/nist/isg/lineageMapper/css/colony.css",cssDir);
    copyFile("/main/java/gov/nist/isg/lineageMapper/css/layout.css",cssDir);
    copyFile("/main/java/gov/nist/isg/lineageMapper/css/main_style.css",cssDir);
    copyFile("/main/java/gov/nist/isg/lineageMapper/css/reset.css",cssDir);
    copyFile("/main/java/gov/nist/isg/lineageMapper/css/style.css",cssDir);

    cssDir = new File(tempDir.getAbsolutePath() + File.separator + "css" + File.separator + "ui-lightness");
    copyFile("/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/jquery-ui.min.css",cssDir);
    copyFile(
        "/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/jquery-ui-1.10.4.custom.min.css",cssDir);

    File imgsDir = new File(tempDir.getAbsolutePath() + File.separator + "css" + File.separator + "ui-lightness" + File.separator + "images");
    copyFile("/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/images/animated-overlay.gif",imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/images/ui-bg_diagonals-thick_18_b81900_40x40.png",imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/images/ui-bg_diagonals-thick_20_666666_40x40.png",imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/images/ui-bg_flat_10_000000_40x100.png",imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/images/ui-bg_glass_65_ffffff_1x400.png",imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/images/ui-bg_glass_100_f6f6f6_1x400.png",imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/images/ui-bg_glass_100_fdf5ce_1x400.png",imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/images/ui-bg_gloss-wave_35_f6a828_500x100.png",imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/images/ui-bg_highlight-soft_75_ffe45c_1x100.png",imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/images/ui-bg_highlight-soft_100_eeeeee_1x100.png",imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/images/ui-icons_228ef1_256x240.png",imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/images/ui-icons_222222_256x240.png",imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/images/ui-icons_ef8c08_256x240.png",imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/images/ui-icons_ffd27a_256x240.png",imgsDir);
    copyFile(
        "/main/java/gov/nist/isg/lineageMapper/css/ui-lightness/images/ui-icons_ffffff_256x240.png",imgsDir);

      index = new File(tempDir.getAbsolutePath() + File.separator + "index.html");


    Log.debug("File copy successful");

    return index;
  }

}
