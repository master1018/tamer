public class StatusBarNotifications extends Activity {
    private NotificationManager mNotificationManager;
    private static int MOOD_NOTIFICATIONS = R.layout.status_bar_notifications;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_bar_notifications);
        Button button;
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        button = (Button) findViewById(R.id.happy);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setMood(R.drawable.stat_happy, R.string.status_bar_notifications_happy_message,
                        false);
            }
        });
        button = (Button) findViewById(R.id.neutral);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setMood(R.drawable.stat_neutral, R.string.status_bar_notifications_ok_message,
                        false);
            }
        });
        button = (Button) findViewById(R.id.sad);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setMood(R.drawable.stat_sad, R.string.status_bar_notifications_sad_message, false);
            }
        });
        button = (Button) findViewById(R.id.happyMarquee);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setMood(R.drawable.stat_happy, R.string.status_bar_notifications_happy_message,
                        true);
            }
        });
        button = (Button) findViewById(R.id.neutralMarquee);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setMood(R.drawable.stat_neutral, R.string.status_bar_notifications_ok_message, true);
            }
        });
        button = (Button) findViewById(R.id.sadMarquee);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setMood(R.drawable.stat_sad, R.string.status_bar_notifications_sad_message, true);
            }
        });
        button = (Button) findViewById(R.id.happyViews);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setMoodView(R.drawable.stat_happy, R.string.status_bar_notifications_happy_message);
            }
        });
        button = (Button) findViewById(R.id.neutralViews);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setMoodView(R.drawable.stat_neutral, R.string.status_bar_notifications_ok_message);
            }
        });
        button = (Button) findViewById(R.id.sadViews);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setMoodView(R.drawable.stat_sad, R.string.status_bar_notifications_sad_message);
            }
        });
        button = (Button) findViewById(R.id.defaultSound);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setDefault(Notification.DEFAULT_SOUND);
            }
        });
        button = (Button) findViewById(R.id.defaultVibrate);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setDefault(Notification.DEFAULT_VIBRATE);
            }
        });
        button = (Button) findViewById(R.id.defaultAll);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setDefault(Notification.DEFAULT_ALL);
            }
        });
        button = (Button) findViewById(R.id.clear);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mNotificationManager.cancel(R.layout.status_bar_notifications);
            }
        });
    }
    private PendingIntent makeMoodIntent(int moodId) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotificationDisplay.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("moodimg", moodId),
                PendingIntent.FLAG_UPDATE_CURRENT);
        return contentIntent;
    }
    private void setMood(int moodId, int textId, boolean showTicker) {
        CharSequence text = getText(textId);
        String tickerText = showTicker ? getString(textId) : null;
        Notification notification = new Notification(moodId, tickerText,
                System.currentTimeMillis());
        notification.setLatestEventInfo(this, getText(R.string.status_bar_notifications_mood_title),
                       text, makeMoodIntent(moodId));
        mNotificationManager.notify(R.layout.status_bar_notifications, notification);
    }
    private void setMoodView(int moodId, int textId) {
        Notification notif = new Notification();
        notif.contentIntent = makeMoodIntent(moodId);
        CharSequence text = getText(textId);
        notif.tickerText = text;
        notif.icon = moodId;
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.status_bar_balloon);
        contentView.setTextViewText(R.id.text, text);
        contentView.setImageViewResource(R.id.icon, moodId);
        notif.contentView = contentView;
        mNotificationManager.notify(R.layout.status_bar_notifications, notif);
    }
    private void setDefault(int defaults) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, StatusBarNotifications.class), 0);
        CharSequence text = getText(R.string.status_bar_notifications_happy_message);
        final Notification notification = new Notification(
                R.drawable.stat_happy,       
                text,                        
                System.currentTimeMillis()); 
        notification.setLatestEventInfo(
                this,                        
                getText(R.string.status_bar_notifications_mood_title),
                text,                        
                contentIntent);              
        notification.defaults = defaults;
        mNotificationManager.notify(
                   R.layout.status_bar_notifications, 
                   notification);                     
    }    
}
