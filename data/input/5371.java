class WScrollbarPeer extends WComponentPeer implements ScrollbarPeer {
    static native int getScrollbarSize(int orientation);
    public Dimension getMinimumSize() {
        if (((Scrollbar)target).getOrientation() == Scrollbar.VERTICAL) {
            return new Dimension(getScrollbarSize(Scrollbar.VERTICAL), 50);
        }
        else {
            return new Dimension(50, getScrollbarSize(Scrollbar.HORIZONTAL));
        }
    }
    public native void setValues(int value, int visible,
                                 int minimum, int maximum);
    public native void setLineIncrement(int l);
    public native void setPageIncrement(int l);
    WScrollbarPeer(Scrollbar target) {
        super(target);
    }
    native void create(WComponentPeer parent);
    void initialize() {
        Scrollbar sb = (Scrollbar)target;
        setValues(sb.getValue(), sb.getVisibleAmount(),
                  sb.getMinimum(), sb.getMaximum());
        super.initialize();
    }
    private void postAdjustmentEvent(final int type, final int value,
                                     final boolean isAdjusting)
    {
        final Scrollbar sb = (Scrollbar)target;
        WToolkit.executeOnEventHandlerThread(sb, new Runnable() {
            public void run() {
                sb.setValueIsAdjusting(isAdjusting);
                sb.setValue(value);
                postEvent(new AdjustmentEvent(sb,
                                AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED,
                                type, value, isAdjusting));
            }
        });
    }
    void lineUp(int value) {
        postAdjustmentEvent(AdjustmentEvent.UNIT_DECREMENT, value, false);
    }
    void lineDown(int value) {
        postAdjustmentEvent(AdjustmentEvent.UNIT_INCREMENT, value, false);
    }
    void pageUp(int value) {
        postAdjustmentEvent(AdjustmentEvent.BLOCK_DECREMENT, value, false);
    }
    void pageDown(int value) {
        postAdjustmentEvent(AdjustmentEvent.BLOCK_INCREMENT, value, false);
    }
    void warp(int value) {
        postAdjustmentEvent(AdjustmentEvent.TRACK, value, false);
    }
    private boolean dragInProgress = false;
    void drag(final int value) {
        if (!dragInProgress) {
            dragInProgress = true;
        }
        postAdjustmentEvent(AdjustmentEvent.TRACK, value, true);
    }
    void dragEnd(final int value) {
        final Scrollbar sb = (Scrollbar)target;
        if (!dragInProgress) {
            return;
        }
        dragInProgress = false;
        WToolkit.executeOnEventHandlerThread(sb, new Runnable() {
            public void run() {
                sb.setValueIsAdjusting(false);
                postEvent(new AdjustmentEvent(sb,
                                AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED,
                                AdjustmentEvent.TRACK, value, false));
            }
        });
    }
    public boolean shouldClearRectBeforePaint() {
        return false;
    }
    public Dimension minimumSize() {
        return getMinimumSize();
    }
}
