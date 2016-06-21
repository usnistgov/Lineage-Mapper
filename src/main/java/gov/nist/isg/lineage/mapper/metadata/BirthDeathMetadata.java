// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package main.java.gov.nist.isg.lineage.mapper.metadata;

import java.util.Vector;

import javax.swing.JTable;

import main.java.gov.nist.isg.lineage.mapper.app.TrackingAppParams;


public class BirthDeathMetadata {

  private JTable table = null;
  private static final String fileName = "birth-death.csv";

  public static String getFileName() {
    return fileName;
  }

  private TrackingAppParams params;

  public BirthDeathMetadata(TrackingAppParams params) {
    this.params = params;
  }

  public void buildMetadataTable() {
    if (table != null) return;

    int[] birth = CellTrackerMetadata.generateBirthMatrix(params);
    int[] death = CellTrackerMetadata.generateDeathMatrix(params);
    if (birth == null) return;
    if (death == null) return;
    if (birth.length != death.length) return;

    Vector<Number> data = new Vector<Number>();
    for (int i = 1; i < birth.length; i++) {
      data.add(i);
      data.add(birth[i] + 1); // +1 is to convert from zero based to 1 based
      data.add(death[i] + 1); // +1 is to convert from zero based to 1 based
    }

    Vector<String> colNames = new Vector<String>();
    colNames.add(0, "Cell ID");
    colNames.add(1, "Birth Frame");
    colNames.add(2, "Death Frame");

    MetadataTableModel tableModel = new MetadataTableModel(colNames, data);
    table = new JTable();
    table.setModel(tableModel);
    table.createDefaultColumnsFromModel();
  }

  public JTable getTable() {
    return table;
  }

}
