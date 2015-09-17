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
