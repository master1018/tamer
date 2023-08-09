public abstract class SunDropTargetContextPeer implements DropTargetContextPeer, Transferable {
    public static final boolean DISPATCH_SYNC = true;
    private   DropTarget              currentDT;
    private   DropTargetContext       currentDTC;
    private   long[]                  currentT;
    private   int                     currentA;   
    private   int                     currentSA;  
    private   int                     currentDA;  
    private   int                     previousDA;
    private   long                    nativeDragContext;
    private   Transferable            local;
    private boolean                   dragRejected = false;
    protected int                     dropStatus   = STATUS_NONE;
    protected boolean                 dropComplete = false;
    boolean                           dropInProcess = false;
    protected static final Object _globalLock = new Object();
    private static final PlatformLogger dndLog = PlatformLogger.getLogger("sun.awt.dnd.SunDropTargetContextPeer");
    protected static Transferable         currentJVMLocalSourceTransferable = null;
    public static void setCurrentJVMLocalSourceTransferable(Transferable t) throws InvalidDnDOperationException {
        synchronized(_globalLock) {
            if (t != null && currentJVMLocalSourceTransferable != null) {
                    throw new InvalidDnDOperationException();
            } else {
                currentJVMLocalSourceTransferable = t;
            }
        }
    }
    private static Transferable getJVMLocalSourceTransferable() {
        return currentJVMLocalSourceTransferable;
    }
    protected final static int STATUS_NONE   =  0; 
    protected final static int STATUS_WAIT   =  1; 
    protected final static int STATUS_ACCEPT =  2;
    protected final static int STATUS_REJECT = -1;
    public SunDropTargetContextPeer() {
        super();
    }
    public DropTarget getDropTarget() { return currentDT; }
    public synchronized void setTargetActions(int actions) {
        currentA = actions &
            (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK);
    }
    public int getTargetActions() {
        return currentA;
    }
    public Transferable getTransferable() {
        return this;
    }
    public DataFlavor[] getTransferDataFlavors() {
        final Transferable    localTransferable = local;
        if (localTransferable != null) {
            return localTransferable.getTransferDataFlavors();
        } else {
            return DataTransferer.getInstance().getFlavorsForFormatsAsArray
                (currentT, DataTransferer.adaptFlavorMap
                    (currentDT.getFlavorMap()));
        }
    }
    public boolean isDataFlavorSupported(DataFlavor df) {
        Transferable localTransferable = local;
        if (localTransferable != null) {
            return localTransferable.isDataFlavorSupported(df);
        } else {
            return DataTransferer.getInstance().getFlavorsForFormats
                (currentT, DataTransferer.adaptFlavorMap
                    (currentDT.getFlavorMap())).
                containsKey(df);
        }
    }
    public Object getTransferData(DataFlavor df)
      throws UnsupportedFlavorException, IOException,
        InvalidDnDOperationException
    {
        SecurityManager sm = System.getSecurityManager();
        try {
            if (!dropInProcess && sm != null) {
                sm.checkSystemClipboardAccess();
            }
        } catch (Exception e) {
            Thread currentThread = Thread.currentThread();
            currentThread.getUncaughtExceptionHandler().uncaughtException(currentThread, e);
            return null;
        }
        Long lFormat = null;
        Transferable localTransferable = local;
        if (localTransferable != null) {
            return localTransferable.getTransferData(df);
        }
        if (dropStatus != STATUS_ACCEPT || dropComplete) {
            throw new InvalidDnDOperationException("No drop current");
        }
        Map flavorMap = DataTransferer.getInstance().getFlavorsForFormats
            (currentT, DataTransferer.adaptFlavorMap
                (currentDT.getFlavorMap()));
        lFormat = (Long)flavorMap.get(df);
        if (lFormat == null) {
            throw new UnsupportedFlavorException(df);
        }
        if (df.isRepresentationClassRemote() &&
            currentDA != DnDConstants.ACTION_LINK) {
            throw new InvalidDnDOperationException("only ACTION_LINK is permissable for transfer of java.rmi.Remote objects");
        }
        final long format = lFormat.longValue();
        Object ret = getNativeData(format);
        if (ret instanceof byte[]) {
            try {
                return DataTransferer.getInstance().
                    translateBytes((byte[])ret, df, format, this);
            } catch (IOException e) {
                throw new InvalidDnDOperationException(e.getMessage());
            }
        } else if (ret instanceof InputStream) {
            try {
                return DataTransferer.getInstance().
                    translateStream((InputStream)ret, df, format, this);
            } catch (IOException e) {
                throw new InvalidDnDOperationException(e.getMessage());
            }
        } else {
            throw new IOException("no native data was transfered");
        }
    }
    protected abstract Object getNativeData(long format)
      throws IOException;
    public boolean isTransferableJVMLocal() {
        return local != null || getJVMLocalSourceTransferable() != null;
    }
    private int handleEnterMessage(final Component component,
                                   final int x, final int y,
                                   final int dropAction,
                                   final int actions, final long[] formats,
                                   final long nativeCtxt) {
        return postDropTargetEvent(component, x, y, dropAction, actions,
                                   formats, nativeCtxt,
                                   SunDropTargetEvent.MOUSE_ENTERED,
                                   SunDropTargetContextPeer.DISPATCH_SYNC);
    }
    protected void processEnterMessage(SunDropTargetEvent event) {
        Component  c    = (Component)event.getSource();
        DropTarget dt   = c.getDropTarget();
        Point      hots = event.getPoint();
        local = getJVMLocalSourceTransferable();
        if (currentDTC != null) { 
            currentDTC.removeNotify();
            currentDTC = null;
        }
        if (c.isShowing() && dt != null && dt.isActive()) {
            currentDT  = dt;
            currentDTC = currentDT.getDropTargetContext();
            currentDTC.addNotify(this);
            currentA   = dt.getDefaultActions();
            try {
                ((DropTargetListener)dt).dragEnter(new DropTargetDragEvent(currentDTC,
                                                                           hots,
                                                                           currentDA,
                                                                           currentSA));
            } catch (Exception e) {
                e.printStackTrace();
                currentDA = DnDConstants.ACTION_NONE;
            }
        } else {
            currentDT  = null;
            currentDTC = null;
            currentDA   = DnDConstants.ACTION_NONE;
            currentSA   = DnDConstants.ACTION_NONE;
            currentA   = DnDConstants.ACTION_NONE;
        }
    }
    private void handleExitMessage(final Component component,
                                   final long nativeCtxt) {
        postDropTargetEvent(component, 0, 0, DnDConstants.ACTION_NONE,
                            DnDConstants.ACTION_NONE, null, nativeCtxt,
                            SunDropTargetEvent.MOUSE_EXITED,
                            SunDropTargetContextPeer.DISPATCH_SYNC);
    }
    protected void processExitMessage(SunDropTargetEvent event) {
        Component         c   = (Component)event.getSource();
        DropTarget        dt  = c.getDropTarget();
        DropTargetContext dtc = null;
        if (dt == null) {
            currentDT = null;
            currentT  = null;
            if (currentDTC != null) {
                currentDTC.removeNotify();
            }
            currentDTC = null;
            return;
        }
        if (dt != currentDT) {
            if (currentDTC != null) {
                currentDTC.removeNotify();
            }
            currentDT  = dt;
            currentDTC = dt.getDropTargetContext();
            currentDTC.addNotify(this);
        }
        dtc = currentDTC;
        if (dt.isActive()) try {
            ((DropTargetListener)dt).dragExit(new DropTargetEvent(dtc));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            currentA  = DnDConstants.ACTION_NONE;
            currentSA = DnDConstants.ACTION_NONE;
            currentDA = DnDConstants.ACTION_NONE;
            currentDT = null;
            currentT  = null;
            currentDTC.removeNotify();
            currentDTC = null;
            local = null;
            dragRejected = false;
        }
    }
    private int handleMotionMessage(final Component component,
                                    final int x, final int y,
                                    final int dropAction,
                                    final int actions, final long[] formats,
                                    final long nativeCtxt) {
        return postDropTargetEvent(component, x, y, dropAction, actions,
                                   formats, nativeCtxt,
                                   SunDropTargetEvent.MOUSE_DRAGGED,
                                   SunDropTargetContextPeer.DISPATCH_SYNC);
    }
    protected void processMotionMessage(SunDropTargetEvent event,
                                      boolean operationChanged) {
        Component         c    = (Component)event.getSource();
        Point             hots = event.getPoint();
        int               id   = event.getID();
        DropTarget        dt   = c.getDropTarget();
        DropTargetContext dtc  = null;
        if (c.isShowing() && (dt != null) && dt.isActive()) {
            if (currentDT != dt) {
                if (currentDTC != null) {
                    currentDTC.removeNotify();
                }
                currentDT  = dt;
                currentDTC = null;
            }
            dtc = currentDT.getDropTargetContext();
            if (dtc != currentDTC) {
                if (currentDTC != null) {
                    currentDTC.removeNotify();
                }
                currentDTC = dtc;
                currentDTC.addNotify(this);
            }
            currentA = currentDT.getDefaultActions();
            try {
                DropTargetDragEvent dtde = new DropTargetDragEvent(dtc,
                                                                   hots,
                                                                   currentDA,
                                                                   currentSA);
                DropTargetListener dtl = (DropTargetListener)dt;
                if (operationChanged) {
                    dtl.dropActionChanged(dtde);
                } else {
                    dtl.dragOver(dtde);
                }
                if (dragRejected) {
                    currentDA = DnDConstants.ACTION_NONE;
                }
            } catch (Exception e) {
                e.printStackTrace();
                currentDA = DnDConstants.ACTION_NONE;
            }
        } else {
            currentDA = DnDConstants.ACTION_NONE;
        }
    }
    private void handleDropMessage(final Component component,
                                   final int x, final int y,
                                   final int dropAction, final int actions,
                                   final long[] formats,
                                   final long nativeCtxt) {
        postDropTargetEvent(component, x, y, dropAction, actions,
                            formats, nativeCtxt,
                            SunDropTargetEvent.MOUSE_DROPPED,
                            !SunDropTargetContextPeer.DISPATCH_SYNC);
    }
    protected void processDropMessage(SunDropTargetEvent event) {
        Component  c    = (Component)event.getSource();
        Point      hots = event.getPoint();
        DropTarget dt   = c.getDropTarget();
        dropStatus   = STATUS_WAIT; 
        dropComplete = false;
        if (c.isShowing() && dt != null && dt.isActive()) {
            DropTargetContext dtc = dt.getDropTargetContext();
            currentDT = dt;
            if (currentDTC != null) {
                currentDTC.removeNotify();
            }
            currentDTC = dtc;
            currentDTC.addNotify(this);
            currentA = dt.getDefaultActions();
            synchronized(_globalLock) {
                if ((local = getJVMLocalSourceTransferable()) != null)
                    setCurrentJVMLocalSourceTransferable(null);
            }
            dropInProcess = true;
            try {
                ((DropTargetListener)dt).drop(new DropTargetDropEvent(dtc,
                                                                      hots,
                                                                      currentDA,
                                                                      currentSA,
                                                                      local != null));
            } finally {
                if (dropStatus == STATUS_WAIT) {
                    rejectDrop();
                } else if (dropComplete == false) {
                    dropComplete(false);
                }
                dropInProcess = false;
            }
        } else {
            rejectDrop();
        }
    }
    protected int postDropTargetEvent(final Component component,
                                      final int x, final int y,
                                      final int dropAction,
                                      final int actions,
                                      final long[] formats,
                                      final long nativeCtxt,
                                      final int eventID,
                                      final boolean dispatchType) {
        AppContext appContext = SunToolkit.targetToAppContext(component);
        EventDispatcher dispatcher =
            new EventDispatcher(this, dropAction, actions, formats, nativeCtxt,
                                dispatchType);
        SunDropTargetEvent event =
            new SunDropTargetEvent(component, eventID, x, y, dispatcher);
        if (dispatchType == SunDropTargetContextPeer.DISPATCH_SYNC) {
            DataTransferer.getInstance().getToolkitThreadBlockedHandler().lock();
        }
        SunToolkit.postEvent(appContext, event);
        eventPosted(event);
        if (dispatchType == SunDropTargetContextPeer.DISPATCH_SYNC) {
            while (!dispatcher.isDone()) {
                DataTransferer.getInstance().getToolkitThreadBlockedHandler().enter();
            }
            DataTransferer.getInstance().getToolkitThreadBlockedHandler().unlock();
            return dispatcher.getReturnValue();
        } else {
            return 0;
        }
    }
    public synchronized void acceptDrag(int dragOperation) {
        if (currentDT == null) {
            throw new InvalidDnDOperationException("No Drag pending");
        }
        currentDA = mapOperation(dragOperation);
        if (currentDA != DnDConstants.ACTION_NONE) {
            dragRejected = false;
        }
    }
    public synchronized void rejectDrag() {
        if (currentDT == null) {
            throw new InvalidDnDOperationException("No Drag pending");
        }
        currentDA = DnDConstants.ACTION_NONE;
        dragRejected = true;
    }
    public synchronized void acceptDrop(int dropOperation) {
        if (dropOperation == DnDConstants.ACTION_NONE)
            throw new IllegalArgumentException("invalid acceptDrop() action");
        if (dropStatus != STATUS_WAIT) {
            throw new InvalidDnDOperationException("invalid acceptDrop()");
        }
        currentDA = currentA = mapOperation(dropOperation & currentSA);
        dropStatus   = STATUS_ACCEPT;
        dropComplete = false;
    }
    public synchronized void rejectDrop() {
        if (dropStatus != STATUS_WAIT) {
            throw new InvalidDnDOperationException("invalid rejectDrop()");
        }
        dropStatus = STATUS_REJECT;
        currentDA = DnDConstants.ACTION_NONE;
        dropComplete(false);
    }
    private int mapOperation(int operation) {
        int[] operations = {
                DnDConstants.ACTION_MOVE,
                DnDConstants.ACTION_COPY,
                DnDConstants.ACTION_LINK,
        };
        int   ret = DnDConstants.ACTION_NONE;
        for (int i = 0; i < operations.length; i++) {
            if ((operation & operations[i]) == operations[i]) {
                    ret = operations[i];
                    break;
            }
        }
        return ret;
    }
    public synchronized void dropComplete(boolean success) {
        if (dropStatus == STATUS_NONE) {
            throw new InvalidDnDOperationException("No Drop pending");
        }
        if (currentDTC != null) currentDTC.removeNotify();
        currentDT  = null;
        currentDTC = null;
        currentT   = null;
        currentA   = DnDConstants.ACTION_NONE;
        synchronized(_globalLock) {
            currentJVMLocalSourceTransferable = null;
        }
        dropStatus   = STATUS_NONE;
        dropComplete = true;
        try {
            doDropDone(success, currentDA, local != null);
        } finally {
            currentDA = DnDConstants.ACTION_NONE;
            nativeDragContext = 0;
        }
    }
    protected abstract void doDropDone(boolean success,
                                       int dropAction, boolean isLocal);
    protected synchronized long getNativeDragContext() {
        return nativeDragContext;
    }
    protected void eventPosted(SunDropTargetEvent e) {}
    protected void eventProcessed(SunDropTargetEvent e, int returnValue,
                                  boolean dispatcherDone) {}
    protected static class EventDispatcher {
        private final SunDropTargetContextPeer peer;
        private final int dropAction;
        private final int actions;
        private final long[] formats;
        private long nativeCtxt;
        private final boolean dispatchType;
        private boolean dispatcherDone = false;
        private int returnValue = 0;
        private final HashSet eventSet = new HashSet(3);
        static final ToolkitThreadBlockedHandler handler =
            DataTransferer.getInstance().getToolkitThreadBlockedHandler();
        EventDispatcher(SunDropTargetContextPeer peer,
                        int dropAction,
                        int actions,
                        long[] formats,
                        long nativeCtxt,
                        boolean dispatchType) {
            this.peer         = peer;
            this.nativeCtxt   = nativeCtxt;
            this.dropAction   = dropAction;
            this.actions      = actions;
            this.formats =
                     (null == formats) ? null : Arrays.copyOf(formats, formats.length);
            this.dispatchType = dispatchType;
        }
        void dispatchEvent(SunDropTargetEvent e) {
            int id = e.getID();
            switch (id) {
            case SunDropTargetEvent.MOUSE_ENTERED:
                dispatchEnterEvent(e);
                break;
            case SunDropTargetEvent.MOUSE_DRAGGED:
                dispatchMotionEvent(e);
                break;
            case SunDropTargetEvent.MOUSE_EXITED:
                dispatchExitEvent(e);
                break;
            case SunDropTargetEvent.MOUSE_DROPPED:
                dispatchDropEvent(e);
                break;
            default:
                throw new InvalidDnDOperationException();
            }
        }
        private void dispatchEnterEvent(SunDropTargetEvent e) {
            synchronized (peer) {
                peer.previousDA = dropAction;
                peer.nativeDragContext = nativeCtxt;
                peer.currentT          = formats;
                peer.currentSA         = actions;
                peer.currentDA         = dropAction;
                peer.dropStatus        = STATUS_ACCEPT;
                peer.dropComplete      = false;
                try {
                    peer.processEnterMessage(e);
                } finally {
                    peer.dropStatus        = STATUS_NONE;
                }
                setReturnValue(peer.currentDA);
            }
        }
        private void dispatchMotionEvent(SunDropTargetEvent e) {
            synchronized (peer) {
                boolean operationChanged = peer.previousDA != dropAction;
                peer.previousDA = dropAction;
                peer.nativeDragContext = nativeCtxt;
                peer.currentT          = formats;
                peer.currentSA         = actions;
                peer.currentDA         = dropAction;
                peer.dropStatus        = STATUS_ACCEPT;
                peer.dropComplete      = false;
                try {
                    peer.processMotionMessage(e, operationChanged);
                } finally {
                    peer.dropStatus        = STATUS_NONE;
                }
                setReturnValue(peer.currentDA);
            }
        }
        private void dispatchExitEvent(SunDropTargetEvent e) {
            synchronized (peer) {
                peer.nativeDragContext = nativeCtxt;
                peer.processExitMessage(e);
            }
        }
        private void dispatchDropEvent(SunDropTargetEvent e) {
            synchronized (peer) {
                peer.nativeDragContext = nativeCtxt;
                peer.currentT          = formats;
                peer.currentSA         = actions;
                peer.currentDA         = dropAction;
                peer.processDropMessage(e);
            }
        }
        void setReturnValue(int ret) {
            returnValue = ret;
        }
        int getReturnValue() {
            return returnValue;
        }
        boolean isDone() {
            return eventSet.isEmpty();
        }
        void registerEvent(SunDropTargetEvent e) {
            handler.lock();
            if (!eventSet.add(e) && dndLog.isLoggable(PlatformLogger.FINE)) {
                dndLog.fine("Event is already registered: " + e);
            }
            handler.unlock();
        }
        void unregisterEvent(SunDropTargetEvent e) {
            handler.lock();
            try {
                if (!eventSet.remove(e)) {
                    return;
                }
                if (eventSet.isEmpty()) {
                    if (!dispatcherDone && dispatchType == DISPATCH_SYNC) {
                        handler.exit();
                    }
                    dispatcherDone = true;
                }
            } finally {
                handler.unlock();
            }
            try {
                peer.eventProcessed(e, returnValue, dispatcherDone);
            } finally {
                if (dispatcherDone) {
                    nativeCtxt = 0;
                    peer.nativeDragContext = 0;
                }
            }
        }
        public void unregisterAllEvents() {
            Object[] events = null;
            handler.lock();
            try {
                events = eventSet.toArray();
            } finally {
                handler.unlock();
            }
            if (events != null) {
                for (int i = 0; i < events.length; i++) {
                    unregisterEvent((SunDropTargetEvent)events[i]);
                }
            }
        }
    }
}
