public class GLDualActivity extends Activity {
    GLSurfaceView mGLView;
    GLDualGL2View mGL2View;
    @Override protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        View root = getLayoutInflater().inflate(R.layout.gldual_activity, null);
        mGLView = (GLSurfaceView) root.findViewById(R.id.gl1);
        mGLView.setEGLConfigChooser(5,6,5,0,0,0);
        mGLView.setRenderer(new TriangleRenderer());
        mGL2View = (GLDualGL2View) root.findViewById(R.id.gl2);
        setContentView(root);
    }
    @Override protected void onPause() {
        super.onPause();
        mGLView.onPause();
        mGL2View.onPause();
    }
    @Override protected void onResume() {
        super.onResume();
        mGLView.onResume();
        mGL2View.onResume();
    }
}
