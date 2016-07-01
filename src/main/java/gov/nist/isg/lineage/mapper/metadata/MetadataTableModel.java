// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package gov.nist.isg.lineage.mapper.metadata;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * Class to hold and manage a metadata table
 */
public class MetadataTableModel extends AbstractTableModel {

  private static final long serialVersionUID = 1L;

  protected Vector<Number> data;
  protected Vector<String> columnNames;

  /**
   * Create a new Metadata Table given the column names and the vector of data
   * @param columnNames the names of the data columns
   * @param data the data to be held in the table
   */
  public MetadataTableModel(Vector<String> columnNames, Vector<Number> data) {
    this.columnNames = columnNames;
    this.data = data;
  }

  public void setData(Vector<Number> data) {
    this.data = data;
  }

  public int getRowCount() {
    return data.size() / getColumnCount();
  }

  public int getColumnCount() {
    return columnNames.size();
  }

  public String getColumnName(int columnIndex) {
    String colName = "";

    if (columnIndex >= 0 && columnIndex <= getColumnCount()) {
      colName = columnNames.elementAt(columnIndex);
    }

    return colName;
  }

  public Class<Number> getColumnClass(int columnIndex) {
    return Number.class;
  }

  public boolean isCellEditable(int row, int col) {
    return false;
  }

  public Object getValueAt(int row, int col) {
    return data.elementAt((row * getColumnCount()) + col);
  }

  public void setValueAt(Object aValue, int row, int col) {
    if (aValue instanceof Number && data != null) {
      data.set((row * getColumnCount() + col), (Number) aValue);
      fireTableCellUpdated(row, col);
    }
  }
}