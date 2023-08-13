public class ListButtonsDiagonalAcrossItems extends ListScenario {
    @Override
    protected void init(Params params) {
        params.setItemsFocusable(true)
                .setNumItems(3)
                .setItemScreenSizeFactor(0.2)
                .setMustFillScreen(false);
    }
    public Button getLeftButton() {
        return (Button) ((ViewGroup) getListView().getChildAt(0)).getChildAt(0);
    }
    public Button getCenterButton() {
        return (Button) ((ViewGroup) getListView().getChildAt(1)).getChildAt(1);
    }
    public Button getRightButton() {
        return (Button) ((ViewGroup) getListView().getChildAt(2)).getChildAt(2);
    }
    @Override
    protected View createView(int position, ViewGroup parent,
            int desiredHeight) {
        final Slot slot = position == 0 ? Slot.Left :
                (position == 1 ? Slot.Middle : Slot.Right);
        return ListItemFactory.horizontalButtonSlots(
                parent.getContext(), desiredHeight, slot);
    }
}
