public class Bug6359330 {
    public static void main(String[] args) throws Throwable {
        System.setSecurityManager(new SecurityManager());
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setValidating(true);
            SAXParser sp = spf.newSAXParser();
            sp.setProperty("foo", "bar");
        } catch (SAXNotRecognizedException e) {
        }
    }
}
