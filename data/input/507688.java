public class App {
	static private final HashMap<Context, App> mMap = new HashMap<Context, App>();
	static public App get(Context context) {
		return mMap.get(context);
	}
    public static final TimeZone CURRENT_TIME_ZONE = TimeZone.getDefault();
    public static float PIXEL_DENSITY = 0.0f;
	private final Context mContext;
    private final HandlerThread mHandlerThread = new HandlerThread("AppHandlerThread");
    private final Handler mHandler;	
    private ReverseGeocoder mReverseGeocoder = null;
    private boolean mPaused = false;
	public App(Context context) {
		mMap.put(context, this);
		mContext = context;
		if(PIXEL_DENSITY == 0.0f) {
			DisplayMetrics metrics = new DisplayMetrics();
			((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
			PIXEL_DENSITY = metrics.density;
		}
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());		
	    mReverseGeocoder = new ReverseGeocoder(mContext);					
	}
	public void shutdown() {
        mReverseGeocoder.shutdown();
        mMap.put(mContext, null);
	}
    public Context getContext() {
        return mContext;
    }	
    public Handler getHandler() {
        while (mHandler == null) {
            ;
        }
        return mHandler;
    }
    public ReverseGeocoder getReverseGeocoder() {
        return mReverseGeocoder;
    }    
    public boolean isPaused() {
    	return mPaused;
    }
    public void onResume() {
    	mPaused = false;
    }
    public void onPause() {
    	mReverseGeocoder.flushCache();
    	mPaused = true;
    }
    public void showToast(final String string, final int duration) {
        mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(mContext, string, duration).show();
            }
        });
    }
}
