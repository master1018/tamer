public class ExtractEditText extends EditText {
    private InputMethodService mIME;
    private int mSettingExtractedText;
    public ExtractEditText(Context context) {
        super(context, null);
    }
    public ExtractEditText(Context context, AttributeSet attrs) {
        super(context, attrs, com.android.internal.R.attr.editTextStyle);
    }
    public ExtractEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    void setIME(InputMethodService ime) {
        mIME = ime;
    }
    public void startInternalChanges() {
        mSettingExtractedText += 1;
    }
    public void finishInternalChanges() {
        mSettingExtractedText -= 1;
    }
    @Override public void setExtractedText(ExtractedText text) {
        try {
            mSettingExtractedText++;
            super.setExtractedText(text);
        } finally {
            mSettingExtractedText--;
        }
    }
    @Override protected void onSelectionChanged(int selStart, int selEnd) {
        if (mSettingExtractedText == 0 && mIME != null && selStart >= 0 && selEnd >= 0) {
            mIME.onExtractedSelectionChanged(selStart, selEnd);
        }
    }
    @Override public boolean performClick() {
        if (!super.performClick() && mIME != null) {
            mIME.onExtractedTextClicked();
            return true;
        }
        return false;
    }
    @Override public boolean onTextContextMenuItem(int id) {
        if (mIME != null) {
            if (mIME.onExtractTextContextMenuItem(id)) {
                return true;
            }
        }
        return super.onTextContextMenuItem(id);
    }
    public boolean isInputMethodTarget() {
        return true;
    }
    public boolean hasVerticalScrollBar() {
        return computeVerticalScrollRange() > computeVerticalScrollExtent();
    }
    @Override public boolean hasWindowFocus() {
        return this.isEnabled() ? true : false;
    }
    @Override public boolean isFocused() {
        return this.isEnabled() ? true : false;
    }
    @Override public boolean hasFocus() {
        return this.isEnabled() ? true : false;
    }
}
