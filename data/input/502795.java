public class ListItemFocusableAboveUnfocusable extends ListScenario {
    protected void init(Params params) {
        params.setNumItems(2)
                .setItemsFocusable(true)
                .setItemScreenSizeFactor(0.2)
                .setMustFillScreen(false);
    }
    @Override
    protected View createView(int position, ViewGroup parent, int desiredHeight) {
        if (position == 0) {
            return ListItemFactory.button(
                    position, parent.getContext(), getValueAtPosition(position), desiredHeight);
        } else {
            return super.createView(position, parent, desiredHeight);
        }
    }
}
