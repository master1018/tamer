@TestTargetClass(SmsMessage.SubmitPdu.class)
public class SmsMessage_SubmitPduTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "toString",
            args = {}
        )
    })
    public void testToString() {
        SmsMessage.SubmitPdu sp = new SmsMessage.SubmitPdu();
        assertNotNull(sp.toString());
    }
}
