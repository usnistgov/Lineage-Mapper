// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.



//
// ================================================================

// ================================================================
//
// Author: tjb3
// Date: Apr 18, 2014 12:42:42 PM EST
//
// Time-stamp: <Apr 18, 2014 12:42:42 PM tjb3>
//
// Description of ValidatedTextField.java
//
// ================================================================

package gov.nist.isg.lineage.mapper.textfield;

import java.awt.Color;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import gov.nist.isg.lineage.mapper.textfield.validator.Validator;

/**
 * Creates a text field that is validated by a validator.
 * @param <T> the type for the text field
 */
public class ValidatedTextField<T> extends JTextField {

  private static final long serialVersionUID = 1L;
  private boolean ignoreErrors = false;
  private Validator<T> validator;

  /**
   * Creates a text field validated by a validator
   * @param size      the size of the text field
   * @param text      the default text for the text field
   * @param validator the validator for the text field
   */
  public ValidatedTextField(int size, String text, Validator<T> validator) {
    super(size);
    this.setText(text);
    this.validator = validator;

    PlainDocument doc = (PlainDocument) super.getDocument();

    doc.setDocumentFilter(new TextFieldFilter<T>(this, validator));
    this.setToolTipText(validator.getErrorText());
    this.hasError();
  }

  /**
   * Shows an error for the text field
   */
  public void showError() {
    this.setBackground(Color.PINK);
  }

  /**
   * Hides an error for the text field
   */
  public void hideError() {
    this.setBackground(Color.WHITE);
  }

  /**
   * Checks if there is an error in the text field
   * @return true if an error exists
   */
  public boolean hasError() {
    if (validator.validate(this.getText())) {
      hideError();
      return false;
    } else {
      showError();
      return true;
    }
  }

  /**
   * Enables ignore errors
   */
  public void enableIgnoreErrors() {
    ignoreErrors = true;
  }

  /**
   * Disables ignore errors
   */
  public void disableIgnoreErrors() {
    ignoreErrors = false;
  }

  /**
   * Creates a text field filter that handles input into the text field.
   * @param <V> the type of the text field
   */
  class TextFieldFilter<V> extends DocumentFilter {

    private JTextField txtArea;
    private Validator<V> validator;

    /**
     * Creates a text field filter
     * @param txtArea   the text area associated with the filter
     * @param validator the validator to validate text
     */
    public TextFieldFilter(JTextField txtArea, Validator<V> validator) {
      this.txtArea = txtArea;
      this.validator = validator;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
        throws BadLocationException {
      if (!ignoreErrors) {
        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.insert(offset, string);
        super.insertString(fb, offset, string, attr);

        if (validator.validate(sb.toString())) {
          txtArea.setBackground(Color.WHITE);
        } else {
          txtArea.setBackground(Color.PINK);
        }

      } else
        super.insertString(fb, offset, string, attr);

    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
        throws BadLocationException {
      if (!ignoreErrors) {
        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.replace(offset, offset + length, text);
        super.replace(fb, offset, length, text, attrs);

        if (validator.validate(sb.toString())) {
          txtArea.setBackground(Color.WHITE);
        } else {
          txtArea.setBackground(Color.PINK);
        }
      } else
        super.replace(fb, offset, length, text, attrs);

    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
      if (!ignoreErrors) {
        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.delete(offset, offset + length);
        super.remove(fb, offset, length);

        if (validator.validate(sb.toString())) {
          txtArea.setBackground(Color.WHITE);
        } else {
          txtArea.setBackground(Color.PINK);

        }
      } else
        super.remove(fb, offset, length);

    }
  }
}
