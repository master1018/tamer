public class ListWithScreenOfNoSelectables extends ListScenario {
    protected void init(Params params) {
        params.setNumItems(10)
                .setItemScreenSizeFactor(0.2)
                .setPositionsUnselectable(1, 2, 3, 4, 5, 6, 7, 8, 9);
    }
}
