public class FieldView extends PlainView {
    public FieldView(Element elem) {
        super(elem);
    }
    protected FontMetrics getFontMetrics() {
        Component c = getContainer();
        return c.getFontMetrics(c.getFont());
    }
    protected Shape adjustAllocation(Shape a) {
        if (a != null) {
            Rectangle bounds = a.getBounds();
            int vspan = (int) getPreferredSpan(Y_AXIS);
            int hspan = (int) getPreferredSpan(X_AXIS);
            if (bounds.height != vspan) {
                int slop = bounds.height - vspan;
                bounds.y += slop / 2;
                bounds.height -= slop;
            }
            Component c = getContainer();
            if (c instanceof JTextField) {
                JTextField field = (JTextField) c;
                BoundedRangeModel vis = field.getHorizontalVisibility();
                int max = Math.max(hspan, bounds.width);
                int value = vis.getValue();
                int extent = Math.min(max, bounds.width - 1);
                if ((value + extent) > max) {
                    value = max - extent;
                }
                vis.setRangeProperties(value, extent, vis.getMinimum(),
                                       max, false);
                if (hspan < bounds.width) {
                    int slop = bounds.width - 1 - hspan;
                    int align = ((JTextField)c).getHorizontalAlignment();
                    if(Utilities.isLeftToRight(c)) {
                        if(align==LEADING) {
                            align = LEFT;
                        }
                        else if(align==TRAILING) {
                            align = RIGHT;
                        }
                    }
                    else {
                        if(align==LEADING) {
                            align = RIGHT;
                        }
                        else if(align==TRAILING) {
                            align = LEFT;
                        }
                    }
                    switch (align) {
                    case SwingConstants.CENTER:
                        bounds.x += slop / 2;
                        bounds.width -= slop;
                        break;
                    case SwingConstants.RIGHT:
                        bounds.x += slop;
                        bounds.width -= slop;
                        break;
                    }
                } else {
                    bounds.width = hspan;
                    bounds.x -= vis.getValue();
                }
            }
            return bounds;
        }
        return null;
    }
    void updateVisibilityModel() {
        Component c = getContainer();
        if (c instanceof JTextField) {
            JTextField field = (JTextField) c;
            BoundedRangeModel vis = field.getHorizontalVisibility();
            int hspan = (int) getPreferredSpan(X_AXIS);
            int extent = vis.getExtent();
            int maximum = Math.max(hspan, extent);
            extent = (extent == 0) ? maximum : extent;
            int value = maximum - extent;
            int oldValue = vis.getValue();
            if ((oldValue + extent) > maximum) {
                oldValue = maximum - extent;
            }
            value = Math.max(0, Math.min(value, oldValue));
            vis.setRangeProperties(value, extent, 0, maximum, false);
        }
    }
    public void paint(Graphics g, Shape a) {
        Rectangle r = (Rectangle) a;
        g.clipRect(r.x, r.y, r.width, r.height);
        super.paint(g, a);
    }
    Shape adjustPaintRegion(Shape a) {
        return adjustAllocation(a);
    }
    public float getPreferredSpan(int axis) {
        switch (axis) {
        case View.X_AXIS:
            Segment buff = SegmentCache.getSharedSegment();
            Document doc = getDocument();
            int width;
            try {
                FontMetrics fm = getFontMetrics();
                doc.getText(0, doc.getLength(), buff);
                width = Utilities.getTabbedTextWidth(buff, fm, 0, this, 0);
                if (buff.count > 0) {
                    Component c = getContainer();
                    firstLineOffset = sun.swing.SwingUtilities2.
                        getLeftSideBearing((c instanceof JComponent) ?
                                           (JComponent)c : null, fm,
                                           buff.array[buff.offset]);
                    firstLineOffset = Math.max(0, -firstLineOffset);
                }
                else {
                    firstLineOffset = 0;
                }
            } catch (BadLocationException bl) {
                width = 0;
            }
            SegmentCache.releaseSharedSegment(buff);
            return width + firstLineOffset;
        default:
            return super.getPreferredSpan(axis);
        }
    }
    public int getResizeWeight(int axis) {
        if (axis == View.X_AXIS) {
            return 1;
        }
        return 0;
    }
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        return super.modelToView(pos, adjustAllocation(a), b);
    }
    public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
        return super.viewToModel(fx, fy, adjustAllocation(a), bias);
    }
    public void insertUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        super.insertUpdate(changes, adjustAllocation(a), f);
        updateVisibilityModel();
    }
    public void removeUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        super.removeUpdate(changes, adjustAllocation(a), f);
        updateVisibilityModel();
    }
}
