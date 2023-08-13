public abstract class SunDragSourceContextPeer implements DragSourceContextPeer {
    private DragGestureEvent  trigger;
    private Component         component;
    private Cursor            cursor;
    private Image             dragImage;
    private Point             dragImageOffset;
    private long              nativeCtxt;
    private DragSourceContext dragSourceContext;
    private int               sourceActions;
    private static boolean    dragDropInProgress = false;
    private static boolean    discardingMouseEvents = false;
    protected final static int DISPATCH_ENTER   = 1;
    protected final static int DISPATCH_MOTION  = 2;
    protected final static int DISPATCH_CHANGED = 3;
    protected final static int DISPATCH_EXIT    = 4;
    protected final static int DISPATCH_FINISH  = 5;
    protected final static int DISPATCH_MOUSE_MOVED  = 6;
    public SunDragSourceContextPeer(DragGestureEvent dge) {
        trigger = dge;
        if (trigger != null) {
            component = trigger.getComponent();
        } else {
            component = null;
        }
    }
    public void startSecondaryEventLoop(){}
    public void quitSecondaryEventLoop(){}
    public void startDrag(DragSourceContext dsc, Cursor c, Image di, Point p)
      throws InvalidDnDOperationException {
        if (getTrigger().getTriggerEvent() == null) {
            throw new InvalidDnDOperationException("DragGestureEvent has a null trigger");
        }
        dragSourceContext = dsc;
        cursor            = c;
        sourceActions     = getDragSourceContext().getSourceActions();
        dragImage         = di;
        dragImageOffset   = p;
        Transferable transferable  = getDragSourceContext().getTransferable();
        SortedMap formatMap = DataTransferer.getInstance().getFormatsForTransferable
             (transferable, DataTransferer.adaptFlavorMap
                 (getTrigger().getDragSource().getFlavorMap()));
        long[] formats = DataTransferer.getInstance().
            keysToLongArray(formatMap);
        startDrag(transferable, formats, formatMap);
        discardingMouseEvents = true;
        EventQueue.invokeLater(new Runnable() {
                public void run() {
                    discardingMouseEvents = false;
                }
            });
    }
    protected abstract void startDrag(Transferable trans,
                                      long[] formats, Map formatMap);
    public void setCursor(Cursor c) throws InvalidDnDOperationException {
        synchronized (this) {
            if (cursor == null || !cursor.equals(c)) {
                cursor = c;
                setNativeCursor(getNativeContext(), c,
                                c != null ? c.getType() : 0);
            }
        }
    }
    public Cursor getCursor() {
        return cursor;
    }
    public Image getDragImage() {
        return dragImage;
    }
    public Point getDragImageOffset() {
        if (dragImageOffset == null) {
            return new Point(0,0);
        }
        return new Point(dragImageOffset);
    }
    protected abstract void setNativeCursor(long nativeCtxt, Cursor c,
                                            int cType);
    protected synchronized void setTrigger(DragGestureEvent dge) {
        trigger = dge;
        if (trigger != null) {
            component = trigger.getComponent();
        } else {
            component = null;
        }
    }
    protected DragGestureEvent getTrigger() {
        return trigger;
    }
    protected Component getComponent() {
        return component;
    }
    protected synchronized void setNativeContext(long ctxt) {
        nativeCtxt = ctxt;
    }
    protected synchronized long getNativeContext() {
        return nativeCtxt;
    }
    protected DragSourceContext getDragSourceContext() {
        return dragSourceContext;
    }
    public void transferablesFlavorsChanged() {
    }
    protected final void postDragSourceDragEvent(final int targetAction,
                                                 final int modifiers,
                                                 final int x, final int y,
                                                 final int dispatchType) {
        final int dropAction =
            SunDragSourceContextPeer.convertModifiersToDropAction(modifiers,
                                                                  sourceActions);
        DragSourceDragEvent event =
            new DragSourceDragEvent(getDragSourceContext(),
                                    dropAction,
                                    targetAction & sourceActions,
                                    modifiers, x, y);
        EventDispatcher dispatcher = new EventDispatcher(dispatchType, event);
        SunToolkit.invokeLaterOnAppContext(
            SunToolkit.targetToAppContext(getComponent()), dispatcher);
        startSecondaryEventLoop();
    }
    private void dragEnter(final int targetActions,
                           final int modifiers,
                           final int x, final int y) {
        postDragSourceDragEvent(targetActions, modifiers, x, y, DISPATCH_ENTER);
    }
    private void dragMotion(final int targetActions,
                            final int modifiers,
                            final int x, final int y) {
        postDragSourceDragEvent(targetActions, modifiers, x, y, DISPATCH_MOTION);
    }
    private void operationChanged(final int targetActions,
                                  final int modifiers,
                                  final int x, final int y) {
        postDragSourceDragEvent(targetActions, modifiers, x, y, DISPATCH_CHANGED);
    }
    protected final void dragExit(final int x, final int y) {
        DragSourceEvent event =
            new DragSourceEvent(getDragSourceContext(), x, y);
        EventDispatcher dispatcher =
            new EventDispatcher(DISPATCH_EXIT, event);
        SunToolkit.invokeLaterOnAppContext(
            SunToolkit.targetToAppContext(getComponent()), dispatcher);
        startSecondaryEventLoop();
    }
    private void dragMouseMoved(final int targetActions,
                                final int modifiers,
                                final int x, final int y) {
        postDragSourceDragEvent(targetActions, modifiers, x, y,
                                DISPATCH_MOUSE_MOVED);
    }
    protected final void dragDropFinished(final boolean success,
                                          final int operations,
                                          final int x, final int y) {
        DragSourceEvent event =
            new DragSourceDropEvent(getDragSourceContext(),
                                    operations & sourceActions,
                                    success, x, y);
        EventDispatcher dispatcher =
            new EventDispatcher(DISPATCH_FINISH, event);
        SunToolkit.invokeLaterOnAppContext(
            SunToolkit.targetToAppContext(getComponent()), dispatcher);
        startSecondaryEventLoop();
        setNativeContext(0);
        dragImage = null;
        dragImageOffset = null;
    }
    public static void setDragDropInProgress(boolean b)
      throws InvalidDnDOperationException {
        if (dragDropInProgress == b) {
            throw new InvalidDnDOperationException(getExceptionMessage(b));
        }
        synchronized (SunDragSourceContextPeer.class) {
            if (dragDropInProgress == b) {
                throw new InvalidDnDOperationException(getExceptionMessage(b));
            }
            dragDropInProgress = b;
        }
    }
    public static boolean checkEvent(AWTEvent event) {
        if (discardingMouseEvents && event instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent)event;
            if (!(mouseEvent instanceof SunDropTargetEvent)) {
                return false;
            }
        }
        return true;
    }
    public static void checkDragDropInProgress()
      throws InvalidDnDOperationException {
        if (dragDropInProgress) {
            throw new InvalidDnDOperationException(getExceptionMessage(true));
        }
    }
    private static String getExceptionMessage(boolean b) {
        return b ? "Drag and drop in progress" : "No drag in progress";
    }
    public static int convertModifiersToDropAction(final int modifiers,
                                                   final int supportedActions) {
        int dropAction = DnDConstants.ACTION_NONE;
        switch (modifiers & (InputEvent.SHIFT_DOWN_MASK |
                             InputEvent.CTRL_DOWN_MASK)) {
        case InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK:
            dropAction = DnDConstants.ACTION_LINK; break;
        case InputEvent.CTRL_DOWN_MASK:
            dropAction = DnDConstants.ACTION_COPY; break;
        case InputEvent.SHIFT_DOWN_MASK:
            dropAction = DnDConstants.ACTION_MOVE; break;
        default:
            if ((supportedActions & DnDConstants.ACTION_MOVE) != 0) {
                dropAction = DnDConstants.ACTION_MOVE;
            } else if ((supportedActions & DnDConstants.ACTION_COPY) != 0) {
                dropAction = DnDConstants.ACTION_COPY;
            } else if ((supportedActions & DnDConstants.ACTION_LINK) != 0) {
                dropAction = DnDConstants.ACTION_LINK;
            }
        }
        return dropAction & supportedActions;
    }
    private void cleanup() {
        trigger = null;
        component = null;
        cursor = null;
        dragSourceContext = null;
        SunDropTargetContextPeer.setCurrentJVMLocalSourceTransferable(null);
        SunDragSourceContextPeer.setDragDropInProgress(false);
    }
    private class EventDispatcher implements Runnable {
        private final int dispatchType;
        private final DragSourceEvent event;
        EventDispatcher(int dispatchType, DragSourceEvent event) {
            switch (dispatchType) {
            case DISPATCH_ENTER:
            case DISPATCH_MOTION:
            case DISPATCH_CHANGED:
            case DISPATCH_MOUSE_MOVED:
                if (!(event instanceof DragSourceDragEvent)) {
                    throw new IllegalArgumentException("Event: " + event);
                }
                break;
            case DISPATCH_EXIT:
                break;
            case DISPATCH_FINISH:
                if (!(event instanceof DragSourceDropEvent)) {
                    throw new IllegalArgumentException("Event: " + event);
                }
                break;
            default:
                throw new IllegalArgumentException("Dispatch type: " +
                                                   dispatchType);
            }
            this.dispatchType  = dispatchType;
            this.event         = event;
        }
        public void run() {
            DragSourceContext dragSourceContext =
                SunDragSourceContextPeer.this.getDragSourceContext();
            try {
                switch (dispatchType) {
                case DISPATCH_ENTER:
                    dragSourceContext.dragEnter((DragSourceDragEvent)event);
                    break;
                case DISPATCH_MOTION:
                    dragSourceContext.dragOver((DragSourceDragEvent)event);
                    break;
                case DISPATCH_CHANGED:
                    dragSourceContext.dropActionChanged((DragSourceDragEvent)event);
                    break;
                case DISPATCH_EXIT:
                    dragSourceContext.dragExit(event);
                    break;
                case DISPATCH_MOUSE_MOVED:
                    dragSourceContext.dragMouseMoved((DragSourceDragEvent)event);
                    break;
                case DISPATCH_FINISH:
                    try {
                        dragSourceContext.dragDropEnd((DragSourceDropEvent)event);
                    } finally {
                        SunDragSourceContextPeer.this.cleanup();
                    }
                    break;
                default:
                    throw new IllegalStateException("Dispatch type: " +
                                                    dispatchType);
                }
            } finally {
                 SunDragSourceContextPeer.this.quitSecondaryEventLoop();
            }
        }
    }
}
