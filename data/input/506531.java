public class AdjacentVerticalRectLists extends Activity {
    private LinearLayout mLayout;
    private InternalSelectionView mLeftColumn;
    private InternalSelectionView mMiddleColumn;
    private InternalSelectionView mRightColumn;
    public LinearLayout getLayout() {
        return mLayout;
    }
    public InternalSelectionView getLeftColumn() {
        return mLeftColumn;
    }
    public InternalSelectionView getMiddleColumn() {
        return mMiddleColumn;
    }
    public InternalSelectionView getRightColumn() {
        return mRightColumn;
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mLayout = new LinearLayout(this);
        mLayout.setOrientation(LinearLayout.HORIZONTAL);
        mLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        mLeftColumn = new InternalSelectionView(this, 5, "left column");
        mLeftColumn.setLayoutParams(params);
        mLeftColumn.setPadding(10, 10, 10, 10);
        mLayout.addView(mLeftColumn);
        mMiddleColumn = new InternalSelectionView(this, 5, "middle column");
        mMiddleColumn.setLayoutParams(params);
        mMiddleColumn.setPadding(10, 10, 10, 10);
        mLayout.addView(mMiddleColumn);
        mRightColumn = new InternalSelectionView(this, 5, "right column");
        mRightColumn.setLayoutParams(params);
        mRightColumn.setPadding(10, 10, 10, 10);
        mLayout.addView(mRightColumn);
        setContentView(mLayout);
    }
}
