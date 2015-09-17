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
// Date: Apr 18, 2014 12:39:52 PM EST
//
// Time-stamp: <Apr 18, 2014 12:39:52 PM tjb3>
//
// Description of TextFieldInputPanel.java
//
// ================================================================

package main.java.gov.nist.isg.tracking.textfield;

import java.awt.*;

import javax.swing.*;

import main.java.gov.nist.isg.tracking.textfield.validator.Validator;

/**
 * Cretes a text field input panel
 * 
 * @author Tim Blattner
 * @version 1.0
 * 
 * @param <T> the type of text field
 */
public class TextFieldInputPanel<T> extends JPanel {

  private static final long serialVersionUID = 1L;

  private JLabel label;
  private ValidatedTextField<T> input;
  private JLabel units;
  private Validator<T> validator;

  /**
   * Creates a text field input panel
   * 
   * @param label the label for the text field
   * @param text the text inside the text field
   * @param validator the validator associated with the text field
   */
  public TextFieldInputPanel(String label, String text, Validator<T> validator) {
    this(label, text, 10, "", validator);
  }

  public TextFieldInputPanel(String label, String text, String units, Validator<T> validator) {
    this(label, text, 10, units, validator);
  }

  /**
   * Creates a text field input panel
   * 
   * @param label the label for the text field
   * @param text the text inside the text field
   * @param sz the size of the text field
   * @param validator the validator associated with the text field
   */
  public TextFieldInputPanel(String label, String text, int sz, String units, Validator<T> validator) {
    super(new FlowLayout(FlowLayout.LEFT));

    this.validator = validator;
    this.label = new JLabel(label);
    this.input = new ValidatedTextField<T>(sz, text, validator);
    this.units = new JLabel(units);
    add(this.label);
    add(this.input);
    add(this.units);
  }

  /**
   * Sets the value for the text field (integer)
   * 
   * @param value the integer value
   */
  public void setValue(int value) {
    input.setText(Integer.toString(value));
  }

  /**
   * Sets the value for the text field (double)
   * 
   * @param value the double value
   */
  public void setValue(double value) {
    input.setText(Double.toString(value));
  }

  /**
   * Sets the value for the text field (String)
   * 
   * @param value the String value
   */
  public void setValue(String value) {
    input.setText(value);
  }

  /**
   * Checks if an error exists in the input
   * 
   * @return true if an error exists
   */
  public boolean hasError() {
    return input.hasError();
  }

  /**
   * Shows the error for this text field
   */
  public void showError() {
    input.showError();
  }

  /**
   * Hides the error for this text field
   */
  public void hideError() {
    input.hideError();
  }

  public String getText() { return input.getText(); }

  /**
   * Gets the value for the text field parsed by the validator
   * 
   * @return the value parsed by the validator
   */
  public T getValue() {
    return validator.getValue(input.getText());
  }

  /**
   * Enables ignoring errors
   */
  public void enableIgnoreErrors() {
    input.enableIgnoreErrors();
  }

  /**
   * Disables ignoring errors
   */
  public void disableIgnoreErrors() {
    input.disableIgnoreErrors();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);

    label.setEnabled(enabled);
    input.setEnabled(enabled);
  }

}
