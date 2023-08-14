public class IIOInvalidTreeException extends IIOException {
    protected Node offendingNode = null;
    public IIOInvalidTreeException(String message, Node offendingNode) {
        super(message);
        this.offendingNode = offendingNode;
    }
    public IIOInvalidTreeException(String message, Throwable cause, Node offendingNode) {
        super(message, cause);
        this.offendingNode = offendingNode;
    }
    public Node getOffendingNode() {
        return offendingNode;
    }
}
