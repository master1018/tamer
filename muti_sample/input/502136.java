public class QName implements Serializable {
    private static final long serialVersionUID;
    private static final long defaultSerialVersionUID = -9120448754896609940L;
    private static final long compatabilitySerialVersionUID = 4418622981026545151L;
    static {
        String compatPropValue = null;
        try {
            compatPropValue = (String)AccessController.doPrivileged(
                    new PrivilegedAction() {
                        public Object run() {
                            return System.getProperty("org.apache.xml.namespace.QName.useCompatibleSerialVersionUID");
                        }
                    });
        } 
        catch (Exception e) {}
        serialVersionUID = !"1.0".equals(compatPropValue) ? defaultSerialVersionUID : compatabilitySerialVersionUID;
    }
    private final String namespaceURI;
    private final String localPart;
    private String prefix;
    private transient String qNameAsString;
    public QName(final String namespaceURI, final String localPart) {
        this(namespaceURI, localPart, XMLConstants.DEFAULT_NS_PREFIX);
    }
    public QName(String namespaceURI, String localPart, String prefix) {
        if (namespaceURI == null) {
            this.namespaceURI = XMLConstants.NULL_NS_URI;
        } else {
            this.namespaceURI = namespaceURI;
        }
        if (localPart == null) {
            throw new IllegalArgumentException("local part cannot be \"null\" when creating a QName");
        }
        this.localPart = localPart;
        if (prefix == null) {
            throw new IllegalArgumentException("prefix cannot be \"null\" when creating a QName");
        }
        this.prefix = prefix;
    }
    public QName(String localPart) {
        this(
            XMLConstants.NULL_NS_URI,
            localPart,
            XMLConstants.DEFAULT_NS_PREFIX);
    }
    public String getNamespaceURI() {
        return namespaceURI;
    }
    public String getLocalPart() {
        return localPart;
    }
    public String getPrefix() {
        return prefix;
    }
    public final boolean equals(Object objectToTest) {
        if (objectToTest == this) {
            return true;
        }
        if (objectToTest instanceof QName) {
            QName qName = (QName) objectToTest;
            return localPart.equals(qName.localPart) && namespaceURI.equals(qName.namespaceURI);
        }
        return false;
    }
    public final int hashCode() {
        return namespaceURI.hashCode() ^ localPart.hashCode();
    }
    public String toString() {
        String _qNameAsString = qNameAsString;
        if (_qNameAsString == null) {
            final int nsLength = namespaceURI.length();
            if (nsLength == 0) {
                _qNameAsString = localPart;
            }
            else {
                StringBuffer buffer = new StringBuffer(nsLength + localPart.length() + 2);
                buffer.append('{');
                buffer.append(namespaceURI);
                buffer.append('}');
                buffer.append(localPart);
                _qNameAsString = buffer.toString();
            }
            qNameAsString = _qNameAsString;
        }
        return _qNameAsString;
    }
    public static QName valueOf(String qNameAsString) {
        if (qNameAsString == null) {
            throw new IllegalArgumentException("cannot create QName from \"null\" or \"\" String");
        }
        if (qNameAsString.length() == 0) {
            return new QName(
                XMLConstants.NULL_NS_URI,
                qNameAsString,
                XMLConstants.DEFAULT_NS_PREFIX);
        }
        if (qNameAsString.charAt(0) != '{') {
            return new QName(
                XMLConstants.NULL_NS_URI,
                qNameAsString,
                XMLConstants.DEFAULT_NS_PREFIX);
        }
        if (qNameAsString.startsWith("{" + XMLConstants.NULL_NS_URI + "}")) {
            throw new IllegalArgumentException(
                "Namespace URI .equals(XMLConstants.NULL_NS_URI), "
                + ".equals(\"" + XMLConstants.NULL_NS_URI + "\"), "
                + "only the local part, "
                + "\"" + qNameAsString.substring(2 + XMLConstants.NULL_NS_URI.length()) + "\", "
                + "should be provided.");
        }
        int endOfNamespaceURI = qNameAsString.indexOf('}');
        if (endOfNamespaceURI == -1) {
            throw new IllegalArgumentException(
                "cannot create QName from \""
                    + qNameAsString
                    + "\", missing closing \"}\"");
        }
        return new QName(
            qNameAsString.substring(1, endOfNamespaceURI),
            qNameAsString.substring(endOfNamespaceURI + 1),
            XMLConstants.DEFAULT_NS_PREFIX);
    }
    private void readObject(ObjectInputStream in) 
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (prefix == null) {
            prefix = XMLConstants.DEFAULT_NS_PREFIX;
        }
    }
}
