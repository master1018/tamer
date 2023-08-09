public class GraphImpl extends AbstractSet implements Graph
{
    private Map  nodeToData ;
    public GraphImpl()
    {
        nodeToData = new HashMap() ;
    }
    public GraphImpl( Collection coll )
    {
        this() ;
        addAll( coll ) ;
    }
    public boolean add( Object obj ) 
    {
        if (!(obj instanceof Node))
            throw new IllegalArgumentException( "Graphs must contain only Node instances" ) ;
        Node node = (Node)obj ;
        boolean found = nodeToData.keySet().contains( obj ) ;
        if (!found) {
            NodeData nd = new NodeData() ;
            nodeToData.put( node, nd ) ;
        }
        return !found ;
    }
    public Iterator iterator()
    {
        return nodeToData.keySet().iterator() ;
    }
    public int size()
    {
        return nodeToData.keySet().size() ;
    }
    public NodeData getNodeData( Node node )
    {
        return (NodeData)nodeToData.get( node ) ;
    }
    private void clearNodeData()
    {
        Iterator iter = nodeToData.entrySet().iterator() ;
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next() ;
            NodeData nd = (NodeData)(entry.getValue()) ;
            nd.clear( ) ;
        }
    }
    interface NodeVisitor
    {
        void visit( Graph graph, Node node, NodeData nd ) ;
    }
    void visitAll( NodeVisitor nv )
    {
        boolean done = false ;
        do {
            done = true ;
            Map.Entry[] entries =
                (Map.Entry[])nodeToData.entrySet().toArray( new Map.Entry[0] ) ;
            for (int ctr=0; ctr<entries.length; ctr++) {
                Map.Entry current = entries[ctr] ;
                Node node = (Node)current.getKey() ;
                NodeData nd = (NodeData)current.getValue() ;
                if (!nd.isVisited()) {
                    nd.visited() ;
                    done = false ;
                    nv.visit( this, node, nd ) ;
                }
            }
        } while (!done) ;
    }
    private void markNonRoots()
    {
        visitAll(
            new NodeVisitor() {
                public void visit( Graph graph, Node node, NodeData nd )
                {
                    Iterator iter = node.getChildren().iterator() ; 
                    while (iter.hasNext()) {
                        Node child = (Node)iter.next() ;
                        graph.add( child ) ;
                        NodeData cnd = graph.getNodeData( child ) ;
                        cnd.notRoot() ;
                    }
                }
            } ) ;
    }
    private Set collectRootSet()
    {
        final Set result = new HashSet() ;
        Iterator iter = nodeToData.entrySet().iterator() ;
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next() ;
            Node node = (Node)entry.getKey() ;
            NodeData nd = (NodeData)entry.getValue() ;
            if (nd.isRoot())
                result.add( node ) ;
        }
        return result ;
    }
    public Set  getRoots()
    {
        clearNodeData() ;
        markNonRoots() ;
        return collectRootSet() ;
    }
}
