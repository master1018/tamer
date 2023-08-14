public class Popup {
    private Component component;
    protected Popup(Component owner, Component contents, int x, int y) {
        this();
        if (contents == null) {
            throw new IllegalArgumentException("Contents must be non-null");
        }
        reset(owner, contents, x, y);
    }
    protected Popup() {
    }
    public void show() {
        Component component = getComponent();
        if (component != null) {
            component.show();
        }
    }
    public void hide() {
        Component component = getComponent();
        if (component instanceof JWindow) {
            component.hide();
            ((JWindow)component).getContentPane().removeAll();
        }
        dispose();
    }
    void dispose() {
        Component component = getComponent();
        Window window = SwingUtilities.getWindowAncestor(component);
        if (component instanceof JWindow) {
            ((Window)component).dispose();
            component = null;
        }
        if (window instanceof DefaultFrame) {
            window.dispose();
        }
    }
    void reset(Component owner, Component contents, int ownerX, int ownerY) {
        if (getComponent() == null) {
            component = createComponent(owner);
        }
        Component c = getComponent();
        if (c instanceof JWindow) {
            JWindow component = (JWindow)getComponent();
            component.setLocation(ownerX, ownerY);
            component.getContentPane().add(contents, BorderLayout.CENTER);
            component.invalidate();
            component.validate();
            if(component.isVisible()) {
                pack();
            }
        }
    }
    void pack() {
        Component component = getComponent();
        if (component instanceof Window) {
            ((Window)component).pack();
        }
    }
    private Window getParentWindow(Component owner) {
        Window window = null;
        if (owner instanceof Window) {
            window = (Window)owner;
        }
        else if (owner != null) {
            window = SwingUtilities.getWindowAncestor(owner);
        }
        if (window == null) {
            window = new DefaultFrame();
        }
        return window;
    }
    Component createComponent(Component owner) {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        return new HeavyWeightWindow(getParentWindow(owner));
    }
    Component getComponent() {
        return component;
    }
    static class HeavyWeightWindow extends JWindow implements ModalExclude {
        HeavyWeightWindow(Window parent) {
            super(parent);
            setFocusableWindowState(false);
            setType(Window.Type.POPUP);
            getRootPane().setUseTrueDoubleBuffering(false);
            try {
                setAlwaysOnTop(true);
            } catch (SecurityException se) {
            }
        }
        public void update(Graphics g) {
            paint(g);
        }
        public void show() {
            this.pack();
            if (getWidth() > 0 && getHeight() > 0) {
                super.show();
            }
        }
    }
    static class DefaultFrame extends Frame {
    }
}
