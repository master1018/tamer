public class URITest extends TestCase {
    @SmallTest
    public void testConstruct() throws Exception {
        construct("http:
                "www.google.com", "/this/is-the/path", true);
    }
    private static void construct(String str, String host, String path, boolean absolute)
            throws URISyntaxException {
        URI uri = new URI(str);
        assertEquals(host, uri.getHost());
        assertEquals(path, uri.getPath());
        assertEquals(absolute, uri.isAbsolute());
    }
    @SmallTest
    public void testResolve() throws Exception {
        resolve("http:
                "mom",
                "http:
    }
    private static void resolve(String base, String uri, String expected) {
        URI b = URI.create(base);
        URI resolved = b.resolve(uri);
        assertEquals(expected, resolved.toString());
    }
}
