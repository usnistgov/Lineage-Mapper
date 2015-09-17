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

//
// ================================================================

// ================================================================
//
// Author: tjb3
// Date: Apr 18, 2014 12:21:22 PM EST
//
// Time-stamp: <Apr 18, 2014 12:21:22 PM tjb3>
//
// Description of DirectoryChooserPanel.java
//
// ================================================================

package main.java.gov.nist.isg.tracking.filechooser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

import main.java.gov.nist.isg.tracking.textfield.ValidatedTextField;
import main.java.gov.nist.isg.tracking.textfield.validator.ValidatorFile;

/**
 * DirectoryChooserPanel is used as a wrapper to contain a directory chooser Utility functions for
 * getting the selected directory are available.
 * 
 * @author Tim Blattner
 * @version 1.0
 * 
 */
public class DirectoryChooserPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private JLabel label;
  private JButton button;

  private ValidatedTextField<File> input;


  /**
   * Creates a directory chooser
   * 
   * @param label the label associated with the directory chooser
   * @param defLocation the default directory
   * @param sz the size of the text field
   */
  public DirectoryChooserPanel(String label, String defLocation, int sz) {
    super(new FlowLayout(FlowLayout.CENTER));

    File f = new File(defLocation);
    f.mkdirs();

    this.label = new JLabel(label);
    this.input = new ValidatedTextField<File>(sz, defLocation, new ValidatorFile());


    this.button = new JButton("Browse");

    this.input.setToolTipText(label);
    this.button.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        JFileChooser chooser = new JFileChooser(input.getText());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int val = chooser.showOpenDialog(button);
        if (val == JFileChooser.APPROVE_OPTION) {
          input.setText(chooser.getSelectedFile().getAbsolutePath());
        }

      }
    });

    add(this.label);
    add(this.input);
    add(this.button);
    this.hasError();
  }

  @Override
  public void setEnabled(boolean val) {
    label.setEnabled(val);
    input.setEnabled(val);
    button.setEnabled(val);
    super.setEnabled(val);
  }

  /**
   * Creates a directory chooser
   * 
   * @param label the label associated with the directory chooser
   * @param defLocation the default directory
   */
  public DirectoryChooserPanel(String label, String defLocation) {
    this(label, defLocation, 20);
  }

  /**
   * Creates a directory chooser with the default location being user.home
   * 
   * @param label the label associated with the directory chooser
   */
  public DirectoryChooserPanel(String label) {
    this(label, System.getProperty("user.home"));
  }

  /**
   * Creates a directory chooser with the default location being user.home
   * 
   * @param label the label associated with the directory chooser
   * @param sz the size of the text field
   */
  public DirectoryChooserPanel(String label, int sz) {
    this(label, System.getProperty("user.home"), sz);
  }

  /**
   * Gets the input text field for the directory chooser
   * 
   * @return the input text field
   */
  public JTextField getInputField() {
    return input;
  }


  public boolean hasError() {
    return input.hasError();
  }
  /**
   * Shows an error for the text field
   */
  public void showError() {
    input.setBackground(Color.RED);
  }

  /**
   * Hides an error for the text field
   */
  public void hideError() {
    input.setBackground(Color.WHITE);
  }

  /**
   * Sets the value for the text field
   * 
   * @param value the value
   */
  public void setValue(String value) {
    input.setText(value);
  }

  /**
   * Gets the value of the text field
   * 
   * @return the text field value
   */
  public String getValue() {
    return input.getText();
  }

}
