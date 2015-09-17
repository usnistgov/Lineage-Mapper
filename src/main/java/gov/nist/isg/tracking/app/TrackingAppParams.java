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


package main.java.gov.nist.isg.tracking.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import ij.plugin.frame.Recorder;
import main.java.gov.nist.isg.tracking.lib.Log;

public class TrackingAppParams extends AppParameters {

  private static final String newline = "\r\n";

  //	********************** Singleton Constructor
  private TrackingAppParams() {
    this.resetToDefaultParams();
  }

  public static TrackingAppParams getInstance() {
    return TrackingAppParamsHolder.INSTANCE;
  }

  //	********************** GUI Parameter Help
  public boolean writeParamsToFile(File file) {
    try {
      FileWriter fw = new FileWriter(file.getAbsolutePath());
      BufferedWriter bw = new BufferedWriter(fw);

      DecimalFormat df = new DecimalFormat("#.##");

      // Change percentages to indexes

      // Cost Function
      bw.write(WGT_CELL_OVERLAP_DESC + DS + df.format(weightCellOverlap) + newline);
      bw.write(WGT_CENTROIDS_DIST_DESC + DS + df.format(weightCentroids) + newline);
      bw.write(WGT_CELL_SIZE_DESC + DS + df.format(weightCellSize) + newline);
      bw.write(MAX_CENTROID_DIST_DESC + DS + df.format(maxCentroidsDist) + newline);

      // Confidence Index
      bw.write(MIN_CELL_LIFE_DESC + DS + minCellLife + newline);
      bw.write(CELL_DEATH_CENT_DESC + DS + df.format(cellDeathDeltaThreshold) + newline);
      bw.write(DENSITY_AFFECT_CI_DESC + DS + cellDensityAffectsCI + newline);
      bw.write(BORDER_AFFECT_CI_DESC + DS + borderCellAffectsCI + newline);

      // Division
      bw.write(D_SIZE_SIM_DESC + DS + df.format(daughterSizeSimilarity) + newline);
      bw.write(MIN_DIV_OVERLAP_DESC + DS + df.format(divisionOverlapThreshold) + newline);
      bw.write(D_ASPECT_RATIO_SIM_DESC + DS + df.format(daughterAspectRatioSimilarity) + newline);
      bw.write(MOTHER_CIRC_IDX_DESC + DS + df.format(motherCircularityThreshold) + newline);
      bw.write(NUM_FRAMES_CIRC_CHECK_DESC + DS + numFramesToCheckCircularity + newline);
      bw.write(DIV_ENABLED_DESC + DS + enableCellDivision + newline);

      // Fusion
      bw.write(MIN_CELL_AREA_DESC + DS + cellSizeThreshold + newline);
      bw.write(MIN_FUSION_OVERLAP_DESC + DS + df.format(fusionOverlapThreshold) + newline);
      bw.write(FUSION_ENABLED_DESC + DS + enableCellFusion + newline);

      // Output
      bw.write(OUTPUT_DIRECTORY_DESC + DS + outputDirectory + newline);
      bw.write(OUTPUT_PREFIX_DESC + DS + prefix + newline);
      bw.write(SAVE_OUTPUTS_DESC + DS + saveOutputsEnabled + newline);
      bw.write(LABEL_OUTPUT_MASKS + DS + labelOutputMasksEnabled + newline);

      bw.close();

      Log.mandatory( "Saved Parameters to " + file.getAbsolutePath());

      return true;

    } catch (IOException e) {
      Log.error("Error occurred writing parameters to " + file.getAbsolutePath());
    }
    return false;
  }



