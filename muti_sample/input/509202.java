@TestTargetClass(SmsMessage.MessageClass.class)
public class SmsMessage_MessageClassTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "valueOf",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "values",
            args = {}
        )
    })
    public void testMessageClass() {
    }
}
