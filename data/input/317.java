public class Graph extends BasicGraph implements GraphI {
    private static final String NEW_LINE = System.getProperty("line.separator");
    private HashMap<String, BasicGraphI> nodes = new HashMap<String, BasicGraphI>();
    private ArrayList<EdgeI> edges = new ArrayList<EdgeI>();
    private HashSet visited = new HashSet();
    public void addNode(BasicGraphI aNode) {
        nodes.put(aNode.getAc(), aNode);
    }
    public BasicGraphI addNode(Interactor anInteractor) {
        BasicGraphI node = nodes.get(anInteractor.getAc());
        if (null == node) {
            node = new Node(anInteractor);
            this.addNode(node);
        }
        return node;
    }
    public void addEdge(EdgeI anEdge) {
        if (!edges.contains(anEdge)) edges.add(anEdge);
    }
    public HashMap<String, BasicGraphI> getNodes() {
        return nodes;
    }
    public Collection<EdgeI> getEdges() {
        return edges;
    }
    public void addVisited(BasicObject anElement) {
        visited.add(anElement.getAc());
    }
    public boolean isVisited(BasicObject anElement) {
        return visited.contains(anElement.getAc());
    }
    public String toString() {
        StringBuffer s = new StringBuffer();
        final int count = edges.size();
        s.append("Graph[" + count + "]").append(NEW_LINE);
        for (int i = 0; i < count; i++) {
            EdgeI e = edges.get(i);
            s.append(e.getNode1().getAc());
            s.append('(');
            s.append(e.getComponent1().getCvComponentRole().getShortLabel());
            s.append(')');
            s.append("-> ");
            s.append(e.getNode2().getAc());
            s.append('(');
            s.append(e.getComponent2().getCvComponentRole().getShortLabel());
            s.append(')');
            s.append(NEW_LINE);
        }
        return s.toString();
    }
}
