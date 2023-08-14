public class GridSetSelection extends GridScenario {
    @Override
    protected void init(Params params) {
        params.setStackFromBottom(false)
                .setStartingSelectionPosition(-1)
                .setMustFillScreen(false)
                .setNumItems(15)
                .setNumColumns(4)
                .setItemScreenSizeFactor(0.12);
    }
}
