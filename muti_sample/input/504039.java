public class CheckBox extends CompoundButton {
    public CheckBox(Context context) {
        this(context, null);
    }
    public CheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.checkboxStyle);
    }
    public CheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
