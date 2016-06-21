// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package main.java.gov.nist.isg.lineage.mapper;

import ij.Macro;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import main.java.gov.nist.isg.lineage.mapper.app.TrackingAppParams;
import main.java.gov.nist.isg.lineage.mapper.app.gui.CellTrackerGUI;
import main.java.gov.nist.isg.lineage.mapper.app.gui.swingworkers.TrackSwingWorker;
import main.java.gov.nist.isg.lineage.mapper.lib.Log;

public class Lineage_Mapper_Plugin implements PlugIn {

  /**
   * The macro recorder command
   */
  public static String recorderCommand;
  private static String macroOptions;
  private TrackingAppParams params;

  public Lineage_Mapper_Plugin() {
    this.params = new TrackingAppParams();
  }

  // LineageMapper call
  public void run(String arg) {

    try {
      // prevents the recorder from printing the call to launch the macro to the recorder window
      // this is done because when using a JFrame macro recording is taken care of explicitly as
      // opposed to allowing the generic dialog to handle it
      recorderCommand = Recorder.getCommand();
      Recorder.setCommand(null);

      macroOptions = Macro.getOptions();
      if (macroOptions != null) {
        // running from a macro
        params.setIsMacro(true);
        params.loadMacro(macroOptions);
        // create a non-showing gui to validate the parameters
        CellTrackerGUI app = new CellTrackerGUI(params, false);
        (new TrackSwingWorker(params)).execute();
      } else {
        params.setIsMacro(false);
        // running from the GUI
        // create the gui to get the remainder of the options for the CT
        CellTrackerGUI app = new CellTrackerGUI(params, true);
      }

    } catch (Exception e) {
      e.printStackTrace();
      Log.error(e);
    }
  }


}
