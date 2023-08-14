class Dots extends LinearLayout {
    private static final int MAX_DOTS = 8;
    private int mSelected = -1;
    public Dots(Context context) {
        this(context, null);
    }
    public Dots(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER);
        setPadding(0, 4, 0, 4);
        LayoutParams lp =
                new LayoutParams(LayoutParams.WRAP_CONTENT,
                                 LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < MAX_DOTS; i++) {
            ImageView dotView = new ImageView(mContext);
            dotView.setImageResource(R.drawable.page_indicator_unselected2);
            addView(dotView, lp);
        }
    }
    public void setDotCount(int dotCount) {
        if (dotCount > 1 && dotCount <= MAX_DOTS) {
            setVisibility(VISIBLE);
            for (int i = 0; i < MAX_DOTS; i++) {
                getChildAt(i).setVisibility(i < dotCount? VISIBLE : GONE);
            }
        } else {
            setVisibility(GONE);
        }
    }
    public void setSelected(int index) {
        if (index < 0 || index >= MAX_DOTS) return;
        if (mSelected >= 0) {
            ((ImageView)getChildAt(mSelected)).setImageResource(
                    R.drawable.page_indicator_unselected2);
        }
        ((ImageView)getChildAt(index)).setImageResource(R.drawable.page_indicator);
        mSelected = index;
    }
}
