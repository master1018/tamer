public class Source {
    private List<InputNode> sourceNodes;
    private Set<Integer> set;
    public Source() {
        sourceNodes = new ArrayList<InputNode>(1);
    }
    public List<InputNode> getSourceNodes() {
        return Collections.unmodifiableList(sourceNodes);
    }
    public Set<Integer> getSourceNodesAsSet() {
        if (set == null) {
            set = new HashSet<Integer>();
            for (InputNode n : sourceNodes) {
                int id = n.getId();
                set.add(id);
            }
        }
        return set;
    }
    public void addSourceNode(InputNode n) {
        sourceNodes.add(n);
        set = null;
    }
    public void removeSourceNode(InputNode n) {
        sourceNodes.remove(n);
        set = null;
    }
    public interface Provider {
        public Source getSource();
    }
    public void setSourceNodes(List<InputNode> sourceNodes) {
        this.sourceNodes = sourceNodes;
        set = null;
    }
    public void addSourceNodes(Source s) {
        for (InputNode n : s.getSourceNodes()) {
            sourceNodes.add(n);
        }
        set = null;
    }
    public boolean isInBlock(InputGraph g, InputBlock blockNode) {
        for (InputNode n : this.getSourceNodes()) {
            if (g.getBlock(n) == blockNode) {
                return true;
            }
        }
        return false;
    }
}
