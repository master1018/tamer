public class WindowsDesktopManager extends DefaultDesktopManager
        implements java.io.Serializable, javax.swing.plaf.UIResource {
    private WeakReference<JInternalFrame> currentFrameRef;
    public void activateFrame(JInternalFrame f) {
        JInternalFrame currentFrame = currentFrameRef != null ?
            currentFrameRef.get() : null;
        try {
            super.activateFrame(f);
            if (currentFrame != null && f != currentFrame) {
                if (currentFrame.isMaximum() &&
                    (f.getClientProperty("JInternalFrame.frameType") !=
                    "optionDialog") ) {
                    if (!currentFrame.isIcon()) {
                        currentFrame.setMaximum(false);
                        if (f.isMaximizable()) {
                            if (!f.isMaximum()) {
                                f.setMaximum(true);
                            } else if (f.isMaximum() && f.isIcon()) {
                                f.setIcon(false);
                            } else {
                                f.setMaximum(false);
                            }
                        }
                    }
                }
                if (currentFrame.isSelected()) {
                    currentFrame.setSelected(false);
                }
            }
            if (!f.isSelected()) {
                f.setSelected(true);
            }
        } catch (PropertyVetoException e) {}
        if (f != currentFrame) {
            currentFrameRef = new WeakReference<JInternalFrame>(f);
        }
    }
}