  public boolean loadParamsFromFile(File file) {
    try {
      if (!file.exists()) {
        Log.error("Failed to load file (does not exist): " + file.getAbsolutePath());
        return false;
      }

      if (!file.getName().endsWith(".txt")) {
        Log.error("ERROR: Invalid file type - must be of type txt.");
        return false;
      }

      FileReader fr = new FileReader(file.getAbsolutePath());
      BufferedReader br = new BufferedReader(fr);

      String line = null;
      while ((line = br.readLine()) != null) {
        String[] contents = line.split(DS);

        if (contents.length == 2) {
          contents[0] = contents[0].trim();
          contents[1] = contents[1].trim();


          // Cost Function
          if (contents[0].equals(WGT_CELL_OVERLAP_DESC)) {
            weightCellOverlap = loadDouble(contents[1], weightCellOverlap);
          } else if (contents[0].equals(WGT_CENTROIDS_DIST_DESC)) {
            weightCentroids = loadDouble(contents[1], weightCentroids);
          } else if (contents[0].equals(WGT_CELL_SIZE_DESC)) {
            weightCellSize = loadDouble(contents[1], weightCellSize);
          } else if (contents[0].equals(MAX_CENTROID_DIST_DESC)) {
            maxCentroidsDist = loadDouble(contents[1], maxCentroidsDist);
          }

          // Confidence Index
          else if (contents[0].equals(MIN_CELL_LIFE_DESC)) {
            minCellLife = loadInteger(contents[1], minCellLife);
          } else if (contents[0].equals(CELL_DEATH_CENT_DESC)) {
            cellDeathDeltaThreshold = loadDouble(contents[1], cellDeathDeltaThreshold);
          } else if (contents[0].equals(DENSITY_AFFECT_CI_DESC)) {
            cellDensityAffectsCI = loadBoolean(contents[1], cellDensityAffectsCI);
          } else if (contents[0].equals(BORDER_AFFECT_CI_DESC)) {
            borderCellAffectsCI = loadBoolean(contents[1], borderCellAffectsCI);
          }

          // Division
          else if (contents[0].equals(D_SIZE_SIM_DESC)) {
            daughterSizeSimilarity = loadDouble(contents[1], daughterSizeSimilarity);
          } else if (contents[0].equals(MIN_DIV_OVERLAP_DESC)) {
            divisionOverlapThreshold = loadDouble(contents[1], divisionOverlapThreshold);
          } else if (contents[0].equals(D_ASPECT_RATIO_SIM_DESC)) {
            daughterAspectRatioSimilarity = loadDouble(contents[1], daughterAspectRatioSimilarity);
          } else if (contents[0].equals(MOTHER_CIRC_IDX_DESC)) {
            motherCircularityThreshold = loadDouble(contents[1], motherCircularityThreshold);
          } else if (contents[0].equals(NUM_FRAMES_CIRC_CHECK_DESC)) {
            numFramesToCheckCircularity = loadInteger(contents[1], numFramesToCheckCircularity);
          } else if (contents[0].equals(DIV_ENABLED_DESC)) {
            enableCellDivision = loadBoolean(contents[1], enableCellDivision);
          }

          // Fusion
          else if (contents[0].equals(MIN_CELL_AREA_DESC)) {
            cellSizeThreshold = loadInteger(contents[1], cellSizeThreshold);
          } else if (contents[0].equals(MIN_FUSION_OVERLAP_DESC)) {
            fusionOverlapThreshold = loadDouble(contents[1], fusionOverlapThreshold);
          } else if (contents[0].equals(FUSION_ENABLED_DESC)) {
            enableCellFusion = loadBoolean(contents[1], enableCellFusion);
          }

          // Output
          else if (contents[0].equals(OUTPUT_DIRECTORY_DESC)) {
            outputDirectory = contents[1];
          } else if (contents[0].equals(OUTPUT_PREFIX_DESC)) {
            prefix = contents[1];
          } else if (contents[0].equals(SAVE_OUTPUTS_DESC)) {
            saveOutputsEnabled = loadBoolean(contents[1], saveOutputsEnabled);
          } else if (contents[0].equals(LABEL_OUTPUT_MASKS)) {
            labelOutputMasksEnabled = loadBoolean(contents[1], labelOutputMasksEnabled);
          }


        }
      }

      br.close();

      return true;

    } catch (IOException e) {
      Log.error(e.getMessage());
    }
    return false;
  }


  private double loadDouble(String val, double def) {
    try {
      return Double.parseDouble(val.trim());
    } catch (NumberFormatException e) {
      Log.error(
          "Error in loading double value: " + val + " using default: " + def);
      return def;
    }
  }

  private int loadInteger(String val, int def) {
    try {
      return Integer.parseInt(val.trim());
    } catch (NumberFormatException e) {
      Log.error(
          "Error in loading integer value: " + val + " using default: " + def);
      return def;
    }
  }

