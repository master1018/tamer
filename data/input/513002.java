public class PhotoAppWidgetConfigure extends Activity {
    @SuppressWarnings("unused")
    private static final String TAG = "PhotoAppWidgetConfigure";
    static final int REQUEST_GET_PHOTO = 2;
    private App mApp = null; 
    int mAppWidgetId = -1;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mApp = new App(PhotoAppWidgetConfigure.this);
        mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        if (mAppWidgetId == -1) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 192);
        intent.putExtra("outputY", 192);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUEST_GET_PHOTO);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && mAppWidgetId != -1) {
            Bitmap bitmap = (Bitmap) data.getParcelableExtra("data");
            PhotoDatabaseHelper helper = new PhotoDatabaseHelper(this);
            if (helper.setPhoto(mAppWidgetId, bitmap)) {
                resultCode = Activity.RESULT_OK;
                RemoteViews views = PhotoAppWidgetProvider.buildUpdate(this, mAppWidgetId, helper);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                appWidgetManager.updateAppWidget(new int[] { mAppWidgetId }, views);
            }
            helper.close();
        } else {
            resultCode = Activity.RESULT_CANCELED;
        }
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(resultCode, resultValue);
        finish();
    }
}
