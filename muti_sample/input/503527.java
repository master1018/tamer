public class LiveWallpaperPreview extends Activity {
    static final String EXTRA_LIVE_WALLPAPER_INTENT = "android.live_wallpaper.intent";
    static final String EXTRA_LIVE_WALLPAPER_SETTINGS = "android.live_wallpaper.settings";
    static final String EXTRA_LIVE_WALLPAPER_PACKAGE = "android.live_wallpaper.package";
    private static final String LOG_TAG = "LiveWallpaperPreview";
    private WallpaperManager mWallpaperManager;
    private WallpaperConnection mWallpaperConnection;
    private String mSettings;
    private String mPackageName;
    private Intent mWallpaperIntent;
    private View mView;
    private Dialog mDialog;
    static void showPreview(Activity activity, int code, Intent intent, WallpaperInfo info) {
        Intent preview = new Intent(activity, LiveWallpaperPreview.class);
        preview.putExtra(EXTRA_LIVE_WALLPAPER_INTENT, intent);
        preview.putExtra(EXTRA_LIVE_WALLPAPER_SETTINGS, info.getSettingsActivity());
        preview.putExtra(EXTRA_LIVE_WALLPAPER_PACKAGE, info.getPackageName());
        activity.startActivityForResult(preview, code);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        mWallpaperIntent = (Intent) extras.get(EXTRA_LIVE_WALLPAPER_INTENT);
        if (mWallpaperIntent == null) {
            setResult(RESULT_CANCELED);
            finish();
        }
        setContentView(R.layout.live_wallpaper_preview);
        mView = findViewById(R.id.configure);
        mSettings = extras.getString(EXTRA_LIVE_WALLPAPER_SETTINGS);
        mPackageName = extras.getString(EXTRA_LIVE_WALLPAPER_PACKAGE);
        if (mSettings == null) {
            mView.setVisibility(View.GONE);
        }
        mWallpaperManager = WallpaperManager.getInstance(this);
        mWallpaperConnection = new WallpaperConnection(mWallpaperIntent);
    }
    public void setLiveWallpaper(View v) {
        try {
            mWallpaperManager.getIWallpaperManager().setWallpaperComponent(
                    mWallpaperIntent.getComponent());
            mWallpaperManager.setWallpaperOffsetSteps(0.5f, 0.0f);
            mWallpaperManager.setWallpaperOffsets(v.getRootView().getWindowToken(), 0.5f, 0.0f);
            setResult(RESULT_OK);
        } catch (RemoteException e) {
        } catch (RuntimeException e) {
            Log.w(LOG_TAG, "Failure setting wallpaper", e);
        }
        finish();
    }
    @SuppressWarnings({"UnusedDeclaration"})
    public void configureLiveWallpaper(View v) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(mPackageName, mSettings));
        intent.putExtra(WallpaperSettingsActivity.EXTRA_PREVIEW_MODE, true);
        startActivity(intent);
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mWallpaperConnection != null && mWallpaperConnection.mEngine != null) {
            try {
                mWallpaperConnection.mEngine.setVisibility(true);
            } catch (RemoteException e) {
            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mWallpaperConnection != null && mWallpaperConnection.mEngine != null) {
            try {
                mWallpaperConnection.mEngine.setVisibility(false);
            } catch (RemoteException e) {
            }
        }
    }
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        showLoading();
        mView.post(new Runnable() {
            public void run() {
                if (!mWallpaperConnection.connect()) {
                    mWallpaperConnection = null;
                }
            }
        });
    }
    private void showLoading() {
        LayoutInflater inflater = LayoutInflater.from(this);
        TextView content = (TextView) inflater.inflate(R.layout.live_wallpaper_loading, null);
        mDialog = new Dialog(this, android.R.style.Theme_Black);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA);
        mDialog.setContentView(content, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));
        mDialog.show();
    }
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDialog != null) mDialog.dismiss();
        if (mWallpaperConnection != null) {
            mWallpaperConnection.disconnect();
        }
        mWallpaperConnection = null;
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mWallpaperConnection != null && mWallpaperConnection.mEngine != null) {
            MotionEvent dup = MotionEvent.obtainNoHistory(ev);
            try {
                mWallpaperConnection.mEngine.dispatchPointer(dup);
            } catch (RemoteException e) {
            }
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            onUserInteraction();
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }
    class WallpaperConnection extends IWallpaperConnection.Stub implements ServiceConnection {
        final Intent mIntent;
        IWallpaperService mService;
        IWallpaperEngine mEngine;
        boolean mConnected;
        WallpaperConnection(Intent intent) {
            mIntent = intent;
        }
        public boolean connect() {
            synchronized (this) {
                if (!bindService(mIntent, this, Context.BIND_AUTO_CREATE)) {
                    return false;
                }
                mConnected = true;
                return true;
            }
        }
        public void disconnect() {
            synchronized (this) {
                mConnected = false;
                if (mEngine != null) {
                    try {
                        mEngine.destroy();
                    } catch (RemoteException e) {
                    }
                    mEngine = null;
                }
                unbindService(this);
                mService = null;
            }
        }
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (mWallpaperConnection == this) {
                mService = IWallpaperService.Stub.asInterface(service);
                try {
                    final View view = mView;
                    final View root = view.getRootView();
                    mService.attach(this, view.getWindowToken(),
                            WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA_OVERLAY,
                            true, root.getWidth(), root.getHeight());
                } catch (RemoteException e) {
                    Log.w(LOG_TAG, "Failed attaching wallpaper; clearing", e);
                }
            }
        }
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mEngine = null;
            if (mWallpaperConnection == this) {
                Log.w(LOG_TAG, "Wallpaper service gone: " + name);
            }
        }
        public void attachEngine(IWallpaperEngine engine) {
            synchronized (this) {
                if (mConnected) {
                    mEngine = engine;
                    try {
                        engine.setVisibility(true);
                    } catch (RemoteException e) {
                    }
                } else {
                    try {
                        engine.destroy();
                    } catch (RemoteException e) {
                    }
                }
            }
        }
        public ParcelFileDescriptor setWallpaper(String name) {
            return null;
        }
    }
}
