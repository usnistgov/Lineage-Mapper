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


package main.java.gov.nist.isg.tracking.metadata;

import java.awt.*;

import javax.swing.*;

import main.java.gov.nist.isg.tracking.app.TrackingAppParams;

public class MetadataDisplayFrame extends JFrame {

  public MetadataDisplayFrame(String title, JTable table) {
    super(title);

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());

    JScrollPane sp = new JScrollPane(table);
    panel.add(sp, BorderLayout.CENTER);

    this.add(panel);
    this.setPreferredSize(new Dimension(120*table.getModel().getColumnCount(), 400));
    this.setLocationRelativeTo(TrackingAppParams.getInstance().getGuiPane());
    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    this.setSize(this.getPreferredSize());
    this.setVisible(true);

  }
}