public class RadioButton extends CompoundButton {
    public RadioButton(Context context) {
        this(context, null);
    }
    public RadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.radioButtonStyle);
    }
    public RadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public void toggle() {
        if (!isChecked()) {
            super.toggle();
        }
    }
}
