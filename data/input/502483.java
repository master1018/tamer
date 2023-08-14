class GalaxyView extends RSSurfaceView {
    public GalaxyView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
        RenderScriptGL RS = createRenderScript(false);
        GalaxyRS render = new GalaxyRS(w, h);
        render.init(RS, getResources(), false);
        render.setOffset(0.5f, 0.0f, 0, 0);
        render.start();
    }
}
