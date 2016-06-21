// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.



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

package main.java.gov.nist.isg.lineage.mapper.textfield.validator;


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
        "<html>Please only enter numbers in the text field.<br>" + "Must be a double.</html>";
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
