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
public class FusionMetadata implements ActionListener {

  private JTable table = null;
  private MetadataDisplayFrame tableFrame = null;
  private static final String fileName = "fusion.csv";

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
      IJ.error("Fusion Metadata Not Available");
      return;
    }

    tableFrame = new MetadataDisplayFrame("Fusion", table);
    try {
      tableFrame.setIconImage(AppImageHelper.loadImage("fusion_icon.png").getImage());
    } catch (FileNotFoundException ignored) {
      Log.debug("Could not load fusion_icon.png");
    }

    TrackingAppParams.getInstance().setFusionMetadata(this);
  }

  public void buildMetadataTable() {

    if( table != null) return;

    HashMap<Integer, ArrayList<Integer>> fusion =  CellTrackerMetadata.generateFusionMatrix();
    if(fusion == null || fusion.size() == 0) return;
    int[] birth = CellTrackerMetadata.generateBirthMatrix();
    if(birth == null) return;


    Vector<Number> data = new Vector<Number>();
    Vector<String> colNames = new Vector<String>();

    int maxNbParents = 0;
    List<Integer> keylist = new ArrayList<Integer>(fusion.keySet());
    Collections.sort(keylist);
    for (Integer i : keylist) {
      maxNbParents = Math.max(maxNbParents, fusion.get(i).size());
    }

    if (maxNbParents > 0) {
      colNames.add("t (time)");
      colNames.add("Fused ID @t+1");
      for (int i = 1; i <= maxNbParents; i++) {
        colNames.add("Fusion ID @t");
      }

      // print each fusion list out
      for (Integer i : keylist) {
        ArrayList<Integer> parents = fusion.get(i);
        data.add(birth[i]+1); // +1 to convert from zero based to one based
        data.add(i);
        int nb = 0;
        for (Integer p : parents) {
          data.add(p);
          nb++;
        }
        for (; nb < maxNbParents; nb++) {
          data.add(null);
        }
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
