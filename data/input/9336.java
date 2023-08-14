class NameNode {
    private String label;               
    private Hashtable children = null;  
    private boolean isZoneCut = false;  
    private int depth = 0;              
    NameNode(String label) {
        this.label = label;
    }
    protected NameNode newNameNode(String label) {
        return new NameNode(label);
    }
    String getLabel() {
        return label;
    }
    int depth() {
        return depth;
    }
    boolean isZoneCut() {
        return isZoneCut;
    }
    void setZoneCut(boolean isZoneCut) {
        this.isZoneCut = isZoneCut;
    }
    Hashtable getChildren() {
        return children;
    }
    NameNode get(String key) {
        return (children != null)
            ? (NameNode) children.get(key)
            : null;
    }
    NameNode get(DnsName name, int idx) {
        NameNode node = this;
        for (int i = idx; i < name.size() && node != null; i++) {
            node = node.get(name.getKey(i));
        }
        return node;
    }
    NameNode add(DnsName name, int idx) {
        NameNode node = this;
        for (int i = idx; i < name.size(); i++) {
            String label = name.get(i);
            String key = name.getKey(i);
            NameNode child = null;
            if (node.children == null) {
                node.children = new Hashtable();
            } else {
                child = (NameNode) node.children.get(key);
            }
            if (child == null) {
                child = newNameNode(label);
                child.depth = node.depth + 1;
                node.children.put(key, child);
            }
            node = child;
        }
        return node;
    }
}
