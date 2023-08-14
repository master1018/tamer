public class EditText extends TextView {
    public EditText(Context context) {
        this(context, null);
    }
    public EditText(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.editTextStyle);
    }
    public EditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    protected boolean getDefaultEditable() {
        return true;
    }
    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ArrowKeyMovementMethod.getInstance();
    }
    @Override
    public Editable getText() {
        return (Editable) super.getText();
    }
    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, BufferType.EDITABLE);
    }
    public void setSelection(int start, int stop) {
        Selection.setSelection(getText(), start, stop);
    }
    public void setSelection(int index) {
        Selection.setSelection(getText(), index);
    }
    public void selectAll() {
        Selection.selectAll(getText());
    }
    public void extendSelection(int index) {
        Selection.extendSelection(getText(), index);
    }
    @Override
    public void setEllipsize(TextUtils.TruncateAt ellipsis) {
        if (ellipsis == TextUtils.TruncateAt.MARQUEE) {
            throw new IllegalArgumentException("EditText cannot use the ellipsize mode "
                    + "TextUtils.TruncateAt.MARQUEE");
        }
        super.setEllipsize(ellipsis);
    }
}
