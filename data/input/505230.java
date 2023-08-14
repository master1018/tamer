@TestTargetClass(Cipher.DESede.class)
public class CipherDESedeTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "method",
        args = {}
    )
    public void test_DESedeNoISO() {
        CipherSymmetricKeyThread DESedeNoISO = new CipherSymmetricKeyThread(
                "DESede", new int[] {112, 168},
                new String[] {
                        "ECB", "CBC", "CFB", "CFB8", "CFB16", "CFB24", "CFB32",
                        "CFB40", "CFB48", "CFB56", "CFB64", "OFB", "OFB8",
                        "OFB16", "OFB24", "OFB32", "OFB40", "OFB48", "OFB56",
                        "OFB64"}, new String[] {"NoPadding", "PKCS5Padding"});
        DESedeNoISO.launcher();
        assertEquals(DESedeNoISO.getFailureMessages(), 0, DESedeNoISO
                .getTotalFailuresNumber());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "method",
        args = {}
    )
    public void test_DESedeISO() {
        CipherSymmetricKeyThread DESedeISO = new CipherSymmetricKeyThread(
                "DESede", new int[] {112, 168},
                new String[] {
                        "ECB", "CBC", "CFB", "CFB8", "CFB16", "CFB24", "CFB32",
                        "CFB40", "CFB48", "CFB56", "CFB64", "OFB", "OFB8",
                        "OFB16", "OFB24", "OFB32", "OFB40", "OFB48", "OFB56",
                        "OFB64"}, new String[] {"ISO10126PADDING"});
        DESedeISO.launcher();
        assertEquals(DESedeISO.getFailureMessages(), 0, DESedeISO
                .getTotalFailuresNumber());
    }
}
