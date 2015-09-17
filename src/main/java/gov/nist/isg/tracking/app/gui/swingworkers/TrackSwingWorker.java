package main.java.gov.nist.isg.tracking.app.gui.swingworkers;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import ij.IJ;
import ij.WindowManager;
import main.java.gov.nist.isg.tracking.Cell_Tracker_Plugin;
import main.java.gov.nist.isg.tracking.app.TrackingAppParams;
import main.java.gov.nist.isg.tracking.app.gui.CellTrackerGUI;
import main.java.gov.nist.isg.tracking.app.gui.panels.ControlPanel;
import main.java.gov.nist.isg.tracking.lib.Log;
import main.java.gov.nist.isg.tracking.metadata.BirthDeathMetadata;
import main.java.gov.nist.isg.tracking.metadata.ConfidenceIndexMetadata;
import main.java.gov.nist.isg.tracking.metadata.DivisionMetadata;
import main.java.gov.nist.isg.tracking.metadata.FusionMetadata;

/**
 * Created by mmajursk on 5/22/2014.
 */
public class TrackSwingWorker extends SwingWorker<Void, Void> {

  @Override
  protected Void doInBackground() {

    TrackingAppParams params = TrackingAppParams.getInstance();
    String name = params.getGuiPane().getInputPanel().getImageWindowName();
    if(name == null || name.compareToIgnoreCase("<null>") == 0) {
      IJ.error("No Image Stack Selected");
      return null;
    }
    params.setInputStack(WindowManager.getImage(name).getImageStack());
    ControlPanel controlPanel = params.getGuiPane().getControlPanel();

    // get the state of tracking and not it, to update the new state
    boolean tracking = !params.isTracking();

    JButton trackButton = controlPanel.getTrackButton();
    if(tracking) {
      CellTrackerGUI guiPane = params.getGuiPane();
      JProgressBar progressBar = guiPane.getControlPanel().getProgressBar();
      try{
        params.pullParamsFromGui();
      }catch(IllegalArgumentException e) {
        progressBar.setString("Invalid Parameters Found");
        return null;
      }

      // record the macro parameters
      params.record();

      List<String> fileList = checkOverwriteExistingOutputFiles();
      if(!canOverwriteExistingFilesCheck(fileList)) {
        return null;
      }

      params.setIsTracking(true); // update params to reflect the new state
      trackButton.setText("Cancel");
      // disable the load and save buttons now that tracking is starting
      controlPanel.getLoadParamsButton().setEnabled(false);
      controlPanel.getSaveParamsButton().setEnabled(false);

      // set the tracking thread in AppParameters so that the tracking can later be canceled if desired
      params.setTrackingThread(Thread.currentThread());

      // init the progress bar
      progressBar.setString("Tracking");
      progressBar.setIndeterminate(false);
      params.setProgressBar(0.0);

      try {

        // call the cell main.java.gov.nist.isg.tracking
        Cell_Tracker_Plugin.track(false);

        progressBar.setString("Tracking Done");


      }catch(IllegalArgumentException e) {
        progressBar.setString("Invalid Tracking Parameter");
      }catch(Exception e) {
        Log.error(e);
        StackTraceElement[] st = e.getStackTrace();
        Log.error("Exception in main.java.gov.nist.isg.tracking.Cell_Tracker_Plugin.track: " + e.getMessage());
        for (int i = 0; i < st.length; i++) {
          Log.error(st[i].toString());
        }
        IJ.error("Exception in main.java.gov.nist.isg.tracking.Cell_Tracker_Plugin.track: " + e.getMessage());
      }

      // clean up the progress bar
      params.setProgressBar(1.0);
      params.setIsTracking(false); // update params to reflect the new state
      // re-enable the load and save buttons now that tracking is done
      controlPanel.getLoadParamsButton().setEnabled(true);
      controlPanel.getSaveParamsButton().setEnabled(true);
      trackButton.setText("Track");



    }else{

      Thread trackingThread = params.getTrackingThread();
      if(trackingThread != null)
        trackingThread.interrupt();

      params.getGuiPane().getControlPanel().getProgressBar().setString("Tracking Canceled");
      params.setProgressBar(0.0);

      trackButton.setText("Track");
    }

    return null;
  }


  private List<String> checkOverwriteExistingOutputFiles() {
    List<String> fileList = new ArrayList<String>();

    TrackingAppParams params = TrackingAppParams.getInstance();

    // check if the output saving has been enabled
    if(params.isSaveOutputsEnabled()) {
      String prefix = params.getPrefix();
      String outDir = params.getOutputDirectory();
      if(!outDir.endsWith(File.separator))
        outDir += File.separator;



      File bdmFile = new File(outDir + prefix + BirthDeathMetadata.getFileName());
      if (bdmFile.exists())
        fileList.add(bdmFile.getAbsolutePath());


      if(params.isEnableCellDivision()) {
        File file = new File(outDir + prefix + DivisionMetadata.getFileName());
        if (file.exists())
          fileList.add(file.getAbsolutePath());
      }

      if(params.isEnableCellFusion()) {
        File file = new File(outDir + prefix + FusionMetadata.getFileName());
        if (file.exists())
          fileList.add(file.getAbsolutePath());
      }

      File ciFile = new File(outDir + prefix + ConfidenceIndexMetadata.getFileName());
      if (ciFile.exists())
        fileList.add(ciFile.getAbsolutePath());


      if(params.isSaveAsStackEnabled()) {
        File file = new File(outDir + params.generateOutputImageFileName(0));
        if(file.exists())
          fileList.add(file.getAbsolutePath());
      }else{
        for(int i = 0; i < params.getInputStack().getSize(); i++) {
          File file = new File(outDir + params.generateOutputImageFileName(i));
          if(file.exists())
            fileList.add(file.getAbsolutePath());
        }
      }

    }

    return fileList;
  }

  private boolean canOverwriteExistingFilesCheck(List<String> fileList) {
    if (fileList.size() > 0) {
      JPanel panel = new JPanel(new GridBagLayout());
      JList jList = new JList(fileList.toArray());
      JScrollPane scrollPane = new JScrollPane(jList);
      scrollPane.setPreferredSize(new Dimension(500, 200));

      JLabel label = new JLabel("Warning: Would you like to overwrite the files listed above?");

      GridBagConstraints c = new GridBagConstraints();
      c.gridx = 0;
      c.gridy = 0;
      panel.add(scrollPane, c);

      c.gridy = 1;
      panel.add(label, c);

      int
          val =
          JOptionPane.showConfirmDialog(null, panel, "Warning: Overwritting files",
                                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                                        null);

      if (val == JOptionPane.YES_OPTION) {
        return true;
      } else {
        return false;
      }

    }
    return true;
  }
}
