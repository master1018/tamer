public class AttrCompare implements Comparator, Serializable {
    private final static long serialVersionUID = -7113259629930576230L;
    private final static int ATTR0_BEFORE_ATTR1 = -1;
    private final static int ATTR1_BEFORE_ATTR0 = 1;
    private final static String XMLNS=Constants.NamespaceSpecNS;
    public int compare(Object obj0, Object obj1) {
        Attr attr0 = (Attr) obj0;
        Attr attr1 = (Attr) obj1;
        String namespaceURI0 = attr0.getNamespaceURI();
        String namespaceURI1 = attr1.getNamespaceURI();
        boolean isNamespaceAttr0 = XMLNS==namespaceURI0;
        boolean isNamespaceAttr1 = XMLNS==namespaceURI1;
        if (isNamespaceAttr0) {
            if (isNamespaceAttr1) {
                String localname0 = attr0.getLocalName();
                String localname1 = attr1.getLocalName();
                if (localname0.equals("xmlns")) {
                    localname0 = "";
                }
                if (localname1.equals("xmlns")) {
                    localname1 = "";
                }
                return localname0.compareTo(localname1);
            }
            return ATTR0_BEFORE_ATTR1;
        }
        if (isNamespaceAttr1) {
            return ATTR1_BEFORE_ATTR0;
        }
        if (namespaceURI0 == null) {
            if (namespaceURI1 == null) {
                String name0 = attr0.getName();
                String name1 = attr1.getName();
                return name0.compareTo(name1);
            }
            return ATTR0_BEFORE_ATTR1;
        }
        if (namespaceURI1 == null) {
            return ATTR1_BEFORE_ATTR0;
        }
        int a = namespaceURI0.compareTo(namespaceURI1);
        if (a != 0) {
            return a;
        }
        return (attr0.getLocalName()).compareTo(attr1.getLocalName());
    }
}
