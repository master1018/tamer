final class DescendantEnumeration extends AxisIteratorImpl {
    private TinyTree tree;
    private TinyNodeImpl startNode;
    private boolean includeSelf;
    private int nextNodeNr;
    private int startDepth;
    private NodeTest test;
    DescendantEnumeration(TinyTree doc, TinyNodeImpl node, NodeTest nodeTest, boolean includeSelf) {
        tree = doc;
        startNode = node;
        this.includeSelf = includeSelf;
        test = nodeTest;
        nextNodeNr = node.nodeNr;
        startDepth = doc.depth[nextNodeNr];
    }
    public Item next() {
        if (position == 0 && includeSelf && test.matches(startNode)) {
            current = startNode;
            position++;
            return current;
        }
        do {
            nextNodeNr++;
            try {
                if (tree.depth[nextNodeNr] <= startDepth) {
                    nextNodeNr = -1;
                    current = null;
                    position = -1;
                    return null;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                nextNodeNr = -1;
                current = null;
                position = -1;
                return null;
            }
        } while (!test.matches(tree, nextNodeNr));
        position++;
        current = tree.getNode(nextNodeNr);
        return current;
    }
    public SequenceIterator getAnother() {
        return new DescendantEnumeration(tree, startNode, test, includeSelf);
    }
}
