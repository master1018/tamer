public class SuggestionsView extends ListView {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.SuggestionsView";
    public SuggestionsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        setItemsCanFocus(true);
    }
    public int getSelectedPosition() {
        return getSelectedItemPosition();
    }
    public SuggestionPosition getSelectedSuggestion() {
        return (SuggestionPosition) getSelectedItem();
    }
}
