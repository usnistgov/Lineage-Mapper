// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package main.java.gov.nist.isg.lineage.mapper.app.gui.panels;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import main.java.gov.nist.isg.lineage.mapper.app.TrackingAppParams;
import main.java.gov.nist.isg.lineage.mapper.filechooser.DirectoryChooserPanel;
import main.java.gov.nist.isg.lineage.mapper.textfield.TextFieldInputPanel;
import main.java.gov.nist.isg.lineage.mapper.textfield.validator.Validator;
import main.java.gov.nist.isg.lineage.mapper.textfield.validator.ValidatorDbl;
import main.java.gov.nist.isg.lineage.mapper.textfield.validator.ValidatorInt;
import main.java.gov.nist.isg.lineage.mapper.textfield.validator.ValidatorPrefix;
import main.java.gov.nist.isg.lineage.mapper.textfield.validator.ValidatorRegex;


/**
 * Special JPanel to hold the general tracking parameters.
 */
public class OptionsPanel extends JPanel {

  /**
   * The filename index regex pattern
   */
  public static final String filenamePatternRegex = "(.*)(\\{[i]+\\})(.*)";

  /**
   * The pattern example
   */
  public static final String filenamePatternExample =
      "<html>Format example:<br> File name = img1234.tif"
          + "<br>Format = img{iiii}.tif"
          + "<br>{iiii} = index;</html>";


  private DirectoryChooserPanel inputDirectoryChooser;
  private TextFieldInputPanel<String> filenamePattern;
  private DirectoryChooserPanel outputDirectoryChooser;
  private TextFieldInputPanel<String> filePrefix;

  private Validator<String> filenamePatternValidator;

  private TextFieldInputPanel<Integer> minObjectSize;
  private TextFieldInputPanel<Double> maxCentroidDisplacement;
  private JCheckBox enableDivisionCheckbox;
  private JCheckBox enableFusionCheckbox;

  private TrackingAppParams params;

  /**
   * Special JPanel to hold the general tracking parameters.
   * @param params instance of TrackingAppParams to be updated by the options available in this
   *               panel.
   */
  public OptionsPanel(TrackingAppParams params) {
    super();

    this.params = params;

    initElements();
    init();

  }

