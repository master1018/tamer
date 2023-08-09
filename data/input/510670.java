public abstract class RenderScriptScene {
    protected int mWidth;
    protected int mHeight;
    protected boolean mPreview;
    protected Resources mResources;
    protected RenderScriptGL mRS;
    protected ScriptC mScript;
    public RenderScriptScene(int width, int height) {
        mWidth = width;
        mHeight = height;
    }
    public void init(RenderScriptGL rs, Resources res, boolean isPreview) {
        mRS = rs;
        mResources = res;
        mPreview = isPreview;
        mScript = createScript();
    }
    public boolean isPreview() {
        return mPreview;
    }
    public int getWidth() {
        return mWidth;
    }
    public int getHeight() {
        return mHeight;
    }
    public Resources getResources() {
        return mResources;
    }
    public RenderScriptGL getRS() {
        return mRS;
    }
    public ScriptC getScript() {
        return mScript;
    }
    protected abstract ScriptC createScript();
    public void stop() {
        mRS.contextBindRootScript(null);
    }
    public void start() {
        mRS.contextBindRootScript(mScript);
    }
    public void resize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }
    @SuppressWarnings({"UnusedDeclaration"})
    public void setOffset(float xOffset, float yOffset,
            float xStep, float yStep, int xPixels, int yPixels) {
    }
    public void onTouchEvent(MotionEvent event) {
    }
}
