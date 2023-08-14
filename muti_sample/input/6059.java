class WCheckboxMenuItemPeer extends WMenuItemPeer implements CheckboxMenuItemPeer {
    public native void setState(boolean t);
    WCheckboxMenuItemPeer(CheckboxMenuItem target) {
        super(target, true);
        setState(target.getState());
    }
    public void handleAction(final boolean state) {
        final CheckboxMenuItem target = (CheckboxMenuItem)this.target;
        WToolkit.executeOnEventHandlerThread(target, new Runnable() {
            public void run() {
                target.setState(state);
                postEvent(new ItemEvent(target, ItemEvent.ITEM_STATE_CHANGED,
                                        target.getLabel(), (state)
                                          ? ItemEvent.SELECTED
                                          : ItemEvent.DESELECTED));
            }
        });
    }
}
