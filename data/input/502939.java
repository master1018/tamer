public class InetAddrTest extends TestCase {
    private static final String[] HOSTS = {
            "localhost", "www.google.com", "www.slashdot.org", "www.wikipedia.org",
            "www.paypal.com", "www.cnn.com", "www.yahoo.com", "www.amazon.com",
            "www.ebay.com", "www.android.com"
    };
    public void testInetAddr() throws Exception {
        byte[] raw;
        InetAddress ia = InetAddress.getByName("localhost");
        raw = ia.getAddress();
        assertEquals(127, raw[0]);
        assertEquals(0, raw[1]);
        assertEquals(0, raw[2]);
        assertEquals(1, raw[3]);
        ia = InetAddress.getByName("127.0.0.1");
        raw = ia.getAddress();
        assertEquals(127, raw[0]);
        assertEquals(0, raw[1]);
        assertEquals(0, raw[2]);
        assertEquals(1, raw[3]);
        ia = InetAddress.getByName(null);
        try {
            InetAddress.getByName(".0.0.1");
            fail("expected ex");
        } catch (UnknownHostException ex) {
        }
        try {
            InetAddress.getByName("thereisagoodchancethisdomaindoesnotexist.weirdtld");
            fail("expected ex");
        } catch (UnknownHostException ex) {
        }
        try {
            InetAddress.getByName("127.0.0.");
            fail("expected ex");
        } catch (UnknownHostException ex) {
        }
        Random random = new Random();
        int count = 0;
        for (int i = 0; i < 100; i++) {
            int index = random.nextInt(HOSTS.length);
            try {
                InetAddress.getByName(HOSTS[index]);
                count++;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                }
            } catch (UnknownHostException ex) {
            }
        }
        assertEquals("Not all host lookups succeeded", 100, count);
    }
}
