// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package gov.nist.isg.lineage.mapper.metadata;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gov.nist.isg.lineage.mapper.app.TrackingAppParams;
import gov.nist.isg.lineage.mapper.lib.Cell;
import gov.nist.isg.lineage.mapper.lib.ImageFrame;
import gov.nist.isg.lineage.mapper.lib.Log;
import gov.nist.isg.lineage.mapper.lib.Matrix2D;


public class CellTrackerMetadata {

  /**
   * Generate the vector containing cell birth frames
   *
   * @return 1D array of birth frames for each global cell label
   */
  public static int[] generateBirthMatrix(TrackingAppParams params) {

    List<ImageFrame> framesList = params.getFramesList();
    if (framesList == null)
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
   *
   * @return 1D array of death frames for each global cell label
   */
  public static int[] generateDeathMatrix(TrackingAppParams params) {

    List<ImageFrame> framesList = params.getFramesList();
    if (framesList == null)
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
   *
   * @return HashMap containing the division matrix
   */
  public static HashMap<Integer, ArrayList<Integer>> generateDivisionMatrix(TrackingAppParams params) {

    List<ImageFrame> framesList = params.getFramesList();
    if (framesList == null)
      return null;

    int[] death = generateDeathMatrix(params);
    int maxCellNumber = death.length - 1;

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
   *
   * @return HashMap containing the fusion matrix
   */
  public static HashMap<Integer, ArrayList<Integer>> generateFusionMatrix(TrackingAppParams params) {
    if (!params.isEnableCellFusion()) {
      return null;
    }

    List<ImageFrame> framesList = params.getFramesList();

    if (framesList == null)
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
   *
   * @return boolean array of global cell labels true for those which touch the border of the image
   * within their lifetime
   */
  public static boolean[] generateBorderMatrix(TrackingAppParams params) {

    List<ImageFrame> framesList = params.getFramesList();
    if (framesList == null)
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
   *
   * @return HashMap containing key global cell label, value the confidence index
   */
  public static HashMap<Integer, Double> generateConfidenceIndex(TrackingAppParams params) {

    List<ImageFrame> framesList = params.getFramesList();

    if (framesList == null)
      return null;

    int[] birth = generateBirthMatrix(params);
    int[] death = generateDeathMatrix(params);
    boolean[] borderCell = generateBorderMatrix(params);

    int maxCellNumber = birth.length - 1;

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

      CI.put(gCellNb, ciVal / 4.0);
    }
    CI = gov.nist.isg.lineage.mapper.lib.Utils.sortHashMapByValuesD(CI);

    return CI;
  }


  /**
   * Generate and save the Lineage Mapper metadata
   */
  public static void generateAndSaveMetadata(TrackingAppParams params) {

    String prefix = params.getOutputPrefix();

    String targetFolder = params.getOutputDirectory();
    if (!targetFolder.endsWith(File.separator))
      targetFolder = targetFolder + File.separator;

    BirthDeathMetadata bdm = new BirthDeathMetadata(params);
    params.setBirthDeathMetadata(bdm);
    bdm.buildMetadataTable();
    if (bdm.getTable() != null) {
      gov.nist.isg.lineage.mapper.lib.Utils.exportToCSVFile(bdm.getTable(), new File(targetFolder + prefix + bdm.getFileName()));
    }

    DivisionMetadata dm = new DivisionMetadata(params);
    params.setDivisionMetadata(dm);
    dm.buildMetadataTable();
    if (dm.getTable() != null) {
      gov.nist.isg.lineage.mapper.lib.Utils.exportToCSVFile(dm.getTable(), new File(targetFolder + prefix + dm.getFileName()));
    }

    FusionMetadata fm = new FusionMetadata(params);
    params.setFusionMetadata(fm);
    fm.buildMetadataTable();
    if (fm.getTable() != null) {
      gov.nist.isg.lineage.mapper.lib.Utils
          .exportToCSVFile(fm.getTable(), new File(targetFolder + prefix + fm.getFileName()));
    }

    ConfidenceIndexMetadata cim = new ConfidenceIndexMetadata(params);
    params.setConfidenceIndexMetadata(cim);
    cim.buildMetadataTable();
    if (cim.getTable() != null) {
      gov.nist.isg.lineage.mapper.lib.Utils.exportToCSVFile(cim.getTable(), new File(targetFolder + prefix + cim.getFileName()));
    }

    params.writeParamsToFile(new File(targetFolder + prefix + "tracking-params.txt"));

    // write the Lineage Viewer webpage to disk
    LineageViewer lv = new LineageViewer(params);
    File linageViewPage = lv.generateLineageViewerHtmlPage();
    if (linageViewPage == null) {
      Log.error("Creating lineage viewer html page failed");
    }
    if (!lv.generateDataJS(linageViewPage)) {
      Log.error("Generating interactive lineage visualization data failed");
    }

    Log.debug("attempting to hand the newly create lineage-viewer.html to a web browser");
    if (!params.isMacro() && Desktop.isDesktopSupported()) {
      Log.debug("Java Desktop is supported");
      try {
        Log.debug("Asking Desktop to open lineage-viewer.html");
        Desktop.getDesktop().open(linageViewPage);
      } catch (IOException ex) {
        Log.mandatory("Opening Lineage Mapper tree in web browser failed.");
        Log.error(ex);
      }
    }

  }


}









