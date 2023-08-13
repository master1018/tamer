public class ComponentView extends View  {
    public ComponentView(Element elem) {
        super(elem);
    }
    protected Component createComponent() {
        AttributeSet attr = getElement().getAttributes();
        Component comp = StyleConstants.getComponent(attr);
        return comp;
    }
    public final Component getComponent() {
        return createdC;
    }
    public void paint(Graphics g, Shape a) {
        if (c != null) {
            Rectangle alloc = (a instanceof Rectangle) ?
                (Rectangle) a : a.getBounds();
            c.setBounds(alloc.x, alloc.y, alloc.width, alloc.height);
        }
    }
    public float getPreferredSpan(int axis) {
        if ((axis != X_AXIS) && (axis != Y_AXIS)) {
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }
        if (c != null) {
            Dimension size = c.getPreferredSize();
            if (axis == View.X_AXIS) {
                return size.width;
            } else {
                return size.height;
            }
        }
        return 0;
    }
    public float getMinimumSpan(int axis) {
        if ((axis != X_AXIS) && (axis != Y_AXIS)) {
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }
        if (c != null) {
            Dimension size = c.getMinimumSize();
            if (axis == View.X_AXIS) {
                return size.width;
            } else {
                return size.height;
            }
        }
        return 0;
    }
    public float getMaximumSpan(int axis) {
        if ((axis != X_AXIS) && (axis != Y_AXIS)) {
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }
        if (c != null) {
            Dimension size = c.getMaximumSize();
            if (axis == View.X_AXIS) {
                return size.width;
            } else {
                return size.height;
            }
        }
        return 0;
    }
    public float getAlignment(int axis) {
        if (c != null) {
            switch (axis) {
            case View.X_AXIS:
                return c.getAlignmentX();
            case View.Y_AXIS:
                return c.getAlignmentY();
            }
        }
        return super.getAlignment(axis);
    }
    public void setParent(View p) {
        super.setParent(p);
        if (SwingUtilities.isEventDispatchThread()) {
            setComponentParent();
        } else {
            Runnable callSetComponentParent = new Runnable() {
                public void run() {
                    Document doc = getDocument();
                    try {
                        if (doc instanceof AbstractDocument) {
                            ((AbstractDocument)doc).readLock();
                        }
                        setComponentParent();
                        Container host = getContainer();
                        if (host != null) {
                            preferenceChanged(null, true, true);
                            host.repaint();
                        }
                    } finally {
                        if (doc instanceof AbstractDocument) {
                            ((AbstractDocument)doc).readUnlock();
                        }
                    }
                }
            };
            SwingUtilities.invokeLater(callSetComponentParent);
        }
    }
    void setComponentParent() {
        View p = getParent();
        if (p != null) {
            Container parent = getContainer();
            if (parent != null) {
                if (c == null) {
                    Component comp = createComponent();
                    if (comp != null) {
                        createdC = comp;
                        c = new Invalidator(comp);
                    }
                }
                if (c != null) {
                    if (c.getParent() == null) {
                        parent.add(c, this);
                        parent.addPropertyChangeListener("enabled", c);
                    }
                }
            }
        } else {
            if (c != null) {
                Container parent = c.getParent();
                if (parent != null) {
                    parent.remove(c);
                    parent.removePropertyChangeListener("enabled", c);
                }
            }
        }
    }
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        int p0 = getStartOffset();
        int p1 = getEndOffset();
        if ((pos >= p0) && (pos <= p1)) {
            Rectangle r = a.getBounds();
            if (pos == p1) {
                r.x += r.width;
            }
            r.width = 0;
            return r;
        }
        throw new BadLocationException(pos + " not in range " + p0 + "," + p1, pos);
    }
    public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
        Rectangle alloc = (Rectangle) a;
        if (x < alloc.x + (alloc.width / 2)) {
            bias[0] = Position.Bias.Forward;
            return getStartOffset();
        }
        bias[0] = Position.Bias.Backward;
        return getEndOffset();
    }
    private Component createdC;
    private Invalidator c;
    class Invalidator extends Container implements PropertyChangeListener {
        Invalidator(Component child) {
            setLayout(null);
            add(child);
            cacheChildSizes();
        }
        public void invalidate() {
            super.invalidate();
            if (getParent() != null) {
                preferenceChanged(null, true, true);
            }
        }
        public void doLayout() {
            cacheChildSizes();
        }
        public void setBounds(int x, int y, int w, int h) {
            super.setBounds(x, y, w, h);
            if (getComponentCount() > 0) {
                getComponent(0).setSize(w, h);
            }
            cacheChildSizes();
        }
        public void validateIfNecessary() {
            if (!isValid()) {
                validate();
             }
        }
        private void cacheChildSizes() {
            if (getComponentCount() > 0) {
                Component child = getComponent(0);
                min = child.getMinimumSize();
                pref = child.getPreferredSize();
                max = child.getMaximumSize();
                yalign = child.getAlignmentY();
                xalign = child.getAlignmentX();
            } else {
                min = pref = max = new Dimension(0, 0);
            }
        }
        public void setVisible(boolean b) {
            super.setVisible(b);
            if (getComponentCount() > 0) {
                getComponent(0).setVisible(b);
            }
        }
        public boolean isShowing() {
            return true;
        }
        public Dimension getMinimumSize() {
            validateIfNecessary();
            return min;
        }
        public Dimension getPreferredSize() {
            validateIfNecessary();
            return pref;
        }
        public Dimension getMaximumSize() {
            validateIfNecessary();
            return max;
        }
        public float getAlignmentX() {
            validateIfNecessary();
            return xalign;
        }
        public float getAlignmentY() {
            validateIfNecessary();
            return yalign;
        }
        public Set<AWTKeyStroke> getFocusTraversalKeys(int id) {
            return KeyboardFocusManager.getCurrentKeyboardFocusManager().
                    getDefaultFocusTraversalKeys(id);
        }
        public void propertyChange(PropertyChangeEvent ev) {
            Boolean enable = (Boolean) ev.getNewValue();
            if (getComponentCount() > 0) {
                getComponent(0).setEnabled(enable);
            }
        }
        Dimension min;
        Dimension pref;
        Dimension max;
        float yalign;
        float xalign;
    }
}
