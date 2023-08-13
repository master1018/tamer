public class ListWithHeaders extends ListScenario {
    @Override
    protected void init(Params params) {
        params.setStackFromBottom(false)
                .setStartingSelectionPosition(-1)
                .setNumItems(24)
                .setItemScreenSizeFactor(0.14);
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final ListView listView = getListView();
        listView.setItemsCanFocus(true);
        for (int i = 0; i < 12; i++) {
            Button header = new Button(this);
            header.setText("Header View");
            listView.addHeaderView(header);
        }
        for (int i = 0; i < 12; i++) {
            Button footer = new Button(this);
            footer.setText("Footer View");
            listView.addFooterView(footer);
        }
        final ListAdapter adapter = listView.getAdapter();
        listView.setAdapter(adapter);
    }
}
