public class SimpleTreeGroupNode implements SimpleTreeNode {
  private List children;
  public SimpleTreeGroupNode() {
    children = new ArrayList();
  }
  public int getChildCount() { return children.size(); }
  public SimpleTreeNode getChild(int index) {
    return (SimpleTreeNode) children.get(index);
  }
  public void addChild(SimpleTreeNode child) {
    children.add(child);
  }
  public SimpleTreeNode removeChild(int index) {
    return (SimpleTreeNode) children.remove(index);
  }
  public void removeAllChildren() {
    children.clear();
  }
  public boolean isLeaf() {
    return false;
  }
  public int getIndexOfChild(SimpleTreeNode child) {
    return children.indexOf(child);
  }
  public String getName()  { return null; }
  public String getValue() { return null; }
}