  private boolean loadBoolean(String val, boolean def) {
    try {
      return Boolean.parseBoolean(val.trim());
    } catch (NumberFormatException e) {
      Log.error(
          "Error in loading boolean value: " + val + " using default: " + def);
      return def;
    }
  }


  public void logParams() {

    Log.debug("");
    Log.debug( "\t-------- Lineage Mapper Parameters --------");

    // Cost Function
    Log.debug( "Cost Function Parameters:");
    Log.debug( WGT_CELL_OVERLAP_DESC + DS + weightCellOverlap);
    Log.debug( WGT_CENTROIDS_DIST_DESC + DS + weightCentroids);
    Log.debug( WGT_CELL_SIZE_DESC + DS + weightCellSize);
    Log.debug( MAX_CENTROID_DIST_DESC + DS + maxCentroidsDist);

    // Confidence Index
    Log.debug("");
    Log.debug( "Confidence Index Parameters:");
    Log.debug( MIN_CELL_LIFE_DESC + DS + minCellLife);
    Log.debug( CELL_DEATH_CENT_DESC + DS + cellDeathDeltaThreshold);
    Log.debug( DENSITY_AFFECT_CI_DESC + DS + cellDensityAffectsCI);
    Log.debug( BORDER_AFFECT_CI_DESC + DS + borderCellAffectsCI);

    // Division
    Log.debug("");
    Log.debug( "Division Parameters:");
    Log.debug( D_SIZE_SIM_DESC + DS + daughterSizeSimilarity);
    Log.debug( MIN_DIV_OVERLAP_DESC + DS + divisionOverlapThreshold);
    Log.debug( D_ASPECT_RATIO_SIM_DESC + DS + daughterAspectRatioSimilarity);
    Log.debug( MOTHER_CIRC_IDX_DESC + DS + motherCircularityThreshold);
    Log.debug( NUM_FRAMES_CIRC_CHECK_DESC + numFramesToCheckCircularity);
    Log.debug( DIV_ENABLED_DESC + DS + enableCellDivision);

    // Fusion
    Log.debug("");
    Log.debug( "Fusion Parameters:");
    Log.debug( MIN_CELL_AREA_DESC + DS + cellSizeThreshold);
    Log.debug( MIN_FUSION_OVERLAP_DESC + DS + fusionOverlapThreshold);
    Log.debug( FUSION_ENABLED_DESC + DS + enableCellFusion);

    // Output
    Log.debug("");
    Log.debug( "Output Parameters:");
    Log.debug( OUTPUT_DIRECTORY_DESC + DS + outputDirectory);
    Log.debug( OUTPUT_PREFIX_DESC + DS + prefix);
    Log.debug( SAVE_OUTPUTS_DESC + DS + saveOutputsEnabled);
    Log.debug( LABEL_OUTPUT_MASKS + DS + labelOutputMasksEnabled);

    Log.debug( "\t-------- End Lineage Mapper Parameters --------");
    Log.debug("");
  }





