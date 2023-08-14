public class DeleteFdnContactScreen extends Activity {
    private static final String LOG_TAG = PhoneApp.LOG_TAG;
    private static final boolean DBG = false;
    private static final String INTENT_EXTRA_NAME = "name";
    private static final String INTENT_EXTRA_NUMBER = "number";
    private static final int PIN2_REQUEST_CODE = 100;
    private String mName;
    private String mNumber;
    private String mPin2;
    protected QueryHandler mQueryHandler;
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        resolveIntent();
        authenticatePin2();
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.delete_fdn_contact_screen);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (DBG) log("onActivityResult");
        switch (requestCode) {
            case PIN2_REQUEST_CODE:
                Bundle extras = (intent != null) ? intent.getExtras() : null;
                if (extras != null) {
                    mPin2 = extras.getString("pin2");
                    showStatus(getResources().getText(
                            R.string.deleting_fdn_contact));
                    deleteContact();
                } else {
                    if (DBG) log("onActivityResult: CANCELLED");
                    displayProgress(false);
                    finish();
                }
                break;
        }
    }
    private void resolveIntent() {
        Intent intent = getIntent();
        mName =  intent.getStringExtra(INTENT_EXTRA_NAME);
        mNumber =  intent.getStringExtra(INTENT_EXTRA_NUMBER);
        if (TextUtils.isEmpty(mName)) {
            finish();
        }
    }
    private void deleteContact() {
        StringBuilder buf = new StringBuilder();
        buf.append("tag='");
        buf.append(mName);
        buf.append("' AND number='");
        buf.append(mNumber);
        buf.append("' AND pin2='");
        buf.append(mPin2);
        buf.append("'");
        Uri uri = Uri.parse("content:
        mQueryHandler = new QueryHandler(getContentResolver());
        mQueryHandler.startDelete(0, null, uri, buf.toString(), null);
        displayProgress(true);
    }
    private void authenticatePin2() {
        Intent intent = new Intent();
        intent.setClass(this, GetPin2Screen.class);
        startActivityForResult(intent, PIN2_REQUEST_CODE);
    }
    private void displayProgress(boolean flag) {
        getWindow().setFeatureInt(
                Window.FEATURE_INDETERMINATE_PROGRESS,
                flag ? PROGRESS_VISIBILITY_ON : PROGRESS_VISIBILITY_OFF);
    }
    private void showStatus(CharSequence statusMsg) {
        if (statusMsg != null) {
            Toast.makeText(this, statusMsg, Toast.LENGTH_SHORT)
            .show();
        }
    }
    private void handleResult(boolean success) {
        if (success) {
            if (DBG) log("handleResult: success!");
            showStatus(getResources().getText(R.string.fdn_contact_deleted));
        } else {
            if (DBG) log("handleResult: failed!");
            showStatus(getResources().getText(R.string.pin2_invalid));
        }
        mHandler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 2000);
    }
    private class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor c) {
        }
        protected void onInsertComplete(int token, Object cookie,
                                        Uri uri) {
        }
        protected void onUpdateComplete(int token, Object cookie, int result) {
        }
        protected void onDeleteComplete(int token, Object cookie, int result) {
            if (DBG) log("onDeleteComplete");
            displayProgress(false);
            handleResult(result > 0);
        }
    }
    private void log(String msg) {
        Log.d(LOG_TAG, "[DeleteFdnContact] " + msg);
    }
}
