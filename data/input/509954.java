public class ListWithOnItemSelectedAction extends ListScenario {
    protected void init(Params params) {
        params.setNumItems(8)
                .setItemScreenSizeFactor(0.2)
                .includeHeaderAboveList(true);
    }
    @Override
    protected void positionSelected(int positon) {
        if (positon != getListView().getSelectedItemPosition()) {
            throw new IllegalStateException("something is fishy... the selected postion does not " +
                    "match what the list reports.");
        }
        setHeaderValue(
                ((TextView) getListView().getSelectedView()).getText().toString());
    }
}
