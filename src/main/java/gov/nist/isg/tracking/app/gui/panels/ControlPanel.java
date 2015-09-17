package main.java.gov.nist.isg.tracking.app.gui.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import javax.swing.*;

import main.java.gov.nist.isg.tracking.app.gui.swingworkers.LoadParamsSwingWorker;
import main.java.gov.nist.isg.tracking.app.gui.swingworkers.SaveParamsSwingWorker;
import main.java.gov.nist.isg.tracking.app.gui.swingworkers.TrackSwingWorker;
import main.java.gov.nist.isg.tracking.app.images.AppImageHelper;
import main.java.gov.nist.isg.tracking.lib.Log;

/**
 * Created by mmajursk on 5/22/2014.
 */
public class ControlPanel extends JPanel {

  private JButton trackButton;
  private JButton saveParamsButton;
  private JButton loadParamsButton;
  private JProgressBar progressBar;
  private JLabel logo;
  private JComboBox logLevelComboBox;
  JPanel logPanel;


  public ControlPanel() {
    super();

    initElements();
    init();
  }

  private void init() {

    GridBagLayout layout = new GridBagLayout();
    this.setLayout(layout);
    GridBagConstraints c = new GridBagConstraints();

    c.anchor = GridBagConstraints.CENTER;
    c.insets = new Insets(2,8,2,8);
    c.gridwidth = 3;

    c.gridx = 0;
    c.gridy = 0;
    this.add(progressBar, c);

    c.gridwidth = 1;
    c.gridx = 3;
    this.add(logPanel, c);

    c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = 1;
    this.add(logo, c);

    c.gridx = 1;
    this.add(saveParamsButton, c);
    c.gridx = 2;
    this.add(trackButton, c);
    c.gridx = 3;
    this.add(loadParamsButton, c);
  }

  private void initElements() {

    trackButton = new JButton("Track");
    trackButton.setPreferredSize(new Dimension(150,40));
    trackButton.setMinimumSize(new Dimension(150,40));
    trackButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        (new TrackSwingWorker()).execute();
      }
    });

    saveParamsButton = new JButton("Save Params");
    saveParamsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        (new SaveParamsSwingWorker()).execute();
      }
    });

    loadParamsButton = new JButton("Load Params");
    loadParamsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        (new LoadParamsSwingWorker()).execute();
      }
    });

    progressBar = new JProgressBar(0,100);
    progressBar.setMinimumSize(new Dimension(400, 20));
    progressBar.setPreferredSize(new Dimension(400, 20));
    progressBar.setString("Waiting to Start");
    progressBar.setStringPainted(true);
    progressBar.setValue(0);

    // default it to none
    Log.LogType[] vals = Log.LogType.values();
    String[] logLevels = new String[vals.length];
    for(int i = 0; i < vals.length; i++)
      logLevels[i] = vals[i].toString();
    logLevelComboBox = new JComboBox(logLevels);
    logLevelComboBox.setToolTipText("<html>Controls the level of logging generated.</html>");
    logLevelComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Log.setLogLevel(logLevelComboBox.getSelectedItem().toString());
        System.out.println("Log: " + logLevelComboBox.getSelectedItem().toString());
      }
    });
    logLevelComboBox.setSelectedIndex(0);


    logPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = 0;
    c.gridx = 0;
    c.anchor = GridBagConstraints.CENTER;
    logPanel.add(new JLabel("Log Level"),c);
    c.gridy = 1;
    logPanel.add(logLevelComboBox,c);



    ImageIcon icon = null;
    try{
      icon = AppImageHelper.loadImage("NIST-Logo_5.png");
    }catch(FileNotFoundException ignored) {}
    if(icon != null)
      logo = new JLabel(icon);
    else
      logo = new JLabel();

  }

  public JProgressBar getProgressBar() { return this.progressBar; }
  public JButton getTrackButton() { return this.trackButton; }
  public JButton getSaveParamsButton() { return this.saveParamsButton; }
  public JButton getLoadParamsButton() { return this.loadParamsButton; }

}
