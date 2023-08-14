public class FilmView extends RSSurfaceView {
    public FilmView(Context context) {
        super(context);
    }
    private RenderScriptGL mRS;
    private FilmRS mRender;
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
        if (mRS == null) {
            mRS = createRenderScript(true);
            mRS.contextSetSurface(w, h, holder.getSurface());
            mRender = new FilmRS();
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
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        boolean ret = true;
        int act = ev.getAction();
        if (act == ev.ACTION_UP) {
            ret = false;
        }
        mRender.setFilmStripPosition((int)ev.getX(), (int)ev.getY() / 5);
        return ret;
    }
}
