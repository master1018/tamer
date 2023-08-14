public class ListItemISVAndButton extends ListScenario {
    @Override
    protected void init(Params params) {
        params.setItemScreenSizeFactor(2.0)
                .setNumItems(3)
                .setItemsFocusable(true);
    }
    @Override
    protected View createView(int position, ViewGroup parent, int desiredHeight) {
        Context context = parent.getContext();
        final LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        final InternalSelectionView isv = new InternalSelectionView(context, 8, "ISV postion " + position);
        isv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                desiredHeight - 240));
        ll.addView(isv);
        final LinearLayout.LayoutParams buttonLp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        40);
        final Button topButton = new Button(context);
        topButton.setLayoutParams(
                buttonLp);
        topButton.setText("button " + position + ")");
        ll.addView(topButton);
        final TextView filler = new TextView(context);
        filler.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                200));
        filler.setText("filler");
        ll.addView(filler);
        return ll;
    }
}
