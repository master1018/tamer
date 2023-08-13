public class DOMSubTreeData implements NodeSetData {
    private boolean excludeComments;
    private Iterator ni;
    private Node root;
    public DOMSubTreeData(Node root, boolean excludeComments) {
        this.root = root;
        this.ni = new DelayedNodeIterator(root, excludeComments);
        this.excludeComments = excludeComments;
    }
    public Iterator iterator() {
        return ni;
    }
    public Node getRoot() {
        return root;
    }
    public boolean excludeComments() {
        return excludeComments;
    }
    static class DelayedNodeIterator implements Iterator {
        private Node root;
        private List nodeSet;
        private ListIterator li;
        private boolean withComments;
        DelayedNodeIterator(Node root, boolean excludeComments) {
            this.root = root;
            this.withComments = !excludeComments;
        }
        public boolean hasNext() {
            if (nodeSet == null) {
                nodeSet = dereferenceSameDocumentURI(root);
                li = nodeSet.listIterator();
            }
            return li.hasNext();
        }
        public Object next() {
            if (nodeSet == null) {
                nodeSet = dereferenceSameDocumentURI(root);
                li = nodeSet.listIterator();
            }
            if (li.hasNext()) {
                return (Node) li.next();
            } else {
                throw new NoSuchElementException();
            }
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
        private List dereferenceSameDocumentURI(Node node) {
            List nodeSet = new ArrayList();
            if (node != null) {
                nodeSetMinusCommentNodes(node, nodeSet, null);
            }
            return nodeSet;
        }
        private void nodeSetMinusCommentNodes(Node node, List nodeSet,
            Node prevSibling) {
            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE :
                    NamedNodeMap attrs = node.getAttributes();
                    if (attrs != null) {
                        for (int i = 0, len = attrs.getLength(); i < len; i++) {
                            nodeSet.add(attrs.item(i));
                        }
                    }
                    nodeSet.add(node);
                case Node.DOCUMENT_NODE :
                    Node pSibling = null;
                    for (Node child = node.getFirstChild(); child != null;
                        child = child.getNextSibling()) {
                        nodeSetMinusCommentNodes(child, nodeSet, pSibling);
                        pSibling = child;
                    }
                    break;
                case Node.TEXT_NODE :
                case Node.CDATA_SECTION_NODE:
                    if (prevSibling != null &&
                        (prevSibling.getNodeType() == Node.TEXT_NODE ||
                         prevSibling.getNodeType() == Node.CDATA_SECTION_NODE)){                        return;
                    }
                case Node.PROCESSING_INSTRUCTION_NODE :
                    nodeSet.add(node);
                    break;
                case Node.COMMENT_NODE:
                    if (withComments) {
                        nodeSet.add(node);
                    }
            }
        }
    }
}
