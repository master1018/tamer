public class SurfaceViewOverlay extends Activity {
    View mVictimContainer;
    View mVictim1;
    View mVictim2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.surface_view_overlay);
        GLSurfaceView glSurfaceView =
            (GLSurfaceView) findViewById(R.id.glsurfaceview);
        glSurfaceView.setRenderer(new CubeRenderer(false));
        mVictimContainer = findViewById(R.id.hidecontainer);
        mVictim1 = findViewById(R.id.hideme1);
        mVictim1.setOnClickListener(new HideMeListener(mVictim1));
        mVictim2 = findViewById(R.id.hideme2);
        mVictim2.setOnClickListener(new HideMeListener(mVictim2));
        Button visibleButton = (Button) findViewById(R.id.vis);
        Button invisibleButton = (Button) findViewById(R.id.invis);
        Button goneButton = (Button) findViewById(R.id.gone);
        visibleButton.setOnClickListener(mVisibleListener);
        invisibleButton.setOnClickListener(mInvisibleListener);
        goneButton.setOnClickListener(mGoneListener);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    class HideMeListener implements OnClickListener {
        final View mTarget;
        HideMeListener(View target) {
            mTarget = target;
        }
        public void onClick(View v) {
            mTarget.setVisibility(View.INVISIBLE);
        }
    }
    OnClickListener mVisibleListener = new OnClickListener() {
        public void onClick(View v) {
            mVictim1.setVisibility(View.VISIBLE);
            mVictim2.setVisibility(View.VISIBLE);
            mVictimContainer.setVisibility(View.VISIBLE);
        }
    };
    OnClickListener mInvisibleListener = new OnClickListener() {
        public void onClick(View v) {
            mVictim1.setVisibility(View.INVISIBLE);
            mVictim2.setVisibility(View.INVISIBLE);
            mVictimContainer.setVisibility(View.INVISIBLE);
        }
    };
    OnClickListener mGoneListener = new OnClickListener() {
        public void onClick(View v) {
            mVictim1.setVisibility(View.GONE);
            mVictim2.setVisibility(View.GONE);
            mVictimContainer.setVisibility(View.GONE);
        }
    };
}
