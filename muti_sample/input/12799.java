public class SimpleTreeTableModel extends SimpleTreeModel implements TreeTableModel {
  private boolean valuesEditable = true;
  public int getColumnCount() {
    return 2;
  }
  public String getColumnName(int column) {
    switch (column) {
    case 0: return "Name";
    case 1: return "Value";
    default: throw new RuntimeException("Index " + column + " out of bounds");
    }
  }
  public Class getColumnClass(int column) {
    switch (column) {
    case 0: return TreeTableModel.class;
    case 1: return String.class;
    default: throw new RuntimeException("Index " + column + " out of bounds");
    }
  }
  public Object getValueAt(Object node, int column) {
    SimpleTreeNode realNode = (SimpleTreeNode) node;
    switch (column) {
    case 0: return realNode.getName();
    case 1: return realNode.getValue();
    default: throw new RuntimeException("Index " + column + " out of bounds");
    }
  }
  public boolean isCellEditable(Object node, int column) {
    switch (column) {
    case 0: return true;
    case 1: return valuesEditable;
    default: throw new RuntimeException("Index " + column + " out of bounds");
    }
  }
  public void setValueAt(Object aValue, Object node, int column) {
    throw new RuntimeException("FIXME: figure out how to handle editing of SimpleTreeNodes");
  }
  public boolean getValuesEditable() {
    return valuesEditable;
  }
  public void setValuesEditable(boolean val) {
    valuesEditable = val;
  }
}
