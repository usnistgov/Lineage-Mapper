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
// Date: Apr 18, 2014 12:28:13 PM EST
//
// Time-stamp: <Apr 18, 2014 12:28:13 PM tjb3>
//
// Description of Validator.java
//
// ================================================================

package main.java.gov.nist.isg.tracking.textfield.validator;

/**
 * Validator is an interface that handles text validation for text fields. Functions for testing the
 * text, getting the values, and getting the error text associated with the validation are provided.
 * 
 * @author Tim Blattner
 * @version 1.0
 * 
 * @param <T> the type of Object for validation
 */
public interface Validator<T> {
  /**
   * Tests the text using a specific validator
   * 
   * @param val the text you wish to test
   * @return true if the the text is valid
   */
  public boolean validate(String val);

  /**
   * Gets the error message associated with this validator
   * 
   * @return the error message
   */
  public String getErrorText();

  /**
   * Gets the value of the text based on the validation type
   * 
   * @param val the value you wish to parse
   * @return the value parsed by the validator
   */
  public T getValue(String val);
}
