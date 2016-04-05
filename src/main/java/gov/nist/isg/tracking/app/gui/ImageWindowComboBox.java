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

import javax.swing.*;

import ij.ImagePlus;
import ij.WindowManager;

/**
 * Created by mmajursk on 5/28/2014.
 */
public class ImageWindowComboBox extends JComboBox {

  public ImageWindowComboBox() {
    super();
    refresh();

  }

  public void refresh() {

    this.removeAllItems();

    int[] imageIDs = WindowManager.getIDList();
    if(imageIDs == null) {
      this.addItem("<null>");
    }else {

      for (int i : imageIDs) {
        ImagePlus imp = WindowManager.getImage(i);
        if (imp.getStackSize() > 1) {
          this.addItem(imp.getTitle());
        }
      }
    }

  }
}
