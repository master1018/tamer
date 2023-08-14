public class ProtipWidget extends AppWidgetProvider {
    public static final String ACTION_NEXT_TIP = "com.android.misterwidget.NEXT_TIP";
    public static final String ACTION_POKE = "com.android.misterwidget.HEE_HEE";
    public static final String EXTRA_TIMES = "times";
    public static final String PREFS_NAME = "Protips";
    public static final String PREFS_TIP_NUMBER = "widget_tip";
    private static Random sRNG = new Random();
    private static final Pattern sNewlineRegex = Pattern.compile(" *\\n *");
    private static final Pattern sDrawableRegex = Pattern.compile(" *@(drawable/[a-z0-9_]+) *");
    private int mIconRes = R.drawable.droidman_open;
    private int mMessage = 0;
    private AppWidgetManager mWidgetManager = null;
    private int[] mWidgetIds;
    private Context mContext;
    private CharSequence[] mTips;
    private void setup(Context context) {
        mContext = context;
        mWidgetManager = AppWidgetManager.getInstance(context);
        mWidgetIds = mWidgetManager.getAppWidgetIds(new ComponentName(context, ProtipWidget.class));
        SharedPreferences pref = context.getSharedPreferences(PREFS_NAME, 0);
        mMessage = pref.getInt(PREFS_TIP_NUMBER, 0);
        mTips = context.getResources().getTextArray(R.array.tips);
        if (mTips != null) {
            if (mMessage >= mTips.length) mMessage = 0;
        } else {
            mMessage = -1;
        }
    }
    public void goodmorning() {
        mMessage = -1;
        try {
            setIcon(R.drawable.droidman_down_closed);
            Thread.sleep(500);
            setIcon(R.drawable.droidman_down_open);
            Thread.sleep(200);
            setIcon(R.drawable.droidman_down_closed);
            Thread.sleep(100);
            setIcon(R.drawable.droidman_down_open);
            Thread.sleep(600);
        } catch (InterruptedException ex) {
        }
        mMessage = 0;
        mIconRes = R.drawable.droidman_open;
        refresh();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        setup(context);
        if (intent.getAction().equals(ACTION_NEXT_TIP)) {
            mMessage = getNextMessageIndex();
            SharedPreferences.Editor pref = context.getSharedPreferences(PREFS_NAME, 0).edit();
            pref.putInt(PREFS_TIP_NUMBER, mMessage);
            pref.commit();
            refresh();
        } else if (intent.getAction().equals(ACTION_POKE)) {
            blink(intent.getIntExtra(EXTRA_TIMES, 1));
        } else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {
            goodmorning();
        } else {
            mIconRes = R.drawable.droidman_open;
            refresh();
        }
    }
    private void refresh() {
        RemoteViews rv = buildUpdate(mContext);
        for (int i : mWidgetIds) {
            mWidgetManager.updateAppWidget(i, rv);
        }
    }
    private void setIcon(int resId) {
        mIconRes = resId;
        refresh();
    }
    private int getNextMessageIndex() {
        return (mMessage + 1) % mTips.length;
    }
    private void blink(int blinks) {
        if (mMessage < 0) return;
        setIcon(R.drawable.droidman_closed);
        try {
            Thread.sleep(100);
            while (0<--blinks) {
                setIcon(R.drawable.droidman_open);
                Thread.sleep(200);
                setIcon(R.drawable.droidman_closed);
                Thread.sleep(100);
            }
        } catch (InterruptedException ex) { }
        setIcon(R.drawable.droidman_open);
    }
    public RemoteViews buildUpdate(Context context) {
        RemoteViews updateViews = new RemoteViews(
            context.getPackageName(), R.layout.widget);
        Intent bcast = new Intent(context, ProtipWidget.class);
        bcast.setAction(ACTION_NEXT_TIP);
        PendingIntent pending = PendingIntent.getBroadcast(
            context, 0, bcast, PendingIntent.FLAG_UPDATE_CURRENT);
        updateViews.setOnClickPendingIntent(R.id.tip_bubble, pending);
        bcast = new Intent(context, ProtipWidget.class);
        bcast.setAction(ACTION_POKE);
        bcast.putExtra(EXTRA_TIMES, 1);
        pending = PendingIntent.getBroadcast(
            context, 0, bcast, PendingIntent.FLAG_UPDATE_CURRENT);
        updateViews.setOnClickPendingIntent(R.id.bugdroid, pending);
        if (mMessage >= 0) {
            String[] parts = sNewlineRegex.split(mTips[mMessage], 2);
            String title = parts[0];
            String text = parts.length > 1 ? parts[1] : "";
            Matcher m = sDrawableRegex.matcher(text);
            if (m.find()) {
                String imageName = m.group(1);
                int resId = context.getResources().getIdentifier(
                    imageName, null, context.getPackageName());
                updateViews.setImageViewResource(R.id.tip_callout, resId);
                updateViews.setViewVisibility(R.id.tip_callout, View.VISIBLE);
                text = m.replaceFirst("");
            } else {
                updateViews.setImageViewResource(R.id.tip_callout, 0);
                updateViews.setViewVisibility(R.id.tip_callout, View.GONE);
            }
            updateViews.setTextViewText(R.id.tip_message, 
                text);
            updateViews.setTextViewText(R.id.tip_header,
                title);
            updateViews.setTextViewText(R.id.tip_footer, 
                context.getResources().getString(
                    R.string.pager_footer,
                    (1+mMessage), mTips.length));
            updateViews.setViewVisibility(R.id.tip_bubble, View.VISIBLE);
        } else {
            updateViews.setViewVisibility(R.id.tip_bubble, View.INVISIBLE);
        }
        updateViews.setImageViewResource(R.id.bugdroid, mIconRes);
        return updateViews;
    }
}
