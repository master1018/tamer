public class MediaStubActivity extends Activity {
    public static final int WIDTH = 320;
    public static final int HEIGHT = 240;
    private SurfaceHolder mHolder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mediaplayer);
        SurfaceView surfaceV = (SurfaceView)findViewById(R.id.surface);
        ViewGroup.LayoutParams lp = surfaceV.getLayoutParams();
        lp.width = WIDTH;
        lp.height = HEIGHT;
        surfaceV.setLayoutParams(lp);
        mHolder = surfaceV.getHolder();
        mHolder.setFixedSize(WIDTH, HEIGHT);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    public SurfaceHolder getSurfaceHolder() {
        return mHolder;
    }
}
