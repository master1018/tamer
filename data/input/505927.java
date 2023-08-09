public class SmsManagerPermissionTest extends AndroidTestCase {
    private static final String MSG_CONTENTS = "hi";
    private static final short DEST_PORT = (short)1004;
    private static final String DEST_NUMBER = "4567";
    private static final String SRC_NUMBER = "1234";
    @SmallTest
    public void testSendTextMessage() {
        try {
            SmsManager.getDefault().sendTextMessage(SRC_NUMBER, DEST_NUMBER, MSG_CONTENTS, null,
                    null);
            fail("SmsManager.sendTextMessage did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testSendDataMessage() {
        try {
            SmsManager.getDefault().sendDataMessage(SRC_NUMBER, DEST_NUMBER, DEST_PORT,
                    MSG_CONTENTS.getBytes(), null, null);
            fail("SmsManager.sendDataMessage did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testSendMultipartMessage() {
        try {
            ArrayList<String> msgParts = new ArrayList<String>(2);
            msgParts.add(MSG_CONTENTS);
            msgParts.add("foo");
            SmsManager.getDefault().sendMultipartTextMessage(SRC_NUMBER, DEST_NUMBER, msgParts,
                    null, null);
            fail("SmsManager.sendMultipartTextMessage did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
}
