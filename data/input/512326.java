public class DOMSource implements Source {
    private Node node;
    private String systemID;
    public static final String FEATURE =
        "http:
    public DOMSource() { }
    public DOMSource(Node n) {
        setNode(n);
    }
    public DOMSource(Node node, String systemID) {
        setNode(node);
        setSystemId(systemID);
    }
    public void setNode(Node node) {
        this.node = node;
    }
    public Node getNode() {
        return node;
    }
    public void setSystemId(String systemID) {
        this.systemID = systemID;
    }
    public String getSystemId() {
        return this.systemID;
    }
}
