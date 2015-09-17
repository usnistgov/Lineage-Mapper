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

package main.java.gov.nist.isg.tracking;

import java.io.File;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.VirtualStack;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import main.java.gov.nist.isg.tracking.app.TrackingAppParams;
import main.java.gov.nist.isg.tracking.app.gui.CellTrackerGUI;
import main.java.gov.nist.isg.tracking.display.OverlayObjectLabels;
import main.java.gov.nist.isg.tracking.lib.Log;
import main.java.gov.nist.isg.tracking.metadata.CellTrackerMetadata;

public class Cell_Tracker_Plugin implements PlugIn {

  public void run(String arg) {

    try {
      // prevents the recorder from printing the call to launch the macro to the recorder window
      // this is done because when using a JFrame macro recording is taken care of explicitly as
      // opposed to allowing the generic dialog to handle it
      Recorder.setCommand(null);

      // create the gui to get the remainder of the options for the CT
      CellTrackerGUI app = new CellTrackerGUI(true);



    } catch (Exception e) {
      e.printStackTrace();
      StackTraceElement[] st = e.getStackTrace();
      Log.error("Exception in main.java.gov.nist.isg.tracking.Cell_Tracker_Plugin.run: " +  e.getMessage());
      for (int i = 0; i < st.length; i++) {
        Log.error(st[i].toString());
      }
      IJ.error("Exception in main.java.gov.nist.isg.tracking.Cell_Tracker_Plugin.run: " + e.getMessage());
    }
  }


  public static void macro(String s1,String s2,String s3,String s4,String s5,String s6,String s7,
                           String s8,String s9,String s10,String s11,String s12,String s13,String s14,
                           String s15,String s16,String s17,String s18,String s19,String s20) {

    try {

      getInputStack();

      CellTrackerGUI app = new CellTrackerGUI(false);
      TrackingAppParams params = TrackingAppParams.getInstance();
      Log.setLogLevel(Log.LogType.MANDATORY);

      // Load the parameters into params
      // Cost Parameters
      params.setWeightCellOverlap(Double.parseDouble(s1));
      params.setWeightCentroids(Double.parseDouble(s2));
      params.setWeightCellSize(Double.parseDouble(s3));
      params.setMaxCentroidsDist(Double.parseDouble(s4));

      // Confidence Index Parameters
      params.setMinCellLife((int) Math.round(Double.parseDouble(s5)));
      params.setCellDeathDeltaTreshold(Double.parseDouble(s6));
      params.setCellDensityAffectsCI(Boolean.parseBoolean(s7));
      params.setBorderCellAffectsCI(Boolean.parseBoolean(s8));

      // Division Parameters
      params.setDaughterSizeSimilarity(Double.parseDouble(s9));
      params.setDivisionOverlapThreshold(Double.parseDouble(s10));
      params.setDaughterAspectRatioSimilarity(Double.parseDouble(s11));
      params.setMotherCircularityThreshold(Double.parseDouble(s12));
      params.setNumFramesToCheckCircularity((int)Math.round(Double.parseDouble(s13)));
      params.setEnableCellDivision(Boolean.parseBoolean(s14));

      // Fusion Parameters
      params.setCellSizeThreshold((int)Math.round(Double.parseDouble(s15)));
      params.setFusionOverlapThreshold(Double.parseDouble(s16));
      params.setEnableCellFusion(Boolean.parseBoolean(s17));

      // Output Parameters
      params.setOutputDirectory(s18.trim());
      params.setPrefix(s19.trim());
      params.setSaveOutputsEnabled(Boolean.parseBoolean(s20));

      params.setInputStack(getInputStack());
      track(true);

    } catch (Exception e) {
      e.printStackTrace();
      StackTraceElement[] st = e.getStackTrace();
      Log.error("Exception in main.java.gov.nist.isg.tracking.Cell_Tracker_Plugin.macro: " + e.getMessage());
      for (int i = 0; i < st.length; i++) {
        Log.error(st[i].toString());
      }
      IJ.error("Exception in main.java.gov.nist.isg.tracking.Cell_Tracker_Plugin.macro: " + e.getMessage());
    }
  }

