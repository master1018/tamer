public class JAXPPrefixResolver implements PrefixResolver
{
    private NamespaceContext namespaceContext;
    public JAXPPrefixResolver ( NamespaceContext nsContext ) {
        this.namespaceContext = nsContext;
    } 
    public String getNamespaceForPrefix( String prefix ) {
        return namespaceContext.getNamespaceURI( prefix );
    }
    public String getBaseIdentifier() {
        return null;
    }
    public boolean handlesNullPrefixes() {
        return false;
    }
    public static final String S_XMLNAMESPACEURI =
        "http:
    public String getNamespaceForPrefix(String prefix,
                                      org.w3c.dom.Node namespaceContext) {
        Node parent = namespaceContext;
        String namespace = null;
        if (prefix.equals("xml")) {
            namespace = S_XMLNAMESPACEURI;
        } else {
            int type;
            while ((null != parent) && (null == namespace)
                && (((type = parent.getNodeType()) == Node.ELEMENT_NODE)
                    || (type == Node.ENTITY_REFERENCE_NODE))) {
                if (type == Node.ELEMENT_NODE) {
                    NamedNodeMap nnm = parent.getAttributes();
                    for (int i = 0; i < nnm.getLength(); i++) {
                        Node attr = nnm.item(i);
                        String aname = attr.getNodeName();
                        boolean isPrefix = aname.startsWith("xmlns:");
                        if (isPrefix || aname.equals("xmlns")) {
                            int index = aname.indexOf(':');
                            String p =isPrefix ?aname.substring(index + 1) :"";
                            if (p.equals(prefix)) {
                                namespace = attr.getNodeValue();
                                break;
                            }
                        }
                    }
                }
                parent = parent.getParentNode();
            }
        }
        return namespace;
    }
}
