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


package main.java.gov.nist.isg.tracking.textfield.validator;

import java.io.File;

/**
 * Validator that checks integer values
 *
 * @author Michael Majurski
 * @version 1.0
 *
 */
public class ValidatorPrefix  implements Validator<String> {

  private String prefix;
  private String errorText;

  /**
   * Creates an integer validator that allows any integer
   */
  public ValidatorPrefix() {
    prefix = "";
    errorText = "<html>Please only enter valid file path characters in the text field.</html>";
  }

  @Override
  public boolean validate(String val) {
    if( prefix.contains(File.separator))
      return false;
    else
      return true;
  }

  @Override
  public String getErrorText() {
    return errorText;
  }

  @Override
  public String getValue(String val) {
    if(validate(val))
      return val;
    else
      return "";
  }

}

