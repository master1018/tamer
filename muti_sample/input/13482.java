public class RepaintArea {
    private static final int MAX_BENEFIT_RATIO = 4;
    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;
    private static final int UPDATE = 2;
    private static final int RECT_COUNT = UPDATE + 1;
    private Rectangle paintRects[] = new Rectangle[RECT_COUNT];
    public RepaintArea() {
    }
    private RepaintArea(RepaintArea ra) {
        for (int i = 0; i < RECT_COUNT; i++) {
            paintRects[i] = ra.paintRects[i];
        }
    }
    public synchronized void add(Rectangle r, int id) {
        if (r.isEmpty()) {
            return;
        }
        int addTo = UPDATE;
        if (id == PaintEvent.PAINT) {
            addTo = (r.width > r.height) ? HORIZONTAL : VERTICAL;
        }
        if (paintRects[addTo] != null) {
            paintRects[addTo].add(r);
        } else {
            paintRects[addTo] = new Rectangle(r);
        }
    }
    private synchronized RepaintArea cloneAndReset() {
        RepaintArea ra = new RepaintArea(this);
        for (int i = 0; i < RECT_COUNT; i++) {
            paintRects[i] = null;
        }
        return ra;
    }
    public boolean isEmpty() {
        for (int i = 0; i < RECT_COUNT; i++) {
            if (paintRects[i] != null) {
                return false;
            }
        }
        return true;
    }
    public synchronized void constrain(int x, int y, int w, int h) {
        for (int i = 0; i < RECT_COUNT; i++) {
            Rectangle rect = paintRects[i];
            if (rect != null) {
                if (rect.x < x) {
                    rect.width -= (x - rect.x);
                    rect.x = x;
                }
                if (rect.y < y) {
                    rect.height -= (y - rect.y);
                    rect.y = y;
                }
                int xDelta = rect.x + rect.width - x - w;
                if (xDelta > 0) {
                    rect.width -= xDelta;
                }
                int yDelta = rect.y + rect.height - y - h;
                if (yDelta > 0) {
                    rect.height -= yDelta;
                }
                if (rect.width <= 0 || rect.height <= 0) {
                    paintRects[i] = null;
                }
            }
        }
    }
    public synchronized void subtract(int x, int y, int w, int h) {
        Rectangle subtract = new Rectangle(x, y, w, h);
        for (int i = 0; i < RECT_COUNT; i++) {
            if (subtract(paintRects[i], subtract)) {
                if (paintRects[i] != null && paintRects[i].isEmpty()) {
                    paintRects[i] = null;
                }
            }
        }
    }
    public void paint(Object target, boolean shouldClearRectBeforePaint) {
        Component comp = (Component)target;
        if (isEmpty()) {
            return;
        }
        if (!comp.isVisible()) {
            return;
        }
        RepaintArea ra = this.cloneAndReset();
        if (!subtract(ra.paintRects[VERTICAL], ra.paintRects[HORIZONTAL])) {
            subtract(ra.paintRects[HORIZONTAL], ra.paintRects[VERTICAL]);
        }
        if (ra.paintRects[HORIZONTAL] != null && ra.paintRects[VERTICAL] != null) {
            Rectangle paintRect = ra.paintRects[HORIZONTAL].union(ra.paintRects[VERTICAL]);
            int square = paintRect.width * paintRect.height;
            int benefit = square - ra.paintRects[HORIZONTAL].width
                * ra.paintRects[HORIZONTAL].height - ra.paintRects[VERTICAL].width
                * ra.paintRects[VERTICAL].height;
            if (MAX_BENEFIT_RATIO * benefit < square) {
                ra.paintRects[HORIZONTAL] = paintRect;
                ra.paintRects[VERTICAL] = null;
            }
        }
        for (int i = 0; i < paintRects.length; i++) {
            if (ra.paintRects[i] != null
                && !ra.paintRects[i].isEmpty())
            {
                Graphics g = comp.getGraphics();
                if (g != null) {
                    try {
                        g.setClip(ra.paintRects[i]);
                        if (i == UPDATE) {
                            updateComponent(comp, g);
                        } else {
                            if (shouldClearRectBeforePaint) {
                                g.clearRect( ra.paintRects[i].x,
                                             ra.paintRects[i].y,
                                             ra.paintRects[i].width,
                                             ra.paintRects[i].height);
                            }
                            paintComponent(comp, g);
                        }
                    } finally {
                        g.dispose();
                    }
                }
            }
        }
    }
    protected void updateComponent(Component comp, Graphics g) {
        if (comp != null) {
            comp.update(g);
        }
    }
    protected void paintComponent(Component comp, Graphics g) {
        if (comp != null) {
            comp.paint(g);
        }
    }
    static boolean subtract(Rectangle rect, Rectangle subtr) {
        if (rect == null || subtr == null) {
            return true;
        }
        Rectangle common = rect.intersection(subtr);
        if (common.isEmpty()) {
            return true;
        }
        if (rect.x == common.x && rect.y == common.y) {
            if (rect.width == common.width) {
                rect.y += common.height;
                rect.height -= common.height;
                return true;
            } else
            if (rect.height == common.height) {
                rect.x += common.width;
                rect.width -= common.width;
                return true;
            }
        } else
        if (rect.x + rect.width == common.x + common.width
            && rect.y + rect.height == common.y + common.height)
        {
            if (rect.width == common.width) {
                rect.height -= common.height;
                return true;
            } else
            if (rect.height == common.height) {
                rect.width -= common.width;
                return true;
            }
        }
        return false;
    }
    public String toString() {
        return super.toString() + "[ horizontal=" + paintRects[0] +
            " vertical=" + paintRects[1] +
            " update=" + paintRects[2] + "]";
    }
}
