public class PreDrawListener extends Activity implements OnClickListener {
    private MyLinearLayout mFrame;
    static public class MyLinearLayout extends LinearLayout implements
            ViewTreeObserver.OnPreDrawListener {
        public boolean mCancelNextDraw;
        public MyLinearLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        public MyLinearLayout(Context context) {
            super(context);
        }
        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            getViewTreeObserver().addOnPreDrawListener(this);
        }
        public boolean onPreDraw() {
            if (mCancelNextDraw) {
                Button b = new Button(this.getContext());
                b.setText("Hello");
                addView(b, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
                mCancelNextDraw = false;
                return false;
            }
            return true;
        }
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.pre_draw_listener);
        mFrame = (MyLinearLayout) findViewById(R.id.frame);
        Button mGoButton = (Button) findViewById(R.id.go);
        mGoButton.setOnClickListener(this);
    }
    public void onClick(View v) {
        mFrame.mCancelNextDraw = true;
        mFrame.invalidate();
    }
}
