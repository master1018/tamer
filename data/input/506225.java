public class ListInterleaveFocusables extends ListScenario {
    private Set<Integer> mFocusablePositions = Sets.newHashSet(1, 3, 6);
    @Override
    protected void init(Params params) {
        params.setNumItems(7)
                .setItemScreenSizeFactor(1.0 / 8)
                .setItemsFocusable(true)
                .setMustFillScreen(false);
    }
    @Override
    protected View createView(int position, ViewGroup parent, int desiredHeight) {
        if (mFocusablePositions.contains(position)) {
            return ListItemFactory.button(
                    position, parent.getContext(), getValueAtPosition(position), desiredHeight);
        } else {
            return super.createView(position, parent, desiredHeight);
        }
    }
    @Override
    public int getItemViewType(int position) {
        return mFocusablePositions.contains(position) ? 0 : 1;
    }
    @Override
    public int getViewTypeCount() {
        return 2;
    }
}
