// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.



//
// ================================================================

// ================================================================
//
// Author: tjb3
// Date: Aug 1, 2013 3:52:18 PM EST
//
// Time-stamp: <Aug 1, 2013 3:52:18 PM tjb3>
//
// Description of Log.java
//
// ================================================================

package gov.nist.isg.lineage.mapper.lib;

import java.util.HashMap;

import ij.IJ;

public class Log {

  /**
   * Different types of logging
   */
  public static enum LogType {

    /**
     * All logging is turned completely off and no log output is printed
     */
    NONE("None"),

    /**
     * Must print log messages that always are printed
     */
    MANDATORY("Mandatory"),

    /**
     * Debug log messages for the programmer
     */
    DEBUG("Debug"),

    /**
     * Verbose log messages prints a lot of information including class, method, and line
     */
    VERBOSE("Verbose");

    private LogType(final String text) {
      this.text = text;
    }

    private final String text;

    @Override
    public String toString() {
      return text;
    }

    private static HashMap<String, LogType> logMap;

    static {
      logMap = new HashMap<String, LogType>();
      for (LogType t : LogType.values()) {
        logMap.put(t.toString(), t);
      }
    }

    public static String[] enumValsToStringArray() {
      LogType[] values = LogType.values();

      String[] ret = new String[values.length];
      for (int i = 0; i < values.length; i++) {
        ret[i++] = values[i].toString();
      }
      return ret;
    }

    public static LogType getLogType(String name) {
      return logMap.get(name);
    }

  }

  private static long startTime = 0;
  private static LogType logLevel = LogType.MANDATORY;
  private static boolean timeEnabled = false;

  /**
   * Enables timing in print statements
   */
  public static void enableTiming() {
    timeEnabled = true;
  }

  /**
   * Disables timing in print statements
   */
  public static void disableTiming() {
    timeEnabled = false;
  }

  /**
   * Sets the logger level
   *
   * @param level the new log level
   */
  public static void setLogLevel(String level) {
    LogType type = LogType.getLogType(level);
    if (type == null) {
      type = LogType.NONE;
    }
    setLogLevel(type);
  }

  /**
   * Set logger level.
   *
   * @param level the new log level
   */
  public static void setLogLevel(LogType level) {
    Log.logLevel = level;
    if (level != LogType.NONE)
      msg("Log Level set to: " + Log.logLevel);
  }

  public static String getLogLevel() {
    return Log.logLevel.toString();
  }


  public static void error(Throwable e) {
    StackTraceElement[] st = e.getStackTrace();

    Log.error("Exception in " + st[0].getClassName() + "." + st[0].getMethodName() + ": " + e.getMessage());
    Log.error("********************** Stack Trace: **********************");
    for (int i = 0; i < st.length; i++) {
      Log.error(st[i].toString());
    }
    Log.error("**********************************************************");
  }

  public static void error(String message) {
    Log.msg(message);
  }

  /**
   * Prints mandatory level messages.
   *
   * @param message the String to be printed.
   */
  public static void mandatory(String message) {
    if (Log.logLevel.ordinal() == LogType.VERBOSE.ordinal()) {
      Log.verbose(message);
    } else if (Log.logLevel.ordinal() >= LogType.MANDATORY.ordinal()) {
      Log.msg(message);
    }
  }


  /**
   * Prints debug level messages.
   *
   * @param message the String to be printed.
   */
  public static void debug(String message) {
    if (Log.logLevel.ordinal() == LogType.VERBOSE.ordinal()) {
      Log.verbose(message);
    } else if (Log.logLevel.ordinal() >= LogType.DEBUG.ordinal()) {
      Log.msg(message);
    }
  }


  /**
   * Prints string with stacktrace information prepended.
   *
   * @param message the the String to be printed.
   */
  public static void verbose(String message) {
    // Get the class and line number information from the stack
    // 3 because we want to omit this method and the calling method
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

    String fullClassName = stackTrace[3].getClassName();
    String methodName = stackTrace[3].getMethodName();
    int lineNumber = stackTrace[3].getLineNumber();
    message = fullClassName + ":" + methodName + ":" + lineNumber + " - " + message;

    Log.msg(message);
  }

  /**
   * Worker function to print the message to the required output
   *
   * @param message the String to be printed
   */
  private static void msg(String message) {
    if (timeEnabled) {
      if (startTime == 0) {
        startTime = System.currentTimeMillis();
      }

      long elapsed = (System.currentTimeMillis() - startTime);
      message = elapsed + "ms: " + message;
    }
    IJ.log(message);
  }


}
