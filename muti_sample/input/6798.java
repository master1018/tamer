public final class javax_swing_JTree extends AbstractTest<JTree> {
    public static void main(String[] args) {
        new javax_swing_JTree().test(true);
    }
    protected JTree getObject() {
        return new JTree(new MyModel());
    }
    protected JTree getAnotherObject() {
        return new JTree();
    }
    protected void validate(JTree before, JTree after) {
        Class type = after.getModel().getClass();
        if (!type.equals(before.getModel().getClass()))
            throw new Error("Invalid model: " + type);
    }
    public static final class MyModel implements TreeModel {
        public Object getRoot() {
            return null;
        }
        public Object getChild(Object parent, int index) {
            return null;
        }
        public int getChildCount(Object parent) {
            return 0;
        }
        public boolean isLeaf(Object node) {
            return false;
        }
        public void valueForPathChanged(TreePath path, Object newValue) {
        }
        public int getIndexOfChild(Object parent, Object child) {
            return 0;
        }
        public void addTreeModelListener(TreeModelListener listener) {
        }
        public void removeTreeModelListener(TreeModelListener listener) {
        }
    }
}
