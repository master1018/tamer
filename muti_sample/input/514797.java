public class GridVerticalSpacing extends GridScenario {
    @Override
    protected void init(Params params) {
        params.setStartingSelectionPosition(-1)
                .setMustFillScreen(false)
                .setNumItems(101)
                .setNumColumns(4)
                .setItemScreenSizeFactor(0.20)
                .setVerticalSpacing(20);
    }
}
