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
// Date: Apr 18, 2014 12:34:15 PM EST
//
// Time-stamp: <Apr 18, 2014 12:34:15 PM tjb3>
//
// Description of ValidatorDbl.java
//
// ================================================================

package main.java.gov.nist.isg.tracking.textfield.validator;

/**
 * Validator that checks double values
 * 
 * @author Tim Blattner
 * @version 1.0
 * 
 */
public class ValidatorDbl implements Validator<Double> {

  private double min;
  private double max;

  private String errorText;

  /**
   * Creates a double validator that allows any double
   */
  public ValidatorDbl() {
    min = Double.NEGATIVE_INFINITY;
    max = Double.POSITIVE_INFINITY;
    errorText =
        "<html>Please only enter numbers in the text field.<br>" + "Must be any double.</html>";
  }

  /**
   * Creates a double validator that checks in bounds by min (inclusive) and max (inclusive)
   * 
   * @param min the minimum value that is valid (inclusive)
   * @param max the maximum value that is valid (inclusive)
   */
  public ValidatorDbl(double min, double max) {
    this.min = min;
    this.max = max;
    errorText =
        "<html>Please only enter numbers in the text field.<br>"
            + "Must be greater than or equal to " + min + " and less than or equal to " + max
            + "</html>";
  }

  @Override
  public boolean validate(String val) {
    try {
      double test = Double.parseDouble(val);

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
  public Double getValue(String val) {
    if (val.equals(""))
      return 0.0;
    try {
      return Double.parseDouble(val);
    } catch (NumberFormatException ex) {
      return min;
    }
  }

}
