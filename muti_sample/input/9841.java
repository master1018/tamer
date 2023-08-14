public class B6277794 {
    public static void main(String[] args) throws Exception {
        testCookieStore();
    }
    private static void testCookieStore() throws Exception {
        CookieManager cm = new CookieManager();
        CookieStore cs = cm.getCookieStore();
        HttpCookie c1 = new HttpCookie("COOKIE1", "COOKIE1");
        HttpCookie c2 = new HttpCookie("COOKIE2", "COOKIE2");
        cs.add(new URI("http:
        cs.add(new URI("http:
        List<URI> uris = cs.getURIs();
        if (uris.size() != 1 ||
                !uris.get(0).equals(new URI("http:
            fail("CookieStore.getURIs() fail.");
        }
    }
    private static void fail(String msg) throws Exception {
        throw new RuntimeException(msg);
    }
}
