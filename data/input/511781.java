public class GridVerticalSpacingStackFromBottom extends GridScenario {
    @Override
    protected void init(Params params) {
        params.setStackFromBottom(true)
                .setStartingSelectionPosition(-1)
                .setMustFillScreen(false)
                .setNumItems(101)
                .setNumColumns(4)
                .setItemScreenSizeFactor(0.20)
                .setVerticalSpacing(20);
    }
}
