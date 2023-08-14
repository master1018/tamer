public class FountainView extends RSSurfaceView {
    public FountainView(Context context) {
        super(context);
    }
    private RenderScriptGL mRS;
    private FountainRS mRender;
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
        if (mRS == null) {
            mRS = createRenderScript(false);
            mRS.contextSetSurface(w, h, holder.getSurface());
            mRender = new FountainRS();
            mRender.init(mRS, getResources(), w, h);
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        if(mRS != null) {
            mRS = null;
            destroyRenderScript();
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        int act = ev.getAction();
        if (act == ev.ACTION_UP) {
            mRender.newTouchPosition(0, 0, 0);
            return false;
        }
        float rate = (ev.getPressure() * 50.f);
        rate *= rate;
        if(rate > 2000.f) {
            rate = 2000.f;
        }
        mRender.newTouchPosition((int)ev.getX(), (int)ev.getY(), (int)rate);
        return true;
    }
}
