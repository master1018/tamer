public class ButtonAboveTallInternalSelectionView extends ScrollViewScenario {
    private final int mNumRowsInIsv = 5;
    public Button getButtonAbove() {
        return getContentChildAt(0);
    }
    public InternalSelectionView getIsv() {
        return getContentChildAt(1);
    }
    protected void init(Params params) {
        params.addButton("howdy", 0.1f)
                .addInternalSelectionView(mNumRowsInIsv, 1.1f)
                .addButton("below", 0.1f);
    }
}
