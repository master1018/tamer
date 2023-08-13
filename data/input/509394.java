public class CookieTest extends AndroidTestCase {
    private CookieManager mCookieManager;
    private static final long WAIT_TIME = 50;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CookieSyncManager.createInstance(getContext());
        mCookieManager = CookieManager.getInstance();
        mCookieManager.removeAllCookie();
        int maxWait = 10;
        for (int i=0; i < maxWait; i++) {
            Thread.sleep(WAIT_TIME);
            if (!mCookieManager.hasCookies()) {
                break;
            }
        }
        assertFalse(mCookieManager.hasCookies());
    }
    public void testParse() {
        String url = "http:
        mCookieManager.setCookie(url, "a=b");
        String cookie = mCookieManager.getCookie(url);
        assertTrue(cookie.equals("a=b"));
        mCookieManager.setCookie(url, "c=\"d;\"");
        cookie = mCookieManager.getCookie(url);
        assertTrue(cookie.contains("a=b"));
        assertTrue(cookie.contains("c=\"d;\""));
    }
    public void testDomain() {
        String url = "http:
        mCookieManager.setCookie(url, "a=b");
        String cookie = mCookieManager.getCookie(url);
        assertTrue(cookie.equals("a=b"));
        cookie = mCookieManager.getCookie("http:
        assertTrue(cookie == null);
        mCookieManager.setCookie(url, "c=d; domain=.foo.com");
        cookie = mCookieManager.getCookie(url);
        assertTrue(cookie.contains("a=b;"));
        assertTrue(cookie.contains("c=d"));
        cookie = mCookieManager.getCookie("http:
        assertTrue(cookie.equals("c=d"));
        mCookieManager.setCookie(url, "e=f; domain=www.foo.com");
        cookie = mCookieManager.getCookie(url);
        assertTrue(cookie.contains("a=b"));
        assertTrue(cookie.contains("c=d"));
        assertTrue(cookie.contains("e=f"));
        cookie = mCookieManager.getCookie("http:
        assertTrue(cookie.contains("c=d"));
        assertTrue(cookie.contains("e=f"));
        cookie = mCookieManager.getCookie("http:
        assertTrue(cookie.equals("c=d"));
    }
    public void testSubDomain() {
        String url_abcd = "http:
        String url_bcd = "http:
        String url_cd = "http:
        String url_d = "http:
        mCookieManager.setCookie(url_abcd, "a=1; domain=.a.b.c.d.com");
        mCookieManager.setCookie(url_abcd, "b=2; domain=.b.c.d.com");
        mCookieManager.setCookie(url_abcd, "c=3; domain=.c.d.com");
        mCookieManager.setCookie(url_abcd, "d=4; domain=.d.com");
        String cookie = mCookieManager.getCookie(url_abcd);
        assertTrue(cookie.contains("a=1"));
        assertTrue(cookie.contains("b=2"));
        assertTrue(cookie.contains("c=3"));
        assertTrue(cookie.contains("d=4"));
        cookie = mCookieManager.getCookie(url_bcd);
        assertTrue(cookie.contains("b=2"));
        assertTrue(cookie.contains("c=3"));
        assertTrue(cookie.contains("d=4"));
        cookie = mCookieManager.getCookie(url_cd);
        assertTrue(cookie.contains("c=3"));
        assertTrue(cookie.contains("d=4"));
        cookie = mCookieManager.getCookie(url_d);
        assertTrue(cookie.equals("d=4"));
        mCookieManager.setCookie(url_bcd, "x=bcd; domain=.b.c.d.com");
        mCookieManager.setCookie(url_bcd, "x=cd; domain=.c.d.com");
        cookie = mCookieManager.getCookie(url_bcd);
        assertTrue(cookie.contains("b=2"));
        assertTrue(cookie.contains("c=3"));
        assertTrue(cookie.contains("d=4"));
        assertTrue(cookie.contains("x=bcd"));
        assertTrue(cookie.contains("x=cd"));
        cookie = mCookieManager.getCookie(url_cd);
        assertTrue(cookie.contains("c=3"));
        assertTrue(cookie.contains("d=4"));
        assertTrue(cookie.contains("x=cd"));
    }
    public void testInvalidDomain() {
        String url = "http:
        mCookieManager.setCookie(url, "a=1; domain=.yo.foo.bar.com");
        String cookie = mCookieManager.getCookie(url);
        assertTrue(cookie == null);
        mCookieManager.setCookie(url, "b=2; domain=.foo.com");
        cookie = mCookieManager.getCookie(url);
        assertTrue(cookie == null);
        mCookieManager.setCookie(url, "c=3; domain=.bar.foo.com");
        cookie = mCookieManager.getCookie(url);
        assertTrue(cookie == null);
        mCookieManager.setCookie(url, "d=4; domain=.foo.bar.com.net");
        cookie = mCookieManager.getCookie(url);
        assertTrue(cookie == null);
        mCookieManager.setCookie(url, "e=5; domain=.ar.com");
        cookie = mCookieManager.getCookie(url);
        assertTrue(cookie == null);
        mCookieManager.setCookie(url, "f=6; domain=.com");
        cookie = mCookieManager.getCookie(url);
        assertTrue(cookie == null);
        mCookieManager.setCookie(url, "g=7; domain=.co.uk");
        cookie = mCookieManager.getCookie(url);
        assertTrue(cookie == null);
        mCookieManager.setCookie(url, "h=8; domain=.foo.bar.com.com");
        cookie = mCookieManager.getCookie(url);
        assertTrue(cookie == null);
    }
    public void testPath() {
        String url = "http:
        mCookieManager.setCookie(url, "a=b; path=/wee");
        String cookie = mCookieManager.getCookie(url + "/wee");
        assertTrue(cookie.equals("a=b"));
        cookie = mCookieManager.getCookie(url + "/wee/");
        assertTrue(cookie.equals("a=b"));
        cookie = mCookieManager.getCookie(url + "/wee/hee");
        assertTrue(cookie.equals("a=b"));
        cookie = mCookieManager.getCookie(url + "/wee/hee/more");
        assertTrue(cookie.equals("a=b"));
        cookie = mCookieManager.getCookie(url + "/weehee");
        assertTrue(cookie == null);
        cookie = mCookieManager.getCookie(url);
        assertTrue(cookie == null);
        mCookieManager.setCookie(url, "a=c; path=");
        cookie = mCookieManager.getCookie(url + "/wee");
        assertTrue(cookie.equals("a=b; a=c"));
        cookie = mCookieManager.getCookie(url);
        assertTrue(cookie.equals("a=c"));
        mCookieManager.setCookie(url, "a=d");
        cookie = mCookieManager.getCookie(url + "/wee");
        assertTrue(cookie.equals("a=b; a=d"));
    }
    public void testEmptyValue() {
        String url = "http:
        mCookieManager.setCookie(url, "foo;");
        String cookie = mCookieManager.getCookie(url);
        assertTrue(cookie.equals("foo"));
        mCookieManager.setCookie(url, "bar");
        cookie = mCookieManager.getCookie(url);
        assertTrue(cookie.equals("bar; foo"));
        mCookieManager.setCookie(url, "bar=");
        cookie = mCookieManager.getCookie(url);
        assertTrue(cookie.equals("bar=; bar; foo"));
        mCookieManager.setCookie(url, "foobar=;");
        cookie = mCookieManager.getCookie(url);
        assertTrue(cookie.equals("bar=; foobar=; bar; foo"));
        mCookieManager.setCookie(url, "baz=; path=/wee");
        cookie = mCookieManager.getCookie(url + "/wee");
        assertTrue(cookie.equals("baz=; bar=; foobar=; bar; foo"));
    }
}
