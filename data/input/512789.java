public class ListWithFirstScreenUnSelectable extends ListScenario {
    @Override
    protected void init(Params params) {
        params.setItemScreenSizeFactor(1.2)
                .setNumItems(2)
                .setPositionsUnselectable(0);
    }
}
