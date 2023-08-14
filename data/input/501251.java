public class DrawableBgMinSize extends Activity implements OnClickListener {
    private boolean mUsingBigBg = false;
    private Drawable mBackgroundDrawable;
    private Drawable mBigBackgroundDrawable;
    private Button mChangeBackgroundsButton;
    private TextView mTextView;
    private LinearLayout mLinearLayout;
    private RelativeLayout mRelativeLayout;
    private FrameLayout mFrameLayout;
    private AbsoluteLayout mAbsoluteLayout;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.drawable_background_minimum_size);
        mBackgroundDrawable = getResources().getDrawable(R.drawable.drawable_background);
        mBigBackgroundDrawable = getResources().getDrawable(R.drawable.big_drawable_background);
        mChangeBackgroundsButton = (Button) findViewById(R.id.change_backgrounds);
        mChangeBackgroundsButton.setOnClickListener(this);
        mTextView = (TextView) findViewById(R.id.text_view);
        mLinearLayout = (LinearLayout) findViewById(R.id.linear_layout);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);
        mFrameLayout = (FrameLayout) findViewById(R.id.frame_layout);
        mAbsoluteLayout = (AbsoluteLayout) findViewById(R.id.absolute_layout);
        changeBackgrounds(mBackgroundDrawable);
    }
    private void changeBackgrounds(Drawable newBg) {
        mTextView.setBackgroundDrawable(newBg);
        mLinearLayout.setBackgroundDrawable(newBg);
        mRelativeLayout.setBackgroundDrawable(newBg);
        mFrameLayout.setBackgroundDrawable(newBg);
        mAbsoluteLayout.setBackgroundDrawable(newBg);
    }
    public void onClick(View v) {
        if (mUsingBigBg) {
            changeBackgrounds(mBackgroundDrawable);
        } else {
            changeBackgrounds(mBigBackgroundDrawable);
        }
        mUsingBigBg = !mUsingBigBg;
    }
}
