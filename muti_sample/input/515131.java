@TestTargetClass(SmsManager.class)
public class SmsManagerTest extends android.telephony.cts.SmsManagerTest {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDefault",
        args = {}
    )
    public void testGetDefault() {
        assertNotNull(getSmsManager());
    }
    @Override
    protected ArrayList<String> divideMessage(String text) {
        return getSmsManager().divideMessage(text);
    }
    private android.telephony.gsm.SmsManager getSmsManager() {
        return android.telephony.gsm.SmsManager.getDefault();
    }
    @Override
    protected void sendMultiPartTextMessage(String destAddr, ArrayList<String> parts,
            ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        getSmsManager().sendMultipartTextMessage(destAddr, null, parts, sentIntents, deliveryIntents);
    }
    @Override
    protected void sendDataMessage(String destAddr,short port, byte[] data, PendingIntent sentIntent, PendingIntent deliveredIntent) {
        getSmsManager().sendDataMessage(destAddr, null, port, data, sentIntent, deliveredIntent);
    }
    @Override
    protected void sendTextMessage(String destAddr, String text, PendingIntent sentIntent, PendingIntent deliveredIntent) {
        getSmsManager().sendTextMessage(destAddr, null, text, sentIntent, deliveredIntent);
    }
}