@TestTargetClass(PolicyQualifierInfo.class)
public class PolicyQualifierInfoTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException and IOException.",
        method = "PolicyQualifierInfo",
        args = {byte[].class}
    )
    public final void test_Ctor() throws IOException {
        try {
            new PolicyQualifierInfo(null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            new PolicyQualifierInfo(new byte[0]);
            fail("IOE expected");
        } catch (IOException e) {
        }
        try {
            new PolicyQualifierInfo(
                    new byte[] {(byte)0x06, (byte)0x03,
                            (byte)0x81, (byte)0x34, (byte)0x03});
            fail("IOE expected");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IOException.",
        method = "PolicyQualifierInfo",
        args = {byte[].class}
    )
    public final void testPolicyQualifierInfo02() {
        byte[] encoding = getDerEncoding();
        encoding[1] = (byte)0x27;
        try {
            new PolicyQualifierInfo(encoding);
            fail("IOE expected");
        } catch (IOException e) {
        }
        encoding = getDerEncoding();
        encoding[2] = (byte)13;
        try {
            new PolicyQualifierInfo(encoding);
            fail("IOE expected");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "PolicyQualifierInfo",
        args = {byte[].class}
    )
    public final void testPolicyQualifierInfo03() throws IOException {
        byte[] encoding = getDerEncoding();
        new PolicyQualifierInfo(encoding);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor with encoded byte array copied on construction.",
        method = "PolicyQualifierInfo",
        args = {byte[].class}
    )
    public final void testPolicyQualifierInfo04() throws IOException  {
        byte[] encoding = getDerEncoding();
        byte[] encodingCopy = encoding.clone();
        PolicyQualifierInfo i = new PolicyQualifierInfo(encodingCopy);
        byte[] encodingRet = i.getEncoded();
        assertTrue(Arrays.equals(encoding, encodingRet));
        encodingCopy[0] = (byte)0;
        byte[] encodingRet1 = i.getEncoded();
        assertTrue(Arrays.equals(encoding, encodingRet1));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getEncoded",
        args = {}
    )
    public final void testGetEncoded01() throws IOException {
        byte[] encoding = getDerEncoding();
        PolicyQualifierInfo i = new PolicyQualifierInfo(encoding);
        byte[] encodingRet = i.getEncoded();
        assertTrue(Arrays.equals(encoding, encodingRet));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getEncoded",
        args = {}
    )
    public final void testGetEncoded02() throws IOException {
        byte[] encoding = getDerEncoding();
        byte[] encodingCopy = encoding.clone();
        PolicyQualifierInfo i = new PolicyQualifierInfo(encodingCopy);
        byte[] encodingRet = i.getEncoded();
        encodingRet[0] = (byte)0;
        byte[] encodingRet1 = i.getEncoded();
        assertTrue(Arrays.equals(encoding, encodingRet1));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPolicyQualifier",
        args = {}
    )
    public final void testGetPolicyQualifier01() throws IOException {
        byte[] encoding = getDerEncoding();
        byte[] pqEncoding = new byte[28];
        System.arraycopy(encoding, 12, pqEncoding, 0, pqEncoding.length);
        PolicyQualifierInfo i = new PolicyQualifierInfo(encoding);
        byte[] pqEncodingRet = i.getPolicyQualifier();
        assertTrue(Arrays.equals(pqEncoding, pqEncodingRet));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPolicyQualifier",
        args = {}
    )
    public final void testGetPolicyQualifier02() throws IOException {
        byte[] encoding = getDerEncoding();
        byte[] pqEncoding = new byte[28];
        System.arraycopy(encoding, 12, pqEncoding, 0, pqEncoding.length);
        PolicyQualifierInfo i = new PolicyQualifierInfo(encoding);
        byte[] pqEncodingRet = i.getPolicyQualifier();
        pqEncodingRet[0] = (byte)0;
        byte[] pqEncodingRet1 = i.getPolicyQualifier();
        assertNotSame(pqEncodingRet, pqEncodingRet1);
        assertTrue(Arrays.equals(pqEncoding, pqEncodingRet1));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPolicyQualifierId",
        args = {}
    )
    public final void testGetPolicyQualifierId() throws IOException {
        byte[] encoding = getDerEncoding();
        PolicyQualifierInfo i = new PolicyQualifierInfo(encoding);
        assertEquals("1.3.6.1.5.5.7.2.1", i.getPolicyQualifierId());
        encoding = getDerEncoding();
        encoding[5] = (byte)0x86;
        encoding[6] = (byte)0x81;
        encoding[8] = (byte)0x85;
        encoding[9] = (byte)0x87;
        i = new PolicyQualifierInfo(encoding);
        assertEquals("1.3.98437.82818.1", i.getPolicyQualifierId());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public final void testToString() throws IOException {
        byte[] encoding = getDerEncoding();
        PolicyQualifierInfo i = new PolicyQualifierInfo(encoding);
        assertNotNull(i.toString());
    }
    private static final byte[] getDerEncoding() {
        return  new byte[] {
            (byte)0x30, (byte)0x26, 
              (byte)0x06, (byte)0x08, 
                (byte)0x2b, (byte)0x06, (byte)0x01, (byte)0x05, 
                (byte)0x05, (byte)0x07, (byte)0x02, (byte)0x01, 
              (byte)0x16, (byte)0x1a, 
                (byte)0x68, (byte)0x74, (byte)0x74, (byte)0x70,  
                (byte)0x3a, (byte)0x2f, (byte)0x2f, (byte)0x77,  
                (byte)0x77, (byte)0x77, (byte)0x2e, (byte)0x71,  
                (byte)0x71, (byte)0x2e, (byte)0x63, (byte)0x6f,  
                (byte)0x6d, (byte)0x2f, (byte)0x73, (byte)0x74,  
                (byte)0x6d, (byte)0x74, (byte)0x2e, (byte)0x74,  
                (byte)0x78, (byte)0x74   
        };
    }
}
