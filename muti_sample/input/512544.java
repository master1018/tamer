public class MacFunctionalTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = Mac.HMACMD5.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = Mac.HMACSHA1.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = Mac.HMACSHA256.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = Mac.HMACSHA384.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = Mac.HMACSHA512.class,
            method = "method",
            args = {}
        )
    })
    public void test_Mac() throws Exception {
        String[] algArray = {"HMACSHA1", "HMACSHA256", "HMACSHA384",
                "HMACSHA512", "HMACMD5"};
        MacThread mt = new MacThread(algArray);
        mt.launcher();
        assertEquals(mt.getFailureMessages(), 0, mt.getTotalFailuresNumber());
    }
}
