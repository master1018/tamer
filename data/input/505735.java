public class DontPressWithParentImageView extends ImageView {
    public DontPressWithParentImageView(Context context, AttributeSet attrs) {
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
