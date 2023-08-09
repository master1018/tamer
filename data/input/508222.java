public class NoCallPermissionTest extends AndroidTestCase {
    @SmallTest
    public void testActionCall() {
        Uri uri = Uri.parse("tel:123456");
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(intent);
            fail("startActivity(Intent.ACTION_CALL) did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testCallVoicemail() {
        try {
            Intent intent = new Intent("android.intent.action.CALL_PRIVILEGED",
                    Uri.fromParts("voicemail", "", null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            fail("startActivity(Intent.ACTION_CALL_PRIVILEGED) did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testCall911() {
        Intent intent = new Intent("android.intent.action.CALL_PRIVILEGED", Uri.parse("tel:911"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(intent);
            fail("startActivity(Intent.ACTION_CALL_PRIVILEGED) did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
}
