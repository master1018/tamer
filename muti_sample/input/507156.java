public class ListOfTouchables extends ListScenario {
    @Override
    protected void init(Params params) {
        params.setItemsFocusable(true)
                .setItemScreenSizeFactor(0.2)
                .setNumItems(100);
    }
    @Override
    protected View createView(int position, ViewGroup parent, int desiredHeight) {
        Button b = new Button(this);
        b.setText("Position " + position);
        b.setId(position);
        return b;
    }
}
