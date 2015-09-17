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


package main.java.gov.nist.isg.tracking.app.gui;


import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import main.java.gov.nist.isg.tracking.app.TrackingAppParams;
import main.java.gov.nist.isg.tracking.app.gui.panels.AdvancedPanel;
import main.java.gov.nist.isg.tracking.app.gui.panels.ControlPanel;
import main.java.gov.nist.isg.tracking.app.gui.panels.HelpPanel;
import main.java.gov.nist.isg.tracking.app.gui.panels.InputPanel;
import main.java.gov.nist.isg.tracking.app.gui.panels.OutputPanel;
import main.java.gov.nist.isg.tracking.app.images.AppImageHelper;
import main.java.gov.nist.isg.tracking.lib.Log;
import main.java.gov.nist.isg.tracking.metadata.MetadataDisplayFrame;


public class CellTrackerGUI extends JFrame {

  InputPanel inputPanel;
  OutputPanel outputPanel;
  AdvancedPanel advancedPanel;
  ControlPanel controlPanel;

  public CellTrackerGUI(boolean showFlag) {
    super(TrackingAppParams.getAppTitle());
    this.setExtendedState(JFrame.NORMAL);
    this.setPreferredSize(new Dimension(550, 500));
    this.setMinimumSize(new Dimension(550, 500));


    init();
    initToolTips();

    if(showFlag) {
      Log.mandatory("Starting Lineage Mapper");
      // Show the gui
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          dispApp();
        }
      });
    }

  }

  private void init() {

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

    inputPanel = new InputPanel();
    tabbedPane.add("Input", inputPanel);
    outputPanel = new OutputPanel();
    tabbedPane.add("Output", outputPanel);
    advancedPanel = new AdvancedPanel();

    JScrollPane sp = new JScrollPane(advancedPanel);
    sp.getVerticalScrollBar().setUnitIncrement(8);
    sp.setBorder(new EmptyBorder(0,0,0,0));
    tabbedPane.add("Advanced", sp);

    tabbedPane.add("Help", new HelpPanel());

    controlPanel = new ControlPanel();


    this.setLayout(new BorderLayout());
    this.add(tabbedPane, BorderLayout.CENTER);
    this.add(controlPanel, BorderLayout.SOUTH);


    TrackingAppParams.getInstance().setGuiPane(this);
    TrackingAppParams.getInstance().loadPreferences();

  }

  private void initToolTips() {
    ToolTipManager manager = ToolTipManager.sharedInstance();
    manager.setDismissDelay(10000);
    manager.setInitialDelay(500);
    manager.setReshowDelay(500);
    manager.setLightWeightPopupEnabled(true);
  }


  public InputPanel getInputPanel() { return inputPanel; }

  public OutputPanel getOutputPanel() { return outputPanel; }

  public AdvancedPanel getAdvancedPanel() { return advancedPanel; }

  public ControlPanel getControlPanel() { return controlPanel; }

  public void setProgressBar(double val) {
    val = Math.min(val,1.0);
    controlPanel.getProgressBar().setValue((int)(100*val));
  }

  private void dispApp() {
    try {
      setIconImage(AppImageHelper.loadImage("tracker_icon.png").getImage());
    } catch (FileNotFoundException ignored) {}

    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        performExit();
        super.windowClosing(e);
      }
    });

//        setBounds(app_bounds);
    setLocationRelativeTo(null);
    setVisible(true);
  }

  public boolean hasError() {
    return this.getInputPanel().hasError() || this.getOutputPanel().hasError() || this.getAdvancedPanel().hasError();
  }


  public void performExit() {
    // controls the closing of data windows associated with this plugin when the plugin window is closed
    TrackingAppParams params = TrackingAppParams.getInstance();

    MetadataDisplayFrame temp;
    if (params.getBirthDeathMetadata() != null && params.getBirthDeathMetadata().getTableFrame() != null) {
      temp = params.getBirthDeathMetadata().getTableFrame();
      temp.dispatchEvent(new WindowEvent(temp, WindowEvent.WINDOW_CLOSING));
    }

    if (params.getDivisionMetadata() != null && params.getDivisionMetadata().getTableFrame() != null) {
      temp = params.getDivisionMetadata().getTableFrame();
      temp.dispatchEvent(new WindowEvent(temp, WindowEvent.WINDOW_CLOSING));
    }

    if (params.getFusionMetadata() != null && params.getFusionMetadata().getTableFrame() != null) {
      temp = params.getFusionMetadata().getTableFrame();
      temp.dispatchEvent(new WindowEvent(temp, WindowEvent.WINDOW_CLOSING));
    }

    if (params.getConfidenceIndexMetadata() != null && params.getConfidenceIndexMetadata().getTableFrame() != null) {
      temp = params.getConfidenceIndexMetadata().getTableFrame();
      temp.dispatchEvent(new WindowEvent(temp, WindowEvent.WINDOW_CLOSING));
    }

  }




}
