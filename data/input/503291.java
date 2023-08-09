public class Upgrade extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Example", "******************* UPGRADE HERE *******************");
        context.getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, getClass()),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
