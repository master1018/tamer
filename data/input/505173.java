public class TriangleActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLView = new GLSurfaceView(this);
        mGLView.setEGLConfigChooser(false);
        mGLView.setRenderer(new StaticTriangleRenderer(this));
        setContentView(mGLView);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }
    private GLSurfaceView mGLView;
}
