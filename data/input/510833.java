public class GridSingleColumn extends GridScenario {
    @Override
    protected void init(Params params) {
        params.setStartingSelectionPosition(-1)
                .setMustFillScreen(false)
                .setNumItems(101)
                .setNumColumns(1)
                .setColumnWidth(60)
                .setItemScreenSizeFactor(0.20)
                .setVerticalSpacing(20)
                .setStretchMode(GridView.STRETCH_SPACING);
    }
}
