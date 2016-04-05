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
import java.io.IOException;

/**
 * Created by mmajursk on 5/28/2014.
 */
public class ValidatorFile implements Validator<File> {


  private String errorText;

  /**
   * Creates an integer validator that allows any integer
   */
  public ValidatorFile() {
    errorText =
        "<html>Please only a valid File Path.</html>";
  }


  @Override
  public boolean validate(String val) {
    File f = new File(val);
    try {
      f.getCanonicalPath();
    } catch (IOException ignored) {
      return false;
    }
    return true;
  }

  @Override
  public String getErrorText() {
    return errorText;
  }

  @Override
  public File getValue(String val) {
    return new File(val);
  }
}