public class VideoSurface extends FrameLayout {
    public VideoSurface(Context context) {
        super(context);
        LayoutParams fp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        this.setLayoutParams(fp);
        GLSurfaceView gl = new GLSurfaceView(context);
        LayoutParams gp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        gl.setLayoutParams(gp);
        this.addView(gl);
        gl.setRenderer(new CubeRenderer(false));
        gl.setWindowType(WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA_OVERLAY);
        this.setWillNotDraw(false);
    }
}
