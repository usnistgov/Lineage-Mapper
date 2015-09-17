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