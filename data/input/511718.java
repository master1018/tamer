public class ListOfItemsShorterThanScreen extends ListScenario {
    protected void init(Params params) {
        params.setNumItems(5)
            .setItemScreenSizeFactor(1.1 / 4)
            .setItemsFocusable(false);
    }
}
