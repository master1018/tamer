public class SelectBytecodesCookie implements Node.Cookie {
    private Set<InputNode> nodes;
    public SelectBytecodesCookie(Set<InputNode> nodes) {
        this.nodes = nodes;
    }
    public Set<InputNode> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }
}
