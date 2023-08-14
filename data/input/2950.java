public class BadOopTreeNodeAdapter extends FieldTreeNodeAdapter {
  private OopHandle oop;
  public BadOopTreeNodeAdapter(OopHandle oop, FieldIdentifier id) {
    this(oop, id, false);
  }
  public BadOopTreeNodeAdapter(OopHandle oop, FieldIdentifier id, boolean treeTableMode) {
    super(id, treeTableMode);
    this.oop = oop;
  }
  public int getChildCount() {
    return 0;
  }
  public SimpleTreeNode getChild(int index) {
    throw new RuntimeException("Should not call this");
  }
  public boolean isLeaf() {
    return true;
  }
  public int getIndexOfChild(SimpleTreeNode child) {
    throw new RuntimeException("Should not call this");
  }
  public String getValue() {
    return "** BAD OOP " + oop + " **";
  }
}
