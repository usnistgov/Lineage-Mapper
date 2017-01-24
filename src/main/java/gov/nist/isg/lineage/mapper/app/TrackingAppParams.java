// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package gov.nist.isg.lineage.mapper.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import gov.nist.isg.lineage.mapper.Lineage_Mapper_Plugin;
import gov.nist.isg.lineage.mapper.app.gui.CellTrackerGUI;
import gov.nist.isg.lineage.mapper.app.gui.panels.AdvancedPanel;
import gov.nist.isg.lineage.mapper.app.gui.panels.OptionsPanel;
import gov.nist.isg.lineage.mapper.app.utils.MacroUtils;
import gov.nist.isg.lineage.mapper.lib.ImageFrame;
import gov.nist.isg.lineage.mapper.lib.Log;
import gov.nist.isg.lineage.mapper.metadata.BirthDeathMetadata;
import gov.nist.isg.lineage.mapper.metadata.ConfidenceIndexMetadata;
import gov.nist.isg.lineage.mapper.metadata.DivisionMetadata;
import gov.nist.isg.lineage.mapper.metadata.FusionMetadata;
import gov.nist.isg.lineage.mapper.metadata.ObjectPositionMetadata;
import gov.nist.isg.lineage.mapper.textfield.validator.ValidatorRegex;
import ij.IJ;
import ij.plugin.frame.Recorder;

/**
 * Class to contain all of the parameters required to perform tracking.
 */
public class TrackingAppParams {

  private static final String newline = "\r\n";


  private static final String APP_TITLE = "Lineage Mapper";
  private static final String PreferencesName = "lineage_mapper";

  //	Descriptions used in writing, loading, and saving gov.nist.isg.lineage.mapper.app parameters
  public static final String DS = ": "; // description separator
  public static final String WGT_CELL_OVERLAP_DESC = "weightCellOverlap";
  public static final String WGT_CENTROIDS_DIST_DESC = "weightCentroidsDistance";
  public static final String WGT_CELL_SIZE_DESC = "weightCellSize";
  public static final String MAX_CENTROID_DIST_DESC = "maxCentroidsDistance";
  public static final String MIN_CELL_LIFE_DESC = "minCellLife";
  public static final String CELL_DEATH_CENT_DESC = "cellDeathDeltaCentroid";
  public static final String DENSITY_AFFECT_CI_DESC = "cellDensityAffectsCI";
  public static final String BORDER_AFFECT_CI_DESC = "borderCellAffectsCI";
  public static final String D_SIZE_SIM_DESC = "daughterSizeSimilarity";
  public static final String MIN_DIV_OVERLAP_DESC = "minDivisionOverlap";
  public static final String D_ASPECT_RATIO_SIM_DESC = "daughterAspectRatioSimilarity";
  public static final String MOTHER_CIRC_IDX_DESC = "motherCircularityIndex";
  public static final String NUM_FRAMES_CIRC_CHECK_DESC = "numFramesToCheckCircularity";
  public static final String DIV_ENABLED_DESC = "enableCellDivision";
  public static final String MIN_CELL_AREA_DESC = "minCellArea";
  public static final String MIN_FUSION_OVERLAP_DESC = "minFusionOverlap";
  public static final String FUSION_ENABLED_DESC = "enableCellFusion";

  public static final String INPUT_DIRECTORY_DESC = "inputDirectory";
  public static final String FILENAME_PATTERN_DESC = "filenamePrefix";
  public static final String OUTPUT_DIRECTORY_DESC = "outputDirectory";
  public static final String OUTPUT_PREFIX_DESC = "outputPrefix";

  // Images
  private String inputDirectory = System.getProperty("user.home");
  private String filenamePattern = "img_{iiii}.tif";
  private String outputDirectory = System.getProperty("user.home");
  private String outputPrefix = "trk-";

