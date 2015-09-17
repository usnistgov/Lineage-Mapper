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


package main.java.gov.nist.isg.tracking.app.utils.useractions;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.*;


/**
 * USAGE:
 *
 * ChooseDirectoryAction action = new ChooseDirectoryAction(frame, new JFileChooser(new File(".")))
 * {
 *
 * @Override protected void doWithSelectedDir(File file) { // do what you want here } }; JButton
 * button = new JButton(action);
 */

public abstract class ChooseDirectoryAction extends AbstractAction {


  private static final long serialVersionUID = 1L;
  private Component c;
  private JFileChooser chooser;

  public ChooseDirectoryAction(Component c, JFileChooser chooser) {
    super("Browse...");
    this.chooser = chooser;
    this.c = c;
    this.chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
  }

  public ChooseDirectoryAction(Component c, JFileChooser chooser, String chooser_title) {
    super(chooser_title);
    this.chooser = chooser;
    this.c = c;
    this.chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
  }


  public void actionPerformed(ActionEvent evt) {
    int option = chooser.showOpenDialog(this.c);

    if (option == JFileChooser.APPROVE_OPTION) {
      File selected_dir = chooser.getSelectedFile();
      doWithSelectedDir(selected_dir);
    }
  }

  protected abstract void doWithSelectedDir(File f);

}