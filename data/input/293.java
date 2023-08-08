public class GrammarGraph extends DefaultDirectedGraph {
    protected StringLabeller nodeLabeller;
    protected EdgeStringLabeller edgeLabeller;
    protected GrammarWeavers crawlers;
    public GrammarGraph(GrammarWeavers crawlers) {
        super();
        nodeLabeller = StringLabeller.getLabeller(this);
        edgeLabeller = EdgeStringLabeller.getLabeller(this);
        this.crawlers = crawlers;
    }
    public Vertex addVertex(Vertex v) {
        if (!(v instanceof GrammarVertex)) {
            System.out.println("Error: the Vertex is no (subclass of) GrammarVertex");
            return null;
        }
        GrammarVertex added = (GrammarVertex) super.addVertex(v);
        String label = Integer.toString(added.getPos());
        try {
            nodeLabeller.setLabel((Vertex) added, label);
        } catch (StringLabeller.UniqueLabelException ule) {
            System.out.println("Remark: Vertex \"" + label + "\" " + "exists already in this graph. This new vertex will" + "be removed again.");
            removeVertex(added);
        }
        return added;
    }
    public Vertex getVertex(Dot dot) {
        String label = dot.toString();
        return nodeLabeller.getVertex(label);
    }
    public Edge addEdge(Edge e) {
        boolean edgeExists = false;
        if (!(e instanceof GrammarEdge)) {
            System.out.println("Error: the Edge is no (subclass of) GrammarEdge");
            return null;
        }
        GrammarEdge added = (GrammarEdge) super.addEdge(e);
        String label = added.toString();
        try {
            edgeLabeller.setLabel((Edge) added, label);
        } catch (EdgeStringLabeller.UniqueLabelException ule) {
            System.out.println("Remark: Edge \"" + label + "\" " + "exists already in this graph. This new edge will " + "be removed again. \nThis warning should be removed.");
            removeEdge(added);
            edgeExists = true;
        }
        if (!edgeExists) {
            if (added.isComplete()) {
                crawlers.getCompleter().complete();
            } else {
                crawlers.getPredictor().predict();
            }
        }
        return added;
    }
    public boolean exists(Edge edge) {
        return (getEdge(edge) != null);
    }
    public Edge getEdge(Edge edge) {
        String label = edge.toString();
        return edgeLabeller.getEdge(label);
    }
}