  //	Cost Function
  private double weightCellOverlap = 1.0; // percentage
  private double weightCentroids = 0.5; // percentage
  private double weightCellSize = 0.2; // percentage
  private double maxCentroidsDist = 50.0;
  //	Confidence Index
  private int minCellLife = 32;
  private double cellDeathDeltaThreshold = 10;
  private boolean cellDensityAffectsCI = true;
  private boolean borderCellAffectsCI = true;
  //	Division
  private double daughterSizeSimilarity = 0.5; // percentage
  private double divisionOverlapThreshold = 0.2; // percentage
  private double daughterAspectRatioSimilarity = 0.7; // percentage
  private double motherCircularityThreshold = 0.3; // percentage
  private int numFramesToCheckCircularity = 5;
  private boolean enableCellDivision = true;
  //	Fusion
  private int cellSizeThreshold = 100; // aka min cell area
  private double fusionOverlapThreshold = 0.2; // percentage
  private boolean enableCellFusion = false;


  private CellTrackerGUI ctGUI;
  private boolean tracking = false;
  private boolean macro = false;
  private Thread trackingThread = null;
  private boolean saveMetadata = true;
  private boolean saveLineageViewerPage = true;

  private BirthDeathMetadata birthDeathMetadata = null;
  private DivisionMetadata divisionMetadata = null;
  private FusionMetadata fusionMetadata = null;
  private ConfidenceIndexMetadata confidenceIndexMetadata = null;
  private ObjectPositionMetadata objectPositionMetadata = null;
  private List<ImageFrame> framesList = null;



  /**
   * Validates the current parameter values.
   * @throws IllegalArgumentException if invalid parameters are found.
   */
  public void validateParameters() throws IllegalArgumentException {

    String errors = "";

    // Filepath Options
    ValidatorRegex filenamePatternValidator = new ValidatorRegex(OptionsPanel
        .filenamePatternRegex, OptionsPanel
        .filenamePatternExample);
    if(!filenamePatternValidator.validate(filenamePattern))
      errors += "Invalid FilenamePattern: \"" + filenamePattern + "\"\n";

    // Input Options
    if(cellSizeThreshold < 0)
      errors += "Invalid Minimum Object Size: \"" + cellSizeThreshold + "\"\n";
    if(maxCentroidsDist < 0)
      errors += "Invalid Maximum Centroid Displacement: \"" + maxCentroidsDist + "\"\n";

    // Advanced Options
    // Cost Function
    if(weightCellOverlap < 0 || weightCellOverlap > 1)
      errors += "Invalid Weight Cell Overlap: \"" + weightCellOverlap + "\"\n";
    if(weightCellSize < 0 || weightCellSize > 1)
      errors += "Invalid Weight Cell Size: \"" + weightCellSize + "\"\n";
    if(weightCentroids < 0 || weightCentroids > 1)
      errors += "Invalid Weight Cell Centroid Distance: \"" + weightCentroids + "\"\n";
    // Division
    if(divisionOverlapThreshold < 0 || divisionOverlapThreshold > 1)
      errors += "Invalid Minimum Division Overlap: \"" + divisionOverlapThreshold + "\"\n";
    if(daughterSizeSimilarity < 0 || daughterSizeSimilarity > 1)
      errors += "Invalid Daughter Size Similarity: \"" + daughterSizeSimilarity + "\"\n";
    if(daughterAspectRatioSimilarity < 0 || daughterAspectRatioSimilarity > 1)
      errors += "Invalid Daughter Aspect Ratio Similarity: \"" + daughterAspectRatioSimilarity + "\"\n";
    if(motherCircularityThreshold < 0 || motherCircularityThreshold > 1)
      errors += "Invalid Mother Cell Circularity Threshold: \"" + motherCircularityThreshold + "\"\n";
    if(numFramesToCheckCircularity < 0)
      errors += "Invalid Number Frames to Check Circularity: \"" + numFramesToCheckCircularity + "\"\n";

    if (!errors.isEmpty()) {
      Log.setLogLevel(Log.LogType.MANDATORY);
      Log.error("Invalid Parameter(s):");
      Log.error(errors);
      IJ.error("Invalid Parameter Found: See Log for Details");

      throw new IllegalArgumentException("Invalid Parameters found");
    }
  }

  /**
   * Sets the progress bar to a percentage complete. Values outside of [0,1] will be set to the
   * nearest value within that range.
   * @param val percentage to display within [0,1]
   */
  public void setProgressBar(double val) {
    if( ctGUI!= null) {
      // constrain val to [0,1]
      val = (val < 0) ? 0 : val;
      val = (val > 1) ? 1 : val;
      ctGUI.getControlPanel().getProgressBar().setValue((int) Math.round(val * 100));
    }
  }

