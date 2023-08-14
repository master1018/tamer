public class VisibilityCallback extends Activity {
    private static final boolean DEBUG = false;
    private MonitoredTextView mVictim;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.visibility_callback);
        mVictim = (MonitoredTextView)findViewById(R.id.victim);
        Button visibleButton = (Button) findViewById(R.id.vis);
        Button invisibleButton = (Button) findViewById(R.id.invis);
        Button goneButton = (Button) findViewById(R.id.gone);
        visibleButton.setOnClickListener(mVisibleListener);
        invisibleButton.setOnClickListener(mInvisibleListener);
        goneButton.setOnClickListener(mGoneListener);
    }
    View.OnClickListener mVisibleListener = new View.OnClickListener() {
        public void onClick(View v) {
            mVictim.setVisibility(View.VISIBLE);
        }
    };
    View.OnClickListener mInvisibleListener = new View.OnClickListener() {
        public void onClick(View v) {
            mVictim.setVisibility(View.INVISIBLE);
        }
    };
    View.OnClickListener mGoneListener = new View.OnClickListener() {
        public void onClick(View v) {
            mVictim.setVisibility(View.GONE);
        }
    };
    public static class MonitoredTextView extends TextView {
        private View mLastVisChangedView;
        private int mLastChangedVisibility;
        public MonitoredTextView(Context context) {
            super(context);
        }
        public MonitoredTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        public MonitoredTextView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }
        public View getLastVisChangedView() {
            return mLastVisChangedView;
        }
        public int getLastChangedVisibility() {
            return mLastChangedVisibility;
        }
        @Override
        protected void onVisibilityChanged(View changedView, int visibility) {
            mLastVisChangedView = changedView;
            mLastChangedVisibility = visibility;
            if (DEBUG) {
                Log.d("viewVis", "visibility: " + visibility);
            }
        }
    }
}
