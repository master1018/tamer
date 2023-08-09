public class Button extends TextView {
    public Button(Context context) {
        this(context, null);
    }
    public Button(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.buttonStyle);
    }
    public Button(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
