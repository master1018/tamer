public class ViewsTreeModel implements TreeModel {
    private final ViewNode root;
    public ViewsTreeModel(ViewNode root) {
        this.root = root;
    }
    public Object getRoot() {
        return root;
    }
    public Object getChild(Object o, int i) {
        return ((ViewNode) o).children.get(i);
    }
    public int getChildCount(Object o) {
        return ((ViewNode) o).children.size();
    }
    public boolean isLeaf(Object child) {
        ViewNode node = (ViewNode) child;
        return node.children == null || node.children.size() == 0;
    }
    public void valueForPathChanged(TreePath treePath, Object child) {
    }
    public int getIndexOfChild(Object parent, Object child) {
        return ((ViewNode) parent).children.indexOf(child);
    }
    public void addTreeModelListener(TreeModelListener treeModelListener) {
    }
    public void removeTreeModelListener(TreeModelListener treeModelListener) {
    }
}
