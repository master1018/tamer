public class EditorInputGraphProvider implements InputGraphProvider {
    public InputGraph getGraph() {
        EditorTopComponent e = EditorTopComponent.getActive();
        if (e == null) {
            return null;
        }
        return e.getDiagramModel().getGraphToView();
    }
    public void setSelectedNodes(Set<InputNode> nodes) {
        EditorTopComponent e = EditorTopComponent.getActive();
        if (e != null) {
            e.setSelectedNodes(nodes);
        }
    }
}
