public class ListOfShortTallShort extends ListScenario {
    protected void init(Params params) {
        params.setItemScreenSizeFactor(1.0 / 8)
                .setNumItems(3)
                .setPositionScreenSizeFactorOverride(1, 1.2);
    }
}
