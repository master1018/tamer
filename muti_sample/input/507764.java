public class BugReportPreviewActivity extends Activity {
    private static final String TAG = "BugReportPreview";
    private TextView mText;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(com.android.bugreportsender.R.layout.bugreport_preview);
        mText = (TextView) findViewById(R.id.preview);
        if (icicle != null && icicle.getString("text") != null) {
            mText.setText(icicle.getString("text"));
        } else {
            Intent intent = getIntent();
            if (intent == null) {
                Log.w(TAG, "No intent provided.");
                return;
            }
            Uri uri = intent.getData();
            String section = intent.getStringExtra("section");
            if (section == null || section.length() == 0) {
                section = "SYSTEM LOG";
            }
            Log.d(TAG, "Loading " + uri);
            InputStream in = null;
            try {
                in = getContentResolver().openInputStream(uri);
                String text = BugReportParser.extractSystemLogs(in, section);
                mText.setText(text);
            } catch (FileNotFoundException fnfe) {
                Log.w(TAG, "Unable to open file: " + uri, fnfe);
		mText.setText("Unable to open file: " + uri + ": " + fnfe);
                return;
            } catch (IOException ioe) {
                Log.w(TAG, "Unable to process log.", ioe);
		mText.setText("Unable to process log: " + ioe);
		return;
            } finally {
                try {
                    if (in != null) {
			in.close();
		    }
                } catch (IOException ioe) {
                }
            }
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle icicle) {
        CharSequence text = mText.getText();
        if (text != null) {
            icicle.putString("text", mText.getText().toString());
        }
    }
}
