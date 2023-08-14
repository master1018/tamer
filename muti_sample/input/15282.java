public class SampleTreeModel extends DefaultTreeModel {
    public SampleTreeModel(TreeNode newRoot) {
        super(newRoot);
    }
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        DefaultMutableTreeNode aNode = (DefaultMutableTreeNode) path.
                getLastPathComponent();
        SampleData sampleData = (SampleData) aNode.getUserObject();
        sampleData.setString((String) newValue);
        sampleData.setColor(Color.green);
        nodeChanged(aNode);
    }
}
