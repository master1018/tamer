public class TallTextAboveButton extends ScrollViewScenario {
    protected void init(Params params) {
        params.addTextView("top tall", 1.1f)
                .addButton("button", 0.2f);
    }
}
