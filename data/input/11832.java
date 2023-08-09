public class URItoURLTest {
    public static void main(String args[]) throws Exception {
        URItoURLTest testClass = new URItoURLTest();
        URL classUrl = testClass.getClass().
                                    getResource("/java/lang/Object.class");
        String[] uris = { "mailto:xyz@abc.de",
                        "file:xyz#ab",
                        "http:abc/xyz/pqr",
                        "file:/C:/v700/dev/unitTesting/tests/apiUtil/uri",
                        "http:
                        classUrl.toExternalForm(),
                        };
        boolean isTestFailed = false;
        boolean isURLFailed = false;
        for (int i = 0; i < uris.length; i++) {
            URI uri = URI.create(uris[i]);
            URL url1 = new URL(uri.toString());
            URL url2 = uri.toURL();
            System.out.println("Testing URI " + uri);
            if (!url1.equals(url2)) {
                System.out.println("equals() FAILED");
                isURLFailed = true;
            }
            if (url1.hashCode() != url2.hashCode()) {
                System.out.println("hashCode() DIDN'T MATCH");
                isURLFailed = true;
            }
            if (!url1.sameFile(url2)) {
                System.out.println("sameFile() FAILED");
                isURLFailed = true;
            }
            if (!equalsComponents("getPath()", url1.getPath(),
                                            url2.getPath())) {
                isURLFailed = true;
            }
            if (!equalsComponents("getFile()", url1.getFile(),
                                            url2.getFile())) {
                isURLFailed = true;
            }
            if (!equalsComponents("getHost()", url1.getHost(),
                                            url2.getHost())) {
                isURLFailed = true;
            }
            if (!equalsComponents("getAuthority()",
                                url1.getAuthority(), url2.getAuthority())) {
                isURLFailed = true;
            }
            if (!equalsComponents("getRef()", url1.getRef(),
                                            url2.getRef())) {
                isURLFailed = true;
            }
            if (!equalsComponents("getUserInfo()", url1.getUserInfo(),
                                            url2.getUserInfo())) {
                isURLFailed = true;
            }
            if (!equalsComponents("toString()", url1.toString(),
                                            url2.toString())) {
                isURLFailed = true;
            }
            if (isURLFailed) {
                isTestFailed = true;
            } else {
                System.out.println("PASSED ..");
            }
            System.out.println();
            isURLFailed = false;
        }
        if (isTestFailed) {
            throw new Exception("URI.toURL() test failed");
        }
    }
    static boolean equalsComponents(String method, String comp1, String comp2) {
        if ((comp1 != null) && (!comp1.equals(comp2))) {
            System.out.println(method + " DIDN'T MATCH" +
                        "  ===>");
                System.out.println("    URL(URI.toString()) returns:" + comp1);
                System.out.println("    URI.toURL() returns:" + comp2);
                return false;
        }
        return true;
    }
}
