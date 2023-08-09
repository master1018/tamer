public final class XClipboard extends SunClipboard implements OwnershipListener
{
    private final XSelection selection;
    private long convertSelectionTime;
    private volatile boolean isSelectionNotifyProcessed;
    private volatile XAtom targetsPropertyAtom;
    private static final Object classLock = new Object();
    private static final int defaultPollInterval = 200;
    private static int pollInterval;
    private static Map<Long, XClipboard> targetsAtom2Clipboard;
    public XClipboard(String name, String selectionName) {
        super(name);
        selection = new XSelection(XAtom.get(selectionName));
        selection.registerOwershipListener(this);
    }
    public void ownershipChanged(final boolean isOwner) {
        if (isOwner) {
            checkChangeHere(contents);
        } else {
            lostOwnershipImpl();
        }
    }
    protected synchronized void setContentsNative(Transferable contents) {
        SortedMap formatMap = DataTransferer.getInstance().getFormatsForTransferable
                (contents, DataTransferer.adaptFlavorMap(flavorMap));
        long[] formats = DataTransferer.keysToLongArray(formatMap);
        if (!selection.setOwner(contents, formatMap, formats,
                                XToolkit.getCurrentServerTime())) {
            this.owner = null;
            this.contents = null;
        }
    }
    public long getID() {
        return selection.getSelectionAtom().getAtom();
    }
    @Override
    public synchronized Transferable getContents(Object requestor) {
        if (contents != null) {
            return contents;
        }
        return new ClipboardTransferable(this);
    }
    protected void clearNativeContext() {
        selection.reset();
    }
    protected long[] getClipboardFormats() {
        return selection.getTargets(XToolkit.getCurrentServerTime());
    }
    protected byte[] getClipboardData(long format) throws IOException {
        return selection.getData(format, XToolkit.getCurrentServerTime());
    }
    private void checkChangeHere(Transferable contents) {
        if (areFlavorListenersRegistered()) {
            checkChange(DataTransferer.getInstance().
                        getFormatsForTransferableAsArray(contents, flavorMap));
        }
    }
    private static int getPollInterval() {
        synchronized (XClipboard.classLock) {
            if (pollInterval <= 0) {
                pollInterval = AccessController.doPrivileged(
                        new GetIntegerAction("awt.datatransfer.clipboard.poll.interval",
                                             defaultPollInterval));
                if (pollInterval <= 0) {
                    pollInterval = defaultPollInterval;
                }
            }
            return pollInterval;
        }
    }
    private XAtom getTargetsPropertyAtom() {
        if (null == targetsPropertyAtom) {
            targetsPropertyAtom =
                    XAtom.get("XAWT_TARGETS_OF_SELECTION:" + selection.getSelectionAtom().getName());
        }
        return targetsPropertyAtom;
    }
    protected void registerClipboardViewerChecked() {
        isSelectionNotifyProcessed = true;
        boolean mustSchedule = false;
        synchronized (XClipboard.classLock) {
            if (targetsAtom2Clipboard == null) {
                targetsAtom2Clipboard = new HashMap<Long, XClipboard>(2);
            }
            mustSchedule = targetsAtom2Clipboard.isEmpty();
            targetsAtom2Clipboard.put(getTargetsPropertyAtom().getAtom(), this);
            if (mustSchedule) {
                XToolkit.addEventDispatcher(XWindow.getXAWTRootWindow().getWindow(),
                                            new SelectionNotifyHandler());
            }
        }
        if (mustSchedule) {
            XToolkit.schedule(new CheckChangeTimerTask(), XClipboard.getPollInterval());
        }
    }
    private static class CheckChangeTimerTask implements Runnable {
        public void run() {
            for (XClipboard clpbrd : targetsAtom2Clipboard.values()) {
                clpbrd.getTargetsDelayed();
            }
            synchronized (XClipboard.classLock) {
                if (targetsAtom2Clipboard != null && !targetsAtom2Clipboard.isEmpty()) {
                    XToolkit.schedule(this, XClipboard.getPollInterval());
                }
            }
        }
    }
    private static class SelectionNotifyHandler implements XEventDispatcher {
        public void dispatchEvent(XEvent ev) {
            if (ev.get_type() == XConstants.SelectionNotify) {
                final XSelectionEvent xse = ev.get_xselection();
                XClipboard clipboard = null;
                synchronized (XClipboard.classLock) {
                    if (targetsAtom2Clipboard != null && !targetsAtom2Clipboard.isEmpty()) {
                        XToolkit.removeEventDispatcher(XWindow.getXAWTRootWindow().getWindow(), this);
                        return;
                    }
                    final long propertyAtom = xse.get_property();
                    clipboard = targetsAtom2Clipboard.get(propertyAtom);
                }
                if (null != clipboard) {
                    clipboard.checkChange(xse);
                }
            }
        }
    }
    protected void unregisterClipboardViewerChecked() {
        isSelectionNotifyProcessed = false;
        synchronized (XClipboard.classLock) {
            targetsAtom2Clipboard.remove(getTargetsPropertyAtom().getAtom());
        }
    }
    private void getTargetsDelayed() {
        XToolkit.awtLock();
        try {
            long curTime = System.currentTimeMillis();
            if (isSelectionNotifyProcessed || curTime >= (convertSelectionTime + UNIXToolkit.getDatatransferTimeout()))
            {
                convertSelectionTime = curTime;
                XlibWrapper.XConvertSelection(XToolkit.getDisplay(),
                                              selection.getSelectionAtom().getAtom(),
                                              XDataTransferer.TARGETS_ATOM.getAtom(),
                                              getTargetsPropertyAtom().getAtom(),
                                              XWindow.getXAWTRootWindow().getWindow(),
                                              XConstants.CurrentTime);
                isSelectionNotifyProcessed = false;
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    private void checkChange(XSelectionEvent xse) {
        final long propertyAtom = xse.get_property();
        if (propertyAtom != getTargetsPropertyAtom().getAtom()) {
            return;
        }
        final XAtom selectionAtom = XAtom.get(xse.get_selection());
        final XSelection changedSelection = XSelection.getSelection(selectionAtom);
        if (null == changedSelection || changedSelection != selection) {
            return;
        }
        isSelectionNotifyProcessed = true;
        if (selection.isOwner()) {
            return;
        }
        long[] formats = null;
        if (propertyAtom == XConstants.None) {
            formats = new long[0];
        } else {
            WindowPropertyGetter targetsGetter =
                new WindowPropertyGetter(XWindow.getXAWTRootWindow().getWindow(),
                                         XAtom.get(propertyAtom), 0,
                                         XSelection.MAX_LENGTH, true,
                                         XConstants.AnyPropertyType);
            try {
                targetsGetter.execute();
                formats = XSelection.getFormats(targetsGetter);
            } finally {
                targetsGetter.dispose();
            }
        }
        checkChange(formats);
    }
}
