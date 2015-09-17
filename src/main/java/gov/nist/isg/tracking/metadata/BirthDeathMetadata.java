package main.java.gov.nist.isg.tracking.metadata;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.util.Vector;

import javax.swing.*;

import ij.IJ;
import main.java.gov.nist.isg.tracking.app.TrackingAppParams;
import main.java.gov.nist.isg.tracking.app.images.AppImageHelper;
import main.java.gov.nist.isg.tracking.lib.Log;

/**
 * Created by mmajursk on 5/27/2014.
 */
public class BirthDeathMetadata implements ActionListener {

  private JTable table = null;
  private MetadataDisplayFrame tableFrame = null;
  private static final String fileName = "birth_death.csv";

  public static String getFileName() { return fileName; }

  @Override
  public void actionPerformed(ActionEvent e) {

    if(tableFrame != null) {
      tableFrame.setVisible(true);
      tableFrame.toFront();
      return;
    }

    buildMetadataTable();
    if(table ==  null) {
      IJ.error("Birth/Death Metadata Not Available");
      return;
    }

    tableFrame = new MetadataDisplayFrame("Birth/Death", table);
    try {
      tableFrame.setIconImage(AppImageHelper.loadImage("birth_death_icon.png").getImage());
    } catch (FileNotFoundException ignored) {
      Log.debug("Could not load birth_death_icon.png");
    }

    TrackingAppParams.getInstance().setBirthDeathMetadata(this);
  }


  public void buildMetadataTable() {
    if( table != null) return;

    int[] birth = CellTrackerMetadata.generateBirthMatrix();
    int[] death = CellTrackerMetadata.generateDeathMatrix();
    if(birth == null) return;
    if(death == null) return;
    if(birth.length != death.length) return;

    Vector<Number> data = new Vector<Number>();
    for(int i = 1; i < birth.length; i++) {
      data.add(i);
      data.add(birth[i]+1); // +1 is to convert from zero based to 1 based
      data.add(death[i]+1); // +1 is to convert from zero based to 1 based
    }

    Vector<String> colNames = new Vector<String>();
    colNames.add(0, "Cell ID");
    colNames.add(1, "Birth Frame");
    colNames.add(2, "Death Frame");

    MetadataTableModel tableModel = new MetadataTableModel(colNames, data);
    table = new JTable();
    table.setModel(tableModel);
    table.createDefaultColumnsFromModel();
  }

  public JTable getTable() { return table; }
  public MetadataDisplayFrame getTableFrame() { return tableFrame; }


  public void reset() {
    if(tableFrame != null) {
      tableFrame.dispatchEvent(new WindowEvent(tableFrame, WindowEvent.WINDOW_CLOSING));
      tableFrame = null;
    }
    table = null;
  }
}
