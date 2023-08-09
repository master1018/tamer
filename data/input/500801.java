public class HorizontalFocusSearch extends Activity {
    private LinearLayout mLayout;
    private Button mLeftTall;
    private Button mMidShort1Top;
    private Button mMidShort2Bottom;
    private Button mRightTall;
    public LinearLayout getLayout() {
        return mLayout;
    }
    public Button getLeftTall() {
        return mLeftTall;
    }
    public Button getMidShort1Top() {
        return mMidShort1Top;
    }
    public Button getMidShort2Bottom() {
        return mMidShort2Bottom;
    }
    public Button getRightTall() {
        return mRightTall;
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mLayout = new LinearLayout(this);
        mLayout.setOrientation(LinearLayout.HORIZONTAL);
        mLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mLeftTall = makeTall("left tall");
        mLayout.addView(mLeftTall);
        mMidShort1Top = addShort(mLayout, "mid(1) top", false);
        mMidShort2Bottom = addShort(mLayout, "mid(2) bottom", true);
        mRightTall = makeTall("right tall");
        mLayout.addView(mRightTall);
        setContentView(mLayout);
    }
    private static class MyButton extends Button {
        public MyButton(Context context) {
            super(context);
        }
        @Override
        public String toString() {
            return getText().toString();
        }
    }
    private Button makeTall(String label) {
        Button button = new MyButton(this);
        button.setText(label);
        button.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return button;
    }
    private Button addShort(LinearLayout root, String label, boolean atBottom) {
        Button button = new MyButton(this);
        button.setText(label);
        button.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0, 
                490));
        TextView filler = new TextView(this);
        filler.setText("filler");
        filler.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0, 
                510));
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
        if (atBottom) {
            ll.addView(filler);
            ll.addView(button);
            root.addView(ll);
        } else {
            ll.addView(button);
            ll.addView(filler);
            root.addView(ll);
        }
        return button;
    }
}
