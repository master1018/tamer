@TestTargetClass(Cipher.AESWrap.class)
public class CipherAesWrapTest extends TestCase {
    @Override protected void setUp() throws Exception {
        super.setUp();
        TestEnvironment.reset();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "method",
        args = {}
    )
    public void test_AesWrap() {
        CipherWrapThread aesWrap = new CipherWrapThread("AESWrap", new int[] {
                128, 192, 256}, 
                new String[] {"ECB"}, new String[] {"NoPadding"});
        aesWrap.launcher();
        assertEquals(aesWrap.getFailureMessages(), 0, aesWrap
                .getTotalFailuresNumber());
    }
}
