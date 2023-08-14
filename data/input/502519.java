public class HighPriorityBroadcastReceiver extends ResultReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Got interrupted during wait()", e);
        }
    }
}
