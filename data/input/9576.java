class Autoscroller implements ActionListener {
    private static Autoscroller sharedInstance = new Autoscroller();
    private static MouseEvent event;
    private static Timer timer;
    private static JComponent component;
    public static void stop(JComponent c) {
        sharedInstance._stop(c);
    }
    public static boolean isRunning(JComponent c) {
        return sharedInstance._isRunning(c);
    }
    public static void processMouseDragged(MouseEvent e) {
        sharedInstance._processMouseDragged(e);
    }
    Autoscroller() {
    }
    private void start(JComponent c, MouseEvent e) {
        Point screenLocation = c.getLocationOnScreen();
        if (component != c) {
            _stop(component);
        }
        component = c;
        event = new MouseEvent(component, e.getID(), e.getWhen(),
                               e.getModifiers(), e.getX() + screenLocation.x,
                               e.getY() + screenLocation.y,
                               e.getXOnScreen(),
                               e.getYOnScreen(),
                               e.getClickCount(), e.isPopupTrigger(),
                               MouseEvent.NOBUTTON);
        if (timer == null) {
            timer = new Timer(100, this);
        }
        if (!timer.isRunning()) {
            timer.start();
        }
    }
    private void _stop(JComponent c) {
        if (component == c) {
            if (timer != null) {
                timer.stop();
            }
            timer = null;
            event = null;
            component = null;
        }
    }
    private boolean _isRunning(JComponent c) {
        return (c == component && timer != null && timer.isRunning());
    }
    private void _processMouseDragged(MouseEvent e) {
        JComponent component = (JComponent)e.getComponent();
        boolean stop = true;
        if (component.isShowing()) {
            Rectangle visibleRect = component.getVisibleRect();
            stop = visibleRect.contains(e.getX(), e.getY());
        }
        if (stop) {
            _stop(component);
        } else {
            start(component, e);
        }
    }
    public void actionPerformed(ActionEvent x) {
        JComponent component = Autoscroller.component;
        if (component == null || !component.isShowing() || (event == null)) {
            _stop(component);
            return;
        }
        Point screenLocation = component.getLocationOnScreen();
        MouseEvent e = new MouseEvent(component, event.getID(),
                                      event.getWhen(), event.getModifiers(),
                                      event.getX() - screenLocation.x,
                                      event.getY() - screenLocation.y,
                                      event.getXOnScreen(),
                                      event.getYOnScreen(),
                                      event.getClickCount(),
                                      event.isPopupTrigger(),
                                      MouseEvent.NOBUTTON);
        component.superProcessMouseMotionEvent(e);
    }
}
