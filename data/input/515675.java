public class ListItemFocusablesFarApart extends ListScenario  {
    @Override
    protected void init(Params params) {
        params.setItemsFocusable(true)
                .setNumItems(2)
                .setItemScreenSizeFactor(2);
    }
    @Override
    protected View createView(int position, ViewGroup parent, int desiredHeight) {
        return ListItemFactory.twoButtonsSeparatedByFiller(
                position, parent.getContext(), desiredHeight);
    }
}
