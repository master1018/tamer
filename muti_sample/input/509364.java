public class ListOfShortShortTallShortShort extends ListScenario {
    protected void init(Params params) {
        params.setNumItems(5)
                .setItemScreenSizeFactor(0.1)
                .setFadingEdgeScreenSizeFactor(0.22)
                .setPositionScreenSizeFactorOverride(2, 1.1);
    }
}
