@TestTargetClass(Cipher.AES.class)
public class CipherAesTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "method",
        args = {}
    )
    public void test_AesNoISO() {
        CipherSymmetricKeyThread aesNoISO = new CipherSymmetricKeyThread("AES",
                new int[] {128, 192, 256}, 
                new String[] {
                        "ECB", "CBC", "CFB", "CFB8", "CFB16", "CFB24", "CFB32",
                        "CFB40", "CFB48", "CFB56", "CFB64", "CFB72", "CFB80",
                        "CFB88", "CFB96", "CFB104", "CFB112", "CFB120",
                        "CFB128", "OFB", "OFB8", "OFB16", "OFB24", "OFB32",
                        "OFB40", "OFB48", "OFB56", "OFB64", "OFB72", "OFB80",
                        "OFB88", "OFB96", "OFB104", "OFB112", "OFB120",
                        "OFB128"}, new String[] {"NoPadding", "PKCS5Padding"});
        aesNoISO.launcher();
        assertEquals(aesNoISO.getFailureMessages(), 0, aesNoISO
                .getTotalFailuresNumber());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "method",
        args = {}
    )
    public void test_AesISO() {
        CipherSymmetricKeyThread aesISO = new CipherSymmetricKeyThread("AES",
                new int[] {128, 192, 256}, 
                new String[] {
                        "ECB", "CBC", "CFB", "CFB8", "CFB16", "CFB24", "CFB32",
                        "CFB40", "CFB48", "CFB56", "CFB64", "CFB72", "CFB80",
                        "CFB88", "CFB96", "CFB104", "CFB112", "CFB120",
                        "CFB128", "OFB", "OFB8", "OFB16", "OFB24", "OFB32",
                        "OFB40", "OFB48", "OFB56", "OFB64", "OFB72", "OFB80",
                        "OFB88", "OFB96", "OFB104", "OFB112", "OFB120",
                        "OFB128"}, new String[] {"ISO10126PADDING"});
        aesISO.launcher();
        assertEquals(aesISO.getFailureMessages(), 0, aesISO
                .getTotalFailuresNumber());
    }
}
