class WButtonPeer extends WComponentPeer implements ButtonPeer {
    static {
        initIDs();
    }
    public Dimension getMinimumSize() {
        FontMetrics fm = getFontMetrics(((Button)target).getFont());
        String label = ((Button)target).getLabel();
        if ( label == null ) {
            label = "";
        }
        return new Dimension(fm.stringWidth(label) + 14,
                             fm.getHeight() + 8);
    }
    public boolean isFocusable() {
        return true;
    }
    public native void setLabel(String label);
    WButtonPeer(Button target) {
        super(target);
    }
    native void create(WComponentPeer peer);
    public void handleAction(final long when, final int modifiers) {
        WToolkit.executeOnEventHandlerThread(target, new Runnable() {
            public void run() {
                postEvent(new ActionEvent(target, ActionEvent.ACTION_PERFORMED,
                                          ((Button)target).getActionCommand(),
                                          when, modifiers));
            }
        }, when);
    }
    public boolean shouldClearRectBeforePaint() {
        return false;
    }
    public Dimension minimumSize() {
        return getMinimumSize();
    }
    private static native void initIDs();
    public boolean handleJavaKeyEvent(KeyEvent e) {
         switch (e.getID()) {
            case KeyEvent.KEY_RELEASED:
                if (e.getKeyCode() == KeyEvent.VK_SPACE){
                    handleAction(e.getWhen(), e.getModifiers());
                }
            break;
         }
         return false;
    }
}
