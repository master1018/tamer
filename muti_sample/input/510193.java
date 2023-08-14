public class GridSetSelectionStackFromBottom extends GridScenario {
    @Override
    protected void init(Params params) {
        params.setStackFromBottom(true)
                .setStartingSelectionPosition(-1)
                .setMustFillScreen(false)
                .setNumItems(15)
                .setNumColumns(4)
                .setItemScreenSizeFactor(0.12);
    }
}
