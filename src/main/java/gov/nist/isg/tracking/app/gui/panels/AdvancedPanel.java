package main.java.gov.nist.isg.tracking.app.gui.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import main.java.gov.nist.isg.tracking.app.TrackingAppParams;
import main.java.gov.nist.isg.tracking.textfield.TextFieldInputPanel;
import main.java.gov.nist.isg.tracking.textfield.validator.ValidatorDbl;
import main.java.gov.nist.isg.tracking.textfield.validator.ValidatorInt;

/**
 * Created by mmajurski on 5/19/14.
 */
public class AdvancedPanel extends JPanel {

  // Cost parameters
  private JLabel costLabel;
  private TextFieldInputPanel<Double> wightCellOverlap;
  private TextFieldInputPanel<Double> weightCellCentroidDistance;
  private TextFieldInputPanel<Double> weightCellSize;

  // Division parameters
  private JLabel divisionLabel;
  private TextFieldInputPanel<Double> minDivisionOverlap;
  private TextFieldInputPanel<Double> daughterSizeSimilarity;
  private TextFieldInputPanel<Double> daughterAspectRatioSimilarity;
  private TextFieldInputPanel<Double> motherCircularityIndex;
  private TextFieldInputPanel<Integer> numberFramesCheckCircularity;

  // Fusion parameters
  private JLabel fusionLabel;
  private TextFieldInputPanel<Double> minFusionOverlap;

  // Confidence Index parameters
  private JLabel ciLabel;
  private TextFieldInputPanel<Integer> minCellLifespan;
  private TextFieldInputPanel<Double> cellDeathDeltaCentroidThreshold;
  private JCheckBox cellDensityAffetsCI;
  private JCheckBox borderCellAffectsCI;
  private JButton setDefaultParamsButton;



  public AdvancedPanel() {
    super();

    initElements();
    init();
  }

  private void init() {

    JPanel content = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();

    c.anchor = GridBagConstraints.LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0,0,0,0);
    c.gridx = 0;


    c.gridy = 0;
    content.add(costLabel, c);
    c.gridy = 1;
    content.add(wightCellOverlap, c);
    c.gridy = 2;
    content.add(weightCellCentroidDistance, c);
    c.gridy = 3;
    content.add(weightCellSize, c);


    c.gridy = 4;
    c.insets = new Insets(20,0,0,0);
    content.add(divisionLabel, c);
    c.insets = new Insets(0,0,0,0);
    c.gridy = 5;
    content.add(minDivisionOverlap, c);
    c.gridy = 6;
    content.add(daughterSizeSimilarity, c);
    c.gridy = 7;
    content.add(daughterAspectRatioSimilarity, c);
    c.gridy = 8;
    content.add(motherCircularityIndex, c);
    c.gridy = 9;
    content.add(numberFramesCheckCircularity, c);


    c.gridy = 10;
    c.insets = new Insets(20,0,0,0);
    content.add(fusionLabel, c);
    c.insets = new Insets(0,0,0,0);

    c.gridy = 11;
    content.add(minFusionOverlap, c);

    c.gridy = 12;
    c.insets = new Insets(20,0,0,0);
    content.add(ciLabel, c);
    c.insets = new Insets(0,0,0,0);
    c.gridy = 13;
    content.add(minCellLifespan, c);
    c.gridy = 14;
    content.add(cellDeathDeltaCentroidThreshold, c);
    c.gridy = 15;
    content.add(cellDensityAffetsCI, c);
    c.gridy = 16;
    content.add(borderCellAffectsCI, c);

    c.gridy = 17;
    c.insets = new Insets(20,0,0,0);
    content.add(setDefaultParamsButton, c);

