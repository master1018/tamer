public class ListTopGravityMany extends ListScenario {
    @Override
    protected void init(Params params) {
        params.setStackFromBottom(false)
                .setStartingSelectionPosition(-1)
                .setNumItems(10)
                .setItemScreenSizeFactor(0.22);
    }    
}
