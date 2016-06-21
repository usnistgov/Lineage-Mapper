// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package main.java.gov.nist.isg.lineage.mapper.app.utils;

import ij.Macro;
import ij.plugin.frame.Recorder;
import main.java.gov.nist.isg.lineage.mapper.lib.Log;


public class MacroUtils {

  /**
   * Loads a macro integer
   *
   * @param options the macro options
   * @param key     the key value
   * @param def     the default value
   * @return the loaded integer
   */
  public static int loadMacroInteger(String options, String key, int def) {
    try {
      return Integer.parseInt(Macro.getValue(options, key.toLowerCase(), Integer.toString(def)));
    } catch (NumberFormatException ex) {
      Log.error("Error parsing macro: " + key + " must be an integer");
      return def;
    }
  }

  /**
   * Loads a macro double
   *
   * @param options the macro options
   * @param key     the key value
   * @param def     the default value
   * @return the loaded double
   */
  public static double loadMacroDouble(String options, String key, double def) {
    try {
      return Double.parseDouble(Macro.getValue(options, key.toLowerCase(), Double.toString(def)));
    } catch (NumberFormatException ex) {
      Log.error("Error parsing macro: " + key + " must be a double");
      return def;
    }
  }

  /**
   * Loads a macro String
   *
   * @param options the macro options
   * @param key     the key value
   * @param def     the default value
   * @return the loaded String
   */
  public static String loadMacroString(String options, String key, String def) {
    return Macro.getValue(options, key.toLowerCase(), def);
  }


  /**
   * Loads a macro boolean
   *
   * @param options the macro options
   * @param key     the key value
   * @param def     the default value
   * @return the loaded boolean
   */
  public static boolean loadMacroBoolean(String options, String key, boolean def) {
    try {
      return Boolean
          .parseBoolean(Macro.getValue(options, key.toLowerCase(), Boolean.toString(def)));
    } catch (NumberFormatException ex) {
      Log.error("Error parsing macro: " + key + " must be a boolean");
      return def;
    }
  }


  /**
   * Records an integer into a macro
   *
   * @param key the key for the macro
   * @param val the value for the macro
   */
  public static void recordInteger(String key, int val) {
    Recorder.recordOption(key, Integer.toString(val));
  }

  /**
   * Records an double into a macro
   *
   * @param key the key for the macro
   * @param val the value for the macro
   */
  public static void recordDouble(String key, double val) {
    Recorder.recordOption(key, Double.toString(val));
  }

  /**
   * Records an string into a macro
   *
   * @param key the key for the macro
   * @param val the value for the macro
   */
  public static void recordString(String key, String val) {
    if (val.equals(""))
      val = "[]";
    if (val.length() >= 3 && Character.isLetter(val.charAt(0))
        && val.charAt(1) == ':' && val.charAt(2) == '\\')
      val = val.replaceAll("\\\\", "\\\\\\\\"); // replace "\" with "\\" in Windows file paths
    Recorder.recordOption(key, val);
  }

  /**
   * Records an boolean into a macro
   *
   * @param key the key for the macro
   * @param val the value for the macro
   */
  public static void recordBoolean(String key, boolean val) {
    Recorder.recordOption(key, Boolean.toString(val));
  }


}
