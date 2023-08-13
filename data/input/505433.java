public class ListTopGravity extends ListScenario {
    @Override
    protected void init(Params params) {
        params.setStackFromBottom(false)
                .setStartingSelectionPosition(-1)
                .setMustFillScreen(false)
                .setNumItems(2)
                .setItemScreenSizeFactor(0.22);
    }
}
