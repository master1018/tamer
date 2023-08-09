public class AddressTreeNodeAdapter extends FieldTreeNodeAdapter {
  private Address val;
  public AddressTreeNodeAdapter(Address val, FieldIdentifier id) {
    this(val, id, false);
  }
  public AddressTreeNodeAdapter(Address val, FieldIdentifier id, boolean treeTableMode) {
    super(id, treeTableMode);
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
    if (val != null) {
      return val.toString();
    }
    return "NULL";
  }
}
