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

import java.util.List;

import ij.IJ;
import ij.ImageStack;
import main.java.gov.nist.isg.tracking.app.gui.CellTrackerGUI;
import main.java.gov.nist.isg.tracking.app.gui.panels.AdvancedPanel;
import main.java.gov.nist.isg.tracking.app.gui.panels.InputPanel;
import main.java.gov.nist.isg.tracking.app.gui.panels.OutputPanel;
import main.java.gov.nist.isg.tracking.lib.ImageFrame;
import main.java.gov.nist.isg.tracking.lib.Log;
import main.java.gov.nist.isg.tracking.metadata.BirthDeathMetadata;
import main.java.gov.nist.isg.tracking.metadata.ConfidenceIndexMetadata;
import main.java.gov.nist.isg.tracking.metadata.DivisionMetadata;
import main.java.gov.nist.isg.tracking.metadata.FusionMetadata;

public class AppParameters {



  private static final String APP_TITLE = "Lineage Mapper";
  private static final String PreferencesName = "lineage_mapper";

  //	Descriptions used in writing, loading, and saving main.java.gov.nist.isg.tracking.app parameters
  public static final String DS = ": "; // description separator
  public static final String WGT_CELL_OVERLAP_DESC = "Weight Cell Overlap";
  public static final String WGT_CENTROIDS_DIST_DESC = "Weight Centroids Distance";
  public static final String WGT_CELL_SIZE_DESC = "Weight Cell Size";
  public static final String MAX_CENTROID_DIST_DESC = "Max Centroids Distance";
  public static final String MIN_CELL_LIFE_DESC = "Min Cell Life";
  public static final String CELL_DEATH_CENT_DESC = "Cell Death Delta Centroid";
  public static final String DENSITY_AFFECT_CI_DESC = "Cell Density Affects CI";
  public static final String BORDER_AFFECT_CI_DESC = "Border Cell Affects CI";
  public static final String D_SIZE_SIM_DESC = "Daughter Size Similarity";
  public static final String MIN_DIV_OVERLAP_DESC = "Min Division Overlap";
  public static final String D_ASPECT_RATIO_SIM_DESC = "Daughter Aspect Ratio Similarity";
  public static final String MOTHER_CIRC_IDX_DESC = "Mother Circularity Index";
  public static final String NUM_FRAMES_CIRC_CHECK_DESC = "Num Frames To Check Circularity";
  public static final String DIV_ENABLED_DESC = "Enable Cell Division";
  public static final String MIN_CELL_AREA_DESC = "Min Cell Area";
  public static final String MIN_FUSION_OVERLAP_DESC = "Min Fusion Overlap";
  public static final String FUSION_ENABLED_DESC = "Enable Cell Fusion";

  public static final String SAVE_OUTPUTS_DESC = "Save Output Images";
  public static final String SAVE_OUTPUTS_STACK_DESC = "Save Output Images as Stack";
  public static final String OUTPUT_DIRECTORY_DESC = "Output Directory";
  public static final String OUTPUT_PREFIX_DESC = "Output Prefix";
  public static final String LABEL_OUTPUT_MASKS = "Label Output Masks";


  //	Cost Function
  protected double weightCellOverlap; // percentage
  protected double weightCentroids; // percentage
  protected double weightCellSize; // percentage
  protected double maxCentroidsDist;
  //	Confidence Index
  protected int minCellLife = 32;
  protected double cellDeathDeltaThreshold = 10;
  protected boolean cellDensityAffectsCI = true;
  protected boolean borderCellAffectsCI = true;
  //	Division
  protected double daughterSizeSimilarity = 0.5; // percentage
  protected double divisionOverlapThreshold = 0.2; // percentage
  protected double daughterAspectRatioSimilarity = 0.7; // percentage
  protected double motherCircularityThreshold = 0.3; // percentage
  protected int numFramesToCheckCircularity = 5;
  protected boolean enableCellDivision = true;
  //	Fusion
  protected int cellSizeThreshold = 100; // aka min cell area
  protected double fusionOverlapThreshold = 0.2; // percentage
  protected boolean enableCellFusion = false;
  // Output
  protected ImageStack inputStack;
  protected ImageStack outputStack;
  protected String outputDirectory = "";
  protected String prefix = "";
  protected boolean saveOutputsEnabled = true;
  protected boolean labelOutputMasksEnabled = false;
  protected boolean saveAsStackEnabled = true;

