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

//
// ================================================================

// ================================================================
//
// Author: dan1
// Date:   Aug 16, 2013 1:27:45 PM EST
//
// Time-stamp: <Aug 16, 2013 1:27:45 PM dan1>
//
// Description of AppImageHelper.java
//
// ================================================================

package main.java.gov.nist.isg.tracking.app.images;

import java.io.FileNotFoundException;

import javax.swing.*;

public class AppImageHelper {

  private static String figureLoc = "/figs/";

  private AppImageHelper() {
  }

  public static ImageIcon loadImage(String name) throws FileNotFoundException {
    ImageIcon image = null;
    java.net.URL url = AppImageHelper.class.getResource(figureLoc + name);
    if (url != null) {
      java.awt.Image img = java.awt.Toolkit.getDefaultToolkit().createImage(url);
      if (img != null) {
        image = new ImageIcon(img);
      }
    }

    if (image == null) {
      throw new FileNotFoundException("ERROR: Image file " + name + " not found.");
    }

    return image;
  }
}