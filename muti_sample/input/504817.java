public class NoReceiveGsmSmsPermissionTest extends NoReceiveSmsPermissionTest {
    protected void sendSms(PendingIntent sentIntent, PendingIntent deliveryIntent,
            String currentNumber) {
        SmsManager.getDefault().sendTextMessage(currentNumber, null, "test message",
                sentIntent, deliveryIntent);
    }
}