    this.add(content);

  }


  private void initElements() {

    TrackingAppParams params = TrackingAppParams.getInstance();
    // cost parameters
    costLabel = new JLabel("Cost Parameters");
    wightCellOverlap = new TextFieldInputPanel<Double>("Weight Cell Overlap:", Double.toString(100*params.getWeightCellOverlap()), "%",
                                                        new ValidatorDbl(0.0, 100.0));
    wightCellOverlap.setToolTipText("<html>Weight of the cell overlap term in the tracking cost function.</html>");

    weightCellCentroidDistance = new TextFieldInputPanel<Double>("Weight Cell Centroid Distance:", Double.toString(100*params.getWeightCentroids()), "%",
                                                       new ValidatorDbl(0.0, 100.0));
    weightCellCentroidDistance.setToolTipText("<html>Weight of the cell centroid distance term in the tracking cost function.</html>");

    weightCellSize = new TextFieldInputPanel<Double>("Weight Cell Size:", Double.toString(100*params.getWeightCellSize()), "%",
                                                       new ValidatorDbl(0.0, 100.0));
    weightCellSize.setToolTipText("<html>Weight of the cell size term in the tracking cost function.</html>");


    // division parameters
    divisionLabel = new JLabel("Division Parameters");
    minDivisionOverlap = new TextFieldInputPanel<Double>("Minimum Division Overlap:", Double.toString(100*params.getDivisionOverlapThreshold()), "%",
                                                     new ValidatorDbl(0.0, 100.0));
    minDivisionOverlap.setToolTipText("<html>Minimum percentage overlap between cells in consecutive frames to be considered a mitotic event.<br>If this parameter is set to 100% no cases will be considered for mitosis.</html>");

    daughterSizeSimilarity = new TextFieldInputPanel<Double>("Daughter Size Similarity:", Double.toString(100*params.getDaughterSizeSimilarity()), "%",
                                                         new ValidatorDbl(0.0, 100.0));
    daughterSizeSimilarity.setToolTipText("<html>This parameter is a measure of the size similarity between daughter cells.<br>In a real mitosis the size of the daughters should be very similar.</html>");

    daughterAspectRatioSimilarity = new TextFieldInputPanel<Double>("Daughter Aspect Ratio Similarity:", Double.toString(100*params.getDaughterAspectRatioSimilarity()), "%",
                                                         new ValidatorDbl(0.0, 100.0));
    daughterAspectRatioSimilarity.setToolTipText("<html>This parameter is a measure of the aspect ratio similarity between daughter cells.</html>");

    motherCircularityIndex = new TextFieldInputPanel<Double>("Mother Circularity Threshold:", Double.toString(100*params.getMotherCircularityThreshold()), "%",
                                                         new ValidatorDbl(0.0, 100.0));
    motherCircularityIndex.setToolTipText("<html>For a cell to be considers a mother cell in a mitotic event it must be round within <i>n</i> frames of the mitotic detection.<br>This threshold controls what is round enough to be considered a mitotic event.</html>");

    numberFramesCheckCircularity = new TextFieldInputPanel<Integer>("Number Frames to Check Circularity:", Integer.toString(params.getNumFramesToCheckCircularity()), "frames",
                                                                   new ValidatorInt(0, Integer.MAX_VALUE));
    numberFramesCheckCircularity.setToolTipText("<html>For a cell to be considers a mother cell in a mitotic event it must be round within this many frames of the mitotic detection.</html>");


    // fusion parameters
    fusionLabel = new JLabel("Fusion Parameters");
    minFusionOverlap = new TextFieldInputPanel<Double>("Minimum Fusion Overlap:", Double.toString(100*params.getFusionOverlapThreshold()), "%",
                                                       new ValidatorDbl(0.1, 100.0));
    minFusionOverlap.setToolTipText("<html>Minimum percentage overlap between cells in consecutive frames to be considered a fusion event.</html>");

    // confidence index parameters
    ciLabel = new JLabel("Confidence Index Parameters");
    minCellLifespan = new TextFieldInputPanel<Integer>("Minimum Cell Lifespan:", Integer.toString(params.getMinCellLife()), "frames",
                                                       new ValidatorInt(0, Integer.MAX_VALUE));
    minCellLifespan.setToolTipText("<html>Minimum number of frames a cell must be present to increase its confidence by a point.</html>");
    cellDeathDeltaCentroidThreshold = new TextFieldInputPanel<Double>("Cell Death Delta Centroid Threshold:", Double.toString(params.getCellDeathDeltaThreshold()), "pixels",
                                                                      new ValidatorDbl(0.0, Double.MAX_VALUE));
    cellDeathDeltaCentroidThreshold.setToolTipText("<html>If a cell does not move more than this value (in pixels) the cell will be recorded as dead.</html>");

    cellDensityAffetsCI = new JCheckBox("Cell Density Affects Confidence Index", params.isCellDensityAffectsCI());
    cellDensityAffetsCI.setToolTipText("<html>If enabled, a lower confidence index will be assigned to cells that have grouped into colonies, or become close to other cells.</html>");
    borderCellAffectsCI = new JCheckBox("Border Cell Affects Confidence Index", params.isBorderCellAffectsCI());
    borderCellAffectsCI.setToolTipText("<html>If enabled, only cells that never touch the FOV boundaries will increase the confidence index by a point.</html>");

    setDefaultParamsButton = new JButton("Load Default Parameters");
    setDefaultParamsButton.setToolTipText("Loads the default parameters into the gui.");
    setDefaultParamsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TrackingAppParams params = TrackingAppParams.getInstance();
        params.resetToDefaultParams();
        params.pushParamsToGUI();
      }
    });

  }

  // Getters
  public double getWeightCellOverlap() { return wightCellOverlap.getValue()/100; }
  public double getWeightCellSize() { return weightCellSize.getValue()/100; }
  public double getWeightCellCentroidDistance() { return weightCellCentroidDistance.getValue()/100; }
  public double getMinDivisionOverlap() { return minDivisionOverlap.getValue()/100; }
  public double getDaughterSizeSimilarity() { return daughterSizeSimilarity.getValue()/100; }
  public double getDaughterAspectRatioSimilarity() { return daughterAspectRatioSimilarity.getValue()/100; }
  public double getMotherCellCircularityIndex() { return motherCircularityIndex.getValue()/100; }
  public int getNumberFramesCheckCircularity() { return numberFramesCheckCircularity.getValue(); }
  public double getMinFusionOverlap() { return minFusionOverlap.getValue()/100; }
  public int getMinCellLifespan() { return minCellLifespan.getValue(); }
  public double getCellDeathDeltaCentroidThreshold() { return cellDeathDeltaCentroidThreshold.getValue(); }
  public boolean isCellDensityAffectCI() { return cellDensityAffetsCI.isSelected(); }
  public boolean isBorderCellAffectCI() { return borderCellAffectsCI.isSelected(); }

  // Setters
  public void setWeightCellOverlap(double val) {
    wightCellOverlap.setValue(val*100);
  }
  public void setWeightCellSize(double val) {
    weightCellSize.setValue(val*100);
  }
  public void setWeightCellCentroidsDistance(double val) {
    weightCellCentroidDistance.setValue(val*100);
  }
  public void setMinDivisionOverlap(double val) {
    minDivisionOverlap.setValue(val*100);
  }
  public void setDaughterSizeSimilarity(double val) {
    daughterSizeSimilarity.setValue(val*100);
  }
  public void setDaughterAspectRatioSimilarity(double val) {
    daughterAspectRatioSimilarity.setValue(val*100);
  }
  public void setMotherCellCircuralrityIndex(double val) {
    motherCircularityIndex.setValue(val*100);
  }
  public void setNumberFramesCheckCircularity(int val) {
    numberFramesCheckCircularity.setValue(val);
  }
  public void setMinFusionOverlap(double val) {
    minFusionOverlap.setValue(val*100);
  }
  public void setMinCellLifespan(int val) {
    minCellLifespan.setValue(val);
  }
  public void setCellDeathDeltaCentroidThreshold(double val) {
    cellDeathDeltaCentroidThreshold.setValue(val);
  }
  public void setCellDensityAffetsCI(boolean val) {
    cellDensityAffetsCI.setSelected(val);
  }
  public void setBorderCellAffectsCI(boolean val) {
    borderCellAffectsCI.setSelected(val);
  }


  /**
   * Check all sub-elements for errors
   * @return true if any contained element has an error
   */
  public boolean hasError() {
    return wightCellOverlap.hasError() || weightCellCentroidDistance.hasError() || weightCellSize.hasError() ||
           minDivisionOverlap.hasError() || daughterSizeSimilarity.hasError() || daughterAspectRatioSimilarity.hasError() ||
           motherCircularityIndex.hasError() || numberFramesCheckCircularity.hasError() ||
           minFusionOverlap.hasError() || minCellLifespan.hasError() || cellDeathDeltaCentroidThreshold.hasError();
  }


  /**
   * Get string detailing what errors exist within the panel
   * @return string representation of the errors present in the panel
   */
  public String getErrorString() {
    String ret = "";

    if (wightCellOverlap.hasError()) {
      ret += "Invalid Weight Cell Overlap: \"" + wightCellOverlap.getText() + "\"\n";
    }

    if (weightCellCentroidDistance.hasError()) {
      ret += "Invalid Weight Cell Centroid Distance: \"" + weightCellCentroidDistance.getText() + "\"\n";
    }

    if (weightCellSize.hasError()) {
      ret += "Invalid Weight Cell Size: \"" + weightCellSize.getText() + "\"\n";
    }

    if (minDivisionOverlap.hasError()) {
      ret += "Invalid Minimum Division Overlap: \"" + minDivisionOverlap.getText() + "\"\n";
    }

    if (daughterSizeSimilarity.hasError()) {
      ret += "Invalid Daughter Size Similarity: \"" + daughterSizeSimilarity.getText() + "\"\n";
    }

    if (daughterAspectRatioSimilarity.hasError()) {
      ret += "Invalid Daughter Aspect Ratio Similarity: \"" + daughterAspectRatioSimilarity.getText() + "\"\n";
    }

    if (motherCircularityIndex.hasError()) {
      ret += "Invalid Mother Cell Circularity Threshold: \"" + motherCircularityIndex.getText() + "\"\n";
    }

    if (numberFramesCheckCircularity.hasError()) {
      ret += "Invalid Number Frames to Check Circularity: \"" + numberFramesCheckCircularity.getText() + "\"\n";
    }

    if (minFusionOverlap.hasError()) {
      ret += "Invalid Minimum Fusion Overlap: \"" + minFusionOverlap.getText() + "\"\n";
    }

    if (minCellLifespan.hasError()) {
      ret += "Invalid Minimum Cell Lifespan: \"" + minCellLifespan.getText() + "\"\n";
    }

    if (cellDeathDeltaCentroidThreshold.hasError()) {
      ret += "Invalid Cell Death Delta Centroid Threshold: \"" + cellDeathDeltaCentroidThreshold.getText() + "\"\n";
    }

    return ret;
  }

  /**
   * Set the division options within this panel to enabled or disabled
   * @param enabled whether to enable or disable the division options
   */
  protected void setDivisionEnabled(boolean enabled) {
//    minDivisionOverlap.setEnabled(enabled);
    daughterSizeSimilarity.setEnabled(enabled);
    daughterAspectRatioSimilarity.setEnabled(enabled);
    motherCircularityIndex.setEnabled(enabled);
    numberFramesCheckCircularity.setEnabled(enabled);
  }


}
