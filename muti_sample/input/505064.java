public class GridSimple extends GridScenario {
    @Override
    protected void init(Params params) {
        params.setStackFromBottom(false)
                .setStartingSelectionPosition(-1)
                .setNumItems(1000)
                .setNumColumns(3)
                .setItemScreenSizeFactor(0.14);
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getGridView().setSelector(new PaintDrawable(0xFFFF0000));
        getGridView().setPadding(0, 0, 0, 0);
        getGridView().setFadingEdgeLength(64);
        getGridView().setVerticalFadingEdgeEnabled(true);
        getGridView().setBackgroundColor(0xFFC0C0C0);
    }
    @Override
    protected View createView(int position, ViewGroup parent, int desiredHeight) {
        View view = super.createView(position, parent, desiredHeight);
        view.setBackgroundColor(0xFF000000);
        ((TextView) view).setTextSize(16.0f);
        return view;
    }
}
