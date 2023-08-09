public abstract class ScrollPaneWheelScroller {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.ScrollPaneWheelScroller");
    private ScrollPaneWheelScroller() {}
    public static void handleWheelScrolling(ScrollPane sp, MouseWheelEvent e) {
        if (log.isLoggable(PlatformLogger.FINER)) {
            log.finer("x = " + e.getX() + ", y = " + e.getY() + ", src is " + e.getSource());
        }
        int increment = 0;
        if (sp != null && e.getScrollAmount() != 0) {
            Adjustable adj = getAdjustableToScroll(sp);
            if (adj != null) {
                increment = getIncrementFromAdjustable(adj, e);
                if (log.isLoggable(PlatformLogger.FINER)) {
                    log.finer("increment from adjustable(" + adj.getClass() + ") : " + increment);
                }
                scrollAdjustable(adj, increment);
            }
        }
    }
    public static Adjustable getAdjustableToScroll(ScrollPane sp) {
        int policy = sp.getScrollbarDisplayPolicy();
        if (policy == ScrollPane.SCROLLBARS_ALWAYS ||
            policy == ScrollPane.SCROLLBARS_NEVER) {
            if (log.isLoggable(PlatformLogger.FINER)) {
                log.finer("using vertical scrolling due to scrollbar policy");
            }
            return sp.getVAdjustable();
        }
        else {
            Insets ins = sp.getInsets();
            int vertScrollWidth = sp.getVScrollbarWidth();
            if (log.isLoggable(PlatformLogger.FINER)) {
                log.finer("insets: l = " + ins.left + ", r = " + ins.right +
                 ", t = " + ins.top + ", b = " + ins.bottom);
                log.finer("vertScrollWidth = " + vertScrollWidth);
            }
            if (ins.right >= vertScrollWidth) {
                if (log.isLoggable(PlatformLogger.FINER)) {
                    log.finer("using vertical scrolling because scrollbar is present");
                }
                return sp.getVAdjustable();
            }
            else {
                int horizScrollHeight = sp.getHScrollbarHeight();
                if (ins.bottom >= horizScrollHeight) {
                    if (log.isLoggable(PlatformLogger.FINER)) {
                        log.finer("using horiz scrolling because scrollbar is present");
                    }
                    return sp.getHAdjustable();
                }
                else {
                    if (log.isLoggable(PlatformLogger.FINER)) {
                        log.finer("using NO scrollbar becsause neither is present");
                    }
                    return null;
                }
            }
        }
    }
    public static int getIncrementFromAdjustable(Adjustable adj,
                                                 MouseWheelEvent e) {
        if (log.isLoggable(PlatformLogger.FINE)) {
            if (adj == null) {
                log.fine("Assertion (adj != null) failed");
            }
        }
        int increment = 0;
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            increment = e.getUnitsToScroll() * adj.getUnitIncrement();
        }
        else if (e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
            increment = adj.getBlockIncrement() * e.getWheelRotation();
        }
        return increment;
    }
    public static void scrollAdjustable(Adjustable adj, int amount) {
        if (log.isLoggable(PlatformLogger.FINE)) {
            if (adj == null) {
                log.fine("Assertion (adj != null) failed");
            }
            if (amount == 0) {
                log.fine("Assertion (amount != 0) failed");
            }
        }
        int current = adj.getValue();
        int upperLimit = adj.getMaximum() - adj.getVisibleAmount();
        if (log.isLoggable(PlatformLogger.FINER)) {
            log.finer("doScrolling by " + amount);
        }
        if (amount > 0 && current < upperLimit) { 
            if (current + amount < upperLimit) {
                adj.setValue(current + amount);
                return;
            }
            else {
                adj.setValue(upperLimit);
                return;
            }
        }
        else if (amount < 0 && current > adj.getMinimum()) { 
            if (current + amount > adj.getMinimum()) {
                adj.setValue(current + amount);
                return;
            }
            else {
                adj.setValue(adj.getMinimum());
                return;
            }
        }
    }
}
