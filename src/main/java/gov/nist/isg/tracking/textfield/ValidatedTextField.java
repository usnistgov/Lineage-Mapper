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
// Date: Apr 18, 2014 12:42:42 PM EST
//
// Time-stamp: <Apr 18, 2014 12:42:42 PM tjb3>
//
// Description of ValidatedTextField.java
//
// ================================================================

package main.java.gov.nist.isg.tracking.textfield;

import java.awt.*;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import main.java.gov.nist.isg.tracking.textfield.validator.Validator;

/**
 * Creates a text field that is validated by a validator.
 * 
 * @author Tim Blattner
 * @version 1.0
 * 
 * @param <T> the type for the text field
 */
public class ValidatedTextField<T> extends JTextField {

  private static final long serialVersionUID = 1L;
  private boolean ignoreErrors = false;
  private Validator<T> validator;

  /**
   * Creates a text field validated by a validator
   * 
   * @param size the size of the text field
   * @param text the default text for the text field
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
   * 
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
   * 
   * @author Tim Blattner
   * @version 1.0
   * 
   * @param <V> the type of the text field
   */
  class TextFieldFilter<V> extends DocumentFilter {

    private JTextField txtArea;
    private Validator<V> validator;

    /**
     * Creates a text field filter
     * 
     * @param txtArea the text area associated with the filter
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
