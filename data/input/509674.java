public class SearchEditText extends EditText {
    private boolean mMagnifyingGlassShown = true;
    private Drawable mMagnifyingGlass;
    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMagnifyingGlass = getCompoundDrawables()[2];
    }
    @Override
    public boolean onPreDraw() {
        boolean emptyText = TextUtils.isEmpty(getText());
        if (mMagnifyingGlassShown != emptyText) {
            mMagnifyingGlassShown = emptyText;
            if (mMagnifyingGlassShown) {
                setCompoundDrawables(null, null, mMagnifyingGlass, null);
            } else {
                setCompoundDrawables(null, null, null, null);
            }
            return false;
        }
        return super.onPreDraw();
    }
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (((ContactsListActivity)getContext()).onKeyPreIme(keyCode, event)) {
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
