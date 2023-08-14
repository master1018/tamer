public class ShortButtons extends ScrollViewScenario {
    private final int mNumButtons = 10;
    protected final float mButtonHeightFactor = 0.2f;
    public int getNumButtons() {
        return mNumButtons;
    }
    public Button getButtonAt(int index) {
        if (index < 3) {
            return getContentChildAt(index);
        } else {
            LinearLayout ll = getContentChildAt(3);
            return (Button) ll.getChildAt(index - 3);
        }
    }
    @Override
    protected void init(Params params) {
        final int numButtonsInSubLayout = getNumButtons() - 3;
        params.addButtons(3, "top-level", mButtonHeightFactor)
                .addVerticalLLOfButtons("embedded",
                        numButtonsInSubLayout,
                        numButtonsInSubLayout * mButtonHeightFactor);
    }
}
