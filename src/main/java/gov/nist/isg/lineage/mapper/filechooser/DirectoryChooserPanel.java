// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package main.java.gov.nist.isg.lineage.mapper.filechooser;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.java.gov.nist.isg.lineage.mapper.textfield.ValidatedTextField;
import main.java.gov.nist.isg.lineage.mapper.textfield.validator.ValidatorFile;

/**
 * DirectoryChooserPanel is used as a wrapper to contain a directory chooser Utility functions for
 * getting the selected directory are available.
 *
 * @author Tim Blattner
 * @version 1.0
 */
public class DirectoryChooserPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private JLabel label;
  private JButton button;

  private ValidatedTextField<File> input;


  /**
   * Creates a directory chooser
   *
   * @param label       the label associated with the directory chooser
   * @param defLocation the default directory
   * @param sz          the size of the text field
   */
  public DirectoryChooserPanel(String label, String defLocation, int sz) {
    super(new FlowLayout(FlowLayout.CENTER));

    File f = new File(defLocation);
    f.mkdirs();

    this.label = new JLabel(label);
    this.input = new ValidatedTextField<File>(sz, defLocation, new ValidatorFile());


    this.button = new JButton("Browse");

    this.input.setToolTipText(label);
    this.button.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        JFileChooser chooser = new JFileChooser(input.getText());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int val = chooser.showOpenDialog(button);
        if (val == JFileChooser.APPROVE_OPTION) {
          input.setText(chooser.getSelectedFile().getAbsolutePath());
        }

      }
    });

    add(this.label);
    add(this.input);
    add(this.button);
    this.hasError();
  }

  @Override
  public void setEnabled(boolean val) {
    label.setEnabled(val);
    input.setEnabled(val);
    button.setEnabled(val);
    super.setEnabled(val);
  }

  /**
   * Creates a directory chooser
   *
   * @param label       the label associated with the directory chooser
   * @param defLocation the default directory
   */
  public DirectoryChooserPanel(String label, String defLocation) {
    this(label, defLocation, 20);
  }

  /**
   * Creates a directory chooser with the default location being user.home
   *
   * @param label the label associated with the directory chooser
   */
  public DirectoryChooserPanel(String label) {
    this(label, System.getProperty("user.home"));
  }

  /**
   * Creates a directory chooser with the default location being user.home
   *
   * @param label the label associated with the directory chooser
   * @param sz    the size of the text field
   */
  public DirectoryChooserPanel(String label, int sz) {
    this(label, System.getProperty("user.home"), sz);
  }

  /**
   * Gets the input text field for the directory chooser
   *
   * @return the input text field
   */
  public JTextField getInputField() {
    return input;
  }


  public boolean hasError() {
    return input.hasError();
  }

  /**
   * Shows an error for the text field
   */
  public void showError() {
    input.setBackground(Color.RED);
  }

  /**
   * Hides an error for the text field
   */
  public void hideError() {
    input.setBackground(Color.WHITE);
  }

  /**
   * Sets the value for the text field
   *
   * @param value the value
   */
  public void setValue(String value) {
    input.setText(value);
  }

  /**
   * Gets the value of the text field
   *
   * @return the text field value
   */
  public String getValue() {
    return input.getText();
  }

}
