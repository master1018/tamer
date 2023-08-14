public class AlarmService extends Activity {
    private PendingIntent mAlarmSender;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlarmSender = PendingIntent.getService(AlarmService.this,
                0, new Intent(AlarmService.this, AlarmService_Service.class), 0);
        setContentView(R.layout.alarm_service);
        Button button = (Button)findViewById(R.id.start_alarm);
        button.setOnClickListener(mStartAlarmListener);
        button = (Button)findViewById(R.id.stop_alarm);
        button.setOnClickListener(mStopAlarmListener);
    }
    private OnClickListener mStartAlarmListener = new OnClickListener() {
        public void onClick(View v) {
            long firstTime = SystemClock.elapsedRealtime();
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            firstTime, 30*1000, mAlarmSender);
            Toast.makeText(AlarmService.this, R.string.repeating_scheduled,
                    Toast.LENGTH_LONG).show();
        }
    };
    private OnClickListener mStopAlarmListener = new OnClickListener() {
        public void onClick(View v) {
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            am.cancel(mAlarmSender);
            Toast.makeText(AlarmService.this, R.string.repeating_unscheduled,
                    Toast.LENGTH_LONG).show();
        }
    };
}
