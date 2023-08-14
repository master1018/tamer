public abstract class FieldTreeNodeAdapter implements SimpleTreeNode {
  private FieldIdentifier id;
  private boolean         treeTableMode;
  public FieldTreeNodeAdapter(FieldIdentifier id, boolean treeTableMode) {
    this.id = id;
    this.treeTableMode = treeTableMode;
  }
  public FieldIdentifier getID() {
    return id;
  }
  public boolean getTreeTableMode() {
    return treeTableMode;
  }
  public Type getType() {
    return getID().getType();
  }
  public String getName() {
    if (getID() != null) {
      return getID().toString();
    }
    return "";
  }
  public String toString() {
    if (treeTableMode) {
      return getName();
    } else {
      if (getID() != null) {
        return getName() + ": " + getValue();
      } else {
        return getValue();
      }
    }
  }
}
