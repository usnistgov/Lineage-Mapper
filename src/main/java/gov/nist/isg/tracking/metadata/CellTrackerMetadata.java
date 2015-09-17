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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import main.java.gov.nist.isg.tracking.app.TrackingAppParams;
import main.java.gov.nist.isg.tracking.lib.Cell;
import main.java.gov.nist.isg.tracking.lib.ImageFrame;
import main.java.gov.nist.isg.tracking.lib.Matrix2D;


public class CellTrackerMetadata {

  /**
   * Generate the vector containing cell birth frames
   * @return 1D array of birth frames for each global cell label
   */
  public static int[] generateBirthMatrix() {
    List<ImageFrame> framesList = TrackingAppParams.getInstance().getFramesList();

    if(framesList == null)
      return null;

    int maxCellNumber = 0;
    for (ImageFrame f : framesList) {
      maxCellNumber = Math.max(maxCellNumber, f.getMaxCellGlobalLabelNumber());
    }

    int[] birth = new int[maxCellNumber + 1];
    for (int i = 0; i < birth.length; i++) {
      birth[i] = -1;
    }

    for (ImageFrame f : framesList) {
      for (Cell c : f.getCellsList()) {
        int idx = c.getGlobalLabel();
        if (birth[idx] < 0) {
          birth[idx] = f.getFrameNb();
        }
      }
    }
    return birth;
  }

  /**
   * Generate the vector containing cell death frames
   * @return 1D array of death frames for each global cell label
   */
  public static int[] generateDeathMatrix() {
    List<ImageFrame> framesList = TrackingAppParams.getInstance().getFramesList();

    if(framesList == null)
      return null;

    int maxCellNumber = 0;
    for (ImageFrame f : framesList) {
      maxCellNumber = Math.max(maxCellNumber, f.getMaxCellGlobalLabelNumber());
    }

    int nbFrames = framesList.size();
    int[] death = new int[maxCellNumber + 1];
    for (int i = 0; i < death.length; i++) {
      death[i] = nbFrames - 1;
    }

    for (ImageFrame f : framesList) {
      for (Cell c : f.getCellsList()) {
        int idx = c.getGlobalLabel();
        death[idx] = f.getFrameNb();
      }
    }
    return death;
  }

  /**
   * Generate a HashMap containing the division matrix, key is the global cell label, value is the
   * list of daughter cell global labels
   * @return HashMap containing the division matrix
   */
  public static HashMap<Integer, ArrayList<Integer>> generateDivisionMatrix() {
    TrackingAppParams params = TrackingAppParams.getInstance();
    List<ImageFrame> framesList = params.getFramesList();

    if(framesList == null)
      return null;

    int[] death = generateDeathMatrix();
    int maxCellNumber = death.length-1;

    // compute division
    HashMap<Integer, ArrayList<Integer>> division = new HashMap<Integer, ArrayList<Integer>>();
    for (int i = 1; i <= maxCellNumber; i++) {
      int frameDeath = death[i];
      if (frameDeath == framesList.size() - 1) {
        continue;
      }

      ArrayList<Integer> curdiv = new ArrayList<Integer>();
      int pixLabel = framesList.get(frameDeath).getCellByGlobalLabel(i).getImgLabel();

      Matrix2D div = framesList.get(frameDeath + 1).getDivision();
      for (int j = 1; j <= div.getM(); j++) {
        if (Double.compare(div.get(j, 1), (double) pixLabel) == 0) {
          int k = framesList.get(frameDeath + 1).getCellByImgLabel(j).getGlobalLabel();
          curdiv.add(k);
        }
      }
      if (!curdiv.isEmpty()) {
        division.put(i, curdiv);
      }
    }

    return division;
  }

  /**
   * Generate a HashMap containing the fusion matrix, key is the global cell label, value is the
   * list of fused cell global labels
   * @return HashMap containing the fusion matrix
   */
  public static HashMap<Integer, ArrayList<Integer>> generateFusionMatrix() {
    TrackingAppParams params = TrackingAppParams.getInstance();
    if (!params.isEnableCellFusion()) {
      return null;
    }

    List<ImageFrame> framesList = params.getFramesList();

    if(framesList == null)
      return null;

    int maxCellNumber = 0;
    for (ImageFrame f : framesList) {
      maxCellNumber = Math.max(maxCellNumber, f.getMaxCellGlobalLabelNumber());
    }

    HashMap<Integer, ArrayList<Integer>> fusion = new HashMap<Integer, ArrayList<Integer>>();
    ImageFrame prev = null;
    for (ImageFrame f : framesList) {
      if (f.getFrameNb() == 0) {
        prev = f;
        continue;
      }
      Matrix2D fus = f.getFusion();

      if (fus.nnz() > 0) {
        ArrayList<Integer> curfus = new ArrayList<Integer>();
        for (int child = 1; child <= fus.getN(); child++) {
          curfus.clear();
          for (int parent = 1; parent <= fus.getM(); parent++) {
            if (fus.get(parent, child) > 0) {
              curfus.add(parent);
            }
          }
          if (!curfus.isEmpty()) {
            ArrayList<Integer> newfus = new ArrayList<Integer>(curfus);
            int globalLabel = f.getCellByImgLabel(child).getGlobalLabel();
            for (int i = 0; i < newfus.size(); i++) {
              int gl = prev.getCellByImgLabel(curfus.get(i)).getGlobalLabel();
              newfus.set(i, gl);
            }
            fusion.put(globalLabel, newfus);
          }
        }
      }
      prev = f;
    }

    return fusion;
  }

