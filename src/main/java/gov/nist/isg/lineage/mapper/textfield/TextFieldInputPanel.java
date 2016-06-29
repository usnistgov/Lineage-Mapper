// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package main.java.gov.nist.isg.lineage.mapper.textfield;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import main.java.gov.nist.isg.lineage.mapper.textfield.validator.Validator;

/**
 * Creates a text field input panel
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
   * @param label     the label for the text field
   * @param text      the text inside the text field
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
   * @param label     the label for the text field
   * @param text      the text inside the text field
   * @param sz        the size of the text field
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
   * @param value the integer value
   */
  public void setValue(int value) {
    input.setText(Integer.toString(value));
  }

  /**
   * Sets the value for the text field (double)
   * @param value the double value
   */
  public void setValue(double value) {
    input.setText(Double.toString(value));
  }

  /**
   * Sets the value for the text field (String)
   * @param value the String value
   */
  public void setValue(String value) {
    input.setText(value);
  }

  /**
   * Checks if an error exists in the input
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

  public String getText() {
    return input.getText();
  }

  /**
   * Gets the value for the text field parsed by the validator
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
