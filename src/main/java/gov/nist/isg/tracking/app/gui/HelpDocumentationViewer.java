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

