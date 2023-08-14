public class ClientSessionContextTest extends TestCase {
    public void testGetSessionById() {
        ClientSessionContext context = new ClientSessionContext(null, null);
        SSLSession a = new FakeSession("a");
        SSLSession b = new FakeSession("b");
        context.putSession(a);
        context.putSession(b);
        assertSame(a, context.getSession("a".getBytes()));
        assertSame(b, context.getSession("b".getBytes()));
        assertSame(a, context.getSession("a", 443));
        assertSame(b, context.getSession("b", 443));
        assertEquals(2, context.sessions.size());
        Set<SSLSession> sessions = new HashSet<SSLSession>();
        Enumeration ids = context.getIds();
        while (ids.hasMoreElements()) {
            sessions.add(context.getSession((byte[]) ids.nextElement()));
        }
        Set<SSLSession> expected = new HashSet<SSLSession>();
        expected.add(a);
        expected.add(b);
        assertEquals(expected, sessions);
    }
    public void testTrimToSize() {
        ClientSessionContext context = new ClientSessionContext(null, null);
        FakeSession a = new FakeSession("a");
        FakeSession b = new FakeSession("b");
        FakeSession c = new FakeSession("c");
        FakeSession d = new FakeSession("d");
        context.putSession(a);
        context.putSession(b);
        context.putSession(c);
        context.putSession(d);
        context.setSessionCacheSize(2);
        Set<SSLSession> sessions = new HashSet<SSLSession>();
        Enumeration ids = context.getIds();
        while (ids.hasMoreElements()) {
            sessions.add(context.getSession((byte[]) ids.nextElement()));
        }
        Set<SSLSession> expected = new HashSet<SSLSession>();
        expected.add(c);
        expected.add(d);
        assertEquals(expected, sessions);               
    }
}
