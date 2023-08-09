class FallView extends RSSurfaceView {
    private FallRS mRender;
    public FallView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
        RenderScriptGL RS = createRenderScript(false);
        mRender = new FallRS(w, h);
        mRender.init(RS, getResources(), false);
        mRender.start();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mRender.addDrop(event.getX(), event.getY());
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                }
                break;
        }
        return true;
    }
}