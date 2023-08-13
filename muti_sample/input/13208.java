public final class javax_swing_tree_DefaultTreeModel extends AbstractTest<DefaultTreeModel> {
    public static void main(String[] args) {
        new javax_swing_tree_DefaultTreeModel().test(true);
    }
    protected DefaultTreeModel getObject() {
        return new DefaultTreeModel(new RootNode());
    }
    protected DefaultTreeModel getAnotherObject() {
        return null; 
    }
    public static final class RootNode implements TreeNode {
        public TreeNode getChildAt(int childIndex) {
            return null;
        }
        public int getChildCount() {
            return 0;
        }
        public TreeNode getParent() {
            return null;
        }
        public int getIndex(TreeNode node) {
            return 0;
        }
        public boolean getAllowsChildren() {
            return false;
        }
        public boolean isLeaf() {
            return false;
        }
        public Enumeration children() {
            return null;
        }
    }
}
