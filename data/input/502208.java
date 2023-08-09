public class ListWithEditTextHeader extends ListScenario {
    @Override
    protected void init(Params params) {
        params.setHeaderViewCount(1)
                .setHeaderFocusable(true)
                .setItemsFocusable(true)
                .setNumItems(6)
                .setItemScreenSizeFactor(0.2);
    }
}
