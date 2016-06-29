// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package main.java.gov.nist.isg.lineage.mapper.app.gui.panels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import main.java.gov.nist.isg.lineage.mapper.app.TrackingAppParams;
import main.java.gov.nist.isg.lineage.mapper.app.gui.swingworkers.LoadParamsSwingWorker;
import main.java.gov.nist.isg.lineage.mapper.app.gui.swingworkers.SaveParamsSwingWorker;
import main.java.gov.nist.isg.lineage.mapper.app.gui.swingworkers.TrackSwingWorker;
import main.java.gov.nist.isg.lineage.mapper.app.images.AppImageHelper;
import main.java.gov.nist.isg.lineage.mapper.lib.Log;

/**
 * Special JPanel to hold the tracking control elements
 */
public class ControlPanel extends JPanel {

  private JButton trackButton;
  private JButton saveParamsButton;
  private JButton loadParamsButton;
  private JLabel logo;
  private JComboBox logLevelComboBox;
  private JPanel logPanel;
  private JProgressBar progressBar;
  private TrackingAppParams params;

  /**
   * Special JPanel to hold the tracking control elements
   * @param params instance of TrackingAppParams to be updated by the options available in this
   *               panel.
   */
  public ControlPanel(TrackingAppParams params) {
    super();

    this.params = params;
    initElements();
    init();
  }

  private void init() {

    GridBagLayout layout = new GridBagLayout();
    this.setLayout(layout);
    GridBagConstraints c = new GridBagConstraints();

    c.anchor = GridBagConstraints.CENTER;
    c.insets = new Insets(2, 8, 2, 8);
    c.gridwidth = 3;
    c.gridy = 0;
    c.gridx = 0;
    this.add(progressBar, c);

    c.gridx = 3;
    this.add(logPanel, c);


    c.gridwidth = 1;
    c.gridy = 1;
    c.gridx = 0;
    this.add(logo, c);

    c.gridx = 1;
    this.add(saveParamsButton, c);

    c.gridx = 2;
    this.add(trackButton, c);

    c.gridx = 3;
    this.add(loadParamsButton, c);


  }

  private void initElements() {

    progressBar = new JProgressBar(0, 100);
    progressBar.setMinimumSize(new Dimension(360, 20));
    progressBar.setPreferredSize(new Dimension(360, 20));
    progressBar.setString("Waiting to Start");
    progressBar.setStringPainted(true);
    progressBar.setValue(0);

    trackButton = new JButton("Track");
    trackButton.setPreferredSize(new Dimension(120, 40));
    trackButton.setMinimumSize(new Dimension(120, 40));
    trackButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        (new TrackSwingWorker(params)).execute();
      }
    });


    // default value is whatever value is in params
    Log.LogType[] vals = Log.LogType.values();
    String[] logLevels = new String[vals.length];
    for (int i = 0; i < vals.length; i++)
      logLevels[i] = vals[i].toString();
    logLevelComboBox = new JComboBox(logLevels);
    logLevelComboBox.setSelectedItem(Log.getLogLevel());
    logLevelComboBox.setToolTipText("<html>Controls the level of logging generated.</html>");
    logLevelComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Log.setLogLevel(logLevelComboBox.getSelectedItem().toString());
        System.out.println("Log: " + logLevelComboBox.getSelectedItem().toString());
      }
    });

    saveParamsButton = new JButton("Save Params");
    saveParamsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        (new SaveParamsSwingWorker(params)).execute();
      }
    });


    loadParamsButton = new JButton("Load Params");
    loadParamsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        (new LoadParamsSwingWorker(params)).execute();
      }
    });


    logPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = 0;
    c.gridx = 0;
    c.anchor = GridBagConstraints.CENTER;
    logPanel.add(new JLabel("Log Level"), c);
    c.gridy = 1;
    logPanel.add(logLevelComboBox, c);


    ImageIcon icon = null;
    try {
      icon = AppImageHelper.loadImage("NIST-Logo_5.png");
    } catch (FileNotFoundException ignored) {
    }
    if (icon != null)
      logo = new JLabel(icon);
    else
      logo = new JLabel();

  }

  public JButton getTrackButton() {
    return this.trackButton;
  }

  public JProgressBar getProgressBar() {
    return this.progressBar;
  }

}
