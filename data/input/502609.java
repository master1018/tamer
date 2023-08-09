public class KeyGeneratorFunctionalTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.AES.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.DES.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.DESede.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.HMACMD5.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.HMACSHA1.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.HMACSHA256.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.HMACSHA384.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.HMACSHA512.class,
            method = "method",
            args = {}
        )
    })
    public void test_() throws Exception {
        String[] algArray = {"AES", "DES", "DESEDE", "DESede", 
                "HMACMD5", "HmacMD5", "HMACSHA1", "HmacSHA1", "HMACSHA256",
                "HmacSHA256", "HMACSHA384", "HmacSHA384", "HMACSHA512",
                "HmacSHA512"};
        KeyGeneratorThread kgt = new KeyGeneratorThread(algArray);
        kgt.launcher();
        assertEquals(kgt.getFailureMessages(), 0, kgt.getTotalFailuresNumber());
    }
}
