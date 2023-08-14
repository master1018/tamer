public class ListOfItemsTallerThanScreen extends ListScenario {
    protected void init(Params params) {
        params.setNumItems(3)
            .setItemScreenSizeFactor(4.0 / 3)
            .setItemsFocusable(false);
    }
}
