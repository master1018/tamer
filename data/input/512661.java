public class CameraStubActivity extends Activity {
    public static SurfaceView mSurfaceView;
    private final int LAYOUT_WIDTH = 480;
    private final int LAYOUT_HEIGHT = 320;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.surface_view);
        mSurfaceView = (SurfaceView)findViewById(R.id.surface_view);
        ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
        lp.width = LAYOUT_WIDTH;
        lp.height = LAYOUT_HEIGHT;
        mSurfaceView.setLayoutParams(lp);
        mSurfaceView.getHolder().setFixedSize(LAYOUT_WIDTH, LAYOUT_HEIGHT);
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
}
