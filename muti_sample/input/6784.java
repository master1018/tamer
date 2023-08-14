public class Box extends JComponent implements Accessible {
    public Box(int axis) {
        super();
        super.setLayout(new BoxLayout(this, axis));
    }
    public static Box createHorizontalBox() {
        return new Box(BoxLayout.X_AXIS);
    }
    public static Box createVerticalBox() {
        return new Box(BoxLayout.Y_AXIS);
    }
    public static Component createRigidArea(Dimension d) {
        return new Filler(d, d, d);
    }
    public static Component createHorizontalStrut(int width) {
        return new Filler(new Dimension(width,0), new Dimension(width,0),
                          new Dimension(width, Short.MAX_VALUE));
    }
    public static Component createVerticalStrut(int height) {
        return new Filler(new Dimension(0,height), new Dimension(0,height),
                          new Dimension(Short.MAX_VALUE, height));
    }
    public static Component createGlue() {
        return new Filler(new Dimension(0,0), new Dimension(0,0),
                          new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
    }
    public static Component createHorizontalGlue() {
        return new Filler(new Dimension(0,0), new Dimension(0,0),
                          new Dimension(Short.MAX_VALUE, 0));
    }
    public static Component createVerticalGlue() {
        return new Filler(new Dimension(0,0), new Dimension(0,0),
                          new Dimension(0, Short.MAX_VALUE));
    }
    public void setLayout(LayoutManager l) {
        throw new AWTError("Illegal request");
    }
    protected void paintComponent(Graphics g) {
        if (ui != null) {
            super.paintComponent(g);
        } else if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    public static class Filler extends JComponent implements Accessible {
        @ConstructorProperties({"minimumSize", "preferredSize", "maximumSize"})
        public Filler(Dimension min, Dimension pref, Dimension max) {
            setMinimumSize(min);
            setPreferredSize(pref);
            setMaximumSize(max);
        }
        public void changeShape(Dimension min, Dimension pref, Dimension max) {
            setMinimumSize(min);
            setPreferredSize(pref);
            setMaximumSize(max);
            revalidate();
        }
        protected void paintComponent(Graphics g) {
            if (ui != null) {
                super.paintComponent(g);
            } else if (isOpaque()) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleBoxFiller();
            }
            return accessibleContext;
        }
        protected class AccessibleBoxFiller extends AccessibleAWTComponent {
            public AccessibleRole getAccessibleRole() {
                return AccessibleRole.FILLER;
            }
        }
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleBox();
        }
        return accessibleContext;
    }
    protected class AccessibleBox extends AccessibleAWTContainer {
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.FILLER;
        }
    } 
}
