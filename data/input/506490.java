public class ListHorizontalFocusWithinItemWins extends ListScenario {
    @Override
    protected void init(Params params) {
        params.setItemsFocusable(true)
                .setNumItems(2)
                .setItemScreenSizeFactor(0.2)
                .setMustFillScreen(false);
    }
    public Button getTopLeftButton() {
        return (Button) ((ViewGroup) getListView().getChildAt(0)).getChildAt(0);
    }
    public Button getTopRightButton() {
        return (Button) ((ViewGroup) getListView().getChildAt(0)).getChildAt(2);
    }
    public Button getBottomMiddleButton() {
        return (Button) ((ViewGroup) getListView().getChildAt(1)).getChildAt(1);
    }
    @Override
    protected View createView(int position, ViewGroup parent,
            int desiredHeight) {
        final Context context = parent.getContext();
        if (position == 0) {
            return ListItemFactory.horizontalButtonSlots(
                    context, desiredHeight, Slot.Left, Slot.Right);
        } else if (position == 1) {
            return ListItemFactory.horizontalButtonSlots(
                    context, desiredHeight, Slot.Middle);
        } else {
            throw new IllegalArgumentException("expecting position 0 or 1");
        }
    }
}
