public class ListWithOffScreenNextSelectable extends ListScenario {
    protected void init(Params params) {
        params.setItemsFocusable(false)
                .setNumItems(5)
                .setItemScreenSizeFactor(0.25)
                .setPositionUnselectable(1)
                .setPositionUnselectable(2)
                .setPositionUnselectable(3);
    }
}
