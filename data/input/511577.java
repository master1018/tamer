public class CalendarDebugReceiver extends BroadcastReceiver {
    public CalendarDebugReceiver() {
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setClass(context, CalendarDebug.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