  /**
   * Resets the parameters in this instance to their default values.
   */
  public void resetToDefaultParams() {

    inputDirectory = System.getProperty("user.home");
    filenamePattern = "img_{iiii}.tif";
    outputDirectory = System.getProperty("user.home");
    outputPrefix = "trk-";

    //	Cost Function
    weightCellOverlap = 1; // percentage
    weightCentroids = 0.5; // percentage
    weightCellSize = 0.2; // percentage
    maxCentroidsDist = 50.0;
    //	Confidence Index
    minCellLife = 32;
    cellDeathDeltaThreshold = 10;
    cellDensityAffectsCI = true;
    borderCellAffectsCI = true;
    //	Division
    daughterSizeSimilarity = 0.5; // percentage
    divisionOverlapThreshold = 0.2; // percentage
    daughterAspectRatioSimilarity = 0.7; // percentage
    motherCircularityThreshold = 0.3; // percentage
    numFramesToCheckCircularity = 5;
    enableCellDivision = true;
    //	Fusion
    cellSizeThreshold = 100; // aka min cell area
    fusionOverlapThreshold = 0.2; // percentage
    enableCellFusion = false;

    birthDeathMetadata = null;
    divisionMetadata = null;
    fusionMetadata = null;
    confidenceIndexMetadata = null;
  }


  /**
   * Push parameters from the instance of TrackingAppParams to their respective GUI elements
   */
  public void pushParamsToGUI() {
    if (this.ctGUI != null){
      this.ctGUI.getOptionsPanel().pushParamsToGUI();
      this.ctGUI.getAdvancedPanel().pushParamsToGUI();
    }
  }

  public boolean isSaveMetadata() { return saveMetadata; }

  public void setIsSaveMetadata(boolean val) { saveMetadata = val; }

  public boolean isSaveLineageViewerPage() { return saveLineageViewerPage; }

  public void setIsSaveLineageViewerPage(boolean val) { saveLineageViewerPage = val; }


  public boolean isTracking() {
    return tracking;
  }

  public void setIsTracking(boolean val) {
    tracking = val;
  }

  public boolean isMacro() {
    return macro;
  }

  public void setIsMacro(boolean val) {
    macro = val;
  }

  public Thread getTrackingThread() {
    return trackingThread;
  }

  public void setTrackingThread(Thread t) {
    trackingThread = t;
  }

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(String val) {
    outputDirectory = val;
    if (!outputDirectory.endsWith(File.separator))
      outputDirectory += File.separator;
  }

  public String getInputDirectory() {
    return inputDirectory;
  }

  public void setInputDirectory(String val) {
    inputDirectory = val;
    if (!inputDirectory.endsWith(File.separator))
      inputDirectory += File.separator;
  }

  public String getFilenamePattern() {
    return filenamePattern;
  }

  public void setFilenamePattern(String val) {
    filenamePattern = val;
  }

  public String getOutputPrefix() {
    return outputPrefix;
  }

  public void setOutputPrefix(String val) {
    outputPrefix = val;
  }


  public BirthDeathMetadata getBirthDeathMetadata() {
    return birthDeathMetadata;
  }

  public void setBirthDeathMetadata(BirthDeathMetadata birthDeathMetadata) {
    this.birthDeathMetadata = birthDeathMetadata;
  }

  public DivisionMetadata getDivisionMetadata() {
    return divisionMetadata;
  }

  public void setDivisionMetadata(DivisionMetadata divisionMetadata) {
    this.divisionMetadata = divisionMetadata;
  }

  public FusionMetadata getFusionMetadata() {
    return fusionMetadata;
  }

  public void setFusionMetadata(FusionMetadata fusionMetadata) {
    this.fusionMetadata = fusionMetadata;
  }

  public ConfidenceIndexMetadata getConfidenceIndexMetadata() {
    return confidenceIndexMetadata;
  }

  public void setConfidenceIndexMetadata(ConfidenceIndexMetadata confidenceIndexMetadata) {
    this.confidenceIndexMetadata = confidenceIndexMetadata;
  }

  public ObjectPositionMetadata getObjectPositionMetadata() { return objectPositionMetadata; }

  public void setObjectPositionMetadata(ObjectPositionMetadata objectPositionMetadata) {
    this.objectPositionMetadata = objectPositionMetadata;
  }