  public void recordPreferences() {

    if(!pullParamsFromGui()) {
      Log.mandatory("Error: attempting to record invalid parameter into preferences file.");
      return;
    }

    Preferences pref = Preferences.userRoot().node(getPreferencesName());

    try {
      pref.clear();
    } catch (BackingStoreException e) {
      Log.mandatory("Error unable clear preferences: " + e.getMessage());
      return;
    }

    Log.mandatory("Recording user preferences");
    // Cost Function
    pref.putDouble(WGT_CELL_OVERLAP_DESC, weightCellOverlap);
    pref.putDouble(WGT_CENTROIDS_DIST_DESC, weightCentroids);
    pref.putDouble(WGT_CELL_SIZE_DESC, weightCellSize);
    pref.putDouble(MAX_CENTROID_DIST_DESC, maxCentroidsDist);

    // Confidence Index
    pref.putInt(MIN_CELL_LIFE_DESC, minCellLife);
    pref.putDouble(CELL_DEATH_CENT_DESC, cellDeathDeltaThreshold);
    pref.putBoolean(DENSITY_AFFECT_CI_DESC, cellDensityAffectsCI);
    pref.putBoolean(BORDER_AFFECT_CI_DESC, borderCellAffectsCI);

    // Division
    pref.putDouble(D_SIZE_SIM_DESC, daughterSizeSimilarity);
    pref.putDouble(MIN_DIV_OVERLAP_DESC, divisionOverlapThreshold);
    pref.putDouble(D_ASPECT_RATIO_SIM_DESC, daughterAspectRatioSimilarity);
    pref.putDouble(MOTHER_CIRC_IDX_DESC, motherCircularityThreshold);
    pref.putInt(NUM_FRAMES_CIRC_CHECK_DESC, numFramesToCheckCircularity);
    pref.putBoolean(DIV_ENABLED_DESC, enableCellDivision);

    // Fusion
    pref.putInt(MIN_CELL_AREA_DESC, cellSizeThreshold);
    pref.putDouble(MIN_FUSION_OVERLAP_DESC, fusionOverlapThreshold);
    pref.putBoolean(FUSION_ENABLED_DESC, enableCellFusion);

    // Output
    pref.put(OUTPUT_DIRECTORY_DESC, outputDirectory);
    pref.put(OUTPUT_PREFIX_DESC, prefix);
    pref.putBoolean(SAVE_OUTPUTS_DESC, saveOutputsEnabled);
    pref.putBoolean(LABEL_OUTPUT_MASKS, labelOutputMasksEnabled);
    pref.putBoolean(SAVE_OUTPUTS_STACK_DESC, saveAsStackEnabled);

    try {
      pref.flush();
    } catch (BackingStoreException e) {
      Log.mandatory("Error unable to record preferences: " + e.getMessage());
    }
  }


  public void loadPreferences() {

    Preferences pref = Preferences.userRoot().node(getPreferencesName());

    try {
      pref.sync();
    } catch (BackingStoreException e) {
      Log.error("Error synchronizing preferences: " + e.getMessage());
    }

    Log.mandatory("Loading user preferences");
    // Cost Function
    weightCellOverlap = pref.getDouble(WGT_CELL_OVERLAP_DESC, weightCellOverlap);
    weightCentroids = pref.getDouble(WGT_CENTROIDS_DIST_DESC, weightCentroids);
    weightCellSize = pref.getDouble(WGT_CELL_SIZE_DESC, weightCellSize);
    maxCentroidsDist = pref.getDouble(MAX_CENTROID_DIST_DESC, maxCentroidsDist);

    // Confidence Index
    minCellLife = pref.getInt(MIN_CELL_LIFE_DESC, minCellLife);
    cellDeathDeltaThreshold = pref.getDouble(CELL_DEATH_CENT_DESC, cellDeathDeltaThreshold);
    cellDensityAffectsCI = pref.getBoolean(DENSITY_AFFECT_CI_DESC, cellDensityAffectsCI);
    borderCellAffectsCI = pref.getBoolean(BORDER_AFFECT_CI_DESC, borderCellAffectsCI);

    // Division
    daughterSizeSimilarity = pref.getDouble(D_SIZE_SIM_DESC, daughterSizeSimilarity);
    divisionOverlapThreshold = pref.getDouble(MIN_DIV_OVERLAP_DESC, divisionOverlapThreshold);
    daughterAspectRatioSimilarity =
        pref.getDouble(D_ASPECT_RATIO_SIM_DESC, daughterAspectRatioSimilarity);
    motherCircularityThreshold = pref.getDouble(MOTHER_CIRC_IDX_DESC, motherCircularityThreshold);
    numFramesToCheckCircularity = pref.getInt(NUM_FRAMES_CIRC_CHECK_DESC,
                                              numFramesToCheckCircularity);
    enableCellDivision = pref.getBoolean(DIV_ENABLED_DESC, enableCellDivision);

    // Fusion
    cellSizeThreshold = pref.getInt(MIN_CELL_AREA_DESC, cellSizeThreshold);
    fusionOverlapThreshold = pref.getDouble(MIN_FUSION_OVERLAP_DESC, fusionOverlapThreshold);
    enableCellFusion = pref.getBoolean(FUSION_ENABLED_DESC, enableCellFusion);

    // Output
    outputDirectory = pref.get(OUTPUT_DIRECTORY_DESC, outputDirectory);
    prefix = pref.get(OUTPUT_PREFIX_DESC, prefix);
    saveOutputsEnabled = pref.getBoolean(SAVE_OUTPUTS_DESC, saveOutputsEnabled);
    labelOutputMasksEnabled = pref.getBoolean(LABEL_OUTPUT_MASKS, labelOutputMasksEnabled);
    saveAsStackEnabled = pref.getBoolean(SAVE_OUTPUTS_STACK_DESC, saveAsStackEnabled);

    pushParamsToGUI();

  }



