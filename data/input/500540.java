public class GridStackFromBottomMany extends GridScenario {
    @Override
    protected void init(Params params) {
        params.setStackFromBottom(true)
                .setStartingSelectionPosition(-1)
                .setMustFillScreen(false)
                .setNumItems(54)
                .setNumColumns(4)
                .setItemScreenSizeFactor(0.12);
    }
}
