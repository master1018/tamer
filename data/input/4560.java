public class CheckNodeListModel extends NodeListModel {
    private Node rootNode;
    @Override
    public void setNode(Node rootNode) {
        this.rootNode = rootNode;
        super.setNode(rootNode);
    }
    public CheckNode getCheckNodeAt(int index) {
        return (CheckNode) rootNode.getChildren().getNodes()[index];
    }
}
