class GLJNIView extends GLSurfaceView {
    GLJNIView(Context context) {
        super(context);
        init();
    }
    public GLJNIView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        setRenderer(new Renderer());
    }
    private class Renderer implements GLSurfaceView.Renderer {
        private static final String TAG = "Renderer";
        public void onDrawFrame(GL10 gl) {
            GLJNILib.step();
        }
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLJNILib.init(width, height);
        }
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        GLJNILib.changeBackground();
        return true;
    }
}
