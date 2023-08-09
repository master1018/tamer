public class ExpandableListWithHeaders extends ExpandableListScenario {
    private static final int[] sNumChildren = {1, 4, 3, 2, 6};
    private static final int sNumOfHeadersAndFooters = 12;
    @Override
    protected void init(ExpandableParams params) {
        params.setStackFromBottom(false)
                .setStartingSelectionPosition(-1)
                .setNumChildren(sNumChildren)
                .setItemScreenSizeFactor(0.14)
                .setConnectAdapter(false);
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final ExpandableListView expandableListView = getExpandableListView();
        expandableListView.setItemsCanFocus(true);
        for (int i = 0; i < sNumOfHeadersAndFooters; i++) {
            Button header = new Button(this);
            header.setText("Header View " + i);
            expandableListView.addHeaderView(header);
        }
        for (int i = 0; i < sNumOfHeadersAndFooters; i++) {
            Button footer = new Button(this);
            footer.setText("Footer View " + i);
            expandableListView.addFooterView(footer);
        }
        setAdapter(expandableListView);
    }
    public int getNumOfHeadersAndFooters() {
        return sNumOfHeadersAndFooters;
    }
}