  private void init() {

    JPanel content = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();

    c.anchor = GridBagConstraints.FIRST_LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridwidth = 1;

    // setup the input options panel
    JPanel inputPanel = new JPanel(new GridBagLayout());
    inputPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "Input Setup"));
    // setup the output options panel
    JPanel outputPanel = new JPanel(new GridBagLayout());
    outputPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "Output Setup"));

    // setup the basic tracking options panel
    JPanel trackingPanel = new JPanel(new GridBagLayout());
    trackingPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "Basic Tracking Parameters"));

    // add things to the inputs panel
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(0, 0, 0, 0);
    inputPanel.add(this.inputDirectoryChooser, c);
    c.gridy = 1;
    inputPanel.add(this.filenamePattern, c);

    // add things to the outputs panel
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(0, 0, 0, 0);
    outputPanel.add(this.outputDirectoryChooser, c);
    c.gridy = 1;
    outputPanel.add(this.filePrefix, c);

    // add things to the tracking options panel
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(0, 0, 0, 0);
    trackingPanel.add(minObjectSize, c);
    c.gridy = 1;
    trackingPanel.add(maxCentroidDisplacement, c);
    c.gridy = 2;
    trackingPanel.add(enableDivisionCheckbox, c);
    c.gridy = 3;
    trackingPanel.add(enableFusionCheckbox, c);


    // add the individual panels to the main panel
    c.insets = new Insets(0, 0, 0, 0);
    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.CENTER;
    c.fill = GridBagConstraints.HORIZONTAL;
    content.add(inputPanel, c);
    c.insets = new Insets(10, 0, 0, 0);
    c.gridy = 1;
    content.add(outputPanel, c);
    c.gridy = 2;
    content.add(trackingPanel, c);

    this.add(content);

  }

  private void initElements() {

    this.inputDirectoryChooser = new DirectoryChooserPanel("Input Directory");
    this.inputDirectoryChooser
        .setToolTipText("<html>The location of the images to be tracked.</html>");

    this.filenamePatternValidator = new ValidatorRegex(this.filenamePatternRegex, this.filenamePatternExample);
    this.filenamePattern =
        new TextFieldInputPanel<String>("Filename Pattern", "img_{iiii}.tif", 25, "", this.filenamePatternValidator);

    this.outputDirectoryChooser = new DirectoryChooserPanel("Output Directory");
    this.outputDirectoryChooser.setToolTipText("<html>The location to save outputs.</html>");

    this.filePrefix = new TextFieldInputPanel<String>("Prefix:", "trk-", 25, "", new ValidatorPrefix());
    this.filePrefix.setToolTipText("<html>The prefix to prepend to any output files generated..</html>");


    minObjectSize =
        new TextFieldInputPanel<Integer>("Minimum Object Size:", Integer.toString(params.getCellSizeThreshold()), "pixels",
            new ValidatorInt(0, Integer.MAX_VALUE));
    minObjectSize.setToolTipText("<html>Controls the minimum object size allowed to persist when a fusion event has been cut apart.</html>");


    maxCentroidDisplacement =
        new TextFieldInputPanel<Double>("Maximum Centroid Displacement:", Double.toString(params.getMaxCentroidsDist()), "pixels",
            new ValidatorDbl(0, Double.MAX_VALUE));
    maxCentroidDisplacement.setToolTipText("<html>The maximum distance in pixels used to consider which objects could be tracked together.</html>");


    enableDivisionCheckbox = new JCheckBox("Enable Division", params.isEnableCellDivision());
    enableDivisionCheckbox.setToolTipText("<html>Controls whether object division is allowed or prevented.</html>");
    enableDivisionCheckbox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        params.setEnableCellDivision(enableDivisionCheckbox.isSelected());
        params.getGuiPane().getAdvancedPanel().setDivisionEnabled(enableDivisionCheckbox.isSelected());
      }
    });
    enableDivisionCheckbox.setSelected(params.isEnableCellDivision());

    enableFusionCheckbox = new JCheckBox("Enable Fusion", params.isEnableCellFusion());
    enableFusionCheckbox.setToolTipText("<html>Controls whether object fusion is allowed or prevented.</html>");
    enableFusionCheckbox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        params.setEnableCellFusion(enableFusionCheckbox.isSelected());
        minObjectSize.setEnabled(!enableFusionCheckbox.isSelected());
      }
    });
    enableFusionCheckbox.setSelected(params.isEnableCellFusion());
  }


  /**
   * Check all sub-elements for errors
   * @return true if any contained element has an error
   */
  public boolean hasError() {
    return minObjectSize.hasError() || maxCentroidDisplacement.hasError();
  }

  /**
   * Get string detailing what errors exist within the panel
   * @return string representation of the errors present in the panel
   */
  public String getErrorString() {
    String ret = "";
    if (this.inputDirectoryChooser.hasError())
      ret += "Invalid Input Directory\n";
    if (this.filenamePattern.hasError())
      ret += "Invalid Filename Pattern: \"" + this.filenamePattern.getText() + "\"\n";
    if (this.outputDirectoryChooser.hasError())
      ret += "Invalid Output Directory\n";
    if (this.filePrefix.hasError())
      ret += "Invalid File Prefix: \"" + this.filePrefix.getText() + "\"\n";
    if (minObjectSize.hasError())
      ret += "Invalid Minimum Object Size: \"" + minObjectSize.getText() + "\"\n";
    if (maxCentroidDisplacement.hasError())
      ret += "Invalid Maximum Centroid Displacement: \"" + maxCentroidDisplacement.getText() + "\"\n";

    return ret;
  }


  /**
   * Push parameters from the instance of TrackingAppParams to their respective GUI elements
   */
  public void pushParamsToGUI() {
    setInputDirectory(params.getInputDirectory());
    setFilenamePattern(params.getFilenamePattern());
    setOutputDirectory(params.getOutputDirectory());
    setOutputPrefix(params.getOutputPrefix());
    setMinObjectSize(params.getCellSizeThreshold());
    setMaxCentroidDisplacement(params.getMaxCentroidsDist());
    setEnableDivision(params.isEnableCellDivision());
    setEnableFusion(params.isEnableCellFusion());
  }

  /**
   * Pull parameters from the GUI elements into their respective variables within the
   * TrackingAppParams instance.
   */
  public void pullParamsFromGUI() {
    params.setInputDirectory(getInputDirectory());
    params.setFilenamePattern(getFilenamePattern());
    params.setOutputDirectory(getOuputDirectory());
    params.setOutputPrefix(getFilePrefix());
    params.setCellSizeThreshold(getMinObjectSize());
    params.setMaxCentroidsDist(getMaxCentroidDisplacement());
    params.setEnableCellDivision(isEnableDivision());
    params.setEnableCellFusion(isEnableFusion());
  }


  public String getInputDirectory() {
    String val = inputDirectoryChooser.getValue();
    if (!val.endsWith(File.separator))
      val += File.separator;
    return val;
  }

  public String getFilenamePattern() {
    return filenamePattern.getValue();
  }

  public String getOuputDirectory() {
    String val = outputDirectoryChooser.getValue();
    if (!val.endsWith(File.separator))
      val += File.separator;
    return val;
  }

  public String getFilePrefix() {
    return filePrefix.getValue();
  }

  public void setInputDirectory(String val) {
    if (!val.endsWith(File.separator))
      val += File.separator;
    inputDirectoryChooser.setValue(val);
  }

  public void setFilenamePattern(String val) {
    filenamePattern.setValue(val);
  }


  public void setOutputDirectory(String val) {
    if (!val.endsWith(File.separator))
      val += File.separator;
    outputDirectoryChooser.setValue(val);
  }

  public void setOutputPrefix(String val) {
    filePrefix.setValue(val);
  }

  public int getMinObjectSize() {
    return minObjectSize.getValue();
  }

  public double getMaxCentroidDisplacement() {
    return maxCentroidDisplacement.getValue();
  }

  public boolean isEnableDivision() {
    return enableDivisionCheckbox.isSelected();
  }

  public boolean isEnableFusion() {
    return enableFusionCheckbox.isSelected();
  }

  public void setMinObjectSize(int val) {
    minObjectSize.setValue(val);
  }

  public void setMaxCentroidDisplacement(double val) {
    maxCentroidDisplacement.setValue(val);
  }

  public void setEnableDivision(boolean val) {
    enableDivisionCheckbox.setSelected(val);
  }

  public void setEnableFusion(boolean val) {
    enableFusionCheckbox.setSelected(val);
  }


}
