public class OneShotAlarm extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Toast.makeText(context, R.string.one_shot_received, Toast.LENGTH_SHORT).show();
    }
}
