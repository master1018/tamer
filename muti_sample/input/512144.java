public class VerticalFocusSearch extends Activity {
    private LinearLayout mLayout;
    private Button mTopWide;
    private Button mMidSkinny1Left;
    private Button mBottomWide;
    private Button mMidSkinny2Right;
    public LinearLayout getLayout() {
        return mLayout;
    }
    public Button getTopWide() {
        return mTopWide;
    }
    public Button getMidSkinny1Left() {
        return mMidSkinny1Left;
    }
    public Button getMidSkinny2Right() {
        return mMidSkinny2Right;
    }
    public Button getBottomWide() {
        return mBottomWide;
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mLayout = new LinearLayout(this);
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mLayout.setHorizontalGravity(Gravity.LEFT);
        mLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mTopWide = makeWide("top wide");
        mLayout.addView(mTopWide);
        mMidSkinny1Left = addSkinny(mLayout, "mid skinny 1(L)", false);
        mMidSkinny2Right = addSkinny(mLayout, "mid skinny 2(R)", true);
        mBottomWide = makeWide("bottom wide");
        mLayout.addView(mBottomWide);
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
    private Button makeWide(String label) {
        Button button = new MyButton(this);
        button.setText(label);
        button.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return button;
    }
    private Button addSkinny(LinearLayout root, String label, boolean atRight) {
        Button button = new MyButton(this);
        button.setText(label);
        button.setLayoutParams(new LinearLayout.LayoutParams(
                0, 
                ViewGroup.LayoutParams.WRAP_CONTENT,
                480));
        TextView filler = new TextView(this);
        filler.setText("filler");
        filler.setLayoutParams(new LinearLayout.LayoutParams(
                0, 
                ViewGroup.LayoutParams.WRAP_CONTENT,
                520));
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));
        if (atRight) {
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
