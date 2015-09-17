package main.java.gov.nist.isg.tracking.app.gui.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import main.java.gov.nist.isg.tracking.app.TrackingAppParams;
import main.java.gov.nist.isg.tracking.filechooser.DirectoryChooserPanel;
import main.java.gov.nist.isg.tracking.metadata.BirthDeathMetadata;
import main.java.gov.nist.isg.tracking.metadata.ConfidenceIndexMetadata;
import main.java.gov.nist.isg.tracking.metadata.DivisionMetadata;
import main.java.gov.nist.isg.tracking.metadata.FusionMetadata;
import main.java.gov.nist.isg.tracking.metadata.LineageMapper;
import main.java.gov.nist.isg.tracking.textfield.TextFieldInputPanel;
import main.java.gov.nist.isg.tracking.textfield.validator.ValidatorPrefix;

/**
 * Created by mmajurski on 5/19/14.
 */
public class OutputPanel extends JPanel {

  private JCheckBox saveOutputsCheckbox;
  private DirectoryChooserPanel outputDirectory;
  private TextFieldInputPanel<String> prefix;

  private JCheckBox labelOutputImagesCheckbox;
  private JCheckBox saveOutputImagesAsStack;

  private JButton showBirthDeathButton;
  private JButton showDivisionButton;
  private JButton showFusionButton;
  private JButton showConfidenceIndexButton;
  private JButton showLinageMapperButton;


  public OutputPanel() {
    super();

    initElements();
    initListeners();
    init();

  }

  private void init() {

    JPanel content = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    c.anchor = GridBagConstraints.FIRST_LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(2,2,2,2);

    c.gridx = 0;
    c.gridy = 0;
    content.add(saveOutputsCheckbox, c);
    c.gridy = 1;
    content.add(outputDirectory, c);

    c.gridy = 2;
    content.add(prefix, c);

    c.gridy = 3;
    content.add(saveOutputImagesAsStack, c);

    c.gridy = 4;
    content.add(labelOutputImagesCheckbox, c);


    c.anchor = GridBagConstraints.CENTER;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(20,2,2,2);
    c.gridy = 5;
    content.add(showBirthDeathButton, c);
    c.insets = new Insets(2,2,2,2);

    c.gridy = 6;
    content.add(showDivisionButton, c);

    c.gridy = 7;
    content.add(showFusionButton, c);

    c.gridy = 8;
    content.add(showConfidenceIndexButton, c);

    c.gridy = 9;
    content.add(showLinageMapperButton, c);

    this.add(content);

  }

