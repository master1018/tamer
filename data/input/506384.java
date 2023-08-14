public class AndroidXPathFactory {
    public final static String DEFAULT_NS_PREFIX = "android"; 
    private final static XPathFactory sFactory = XPathFactory.newInstance();
    private static class AndroidNamespaceContext implements NamespaceContext {
        private final static AndroidNamespaceContext sThis = new AndroidNamespaceContext(
                DEFAULT_NS_PREFIX);
        private final String mAndroidPrefix;
        private final List<String> mAndroidPrefixes = new ArrayList<String>();
        private static AndroidNamespaceContext getDefault() {
            return sThis;
        }
        public AndroidNamespaceContext(String androidPrefix) {
            mAndroidPrefix = androidPrefix;
            mAndroidPrefixes.add(mAndroidPrefix);
        }
        public String getNamespaceURI(String prefix) {
            if (prefix != null) {
                if (prefix.equals(mAndroidPrefix)) {
                    return SdkConstants.NS_RESOURCES;
                }
            }
            return XMLConstants.NULL_NS_URI;
        }
        public String getPrefix(String namespaceURI) {
            if (SdkConstants.NS_RESOURCES.equals(namespaceURI)) {
                return mAndroidPrefix;
            }
            return null;
        }
        public Iterator<?> getPrefixes(String namespaceURI) {
            if (SdkConstants.NS_RESOURCES.equals(namespaceURI)) {
                return mAndroidPrefixes.iterator();
            }
            return null;
        }
    }
    public static XPath newXPath(String androidPrefix) {
        XPath xpath = sFactory.newXPath();
        xpath.setNamespaceContext(new AndroidNamespaceContext(androidPrefix));
        return xpath;
    }
    public static XPath newXPath() {
        XPath xpath = sFactory.newXPath();
        xpath.setNamespaceContext(AndroidNamespaceContext.getDefault());
        return xpath;
    }
}
