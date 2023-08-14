public class OrientableFlowLayout extends FlowLayout {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL   = 1;
    public static final int TOP        = 0;
    public static final int BOTTOM     = 2; 
    int orientation;
    int vAlign;
    int vHGap;
    int vVGap;
    public OrientableFlowLayout() {
        this(HORIZONTAL, CENTER, CENTER, 5, 5, 5, 5);
    }
    public OrientableFlowLayout(int orientation) {
        this(orientation, CENTER, CENTER, 5, 5, 5, 5);
    }
    public OrientableFlowLayout(int orientation, int hAlign, int vAlign) {
        this(orientation, hAlign, vAlign, 5, 5, 5, 5);
    }
    public OrientableFlowLayout(int orientation, int hAlign, int vAlign, int hHGap, int hVGap, int vHGap, int vVGap) {
        super(hAlign, hHGap, hVGap);
        this.orientation = orientation;
        this.vAlign      = vAlign;
        this.vHGap       = vHGap;
        this.vVGap       = vVGap;
    }
    public synchronized void orientHorizontally() {
        orientation = HORIZONTAL;
    }
    public synchronized void orientVertically() {
        orientation = VERTICAL;
    }
    public Dimension preferredLayoutSize(Container target) {
        if (orientation == HORIZONTAL) {
            return super.preferredLayoutSize(target);
        }
        else {
            Dimension dim = new Dimension(0, 0);
            int n = target.countComponents();
            for (int i = 0; i < n; i++) {
                Component c = target.getComponent(i);
                if (c.isVisible()) {
                    Dimension cDim = c.preferredSize();
                    dim.width = Math.max(dim.width, cDim.width);
                    if (i > 0) {
                        dim.height += vVGap;
                    }
                    dim.height += cDim.height;
                }
            }
            Insets insets = target.insets();;
            dim.width  += insets.left + insets.right  + vHGap*2;
            dim.height += insets.top  + insets.bottom + vVGap*2;
            return dim;
        }
    }
    public Dimension minimumLayoutSize(Container target) {
        if (orientation == HORIZONTAL) {
            return super.minimumLayoutSize(target);
        }
        else {
            Dimension dim = new Dimension(0, 0);
            int n = target.countComponents();
            for (int i = 0; i < n; i++) {
                Component c = target.getComponent(i);
                if (c.isVisible()) {
                    Dimension cDim = c.minimumSize();
                    dim.width = Math.max(dim.width, cDim.width);
                    if (i > 0) {
                        dim.height += vVGap;
                    }
                    dim.height += cDim.height;
                }
            }
            Insets insets = target.insets();
            dim.width  += insets.left + insets.right  + vHGap*2;
            dim.height += insets.top  + insets.bottom + vVGap*2;
            return dim;
        }
    }
    public void layoutContainer(Container target) {
        if (orientation == HORIZONTAL) {
            super.layoutContainer(target);
        }
        else {
            Insets insets = target.insets();
            Dimension targetDim = target.size();
            int maxHeight = targetDim.height - (insets.top + insets.bottom + vVGap*2);
            int x = insets.left + vHGap;
            int y = 0;
            int colWidth = 0;
            int start = 0;
            int n = target.countComponents();
            for (int i = 0; i < n; i++) {
                Component c = target.getComponent(i);
                if (c.isVisible()) {
                    Dimension cDim = c.preferredSize();
                    c.resize(cDim.width, cDim.height);
                    if ((y == 0) || ((y + cDim.height) <= maxHeight)) {
                        if (y > 0) {
                            y += vVGap;
                        }
                        y += cDim.height;
                        colWidth = Math.max(colWidth, cDim.width);
                    }
                    else {
                        moveComponents(target,
                                       x,
                                       insets.top + vVGap,
                                       colWidth,
                                       maxHeight - y,
                                       start,
                                       i);
                        x += vHGap + colWidth;
                        y = cDim.width;
                        colWidth = cDim.width;
                        start = i;
                    }
                }
            }
            moveComponents(target,
                           x,
                           insets.top + vVGap,
                           colWidth,
                           maxHeight - y,
                           start,
                           n);
        }
    }
    private void moveComponents(Container target, int x, int y, int width, int height, int colStart, int colEnd) {
        switch (vAlign) {
        case TOP:
            break;
        case CENTER:
            y += height/2;
            break;
        case BOTTOM:
            y += height;
        }
        for (int i = colStart; i < colEnd; i++) {
            Component c = target.getComponent(i);
            Dimension cDim = c.size();
            if (c.isVisible()) {
                c.move(x + (width - cDim.width)/2, y);
                y += vVGap + cDim.height;
            }
        }
    }
    public String toString() {
        String str = "";
        switch (orientation) {
        case HORIZONTAL:
            str = "orientation=horizontal, ";
            break;
        case VERTICAL:
            str = "orientation=vertical, ";
            break;
        }
        return getClass().getName() + "[" + str + super.toString() + "]";
    }
}
