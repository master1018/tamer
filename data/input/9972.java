class SynthArrowButton extends JButton implements SwingConstants, UIResource {
    private int direction;
    public SynthArrowButton(int direction) {
        super();
        super.setFocusable(false);
        setDirection(direction);
        setDefaultCapable(false);
    }
    public String getUIClassID() {
        return "ArrowButtonUI";
    }
    public void updateUI() {
        setUI(new SynthArrowButtonUI());
    }
    public void setDirection(int dir) {
        direction = dir;
        putClientProperty("__arrow_direction__", Integer.valueOf(dir));
        repaint();
    }
    public int getDirection() {
        return direction;
    }
    public void setFocusable(boolean focusable) {}
    private static class SynthArrowButtonUI extends SynthButtonUI {
        protected void installDefaults(AbstractButton b) {
            super.installDefaults(b);
            updateStyle(b);
        }
        protected void paint(SynthContext context, Graphics g) {
            SynthArrowButton button = (SynthArrowButton)context.
                                      getComponent();
            context.getPainter().paintArrowButtonForeground(
                context, g, 0, 0, button.getWidth(), button.getHeight(),
                button.getDirection());
        }
        void paintBackground(SynthContext context, Graphics g, JComponent c) {
            context.getPainter().paintArrowButtonBackground(context, g, 0, 0,
                                                c.getWidth(), c.getHeight());
        }
        public void paintBorder(SynthContext context, Graphics g, int x,
                                int y, int w, int h) {
            context.getPainter().paintArrowButtonBorder(context, g, x, y, w,h);
        }
        public Dimension getMinimumSize() {
            return new Dimension(5, 5);
        }
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
        public Dimension getPreferredSize(JComponent c) {
            SynthContext context = getContext(c);
            Dimension dim = null;
            if (context.getComponent().getName() == "ScrollBar.button") {
                dim = (Dimension)
                    context.getStyle().get(context, "ScrollBar.buttonSize");
            }
            if (dim == null) {
                int size =
                    context.getStyle().getInt(context, "ArrowButton.size", 16);
                dim = new Dimension(size, size);
            }
            Container parent = context.getComponent().getParent();
            if (parent instanceof JComponent && !(parent instanceof JComboBox)) {
                Object scaleKey = ((JComponent)parent).
                                    getClientProperty("JComponent.sizeVariant");
                if (scaleKey != null){
                    if ("large".equals(scaleKey)){
                        dim = new Dimension(
                                (int)(dim.width * 1.15),
                                (int)(dim.height * 1.15));
                    } else if ("small".equals(scaleKey)){
                        dim = new Dimension(
                                (int)(dim.width * 0.857),
                                (int)(dim.height * 0.857));
                    } else if ("mini".equals(scaleKey)){
                        dim = new Dimension(
                                (int)(dim.width * 0.714),
                                (int)(dim.height * 0.714));
                    }
                }
            }
            context.dispose();
            return dim;
        }
    }
}
