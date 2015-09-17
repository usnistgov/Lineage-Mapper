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
// Date: Apr 18, 2014 12:35:55 PM EST
//
// Time-stamp: <Apr 18, 2014 12:35:55 PM tjb3>
//
// Description of ValidatorInt.java
//
// ================================================================

package main.java.gov.nist.isg.tracking.textfield.validator;

/**
 * Validator that checks integer values
 * 
 * @author Tim Blattner
 * @version 1.0
 * 
 */
public class ValidatorInt implements Validator<Integer> {

  private int min;
  private int max;

  private String errorText;

  /**
   * Creates an integer validator that allows any integer
   */
  public ValidatorInt() {
    min = Integer.MIN_VALUE;
    max = Integer.MAX_VALUE;
    errorText =
        "<html>Please only enter numbers in the text field.<br>" + "Must be any integer.</html>";
  }

  /**
   * Creates an integer validator that checks in bounds by min (inclusive) and max (inclusive)
   * 
   * @param min the minimum value that is valid (inclusive)
   * @param max the maximum value that is valid (inclusive)
   */
  public ValidatorInt(int min, int max) {
    this.min = min;
    this.max = max;
    errorText =
        "<html>Please only enter integers in the text field.<br>"
            + "Must be greater than or equal to " + min + " and less than or equal to " + max
            + "</html>";
  }

  @Override
  public boolean validate(String val) {
    try {
      int test = Integer.parseInt(val);

      if (test < min || test > max)
        return false;
      else
        return true;
    } catch (NumberFormatException e) {
      return false;
    }

  }

  @Override
  public String getErrorText() {
    return errorText;
  }

  @Override
  public Integer getValue(String val) {
    if (val.equals(""))
      return 0;
    try {
      return Integer.parseInt(val);
    } catch (NumberFormatException ex) {
      return min;
    }
  }
}
