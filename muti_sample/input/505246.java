@TestTargetClass(Cipher.DESedeWrap.class)
public class CipherDESedeWrapTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "method",
        args = {}
    )
    public void test_DESedeWrap() {
        CipherWrapThread DESedeWrap = new CipherWrapThread("DESedeWrap",
                new int[] {112, 168}, 
                new String[] {"CBC"}, new String[] {"NoPadding"});
        DESedeWrap.launcher();
        assertEquals(DESedeWrap.getFailureMessages(), 0, DESedeWrap
                .getTotalFailuresNumber());
    }
}
