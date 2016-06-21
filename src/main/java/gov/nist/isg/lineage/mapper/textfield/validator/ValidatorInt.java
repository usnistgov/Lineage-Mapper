// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package main.java.gov.nist.isg.lineage.mapper.textfield.validator;


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
        "<html>Please only enter numbers in the text field.<br>" + "Must be an integer.</html>";
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
