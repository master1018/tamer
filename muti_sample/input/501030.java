public abstract class MessageDigestTest extends TestCase {
    private String digestAlgorithmName;
    protected MessageDigestTest(String digestAlgorithmName) {
        super();
        this.digestAlgorithmName = digestAlgorithmName;
    }
    private MessageDigest digest;
    private InputStream sourceData;
    private byte[] checkDigest;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.source3 = getLongMessage(1000000);
        this.digest = getMessageDigest();
        this.sourceData = getSourceData();
        this.checkDigest = getCheckDigest();
    }
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        source1 = null;
        source2 = null;
        source3 = null;
        expected1 = null;
        expected2 = null;
        expected3 = null;
        digest = null;
        sourceData = null;
        checkDigest = null;
        System.gc();
    }
    MessageDigest getMessageDigest()
    {
        try {
            return MessageDigest.getInstance(digestAlgorithmName);
        } catch (NoSuchAlgorithmException e) {
            fail("failed to get digest instance: " + e);
            return null;
        }
    }
    InputStream getSourceData()
    {
        InputStream sourceStream = getClass().getResourceAsStream(
                digestAlgorithmName + ".data");
        assertNotNull("digest source data not found: " + digestAlgorithmName, 
                sourceStream);
        return sourceStream;
    }
    byte[] getCheckDigest()
    {
        InputStream checkDigestStream = getClass().getResourceAsStream(
                digestAlgorithmName + ".check");
        byte[] checkDigest = new byte[digest.getDigestLength()];
        int read = 0;
        int index = 0;
        try {
            while ((read = checkDigestStream.read()) != -1)
            {
                checkDigest[index++] = (byte)read;
            }
        } catch (IOException e) {
            fail("failed to read digest golden data: " + digestAlgorithmName);
        }
        return checkDigest;
    }
    @TestTargets({
        @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "update",
                args = {byte[].class,int.class,int.class}
            ),
            @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "digest",
                args = {}
            ),
            @TestTargetNew(
                level = TestLevel.COMPLETE,
                method = "method",
                args = {}
            )
    })
    public void testMessageDigest1()
    {
        byte[] buf = new byte[128];
        int read = 0;
        try {
            while ((read = sourceData.read(buf)) != -1)
            {
                digest.update(buf, 0, read);
            }
        } catch (IOException e) {
            fail("failed to read digest data");
        }
        byte[] computedDigest = digest.digest();
        assertNotNull("computed digest is is null", computedDigest);
        assertEquals("digest length mismatch", checkDigest.length,
                computedDigest.length);
        for (int i = 0; i < checkDigest.length; i++)
        {
            assertEquals("byte " + i + " of computed and check digest differ",
                    checkDigest[i], computedDigest[i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "update",
                args = {byte.class}
            ),
            @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "digest",
                args = {}
            ),
            @TestTargetNew(
                level = TestLevel.COMPLETE,
                method = "method",
                args = {}
            )
    })
    public void testMessageDigest2()
    {
        int val;
        try {
            while ((val = sourceData.read()) != -1)
            {
                digest.update((byte)val);
            }
        } catch (IOException e) {
            fail("failed to read digest data");
        }
        byte[] computedDigest = digest.digest();
        assertNotNull("computed digest is is null", computedDigest);
        assertEquals("digest length mismatch", checkDigest.length,
                computedDigest.length);
        for (int i = 0; i < checkDigest.length; i++)
        {
            assertEquals("byte " + i + " of computed and check digest differ",
                    checkDigest[i], computedDigest[i]);
        }
    }
    protected String source1, source2, source3;
    protected String expected1, expected2, expected3;
    String getLongMessage(int length) {
        StringBuilder sourceBuilder = new StringBuilder(length);
        for (int i = 0; i < length / 10; i++) {
            sourceBuilder.append("aaaaaaaaaa");
        }
        return sourceBuilder.toString();
    }
    @TestTargets({
        @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "update",
                args = {byte.class}
            ),
            @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "digest",
                args = {}
            ),
            @TestTargetNew(
                level = TestLevel.COMPLETE,
                method = "method",
                args = {}
            )
    })
    public void testfips180_2_singleblock() {
        digest.update(source1.getBytes(), 0, source1.length());
        byte[] computedDigest = digest.digest();
        assertNotNull("computed digest is null", computedDigest);
        StringBuilder sb = new StringBuilder();
        String res;
        for (int i = 0; i < computedDigest.length; i++)
        {
            res = Integer.toHexString(computedDigest[i] & 0xFF);
            sb.append((res.length() == 1 ? "0" : "") + res);
        }
        assertEquals("computed and check digest differ", expected1, 
                sb.toString());
    }
    @TestTargets({
        @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "update",
                args = {byte.class}
            ),
            @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "digest",
                args = {}
            ),
            @TestTargetNew(
                level = TestLevel.COMPLETE,
                method = "method",
                args = {}
            )
    })
    public void testfips180_2_multiblock() {
        digest.update(source2.getBytes(), 0, source2.length());
        byte[] computedDigest = digest.digest();
        assertNotNull("computed digest is null", computedDigest);
        StringBuilder sb = new StringBuilder();
        String res;
        for (int i = 0; i < computedDigest.length; i++)
        {
            res = Integer.toHexString(computedDigest[i] & 0xFF);
            sb.append((res.length() == 1 ? "0" : "") + res);
        }
        assertEquals("computed and check digest differ", expected2,
                sb.toString());
    }
    @TestTargets({
        @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "update",
                args = {byte.class}
            ),
            @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "digest",
                args = {}
            ),
            @TestTargetNew(
                level = TestLevel.COMPLETE,
                method = "method",
                args = {}
            )
    })
    public void testfips180_2_longMessage() {
        digest.update(source3.getBytes(), 0, source3.length());
        byte[] computedDigest = digest.digest();
        assertNotNull("computed digest is null", computedDigest);
        StringBuilder sb = new StringBuilder();
        String res;
        for (int i = 0; i < computedDigest.length; i++)
        {
            res = Integer.toHexString(computedDigest[i] & 0xFF);
            sb.append((res.length() == 1 ? "0" : "") + res);
        }
        assertEquals("computed and check digest differ", expected3, 
                sb.toString());
    }
}
