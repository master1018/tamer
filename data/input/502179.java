public class IncomingMessage extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.incoming_message);
        Button button = (Button) findViewById(R.id.notify);
        button.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    showToast();
                    showNotification();
                }
            });
    }
    protected void showToast() {
        View view = inflateView(R.layout.incoming_message_panel);
        TextView tv = (TextView)view.findViewById(R.id.message);
        tv.setText("khtx. meet u for dinner. cul8r");
        Toast toast = new Toast(this);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }
    private View inflateView(int resource) {
        LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return vi.inflate(resource, null);
    }
    protected void showNotification() {
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        CharSequence from = "Joe";
        CharSequence message = "kthx. meet u for dinner. cul8r";
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, IncomingMessageView.class), 0);
        String tickerText = getString(R.string.imcoming_message_ticker_text, message);
        Notification notif = new Notification(R.drawable.stat_sample, tickerText,
                System.currentTimeMillis());
        notif.setLatestEventInfo(this, from, message, contentIntent);
        notif.vibrate = new long[] { 100, 250, 100, 500};
        nm.notify(R.string.imcoming_message_ticker_text, notif);
    }
}
