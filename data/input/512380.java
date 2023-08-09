public abstract class InnerNodeImpl extends LeafNodeImpl {
    List<LeafNodeImpl> children = new ArrayList<LeafNodeImpl>();
    protected InnerNodeImpl(DocumentImpl document) {
        super(document);
    }
    public Node appendChild(Node newChild) throws DOMException {
        return insertChildAt(newChild, children.size());
    }
    public NodeList getChildNodes() {
        NodeListImpl list = new NodeListImpl();
        for (NodeImpl node : children) {
            list.add(node);
        }
        return list;
    }
    public Node getFirstChild() {
        return (!children.isEmpty() ? children.get(0) : null);
    }
    public Node getLastChild() {
        return (!children.isEmpty() ? children.get(children.size() - 1) : null);
    }
    public Node getNextSibling() {
        if (parent == null || index + 1 >= parent.children.size()) {
            return null;
        }
        return parent.children.get(index + 1);
    }
    public boolean hasChildNodes() {
        return children.size() != 0;
    }
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        LeafNodeImpl refChildImpl = (LeafNodeImpl) refChild;
        if (refChildImpl.document != document) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }
        if (refChildImpl.parent != this) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
        }
        return insertChildAt(newChild, refChildImpl.index);
    }
    public Node insertChildAt(Node newChild, int index) throws DOMException {
        LeafNodeImpl newChildImpl = (LeafNodeImpl) newChild;
        if (document != null && newChildImpl.document != null && newChildImpl.document != document) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }
        if (newChildImpl.isParentOf(this)) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
        }
        if (newChildImpl.parent != null) {
            int oldIndex = newChildImpl.index;
            newChildImpl.parent.children.remove(oldIndex);
            newChildImpl.parent.refreshIndices(oldIndex);
        }
        children.add(index, newChildImpl);
        newChildImpl.parent = this;
        refreshIndices(index);
        return newChild;
    }
    public boolean isParentOf(Node node) {
        LeafNodeImpl nodeImpl = (LeafNodeImpl) node;
        while (nodeImpl != null) {
            if (nodeImpl == this) {
                return true;
            }
            nodeImpl = nodeImpl.parent;
        }
        return false;
    }
    @Override
    public final void normalize() {
        Node next;
        for (Node node = getFirstChild(); node != null; node = next) {
            next = node.getNextSibling();
            node.normalize();
            if (node.getNodeType() == Node.TEXT_NODE) {
                ((TextImpl) node).minimize();
            }
        }
    }
    private void refreshIndices(int fromIndex) {
        for (int i = fromIndex; i < children.size(); i++) {
            children.get(i).index = i;
        }
    }
    public Node removeChild(Node oldChild) throws DOMException {
        LeafNodeImpl oldChildImpl = (LeafNodeImpl) oldChild;
        if (oldChildImpl.document != document) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }
        if (oldChildImpl.parent != this) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
        }
        int index = oldChildImpl.index;
        children.remove(index);
        oldChildImpl.parent = null;
        refreshIndices(index);
        return oldChild;
    }
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        LeafNodeImpl oldChildImpl = (LeafNodeImpl) oldChild;
        LeafNodeImpl newChildImpl = (LeafNodeImpl) newChild;
        if (oldChildImpl.document != document
                || newChildImpl.document != document) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }
        if (oldChildImpl.parent != this || newChildImpl.isParentOf(this)) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
        }
        int index = oldChildImpl.index;
        children.set(index, newChildImpl);
        oldChildImpl.parent = null;
        newChildImpl.parent = this;
        refreshIndices(index);
        return oldChildImpl;
    }
    public String getTextContent() throws DOMException {
        Node child = getFirstChild();
        if (child == null) {
            return "";
        }
        Node next = child.getNextSibling();
        if (next == null) {
            return hasTextContent(child) ? child.getTextContent() : "";
        }
        StringBuilder buf = new StringBuilder();
        getTextContent(buf);
        return buf.toString();
    }
    void getTextContent(StringBuilder buf) throws DOMException {
        Node child = getFirstChild();
        while (child != null) {
            if (hasTextContent(child)) {
                ((NodeImpl) child).getTextContent(buf);
            }
            child = child.getNextSibling();
        }
    }
    final boolean hasTextContent(Node child) {
        return child.getNodeType() != Node.COMMENT_NODE
                && child.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE;
    }
}
