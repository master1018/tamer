public abstract class Test4631471 extends AbstractTest {
    public static void main(String[] args) throws Exception {
        main();
        System.setSecurityManager(new SecurityManager());
        main();
    }
    private static void main() throws Exception {
        new Test4631471() {
            protected Object getObject() {
                return getRoot();
            }
        }.test(false);
        new Test4631471() {
            protected Object getObject() {
                return getModel();
            }
        }.test(false);
        new Test4631471() {
            protected Object getObject() {
                return new DefaultTreeModel((TreeNode) getModel().getRoot());
            }
        }.test(false);
        new Test4631471() {
            protected Object getObject() {
                return getTree();
            }
        }.test(false);
    }
    protected final void validate(Object before, Object after) {
    }
    protected final void initialize(XMLEncoder encoder) {
        encoder.setExceptionListener(this);
    }
    public static TreeNode getRoot() {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode first = new DefaultMutableTreeNode("first");
        DefaultMutableTreeNode second = new DefaultMutableTreeNode("second");
        DefaultMutableTreeNode third = new DefaultMutableTreeNode("third");
        first.add(new DefaultMutableTreeNode("1.1"));
        first.add(new DefaultMutableTreeNode("1.2"));
        first.add(new DefaultMutableTreeNode("1.3"));
        second.add(new DefaultMutableTreeNode("2.1"));
        second.add(new DefaultMutableTreeNode("2.2"));
        second.add(new DefaultMutableTreeNode("2.3"));
        third.add(new DefaultMutableTreeNode("3.1"));
        third.add(new DefaultMutableTreeNode("3.2"));
        third.add(new DefaultMutableTreeNode("3.3"));
        node.add(first);
        node.add(second);
        node.add(third);
        return node;
    }
    public static JTree getTree() {
        return new JTree(getRoot());
    }
    public static TreeModel getModel() {
        return getTree().getModel();
    }
}
