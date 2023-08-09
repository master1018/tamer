public class TextSwitcher extends ViewSwitcher {
    public TextSwitcher(Context context) {
        super(context);
    }
    public TextSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!(child instanceof TextView)) {
            throw new IllegalArgumentException(
                    "TextSwitcher children must be instances of TextView");
        }
        super.addView(child, index, params);
    }
    public void setText(CharSequence text) {
        final TextView t = (TextView) getNextView();
        t.setText(text);
        showNext();
    }
    public void setCurrentText(CharSequence text) {
        ((TextView)getCurrentView()).setText(text);
    }
}
