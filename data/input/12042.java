class SimpleNode implements Node {
    protected Node parent;
    protected Node[] children;
    protected int id;
    protected Parser parser;
    public SimpleNode(int i) {
        id = i;
    }
    public SimpleNode(Parser p, int i) {
        this(i);
        parser = p;
    }
    public static Node jjtCreate(int id) {
        return new SimpleNode(id);
    }
    public static Node jjtCreate(Parser p, int id) {
        return new SimpleNode(p, id);
    }
    public void jjtOpen() {
    }
    public void jjtClose() {
    }
    public void jjtSetParent(Node n) { parent = n; }
    public Node jjtGetParent() { return parent; }
    public void jjtAddChild(Node n, int i) {
        if (children == null) {
            children = new Node[i + 1];
        } else if (i >= children.length) {
            Node c[] = new Node[i + 1];
            System.arraycopy(children, 0, c, 0, children.length);
            children = c;
        }
        children[i] = n;
    }
    public Node jjtGetChild(int i) {
        return children[i];
    }
    public int jjtGetNumChildren() {
        return (children == null) ? 0 : children.length;
    }
    public void buildTrapEntries(Hashtable<InetAddress, Vector<String>> dest) {
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                SimpleNode n = (SimpleNode)children[i];
                if (n != null) {
                    n.buildTrapEntries(dest);
                }
            } 
        }
    }
    public void buildInformEntries(Hashtable<InetAddress, Vector<String>> dest) {
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                SimpleNode n = (SimpleNode)children[i];
                if (n != null) {
                    n.buildInformEntries(dest);
                }
            } 
        }
    }
    public void buildAclEntries(PrincipalImpl owner, AclImpl acl) {
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                SimpleNode n = (SimpleNode)children[i];
                if (n != null) {
                    n.buildAclEntries(owner, acl);
                }
            } 
        }
    }
    public String toString() { return ParserTreeConstants.jjtNodeName[id]; }
    public String toString(String prefix) { return prefix + toString(); }
    public void dump(String prefix) {
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                SimpleNode n = (SimpleNode)children[i];
                if (n != null) {
                    n.dump(prefix + " ");
                }
            }
        }
    }
}
