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
// Date: Aug 1, 2013 3:52:18 PM EST
//
// Time-stamp: <Aug 1, 2013 3:52:18 PM tjb3>
//
// Description of Log.java
//
// ================================================================

package main.java.gov.nist.isg.tracking.lib;

import java.util.HashMap;

import ij.IJ;

/**
 * @author Tim Blattner
 * @version 2.0
 */
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
      for(int i = 0; i < values.length; i++ ) {
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
   * @param level the new log level
   */
  public static void setLogLevel(LogType level) {
    Log.logLevel = level;
    if(level != LogType.NONE)
      msg("Log Level set to: " + Log.logLevel);
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
   * @param message the String to be printed
   */
  private static void msg(String message) {
    if (timeEnabled) {
      if (startTime == 0) {
        startTime = System.currentTimeMillis();
      }

      long elapsed = (System.currentTimeMillis() - startTime);

      IJ.log(elapsed + "ms: " + message);
    } else {
      IJ.log(message);
    }
  }


}
