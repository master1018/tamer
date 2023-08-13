public class ListItemFocusablesClose extends ListScenario {
    public View getChildOfItem(int listIndex, int index) {
        return ((ViewGroup) getListView().getChildAt(listIndex)).getChildAt(index);
    }
    @Override
    protected void init(Params params) {
        params.setItemsFocusable(true)
                .setNumItems(2)
                .setItemScreenSizeFactor(0.55);
    }
    @Override
    protected View createView(int position, ViewGroup parent, int desiredHeight) {
        return ListItemFactory.twoButtonsSeparatedByFiller(
                position, parent.getContext(), desiredHeight);
    }
}
