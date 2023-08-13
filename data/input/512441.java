public class QuakeActivity extends Activity {
    QuakeView mQuakeView;
    static QuakeLib mQuakeLib;
    boolean mKeepScreenOn = true;
    @Override protected void onCreate(Bundle icicle) {
        Log.i("QuakeActivity", "onCreate");
        super.onCreate(icicle);
        if (USE_DOWNLOADER) {
            if (! DownloaderActivity.ensureDownloaded(this,
                    getString(R.string.quake_customDownloadText), FILE_CONFIG_URL,
                    CONFIG_VERSION, SDCARD_DATA_PATH, USER_AGENT)) {
                return;
            }
        }
        if (foundQuakeData()) {
            if (mQuakeLib == null) {
                mQuakeLib = new QuakeLib();
                if(! mQuakeLib.init()) {
                    setContentView(new QuakeViewNoData(
                            getApplication(),
                            QuakeViewNoData.E_INITFAILED));
                    return;
                }
            }
            if (mKeepScreenOn) {
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            if (mQuakeView == null) {
                mQuakeView = new
                QuakeView(getApplication());
                mQuakeView.setQuakeLib(mQuakeLib);
            }
            setContentView(mQuakeView);
        }
        else {
            setContentView(new QuakeViewNoData(getApplication(),
                            QuakeViewNoData.E_NODATA));
        }
    }
    @Override protected void onPause() {
        super.onPause();
        if (mQuakeView != null) {
            mQuakeView.onPause();
        }
    }
    @Override protected void onResume() {
        super.onResume();
        if (mQuakeView != null) {
            mQuakeView.onResume();
        }
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        if (mQuakeLib != null) {
            mQuakeLib.quit();
        }
    }
    private boolean foundQuakeData() {
        return fileExists(SDCARD_DATA_PATH + PAK0_PATH)
		|| fileExists(INTERNAL_DATA_PATH + PAK0_PATH);
    }
    private boolean fileExists(String s) {
        File f = new File(s);
        return f.exists();
    }
    private final static boolean USE_DOWNLOADER = false;
    private final static String FILE_CONFIG_URL =
        "http:
    private final static String CONFIG_VERSION = "1.1";
    private final static String SDCARD_DATA_PATH = "/sdcard/data/quake";
    private final static String INTERNAL_DATA_PATH = "/data/quake";
    private final static String PAK0_PATH = "/id1/pak0.pak";
    private final static String USER_AGENT = "Android Quake Downloader";
}
