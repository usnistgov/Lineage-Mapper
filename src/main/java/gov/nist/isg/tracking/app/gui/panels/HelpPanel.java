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

package main.java.gov.nist.isg.tracking.app.gui.panels;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.*;

import main.java.gov.nist.isg.tracking.app.gui.HelpDocumentationViewer;


/**
 * Created by mmajursk on 7/7/2014.
 */
public class HelpPanel extends JPanel {

  private static final String documentationURL =
      "https://github.com/usnistgov/Lineage-Mapper/wiki";

  private static final String sourceURL =
      "https://github.com/usnistgov/Lineage-Mapper";


  private static final String aboutUsURL =
      "https://isg.nist.gov";

  private static final String license =
      "<html>This software was developed at the National Institute of Standards and<br>" +
      "Technology by employees of the Federal Government in the course of<br>" +
      "their official duties. Pursuant to title 17 Section 105 of the United<br>" +
      "States Code this software is not subject to copyright protection and is<br>" +
      "in the public domain. This software is an experimental system. NIST<br>" +
      "assumes no responsibility whatsoever for its use by other parties, and<br>" +
      "makes no guarantees, expressed or implied, about its quality, reliability,<br>" +
      "or any other characteristic. We would appreciate acknowledgement if the<br>" +
      "software is used.</html>";

  JButton openHelpButton;
  JLabel link;

  public HelpPanel() {
    super();

    initElements();
    init();
  }

  private void initElements() {
    link = new JLabel("<html><a href=\"\">" + documentationURL + "</a></html>");
    link.setCursor(new Cursor(Cursor.HAND_CURSOR));

    openHelpButton = new JButton("Open Local Help Documentation");

    HelpDocumentationViewer helpDialog = new HelpDocumentationViewer("lineage-mapper-user-guide");
    openHelpButton.addActionListener(helpDialog);

    openHelpButton.setPreferredSize(new Dimension(220, 40));
  }

  private void init() {

    JPanel content = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    c.insets = new Insets(10, 10, 0, 0);
    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.FIRST_LINE_START;

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(openHelpButton);
    content.add(buttonPanel,c);

    c.gridy = 1;
    JLabel aboutUsLink = new JLabel("<html><a href=\"" + aboutUsURL + "\">" + "About " +
        "Lineage-Mapper" + "</a></html>");
    aboutUsLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
    content.add(aboutUsLink, c);

    c.gridy = 2;
    JLabel link = new JLabel("<html><a href=\"" + documentationURL + "\">" + "Online Documentation" + "</a></html>");

    link.setCursor(new Cursor(Cursor.HAND_CURSOR));
    content.add(link, c);

    c.gridy = 3;
    JLabel srclink = new JLabel("<html><a href=\"" + sourceURL +"\">" + "Source Code" + "</a></html>");

    srclink.setCursor(new Cursor(Cursor.HAND_CURSOR));
    content.add(srclink, c);

    c.gridy = 4;
    content.add(new JLabel(license), c);

    goWebsiteDocumentation(link);
    goWebsiteSourceCode(srclink);
    goWebsiteAboutUs(aboutUsLink);


    this.add(content);
  }

  private static void goWebsiteDocumentation(JLabel website) {
    website.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        try {
          Desktop.getDesktop().browse(new URI(documentationURL));
        } catch (URISyntaxException ex) {
        } catch (IOException ex) {

        }
      }
    });
  }

  private static void goWebsiteSourceCode(JLabel website) {
    website.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        try {
          Desktop.getDesktop().browse(new URI(sourceURL));
        } catch (URISyntaxException ex) {
        } catch (IOException ex) {

        }
      }
    });
  }
  private static void goWebsiteAboutUs(JLabel website) {
    website.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        try {
          Desktop.getDesktop().browse(new URI(aboutUsURL));
        } catch (URISyntaxException ex) {
        } catch (IOException ex) {

        }
      }
    });
  }

}
