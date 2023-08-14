public class RevPtrsTreeNodeAdapter extends FieldTreeNodeAdapter {
  private static FieldIdentifier fid = new NamedFieldIdentifier("_revPtrs");
  private List children;
  public RevPtrsTreeNodeAdapter(Oop oop) {
    this(oop, false);
  }
  public RevPtrsTreeNodeAdapter(Oop oop, boolean treeTableMode) {
    super(fid, treeTableMode);
    children = VM.getVM().getRevPtrs().get(oop);
  }
  public int getChildCount() {
    return children != null ? children.size() : 0;
  }
  public SimpleTreeNode getChild(int index) {
    LivenessPathElement lpe = (LivenessPathElement)children.get(index);
    IndexableFieldIdentifier ifid = new IndexableFieldIdentifier(index);
    Oop oop = lpe.getObj();
    if (oop != null) {
      return new OopTreeNodeAdapter(oop, ifid, getTreeTableMode());
    } else {
      NamedFieldIdentifier nfi = (NamedFieldIdentifier)lpe.getField();
      return new RootTreeNodeAdapter(nfi.getName(), ifid, getTreeTableMode());
    }
  }
  public boolean isLeaf() {
    return false;
  }
  public int getIndexOfChild(SimpleTreeNode child) {
    FieldIdentifier id = ((FieldTreeNodeAdapter) child).getID();
    IndexableFieldIdentifier ifid = (IndexableFieldIdentifier)id;
    return ifid.getIndex();
  }
  public String getName()  { return "<<Reverse pointers>>"; }
  public String getValue() { return ""; }
}
