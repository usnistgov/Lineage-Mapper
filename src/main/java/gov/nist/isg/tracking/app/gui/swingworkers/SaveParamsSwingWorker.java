package main.java.gov.nist.isg.tracking.app.gui.swingworkers;

import java.io.File;

import javax.swing.*;

import main.java.gov.nist.isg.tracking.app.TrackingAppParams;
import main.java.gov.nist.isg.tracking.app.gui.CellTrackerGUI;
import main.java.gov.nist.isg.tracking.lib.Log;

/**
 * Created by mmajursk on 5/22/2014.
 */
public class SaveParamsSwingWorker extends SwingWorker<Void, Void> {

  @Override
  protected Void doInBackground() {

    TrackingAppParams params = TrackingAppParams.getInstance();

    CellTrackerGUI guiPane = params.getGuiPane();
    // update the progress bar to indicate parameters are being saved
    JProgressBar progressBar = guiPane.getControlPanel().getProgressBar();
    progressBar.setString("Saving Parameters...");
    progressBar.setIndeterminate(true);

    if(!guiPane.hasError()) {
      params.pullParamsFromGui();
    }else {
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

      params.logParams();
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
