class XCheckboxMenuItemPeer extends XMenuItemPeer implements CheckboxMenuItemPeer {
    private final static Field f_state;
    static {
        f_state = SunToolkit.getField(CheckboxMenuItem.class, "state");
    }
    XCheckboxMenuItemPeer(CheckboxMenuItem target) {
        super(target);
    }
    public void setState(boolean t) {
        repaintIfShowing();
    }
    boolean getTargetState() {
        MenuItem target = getTarget();
        if (target == null) {
            return false;
        }
        try {
            return f_state.getBoolean(target);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }
    void action(final long when) {
        XToolkit.executeOnEventHandlerThread((CheckboxMenuItem)getTarget(), new Runnable() {
                public void run() {
                    doToggleState(when);
                }
            });
    }
    private void doToggleState(long when) {
        CheckboxMenuItem cb = (CheckboxMenuItem)getTarget();
        boolean newState = !getTargetState();
        cb.setState(newState);
        ItemEvent e = new ItemEvent(cb,
                                    ItemEvent.ITEM_STATE_CHANGED,
                                    getTargetLabel(),
                                    getTargetState() ? ItemEvent.SELECTED : ItemEvent.DESELECTED);
        XWindow.postEventStatic(e);
    }
} 