  public void setGuiPane(CellTrackerGUI gui) {
    this.ctGUI = gui;
  }

  public CellTrackerGUI getGuiPane() {
    return this.ctGUI;
  }

  public static String getAppTitle() {
    return APP_TITLE;
  }

  public static String getPreferencesName() {
    return PreferencesName;
  }


  public List<ImageFrame> getFramesList() {
    return this.framesList;
  }

  public void setFramesList(List<ImageFrame> framesList) {
    this.framesList = framesList;
  }

  public ImageFrame getImageFrame(int index) {
    return this.framesList.get(index);
  }


  public double getWeightCellOverlap() {
    return weightCellOverlap;
  }

  public void setWeightCellOverlap(double weightCellOverlap) {
    this.weightCellOverlap = weightCellOverlap;
  }

  public double getWeightCentroids() {
    return weightCentroids;
  }

  public void setWeightCentroids(double weightCentroids) {
    this.weightCentroids = weightCentroids;
  }

  public double getWeightCellSize() {
    return weightCellSize;
  }

  public void setWeightCellSize(double weightCellSize) {
    this.weightCellSize = weightCellSize;
  }

  public double getMaxCentroidsDist() {
    return maxCentroidsDist;
  }

  public void setMaxCentroidsDist(double maxCentroidsDist) {
    this.maxCentroidsDist = maxCentroidsDist;
  }

  public int getMinCellLife() {
    return minCellLife;
  }

  public void setMinCellLife(int minCellLife) {
    this.minCellLife = minCellLife;
  }

  public double getCellDeathDeltaThreshold() {
    return cellDeathDeltaThreshold;
  }

  public void setCellDeathDeltaTreshold(double cellDeathDeltaTreshold) {
    this.cellDeathDeltaThreshold = cellDeathDeltaTreshold;
  }

  public boolean isCellDensityAffectsCI() {
    return cellDensityAffectsCI;
  }

  public void setCellDensityAffectsCI(boolean cellDensityAffectsCI) {
    this.cellDensityAffectsCI = cellDensityAffectsCI;
  }

  public boolean isBorderCellAffectsCI() {
    return borderCellAffectsCI;
  }

  public void setBorderCellAffectsCI(boolean borderCellAffectsCI) {
    this.borderCellAffectsCI = borderCellAffectsCI;
  }

  public double getDaughterSizeSimilarity() {
    return daughterSizeSimilarity;
  }

  public void setDaughterSizeSimilarity(double daughterSizeSimilarity) {
    this.daughterSizeSimilarity = daughterSizeSimilarity;
  }

  public double getDivisionOverlapThreshold() {
    return divisionOverlapThreshold;
  }

  public void setDivisionOverlapThreshold(double divisionOverlapThreshold) {
    this.divisionOverlapThreshold = divisionOverlapThreshold;
  }

  public double getDaughterAspectRatioSimilarity() {
    return daughterAspectRatioSimilarity;
  }

  public void setDaughterAspectRatioSimilarity(double daughterAspectRatioSimilarity) {
    this.daughterAspectRatioSimilarity = daughterAspectRatioSimilarity;
  }

  public double getMotherCircularityThreshold() {
    return motherCircularityThreshold;
  }

  public void setMotherCircularityThreshold(double motherCircularityThreshold) {
    this.motherCircularityThreshold = motherCircularityThreshold;
  }

  public int getNumFramesToCheckCircularity() {
    return numFramesToCheckCircularity;
  }

  public void setNumFramesToCheckCircularity(int numFramesToCheckCircularity) {
    this.numFramesToCheckCircularity = numFramesToCheckCircularity;
  }

  public boolean isEnableCellDivision() {
    return enableCellDivision;
  }

  public void setEnableCellDivision(boolean enableCellDivision) {
    this.enableCellDivision = enableCellDivision;
  }

  public int getCellSizeThreshold() {
    return cellSizeThreshold;
  }

  public void setCellSizeThreshold(int cellSizeThreshold) {
    this.cellSizeThreshold = cellSizeThreshold;
  }

  public double getFusionOverlapThreshold() {
    return fusionOverlapThreshold;
  }

  public void setFusionOverlapThreshold(double fusionOverlapThreshold) {
    this.fusionOverlapThreshold = fusionOverlapThreshold;
  }

