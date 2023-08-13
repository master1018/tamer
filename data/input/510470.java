public class ListOfThinItems extends ListScenario {
    protected void init(Params params) {
        final int numItemsOnScreen = getScreenHeight() / 18;
        params.setNumItems(numItemsOnScreen + 5)
            .setItemScreenSizeFactor(18.0 / getScreenHeight())
            .setItemsFocusable(false);
    }
}
