public class RSSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mSurfaceHolder;
    private RenderScriptGL mRS;
    public RSSurfaceView(Context context) {
        super(context);
        init();
    }
    public RSSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(RenderScript.LOG_TAG, "surfaceCreated");
        mSurfaceHolder = holder;
    }
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(RenderScript.LOG_TAG, "surfaceDestroyed");
        if (mRS != null) {
            mRS.contextSetSurface(0, 0, null);
        }
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.v(RenderScript.LOG_TAG, "surfaceChanged");
        if (mRS != null) {
            mRS.contextSetSurface(w, h, holder.getSurface());
        }
    }
    public void onPause() {
        if(mRS != null) {
            mRS.pause();
        }
    }
    public void onResume() {
        if(mRS != null) {
            mRS.resume();
        }
    }
    public void queueEvent(Runnable r) {
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
    public RenderScriptGL createRenderScript(boolean useDepth, boolean forceSW) {
        Log.v(RenderScript.LOG_TAG, "createRenderScript");
        mRS = new RenderScriptGL(useDepth, forceSW);
        return mRS;
    }
    public RenderScriptGL createRenderScript(boolean useDepth) {
        return createRenderScript(useDepth, false);
    }
    public void destroyRenderScript() {
        Log.v(RenderScript.LOG_TAG, "destroyRenderScript");
        mRS.destroy();
        mRS = null;
    }
    public void createRenderScript(RenderScriptGL rs) {
        mRS = rs;
    }
}