  public boolean isEnableCellFusion() {
    return enableCellFusion;
  }

  public void setEnableCellFusion(boolean enableCellFusion) {
    this.enableCellFusion = enableCellFusion;
  }


  /**
   * Writes the current set of parameters to the log file.
   */
  public void writeParamsToLog() {

    DecimalFormat df = new DecimalFormat("#.##");

    // Change percentages to indexes

    // Input
    Log.mandatory(INPUT_DIRECTORY_DESC + DS + inputDirectory);
    Log.mandatory(FILENAME_PATTERN_DESC + DS + filenamePattern);

    // Output
    Log.mandatory(OUTPUT_DIRECTORY_DESC + DS + outputDirectory);
    Log.mandatory(OUTPUT_PREFIX_DESC + DS + outputPrefix);

    // Cost Function
    Log.mandatory(WGT_CELL_OVERLAP_DESC + DS + df.format(weightCellOverlap));
    Log.mandatory(WGT_CENTROIDS_DIST_DESC + DS + df.format(weightCentroids));
    Log.mandatory(WGT_CELL_SIZE_DESC + DS + df.format(weightCellSize));
    Log.mandatory(MAX_CENTROID_DIST_DESC + DS + df.format(maxCentroidsDist));

    // Confidence Index
    Log.mandatory(MIN_CELL_LIFE_DESC + DS + minCellLife);
    Log.mandatory(CELL_DEATH_CENT_DESC + DS + df.format(cellDeathDeltaThreshold));
    Log.mandatory(DENSITY_AFFECT_CI_DESC + DS + cellDensityAffectsCI);
    Log.mandatory(BORDER_AFFECT_CI_DESC + DS + borderCellAffectsCI);

    // Division
    Log.mandatory(D_SIZE_SIM_DESC + DS + df.format(daughterSizeSimilarity));
    Log.mandatory(MIN_DIV_OVERLAP_DESC + DS + df.format(divisionOverlapThreshold));
    Log.mandatory(D_ASPECT_RATIO_SIM_DESC + DS + df.format(daughterAspectRatioSimilarity));
    Log.mandatory(MOTHER_CIRC_IDX_DESC + DS + df.format(motherCircularityThreshold));
    Log.mandatory(NUM_FRAMES_CIRC_CHECK_DESC + DS + numFramesToCheckCircularity);
    Log.mandatory(DIV_ENABLED_DESC + DS + enableCellDivision);

    // Fusion
    Log.mandatory(MIN_CELL_AREA_DESC + DS + cellSizeThreshold);
    Log.mandatory(MIN_FUSION_OVERLAP_DESC + DS + df.format(fusionOverlapThreshold));
    Log.mandatory(FUSION_ENABLED_DESC + DS + enableCellFusion);

  }


  /**
   * Writes the current set of parameters to a text file. This file can later be loaded by the
   * loadParamsFromFile functionality.
   * @param file the file to write the parameters into.
   * @return true is successful, false otherwise.
   */
  public boolean writeParamsToFile(File file) {
    try {
      FileWriter fw = new FileWriter(file.getAbsolutePath());
      BufferedWriter bw = new BufferedWriter(fw);

      DecimalFormat df = new DecimalFormat("#.##");

      // Change percentages to indexes

      // Input
      bw.write(INPUT_DIRECTORY_DESC + DS + inputDirectory + newline);
      bw.write(FILENAME_PATTERN_DESC + DS + filenamePattern + newline);

      // Output
      bw.write(OUTPUT_DIRECTORY_DESC + DS + outputDirectory + newline);
      bw.write(OUTPUT_PREFIX_DESC + DS + outputPrefix + newline);

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

      bw.close();

      Log.mandatory("Saved Parameters to " + file.getAbsolutePath());

      return true;

    } catch (IOException e) {
      Log.error("Error occurred writing parameters to " + file.getAbsolutePath());
    }
    return false;
  }

  /**
   * Loads parameters from the file specified into this instance of TrackingAppParams.
   * @param file the file to read the parameters from.
   * @return true is successful, false otherwise.
   */
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

          // Input
          if (contents[0].equals(INPUT_DIRECTORY_DESC)) {
            inputDirectory = contents[1];
          } else if (contents[0].equals(FILENAME_PATTERN_DESC)) {
            filenamePattern = contents[1];
          }

