public class XMLNode {
    XMLNode(XMLNode parent, String qname) {
        this.parent = parent;
        name = qname;
        attrs = new HashMap<String,String>();
        children = new ArrayList<XMLNode>();
        if (parent != null)
            parent.children.add(this);
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(name);
        for (Map.Entry<String,String> e: attrs.entrySet())
            sb.append(" " + e.getKey() + "=\"" + e.getValue() + "\"");
        if (children.size() == 0)
            sb.append("/>");
        else {
            sb.append(">");
            for (XMLNode c: children)
                sb.append(c.toString());
            sb.append("</" + name + ">");
        }
        return sb.toString();
    }
    final XMLNode parent;
    final String name;
    final Map<String,String> attrs;
    final List<XMLNode> children;
}
