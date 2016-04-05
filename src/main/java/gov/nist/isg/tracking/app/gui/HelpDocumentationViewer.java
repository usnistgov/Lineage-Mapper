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

import ij.IJ;
import main.java.gov.nist.isg.tracking.lib.Log;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
/**
 * Simple help documentation class that opens the HTML help documentation in
 * the O/S default web browswer
 * @author tjb3
 *
 */
public class HelpDocumentationViewer implements ActionListener{


  private static final String helpDocumentation = "User-Guide.md.html";
  private static File mainDocumentationTempFile;

  private File htmlAncorLoader;
  private String tag;

  /**
   * Constructs the viewer
   * @param tag the html tag to point to
   */
  public HelpDocumentationViewer(String tag)
  {
    this.tag = tag;
    try {
      this.htmlAncorLoader = File.createTempFile(tag, ".html");
      this.htmlAncorLoader.deleteOnExit();
    } catch (IOException e) {
      Log.mandatory("Error creating temporary documentation file: " + e.getMessage());
    }

    mainDocumentationTempFile = FileUtils.loadCompressedResource(helpDocumentation, ".html");

    createHtmlAncorLoader();

  }

  @Override
  public void actionPerformed(ActionEvent arg0) {

    if (Desktop.isDesktopSupported()) {
      try {
        Desktop.getDesktop().browse(this.htmlAncorLoader.toURI());
      } catch (IOException e) {
        Log.mandatory("Error: IOException - " + e.getMessage());
      }
    }
  }

  private void createHtmlAncorLoader() {
    String contents = "<html><head><meta http-equiv=\"refresh\" content=\"0;url="
                      + mainDocumentationTempFile.getName() + "#" + this.tag + "\" /></head></html>";

    try {
      FileWriter writer = new FileWriter(this.htmlAncorLoader);
      writer.write(contents);
      writer.close();
    } catch (IOException e) {
      Log.mandatory("Error writing to temporary file: " + e.getMessage());
    }

  }





}

