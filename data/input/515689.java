public class WallpaperManager {
    private static String TAG = "WallpaperManager";
    private static boolean DEBUG = false;
    private float mWallpaperXStep = -1;
    private float mWallpaperYStep = -1;
    public static final String ACTION_LIVE_WALLPAPER_CHOOSER
            = "android.service.wallpaper.LIVE_WALLPAPER_CHOOSER";
    public static final String COMMAND_TAP = "android.wallpaper.tap";
    public static final String COMMAND_DROP = "android.home.drop";
    private final Context mContext;
    static class FastBitmapDrawable extends Drawable {
        private final Bitmap mBitmap;
        private final int mWidth;
        private final int mHeight;
        private int mDrawLeft;
        private int mDrawTop;
        private FastBitmapDrawable(Bitmap bitmap) {
            mBitmap = bitmap;
            mWidth = bitmap.getWidth();
            mHeight = bitmap.getHeight();
            setBounds(0, 0, mWidth, mHeight);
        }
        @Override
        public void draw(Canvas canvas) {
            canvas.drawBitmap(mBitmap, mDrawLeft, mDrawTop, null);
        }
        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
        @Override
        public void setBounds(int left, int top, int right, int bottom) {
            mDrawLeft = left + (right-left - mWidth) / 2;
            mDrawTop = top + (bottom-top - mHeight) / 2;
        }
        @Override
        public void setBounds(Rect bounds) {
            super.setBounds(bounds);
        }
        @Override
        public void setAlpha(int alpha) {
            throw new UnsupportedOperationException(
                    "Not supported with this drawable");
        }
        @Override
        public void setColorFilter(ColorFilter cf) {
            throw new UnsupportedOperationException(
                    "Not supported with this drawable");
        }
        @Override
        public void setDither(boolean dither) {
            throw new UnsupportedOperationException(
                    "Not supported with this drawable");
        }
        @Override
        public void setFilterBitmap(boolean filter) {
            throw new UnsupportedOperationException(
                    "Not supported with this drawable");
        }
        @Override
        public int getIntrinsicWidth() {
            return mWidth;
        }
        @Override
        public int getIntrinsicHeight() {
            return mHeight;
        }
        @Override
        public int getMinimumWidth() {
            return mWidth;
        }
        @Override
        public int getMinimumHeight() {
            return mHeight;
        }
    }
    static class Globals extends IWallpaperManagerCallback.Stub {
        private IWallpaperManager mService;
        private Bitmap mWallpaper;
        private Bitmap mDefaultWallpaper;
        private static final int MSG_CLEAR_WALLPAPER = 1;
        private final Handler mHandler;
        Globals(Looper looper) {
            IBinder b = ServiceManager.getService(Context.WALLPAPER_SERVICE);
            mService = IWallpaperManager.Stub.asInterface(b);
            mHandler = new Handler(looper) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MSG_CLEAR_WALLPAPER:
                            synchronized (this) {
                                mWallpaper = null;
                                mDefaultWallpaper = null;
                            }
                            break;
                    }
                }
            };
        }
        public void onWallpaperChanged() {
            mHandler.sendEmptyMessage(MSG_CLEAR_WALLPAPER);
        }
        public Bitmap peekWallpaperBitmap(Context context, boolean returnDefault) {
            synchronized (this) {
                if (mWallpaper != null) {
                    return mWallpaper;
                }
                if (mDefaultWallpaper != null) {
                    return mDefaultWallpaper;
                }
                mWallpaper = null;
                try {
                    mWallpaper = getCurrentWallpaperLocked(context);
                } catch (OutOfMemoryError e) {
                    Log.w(TAG, "No memory load current wallpaper", e);
                }
                if (mWallpaper == null && returnDefault) {
                    mDefaultWallpaper = getDefaultWallpaperLocked(context);
                    return mDefaultWallpaper;
                }
                return mWallpaper;
            }
        }
        private Bitmap getCurrentWallpaperLocked(Context context) {
            try {
                Bundle params = new Bundle();
                ParcelFileDescriptor fd = mService.getWallpaper(this, params);
                if (fd != null) {
                    int width = params.getInt("width", 0);
                    int height = params.getInt("height", 0);
                    if (width <= 0 || height <= 0) {
                        Bitmap bm = BitmapFactory.decodeFileDescriptor(
                                fd.getFileDescriptor(), null, null);
                        try {
                            fd.close();
                        } catch (IOException e) {
                        }
                        if (bm != null) {
                            bm.setDensity(DisplayMetrics.DENSITY_DEVICE);
                        }
                        return bm;
                    }
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inDither = false;
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bm = BitmapFactory.decodeFileDescriptor(
                            fd.getFileDescriptor(), null, options);
                    try {
                        fd.close();
                    } catch (IOException e) {
                    }
                    return generateBitmap(context, bm, width, height);
                }
            } catch (RemoteException e) {
            }
            return null;
        }
        private Bitmap getDefaultWallpaperLocked(Context context) {
            try {
                InputStream is = context.getResources().openRawResource(
                        com.android.internal.R.drawable.default_wallpaper);
                if (is != null) {
                    int width = mService.getWidthHint();
                    int height = mService.getHeightHint();
                    if (width <= 0 || height <= 0) {
                        Bitmap bm = BitmapFactory.decodeStream(is, null, null);
                        try {
                            is.close();
                        } catch (IOException e) {
                        }
                        if (bm != null) {
                            bm.setDensity(DisplayMetrics.DENSITY_DEVICE);
                        }
                        return bm;
                    }
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inDither = false;
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bm = BitmapFactory.decodeStream(is, null, options);
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                    try {
                        return generateBitmap(context, bm, width, height);
                    } catch (OutOfMemoryError e) {
                        Log.w(TAG, "Can't generate default bitmap", e);
                        return bm;
                    }
                }
            } catch (RemoteException e) {
            }
            return null;
        }
    }
    private static Object mSync = new Object();
    private static Globals sGlobals;
    static void initGlobals(Looper looper) {
        synchronized (mSync) {
            if (sGlobals == null) {
                sGlobals = new Globals(looper);
            }
        }
    }
     WallpaperManager(Context context, Handler handler) {
        mContext = context;
        initGlobals(context.getMainLooper());
    }
    public static WallpaperManager getInstance(Context context) {
        return (WallpaperManager)context.getSystemService(
                Context.WALLPAPER_SERVICE);
    }
    public IWallpaperManager getIWallpaperManager() {
        return sGlobals.mService;
    }
    public Drawable getDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(mContext, true);
        if (bm != null) {
            Drawable dr = new BitmapDrawable(mContext.getResources(), bm);
            dr.setDither(false);
            return dr;
        }
        return null;
    }
    public Drawable peekDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(mContext, false);
        if (bm != null) {
            Drawable dr = new BitmapDrawable(mContext.getResources(), bm);
            dr.setDither(false);
            return dr;
        }
        return null;
    }
    public Drawable getFastDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(mContext, true);
        if (bm != null) {
            Drawable dr = new FastBitmapDrawable(bm);
            return dr;
        }
        return null;
    }
    public Drawable peekFastDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(mContext, false);
        if (bm != null) {
            Drawable dr = new FastBitmapDrawable(bm);
            return dr;
        }
        return null;
    }
    public WallpaperInfo getWallpaperInfo() {
        try {
            return sGlobals.mService.getWallpaperInfo();
        } catch (RemoteException e) {
            return null;
        }
    }
    public void setResource(int resid) throws IOException {
        try {
            Resources resources = mContext.getResources();
            ParcelFileDescriptor fd = sGlobals.mService.setWallpaper(
                    "res:" + resources.getResourceName(resid));
            if (fd != null) {
                FileOutputStream fos = null;
                try {
                    fos = new ParcelFileDescriptor.AutoCloseOutputStream(fd);
                    setWallpaper(resources.openRawResource(resid), fos);
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                }
            }
        } catch (RemoteException e) {
        }
    }
    public void setBitmap(Bitmap bitmap) throws IOException {
        try {
            ParcelFileDescriptor fd = sGlobals.mService.setWallpaper(null);
            if (fd == null) {
                return;
            }
            FileOutputStream fos = null;
            try {
                fos = new ParcelFileDescriptor.AutoCloseOutputStream(fd);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        } catch (RemoteException e) {
        }
    }
    public void setStream(InputStream data) throws IOException {
        try {
            ParcelFileDescriptor fd = sGlobals.mService.setWallpaper(null);
            if (fd == null) {
                return;
            }
            FileOutputStream fos = null;
            try {
                fos = new ParcelFileDescriptor.AutoCloseOutputStream(fd);
                setWallpaper(data, fos);
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        } catch (RemoteException e) {
        }
    }
    private void setWallpaper(InputStream data, FileOutputStream fos)
            throws IOException {
        byte[] buffer = new byte[32768];
        int amt;
        while ((amt=data.read(buffer)) > 0) {
            fos.write(buffer, 0, amt);
        }
    }
    public int getDesiredMinimumWidth() {
        try {
            return sGlobals.mService.getWidthHint();
        } catch (RemoteException e) {
            return 0;
        }
    }
    public int getDesiredMinimumHeight() {
        try {
            return sGlobals.mService.getHeightHint();
        } catch (RemoteException e) {
            return 0;
        }
    }
    public void suggestDesiredDimensions(int minimumWidth, int minimumHeight) {
        try {
            sGlobals.mService.setDimensionHints(minimumWidth, minimumHeight);
        } catch (RemoteException e) {
        }
    }
    public void setWallpaperOffsets(IBinder windowToken, float xOffset, float yOffset) {
        try {
            ViewRoot.getWindowSession(mContext.getMainLooper()).setWallpaperPosition(
                    windowToken, xOffset, yOffset, mWallpaperXStep, mWallpaperYStep);
        } catch (RemoteException e) {
        }
    }
    public void setWallpaperOffsetSteps(float xStep, float yStep) {
        mWallpaperXStep = xStep;
        mWallpaperYStep = yStep;
    }
    public void sendWallpaperCommand(IBinder windowToken, String action,
            int x, int y, int z, Bundle extras) {
        try {
            ViewRoot.getWindowSession(mContext.getMainLooper()).sendWallpaperCommand(
                    windowToken, action, x, y, z, extras, false);
        } catch (RemoteException e) {
        }
    }
    public void clearWallpaperOffsets(IBinder windowToken) {
        try {
            ViewRoot.getWindowSession(mContext.getMainLooper()).setWallpaperPosition(
                    windowToken, -1, -1, -1, -1);
        } catch (RemoteException e) {
        }
    }
    public void clear() throws IOException {
        setResource(com.android.internal.R.drawable.default_wallpaper);
    }
    static Bitmap generateBitmap(Context context, Bitmap bm, int width, int height) {
        if (bm == null) {
            return bm;
        }
        bm.setDensity(DisplayMetrics.DENSITY_DEVICE);
        Bitmap newbm = Bitmap.createBitmap(width, height,
                Bitmap.Config.RGB_565);
        newbm.setDensity(DisplayMetrics.DENSITY_DEVICE);
        Canvas c = new Canvas(newbm);
        c.setDensity(DisplayMetrics.DENSITY_DEVICE);
        Rect targetRect = new Rect();
        targetRect.left = targetRect.top = 0;
        targetRect.right = bm.getWidth();
        targetRect.bottom = bm.getHeight();
        int deltaw = width - targetRect.right;
        int deltah = height - targetRect.bottom;
        if (deltaw > 0 || deltah > 0) {
            float scale = 1.0f;
            if (deltaw > deltah) {
                scale = width / (float)targetRect.right;
            } else {
                scale = height / (float)targetRect.bottom;
            }
            targetRect.right = (int)(targetRect.right*scale);
            targetRect.bottom = (int)(targetRect.bottom*scale);
            deltaw = width - targetRect.right;
            deltah = height - targetRect.bottom;
        }
        targetRect.offset(deltaw/2, deltah/2);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setDither(true);
        c.drawBitmap(bm, null, targetRect, paint);
        bm.recycle();
        return newbm;
    }
}
