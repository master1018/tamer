public class DisableKeyguardReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        KeyguardManager keyguardManager =
            (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        keyguardManager.newKeyguardLock("cts").disableKeyguard();
    }
}
