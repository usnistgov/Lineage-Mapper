package main.java.gov.nist.isg.tracking.metadata;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
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
public class DivisionMetadata implements ActionListener {

  private JTable table = null;
  private MetadataDisplayFrame tableFrame = null;
  private static final String fileName = "division.csv";

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
      IJ.error("Division Metadata Not Available");
      return;
    }

    tableFrame = new MetadataDisplayFrame("Division", table);

    try {
      tableFrame.setIconImage(AppImageHelper.loadImage("division_icon.png").getImage());
    } catch (FileNotFoundException ignored) {
      Log.debug("Could not load division_icon.png");
    }

    TrackingAppParams.getInstance().setDivisionMetadata(this);
  }

  public void buildMetadataTable() {

    if( table != null) return;

    HashMap<Integer, ArrayList<Integer>> division =  CellTrackerMetadata.generateDivisionMatrix();
    if(division == null) return;
    int[] death = CellTrackerMetadata.generateDeathMatrix();
    if(death == null) return;


    Vector<Number> data = new Vector<Number>();

    int maxNbDaughters = 0;
    List<Integer> keylist = new ArrayList<Integer>(division.keySet());
    Collections.sort(keylist);
    for (Integer i : keylist) {
      maxNbDaughters = Math.max(maxNbDaughters, division.get(i).size());
    }

    Vector<String> colNames = new Vector<String>();
    colNames.add("t (time)");
    colNames.add("Mother ID @t");
    for (int i = 1; i <= maxNbDaughters; i++) {
      colNames.add("Daughter ID @t+1");
    }

    // print each division list out
    for (Integer i : keylist) {
      ArrayList<Integer> daughters = division.get(i);
      data.add(death[i]+1+1); // +1 is to convert from zero based to one based, second +1 is because the division frame is the frame after the death of the mother
      data.add(i);
      int nb = 0;
      for(Integer d : daughters) {
        data.add(d);
        nb++;
      }
      for (; nb < maxNbDaughters; nb++) {
        data.add(Double.NaN);
      }
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
