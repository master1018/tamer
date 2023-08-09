public class GraphViewerImplementation implements GraphViewer {
    public void view(InputGraph graph) {
        Diagram diagram = Diagram.createDiagram(graph, Settings.get().get(Settings.NODE_TEXT, Settings.NODE_TEXT_DEFAULT));
        EditorTopComponent tc = new EditorTopComponent(diagram);
        tc.open();
        tc.requestActive();
    }
}
