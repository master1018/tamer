public class ListSimple extends ListScenario {
    @Override
    protected void init(Params params) {
        params.setStackFromBottom(false)
                .setStartingSelectionPosition(-1)
                .setNumItems(1000)
                .setItemScreenSizeFactor(0.14);
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getListView().setVerticalScrollBarEnabled(true);
        getListView().setFadingEdgeLength(12);
        getListView().setVerticalFadingEdgeEnabled(true);
    }
    @Override
    protected View createView(int position, ViewGroup parent, int desiredHeight) {
        View view = super.createView(position, parent, desiredHeight);
        view.setBackgroundColor(0xFF191919);
        ((TextView) view).setTextSize(16.0f);
        return view;
    }
}