  private void initElements() {

    outputDirectory = new DirectoryChooserPanel("Directory:", "", 20);
    outputDirectory.setToolTipText("<html>The location to save outputs.</html>");
    outputDirectory.setEnabled(TrackingAppParams.getInstance().isSaveOutputsEnabled());

    prefix = new TextFieldInputPanel<String>("Prefix:", "", "", new ValidatorPrefix());
    prefix.setToolTipText("<html>The prefix to prepend to any output files generated..</html>");
    prefix.setEnabled(TrackingAppParams.getInstance().isSaveOutputsEnabled());

    saveOutputsCheckbox = new JCheckBox("Save Outputs to Disk", TrackingAppParams.getInstance().isSaveOutputsEnabled());
    saveOutputsCheckbox.setToolTipText("<html>Controls whether the resulting outputs are saved to disk.</html>");
    saveOutputsCheckbox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TrackingAppParams.getInstance()
            .setSaveOutputsEnabled(saveOutputsCheckbox.isSelected());
        prefix.setEnabled(saveOutputsCheckbox.isSelected());
        outputDirectory.setEnabled(saveOutputsCheckbox.isSelected());
        saveOutputImagesAsStack.setEnabled(saveOutputsCheckbox.isSelected());
      }
    });

    labelOutputImagesCheckbox = new JCheckBox("Overlay Object Labels onto Output Images", TrackingAppParams.getInstance().isLabelOutputMasksEnabled());
    labelOutputImagesCheckbox.setToolTipText("<html>Controls whether the output binary labeled masks have the object label numbers displayed using ROI tags.</html>");
    labelOutputImagesCheckbox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TrackingAppParams.getInstance().setLabelOutputMasksEnabled(labelOutputImagesCheckbox.isSelected());
      }
    });


    saveOutputImagesAsStack =  new JCheckBox("Save Output Images in Single Tiff Stack", TrackingAppParams.getInstance().isSaveAsStackEnabled());
    saveOutputImagesAsStack.setToolTipText("<html>Controls whether the output images are saved into a single tiff file as an image stack, or individual tiff files.</html>");
    saveOutputImagesAsStack.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TrackingAppParams.getInstance().setSaveAsStackEnabled(saveOutputImagesAsStack.isSelected());
      }
    });
    saveOutputImagesAsStack.setEnabled(TrackingAppParams.getInstance().isSaveOutputsEnabled());

    showBirthDeathButton = new JButton("Show Birth/Death");
    showDivisionButton = new JButton("Show Division");
    showFusionButton = new JButton("Show Fusion");
    showConfidenceIndexButton = new JButton("Show Confidence Index");
    showLinageMapperButton = new JButton("Show Lineage Mapper");

  }

  public boolean hasError() {
    return outputDirectory.hasError() || prefix.hasError();
  }

  public String getErrorString() {
    String str = "";

    if(outputDirectory.hasError())
      str += "Invalid Output Directory: \"" + outputDirectory.getValue() + "\"\n";
    if(prefix.hasError())
      str += "Invalid Output Prefix: \"" + prefix.getText() + "\"\n";

    return str;
  }


  public String getOutputDiectory() { return outputDirectory.getValue(); }
  public String getPrefix() { return prefix.getValue(); }
  public void setOutputDirectory(String val) {
    outputDirectory.setValue(val);
  }
  public void setPrefix(String val) {
    prefix.setValue(val);
  }
  public boolean isSaveOutputsEnabled() { return saveOutputsCheckbox.isSelected(); }
  public void setSaveOutputsEnabled(boolean val) {
    saveOutputsCheckbox.setSelected(val);
  }
  public boolean isLabelOutputImagesEnabled() { return labelOutputImagesCheckbox.isSelected(); }
  public void setLabelOutputImagesEnabled(boolean val) { labelOutputImagesCheckbox.setSelected(val); }
  public boolean isSaveOutputImagesAsStackEnabled() { return saveOutputImagesAsStack.isSelected(); }
  public void setSaveOutputImagesAsStack(boolean val) { saveOutputImagesAsStack.setSelected(val); }

  public void updateCheckboxDependencies() {
    boolean val = TrackingAppParams.getInstance().isSaveOutputsEnabled();

    saveOutputsCheckbox.setSelected(val);
    prefix.setEnabled(saveOutputsCheckbox.isSelected());
    outputDirectory.setEnabled(saveOutputsCheckbox.isSelected());
    saveOutputImagesAsStack.setEnabled(saveOutputsCheckbox.isSelected());
  }


  private void initListeners() {
    BirthDeathMetadata bdm = new BirthDeathMetadata();
    DivisionMetadata dm = new DivisionMetadata();
    FusionMetadata fm = new FusionMetadata();
    ConfidenceIndexMetadata cim = new ConfidenceIndexMetadata();
    LineageMapper lm = new LineageMapper();

    TrackingAppParams params = TrackingAppParams.getInstance();
    params.setBirthDeathMetadata(bdm);
    params.setDivisionMetadata(dm);
    params.setFusionMetadata(fm);
    params.setConfidenceIndexMetadata(cim);


    showBirthDeathButton.addActionListener(bdm);
    showDivisionButton.addActionListener(dm);
    showFusionButton.addActionListener(fm);
    showConfidenceIndexButton.addActionListener(cim);
    showLinageMapperButton.addActionListener(lm);

  }
}
