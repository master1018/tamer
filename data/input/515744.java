class ExtractButton extends Button {
    public ExtractButton(Context context) {
        super(context, null);
    }
    public ExtractButton(Context context, AttributeSet attrs) {
        super(context, attrs, com.android.internal.R.attr.buttonStyle);
    }
    public ExtractButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override public boolean hasWindowFocus() {
        return this.isEnabled() ? true : false;
    }
}
