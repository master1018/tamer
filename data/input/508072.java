public class ListEndingWithMultipleSeparators extends ListScenario {
    protected void init(Params params) {
        params.setItemsFocusable(false)
                .setNumItems(5)
                .setItemScreenSizeFactor(0.22)
                .setPositionUnselectable(3)
                .setPositionUnselectable(4);
    }
}
