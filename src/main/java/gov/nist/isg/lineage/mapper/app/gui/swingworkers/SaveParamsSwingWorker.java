// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package gov.nist.isg.lineage.mapper.app.gui.swingworkers;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import gov.nist.isg.lineage.mapper.app.TrackingAppParams;
import gov.nist.isg.lineage.mapper.app.gui.CellTrackerGUI;
import gov.nist.isg.lineage.mapper.lib.Log;


/**
 * Swing worker to load the tracking parameters from a text file into the TrackingAppParams instance
 */
public class SaveParamsSwingWorker extends SwingWorker<Void, Void> {

  private TrackingAppParams params;

  /**
   * Swing worker to load the tracking parameters from a text file into the TrackingAppParams instance
   * @param params the TrackingAppParams instance to load parameters into
   */
  public SaveParamsSwingWorker(TrackingAppParams params) {
    this.params = params;
  }

  @Override
  protected Void doInBackground() {

    CellTrackerGUI guiPane = params.getGuiPane();
    // update the progress bar to indicate parameters are being saved
    JProgressBar progressBar = guiPane.getControlPanel().getProgressBar();
    progressBar.setString("Saving Parameters...");
    progressBar.setIndeterminate(true);

    if (!guiPane.hasError()) {
      params.pullParamsFromGui();
    } else {
      progressBar.setString("Invalid Parameters Found");
      progressBar.setIndeterminate(false);
      Log.error("Invalid Parameters Discovered");
      return null;
    }

    JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(false);

    int val = chooser.showOpenDialog(guiPane);
    if (val == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();

      params.writeParamsToLog();
      params.writeParamsToFile(file);
      // update the progress bar to indicate parameters are saved
      progressBar.setString("Parameter Saved");
      progressBar.setIndeterminate(false);

    } else {
      progressBar.setString("Parameter Save Aborted");
      progressBar.setIndeterminate(false);
    }


    return null;
  }

}
