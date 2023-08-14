@TestTargetClass(Cipher.DES.class)
public class CipherDesTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "method",
        args = {}
    )
    public void test_DesNoISO() {
        CipherSymmetricKeyThread desNoISO = new CipherSymmetricKeyThread("DES",
                new int[] {56},
                new String[] {
                        "ECB", "CBC", "CFB", "CFB8", "CFB16", "CFB24", "CFB32",
                        "CFB40", "CFB48", "CFB56", "CFB64", "OFB", "OFB8",
                        "OFB16", "OFB24", "OFB32", "OFB40", "OFB48", "OFB56",
                        "OFB64"}, new String[] {"NoPadding", "PKCS5Padding"});
        desNoISO.launcher();
        assertEquals(desNoISO.getFailureMessages(), 0, desNoISO
                .getTotalFailuresNumber());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "method",
        args = {}
    )
    public void test_DesISO() {
        CipherSymmetricKeyThread desISO = new CipherSymmetricKeyThread("DES",
                new int[] {56},
                new String[] {
                        "ECB", "CBC", "CFB", "CFB8", "CFB16", "CFB24", "CFB32",
                        "CFB40", "CFB48", "CFB56", "CFB64", "OFB", "OFB8",
                        "OFB16", "OFB24", "OFB32", "OFB40", "OFB48", "OFB56",
                        "OFB64"}, new String[] {"ISO10126PADDING"});
        desISO.launcher();
        assertEquals(desISO.getFailureMessages(), 0, desISO
                .getTotalFailuresNumber());
    }
}
