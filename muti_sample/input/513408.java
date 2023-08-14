public class IntentSenderTest extends BroadcastTest {
    public void testRegisteredReceivePermissionGranted() throws Exception {
        setExpectedReceivers(new String[]{RECEIVER_REG});
        registerMyReceiver(new IntentFilter(BROADCAST_REGISTERED), PERMISSION_GRANTED);
        addIntermediate("after-register");
        PendingIntent is = PendingIntent.getBroadcast(getContext(), 0,
                makeBroadcastIntent(BROADCAST_REGISTERED), 0);
        is.send();
        waitForResultOrThrow(BROADCAST_TIMEOUT);
        is.cancel();
    }
    public void testRegisteredReceivePermissionDenied() throws Exception {
        final Intent intent = makeBroadcastIntent(BROADCAST_REGISTERED);
        setExpectedReceivers(new String[]{RECEIVER_RESULTS});
        registerMyReceiver(new IntentFilter(BROADCAST_REGISTERED), PERMISSION_DENIED);
        addIntermediate("after-register");
        PendingIntent.OnFinished finish = new PendingIntent.OnFinished() {
            public void onSendFinished(PendingIntent pi, Intent intent,
                    int resultCode, String resultData, Bundle resultExtras) {
                gotReceive(RECEIVER_RESULTS, intent);
            }
        };
        PendingIntent is = PendingIntent.getBroadcast(getContext(), 0, intent, 0);
        is.send(Activity.RESULT_CANCELED, finish, null);
        waitForResultOrThrow(BROADCAST_TIMEOUT);
        is.cancel();
    }
    public void testLocalReceivePermissionGranted() throws Exception {
        setExpectedReceivers(new String[]{RECEIVER_LOCAL});
        PendingIntent is = PendingIntent.getBroadcast(getContext(), 0,
                makeBroadcastIntent(BROADCAST_LOCAL_GRANTED), 0);
        is.send();
        waitForResultOrThrow(BROADCAST_TIMEOUT);
        is.cancel();
    }
    public void testLocalReceivePermissionDenied() throws Exception {
        final Intent intent = makeBroadcastIntent(BROADCAST_LOCAL_DENIED);
        setExpectedReceivers(new String[]{RECEIVER_RESULTS});
        PendingIntent.OnFinished finish = new PendingIntent.OnFinished() {
            public void onSendFinished(PendingIntent pi, Intent intent,
                    int resultCode, String resultData, Bundle resultExtras) {
                gotReceive(RECEIVER_RESULTS, intent);
            }
        };
        PendingIntent is = PendingIntent.getBroadcast(getContext(), 0, intent, 0);
        is.send(Activity.RESULT_CANCELED, finish, null);
        waitForResultOrThrow(BROADCAST_TIMEOUT);
        is.cancel();
    }
}