  public static void track(boolean macro) {


    try { // to catch thread interrupted exceptions if the user cancels execution.


      TrackingAppParams params = TrackingAppParams.getInstance();

      params.validateParameters();

      // reset metadata generated in previous runs
      if(params.getBirthDeathMetadata() != null) { params.getBirthDeathMetadata().reset(); }
      if(params.getDivisionMetadata() != null) { params.getDivisionMetadata().reset(); }
      if(params.getFusionMetadata() != null) { params.getFusionMetadata().reset(); }
      if(params.getConfidenceIndexMetadata() != null) { params.getConfidenceIndexMetadata().reset(); }


      // Limit the number of frames available so that the code does not try to check more frames than exist
      params.setNumFramesToCheckCircularity(
          Math.min(params.getNumFramesToCheckCircularity(), params.getInputStack().getSize()));
      params.setMinCellLife(Math.min(params.getMinCellLife(), params.getInputStack().getSize()));

      ImageStack outputStack = null;
      ImageStack inputStack = params.getInputStack();


      if(inputStack == null)
        throw new IllegalArgumentException("No input Image Stack found");


      // if the output folder does not exist create it
      if(params.isSaveOutputsEnabled()) {
        File f = new File(params.getOutputDirectory());
        if(!f.exists()) {
          f.mkdir();
        }
      }

      if(inputStack.isVirtual()) {
        String path = params.getOutputDirectory();
        if(!params.isSaveOutputsEnabled())
          path = null;
        if(path != null) {
          if (!path.endsWith(File.separator))
            path += File.separator;
          outputStack =
              new VirtualStack(inputStack.getWidth(), inputStack.getHeight(),
                               inputStack.getColorModel(), path);
        }else{
          if(!macro)
          IJ.error("Virtual Stack Error","When using a virtual stack for input, failure to specify an output directory will result in no images being saved or displayed.");
        }
      }else{
        outputStack = new ImageStack(inputStack.getWidth(), inputStack.getHeight());
      }
      params.setOutputStack(outputStack);

      if(outputStack != null) {
        while (outputStack.getSize() > 0) {
          outputStack.deleteLastSlice();
        }
      }

      Log.mandatory("Tracking Start");
      SimpleCellTracker ct = new SimpleCellTracker(params);
      ct.worker();
      Log.mandatory("Tracking Done");

      TrackingAppParams.getInstance();
      if(TrackingAppParams.getInstance().isSaveOutputsEnabled()) {
        Log.mandatory("Generating Output Metadata");
        CellTrackerMetadata.generateAndSaveMetadata();
        Log.mandatory("Done");
      }

      showOutputStack();

      TrackingAppParams.getInstance().recordPreferences();

    } catch (InterruptedException e) {
      Log.error("Tracking Interrupted");
      Log.error(e);
    }

  }


  private static void showOutputStack() {

    ImageStack outputStack = TrackingAppParams.getInstance().getOutputStack();
    if(outputStack != null) {
      ImagePlus temp = (new ImagePlus("Tracking Results", outputStack));
      if(TrackingAppParams.getInstance().isLabelOutputMasksEnabled()) {

        temp = OverlayObjectLabels.overlayLabels(temp);
      }
      temp.show();
    }
  }

  private static ImageStack getInputStack() {

    // get the currently open image stack
    ImageStack inputStack = IJ.getImage().getImageStack();
    if (inputStack == null) {
      throw new IllegalArgumentException(
          "Input image stack is null.\nThis command requires an open image stack of labeled masks to track.");
    }
    return inputStack;
  }


  public static void main(String args[]) {
    new ImageJ();

    Cell_Tracker_Plugin ct = new Cell_Tracker_Plugin();
    ct.run("null");

  }

}
