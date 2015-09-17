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

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class MetadataTableModel extends AbstractTableModel {

  private static final long serialVersionUID = 1L;

  protected Vector<Number> data;
  protected Vector<String> columnNames;

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
    if(aValue instanceof Number && data != null) {
      data.set((row*getColumnCount() + col), (Number)aValue);
      fireTableCellUpdated(row,col);
    }
  }
}