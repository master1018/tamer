class XMouseDragGestureRecognizer extends MouseDragGestureRecognizer {
    private static final long serialVersionUID = -841711780352520383L;
    protected static int motionThreshold;
    protected static final int ButtonMask = InputEvent.BUTTON1_DOWN_MASK |
                                            InputEvent.BUTTON2_DOWN_MASK |
                                            InputEvent.BUTTON3_DOWN_MASK;
    protected XMouseDragGestureRecognizer(DragSource ds, Component c, int act, DragGestureListener dgl) {
        super(ds, c, act, dgl);
    }
    protected XMouseDragGestureRecognizer(DragSource ds, Component c, int act) {
        this(ds, c, act, null);
    }
    protected XMouseDragGestureRecognizer(DragSource ds, Component c) {
        this(ds, c, DnDConstants.ACTION_NONE);
    }
    protected XMouseDragGestureRecognizer(DragSource ds) {
        this(ds, null);
    }
    protected int mapDragOperationFromModifiers(MouseEvent e) {
        int mods = e.getModifiersEx();
        int btns = mods & ButtonMask;
        if (!(btns == InputEvent.BUTTON1_DOWN_MASK ||
              btns == InputEvent.BUTTON2_DOWN_MASK)) {
            return DnDConstants.ACTION_NONE;
        }
        return
            SunDragSourceContextPeer.convertModifiersToDropAction(mods,
                                                                  getSourceActions());
    }
    public void mouseClicked(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
        events.clear();
        if (mapDragOperationFromModifiers(e) != DnDConstants.ACTION_NONE) {
            try {
                motionThreshold = DragSource.getDragThreshold();
            } catch (Exception exc) {
                motionThreshold = 5;
            }
            appendEvent(e);
        }
    }
    public void mouseReleased(MouseEvent e) {
        events.clear();
    }
    public void mouseEntered(MouseEvent e) {
        events.clear();
    }
    public void mouseExited(MouseEvent e) {
        if (!events.isEmpty()) { 
            int dragAction = mapDragOperationFromModifiers(e);
            if (dragAction == DnDConstants.ACTION_NONE) {
                events.clear();
            }
        }
    }
    public void mouseDragged(MouseEvent e) {
        if (!events.isEmpty()) { 
            int dop = mapDragOperationFromModifiers(e);
            if (dop == DnDConstants.ACTION_NONE) {
                return;
            }
            MouseEvent trigger = (MouseEvent)events.get(0);
            Point      origin  = trigger.getPoint();
            Point      current = e.getPoint();
            int        dx      = Math.abs(origin.x - current.x);
            int        dy      = Math.abs(origin.y - current.y);
            if (dx > motionThreshold || dy > motionThreshold) {
                fireDragGestureRecognized(dop, ((MouseEvent)getTriggerEvent()).getPoint());
            } else
                appendEvent(e);
        }
    }
    public void mouseMoved(MouseEvent e) {
    }
}