          // Output
          if (contents[0].equals(OUTPUT_DIRECTORY_DESC)) {
            outputDirectory = contents[1];
          } else if (contents[0].equals(OUTPUT_PREFIX_DESC)) {
            outputPrefix = contents[1];
          }

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

        }
      }

      br.close();

      return true;

    } catch (IOException e) {
      Log.error(e.getMessage());
    }
    return false;
  }


  public static double loadDouble(String val, double def) {
    try {
      return Double.parseDouble(val.trim());
    } catch (NumberFormatException e) {
      Log.error(
          "Error in loading double value: " + val + " using default: " + def);
      return def;
    }
  }

  public static int loadInteger(String val, int def) {
    try {
      return Integer.parseInt(val.trim());
    } catch (NumberFormatException e) {
      Log.error(
          "Error in loading integer value: " + val + " using default: " + def);
      return def;
    }
  }

  public static boolean loadBoolean(String val, boolean def) {
    try {
      return Boolean.parseBoolean(val.trim());
    } catch (NumberFormatException e) {
      Log.error(
          "Error in loading boolean value: " + val + " using default: " + def);
      return def;
    }
  }

  /**
   * Records the current state of the tracking parameters into Java Prefs
   */
  public void recordPreferences() {
    if(ctGUI != null)
      ctGUI.copyToTrackingAppParams();

    Preferences pref = Preferences.userRoot().node(getPreferencesName());

    try {
      pref.clear();
    } catch (BackingStoreException e) {
      Log.mandatory("Error unable clear preferences: " + e.getMessage());
      return;
    }

    Log.debug("Recording user preferences");
    // input options
    pref.put(INPUT_DIRECTORY_DESC, inputDirectory);
    pref.put(FILENAME_PATTERN_DESC, filenamePattern);
    // output options
    pref.put(OUTPUT_DIRECTORY_DESC, outputDirectory);
    pref.put(OUTPUT_PREFIX_DESC, outputPrefix);

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

    try {
      pref.flush();
    } catch (BackingStoreException e) {
      Log.mandatory("Error unable to record preferences: " + e.getMessage());
    }
  }

  /**
   * Loads the tracking parameters from the Java Prefs
   */
  public void loadPreferences() {

    Preferences pref = Preferences.userRoot().node(getPreferencesName());

    try {
      pref.sync();
    } catch (BackingStoreException e) {
      Log.error("Error synchronizing preferences: " + e.getMessage());
    }

    Log.mandatory("Loading user preferences");
    // input options
    inputDirectory = pref.get(INPUT_DIRECTORY_DESC, inputDirectory);
    filenamePattern = pref.get(FILENAME_PATTERN_DESC, filenamePattern);
    // output options
    outputDirectory = pref.get(OUTPUT_DIRECTORY_DESC, outputDirectory);
    outputPrefix = pref.get(OUTPUT_PREFIX_DESC, outputPrefix);

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


    // push the updated parameters to the GUI
    pushParamsToGUI();

  }


  /**
   * Records the macro options into the Macro Recorder object
   */
  public void recordMacro() {
    Recorder.setCommand(Lineage_Mapper_Plugin.recorderCommand);

    // Input
    MacroUtils.recordString(INPUT_DIRECTORY_DESC + DS, inputDirectory);
    MacroUtils.recordString(FILENAME_PATTERN_DESC + DS, filenamePattern);
    // Output
    MacroUtils.recordString(OUTPUT_DIRECTORY_DESC + DS, outputDirectory);
    MacroUtils.recordString(OUTPUT_PREFIX_DESC + DS, outputPrefix);

    // Cost Function
    MacroUtils.recordDouble(WGT_CELL_OVERLAP_DESC + DS, weightCellOverlap);
    MacroUtils.recordDouble(WGT_CENTROIDS_DIST_DESC + DS, weightCentroids);
    MacroUtils.recordDouble(WGT_CELL_SIZE_DESC + DS, weightCellSize);
    MacroUtils.recordDouble(MAX_CENTROID_DIST_DESC + DS, maxCentroidsDist);

    // Confidence Index
    MacroUtils.recordInteger(MIN_CELL_LIFE_DESC + DS, minCellLife);
    MacroUtils.recordDouble(CELL_DEATH_CENT_DESC + DS, cellDeathDeltaThreshold);
    MacroUtils.recordBoolean(DENSITY_AFFECT_CI_DESC + DS, cellDensityAffectsCI);
    MacroUtils.recordBoolean(BORDER_AFFECT_CI_DESC + DS, borderCellAffectsCI);

    // Division
    MacroUtils.recordDouble(D_SIZE_SIM_DESC + DS, daughterSizeSimilarity);
    MacroUtils.recordDouble(MIN_DIV_OVERLAP_DESC + DS, divisionOverlapThreshold);
    MacroUtils.recordDouble(D_ASPECT_RATIO_SIM_DESC + DS, daughterAspectRatioSimilarity);
    MacroUtils.recordDouble(MOTHER_CIRC_IDX_DESC + DS, motherCircularityThreshold);
    MacroUtils.recordInteger(NUM_FRAMES_CIRC_CHECK_DESC + DS, numFramesToCheckCircularity);
    MacroUtils.recordBoolean(DIV_ENABLED_DESC + DS, enableCellDivision);

    // Fusion
    MacroUtils.recordInteger(MIN_CELL_AREA_DESC + DS, cellSizeThreshold);
    MacroUtils.recordDouble(MIN_FUSION_OVERLAP_DESC + DS, fusionOverlapThreshold);
    MacroUtils.recordBoolean(FUSION_ENABLED_DESC + DS, enableCellFusion);

    Recorder.saveCommand();
  }

  /**
   * Loads tracking parameters from the macro options specified.
   * @param macroOptions String containing the macro options passed to the LineageMapper
   */
  public void loadMacro(String macroOptions) {
    // input options
    inputDirectory = MacroUtils.loadMacroString(macroOptions, INPUT_DIRECTORY_DESC,
        inputDirectory);
    filenamePattern = MacroUtils.loadMacroString(macroOptions, FILENAME_PATTERN_DESC,
        filenamePattern);
    // output options
    outputDirectory = MacroUtils.loadMacroString(macroOptions, OUTPUT_DIRECTORY_DESC,
        outputDirectory);
    outputPrefix = MacroUtils.loadMacroString(macroOptions, OUTPUT_PREFIX_DESC,
        outputPrefix);

    // Cost Function
    weightCellOverlap = MacroUtils.loadMacroDouble(macroOptions, WGT_CELL_OVERLAP_DESC,
        weightCellOverlap);
    weightCentroids = MacroUtils.loadMacroDouble(macroOptions, WGT_CENTROIDS_DIST_DESC,
        weightCentroids);
    weightCellSize = MacroUtils.loadMacroDouble(macroOptions, WGT_CELL_SIZE_DESC, weightCellSize);
    maxCentroidsDist = MacroUtils.loadMacroDouble(macroOptions, MAX_CENTROID_DIST_DESC,
        maxCentroidsDist);

    // Confidence Index
    minCellLife = MacroUtils.loadMacroInteger(macroOptions, MIN_CELL_LIFE_DESC, minCellLife);
    cellDeathDeltaThreshold = MacroUtils.loadMacroDouble(macroOptions, CELL_DEATH_CENT_DESC, cellDeathDeltaThreshold);
    cellDensityAffectsCI = MacroUtils.loadMacroBoolean(macroOptions, DENSITY_AFFECT_CI_DESC,
        cellDensityAffectsCI);
    borderCellAffectsCI = MacroUtils.loadMacroBoolean(macroOptions, BORDER_AFFECT_CI_DESC,
        borderCellAffectsCI);

    // Division
    daughterSizeSimilarity = MacroUtils.loadMacroDouble(macroOptions, D_SIZE_SIM_DESC, daughterSizeSimilarity);
    divisionOverlapThreshold = MacroUtils.loadMacroDouble(macroOptions, MIN_DIV_OVERLAP_DESC, divisionOverlapThreshold);
    daughterAspectRatioSimilarity =
        MacroUtils.loadMacroDouble(macroOptions, D_ASPECT_RATIO_SIM_DESC, daughterAspectRatioSimilarity);
    motherCircularityThreshold = MacroUtils.loadMacroDouble(macroOptions, MOTHER_CIRC_IDX_DESC, motherCircularityThreshold);
    numFramesToCheckCircularity = MacroUtils.loadMacroInteger(macroOptions, NUM_FRAMES_CIRC_CHECK_DESC,
        numFramesToCheckCircularity);
    enableCellDivision = MacroUtils.loadMacroBoolean(macroOptions, DIV_ENABLED_DESC, enableCellDivision);

    // Fusion
    cellSizeThreshold = MacroUtils.loadMacroInteger(macroOptions, MIN_CELL_AREA_DESC, cellSizeThreshold);
    fusionOverlapThreshold = MacroUtils.loadMacroDouble(macroOptions, MIN_FUSION_OVERLAP_DESC, fusionOverlapThreshold);
    enableCellFusion = MacroUtils.loadMacroBoolean(macroOptions, FUSION_ENABLED_DESC, enableCellFusion);
  }


  public static void printParameterHelp() {
    String tab = "\t";
    System.out.println("-h, --help");
    System.out.println(tab + "display a help message and exit");

    System.out.println(TrackingAppParams.INPUT_DIRECTORY_DESC + " <value>");
    System.out.println(tab + "the input directory containing the image to track");
    System.out.println(TrackingAppParams.FILENAME_PATTERN_DESC + " <value>");
    System.out.println(tab + "the filename pattern controlling which images to select");

    System.out.println(TrackingAppParams.OUTPUT_DIRECTORY_DESC + " <value>");
    System.out.println(tab + "the output directory where outputs are saved");
    System.out.println(TrackingAppParams.OUTPUT_PREFIX_DESC + " <value>");
    System.out.println(tab + "the prefix prepended to all output files");

    System.out.println(TrackingAppParams.WGT_CELL_OVERLAP_DESC + " <value>");
    System.out.println(tab + "the tracking cost function cell overlap weight");
    System.out.println(TrackingAppParams.WGT_CENTROIDS_DIST_DESC + " <value>");
    System.out.println(tab + "the tracking cost function cell centroid distance weight");
    System.out.println(TrackingAppParams.WGT_CELL_SIZE_DESC + " <value>");
    System.out.println(tab + "the tracking cost function cell size weight");
    System.out.println(TrackingAppParams.MAX_CENTROID_DIST_DESC + " <value>");
    System.out.println(tab + "the maximum centroid distance between two objects that could " +
        "possibly be tracked together");

    System.out.println(TrackingAppParams.D_SIZE_SIM_DESC + " <value>");
    System.out.println(tab + "the daughter size similarity");
    System.out.println(TrackingAppParams.MIN_DIV_OVERLAP_DESC + " <value>");
    System.out.println(tab + "the minimum division overlap threshold");
    System.out.println(TrackingAppParams.D_ASPECT_RATIO_SIM_DESC + " <value>");
    System.out.println(tab + "the daughter aspect ratio similarity");
    System.out.println(TrackingAppParams.NUM_FRAMES_CIRC_CHECK_DESC + " <value>");
    System.out.println(tab + "the number of frames to check circularity for mitosis");
    System.out.println(TrackingAppParams.DIV_ENABLED_DESC + " <value>");
    System.out.println(tab + "is cell division enabled");

    System.out.println(TrackingAppParams.MIN_CELL_AREA_DESC + " <value>");
    System.out.println(tab + "the minimum cell area to be recognized as a cell");
    System.out.println(TrackingAppParams.MIN_FUSION_OVERLAP_DESC + " <value>");
    System.out.println(tab + "the minimum fusion overlap threshold");
    System.out.println(TrackingAppParams.FUSION_ENABLED_DESC + " <value>");
    System.out.println(tab + "is cell fusion enabled");

    System.out.println(TrackingAppParams.MIN_CELL_LIFE_DESC + " <value>");
    System.out.println(tab + "the minimum lifespan of a cell");
    System.out.println(TrackingAppParams.CELL_DEATH_CENT_DESC + " <value>");
    System.out.println(tab + "the maximum displacement allowed for a be considered dead");
    System.out.println(TrackingAppParams.DENSITY_AFFECT_CI_DESC + " <value>");
    System.out.println(tab + "does cell density affect the confidence index");
    System.out.println(TrackingAppParams.BORDER_AFFECT_CI_DESC + " <value>");
    System.out.println(tab + "do border cells affect the confidence index");
  }

}