  protected CellTrackerGUI ctGUI = null;
  protected boolean tracking = false;
  protected Thread trackingThread = null;

  // main.java.gov.nist.isg.tracking.metadata
  protected BirthDeathMetadata birthDeathMetadata = null;
  protected DivisionMetadata divisionMetadata = null;
  protected FusionMetadata fusionMetadata = null;
  protected ConfidenceIndexMetadata confidenceIndexMetadata = null;
  protected List<ImageFrame> framesList = null;


  public boolean hasError() {
    if(ctGUI == null)
      return true;

    return ctGUI.getInputPanel().hasError() || ctGUI.getOutputPanel().hasError() || ctGUI.getAdvancedPanel().hasError();
  }

  public void validateParameters() throws IllegalArgumentException {
    String errors = getErrorString();

    if (!errors.isEmpty()) {
      Log.setLogLevel(Log.LogType.MANDATORY);
      Log.error("Invalid Parameter(s):");
      Log.error(errors);
      IJ.error("Invalid Parameter Found: See Log for Details");

      throw new IllegalArgumentException("Invalid Parameters found");
    }
  }

  public String getErrorString() {
    return ctGUI.getInputPanel().getErrorString() + ctGUI.getOutputPanel().getErrorString() + ctGUI.getAdvancedPanel().getErrorString();
  }

  public void resetToDefaultParams() {
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
    // Output
    saveOutputsEnabled = false;
    labelOutputMasksEnabled = false;
    saveAsStackEnabled = true;
    outputDirectory = System.getProperty("user.home");

    birthDeathMetadata = null;
    divisionMetadata = null;
    fusionMetadata = null;
    confidenceIndexMetadata = null;
  }

  public boolean pullParamsFromGui() {
    validateParameters();

    InputPanel inputPanel = ctGUI.getInputPanel();
    OutputPanel outputPanel = ctGUI.getOutputPanel();
    AdvancedPanel advancedPanel = ctGUI.getAdvancedPanel();

    weightCellOverlap = advancedPanel.getWeightCellOverlap();
    weightCentroids = advancedPanel.getWeightCellCentroidDistance();
    weightCellSize = advancedPanel.getWeightCellSize();
    maxCentroidsDist = inputPanel.getMaxCentroidDisplacement();

    minCellLife = advancedPanel.getMinCellLifespan();
    cellDeathDeltaThreshold = advancedPanel.getCellDeathDeltaCentroidThreshold();
    cellDensityAffectsCI = advancedPanel.isCellDensityAffectCI();
    borderCellAffectsCI = advancedPanel.isBorderCellAffectCI();

    daughterSizeSimilarity = advancedPanel.getDaughterSizeSimilarity();
    divisionOverlapThreshold = advancedPanel.getMinDivisionOverlap();
    daughterAspectRatioSimilarity = advancedPanel.getDaughterAspectRatioSimilarity();
    motherCircularityThreshold = advancedPanel.getMotherCellCircularityIndex();
    numFramesToCheckCircularity = advancedPanel.getNumberFramesCheckCircularity();
    enableCellDivision = inputPanel.isEnableDivision();

    cellSizeThreshold = inputPanel.getMinObjectSize();
    fusionOverlapThreshold = advancedPanel.getMinFusionOverlap();
    enableCellFusion = inputPanel.isEnableFusion();

    outputDirectory = outputPanel.getOutputDiectory();
    prefix = outputPanel.getPrefix();
    saveOutputsEnabled = outputPanel.isSaveOutputsEnabled();
    labelOutputMasksEnabled = outputPanel.isLabelOutputImagesEnabled();
    saveAsStackEnabled = outputPanel.isSaveOutputImagesAsStackEnabled();
    return true;
  }

