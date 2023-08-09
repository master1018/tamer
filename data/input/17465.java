public class HierarchicalGraphLayout<N, E> extends GraphLayout<N, E> {
    public HierarchicalGraphLayout() {
    }
    private class LinkWrapper implements Link {
        private VertexWrapper from;
        private VertexWrapper to;
        public LinkWrapper(VertexWrapper from, VertexWrapper to) {
            this.from = from;
            this.to = to;
        }
        public Port getFrom() {
            return from.getSlot();
        }
        public Port getTo() {
            return to.getSlot();
        }
        public List<Point> getControlPoints() {
            return new ArrayList<Point>();
        }
        public void setControlPoints(List<Point> list) {
        }
    }
    private class VertexWrapper implements Vertex {
        private N node;
        private UniversalGraph<N, E> graph;
        private Port slot;
        private Point position;
        public VertexWrapper(N node, UniversalGraph<N, E> graph) {
            this.node = node;
            this.graph = graph;
            final VertexWrapper vertex = this;
            this.slot = new Port() {
                public Vertex getVertex() {
                    return vertex;
                }
                public Point getRelativePosition() {
                    return new Point((int) (vertex.getSize().getWidth() / 2), (int) (vertex.getSize().getHeight() / 2));
                }
            };
            Widget w = graph.getScene().findWidget(node);
            this.position = w.getPreferredLocation();
        }
        public Cluster getCluster() {
            return null;
        }
        public Dimension getSize() {
            Widget w = graph.getScene().findWidget(node);
            return w.getBounds().getSize();
        }
        public Point getPosition() {
            return position;
        }
        public void setPosition(Point p) {
            HierarchicalGraphLayout.this.setResolvedNodeLocation(graph, node, p);
            position = p;
        }
        public boolean isRoot() {
            return false;
        }
        public int compareTo(Vertex o) {
            VertexWrapper vw = (VertexWrapper) o;
            return node.toString().compareTo(vw.node.toString());
        }
        public Port getSlot() {
            return slot;
        }
    }
    protected void performGraphLayout(UniversalGraph<N, E> graph) {
        Set<LinkWrapper> links = new HashSet<LinkWrapper>();
        Set<VertexWrapper> vertices = new HashSet<VertexWrapper>();
        Map<N, VertexWrapper> vertexMap = new HashMap<N, VertexWrapper>();
        for (N node : graph.getNodes()) {
            VertexWrapper v = new VertexWrapper(node, graph);
            vertexMap.put(node, v);
            vertices.add(v);
        }
        for (E edge : graph.getEdges()) {
            N source = graph.getEdgeSource(edge);
            N target = graph.getEdgeTarget(edge);
            LinkWrapper l = new LinkWrapper(vertexMap.get(source), vertexMap.get(target));
            links.add(l);
        }
        HierarchicalLayoutManager m = new HierarchicalLayoutManager(HierarchicalLayoutManager.Combine.NONE);
        LayoutGraph layoutGraph = new LayoutGraph(links, vertices);
        m.doLayout(layoutGraph);
    }
    protected void performNodesLayout(UniversalGraph<N, E> graph, Collection<N> nodes) {
        throw new UnsupportedOperationException();
    }
}
