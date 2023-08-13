class PartiallyOrderedSet extends AbstractSet {
    private Map poNodes = new HashMap();
    private Set nodes = poNodes.keySet();
    public PartiallyOrderedSet() {}
    public int size() {
        return nodes.size();
    }
    public boolean contains(Object o) {
        return nodes.contains(o);
    }
    public Iterator iterator() {
        return new PartialOrderIterator(poNodes.values().iterator());
    }
    public boolean add(Object o) {
        if (nodes.contains(o)) {
            return false;
        }
        DigraphNode node = new DigraphNode(o);
        poNodes.put(o, node);
        return true;
    }
    public boolean remove(Object o) {
        DigraphNode node = (DigraphNode)poNodes.get(o);
        if (node == null) {
            return false;
        }
        poNodes.remove(o);
        node.dispose();
        return true;
    }
    public void clear() {
        poNodes.clear();
    }
    public boolean setOrdering(Object first, Object second) {
        DigraphNode firstPONode =
            (DigraphNode)poNodes.get(first);
        DigraphNode secondPONode =
            (DigraphNode)poNodes.get(second);
        secondPONode.removeEdge(firstPONode);
        return firstPONode.addEdge(secondPONode);
    }
    public boolean unsetOrdering(Object first, Object second) {
        DigraphNode firstPONode =
            (DigraphNode)poNodes.get(first);
        DigraphNode secondPONode =
            (DigraphNode)poNodes.get(second);
        return firstPONode.removeEdge(secondPONode) ||
            secondPONode.removeEdge(firstPONode);
    }
    public boolean hasOrdering(Object preferred, Object other) {
        DigraphNode preferredPONode =
            (DigraphNode)poNodes.get(preferred);
        DigraphNode otherPONode =
            (DigraphNode)poNodes.get(other);
        return preferredPONode.hasEdge(otherPONode);
    }
}
class PartialOrderIterator implements Iterator {
    LinkedList zeroList = new LinkedList();
    Map inDegrees = new HashMap(); 
    public PartialOrderIterator(Iterator iter) {
        while (iter.hasNext()) {
            DigraphNode node = (DigraphNode)iter.next();
            int inDegree = node.getInDegree();
            inDegrees.put(node, new Integer(inDegree));
            if (inDegree == 0) {
                zeroList.add(node);
            }
        }
    }
    public boolean hasNext() {
        return !zeroList.isEmpty();
    }
    public Object next() {
        DigraphNode first = (DigraphNode)zeroList.removeFirst();
        Iterator outNodes = first.getOutNodes();
        while (outNodes.hasNext()) {
            DigraphNode node = (DigraphNode)outNodes.next();
            int inDegree = ((Integer)inDegrees.get(node)).intValue() - 1;
            inDegrees.put(node, new Integer(inDegree));
            if (inDegree == 0) {
                zeroList.add(node);
            }
        }
        return first.getData();
    }
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
