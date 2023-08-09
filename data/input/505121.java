public abstract class ScrollViewScenario extends Activity {
    private LinearLayout mLinearLayout;
    private ScrollView mScrollView;
    private interface ViewFactory {
        View create(final Context context);
        float getHeightRatio();
    }
    private static abstract class ViewFactoryBase implements ViewFactory {
        private float mHeightRatio;
        @SuppressWarnings({"UnusedDeclaration"})
        private ViewFactoryBase() {throw new UnsupportedOperationException("don't call this!");}
        protected ViewFactoryBase(float heightRatio) {
            mHeightRatio = heightRatio;
        }
        public float getHeightRatio() {
            return mHeightRatio;
        }
    }
    @SuppressWarnings({"JavaDoc"})
    public static class Params {
        List<ViewFactory> mViewFactories = Lists.newArrayList();
        public Params addTextView(final String text, float heightRatio) {
            mViewFactories.add(new ViewFactoryBase(heightRatio) {
                public View create(final Context context) {
                    final TextView tv = new TextView(context);
                    tv.setText(text);
                    return tv;
                }
            });
            return this;
        }
        public Params addTextViews(int numViews, String textPrefix, float heightRatio) {
            for (int i = 0; i < numViews; i++) {
                addTextView(textPrefix + i, heightRatio);
            }
            return this;
        }
        public Params addButton(final String text, float heightRatio) {
            mViewFactories.add(new ViewFactoryBase(heightRatio) {
                public View create(final Context context) {
                    final Button button = new Button(context);
                    button.setText(text);
                    return button;
                }
            });
            return this;
        }
        public Params addButtons(int numButtons, String textPrefix, float heightRatio) {
            for (int i = 0; i < numButtons; i++) {
                addButton(textPrefix + i, heightRatio);
            }
            return this;
        }
        public Params addInternalSelectionView(final int numRows, float heightRatio) {
            mViewFactories.add(new ViewFactoryBase(heightRatio) {
                public View create(final Context context) {
                    return new InternalSelectionView(context, numRows, "isv");
                }
            });
            return this;
        }
        public Params addVerticalLLOfButtons(final String prefix, final int numButtons, float heightRatio) {
            mViewFactories.add(new ViewFactoryBase(heightRatio) {
                public View create(Context context) {
                    final LinearLayout ll = new LinearLayout(context);
                    ll.setOrientation(LinearLayout.VERTICAL);
                    final LinearLayout.LayoutParams lp =
                            new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
                    for (int i = 0; i < numButtons; i++) {
                        final Button button = new Button(context);
                        button.setText(prefix + i);
                        ll.addView(button, lp);
                    }
                    return ll;
                }
            });
            return this;
        }
    }
    protected abstract void init(Params params);
    public LinearLayout getLinearLayout() {
        return mLinearLayout;
    }
    public ScrollView getScrollView() {
        return mScrollView;
    }
    @SuppressWarnings({"unchecked"})
    public <T extends View> T getContentChildAt(int index) {
        return (T) mLinearLayout.getChildAt(index);
    }
    @SuppressWarnings({"JavaDoc"})
    protected ScrollView createScrollView() {
        return new ScrollView(this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight()
                - 25;
        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        final Params params = new Params();
        init(params);
        for (ViewFactory viewFactory : params.mViewFactories) {
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) (viewFactory.getHeightRatio() * screenHeight));
            mLinearLayout.addView(viewFactory.create(this), lp);
        }
        mScrollView = createScrollView();
        mScrollView.addView(mLinearLayout, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mScrollView.setSmoothScrollingEnabled(false);
        setContentView(mScrollView);
    }
}
