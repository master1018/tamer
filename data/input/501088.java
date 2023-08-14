public class ImageWallpaper extends WallpaperService {
    WallpaperManager mWallpaperManager;
    private HandlerThread mThread;
    @Override
    public void onCreate() {
        super.onCreate();
        mWallpaperManager = (WallpaperManager) getSystemService(WALLPAPER_SERVICE);
        Looper looper = WindowManagerPolicyThread.getLooper();
        if (looper != null) {
            setCallbackLooper(looper);
        } else {
            mThread = new HandlerThread("Wallpaper", Process.THREAD_PRIORITY_FOREGROUND);
            mThread.start();
            setCallbackLooper(mThread.getLooper());
        }
    }
    public Engine onCreateEngine() {
        return new DrawableEngine();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mThread != null) {
            mThread.quit();
        }
    }
    class DrawableEngine extends Engine {
        private final Object mLock = new Object();
        private WallpaperObserver mReceiver;
        Drawable mBackground;
        float mXOffset;
        float mYOffset;
        class WallpaperObserver extends BroadcastReceiver {
            public void onReceive(Context context, Intent intent) {
                updateWallpaper();
                drawFrame();
                System.gc();
            }
        }
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            IntentFilter filter = new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED);
            mReceiver = new WallpaperObserver();
            registerReceiver(mReceiver, filter);
            updateWallpaper();
            surfaceHolder.setSizeFromLayout();
        }
        @Override
        public void onDestroy() {
            super.onDestroy();
            unregisterReceiver(mReceiver);
        }
        @Override
        public void onVisibilityChanged(boolean visible) {
            drawFrame();
        }
        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
        }
        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xOffsetStep, float yOffsetStep,
                int xPixels, int yPixels) {
            mXOffset = xOffset;
            mYOffset = yOffset;
            drawFrame();
        }
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            drawFrame();
        }
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
        }
        void drawFrame() {
            SurfaceHolder sh = getSurfaceHolder();
            Canvas c = sh.lockCanvas();
            if (c != null) {
                final Rect frame = sh.getSurfaceFrame();
                synchronized (mLock) {
                    final Drawable background = mBackground;
                    final int dw = frame.width();
                    final int dh = frame.height();
                    final int bw = background != null ? background.getIntrinsicWidth() : 0;
                    final int bh = background != null ? background.getIntrinsicHeight() : 0;
                    final int availw = dw-bw;
                    final int availh = dh-bh;
                    int xPixels = availw < 0 ? (int)(availw*mXOffset+.5f) : (availw/2);
                    int yPixels = availh < 0 ? (int)(availh*mYOffset+.5f) : (availh/2);
                    c.translate(xPixels, yPixels);
                    if (availw<0 || availh<0) {
                        c.save(Canvas.CLIP_SAVE_FLAG);
                        c.clipRect(0, 0, bw, bh, Op.DIFFERENCE);
                        c.drawColor(0xff000000);
                        c.restore();
                    }
                    if (background != null) {
                        background.draw(c);
                    }
                }
                sh.unlockCanvasAndPost(c);
            }
        }
        void updateWallpaper() {
            synchronized (mLock) {
                try {
                    mBackground = mWallpaperManager.getFastDrawable();
                } catch (RuntimeException e) {
                    Log.w("ImageWallpaper", "Unable to load wallpaper!", e);
                }
            }
        }
    }
}
