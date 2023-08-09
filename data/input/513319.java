public class AddBookmarkPage extends Activity {
    private final String LOGTAG = "Bookmarks";
    private EditText    mTitle;
    private EditText    mAddress;
    private TextView    mButton;
    private View        mCancelButton;
    private boolean     mEditingExisting;
    private Bundle      mMap;
    private String      mTouchIconUrl;
    private Bitmap      mThumbnail;
    private String      mOriginalUrl;
    private static final int SAVE_BOOKMARK = 100;
    private Handler mHandler;
    private View.OnClickListener mSaveBookmark = new View.OnClickListener() {
        public void onClick(View v) {
            if (save()) {
                finish();
            }
        }
    };
    private View.OnClickListener mCancel = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.browser_add_bookmark);
        setTitle(R.string.save_to_bookmarks);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_list_bookmark);
        String title = null;
        String url = null;
        mMap = getIntent().getExtras();
        if (mMap != null) {
            Bundle b = mMap.getBundle("bookmark");
            if (b != null) {
                mMap = b;
                mEditingExisting = true;
                setTitle(R.string.edit_bookmark);
            }
            title = mMap.getString("title");
            url = mOriginalUrl = mMap.getString("url");
            mTouchIconUrl = mMap.getString("touch_icon_url");
            mThumbnail = (Bitmap) mMap.getParcelable("thumbnail");
        }
        mTitle = (EditText) findViewById(R.id.title);
        mTitle.setText(title);
        mAddress = (EditText) findViewById(R.id.address);
        mAddress.setText(url);
        View.OnClickListener accept = mSaveBookmark;
        mButton = (TextView) findViewById(R.id.OK);
        mButton.setOnClickListener(accept);
        mCancelButton = findViewById(R.id.cancel);
        mCancelButton.setOnClickListener(mCancel);
        if (!getWindow().getDecorView().isInTouchMode()) {
            mButton.requestFocus();
        }
    }
    private class SaveBookmarkRunnable implements Runnable {
        private Message mMessage;
        public SaveBookmarkRunnable(Message msg) {
            mMessage = msg;
        }
        public void run() {
            Bundle bundle = mMessage.getData();
            String title = bundle.getString("title");
            String url = bundle.getString("url");
            boolean invalidateThumbnail = bundle.getBoolean(
                    "invalidateThumbnail");
            Bitmap thumbnail = invalidateThumbnail ? null
                    : (Bitmap) bundle.getParcelable("thumbnail");
            String touchIconUrl = bundle.getString("touchIconUrl");
            try {
                final ContentResolver cr = getContentResolver();
                Bookmarks.addBookmark(null, cr, url, title, thumbnail, true);
                if (touchIconUrl != null) {
                    new DownloadTouchIcon(cr, url).execute(mTouchIconUrl);
                }
                mMessage.arg1 = 1;
            } catch (IllegalStateException e) {
                mMessage.arg1 = 0;
            }
            mMessage.sendToTarget();
        }
    }
    private void createHandler() {
        if (mHandler == null) {
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case SAVE_BOOKMARK:
                            if (1 == msg.arg1) {
                                Toast.makeText(AddBookmarkPage.this, R.string.bookmark_saved,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(AddBookmarkPage.this, R.string.bookmark_not_saved,
                                        Toast.LENGTH_LONG).show();
                            }
                            break;
                    }
                }
            };
        }
    }
    boolean save() {
        createHandler();
        String title = mTitle.getText().toString().trim();
        String unfilteredUrl = 
                BrowserActivity.fixUrl(mAddress.getText().toString());
        boolean emptyTitle = title.length() == 0;
        boolean emptyUrl = unfilteredUrl.trim().length() == 0;
        Resources r = getResources();
        if (emptyTitle || emptyUrl) {
            if (emptyTitle) {
                mTitle.setError(r.getText(R.string.bookmark_needs_title));
            }
            if (emptyUrl) {
                mAddress.setError(r.getText(R.string.bookmark_needs_url));
            }
            return false;
        }
        String url = unfilteredUrl.trim();
        try {
            if (!url.toLowerCase().startsWith("javascript:")) {
                URI uriObj = new URI(url);
                String scheme = uriObj.getScheme();
                if (!Bookmarks.urlHasAcceptableScheme(url)) {
                    if (scheme != null) {
                        mAddress.setError(r.getText(R.string.bookmark_cannot_save_url));
                        return false;
                    }
                    WebAddress address;
                    try {
                        address = new WebAddress(unfilteredUrl);
                    } catch (ParseException e) {
                        throw new URISyntaxException("", "");
                    }
                    if (address.mHost.length() == 0) {
                        throw new URISyntaxException("", "");
                    }
                    url = address.toString();
                }
            }
        } catch (URISyntaxException e) {
            mAddress.setError(r.getText(R.string.bookmark_url_not_valid));
            return false;
        }
        if (mEditingExisting) {
            mMap.putString("title", title);
            mMap.putString("url", url);
            mMap.putBoolean("invalidateThumbnail", !url.equals(mOriginalUrl));
            setResult(RESULT_OK, (new Intent()).setAction(
                    getIntent().toString()).putExtras(mMap));
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("title", title);
            bundle.putString("url", url);
            bundle.putParcelable("thumbnail", mThumbnail);
            bundle.putBoolean("invalidateThumbnail", !url.equals(mOriginalUrl));
            bundle.putString("touchIconUrl", mTouchIconUrl);
            Message msg = Message.obtain(mHandler, SAVE_BOOKMARK);
            msg.setData(bundle);
            Thread t = new Thread(new SaveBookmarkRunnable(msg));
            t.start();
            setResult(RESULT_OK);
            LogTag.logBookmarkAdded(url, "bookmarkview");
        }
        return true;
    }
}