  /**
   * Generate the index listing whether a global cell label touches the border at any point within
   * its lifetime
   * @return boolean array of global cell labels true for those which touch the border of the image
   * within their lifetime
   */
  public static boolean[] generateBorderMatrix() {
    TrackingAppParams params = TrackingAppParams.getInstance();
    List<ImageFrame> framesList = params.getFramesList();

    if(framesList == null)
      return null;

    int maxCellNumber = 0;
    for (ImageFrame f : framesList) {
      maxCellNumber = Math.max(maxCellNumber, f.getMaxCellGlobalLabelNumber());
    }

    boolean[] borderCell = new boolean[maxCellNumber + 1];

    for (int i = 0; i < borderCell.length; i++) {
      borderCell[i] = false;
    }

    for (ImageFrame f : framesList) {
      for (Cell c : f.getCellsList()) {
        int idx = c.getGlobalLabel();
        borderCell[idx] = borderCell[idx] || c.isBorderCell();
      }
    }
    return borderCell;
  }

  /**
   * Generates the confidence index for each tracked global cell label
   * @return HashMap containing key global cell label, value the confidence index
   */
  public static HashMap<Integer, Double> generateConfidenceIndex() {
    TrackingAppParams params = TrackingAppParams.getInstance();
    List<ImageFrame> framesList = params.getFramesList();

    if(framesList == null)
      return null;

    int[] birth = generateBirthMatrix();
    int[] death = generateDeathMatrix();
    boolean[] borderCell = generateBorderMatrix();

    int maxCellNumber = birth.length-1;

    List<Integer> touch = new ArrayList<Integer>();
    HashMap<Integer, Double> CI = new HashMap<Integer, Double>();
    for (int gCellNb = 1; gCellNb <= maxCellNumber; gCellNb++) {
      int lifespan = death[gCellNb] - birth[gCellNb] + 1;
      double ciVal = 1;
      if (!borderCell[gCellNb]) {
        ciVal++;
      }

      // add in density component
      touch.clear();
      for (int k = birth[gCellNb]; k <= death[gCellNb]; k++) {
        Cell c = framesList.get(k).getCellByGlobalLabel(gCellNb);
        if (c != null && c.getTouchingCells() != null) {
          touch.add(c.getTouchingCells().size());
        }
      }
      // get the mean number of touching cells
      if (!touch.isEmpty()) {
        double meanval = 0;
        for (int k = 0; k < touch.size(); k++) {
          meanval += touch.get(k);
        }
        meanval = meanval / (double) touch.size();
        ciVal += (1 / (meanval + 1));
      } else {
        ciVal++;
      }

      if (lifespan > params.getMinCellLife()) {
        ciVal++;
      }

      CI.put(gCellNb, ciVal/4.0);
    }
    CI = main.java.gov.nist.isg.tracking.lib.Utils.sortHashMapByValuesD(CI);

    return CI;
  }


  /**
   * Generate and save the Lineage Mapper metadata
   */
  public static void generateAndSaveMetadata() {

    TrackingAppParams params = TrackingAppParams.getInstance();
    if(!params.isSaveOutputsEnabled())
      return;

    String prefix = params.getPrefix();

    String targetFolder = params.getOutputDirectory();
    if(!targetFolder.endsWith(File.separator))
      targetFolder = targetFolder + File.separator;

    BirthDeathMetadata bdm = new BirthDeathMetadata();
    bdm.buildMetadataTable();
    if (bdm.getTable() != null) {
      main.java.gov.nist.isg.tracking.lib.Utils.exportToCSVFile(bdm.getTable(), new File(targetFolder + prefix + bdm.getFileName()));
    }

    DivisionMetadata dm = new DivisionMetadata();
    dm.buildMetadataTable();
    if (dm.getTable() != null) {
      main.java.gov.nist.isg.tracking.lib.Utils.exportToCSVFile(dm.getTable(), new File(targetFolder + prefix + dm.getFileName()));
    }

    FusionMetadata fm = new FusionMetadata();
    fm.buildMetadataTable();
    if (fm.getTable() != null) {
      main.java.gov.nist.isg.tracking.lib.Utils
          .exportToCSVFile(fm.getTable(), new File(targetFolder + prefix + fm.getFileName()));
    }

    ConfidenceIndexMetadata cim = new ConfidenceIndexMetadata();
    cim.buildMetadataTable();
    if (cim.getTable() != null) {
      main.java.gov.nist.isg.tracking.lib.Utils.exportToCSVFile(cim.getTable(), new File(targetFolder + prefix + cim.getFileName()));
    }

  }




}









