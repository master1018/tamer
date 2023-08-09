public class Group extends Properties.Entity implements ChangedEventProvider<Group> {
    private List<InputGraph> graphs;
    private transient ChangedEvent<Group> changedEvent;
    private GraphDocument document;
    private InputMethod method;
    private String assembly;
    public Group() {
        graphs = new ArrayList<InputGraph>();
        init();
    }
    private void init() {
        changedEvent = new ChangedEvent<Group>(this);
    }
    public void fireChangedEvent() {
        changedEvent.fire();
    }
    public void setAssembly(String s) {
        this.assembly = s;
    }
    public String getAssembly() {
        return assembly;
    }
    public void setMethod(InputMethod method) {
        this.method = method;
    }
    public InputMethod getMethod() {
        return method;
    }
    void setDocument(GraphDocument document) {
        this.document = document;
    }
    public GraphDocument getDocument() {
        return document;
    }
    public ChangedEvent<Group> getChangedEvent() {
        return changedEvent;
    }
    public List<InputGraph> getGraphs() {
        return Collections.unmodifiableList(graphs);
    }
    public void addGraph(InputGraph g) {
        assert g != null;
        assert !graphs.contains(g);
        graphs.add(g);
        changedEvent.fire();
    }
    public void removeGraph(InputGraph g) {
        int index = graphs.indexOf(g);
        if (index != -1) {
            graphs.remove(g);
            changedEvent.fire();
        }
    }
    public Set<Integer> getAllNodes() {
        Set<Integer> result = new HashSet<Integer>();
        for (InputGraph g : graphs) {
            Set<Integer> ids = g.getNodesAsSet();
            result.addAll(g.getNodesAsSet());
            for (Integer i : ids) {
                result.add(-i);
            }
        }
        return result;
    }
    public InputGraph getLastAdded() {
        if (graphs.size() == 0) {
            return null;
        }
        return graphs.get(graphs.size() - 1);
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Group " + getProperties().toString() + "\n");
        for (InputGraph g : graphs) {
            sb.append(g.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
    public String getName() {
        return getProperties().get("name");
    }
}