  public void record() {
    Log.mandatory("Recording Macro Options");

    String paramNames = "// Parameters:\n";
    String command = "call(\"main.java.gov.nist.isg.tracking.Cell_Tracker_Plugin.macro";

    // Cost Parameters
    command += "\", \"" + Double.toString(weightCellOverlap);
    command += "\", \"" + Double.toString(weightCentroids);
    command += "\", \"" + Double.toString(weightCellSize);
    command += "\", \"" + Double.toString(maxCentroidsDist);
    paramNames += "// Weight Cell Overlap: range [0,1]\n"
                  + "// Weight Cell Centroids: range [0,1]\n"
                  + "// Weight Cell Size: range [0,1]\n"
                  + "// Max Centroids Distance: range [0, inf]\n";

    // Confidence Index Parameters
    command += "\", \"" + Integer.toString(minCellLife);
    command += "\", \"" + Double.toString(cellDeathDeltaThreshold);
    command += "\", \"" + Boolean.toString(cellDensityAffectsCI);
    command += "\", \"" + Boolean.toString(borderCellAffectsCI);
    paramNames += "// Min Cell Life: range [0, inf]\n"
                  + "// Cell Death Delta Centroids Threshold: range [0, inf]\n"
                  + "// Cell Density Affects Confidence: range [true, false]\n"
                  + "// Border Cell Affects Confidence: range [true, false]\n";

    // Division Parameters
    command += "\", \"" + Double.toString(daughterSizeSimilarity);
    command += "\", \"" + Double.toString(divisionOverlapThreshold);
    command += "\", \"" + Double.toString(daughterAspectRatioSimilarity);
    command += "\", \"" + Double.toString(motherCircularityThreshold);
    command += "\", \"" + Integer.toString(numFramesToCheckCircularity);
    command += "\", \"" + Boolean.toString(enableCellDivision);
    paramNames += "// Daughter Size Similarity: range [0,1]\n"
                  + "// Division Overlap Threshold: range [0,1]\n"
                  + "// Daughter Aspect Ratio Similarity: range [0,1]\n"
                  + "// Mother Cell Circularity Threshold: range [0,1]\n"
                  + "// Number Frames Check Circularity: range [0, inf]\n"
                  + "// Enable Cell Division: range [true, false]\n";

    // Fusion Parameters
    command += "\", \"" + Integer.toString(cellSizeThreshold);
    command += "\", \"" + Double.toString(fusionOverlapThreshold);
    command += "\", \"" + Boolean.toString(enableCellFusion);
    paramNames += "// Cell Size Threshold: range [0, inf]\n"
                  + "// Fusion Overlap Threshold: range [0,1]\n"
                  + "// Enable Cell Fusion: range [true, false]\n";

    // Output Parameters
    command += "\", \"" + outputDirectory.replace("\\","\\\\");
    command += "\", \"" + prefix;
    command += "\", \"" + Boolean.toString(saveOutputsEnabled);
    paramNames += "// Output Directory: a valid file path\n"
                  + "// Output Prefix: zero or more valid file name characters\n"
                  + "// Save Outputs Enabled: range [true, false]\n";


    command += "\");\n";
    if(Recorder.record) {
      Recorder.recordString(paramNames);
      Recorder.recordString(command);
    }
  }


  public String generateOutputImageFileName(int nb) {
    if(inputStack == null) {
      return null;
    }

    if(saveAsStackEnabled) {
     return prefix + ".tif";
    }else {
      String temp = Integer.toString(Math.max(4, Integer.toString(inputStack.getSize()).length()));

      return prefix + String.format("%0" + temp + "d", nb) + ".tif";
    }
  }


  private static class TrackingAppParamsHolder {

    public static final TrackingAppParams INSTANCE = new TrackingAppParams();
  }


}
