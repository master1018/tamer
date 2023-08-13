public class EnumTreeNodeAdapter extends FieldTreeNodeAdapter {
  private long val;
  private String enumName;
  public EnumTreeNodeAdapter(String enumName, long val, FieldIdentifier id) {
    this(enumName, val, id, false);
  }
  public EnumTreeNodeAdapter(String enumName, long val, FieldIdentifier id, boolean treeTableMode) {
    super(id, treeTableMode);
    this.enumName = enumName;
    this.val = val;
  }
  public int getChildCount() {
    return 0;
  }
  public SimpleTreeNode getChild(int index) {
    return null;
  }
  public boolean isLeaf() {
    return true;
  }
  public int getIndexOfChild(SimpleTreeNode child) {
    return 0;
  }
  public String getValue() {
    if (enumName != null) {
      return enumName;
    } else {
      return Long.toString(val);
    }
  }
}
