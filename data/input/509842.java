public class InternalSelectionFocus extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        final InternalSelectionView leftColumn = new InternalSelectionView(this, 5, "left column");
        leftColumn.setLayoutParams(params);
        leftColumn.setPadding(10, 10, 10, 10);
        layout.addView(leftColumn);
        final InternalSelectionView middleColumn = new InternalSelectionView(this, 5, "middle column");
        middleColumn.setLayoutParams(params);
        middleColumn.setPadding(10, 10, 10, 10);
        layout.addView(middleColumn);
        final InternalSelectionView rightColumn = new InternalSelectionView(this, 5, "right column");
        rightColumn.setLayoutParams(params);
        rightColumn.setPadding(10, 10, 10, 10);
        layout.addView(rightColumn);
        setContentView(layout);
    }
}
