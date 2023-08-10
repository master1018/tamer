public class FixedJTable extends JTable {
    private static final long serialVersionUID = 5420351985543138872L;
    public FixedJTable(GenericTable gt) {
        super(gt);
    }
    public boolean getScrollableTracksViewportWidth() {
        if (autoResizeMode != AUTO_RESIZE_OFF) {
            if (getParent() instanceof JViewport) {
                return (((JViewport) getParent()).getWidth() > getPreferredSize().width);
            }
        }
        return false;
    }
}
