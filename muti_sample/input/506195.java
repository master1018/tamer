public class ListBottomGravityMany extends ListScenario {
    @Override
    protected void init(Params params) {
        params.setStackFromBottom(true)
                .setStartingSelectionPosition(-1)
                .setNumItems(10)
                .setItemScreenSizeFactor(0.22);
    }
}
