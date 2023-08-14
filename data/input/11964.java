class XHorizontalScrollbar extends XScrollbar {
    public XHorizontalScrollbar(XScrollbarClient sb) {
        super(ALIGNMENT_HORIZONTAL, sb);
    }
    public void setSize(int width, int height) {
        super.setSize(width, height);
        this.barWidth = height;
        this.barLength = width;
        calculateArrowWidth();
        rebuildArrows();
    }
    protected void rebuildArrows() {
        firstArrow = createArrowShape(false, true);
        secondArrow = createArrowShape(false, false);
    }
    boolean beforeThumb(int x, int y) {
        Rectangle pos = calculateThumbRect();
        return (x < pos.x);
    }
    protected Rectangle getThumbArea() {
        return new Rectangle(getArrowAreaWidth(), 2, width - 2*getArrowAreaWidth(), height-4);
    }
}
