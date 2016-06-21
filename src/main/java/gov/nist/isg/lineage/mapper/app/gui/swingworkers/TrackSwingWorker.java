// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package main.java.gov.nist.isg.lineage.mapper.app.gui.swingworkers;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import ij.IJ;
import ij.plugin.frame.Recorder;
import main.java.gov.nist.isg.lineage.mapper.LineageMapper;
import main.java.gov.nist.isg.lineage.mapper.app.TrackingAppParams;
import main.java.gov.nist.isg.lineage.mapper.app.gui.panels.ControlPanel;
import main.java.gov.nist.isg.lineage.mapper.lib.ImageFrame;
import main.java.gov.nist.isg.lineage.mapper.lib.Log;
import main.java.gov.nist.isg.lineage.mapper.metadata.BirthDeathMetadata;
import main.java.gov.nist.isg.lineage.mapper.metadata.ConfidenceIndexMetadata;
import main.java.gov.nist.isg.lineage.mapper.metadata.DivisionMetadata;
import main.java.gov.nist.isg.lineage.mapper.metadata.FusionMetadata;


public class TrackSwingWorker extends SwingWorker<Void, Void> {

  private TrackingAppParams params;

  public TrackSwingWorker(TrackingAppParams params) {
    this.params = params;
  }

  @Override
  protected Void doInBackground() {

    ControlPanel controlPanel = params.getGuiPane().getControlPanel();

    JButton trackButton = controlPanel.getTrackButton();
    LineageMapper lm;
    if (!params.isTracking()) { // if its are not currently tracking, then it can start
      JProgressBar progressBar = controlPanel.getProgressBar();

      if (params.isMacro()) {
        // Running from the macro
        params.getGuiPane().getOptionsPanel().pushParamsToGUI();
        params.getGuiPane().getAdvancedPanel().pushParamsToGUI();

        params.validateParameters();
        lm = new LineageMapper(params);

        List<String> fileList = checkOverwriteExistingOutputFiles(params);

        for (String s : fileList)
          Log.mandatory("Overwriting: " + s);

      } else {
        // Running from the GUI
        try {
          params.pullParamsFromGui();
        } catch (IllegalArgumentException e) {
          progressBar.setString("Invalid Parameters Found");
          Log.error("Invalid Parameters Found");
          return null;
        }

        if (Recorder.record || Recorder.recordInMacros) {
          Log.mandatory("Recording Macro Options");
          params.recordMacro();
        }
        lm = new LineageMapper(params);

        List<String> fileList = checkOverwriteExistingOutputFiles(params);
        if (!canOverwriteExistingFilesCheck(fileList)) {
          return null;
        }
      }


      params.setIsTracking(true); // update params to reflect the new state
      trackButton.setText("Cancel");

      // set the tracking thread in AppParameters so that the tracking can later be canceled if desired
      params.setTrackingThread(Thread.currentThread());

      progressBar.setString("Tracking");
      progressBar.setIndeterminate(false);
      params.setProgressBar(0.0);

      // run the tracking
      lm.run();

      params.setIsTracking(false); // update params to reflect the new state
      trackButton.setText("Track");
      params.setProgressBar(1.0);
      progressBar.setString("Done Tracking");

    } else { // its already tracking, so we can only cancel

      Thread trackingThread = params.getTrackingThread();
      if (trackingThread != null)
        trackingThread.interrupt();

      params.getGuiPane().getControlPanel().getProgressBar().setString("Tracking Canceled");
      params.setProgressBar(0.0);
      trackButton.setText("Track");
    }

    return null;
  }


  public static List<String> checkOverwriteExistingOutputFiles(TrackingAppParams params) {
    List<String> fileList = new ArrayList<String>();
    // check if the output saving has been enabled


    String prefix = params.getOutputPrefix();
    String outDir = params.getOutputDirectory();


    File bdmFile = new File(outDir + prefix + BirthDeathMetadata.getFileName());
    if (bdmFile.exists())
      fileList.add(bdmFile.getAbsolutePath());


    if (params.isEnableCellDivision()) {
      File file = new File(outDir + prefix + DivisionMetadata.getFileName());
      if (file.exists())
        fileList.add(file.getAbsolutePath());
    }

    if (params.isEnableCellFusion()) {
      File file = new File(outDir + prefix + FusionMetadata.getFileName());
      if (file.exists())
        fileList.add(file.getAbsolutePath());
    }

    File ciFile = new File(outDir + prefix + ConfidenceIndexMetadata.getFileName());
    if (ciFile.exists())
      fileList.add(ciFile.getAbsolutePath());

    File paramsFile = new File(outDir + prefix + "tracking-params.txt");
    if (paramsFile.exists())
      fileList.add(paramsFile.getAbsolutePath());

    // warn the user about overwriting all of the lineage viewer files
    File viewerFiles = new File(outDir + prefix + "lineage-viewer");
    if (viewerFiles.exists())
      fileList.add(viewerFiles.getAbsolutePath() + File.separator + "*");

    // get the write conflicts in the set of images to be written to disk
    for (ImageFrame imf : params.getFramesList()) {
      File file = new File(imf.getImage().getOutputFilepath());
      if (file.exists()) {
        fileList.add(file.getAbsolutePath());
      }
    }

    return fileList;
  }

  public static boolean canOverwriteExistingFilesCheck(List<String> fileList) {
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
          JOptionPane.showConfirmDialog(null, panel, "Warning: Overwriting files",
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
