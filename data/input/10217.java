public class RepaintManager
{
    static final boolean HANDLE_TOP_LEVEL_PAINT;
    private static final short BUFFER_STRATEGY_NOT_SPECIFIED = 0;
    private static final short BUFFER_STRATEGY_SPECIFIED_ON = 1;
    private static final short BUFFER_STRATEGY_SPECIFIED_OFF = 2;
    private static final short BUFFER_STRATEGY_TYPE;
    private Map<GraphicsConfiguration,VolatileImage> volatileMap = new
                        HashMap<GraphicsConfiguration,VolatileImage>(1);
    private Map<Container,Rectangle> hwDirtyComponents;
    private Map<Component,Rectangle> dirtyComponents;
    private Map<Component,Rectangle> tmpDirtyComponents;
    private java.util.List<Component> invalidComponents;
    private java.util.List<Runnable> runnableList;
    boolean   doubleBufferingEnabled = true;
    private Dimension doubleBufferMaxSize;
    DoubleBufferInfo standardDoubleBuffer;
    private PaintManager paintManager;
    private static final Object repaintManagerKey = RepaintManager.class;
    static boolean volatileImageBufferEnabled = true;
    private static boolean nativeDoubleBuffering;
    private static final int VOLATILE_LOOP_MAX = 2;
    private int paintDepth = 0;
    private short bufferStrategyType;
    private boolean painting;
    private JComponent repaintRoot;
    private Thread paintThread;
    private final ProcessingRunnable processingRunnable;
    static {
        volatileImageBufferEnabled = "true".equals(AccessController.
                doPrivileged(new GetPropertyAction(
                "swing.volatileImageBufferEnabled", "true")));
        boolean headless = GraphicsEnvironment.isHeadless();
        if (volatileImageBufferEnabled && headless) {
            volatileImageBufferEnabled = false;
        }
        nativeDoubleBuffering = "true".equals(AccessController.doPrivileged(
                    new GetPropertyAction("awt.nativeDoubleBuffering")));
        String bs = AccessController.doPrivileged(
                          new GetPropertyAction("swing.bufferPerWindow"));
        if (headless) {
            BUFFER_STRATEGY_TYPE = BUFFER_STRATEGY_SPECIFIED_OFF;
        }
        else if (bs == null) {
            BUFFER_STRATEGY_TYPE = BUFFER_STRATEGY_NOT_SPECIFIED;
        }
        else if ("true".equals(bs)) {
            BUFFER_STRATEGY_TYPE = BUFFER_STRATEGY_SPECIFIED_ON;
        }
        else {
            BUFFER_STRATEGY_TYPE = BUFFER_STRATEGY_SPECIFIED_OFF;
        }
        HANDLE_TOP_LEVEL_PAINT = "true".equals(AccessController.doPrivileged(
               new GetPropertyAction("swing.handleTopLevelPaint", "true")));
        GraphicsEnvironment ge = GraphicsEnvironment.
                getLocalGraphicsEnvironment();
        if (ge instanceof SunGraphicsEnvironment) {
            ((SunGraphicsEnvironment)ge).addDisplayChangedListener(
                    new DisplayChangedHandler());
        }
    }
    public static RepaintManager currentManager(Component c) {
        return currentManager(AppContext.getAppContext());
    }
    static RepaintManager currentManager(AppContext appContext) {
        RepaintManager rm = (RepaintManager)appContext.get(repaintManagerKey);
        if (rm == null) {
            rm = new RepaintManager(BUFFER_STRATEGY_TYPE);
            appContext.put(repaintManagerKey, rm);
        }
        return rm;
    }
    public static RepaintManager currentManager(JComponent c) {
        return currentManager((Component)c);
    }
    public static void setCurrentManager(RepaintManager aRepaintManager) {
        if (aRepaintManager != null) {
            SwingUtilities.appContextPut(repaintManagerKey, aRepaintManager);
        } else {
            SwingUtilities.appContextRemove(repaintManagerKey);
        }
    }
    public RepaintManager() {
        this(BUFFER_STRATEGY_SPECIFIED_OFF);
    }
    private RepaintManager(short bufferStrategyType) {
        doubleBufferingEnabled = !nativeDoubleBuffering;
        synchronized(this) {
            dirtyComponents = new IdentityHashMap<Component,Rectangle>();
            tmpDirtyComponents = new IdentityHashMap<Component,Rectangle>();
            this.bufferStrategyType = bufferStrategyType;
            hwDirtyComponents = new IdentityHashMap<Container,Rectangle>();
        }
        processingRunnable = new ProcessingRunnable();
    }
    private void displayChanged() {
        clearImages();
    }
    public synchronized void addInvalidComponent(JComponent invalidComponent)
    {
        RepaintManager delegate = getDelegate(invalidComponent);
        if (delegate != null) {
            delegate.addInvalidComponent(invalidComponent);
            return;
        }
        Component validateRoot =
            SwingUtilities.getValidateRoot(invalidComponent, true);
        if (validateRoot == null) {
            return;
        }
        if (invalidComponents == null) {
            invalidComponents = new ArrayList<Component>();
        }
        else {
            int n = invalidComponents.size();
            for(int i = 0; i < n; i++) {
                if(validateRoot == invalidComponents.get(i)) {
                    return;
                }
            }
        }
        invalidComponents.add(validateRoot);
        scheduleProcessingRunnable();
    }
    public synchronized void removeInvalidComponent(JComponent component) {
        RepaintManager delegate = getDelegate(component);
        if (delegate != null) {
            delegate.removeInvalidComponent(component);
            return;
        }
        if(invalidComponents != null) {
            int index = invalidComponents.indexOf(component);
            if(index != -1) {
                invalidComponents.remove(index);
            }
        }
    }
    private void addDirtyRegion0(Container c, int x, int y, int w, int h) {
        if ((w <= 0) || (h <= 0) || (c == null)) {
            return;
        }
        if ((c.getWidth() <= 0) || (c.getHeight() <= 0)) {
            return;
        }
        if (extendDirtyRegion(c, x, y, w, h)) {
            return;
        }
        Component root = null;
        for (Container p = c; p != null; p = p.getParent()) {
            if (!p.isVisible() || (p.getPeer() == null)) {
                return;
            }
            if ((p instanceof Window) || (p instanceof Applet)) {
                if (p instanceof Frame &&
                        (((Frame)p).getExtendedState() & Frame.ICONIFIED) ==
                                    Frame.ICONIFIED) {
                    return;
                }
                root = p;
                break;
            }
        }
        if (root == null) return;
        synchronized(this) {
            if (extendDirtyRegion(c, x, y, w, h)) {
                return;
            }
            dirtyComponents.put(c, new Rectangle(x, y, w, h));
        }
        scheduleProcessingRunnable();
    }
    public void addDirtyRegion(JComponent c, int x, int y, int w, int h)
    {
        RepaintManager delegate = getDelegate(c);
        if (delegate != null) {
            delegate.addDirtyRegion(c, x, y, w, h);
            return;
        }
        addDirtyRegion0(c, x, y, w, h);
    }
    public void addDirtyRegion(Window window, int x, int y, int w, int h) {
        addDirtyRegion0(window, x, y, w, h);
    }
    public void addDirtyRegion(Applet applet, int x, int y, int w, int h) {
        addDirtyRegion0(applet, x, y, w, h);
    }
    void scheduleHeavyWeightPaints() {
        Map<Container,Rectangle> hws;
        synchronized(this) {
            if (hwDirtyComponents.size() == 0) {
                return;
            }
            hws = hwDirtyComponents;
            hwDirtyComponents =  new IdentityHashMap<Container,Rectangle>();
        }
        for (Container hw : hws.keySet()) {
            Rectangle dirty = hws.get(hw);
            if (hw instanceof Window) {
                addDirtyRegion((Window)hw, dirty.x, dirty.y,
                               dirty.width, dirty.height);
            }
            else if (hw instanceof Applet) {
                addDirtyRegion((Applet)hw, dirty.x, dirty.y,
                               dirty.width, dirty.height);
            }
            else { 
                addDirtyRegion0(hw, dirty.x, dirty.y,
                                dirty.width, dirty.height);
            }
        }
    }
    void nativeAddDirtyRegion(AppContext appContext, Container c,
                              int x, int y, int w, int h) {
        if (w > 0 && h > 0) {
            synchronized(this) {
                Rectangle dirty = hwDirtyComponents.get(c);
                if (dirty == null) {
                    hwDirtyComponents.put(c, new Rectangle(x, y, w, h));
                }
                else {
                    hwDirtyComponents.put(c, SwingUtilities.computeUnion(
                                              x, y, w, h, dirty));
                }
            }
            scheduleProcessingRunnable(appContext);
        }
    }
    void nativeQueueSurfaceDataRunnable(AppContext appContext, Component c,
                                        Runnable r) {
        synchronized(this) {
            if (runnableList == null) {
                runnableList = new LinkedList<Runnable>();
            }
            runnableList.add(r);
        }
        scheduleProcessingRunnable(appContext);
    }
    private synchronized boolean extendDirtyRegion(
        Component c, int x, int y, int w, int h) {
        Rectangle r = dirtyComponents.get(c);
        if (r != null) {
            SwingUtilities.computeUnion(x, y, w, h, r);
            return true;
        }
        return false;
    }
    public Rectangle getDirtyRegion(JComponent aComponent) {
        RepaintManager delegate = getDelegate(aComponent);
        if (delegate != null) {
            return delegate.getDirtyRegion(aComponent);
        }
        Rectangle r;
        synchronized(this) {
            r = dirtyComponents.get(aComponent);
        }
        if(r == null)
            return new Rectangle(0,0,0,0);
        else
            return new Rectangle(r);
    }
    public void markCompletelyDirty(JComponent aComponent) {
        RepaintManager delegate = getDelegate(aComponent);
        if (delegate != null) {
            delegate.markCompletelyDirty(aComponent);
            return;
        }
        addDirtyRegion(aComponent,0,0,Integer.MAX_VALUE,Integer.MAX_VALUE);
    }
    public void markCompletelyClean(JComponent aComponent) {
        RepaintManager delegate = getDelegate(aComponent);
        if (delegate != null) {
            delegate.markCompletelyClean(aComponent);
            return;
        }
        synchronized(this) {
                dirtyComponents.remove(aComponent);
        }
    }
    public boolean isCompletelyDirty(JComponent aComponent) {
        RepaintManager delegate = getDelegate(aComponent);
        if (delegate != null) {
            return delegate.isCompletelyDirty(aComponent);
        }
        Rectangle r;
        r = getDirtyRegion(aComponent);
        if(r.width == Integer.MAX_VALUE &&
           r.height == Integer.MAX_VALUE)
            return true;
        else
            return false;
    }
    public void validateInvalidComponents() {
        java.util.List<Component> ic;
        synchronized(this) {
            if(invalidComponents == null) {
                return;
            }
            ic = invalidComponents;
            invalidComponents = null;
        }
        int n = ic.size();
        for(int i = 0; i < n; i++) {
            ic.get(i).validate();
        }
    }
    private void prePaintDirtyRegions() {
        Map<Component,Rectangle> dirtyComponents;
        java.util.List<Runnable> runnableList;
        synchronized(this) {
            dirtyComponents = this.dirtyComponents;
            runnableList = this.runnableList;
            this.runnableList = null;
        }
        if (runnableList != null) {
            for (Runnable runnable : runnableList) {
                runnable.run();
            }
        }
        paintDirtyRegions();
        if (dirtyComponents.size() > 0) {
            paintDirtyRegions(dirtyComponents);
        }
    }
    private void updateWindows(Map<Component,Rectangle> dirtyComponents) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if (!(toolkit instanceof SunToolkit &&
              ((SunToolkit)toolkit).needUpdateWindow()))
        {
            return;
        }
        Set<Window> windows = new HashSet<Window>();
        Set<Component> dirtyComps = dirtyComponents.keySet();
        for (Iterator<Component> it = dirtyComps.iterator(); it.hasNext();) {
            Component dirty = it.next();
            Window window = dirty instanceof Window ?
                (Window)dirty :
                SwingUtilities.getWindowAncestor(dirty);
            if (window != null &&
                !window.isOpaque())
            {
                windows.add(window);
            }
        }
        for (Window window : windows) {
            AWTAccessor.getWindowAccessor().updateWindow(window);
        }
    }
    boolean isPainting() {
        return painting;
    }
    public void paintDirtyRegions() {
        synchronized(this) {  
            Map<Component,Rectangle> tmp = tmpDirtyComponents;
            tmpDirtyComponents = dirtyComponents;
            dirtyComponents = tmp;
            dirtyComponents.clear();
        }
        paintDirtyRegions(tmpDirtyComponents);
    }
    private void paintDirtyRegions(Map<Component,Rectangle>
                                   tmpDirtyComponents){
        int i, count;
        java.util.List<Component> roots;
        Component dirtyComponent;
        count = tmpDirtyComponents.size();
        if (count == 0) {
            return;
        }
        Rectangle rect;
        int localBoundsX = 0;
        int localBoundsY = 0;
        int localBoundsH;
        int localBoundsW;
        Enumeration keys;
        roots = new ArrayList<Component>(count);
        for (Component dirty : tmpDirtyComponents.keySet()) {
            collectDirtyComponents(tmpDirtyComponents, dirty, roots);
        }
        count = roots.size();
        painting = true;
        try {
            for(i=0 ; i < count ; i++) {
                dirtyComponent = roots.get(i);
                rect = tmpDirtyComponents.get(dirtyComponent);
                localBoundsH = dirtyComponent.getHeight();
                localBoundsW = dirtyComponent.getWidth();
                SwingUtilities.computeIntersection(localBoundsX,
                                                   localBoundsY,
                                                   localBoundsW,
                                                   localBoundsH,
                                                   rect);
                if (dirtyComponent instanceof JComponent) {
                    ((JComponent)dirtyComponent).paintImmediately(
                        rect.x,rect.y,rect.width, rect.height);
                }
                else if (dirtyComponent.isShowing()) {
                    Graphics g = JComponent.safelyGetGraphics(
                            dirtyComponent, dirtyComponent);
                    if (g != null) {
                        g.setClip(rect.x, rect.y, rect.width, rect.height);
                        try {
                            dirtyComponent.paint(g);
                        } finally {
                            g.dispose();
                        }
                    }
                }
                if (repaintRoot != null) {
                    adjustRoots(repaintRoot, roots, i + 1);
                    count = roots.size();
                    paintManager.isRepaintingRoot = true;
                    repaintRoot.paintImmediately(0, 0, repaintRoot.getWidth(),
                                                 repaintRoot.getHeight());
                    paintManager.isRepaintingRoot = false;
                    repaintRoot = null;
                }
            }
        } finally {
            painting = false;
        }
        updateWindows(tmpDirtyComponents);
        tmpDirtyComponents.clear();
    }
    private void adjustRoots(JComponent root,
                             java.util.List<Component> roots, int index) {
        for (int i = roots.size() - 1; i >= index; i--) {
            Component c = roots.get(i);
            for(;;) {
                if (c == root || c == null || !(c instanceof JComponent)) {
                    break;
                }
                c = c.getParent();
            }
            if (c == root) {
                roots.remove(i);
            }
        }
    }
    Rectangle tmp = new Rectangle();
    void collectDirtyComponents(Map<Component,Rectangle> dirtyComponents,
                                Component dirtyComponent,
                                java.util.List<Component> roots) {
        int dx, dy, rootDx, rootDy;
        Component component, rootDirtyComponent,parent;
        Rectangle cBounds;
        component = rootDirtyComponent = dirtyComponent;
        int x = dirtyComponent.getX();
        int y = dirtyComponent.getY();
        int w = dirtyComponent.getWidth();
        int h = dirtyComponent.getHeight();
        dx = rootDx = 0;
        dy = rootDy = 0;
        tmp.setBounds(dirtyComponents.get(dirtyComponent));
        SwingUtilities.computeIntersection(0,0,w,h,tmp);
        if (tmp.isEmpty()) {
            return;
        }
        for(;;) {
            if(!(component instanceof JComponent))
                break;
            parent = component.getParent();
            if(parent == null)
                break;
            component = parent;
            dx += x;
            dy += y;
            tmp.setLocation(tmp.x + x, tmp.y + y);
            x = component.getX();
            y = component.getY();
            w = component.getWidth();
            h = component.getHeight();
            tmp = SwingUtilities.computeIntersection(0,0,w,h,tmp);
            if (tmp.isEmpty()) {
                return;
            }
            if (dirtyComponents.get(component) != null) {
                rootDirtyComponent = component;
                rootDx = dx;
                rootDy = dy;
            }
        }
        if (dirtyComponent != rootDirtyComponent) {
            Rectangle r;
            tmp.setLocation(tmp.x + rootDx - dx,
                            tmp.y + rootDy - dy);
            r = dirtyComponents.get(rootDirtyComponent);
            SwingUtilities.computeUnion(tmp.x,tmp.y,tmp.width,tmp.height,r);
        }
        if (!roots.contains(rootDirtyComponent))
            roots.add(rootDirtyComponent);
    }
    public synchronized String toString() {
        StringBuffer sb = new StringBuffer();
        if(dirtyComponents != null)
            sb.append("" + dirtyComponents);
        return sb.toString();
    }
    public Image getOffscreenBuffer(Component c,int proposedWidth,int proposedHeight) {
        RepaintManager delegate = getDelegate(c);
        if (delegate != null) {
            return delegate.getOffscreenBuffer(c, proposedWidth, proposedHeight);
        }
        return _getOffscreenBuffer(c, proposedWidth, proposedHeight);
    }
    public Image getVolatileOffscreenBuffer(Component c,
                                            int proposedWidth,int proposedHeight) {
        RepaintManager delegate = getDelegate(c);
        if (delegate != null) {
            return delegate.getVolatileOffscreenBuffer(c, proposedWidth,
                                                        proposedHeight);
        }
        Window w = (c instanceof Window) ? (Window)c : SwingUtilities.getWindowAncestor(c);
        if (!w.isOpaque()) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            if ((tk instanceof SunToolkit) && (((SunToolkit)tk).needUpdateWindow())) {
                return null;
            }
        }
        GraphicsConfiguration config = c.getGraphicsConfiguration();
        if (config == null) {
            config = GraphicsEnvironment.getLocalGraphicsEnvironment().
                            getDefaultScreenDevice().getDefaultConfiguration();
        }
        Dimension maxSize = getDoubleBufferMaximumSize();
        int width = proposedWidth < 1 ? 1 :
            (proposedWidth > maxSize.width? maxSize.width : proposedWidth);
        int height = proposedHeight < 1 ? 1 :
            (proposedHeight > maxSize.height? maxSize.height : proposedHeight);
        VolatileImage image = volatileMap.get(config);
        if (image == null || image.getWidth() < width ||
                             image.getHeight() < height) {
            if (image != null) {
                image.flush();
            }
            image = config.createCompatibleVolatileImage(width, height);
            volatileMap.put(config, image);
        }
        return image;
    }
    private Image _getOffscreenBuffer(Component c, int proposedWidth, int proposedHeight) {
        Dimension maxSize = getDoubleBufferMaximumSize();
        DoubleBufferInfo doubleBuffer;
        int width, height;
        Window w = (c instanceof Window) ? (Window)c : SwingUtilities.getWindowAncestor(c);
        if (!w.isOpaque()) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            if ((tk instanceof SunToolkit) && (((SunToolkit)tk).needUpdateWindow())) {
                return null;
            }
        }
        if (standardDoubleBuffer == null) {
            standardDoubleBuffer = new DoubleBufferInfo();
        }
        doubleBuffer = standardDoubleBuffer;
        width = proposedWidth < 1? 1 :
                  (proposedWidth > maxSize.width? maxSize.width : proposedWidth);
        height = proposedHeight < 1? 1 :
                  (proposedHeight > maxSize.height? maxSize.height : proposedHeight);
        if (doubleBuffer.needsReset || (doubleBuffer.image != null &&
                                        (doubleBuffer.size.width < width ||
                                         doubleBuffer.size.height < height))) {
            doubleBuffer.needsReset = false;
            if (doubleBuffer.image != null) {
                doubleBuffer.image.flush();
                doubleBuffer.image = null;
            }
            width = Math.max(doubleBuffer.size.width, width);
            height = Math.max(doubleBuffer.size.height, height);
        }
        Image result = doubleBuffer.image;
        if (doubleBuffer.image == null) {
            result = c.createImage(width , height);
            doubleBuffer.size = new Dimension(width, height);
            if (c instanceof JComponent) {
                ((JComponent)c).setCreatedDoubleBuffer(true);
                doubleBuffer.image = result;
            }
        }
        return result;
    }
    public void setDoubleBufferMaximumSize(Dimension d) {
        doubleBufferMaxSize = d;
        if (doubleBufferMaxSize == null) {
            clearImages();
        } else {
            clearImages(d.width, d.height);
        }
    }
    private void clearImages() {
        clearImages(0, 0);
    }
    private void clearImages(int width, int height) {
        if (standardDoubleBuffer != null && standardDoubleBuffer.image != null) {
            if (standardDoubleBuffer.image.getWidth(null) > width ||
                standardDoubleBuffer.image.getHeight(null) > height) {
                standardDoubleBuffer.image.flush();
                standardDoubleBuffer.image = null;
            }
        }
        Iterator gcs = volatileMap.keySet().iterator();
        while (gcs.hasNext()) {
            GraphicsConfiguration gc = (GraphicsConfiguration)gcs.next();
            VolatileImage image = volatileMap.get(gc);
            if (image.getWidth() > width || image.getHeight() > height) {
                image.flush();
                gcs.remove();
            }
        }
    }
    public Dimension getDoubleBufferMaximumSize() {
        if (doubleBufferMaxSize == null) {
            try {
                Rectangle virtualBounds = new Rectangle();
                GraphicsEnvironment ge = GraphicsEnvironment.
                                                 getLocalGraphicsEnvironment();
                for (GraphicsDevice gd : ge.getScreenDevices()) {
                    GraphicsConfiguration gc = gd.getDefaultConfiguration();
                    virtualBounds = virtualBounds.union(gc.getBounds());
                }
                doubleBufferMaxSize = new Dimension(virtualBounds.width,
                                                    virtualBounds.height);
            } catch (HeadlessException e) {
                doubleBufferMaxSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
        }
        return doubleBufferMaxSize;
    }
    public void setDoubleBufferingEnabled(boolean aFlag) {
        doubleBufferingEnabled = aFlag;
        PaintManager paintManager = getPaintManager();
        if (!aFlag && paintManager.getClass() != PaintManager.class) {
            setPaintManager(new PaintManager());
        }
    }
    public boolean isDoubleBufferingEnabled() {
        return doubleBufferingEnabled;
    }
    void resetDoubleBuffer() {
        if (standardDoubleBuffer != null) {
            standardDoubleBuffer.needsReset = true;
        }
    }
    void resetVolatileDoubleBuffer(GraphicsConfiguration gc) {
        Image image = volatileMap.remove(gc);
        if (image != null) {
            image.flush();
        }
    }
    boolean useVolatileDoubleBuffer() {
        return volatileImageBufferEnabled;
    }
    private synchronized boolean isPaintingThread() {
        return (Thread.currentThread() == paintThread);
    }
    void paint(JComponent paintingComponent,
               JComponent bufferComponent, Graphics g,
               int x, int y, int w, int h) {
        PaintManager paintManager = getPaintManager();
        if (!isPaintingThread()) {
            if (paintManager.getClass() != PaintManager.class) {
                paintManager = new PaintManager();
                paintManager.repaintManager = this;
            }
        }
        if (!paintManager.paint(paintingComponent, bufferComponent, g,
                                x, y, w, h)) {
            g.setClip(x, y, w, h);
            paintingComponent.paintToOffscreen(g, x, y, w, h, x + w, y + h);
        }
    }
    void copyArea(JComponent c, Graphics g, int x, int y, int w, int h,
                  int deltaX, int deltaY, boolean clip) {
        getPaintManager().copyArea(c, g, x, y, w, h, deltaX, deltaY, clip);
    }
    void beginPaint() {
        boolean multiThreadedPaint = false;
        int paintDepth;
        Thread currentThread = Thread.currentThread();
        synchronized(this) {
            paintDepth = this.paintDepth;
            if (paintThread == null || currentThread == paintThread) {
                paintThread = currentThread;
                this.paintDepth++;
            } else {
                multiThreadedPaint = true;
            }
        }
        if (!multiThreadedPaint && paintDepth == 0) {
            getPaintManager().beginPaint();
        }
    }
    void endPaint() {
        if (isPaintingThread()) {
            PaintManager paintManager = null;
            synchronized(this) {
                if (--paintDepth == 0) {
                    paintManager = getPaintManager();
                }
            }
            if (paintManager != null) {
                paintManager.endPaint();
                synchronized(this) {
                    paintThread = null;
                }
            }
        }
    }
    boolean show(Container c, int x, int y, int w, int h) {
        return getPaintManager().show(c, x, y, w, h);
    }
    void doubleBufferingChanged(JRootPane rootPane) {
        getPaintManager().doubleBufferingChanged(rootPane);
    }
    void setPaintManager(PaintManager paintManager) {
        if (paintManager == null) {
            paintManager = new PaintManager();
        }
        PaintManager oldPaintManager;
        synchronized(this) {
            oldPaintManager = this.paintManager;
            this.paintManager = paintManager;
            paintManager.repaintManager = this;
        }
        if (oldPaintManager != null) {
            oldPaintManager.dispose();
        }
    }
    private synchronized PaintManager getPaintManager() {
        if (paintManager == null) {
            PaintManager paintManager = null;
            if (doubleBufferingEnabled && !nativeDoubleBuffering) {
                switch (bufferStrategyType) {
                case BUFFER_STRATEGY_NOT_SPECIFIED:
                    Toolkit tk = Toolkit.getDefaultToolkit();
                    if (tk instanceof SunToolkit) {
                        SunToolkit stk = (SunToolkit) tk;
                        if (stk.useBufferPerWindow()) {
                            paintManager = new BufferStrategyPaintManager();
                        }
                    }
                    break;
                case BUFFER_STRATEGY_SPECIFIED_ON:
                    paintManager = new BufferStrategyPaintManager();
                    break;
                default:
                    break;
                }
            }
            setPaintManager(paintManager);
        }
        return paintManager;
    }
    private void scheduleProcessingRunnable() {
        scheduleProcessingRunnable(AppContext.getAppContext());
    }
    private void scheduleProcessingRunnable(AppContext context) {
        if (processingRunnable.markPending()) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            if (tk instanceof SunToolkit) {
                SunToolkit.getSystemEventQueueImplPP(context).
                  postEvent(new InvocationEvent(Toolkit.getDefaultToolkit(),
                                                processingRunnable));
            } else {
                Toolkit.getDefaultToolkit().getSystemEventQueue().
                      postEvent(new InvocationEvent(Toolkit.getDefaultToolkit(),
                                                    processingRunnable));
            }
        }
    }
    static class PaintManager {
        protected RepaintManager repaintManager;
        boolean isRepaintingRoot;
        public boolean paint(JComponent paintingComponent,
                             JComponent bufferComponent, Graphics g,
                             int x, int y, int w, int h) {
            boolean paintCompleted = false;
            Image offscreen;
            if (repaintManager.useVolatileDoubleBuffer() &&
                (offscreen = getValidImage(repaintManager.
                getVolatileOffscreenBuffer(bufferComponent, w, h))) != null) {
                VolatileImage vImage = (java.awt.image.VolatileImage)offscreen;
                GraphicsConfiguration gc = bufferComponent.
                                            getGraphicsConfiguration();
                for (int i = 0; !paintCompleted &&
                         i < RepaintManager.VOLATILE_LOOP_MAX; i++) {
                    if (vImage.validate(gc) ==
                                   VolatileImage.IMAGE_INCOMPATIBLE) {
                        repaintManager.resetVolatileDoubleBuffer(gc);
                        offscreen = repaintManager.getVolatileOffscreenBuffer(
                            bufferComponent,w, h);
                        vImage = (java.awt.image.VolatileImage)offscreen;
                    }
                    paintDoubleBuffered(paintingComponent, vImage, g, x, y,
                                        w, h);
                    paintCompleted = !vImage.contentsLost();
                }
            }
            if (!paintCompleted && (offscreen = getValidImage(
                      repaintManager.getOffscreenBuffer(
                      bufferComponent, w, h))) != null) {
                paintDoubleBuffered(paintingComponent, offscreen, g, x, y, w,
                                    h);
                paintCompleted = true;
            }
            return paintCompleted;
        }
        public void copyArea(JComponent c, Graphics g, int x, int y, int w,
                             int h, int deltaX, int deltaY, boolean clip) {
            g.copyArea(x, y, w, h, deltaX, deltaY);
        }
        public void beginPaint() {
        }
        public void endPaint() {
        }
        public boolean show(Container c, int x, int y, int w, int h) {
            return false;
        }
        public void doubleBufferingChanged(JRootPane rootPane) {
        }
        protected void paintDoubleBuffered(JComponent c, Image image,
                            Graphics g, int clipX, int clipY,
                            int clipW, int clipH) {
            Graphics osg = image.getGraphics();
            int bw = Math.min(clipW, image.getWidth(null));
            int bh = Math.min(clipH, image.getHeight(null));
            int x,y,maxx,maxy;
            try {
                for(x = clipX, maxx = clipX+clipW; x < maxx ;  x += bw ) {
                    for(y=clipY, maxy = clipY + clipH; y < maxy ; y += bh) {
                        osg.translate(-x, -y);
                        osg.setClip(x,y,bw,bh);
                        c.paintToOffscreen(osg, x, y, bw, bh, maxx, maxy);
                        g.setClip(x, y, bw, bh);
                        g.drawImage(image, x, y, c);
                        osg.translate(x, y);
                    }
                }
            } finally {
                osg.dispose();
            }
        }
        private Image getValidImage(Image image) {
            if (image != null && image.getWidth(null) > 0 &&
                                 image.getHeight(null) > 0) {
                return image;
            }
            return null;
        }
        protected void repaintRoot(JComponent root) {
            assert (repaintManager.repaintRoot == null);
            if (repaintManager.painting) {
                repaintManager.repaintRoot = root;
            }
            else {
                root.repaint();
            }
        }
        protected boolean isRepaintingRoot() {
            return isRepaintingRoot;
        }
        protected void dispose() {
        }
    }
    private class DoubleBufferInfo {
        public Image image;
        public Dimension size;
        public boolean needsReset = false;
    }
    private static final class DisplayChangedHandler implements
                                             DisplayChangedListener {
        public void displayChanged() {
            scheduleDisplayChanges();
        }
        public void paletteChanged() {
        }
        private void scheduleDisplayChanges() {
            for (Object c : AppContext.getAppContexts()) {
                AppContext context = (AppContext) c;
                synchronized(context) {
                    if (!context.isDisposed()) {
                        EventQueue eventQueue = (EventQueue)context.get(
                            AppContext.EVENT_QUEUE_KEY);
                        if (eventQueue != null) {
                            eventQueue.postEvent(new InvocationEvent(
                                Toolkit.getDefaultToolkit(),
                                new DisplayChangedRunnable()));
                        }
                    }
                }
            }
        }
    }
    private static final class DisplayChangedRunnable implements Runnable {
        public void run() {
            RepaintManager.currentManager((JComponent)null).displayChanged();
        }
    }
    private final class ProcessingRunnable implements Runnable {
        private boolean pending;
        public synchronized boolean markPending() {
            if (!pending) {
                pending = true;
                return true;
            }
            return false;
        }
        public void run() {
            synchronized (this) {
                pending = false;
            }
            scheduleHeavyWeightPaints();
            validateInvalidComponents();
            prePaintDirtyRegions();
        }
    }
    private RepaintManager getDelegate(Component c) {
        RepaintManager delegate = SwingUtilities3.getDelegateRepaintManager(c);
        if (this == delegate) {
            delegate = null;
        }
        return delegate;
    }
}
