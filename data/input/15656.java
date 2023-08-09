public class MotifButtonListener extends BasicButtonListener {
    public MotifButtonListener(AbstractButton b ) {
        super(b);
    }
    protected void checkOpacity(AbstractButton b) {
        b.setOpaque( false );
    }
}
