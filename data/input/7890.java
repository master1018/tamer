public abstract class SunToolkit extends Toolkit
    implements WindowClosingSupport, WindowClosingListener,
    ComponentFactory, InputMethodSupport, KeyboardFocusManagerPeerProvider {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.SunToolkit");
    static {
        if (AccessController.doPrivileged(new GetBooleanAction("sun.awt.nativedebug"))) {
            DebugSettings.init();
        }
    };
    public static final int GRAB_EVENT_MASK = 0x80000000;
    private static Method  wakeupMethod;
    private static final String POST_EVENT_QUEUE_KEY = "PostEventQueue";
    protected static int numberOfButtons = 0;
    public final static int MAX_BUTTONS_SUPPORTED = 20;
    public SunToolkit() {
        Runnable initEQ = new Runnable() {
            public void run () {
                EventQueue eventQueue;
                String eqName = System.getProperty("AWT.EventQueueClass",
                                                   "java.awt.EventQueue");
                try {
                    eventQueue = (EventQueue)Class.forName(eqName).newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Failed loading " + eqName + ": " + e);
                    eventQueue = new EventQueue();
                }
                AppContext appContext = AppContext.getAppContext();
                appContext.put(AppContext.EVENT_QUEUE_KEY, eventQueue);
                PostEventQueue postEventQueue = new PostEventQueue(eventQueue);
                appContext.put(POST_EVENT_QUEUE_KEY, postEventQueue);
            }
        };
        initEQ.run();
    }
    public boolean useBufferPerWindow() {
        return false;
    }
    public abstract WindowPeer createWindow(Window target)
        throws HeadlessException;
    public abstract FramePeer createFrame(Frame target)
        throws HeadlessException;
    public abstract DialogPeer createDialog(Dialog target)
        throws HeadlessException;
    public abstract ButtonPeer createButton(Button target)
        throws HeadlessException;
    public abstract TextFieldPeer createTextField(TextField target)
        throws HeadlessException;
    public abstract ChoicePeer createChoice(Choice target)
        throws HeadlessException;
    public abstract LabelPeer createLabel(Label target)
        throws HeadlessException;
    public abstract ListPeer createList(java.awt.List target)
        throws HeadlessException;
    public abstract CheckboxPeer createCheckbox(Checkbox target)
        throws HeadlessException;
    public abstract ScrollbarPeer createScrollbar(Scrollbar target)
        throws HeadlessException;
    public abstract ScrollPanePeer createScrollPane(ScrollPane target)
        throws HeadlessException;
    public abstract TextAreaPeer createTextArea(TextArea target)
        throws HeadlessException;
    public abstract FileDialogPeer createFileDialog(FileDialog target)
        throws HeadlessException;
    public abstract MenuBarPeer createMenuBar(MenuBar target)
        throws HeadlessException;
    public abstract MenuPeer createMenu(Menu target)
        throws HeadlessException;
    public abstract PopupMenuPeer createPopupMenu(PopupMenu target)
        throws HeadlessException;
    public abstract MenuItemPeer createMenuItem(MenuItem target)
        throws HeadlessException;
    public abstract CheckboxMenuItemPeer createCheckboxMenuItem(
        CheckboxMenuItem target)
        throws HeadlessException;
    public abstract DragSourceContextPeer createDragSourceContextPeer(
        DragGestureEvent dge)
        throws InvalidDnDOperationException;
    public abstract TrayIconPeer createTrayIcon(TrayIcon target)
        throws HeadlessException, AWTException;
    public abstract SystemTrayPeer createSystemTray(SystemTray target);
    public abstract boolean isTraySupported();
    public abstract FontPeer getFontPeer(String name, int style);
    public abstract RobotPeer createRobot(Robot target, GraphicsDevice screen)
        throws AWTException;
    public abstract KeyboardFocusManagerPeer createKeyboardFocusManagerPeer(KeyboardFocusManager manager)
        throws HeadlessException;
    private static final ReentrantLock AWT_LOCK = new ReentrantLock();
    private static final Condition AWT_LOCK_COND = AWT_LOCK.newCondition();
    public static final void awtLock() {
        AWT_LOCK.lock();
    }
    public static final boolean awtTryLock() {
        return AWT_LOCK.tryLock();
    }
    public static final void awtUnlock() {
        AWT_LOCK.unlock();
    }
    public static final void awtLockWait()
        throws InterruptedException
    {
        AWT_LOCK_COND.await();
    }
    public static final void awtLockWait(long timeout)
        throws InterruptedException
    {
        AWT_LOCK_COND.await(timeout, TimeUnit.MILLISECONDS);
    }
    public static final void awtLockNotify() {
        AWT_LOCK_COND.signal();
    }
    public static final void awtLockNotifyAll() {
        AWT_LOCK_COND.signalAll();
    }
    public static final boolean isAWTLockHeldByCurrentThread() {
        return AWT_LOCK.isHeldByCurrentThread();
    }
    public static AppContext createNewAppContext() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        AppContext appContext = new AppContext(threadGroup);
        EventQueue eventQueue;
        String eqName = System.getProperty("AWT.EventQueueClass",
                                           "java.awt.EventQueue");
        try {
            eventQueue = (EventQueue)Class.forName(eqName).newInstance();
        } catch (Exception e) {
            System.err.println("Failed loading " + eqName + ": " + e);
            eventQueue = new EventQueue();
        }
        appContext.put(AppContext.EVENT_QUEUE_KEY, eventQueue);
        PostEventQueue postEventQueue = new PostEventQueue(eventQueue);
        appContext.put(POST_EVENT_QUEUE_KEY, postEventQueue);
        return appContext;
    }
    public static Field getField(final Class klass, final String fieldName) {
        return AccessController.doPrivileged(new PrivilegedAction<Field>() {
            public Field run() {
                try {
                    Field field = klass.getDeclaredField(fieldName);
                    assert (field != null);
                    field.setAccessible(true);
                    return field;
                } catch (SecurityException e) {
                    assert false;
                } catch (NoSuchFieldException e) {
                    assert false;
                }
                return null;
            }
        });
    }
    static void wakeupEventQueue(EventQueue q, boolean isShutdown){
        if (wakeupMethod == null){
            wakeupMethod = (Method)AccessController.doPrivileged(new PrivilegedAction(){
                    public Object run(){
                        try {
                            Method method  = EventQueue.class.getDeclaredMethod("wakeup",new Class [] {Boolean.TYPE} );
                            if (method != null) {
                                method.setAccessible(true);
                            }
                            return method;
                        } catch (NoSuchMethodException e) {
                            assert false;
                        } catch (SecurityException e) {
                            assert false;
                        }
                        return null;
                    }
                });
        }
        try{
            if (wakeupMethod != null){
                wakeupMethod.invoke(q, new Object[]{Boolean.valueOf(isShutdown)});
            }
        } catch (InvocationTargetException e){
            assert false;
        } catch (IllegalAccessException e) {
            assert false;
        }
    }
    protected static Object targetToPeer(Object target) {
        if (target != null && !GraphicsEnvironment.isHeadless()) {
            return AWTAutoShutdown.getInstance().getPeer(target);
        }
        return null;
    }
    protected static void targetCreatedPeer(Object target, Object peer) {
        if (target != null && peer != null &&
            !GraphicsEnvironment.isHeadless())
        {
            AWTAutoShutdown.getInstance().registerPeer(target, peer);
        }
    }
    protected static void targetDisposedPeer(Object target, Object peer) {
        if (target != null && peer != null &&
            !GraphicsEnvironment.isHeadless())
        {
            AWTAutoShutdown.getInstance().unregisterPeer(target, peer);
        }
    }
    private static final Map appContextMap =
        Collections.synchronizedMap(new WeakHashMap());
    private static boolean setAppContext(Object target,
                                         AppContext context) {
        if (target instanceof Component) {
            AWTAccessor.getComponentAccessor().
                setAppContext((Component)target, context);
        } else if (target instanceof MenuComponent) {
            AWTAccessor.getMenuComponentAccessor().
                setAppContext((MenuComponent)target, context);
        } else {
            return false;
        }
        return true;
    }
    private static AppContext getAppContext(Object target) {
        if (target instanceof Component) {
            return AWTAccessor.getComponentAccessor().
                       getAppContext((Component)target);
        } else if (target instanceof MenuComponent) {
            return AWTAccessor.getMenuComponentAccessor().
                       getAppContext((MenuComponent)target);
        } else {
            return null;
        }
    }
    public static AppContext targetToAppContext(Object target) {
        if (target == null || GraphicsEnvironment.isHeadless()) {
            return null;
        }
        AppContext context = getAppContext(target);
        if (context == null) {
            context = (AppContext)appContextMap.get(target);
        }
        return context;
    }
    public static void setLWRequestStatus(Window changed,boolean status){
        AWTAccessor.getWindowAccessor().setLWRequestStatus(changed, status);
    };
    public static void checkAndSetPolicy(Container cont, boolean isSwingCont)
    {
        FocusTraversalPolicy defaultPolicy = KeyboardFocusManager
            .getCurrentKeyboardFocusManager().getDefaultFocusTraversalPolicy();
        String toolkitName = Toolkit.getDefaultToolkit().getClass().getName();
        if (!"sun.awt.X11.XToolkit".equals(toolkitName)) {
            cont.setFocusTraversalPolicy(defaultPolicy);
            return;
        }
        String policyName = defaultPolicy.getClass().getName();
        if (DefaultFocusTraversalPolicy.class != defaultPolicy.getClass()) {
            if (policyName.startsWith("java.awt.")) {
                if (isSwingCont) {
                    defaultPolicy = createLayoutPolicy();
                } else {
                }
            } else if (policyName.startsWith("javax.swing.")) {
                if (isSwingCont) {
                } else {
                    defaultPolicy = new DefaultFocusTraversalPolicy();
                }
            }
        } else {
            if (isSwingCont) {
                defaultPolicy = createLayoutPolicy();
            }
        }
        cont.setFocusTraversalPolicy(defaultPolicy);
    }
    private static FocusTraversalPolicy createLayoutPolicy() {
        FocusTraversalPolicy policy = null;
        try {
            Class layoutPolicyClass =
                Class.forName("javax.swing.LayoutFocusTraversalPolicy");
            policy = (FocusTraversalPolicy) layoutPolicyClass.newInstance();
        }
        catch (ClassNotFoundException e) {
            assert false;
        }
        catch (InstantiationException e) {
            assert false;
        }
        catch (IllegalAccessException e) {
            assert false;
        }
        return policy;
    }
    public static void insertTargetMapping(Object target, AppContext appContext) {
        if (!GraphicsEnvironment.isHeadless()) {
            if (!setAppContext(target, appContext)) {
                appContextMap.put(target, appContext);
            }
        }
    }
    public static void postEvent(AppContext appContext, AWTEvent event) {
        if (event == null) {
            throw new NullPointerException();
        }
        setSystemGenerated(event);
        AppContext eventContext = targetToAppContext(event.getSource());
        if (eventContext != null && !eventContext.equals(appContext)) {
            log.fine("Event posted on wrong app context : " + event);
        }
        PostEventQueue postEventQueue =
            (PostEventQueue)appContext.get(POST_EVENT_QUEUE_KEY);
        if (postEventQueue != null) {
            postEventQueue.postEvent(event);
        }
    }
    public static void postPriorityEvent(final AWTEvent e) {
        PeerEvent pe = new PeerEvent(Toolkit.getDefaultToolkit(), new Runnable() {
                public void run() {
                    AWTAccessor.getAWTEventAccessor().setPosted(e);
                    ((Component)e.getSource()).dispatchEvent(e);
                }
            }, PeerEvent.ULTIMATE_PRIORITY_EVENT);
        postEvent(targetToAppContext(e.getSource()), pe);
    }
    private static final Lock flushLock = new ReentrantLock();
    private static boolean isFlushingPendingEvents = false;
    public static void flushPendingEvents()  {
        flushLock.lock();
        try {
            if (!isFlushingPendingEvents) {
                isFlushingPendingEvents = true;
                AppContext appContext = AppContext.getAppContext();
                PostEventQueue postEventQueue =
                    (PostEventQueue)appContext.get(POST_EVENT_QUEUE_KEY);
                if (postEventQueue != null) {
                    postEventQueue.flush();
                }
            }
        } finally {
            isFlushingPendingEvents = false;
            flushLock.unlock();
        }
    }
    public static boolean isPostEventQueueEmpty()  {
        AppContext appContext = AppContext.getAppContext();
        PostEventQueue postEventQueue =
            (PostEventQueue)appContext.get(POST_EVENT_QUEUE_KEY);
        if (postEventQueue != null) {
            return postEventQueue.noEvents();
        } else {
            return true;
        }
    }
    public static void executeOnEventHandlerThread(Object target,
                                                   Runnable runnable) {
        executeOnEventHandlerThread(new PeerEvent(target, runnable, PeerEvent.PRIORITY_EVENT));
    }
    public static void executeOnEventHandlerThread(Object target,
                                                   Runnable runnable,
                                                   final long when) {
        executeOnEventHandlerThread(new PeerEvent(target, runnable, PeerEvent.PRIORITY_EVENT){
                public long getWhen(){
                    return when;
                }
            });
    }
    public static void executeOnEventHandlerThread(PeerEvent peerEvent) {
        postEvent(targetToAppContext(peerEvent.getSource()), peerEvent);
    }
     public static void invokeLaterOnAppContext(
        AppContext appContext, Runnable dispatcher)
     {
        postEvent(appContext,
            new PeerEvent(Toolkit.getDefaultToolkit(), dispatcher,
                PeerEvent.PRIORITY_EVENT));
     }
    public static void executeOnEDTAndWait(Object target, Runnable runnable)
        throws InterruptedException, InvocationTargetException
    {
        if (EventQueue.isDispatchThread()) {
            throw new Error("Cannot call executeOnEDTAndWait from any event dispatcher thread");
        }
        class AWTInvocationLock {}
        Object lock = new AWTInvocationLock();
        PeerEvent event = new PeerEvent(target, runnable, lock, true, PeerEvent.PRIORITY_EVENT);
        synchronized (lock) {
            executeOnEventHandlerThread(event);
            while(!event.isDispatched()) {
                lock.wait();
            }
        }
        Throwable eventThrowable = event.getThrowable();
        if (eventThrowable != null) {
            throw new InvocationTargetException(eventThrowable);
        }
    }
    public static boolean isDispatchThreadForAppContext(Object target) {
        AppContext appContext = targetToAppContext(target);
        EventQueue eq = (EventQueue)appContext.get(AppContext.EVENT_QUEUE_KEY);
        AWTAccessor.EventQueueAccessor accessor = AWTAccessor.getEventQueueAccessor();
        return accessor.isDispatchThreadImpl(eq);
    }
    public Dimension getScreenSize() {
        return new Dimension(getScreenWidth(), getScreenHeight());
    }
    protected abstract int getScreenWidth();
    protected abstract int getScreenHeight();
    public FontMetrics getFontMetrics(Font font) {
        return FontDesignMetrics.getMetrics(font);
    }
    public String[] getFontList() {
        String[] hardwiredFontList = {
            Font.DIALOG, Font.SANS_SERIF, Font.SERIF, Font.MONOSPACED,
            Font.DIALOG_INPUT
        };
        return hardwiredFontList;
    }
    public PanelPeer createPanel(Panel target) {
        return (PanelPeer)createComponent(target);
    }
    public CanvasPeer createCanvas(Canvas target) {
        return (CanvasPeer)createComponent(target);
    }
    public void disableBackgroundErase(Canvas canvas) {
        disableBackgroundEraseImpl(canvas);
    }
    public void disableBackgroundErase(Component component) {
        disableBackgroundEraseImpl(component);
    }
    private void disableBackgroundEraseImpl(Component component) {
        AWTAccessor.getComponentAccessor().setBackgroundEraseDisabled(component, true);
    }
    public static boolean getSunAwtNoerasebackground() {
        return AccessController.doPrivileged(new GetBooleanAction("sun.awt.noerasebackground"));
    }
    public static boolean getSunAwtErasebackgroundonresize() {
        return AccessController.doPrivileged(new GetBooleanAction("sun.awt.erasebackgroundonresize"));
    }
    static final SoftCache imgCache = new SoftCache();
    static Image getImageFromHash(Toolkit tk, URL url) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                java.security.Permission perm =
                    url.openConnection().getPermission();
                if (perm != null) {
                    try {
                        sm.checkPermission(perm);
                    } catch (SecurityException se) {
                        if ((perm instanceof java.io.FilePermission) &&
                            perm.getActions().indexOf("read") != -1) {
                            sm.checkRead(perm.getName());
                        } else if ((perm instanceof
                            java.net.SocketPermission) &&
                            perm.getActions().indexOf("connect") != -1) {
                            sm.checkConnect(url.getHost(), url.getPort());
                        } else {
                            throw se;
                        }
                    }
                }
            } catch (java.io.IOException ioe) {
                    sm.checkConnect(url.getHost(), url.getPort());
            }
        }
        synchronized (imgCache) {
            Image img = (Image)imgCache.get(url);
            if (img == null) {
                try {
                    img = tk.createImage(new URLImageSource(url));
                    imgCache.put(url, img);
                } catch (Exception e) {
                }
            }
            return img;
        }
    }
    static Image getImageFromHash(Toolkit tk,
                                               String filename) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(filename);
        }
        synchronized (imgCache) {
            Image img = (Image)imgCache.get(filename);
            if (img == null) {
                try {
                    img = tk.createImage(new FileImageSource(filename));
                    imgCache.put(filename, img);
                } catch (Exception e) {
                }
            }
            return img;
        }
    }
    public Image getImage(String filename) {
        return getImageFromHash(this, filename);
    }
    public Image getImage(URL url) {
        return getImageFromHash(this, url);
    }
    public Image createImage(String filename) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(filename);
        }
        return createImage(new FileImageSource(filename));
    }
    public Image createImage(URL url) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                java.security.Permission perm =
                    url.openConnection().getPermission();
                if (perm != null) {
                    try {
                        sm.checkPermission(perm);
                    } catch (SecurityException se) {
                        if ((perm instanceof java.io.FilePermission) &&
                            perm.getActions().indexOf("read") != -1) {
                            sm.checkRead(perm.getName());
                        } else if ((perm instanceof
                            java.net.SocketPermission) &&
                            perm.getActions().indexOf("connect") != -1) {
                            sm.checkConnect(url.getHost(), url.getPort());
                        } else {
                            throw se;
                        }
                    }
                }
            } catch (java.io.IOException ioe) {
                    sm.checkConnect(url.getHost(), url.getPort());
            }
        }
        return createImage(new URLImageSource(url));
    }
    public Image createImage(byte[] data, int offset, int length) {
        return createImage(new ByteArrayImageSource(data, offset, length));
    }
    public Image createImage(ImageProducer producer) {
        return new ToolkitImage(producer);
    }
    public int checkImage(Image img, int w, int h, ImageObserver o) {
        if (!(img instanceof ToolkitImage)) {
            return ImageObserver.ALLBITS;
        }
        ToolkitImage tkimg = (ToolkitImage)img;
        int repbits;
        if (w == 0 || h == 0) {
            repbits = ImageObserver.ALLBITS;
        } else {
            repbits = tkimg.getImageRep().check(o);
        }
        return tkimg.check(o) | repbits;
    }
    public boolean prepareImage(Image img, int w, int h, ImageObserver o) {
        if (w == 0 || h == 0) {
            return true;
        }
        if (!(img instanceof ToolkitImage)) {
            return true;
        }
        ToolkitImage tkimg = (ToolkitImage)img;
        if (tkimg.hasError()) {
            if (o != null) {
                o.imageUpdate(img, ImageObserver.ERROR|ImageObserver.ABORT,
                              -1, -1, -1, -1);
            }
            return false;
        }
        ImageRepresentation ir = tkimg.getImageRep();
        return ir.prepare(o);
    }
    public static BufferedImage getScaledIconImage(java.util.List<Image> imageList, int width, int height) {
        if (width == 0 || height == 0) {
            return null;
        }
        Image bestImage = null;
        int bestWidth = 0;
        int bestHeight = 0;
        double bestSimilarity = 3; 
        double bestScaleFactor = 0;
        for (Iterator<Image> i = imageList.iterator();i.hasNext();) {
            Image im = i.next();
            if (im == null) {
                if (log.isLoggable(PlatformLogger.FINER)) {
                    log.finer("SunToolkit.getScaledIconImage: " +
                              "Skipping the image passed into Java because it's null.");
                }
                continue;
            }
            if (im instanceof ToolkitImage) {
                ImageRepresentation ir = ((ToolkitImage)im).getImageRep();
                ir.reconstruct(ImageObserver.ALLBITS);
            }
            int iw;
            int ih;
            try {
                iw = im.getWidth(null);
                ih = im.getHeight(null);
            } catch (Exception e){
                if (log.isLoggable(PlatformLogger.FINER)) {
                    log.finer("SunToolkit.getScaledIconImage: " +
                              "Perhaps the image passed into Java is broken. Skipping this icon.");
                }
                continue;
            }
            if (iw > 0 && ih > 0) {
                double scaleFactor = Math.min((double)width / (double)iw,
                                              (double)height / (double)ih);
                int adjw = 0;
                int adjh = 0;
                double scaleMeasure = 1; 
                if (scaleFactor >= 2) {
                    scaleFactor = Math.floor(scaleFactor);
                    adjw = iw * (int)scaleFactor;
                    adjh = ih * (int)scaleFactor;
                    scaleMeasure = 1.0 - 0.5 / scaleFactor;
                } else if (scaleFactor >= 1) {
                    scaleFactor = 1.0;
                    adjw = iw;
                    adjh = ih;
                    scaleMeasure = 0;
                } else if (scaleFactor >= 0.75) {
                    scaleFactor = 0.75;
                    adjw = iw * 3 / 4;
                    adjh = ih * 3 / 4;
                    scaleMeasure = 0.3;
                } else if (scaleFactor >= 0.6666) {
                    scaleFactor = 0.6666;
                    adjw = iw * 2 / 3;
                    adjh = ih * 2 / 3;
                    scaleMeasure = 0.33;
                } else {
                    double scaleDivider = Math.ceil(1.0 / scaleFactor);
                    scaleFactor = 1.0 / scaleDivider;
                    adjw = (int)Math.round((double)iw / scaleDivider);
                    adjh = (int)Math.round((double)ih / scaleDivider);
                    scaleMeasure = 1.0 - 1.0 / scaleDivider;
                }
                double similarity = ((double)width - (double)adjw) / (double)width +
                    ((double)height - (double)adjh) / (double)height + 
                    scaleMeasure; 
                if (similarity < bestSimilarity) {
                    bestSimilarity = similarity;
                    bestScaleFactor = scaleFactor;
                    bestImage = im;
                    bestWidth = adjw;
                    bestHeight = adjh;
                }
                if (similarity == 0) break;
            }
        }
        if (bestImage == null) {
            return null;
        }
        BufferedImage bimage =
            new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bimage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        try {
            int x = (width - bestWidth) / 2;
            int y = (height - bestHeight) / 2;
            if (log.isLoggable(PlatformLogger.FINER)) {
                log.finer("WWindowPeer.getScaledIconData() result : " +
                        "w : " + width + " h : " + height +
                        " iW : " + bestImage.getWidth(null) + " iH : " + bestImage.getHeight(null) +
                        " sim : " + bestSimilarity + " sf : " + bestScaleFactor +
                        " adjW : " + bestWidth + " adjH : " + bestHeight +
                        " x : " + x + " y : " + y);
            }
            g.drawImage(bestImage, x, y, bestWidth, bestHeight, null);
        } finally {
            g.dispose();
        }
        return bimage;
    }
    public static DataBufferInt getScaledIconData(java.util.List<Image> imageList, int width, int height) {
        BufferedImage bimage = getScaledIconImage(imageList, width, height);
        if (bimage == null) {
             if (log.isLoggable(PlatformLogger.FINER)) {
                 log.finer("SunToolkit.getScaledIconData: " +
                           "Perhaps the image passed into Java is broken. Skipping this icon.");
             }
            return null;
        }
        Raster raster = bimage.getRaster();
        DataBuffer buffer = raster.getDataBuffer();
        return (DataBufferInt)buffer;
    }
    protected EventQueue getSystemEventQueueImpl() {
        return getSystemEventQueueImplPP();
    }
    static EventQueue getSystemEventQueueImplPP() {
        return getSystemEventQueueImplPP(AppContext.getAppContext());
    }
    public static EventQueue getSystemEventQueueImplPP(AppContext appContext) {
        EventQueue theEventQueue =
            (EventQueue)appContext.get(AppContext.EVENT_QUEUE_KEY);
        return theEventQueue;
    }
    public static Container getNativeContainer(Component c) {
        return Toolkit.getNativeContainer(c);
    }
    public static Component getHeavyweightComponent(Component c) {
        while (c != null && AWTAccessor.getComponentAccessor().isLightweight(c)) {
            c = AWTAccessor.getComponentAccessor().getParent(c);
        }
        return c;
    }
    public Window createInputMethodWindow(String title, InputContext context) {
        return new sun.awt.im.SimpleInputMethodWindow(title, context);
    }
    public boolean enableInputMethodsForTextComponent() {
        return false;
    }
    private static Locale startupLocale = null;
    public static Locale getStartupLocale() {
        if (startupLocale == null) {
            String language, region, country, variant;
            language = (String) AccessController.doPrivileged(
                            new GetPropertyAction("user.language", "en"));
            region = (String) AccessController.doPrivileged(
                            new GetPropertyAction("user.region"));
            if (region != null) {
                int i = region.indexOf('_');
                if (i >= 0) {
                    country = region.substring(0, i);
                    variant = region.substring(i + 1);
                } else {
                    country = region;
                    variant = "";
                }
            } else {
                country = (String) AccessController.doPrivileged(
                                new GetPropertyAction("user.country", ""));
                variant = (String) AccessController.doPrivileged(
                                new GetPropertyAction("user.variant", ""));
            }
            startupLocale = new Locale(language, country, variant);
        }
        return startupLocale;
    }
    public Locale getDefaultKeyboardLocale() {
        return getStartupLocale();
    }
    private static String dataTransfererClassName = null;
    protected static void setDataTransfererClassName(String className) {
        dataTransfererClassName = className;
    }
    public static String getDataTransfererClassName() {
        if (dataTransfererClassName == null) {
            Toolkit.getDefaultToolkit(); 
        }
        return dataTransfererClassName;
    }
    private transient WindowClosingListener windowClosingListener = null;
    public WindowClosingListener getWindowClosingListener() {
        return windowClosingListener;
    }
    public void setWindowClosingListener(WindowClosingListener wcl) {
        windowClosingListener = wcl;
    }
    public RuntimeException windowClosingNotify(WindowEvent event) {
        if (windowClosingListener != null) {
            return windowClosingListener.windowClosingNotify(event);
        } else {
            return null;
        }
    }
    public RuntimeException windowClosingDelivered(WindowEvent event) {
        if (windowClosingListener != null) {
            return windowClosingListener.windowClosingDelivered(event);
        } else {
            return null;
        }
    }
    private static DefaultMouseInfoPeer mPeer = null;
    protected synchronized MouseInfoPeer getMouseInfoPeer() {
        if (mPeer == null) {
            mPeer = new DefaultMouseInfoPeer();
        }
        return mPeer;
    }
    public static boolean needsXEmbed() {
        String noxembed = (String) AccessController.
            doPrivileged(new GetPropertyAction("sun.awt.noxembed", "false"));
        if ("true".equals(noxembed)) {
            return false;
        }
        Toolkit tk = Toolkit.getDefaultToolkit();
        if (tk instanceof SunToolkit) {
            return ((SunToolkit)tk).needsXEmbedImpl();
        } else {
            return false;
        }
    }
    protected boolean needsXEmbedImpl() {
        return false;
    }
    private static Dialog.ModalExclusionType DEFAULT_MODAL_EXCLUSION_TYPE = null;
    protected final boolean isXEmbedServerRequested() {
        return AccessController.doPrivileged(new GetBooleanAction("sun.awt.xembedserver"));
    }
    public static boolean isModalExcludedSupported()
    {
        Toolkit tk = Toolkit.getDefaultToolkit();
        return tk.isModalExclusionTypeSupported(DEFAULT_MODAL_EXCLUSION_TYPE);
    }
    protected boolean isModalExcludedSupportedImpl()
    {
        return false;
    }
    public static void setModalExcluded(Window window)
    {
        if (DEFAULT_MODAL_EXCLUSION_TYPE == null) {
            DEFAULT_MODAL_EXCLUSION_TYPE = Dialog.ModalExclusionType.APPLICATION_EXCLUDE;
        }
        window.setModalExclusionType(DEFAULT_MODAL_EXCLUSION_TYPE);
    }
    public static boolean isModalExcluded(Window window)
    {
        if (DEFAULT_MODAL_EXCLUSION_TYPE == null) {
            DEFAULT_MODAL_EXCLUSION_TYPE = Dialog.ModalExclusionType.APPLICATION_EXCLUDE;
        }
        return window.getModalExclusionType().compareTo(DEFAULT_MODAL_EXCLUSION_TYPE) >= 0;
    }
    public boolean isModalityTypeSupported(Dialog.ModalityType modalityType) {
        return (modalityType == Dialog.ModalityType.MODELESS) ||
               (modalityType == Dialog.ModalityType.APPLICATION_MODAL);
    }
    public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType exclusionType) {
        return (exclusionType == Dialog.ModalExclusionType.NO_EXCLUDE);
    }
    private ModalityListenerList modalityListeners = new ModalityListenerList();
    public void addModalityListener(ModalityListener listener) {
        modalityListeners.add(listener);
    }
    public void removeModalityListener(ModalityListener listener) {
        modalityListeners.remove(listener);
    }
    public void notifyModalityPushed(Dialog dialog) {
        notifyModalityChange(ModalityEvent.MODALITY_PUSHED, dialog);
    }
    public void notifyModalityPopped(Dialog dialog) {
        notifyModalityChange(ModalityEvent.MODALITY_POPPED, dialog);
    }
    final void notifyModalityChange(int id, Dialog source) {
        ModalityEvent ev = new ModalityEvent(source, modalityListeners, id);
        ev.dispatch();
    }
    static class ModalityListenerList implements ModalityListener {
        Vector<ModalityListener> listeners = new Vector<ModalityListener>();
        void add(ModalityListener listener) {
            listeners.addElement(listener);
        }
        void remove(ModalityListener listener) {
            listeners.removeElement(listener);
        }
        public void modalityPushed(ModalityEvent ev) {
            Iterator<ModalityListener> it = listeners.iterator();
            while (it.hasNext()) {
                it.next().modalityPushed(ev);
            }
        }
        public void modalityPopped(ModalityEvent ev) {
            Iterator<ModalityListener> it = listeners.iterator();
            while (it.hasNext()) {
                it.next().modalityPopped(ev);
            }
        }
    } 
    public static boolean isLightweightOrUnknown(Component comp) {
        if (comp.isLightweight()
            || !(getDefaultToolkit() instanceof SunToolkit))
        {
            return true;
        }
        return !(comp instanceof Button
            || comp instanceof Canvas
            || comp instanceof Checkbox
            || comp instanceof Choice
            || comp instanceof Label
            || comp instanceof java.awt.List
            || comp instanceof Panel
            || comp instanceof Scrollbar
            || comp instanceof ScrollPane
            || comp instanceof TextArea
            || comp instanceof TextField
            || comp instanceof Window);
    }
    public static Method getMethod(final Class clz, final String methodName, final Class[] params) {
        Method res = null;
        try {
            res = AccessController.doPrivileged(new PrivilegedExceptionAction<Method>() {
                    public Method run() throws Exception {
                        Method m = clz.getDeclaredMethod(methodName, params);
                        m.setAccessible(true);
                        return m;
                    }
                });
        } catch (PrivilegedActionException ex) {
            ex.printStackTrace();
        }
        return res;
    }
    public static class OperationTimedOut extends RuntimeException {
        public OperationTimedOut(String msg) {
            super(msg);
        }
        public OperationTimedOut() {
        }
    }
    public static class InfiniteLoop extends RuntimeException {
    }
    public static class IllegalThreadException extends RuntimeException {
        public IllegalThreadException(String msg) {
            super(msg);
        }
        public IllegalThreadException() {
        }
    }
    public static final int DEFAULT_WAIT_TIME = 10000;
    private static final int MAX_ITERS = 20;
    private static final int MIN_ITERS = 0;
    private static final int MINIMAL_EDELAY = 0;
    public void realSync() throws OperationTimedOut, InfiniteLoop {
        realSync(DEFAULT_WAIT_TIME);
    }
    public void realSync(final long timeout) throws OperationTimedOut, InfiniteLoop
    {
        if (EventQueue.isDispatchThread()) {
            throw new IllegalThreadException("The SunToolkit.realSync() method cannot be used on the event dispatch thread (EDT).");
        }
        int bigLoop = 0;
        do {
            sync();
            int iters = 0;
            while (iters < MIN_ITERS) {
                syncNativeQueue(timeout);
                iters++;
            }
            while (syncNativeQueue(timeout) && iters < MAX_ITERS) {
                iters++;
            }
            if (iters >= MAX_ITERS) {
                throw new InfiniteLoop();
            }
            iters = 0;
            while (iters < MIN_ITERS) {
                waitForIdle(timeout);
                iters++;
            }
            while (waitForIdle(timeout) && iters < MAX_ITERS) {
                iters++;
            }
            if (iters >= MAX_ITERS) {
                throw new InfiniteLoop();
            }
            bigLoop++;
        } while ((syncNativeQueue(timeout) || waitForIdle(timeout)) && bigLoop < MAX_ITERS);
    }
    protected abstract boolean syncNativeQueue(final long timeout);
    private boolean eventDispatched = false;
    private boolean queueEmpty = false;
    private final Object waitLock = "Wait Lock";
    static Method eqNoEvents;
    private boolean isEQEmpty() {
        EventQueue queue = getSystemEventQueueImpl();
        synchronized(SunToolkit.class) {
            if (eqNoEvents == null) {
                eqNoEvents = getMethod(java.awt.EventQueue.class, "noEvents", null);
            }
        }
        try {
            return (Boolean)eqNoEvents.invoke(queue);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    protected final boolean waitForIdle(final long timeout) {
        flushPendingEvents();
        boolean queueWasEmpty = isEQEmpty();
        queueEmpty = false;
        eventDispatched = false;
        synchronized(waitLock) {
            postEvent(AppContext.getAppContext(),
                      new PeerEvent(getSystemEventQueueImpl(), null, PeerEvent.LOW_PRIORITY_EVENT) {
                          public void dispatch() {
                              int iters = 0;
                              while (iters < MIN_ITERS) {
                                  syncNativeQueue(timeout);
                                  iters++;
                              }
                              while (syncNativeQueue(timeout) && iters < MAX_ITERS) {
                                  iters++;
                              }
                              flushPendingEvents();
                              synchronized(waitLock) {
                                  queueEmpty = isEQEmpty();
                                  eventDispatched = true;
                                  waitLock.notifyAll();
                              }
                          }
                      });
            try {
                while (!eventDispatched) {
                    waitLock.wait();
                }
            } catch (InterruptedException ie) {
                return false;
            }
        }
        try {
            Thread.sleep(MINIMAL_EDELAY);
        } catch (InterruptedException ie) {
            throw new RuntimeException("Interrupted");
        }
        flushPendingEvents();
        synchronized (waitLock) {
            return !(queueEmpty && isEQEmpty() && queueWasEmpty);
        }
    }
    public abstract void grab(Window w);
    public abstract void ungrab(Window w);
    public static native void closeSplashScreen();
    private void fireDesktopFontPropertyChanges() {
        setDesktopProperty(SunToolkit.DESKTOPFONTHINTS,
                           SunToolkit.getDesktopFontHints());
    }
    private static boolean checkedSystemAAFontSettings;
    private static boolean useSystemAAFontSettings;
    private static boolean lastExtraCondition = true;
    private static RenderingHints desktopFontHints;
    public static void setAAFontSettingsCondition(boolean extraCondition) {
        if (extraCondition != lastExtraCondition) {
            lastExtraCondition = extraCondition;
            if (checkedSystemAAFontSettings) {
                checkedSystemAAFontSettings = false;
                Toolkit tk = Toolkit.getDefaultToolkit();
                if (tk instanceof SunToolkit) {
                     ((SunToolkit)tk).fireDesktopFontPropertyChanges();
                }
            }
        }
    }
    private static RenderingHints getDesktopAAHintsByName(String hintname) {
        Object aaHint = null;
        hintname = hintname.toLowerCase(Locale.ENGLISH);
        if (hintname.equals("on")) {
            aaHint = VALUE_TEXT_ANTIALIAS_ON;
        } else if (hintname.equals("gasp")) {
            aaHint = VALUE_TEXT_ANTIALIAS_GASP;
        } else if (hintname.equals("lcd") || hintname.equals("lcd_hrgb")) {
            aaHint = VALUE_TEXT_ANTIALIAS_LCD_HRGB;
        } else if (hintname.equals("lcd_hbgr")) {
            aaHint = VALUE_TEXT_ANTIALIAS_LCD_HBGR;
        } else if (hintname.equals("lcd_vrgb")) {
            aaHint = VALUE_TEXT_ANTIALIAS_LCD_VRGB;
        } else if (hintname.equals("lcd_vbgr")) {
            aaHint = VALUE_TEXT_ANTIALIAS_LCD_VBGR;
        }
        if (aaHint != null) {
            RenderingHints map = new RenderingHints(null);
            map.put(KEY_TEXT_ANTIALIASING, aaHint);
            return map;
        } else {
            return null;
        }
    }
    private static boolean useSystemAAFontSettings() {
        if (!checkedSystemAAFontSettings) {
            useSystemAAFontSettings = true; 
            String systemAAFonts = null;
            Toolkit tk = Toolkit.getDefaultToolkit();
            if (tk instanceof SunToolkit) {
                systemAAFonts =
                    (String)AccessController.doPrivileged(
                         new GetPropertyAction("awt.useSystemAAFontSettings"));
            }
            if (systemAAFonts != null) {
                useSystemAAFontSettings =
                    Boolean.valueOf(systemAAFonts).booleanValue();
                if (!useSystemAAFontSettings) {
                    desktopFontHints = getDesktopAAHintsByName(systemAAFonts);
                }
            }
            if (useSystemAAFontSettings) {
                 useSystemAAFontSettings = lastExtraCondition;
            }
            checkedSystemAAFontSettings = true;
        }
        return useSystemAAFontSettings;
    }
    public static final String DESKTOPFONTHINTS = "awt.font.desktophints";
    protected RenderingHints getDesktopAAHints() {
        return null;
    }
    public static RenderingHints getDesktopFontHints() {
        if (useSystemAAFontSettings()) {
             Toolkit tk = Toolkit.getDefaultToolkit();
             if (tk instanceof SunToolkit) {
                 Object map = ((SunToolkit)tk).getDesktopAAHints();
                 return (RenderingHints)map;
             } else { 
                 return null;
             }
        } else if (desktopFontHints != null) {
            return (RenderingHints)(desktopFontHints.clone());
        } else {
            return null;
        }
    }
    public abstract boolean isDesktopSupported();
    private static Method consumeNextKeyTypedMethod = null;
    public static synchronized void consumeNextKeyTyped(KeyEvent keyEvent) {
        if (consumeNextKeyTypedMethod == null) {
            consumeNextKeyTypedMethod = getMethod(DefaultKeyboardFocusManager.class,
                                                  "consumeNextKeyTyped",
                                                  new Class[] {KeyEvent.class});
        }
        try {
            consumeNextKeyTypedMethod.invoke(KeyboardFocusManager.getCurrentKeyboardFocusManager(),
                                             keyEvent);
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
        }
    }
    protected static void dumpPeers(final PlatformLogger aLog) {
        AWTAutoShutdown.getInstance().dumpPeers(aLog);
    }
    public static Window getContainingWindow(Component comp) {
        while (comp != null && !(comp instanceof Window)) {
            comp = comp.getParent();
        }
        return (Window)comp;
    }
    public static String getSystemProperty(final String key) {
        return (String)AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return System.getProperty(key);
                }
            });
    }
    protected static Boolean getBooleanSystemProperty(String key) {
        return Boolean.valueOf(AccessController.
                   doPrivileged(new GetBooleanAction(key)));
    }
    private static Boolean sunAwtDisableMixing = null;
    public synchronized static boolean getSunAwtDisableMixing() {
        if (sunAwtDisableMixing == null) {
            sunAwtDisableMixing = getBooleanSystemProperty("sun.awt.disableMixing");
        }
        return sunAwtDisableMixing.booleanValue();
    }
    public boolean isNativeGTKAvailable() {
        return false;
    }
    public boolean isWindowOpacitySupported() {
        return false;
    }
    public boolean isWindowShapingSupported() {
        return false;
    }
    public boolean isWindowTranslucencySupported() {
        return false;
    }
    public boolean isTranslucencyCapable(GraphicsConfiguration gc) {
        return false;
    }
    public static boolean isContainingTopLevelOpaque(Component c) {
        Window w = getContainingWindow(c);
        return w != null && w.isOpaque();
    }
    public static boolean isContainingTopLevelTranslucent(Component c) {
        Window w = getContainingWindow(c);
        return w != null && ((Window)w).getOpacity() < 1.0f;
    }
    public boolean needUpdateWindow() {
        return false;
    }
    public int getNumberOfButtons(){
        return 3;
    }
    public static boolean isInstanceOf(Object obj, String type) {
        if (obj == null) return false;
        if (type == null) return false;
        return isInstanceOf(obj.getClass(), type);
    }
    private static boolean isInstanceOf(Class cls, String type) {
        if (cls == null) return false;
        if (cls.getName().equals(type)) {
            return true;
        }
        for (Class c : cls.getInterfaces()) {
            if (c.getName().equals(type)) {
                return true;
            }
        }
        return isInstanceOf(cls.getSuperclass(), type);
    }
    public static void setSystemGenerated(AWTEvent e) {
        AWTAccessor.getAWTEventAccessor().setSystemGenerated(e);
    }
    public static boolean isSystemGenerated(AWTEvent e) {
        return AWTAccessor.getAWTEventAccessor().isSystemGenerated(e);
    }
} 
class PostEventQueue {
    private EventQueueItem queueHead = null;
    private EventQueueItem queueTail = null;
    private final EventQueue eventQueue;
    PostEventQueue(EventQueue eq) {
        eventQueue = eq;
    }
    public synchronized boolean noEvents() {
        return queueHead == null;
    }
    public synchronized void flush() {
        EventQueueItem tempQueue = queueHead;
        queueHead = queueTail = null;
        while (tempQueue != null) {
            eventQueue.postEvent(tempQueue.event);
            tempQueue = tempQueue.next;
        }
    }
    void postEvent(AWTEvent event) {
        EventQueueItem item = new EventQueueItem(event);
        synchronized (this) {
            if (queueHead == null) {
                queueHead = queueTail = item;
            } else {
                queueTail.next = item;
                queueTail = item;
            }
        }
        SunToolkit.wakeupEventQueue(eventQueue, event.getSource() == AWTAutoShutdown.getInstance());
    }
} 
