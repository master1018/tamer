public abstract class SunClipboard extends Clipboard
    implements PropertyChangeListener {
    public static final FlavorTable flavorMap =
        (FlavorTable)SystemFlavorMap.getDefaultFlavorMap();
    private AppContext contentsContext = null;
    private final Object CLIPBOARD_FLAVOR_LISTENER_KEY;
    private volatile int numberOfFlavorListeners = 0;
    private volatile Set currentDataFlavors;
    public SunClipboard(String name) {
        super(name);
        CLIPBOARD_FLAVOR_LISTENER_KEY = new StringBuffer(name + "_CLIPBOARD_FLAVOR_LISTENER_KEY");
    }
    public synchronized void setContents(Transferable contents,
                                         ClipboardOwner owner) {
        if (contents == null) {
            throw new NullPointerException("contents");
        }
        initContext();
        final ClipboardOwner oldOwner = this.owner;
        final Transferable oldContents = this.contents;
        try {
            this.owner = owner;
            this.contents = new TransferableProxy(contents, true);
            setContentsNative(contents);
        } finally {
            if (oldOwner != null && oldOwner != owner) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        oldOwner.lostOwnership(SunClipboard.this, oldContents);
                    }
                });
            }
        }
    }
    private synchronized void initContext() {
        final AppContext context = AppContext.getAppContext();
        if (contentsContext != context) {
            synchronized (context) {
                if (context.isDisposed()) {
                    throw new IllegalStateException("Can't set contents from disposed AppContext");
                }
                context.addPropertyChangeListener
                    (AppContext.DISPOSED_PROPERTY_NAME, this);
            }
            if (contentsContext != null) {
                contentsContext.removePropertyChangeListener
                    (AppContext.DISPOSED_PROPERTY_NAME, this);
            }
            contentsContext = context;
        }
    }
    public synchronized Transferable getContents(Object requestor) {
        if (contents != null) {
            return contents;
        }
        return new ClipboardTransferable(this);
    }
    private synchronized Transferable getContextContents() {
        AppContext context = AppContext.getAppContext();
        return (context == contentsContext) ? contents : null;
    }
    public DataFlavor[] getAvailableDataFlavors() {
        Transferable cntnts = getContextContents();
        if (cntnts != null) {
            return cntnts.getTransferDataFlavors();
        }
        long[] formats = getClipboardFormatsOpenClose();
        return DataTransferer.getInstance().
            getFlavorsForFormatsAsArray(formats, flavorMap);
    }
    public boolean isDataFlavorAvailable(DataFlavor flavor) {
        if (flavor == null) {
            throw new NullPointerException("flavor");
        }
        Transferable cntnts = getContextContents();
        if (cntnts != null) {
            return cntnts.isDataFlavorSupported(flavor);
        }
        long[] formats = getClipboardFormatsOpenClose();
        return formatArrayAsDataFlavorSet(formats).contains(flavor);
    }
    public Object getData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
        if (flavor == null) {
            throw new NullPointerException("flavor");
        }
        Transferable cntnts = getContextContents();
        if (cntnts != null) {
            return cntnts.getTransferData(flavor);
        }
        long format = 0;
        byte[] data = null;
        Transferable localeTransferable = null;
        try {
            openClipboard(null);
            long[] formats = getClipboardFormats();
            Long lFormat = (Long)DataTransferer.getInstance().
                    getFlavorsForFormats(formats, flavorMap).get(flavor);
            if (lFormat == null) {
                throw new UnsupportedFlavorException(flavor);
            }
            format = lFormat.longValue();
            data = getClipboardData(format);
            if (DataTransferer.getInstance().isLocaleDependentTextFormat(format)) {
                localeTransferable = createLocaleTransferable(formats);
            }
        } finally {
            closeClipboard();
        }
        return DataTransferer.getInstance().
                translateBytes(data, flavor, format, localeTransferable);
    }
    protected Transferable createLocaleTransferable(long[] formats) throws IOException {
        return null;
    }
    public void openClipboard(SunClipboard newOwner) {}
    public void closeClipboard() {}
    public abstract long getID();
    public void propertyChange(PropertyChangeEvent evt) {
        if (AppContext.DISPOSED_PROPERTY_NAME.equals(evt.getPropertyName()) &&
            Boolean.TRUE.equals(evt.getNewValue())) {
            final AppContext disposedContext = (AppContext)evt.getSource();
            lostOwnershipLater(disposedContext);
        }
    }
    protected void lostOwnershipImpl() {
        lostOwnershipLater(null);
    }
    protected void lostOwnershipLater(final AppContext disposedContext) {
        final AppContext context = this.contentsContext;
        if (context == null) {
            return;
        }
        final Runnable runnable = new Runnable() {
                public void run() {
                    final SunClipboard sunClipboard = SunClipboard.this;
                    ClipboardOwner owner = null;
                    Transferable contents = null;
                    synchronized (sunClipboard) {
                        final AppContext context = sunClipboard.contentsContext;
                        if (context == null) {
                            return;
                        }
                        if (disposedContext == null || context == disposedContext) {
                            owner = sunClipboard.owner;
                            contents = sunClipboard.contents;
                            sunClipboard.contentsContext = null;
                            sunClipboard.owner = null;
                            sunClipboard.contents = null;
                            sunClipboard.clearNativeContext();
                            context.removePropertyChangeListener
                                (AppContext.DISPOSED_PROPERTY_NAME, sunClipboard);
                        } else {
                            return;
                        }
                    }
                    if (owner != null) {
                        owner.lostOwnership(sunClipboard, contents);
                    }
                }
            };
        SunToolkit.postEvent(context, new PeerEvent(this, runnable,
                                                    PeerEvent.PRIORITY_EVENT));
    }
    protected abstract void clearNativeContext();
    protected abstract void setContentsNative(Transferable contents);
    protected long[] getClipboardFormatsOpenClose() {
        try {
            openClipboard(null);
            return getClipboardFormats();
        } finally {
            closeClipboard();
        }
    }
    protected abstract long[] getClipboardFormats();
    protected abstract byte[] getClipboardData(long format) throws IOException;
    private static Set formatArrayAsDataFlavorSet(long[] formats) {
        return (formats == null) ? null :
                DataTransferer.getInstance().
                getFlavorsForFormatsAsSet(formats, flavorMap);
    }
    public synchronized void addFlavorListener(FlavorListener listener) {
        if (listener == null) {
            return;
        }
        AppContext appContext = AppContext.getAppContext();
        EventListenerAggregate contextFlavorListeners = (EventListenerAggregate)
                appContext.get(CLIPBOARD_FLAVOR_LISTENER_KEY);
        if (contextFlavorListeners == null) {
            contextFlavorListeners = new EventListenerAggregate(FlavorListener.class);
            appContext.put(CLIPBOARD_FLAVOR_LISTENER_KEY, contextFlavorListeners);
        }
        contextFlavorListeners.add(listener);
        if (numberOfFlavorListeners++ == 0) {
            long[] currentFormats = null;
            try {
                openClipboard(null);
                currentFormats = getClipboardFormats();
            } catch (IllegalStateException exc) {
            } finally {
                closeClipboard();
            }
            currentDataFlavors = formatArrayAsDataFlavorSet(currentFormats);
            registerClipboardViewerChecked();
        }
    }
    public synchronized void removeFlavorListener(FlavorListener listener) {
        if (listener == null) {
            return;
        }
        AppContext appContext = AppContext.getAppContext();
        EventListenerAggregate contextFlavorListeners = (EventListenerAggregate)
                appContext.get(CLIPBOARD_FLAVOR_LISTENER_KEY);
        if (contextFlavorListeners == null){
            return;
        }
        if (contextFlavorListeners.remove(listener) &&
                --numberOfFlavorListeners == 0) {
            unregisterClipboardViewerChecked();
            currentDataFlavors = null;
        }
    }
    public synchronized FlavorListener[] getFlavorListeners() {
        EventListenerAggregate contextFlavorListeners = (EventListenerAggregate)
                AppContext.getAppContext().get(CLIPBOARD_FLAVOR_LISTENER_KEY);
        return contextFlavorListeners == null ? new FlavorListener[0] :
                (FlavorListener[])contextFlavorListeners.getListenersCopy();
    }
    public boolean areFlavorListenersRegistered() {
        return (numberOfFlavorListeners > 0);
    }
    protected abstract void registerClipboardViewerChecked();
    protected abstract void unregisterClipboardViewerChecked();
    public void checkChange(long[] formats) {
        Set prevDataFlavors = currentDataFlavors;
        currentDataFlavors = formatArrayAsDataFlavorSet(formats);
        if ((prevDataFlavors != null) && (currentDataFlavors != null) &&
                prevDataFlavors.equals(currentDataFlavors)) {
            return;
        }
        class SunFlavorChangeNotifier implements Runnable {
            private final FlavorListener flavorListener;
            SunFlavorChangeNotifier(FlavorListener flavorListener) {
                this.flavorListener = flavorListener;
            }
            public void run() {
                if (flavorListener != null) {
                    flavorListener.flavorsChanged(new FlavorEvent(SunClipboard.this));
                }
            }
        };
        for (Iterator it = AppContext.getAppContexts().iterator(); it.hasNext();) {
            AppContext appContext = (AppContext)it.next();
            if (appContext == null || appContext.isDisposed()) {
                continue;
            }
            EventListenerAggregate flavorListeners = (EventListenerAggregate)
                    appContext.get(CLIPBOARD_FLAVOR_LISTENER_KEY);
            if (flavorListeners != null) {
                FlavorListener[] flavorListenerArray =
                        (FlavorListener[])flavorListeners.getListenersInternal();
                for (int i = 0; i < flavorListenerArray.length; i++) {
                    SunToolkit.postEvent(appContext, new PeerEvent(this,
                            new SunFlavorChangeNotifier(flavorListenerArray[i]),
                            PeerEvent.PRIORITY_EVENT));
                }
            }
        }
    }
}
