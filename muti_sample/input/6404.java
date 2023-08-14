public class B4148751
{
    final static String scheme = "http";
    final static String auth = "web2.javasoft.com";
    final static String path = "/some file.html";
    final static String unencoded = "http:
    final static String encoded = "http:
    public static void main(String args[]) throws URISyntaxException,
        MalformedURLException {
        URL url = null;
        URL url1 = null;
        try {
            url = new URL(unencoded);
            url1 = new URL(encoded);
        }
        catch(Exception e) {
            System.out.println("Unexpected exception :" + e);
            System.exit(-1);
        }
        if(url.sameFile(url1)) {
            throw new RuntimeException ("URL does not understand escaping");
        }
        URI uri = url1.toURI();
        if (!uri.getPath().equals (path)) {
            throw new RuntimeException ("Got: " + uri.getPath() + " expected: " +
                path);
        }
        URI uri1 = new URI (scheme, auth, path);
        url = uri.toURL();
        if (!url.toString().equals (encoded)) {
            throw new RuntimeException ("Got: " + url.toString() + " expected: " +
                encoded);
        }
    }
}
