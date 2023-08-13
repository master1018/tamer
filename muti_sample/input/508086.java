public class ATResponseParserTest extends TestCase {
    @SmallTest
    public void testBasic() throws Exception {
        ATResponseParser p = new ATResponseParser("+CREG: 0");
        assertEquals(0, p.nextInt());
        assertFalse(p.hasMore());
        try {
            p.nextInt();
            fail("exception expected");
        } catch (ATParseEx ex) {
        }
        p = new ATResponseParser("+CREG: 0,1");
        assertEquals(0, p.nextInt());
        assertEquals(1, p.nextInt());
        assertFalse(p.hasMore());
        p = new ATResponseParser("+CREG: 0, 1");
        assertEquals(0, p.nextInt());
        assertEquals(1, p.nextInt());
        assertFalse(p.hasMore());
        p = new ATResponseParser("+CREG: 0, 1,");
        assertEquals(0, p.nextInt());
        assertEquals(1, p.nextInt());
        assertFalse(p.hasMore());
        try {
            p.nextInt();
            fail("exception expected");
        } catch (ATParseEx ex) {
        }
        p = new ATResponseParser("+CREG: 0, 1 ");
        assertEquals(0, p.nextInt());
        assertEquals(1, p.nextInt());
        assertFalse(p.hasMore());
        p = new ATResponseParser("0, 1 ");
        try {
            p.nextInt();
            fail("exception expected");
        } catch (ATParseEx ex) {
        }
        p = new ATResponseParser("+CREG: 0, 1, 5");
        assertFalse(p.nextBoolean());
        assertTrue(p.nextBoolean());
        try {
            p.nextBoolean();
            fail("exception expected");
        } catch (ATParseEx ex) {
        }
        p = new ATResponseParser("+CLCC: 1,0,2,0,0,\"+18005551212\",145");
        assertEquals(1, p.nextInt());
        assertFalse(p.nextBoolean());
        assertEquals(2, p.nextInt());
        assertEquals(0, p.nextInt());
        assertEquals(0, p.nextInt());
        assertEquals("+18005551212", p.nextString());
        assertEquals(145, p.nextInt());
        assertFalse(p.hasMore());
        p = new ATResponseParser("+CLCC: 1,0,2,0,0,\"+18005551212,145");
        assertEquals(1, p.nextInt());
        assertFalse(p.nextBoolean());
        assertEquals(2, p.nextInt());
        assertEquals(0, p.nextInt());
        assertEquals(0, p.nextInt());
        try {
            p.nextString();
            fail("expected ex");
        } catch (ATParseEx ex) {
        }
        p = new ATResponseParser("+FOO: \"\"");
        assertEquals("", p.nextString());
    }
}
