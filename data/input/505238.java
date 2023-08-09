public class RepeatingAlarm extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Toast.makeText(context, R.string.repeating_received, Toast.LENGTH_SHORT).show();
    }
}
