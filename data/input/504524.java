public class SecretKeyFactoryFunctionalTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = SecretKeyFactory.DES.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = SecretKeyFactory.DESede.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = SecretKeyFactory.PBEWITHMD5ANDDES.class,
            method = "method",
            args = {}
        )
    })
    public void test_() throws Exception {
        String[] algArray = {"DES", "DESede", "PBEWITHMD5ANDDES"};
        SecretKeyFactoryThread skft = new SecretKeyFactoryThread(algArray);
        skft.launcher();
        assertEquals(skft.getFailureMessages(), 0, skft.getTotalFailuresNumber());
    }
}
