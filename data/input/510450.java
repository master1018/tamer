public class AlarmController extends Activity {
    Toast mToast;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_controller);
        Button button = (Button)findViewById(R.id.one_shot);
        button.setOnClickListener(mOneShotListener);
        button = (Button)findViewById(R.id.start_repeating);
        button.setOnClickListener(mStartRepeatingListener);
        button = (Button)findViewById(R.id.stop_repeating);
        button.setOnClickListener(mStopRepeatingListener);
    }
    private OnClickListener mOneShotListener = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(AlarmController.this, OneShotAlarm.class);
            PendingIntent sender = PendingIntent.getBroadcast(AlarmController.this,
                    0, intent, 0);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.SECOND, 30);
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(AlarmController.this, R.string.one_shot_scheduled,
                    Toast.LENGTH_LONG);
            mToast.show();
        }
    };
    private OnClickListener mStartRepeatingListener = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(AlarmController.this, RepeatingAlarm.class);
            PendingIntent sender = PendingIntent.getBroadcast(AlarmController.this,
                    0, intent, 0);
            long firstTime = SystemClock.elapsedRealtime();
            firstTime += 15*1000;
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            firstTime, 15*1000, sender);
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(AlarmController.this, R.string.repeating_scheduled,
                    Toast.LENGTH_LONG);
            mToast.show();
        }
    };
    private OnClickListener mStopRepeatingListener = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(AlarmController.this, RepeatingAlarm.class);
            PendingIntent sender = PendingIntent.getBroadcast(AlarmController.this,
                    0, intent, 0);
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            am.cancel(sender);
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(AlarmController.this, R.string.repeating_unscheduled,
                    Toast.LENGTH_LONG);
            mToast.show();
        }
    };
}