  public void pushParamsToGUI() {

    InputPanel inputPanel = ctGUI.getInputPanel();
    OutputPanel outputPanel = ctGUI.getOutputPanel();
    AdvancedPanel advancedPanel = ctGUI.getAdvancedPanel();

    advancedPanel.setWeightCellOverlap(weightCellOverlap);
    advancedPanel.setWeightCellCentroidsDistance(weightCentroids);
    advancedPanel.setWeightCellSize(weightCellSize);
    inputPanel.setMaxCentroidDisplacement(maxCentroidsDist);

    advancedPanel.setMinCellLifespan(minCellLife);
    advancedPanel.setCellDeathDeltaCentroidThreshold(cellDeathDeltaThreshold);
    advancedPanel.setCellDensityAffetsCI(cellDensityAffectsCI);
    advancedPanel.setBorderCellAffectsCI(borderCellAffectsCI);

    advancedPanel.setDaughterSizeSimilarity(daughterSizeSimilarity);
    advancedPanel.setMinDivisionOverlap(divisionOverlapThreshold);
    advancedPanel.setDaughterAspectRatioSimilarity(daughterAspectRatioSimilarity);
    advancedPanel.setMotherCellCircuralrityIndex(motherCircularityThreshold);
    advancedPanel.setNumberFramesCheckCircularity(numFramesToCheckCircularity);
    inputPanel.setEnableDivision(enableCellDivision);

    inputPanel.setMinObjectSize(cellSizeThreshold);
    advancedPanel.setMinFusionOverlap(fusionOverlapThreshold);
    inputPanel.setEnableFusion(enableCellFusion);

    outputPanel.setOutputDirectory(outputDirectory);
    outputPanel.setPrefix(prefix);
    outputPanel.setLabelOutputImagesEnabled(labelOutputMasksEnabled);
    outputPanel.setSaveOutputImagesAsStack(saveAsStackEnabled);
    outputPanel.setSaveOutputsEnabled(saveOutputsEnabled);
    outputPanel.updateCheckboxDependencies();

    validateParameters();
  }

  public void setProgressBar(double val) {
    if(ctGUI != null) {
      ctGUI.setProgressBar(val);
    }
  }

  public boolean isTracking() { return tracking; }
  public void setIsTracking(boolean val) { tracking = val; }
  public Thread getTrackingThread() { return trackingThread; }
  public void setTrackingThread(Thread t) { trackingThread = t; }

  public void setSaveOutputsEnabled(boolean val) {
    saveOutputsEnabled = val;
  }
  public boolean isSaveOutputsEnabled() { return saveOutputsEnabled; }
  public boolean isLabelOutputMasksEnabled() { return labelOutputMasksEnabled; }
  public void setLabelOutputMasksEnabled(boolean val) { labelOutputMasksEnabled = val; }

  public void setOutputDirectory(String val) {
    outputDirectory = val;
  }
  public String getOutputDirectory() { return outputDirectory; }
  public void setPrefix(String val) {
    prefix = val;
  }
  public String getPrefix() { return prefix; }
  public boolean isSaveAsStackEnabled() { return this.saveAsStackEnabled; }
  public void setSaveAsStackEnabled(boolean val) { this.saveAsStackEnabled = val; }


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

  public void setGuiPane(CellTrackerGUI gui) {
    this.ctGUI = gui;
  }
  public CellTrackerGUI getGuiPane() { return this.ctGUI; }

  public static String getAppTitle() {
    return APP_TITLE;
  }

  public static String getPreferencesName() { return PreferencesName; }



  public ImageStack getInputStack() {
    return this.inputStack;
  }

  public void setInputStack(ImageStack inputStack) {
    this.inputStack = inputStack;
  }

  public ImageStack getOutputStack() {
    return this.outputStack;
  }

  public void setOutputStack(ImageStack outputStack) {
    this.outputStack = outputStack;
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


}
