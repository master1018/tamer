public class ListWithNoFadingEdge extends ListScenario {
    protected void init(Params params) {
        params.setFadingEdgeScreenSizeFactor(0.0)
                .setNumItems(10)
                .setItemScreenSizeFactor(0.2);
    }
}
