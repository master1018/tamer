public class ListBottomGravity extends ListScenario {
    @Override
    protected void init(Params params) {
        params.setStackFromBottom(true)
                .setStartingSelectionPosition(-1)
                .setMustFillScreen(false)
                .setNumItems(2)
                .setItemScreenSizeFactor(0.22);
    }
}
