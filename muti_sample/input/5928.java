public class ApacheNodeSetData implements ApacheData, NodeSetData {
    private XMLSignatureInput xi;
    public ApacheNodeSetData(XMLSignatureInput xi) {
        this.xi = xi;
    }
    public Iterator iterator() {
        if (xi.getNodeFilters() != null) {
            return Collections.unmodifiableSet
                (getNodeSet(xi.getNodeFilters())).iterator();
        }
        try {
            return Collections.unmodifiableSet(xi.getNodeSet()).iterator();
        } catch (Exception e) {
            throw new RuntimeException
                ("unrecoverable error retrieving nodeset", e);
        }
    }
    public XMLSignatureInput getXMLSignatureInput() {
        return xi;
    }
    private Set getNodeSet(List nodeFilters) {
        if (xi.isNeedsToBeExpanded()) {
            XMLUtils.circumventBug2650
                (XMLUtils.getOwnerDocument(xi.getSubNode()));
        }
        Set inputSet = new LinkedHashSet();
        XMLUtils.getSet
          (xi.getSubNode(), inputSet, null, !xi.isExcludeComments());
        Set nodeSet = new LinkedHashSet();
        Iterator i = inputSet.iterator();
        while (i.hasNext()) {
            Node currentNode = (Node) i.next();
            Iterator it = nodeFilters.iterator();
            boolean skipNode = false;
            while (it.hasNext() && !skipNode) {
                NodeFilter nf = (NodeFilter) it.next();
                if (nf.isNodeInclude(currentNode)!=1) {
                    skipNode = true;
                }
            }
            if (!skipNode) {
                nodeSet.add(currentNode);
            }
        }
        return nodeSet;
    }
}
