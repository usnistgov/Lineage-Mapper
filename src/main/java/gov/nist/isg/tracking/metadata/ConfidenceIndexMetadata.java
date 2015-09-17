package main.java.gov.nist.isg.tracking.metadata;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.*;

import ij.IJ;
import main.java.gov.nist.isg.tracking.app.TrackingAppParams;
import main.java.gov.nist.isg.tracking.app.images.AppImageHelper;
import main.java.gov.nist.isg.tracking.lib.Log;

/**
 * Created by mmajursk on 5/27/2014.
 */
public class ConfidenceIndexMetadata implements ActionListener {

  private JTable table = null;
  private MetadataDisplayFrame tableFrame = null;
  private static final String fileName = "confidence_index.csv";

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
      IJ.error("Confidence Index Metadata Not Available");
      return;
    }

    tableFrame = new MetadataDisplayFrame("Confidence Index", table);
    try {
      tableFrame.setIconImage(AppImageHelper.loadImage("confidence_index_icon.png").getImage());
    } catch (FileNotFoundException ignored) {
      Log.debug("Could not load confidence_index_icon.png");
    }

    TrackingAppParams.getInstance().setConfidenceIndexMetadata(this);
  }

  public void buildMetadataTable() {

    if (table != null)
      return;

    HashMap<Integer, Double> confidenceIndex = CellTrackerMetadata.generateConfidenceIndex();
    if (confidenceIndex == null) return;

    Vector<Number> data = new Vector<Number>();
    Vector<String> colNames = new Vector<String>();

    colNames.add("Cell ID");
    colNames.add("Confidence Index");

    List<Integer> keylist = new ArrayList<Integer>(confidenceIndex.keySet());
    for (Integer i : keylist) {
      data.add(i);
      data.add(confidenceIndex.get(i));
    }

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
