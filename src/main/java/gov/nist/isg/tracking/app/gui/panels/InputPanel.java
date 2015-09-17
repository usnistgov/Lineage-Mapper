package main.java.gov.nist.isg.tracking.app.gui.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import main.java.gov.nist.isg.tracking.app.gui.ImageWindowComboBox;
import main.java.gov.nist.isg.tracking.app.TrackingAppParams;
import main.java.gov.nist.isg.tracking.textfield.TextFieldInputPanel;
import main.java.gov.nist.isg.tracking.textfield.validator.ValidatorDbl;
import main.java.gov.nist.isg.tracking.textfield.validator.ValidatorInt;

/**
 * Created by mmajurski on 5/19/14.
 */
public class InputPanel extends JPanel {

  private ImageWindowComboBox imageWindowComboBox;
  private JButton refreshButton;
  private TextFieldInputPanel<Integer> minObjectSize;
  private TextFieldInputPanel<Double> maxCentroidDisplacement;
  private JCheckBox enableDivisionCheckbox;
  private JCheckBox enableFusionCheckbox;

  public InputPanel() {
    super();

    initElements();
    init();

  }

  private void init() {

    JPanel content = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();

    c.anchor = GridBagConstraints.FIRST_LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridwidth = 1;

    JPanel imgSequence = new JPanel();

    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(0,0,0,0);
    imgSequence.add(new JLabel("Image Sequence to Track: "), c);

    c.gridx = 1;
    imgSequence.add(imageWindowComboBox, c);
    c.gridx = 2;
    c.fill = GridBagConstraints.NONE;
    imgSequence.add(refreshButton, c);

    c.insets = new Insets(10,10,20,10);
    c.gridx = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    content.add(imgSequence, c);

    c.insets = new Insets(0,0,0,0);

    c.gridx = 0;
    c.gridy = 1;
    content.add(minObjectSize, c);

    c.gridy = 2;
    content.add(maxCentroidDisplacement, c);

    c.gridy = 3;
    content.add(enableDivisionCheckbox, c);

    c.gridy = 4;
    content.add(enableFusionCheckbox, c);

    this.add(content);

  }

  private void initElements() {


    imageWindowComboBox = new ImageWindowComboBox();
    imageWindowComboBox.setToolTipText("<html>Controls the selection of which open image stack to apply the tracker on.</html>");
    imageWindowComboBox.setSelectedIndex(0);

    refreshButton = new JButton("Refresh");
    refreshButton.setToolTipText("Refresh the list of open Images Stacks.");
    refreshButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        imageWindowComboBox.refresh();
      }
    });



    minObjectSize =
        new TextFieldInputPanel<Integer>("Minimum Object Size:", Integer.toString(TrackingAppParams.getInstance().getCellSizeThreshold()), "pixels",
                                         new ValidatorInt(0, Integer.MAX_VALUE));
    minObjectSize.setToolTipText("<html>Controls the minimum object size allowed to persist when a fusion event has been cut apart.</html>");


    maxCentroidDisplacement =
        new TextFieldInputPanel<Double>("Maximum Centroid Displacement:", Double.toString(TrackingAppParams.getInstance().getMaxCentroidsDist()), "pixels",
                                         new ValidatorDbl(0, Double.MAX_VALUE));
    maxCentroidDisplacement.setToolTipText("<html>The maximum distance in pixels used to consider which objects could be tracked together.</html>");


    enableDivisionCheckbox = new JCheckBox("Enable Division", TrackingAppParams.getInstance().isEnableCellDivision());
    enableDivisionCheckbox.setToolTipText("<html>Controls whether object division is allowed or prevented.</html>");
    enableDivisionCheckbox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TrackingAppParams.getInstance().setEnableCellDivision(enableDivisionCheckbox.isSelected());
        TrackingAppParams.getInstance().getGuiPane().getAdvancedPanel().setDivisionEnabled(enableDivisionCheckbox.isSelected());
      }
    });
    enableDivisionCheckbox.setSelected(TrackingAppParams.getInstance().isEnableCellDivision());

    enableFusionCheckbox = new JCheckBox("Enable Fusion", TrackingAppParams.getInstance().isEnableCellFusion());
    enableFusionCheckbox.setToolTipText("<html>Controls whether object fusion is allowed or prevented.</html>");
    enableFusionCheckbox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TrackingAppParams.getInstance().setEnableCellFusion(enableFusionCheckbox.isSelected());
        minObjectSize.setEnabled(!enableFusionCheckbox.isSelected());
      }
    });
    enableFusionCheckbox.setSelected(TrackingAppParams.getInstance().isEnableCellFusion());
  }


  public boolean hasError() {
    return minObjectSize.hasError() || maxCentroidDisplacement.hasError();
  }

  public String getErrorString() {
    String ret = "";

    if(minObjectSize.hasError())
      ret += "Invalid Minimum Object Size: \"" + minObjectSize.getText() + "\"\n";
    if(maxCentroidDisplacement.hasError())
      ret += "Invalid Maximum Centroid Displacement: \"" + maxCentroidDisplacement.getText() + "\"\n";

    return ret;
  }

  public int getMinObjectSize() { return minObjectSize.getValue(); }
  public double getMaxCentroidDisplacement() { return maxCentroidDisplacement.getValue(); }
  public boolean isEnableDivision() { return enableDivisionCheckbox.isSelected(); }
  public boolean isEnableFusion() { return enableFusionCheckbox.isSelected(); }
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
  public String getImageWindowName() {
    return (String)imageWindowComboBox.getSelectedItem();
  }

}
