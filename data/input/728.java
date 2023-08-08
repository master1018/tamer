public class GraphEventMulticaster extends EventMulticaster implements GraphEventListener {
    public void nodeAdded(Graph g, Node n) {
        ((GraphEventListener) a).nodeAdded(g, n);
        ((GraphEventListener) b).nodeAdded(g, n);
    }
    public void nodeRemoved(Graph g, Node n) {
        ((GraphEventListener) a).nodeRemoved(g, n);
        ((GraphEventListener) b).nodeRemoved(g, n);
    }
    public void nodeReplaced(Graph g, Node o, Node n) {
        ((GraphEventListener) a).nodeReplaced(g, o, n);
        ((GraphEventListener) b).nodeReplaced(g, o, n);
    }
    public void edgeAdded(Graph g, Edge e) {
        ((GraphEventListener) a).edgeAdded(g, e);
        ((GraphEventListener) b).edgeAdded(g, e);
    }
    public void edgeRemoved(Graph g, Edge e) {
        ((GraphEventListener) a).edgeRemoved(g, e);
        ((GraphEventListener) b).edgeRemoved(g, e);
    }
    public void edgeReplaced(Graph g, Edge o, Edge n) {
        ((GraphEventListener) a).edgeReplaced(g, o, n);
        ((GraphEventListener) b).edgeReplaced(g, o, n);
    }
    public static GraphEventListener add(GraphEventListener a, GraphEventListener b) {
        return (GraphEventListener) addInternal(a, b);
    }
    public static GraphEventListener remove(GraphEventListener a, GraphEventListener b) {
        return (GraphEventListener) removeInternal(a, b);
    }
    protected static EventListener addInternal(EventListener a, EventListener b) {
        if (a == null) return b;
        if (b == null) return a;
        return new GraphEventMulticaster(a, b);
    }
    protected EventListener remove(EventListener oldl) {
        if (oldl == a) return b;
        if (oldl == b) return a;
        EventListener a2 = removeInternal(a, oldl);
        EventListener b2 = removeInternal(b, oldl);
        if (a2 == a && b2 == b) {
            return this;
        }
        return addInternal(a2, b2);
    }
    protected GraphEventMulticaster(EventListener a, EventListener b) {
        super(a, b);
    }
}
