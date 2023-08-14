public class ScrollViewButtonsAndLabels extends Activity {
    private ScrollView mScrollView;
    private LinearLayout mLinearLayout;
    private int mNumGroups = 10;
    public ScrollView getScrollView() {
        return mScrollView;
    }
    public LinearLayout getLinearLayout() {
        return mLinearLayout;
    }
    public int getNumButtons() {
        return mNumGroups;
    }
    public Button getButton(int groupNum) {
        if (groupNum > mNumGroups) {
            throw new IllegalArgumentException("groupNum > " + mNumGroups);
        }
        return (Button) mLinearLayout.getChildAt(2*groupNum);
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.scrollview_linear_layout);
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        mNumGroups = screenHeight / 30;
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mLinearLayout = (LinearLayout) findViewById(R.id.layout);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        for (int i = 0; i < mNumGroups; i++) {
            if (i > 0) {
                TextView textView = new TextView(this);
                textView.setText("Text View " + i);
                mLinearLayout.addView(textView, p);
            }
            Button button = new Button(this);
            button.setText("Button " + (i + 1));
            mLinearLayout.addView(button, p);
        }
    }
}
