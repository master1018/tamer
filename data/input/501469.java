public class PhotoAppWidgetBind extends Activity {
    private static final String TAG = "PhotoAppWidgetBind";
    private static final String EXTRA_APPWIDGET_BITMAPS = "com.android.camera.appwidgetbitmaps";
    private App mApp = null;    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mApp = new App(PhotoAppWidgetBind.this);        
        finish();
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        final int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        final ArrayList<Bitmap> bitmaps = extras.getParcelableArrayList(EXTRA_APPWIDGET_BITMAPS);
        if (appWidgetIds == null || bitmaps == null || appWidgetIds.length != bitmaps.size()) {
            Log.e(TAG, "Problem parsing photo widget bind request");
            return;
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        PhotoDatabaseHelper helper = new PhotoDatabaseHelper(this);
        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];
            helper.setPhoto(appWidgetId, bitmaps.get(i));
            RemoteViews views = PhotoAppWidgetProvider.buildUpdate(this, appWidgetId, helper);
            appWidgetManager.updateAppWidget(new int[] { appWidgetId }, views);
        }
        helper.close();
    }
    @Override
    public void onPause() {
        super.onPause();
    	mApp.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
    	mApp.onResume();
    }
    @Override
    public void onDestroy() {
    	mApp.shutdown();
    	super.onDestroy();
    }       
}
