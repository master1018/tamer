public class DeleteImage extends NoSearchActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "DeleteImage";
    private ProgressBar mProgressBar;
    private ArrayList<Uri> mUriList;  
    private int mIndex = 0;  
    private final Handler mHandler = new Handler();
    private final Runnable mDeleteNextRunnable = new Runnable() {
        public void run() {
            deleteNext();
        }
    };
    private ContentResolver mContentResolver;
    private boolean mPausing;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mUriList = intent.getParcelableArrayListExtra("delete-uris");
        if (mUriList == null) {
            finish();
        }
        setContentView(R.layout.delete_image);
        mProgressBar = (ProgressBar) findViewById(R.id.delete_progress);
        mContentResolver = getContentResolver();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mPausing = false;
        mHandler.post(mDeleteNextRunnable);
    }
    private void deleteNext() {
        if (mPausing) return;
        if (mIndex >= mUriList.size()) {
            finish();
            return;
        }
        Uri uri = mUriList.get(mIndex++);
        mProgressBar.setProgress(mIndex * 10000 / mUriList.size());
        mContentResolver.delete(uri, null, null);
        mHandler.post(mDeleteNextRunnable);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mPausing = true;
    }
}
