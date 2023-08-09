public class DontPressWithParentLayout extends LinearLayout {
    public DontPressWithParentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public void setPressed(boolean pressed) {
        if (pressed && ((View) getParent()).isPressed()) {
            return;
        }
        super.setPressed(pressed);
    }
}
