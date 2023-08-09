@TestTargetClass(Charset.class)
public abstract class AbstractCharsetTestCase extends TestCase {
    protected final String canonicalName;
    protected final String[] aliases;
    protected final boolean canEncode;
    protected final boolean isRegistered;
    protected Charset testingCharset;
    protected void setUp() throws Exception {
        super.setUp();
        this.testingCharset = Charset.forName(this.canonicalName);
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    public AbstractCharsetTestCase(String canonicalName,
            String[] aliases, boolean canEncode, boolean isRegistered) {
        this.canonicalName = canonicalName;
        this.canEncode = canEncode;
        this.isRegistered = isRegistered;
        this.aliases = aliases;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "canEncode",
        args = {}
    )
    public void testCanEncode() {
        assertEquals(this.canEncode, this.testingCharset.canEncode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isRegistered",
        args = {}
    )
    public void testIsRegistered() {
        assertEquals(this.isRegistered, this.testingCharset.isRegistered());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "name",
        args = {}
    )
    public void testName() {
        assertEquals(this.canonicalName, this.testingCharset.name());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Test functionality completely missed.",
        method = "aliases",
        args = {}
    )
    public void testAliases() {
        for (int i = 0; i < this.aliases.length; i++) {
            Charset c = Charset.forName(this.aliases[i]);
            assertEquals(this.canonicalName, c.name());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "",
        method = "encode",
        args = {java.lang.String.class}
    )
    public void testEncode_String_Null() {
        try {
            this.testingCharset.encode((String) null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "",
        method = "encode",
        args = {java.nio.CharBuffer.class}
    )
    public void testEncode_CharBuffer_Null() {
        try {
            this.testingCharset.encode((CharBuffer) null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    protected void internalTestEncode(String input, byte[] output) {
        ByteBuffer bb = this.testingCharset.encode(input);
        int i = 0;
        bb.rewind();
        while (bb.hasRemaining() && i < output.length) {
            assertEquals(output[i], bb.get());
            i++;
        }
        assertFalse(bb.hasRemaining());
        assertEquals(output.length, i);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "newEncoder",
        args = {}
    )
    public abstract void testEncode_Normal();
    protected void internalTestDecode(byte[] input, char[] output) {
        CharBuffer chb = this.testingCharset.decode(ByteBuffer.wrap(input));
        int i = 0;
        chb.rewind();
        while (chb.hasRemaining() && i < output.length) {
            assertEquals(output[i], chb.get());
            i++;
        }
        assertFalse(chb.hasRemaining());
        assertEquals(output.length, i);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "newDecoder",
        args = {}
    )
    public abstract void testDecode_Normal();
}
