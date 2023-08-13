public class ButtonsWithTallTextViewInBetween extends ScrollViewScenario {
    public Button getTopButton() {
        return getContentChildAt(0);
    }
    public TextView getMiddleFiller() {
        return getContentChildAt(1);
    }
    public Button getBottomButton() {
        LinearLayout ll = getContentChildAt(2);
        return (Button) ll.getChildAt(0);
    }
    protected void init(Params params) {
        params.addButton("top button", 0.2f)
                .addTextView("middle filler", 1.51f)
                .addVerticalLLOfButtons("bottom", 1, 0.2f);
    }
}
