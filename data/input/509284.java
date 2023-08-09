public class PhoneNumberFormattingTextWatcher implements TextWatcher {
    static private int sFormatType;
    static private Locale sCachedLocale;
    private boolean mFormatting;
    private boolean mDeletingHyphen;
    private int mHyphenStart;
    private boolean mDeletingBackward;
    public PhoneNumberFormattingTextWatcher() {
        if (sCachedLocale == null || sCachedLocale != Locale.getDefault()) {
            sCachedLocale = Locale.getDefault();
            sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
        }
    }
    public synchronized void afterTextChanged(Editable text) {
        if (!mFormatting) {
            mFormatting = true;
            if (mDeletingHyphen && mHyphenStart > 0) {
                if (mDeletingBackward) {
                    if (mHyphenStart - 1 < text.length()) {
                        text.delete(mHyphenStart - 1, mHyphenStart);
                    }
                } else if (mHyphenStart < text.length()) {
                    text.delete(mHyphenStart, mHyphenStart + 1);
                }
            }
            PhoneNumberUtils.formatNumber(text, sFormatType);
            mFormatting = false;
        }
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!mFormatting) {
            final int selStart = Selection.getSelectionStart(s);
            final int selEnd = Selection.getSelectionEnd(s);
            if (s.length() > 1 
                    && count == 1 
                    && after == 0 
                    && s.charAt(start) == '-' 
                    && selStart == selEnd) { 
                mDeletingHyphen = true;
                mHyphenStart = start;
                if (selStart == start + 1) {
                    mDeletingBackward = true;
                } else {
                    mDeletingBackward = false;
                }
            } else {
                mDeletingHyphen = false;
            }
        }
    }
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
