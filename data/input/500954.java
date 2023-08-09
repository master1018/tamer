class TestView extends GLSurfaceView {
    TestView(Context context) {
        super(context);
        init();
    }
    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        setRenderer(new Renderer());
    }
    private class Renderer implements GLSurfaceView.Renderer {
        private static final String TAG = "Renderer";
        public void onDrawFrame(GL10 gl) {
        }
        public void onSurfaceChanged(GL10 gl, int width, int height) {
        }
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        }
    }
}
