public abstract class LeafNodeImpl extends NodeImpl {
    InnerNodeImpl parent;
    int index;
    LeafNodeImpl(DocumentImpl document) {
        super(document);
    }
    public Node getNextSibling() {
        if (parent == null || index + 1 >= parent.children.size()) {
            return null;
        }
        return parent.children.get(index + 1);
    }
    public Node getParentNode() {
        return parent;
    }
    public Node getPreviousSibling() {
        if (parent == null || index == 0) {
            return null;
        }
        return parent.children.get(index - 1);
    }
    boolean isParentOf(Node node) {
        return false;
    }
}
