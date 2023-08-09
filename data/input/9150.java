public final class javax_swing_tree_TreePath extends AbstractTest<TreePath> {
    public static void main(String[] args) {
        new javax_swing_tree_TreePath().test(true);
    }
    protected TreePath getObject() {
        return new TreePath("SinglePath");
    }
    protected TreePath getAnotherObject() {
        return new TreePath(new String[] {"First", "Second"});
    }
}
