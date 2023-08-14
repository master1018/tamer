public class PopupFactory {
    private static final Object SharedInstanceKey =
        new StringBuffer("PopupFactory.SharedInstanceKey");
    private static final int MAX_CACHE_SIZE = 5;
    static final int LIGHT_WEIGHT_POPUP   = 0;
    static final int MEDIUM_WEIGHT_POPUP  = 1;
    static final int HEAVY_WEIGHT_POPUP   = 2;
    private int popupType = LIGHT_WEIGHT_POPUP;
    public static void setSharedInstance(PopupFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("PopupFactory can not be null");
        }
        SwingUtilities.appContextPut(SharedInstanceKey, factory);
    }
    public static PopupFactory getSharedInstance() {
        PopupFactory factory = (PopupFactory)SwingUtilities.appContextGet(
                         SharedInstanceKey);
        if (factory == null) {
            factory = new PopupFactory();
            setSharedInstance(factory);
        }
        return factory;
    }
    void setPopupType(int type) {
        popupType = type;
    }
    int getPopupType() {
        return popupType;
    }
    public Popup getPopup(Component owner, Component contents,
                          int x, int y) throws IllegalArgumentException {
        if (contents == null) {
            throw new IllegalArgumentException(
                          "Popup.getPopup must be passed non-null contents");
        }
        int popupType = getPopupType(owner, contents, x, y);
        Popup popup = getPopup(owner, contents, x, y, popupType);
        if (popup == null) {
            popup = getPopup(owner, contents, x, y, HEAVY_WEIGHT_POPUP);
        }
        return popup;
    }
    private int getPopupType(Component owner, Component contents,
                             int ownerX, int ownerY) {
        int popupType = getPopupType();
        if (owner == null || invokerInHeavyWeightPopup(owner)) {
            popupType = HEAVY_WEIGHT_POPUP;
        }
        else if (popupType == LIGHT_WEIGHT_POPUP &&
                 !(contents instanceof JToolTip) &&
                 !(contents instanceof JPopupMenu)) {
            popupType = MEDIUM_WEIGHT_POPUP;
        }
        Component c = owner;
        while (c != null) {
            if (c instanceof JComponent) {
                if (((JComponent)c).getClientProperty(
                            PopupFactory_FORCE_HEAVYWEIGHT_POPUP) == Boolean.TRUE) {
                    popupType = HEAVY_WEIGHT_POPUP;
                    break;
                }
            } else if (c instanceof Window) {
                Window w = (Window) c;
                if (!w.isOpaque() || w.getOpacity() < 1 || w.getShape() != null) {
                    popupType = HEAVY_WEIGHT_POPUP;
                    break;
                }
            }
            c = c.getParent();
        }
        return popupType;
    }
    private Popup getPopup(Component owner, Component contents,
                           int ownerX, int ownerY, int popupType) {
        if (GraphicsEnvironment.isHeadless()) {
            return getHeadlessPopup(owner, contents, ownerX, ownerY);
        }
        switch(popupType) {
        case LIGHT_WEIGHT_POPUP:
            return getLightWeightPopup(owner, contents, ownerX, ownerY);
        case MEDIUM_WEIGHT_POPUP:
            return getMediumWeightPopup(owner, contents, ownerX, ownerY);
        case HEAVY_WEIGHT_POPUP:
            return getHeavyWeightPopup(owner, contents, ownerX, ownerY);
        }
        return null;
    }
    private Popup getHeadlessPopup(Component owner, Component contents,
                                   int ownerX, int ownerY) {
        return HeadlessPopup.getHeadlessPopup(owner, contents, ownerX, ownerY);
    }
    private Popup getLightWeightPopup(Component owner, Component contents,
                                         int ownerX, int ownerY) {
        return LightWeightPopup.getLightWeightPopup(owner, contents, ownerX,
                                                    ownerY);
    }
    private Popup getMediumWeightPopup(Component owner, Component contents,
                                          int ownerX, int ownerY) {
        return MediumWeightPopup.getMediumWeightPopup(owner, contents,
                                                      ownerX, ownerY);
    }
    private Popup getHeavyWeightPopup(Component owner, Component contents,
                                         int ownerX, int ownerY) {
        if (GraphicsEnvironment.isHeadless()) {
            return getMediumWeightPopup(owner, contents, ownerX, ownerY);
        }
        return HeavyWeightPopup.getHeavyWeightPopup(owner, contents, ownerX,
                                                    ownerY);
    }
    private boolean invokerInHeavyWeightPopup(Component i) {
        if (i != null) {
            Container parent;
            for(parent = i.getParent() ; parent != null ; parent =
                    parent.getParent()) {
                if (parent instanceof Popup.HeavyWeightWindow) {
                    return true;
                }
            }
        }
        return false;
    }
    private static class HeavyWeightPopup extends Popup {
        private static final Object heavyWeightPopupCacheKey =
                 new StringBuffer("PopupFactory.heavyWeightPopupCache");
        static Popup getHeavyWeightPopup(Component owner, Component contents,
                                         int ownerX, int ownerY) {
            Window window = (owner != null) ? SwingUtilities.
                              getWindowAncestor(owner) : null;
            HeavyWeightPopup popup = null;
            if (window != null) {
                popup = getRecycledHeavyWeightPopup(window);
            }
            boolean focusPopup = false;
            if(contents != null && contents.isFocusable()) {
                if(contents instanceof JPopupMenu) {
                    JPopupMenu jpm = (JPopupMenu) contents;
                    Component popComps[] = jpm.getComponents();
                    for (Component popComp : popComps) {
                        if (!(popComp instanceof MenuElement) &&
                                !(popComp instanceof JSeparator)) {
                            focusPopup = true;
                            break;
                        }
                    }
                }
            }
            if (popup == null ||
                ((JWindow) popup.getComponent())
                 .getFocusableWindowState() != focusPopup) {
                if(popup != null) {
                    popup._dispose();
                }
                popup = new HeavyWeightPopup();
            }
            popup.reset(owner, contents, ownerX, ownerY);
            if(focusPopup) {
                JWindow wnd = (JWindow) popup.getComponent();
                wnd.setFocusableWindowState(true);
                wnd.setName("###focusableSwingPopup###");
            }
            return popup;
        }
        private static HeavyWeightPopup getRecycledHeavyWeightPopup(Window w) {
            synchronized (HeavyWeightPopup.class) {
                List<HeavyWeightPopup> cache;
                Map<Window, List<HeavyWeightPopup>> heavyPopupCache = getHeavyWeightPopupCache();
                if (heavyPopupCache.containsKey(w)) {
                    cache = heavyPopupCache.get(w);
                } else {
                    return null;
                }
                if (cache.size() > 0) {
                    HeavyWeightPopup r = cache.get(0);
                    cache.remove(0);
                    return r;
                }
                return null;
            }
        }
        private static Map<Window, List<HeavyWeightPopup>> getHeavyWeightPopupCache() {
            synchronized (HeavyWeightPopup.class) {
                Map<Window, List<HeavyWeightPopup>> cache = (Map<Window, List<HeavyWeightPopup>>)SwingUtilities.appContextGet(
                                  heavyWeightPopupCacheKey);
                if (cache == null) {
                    cache = new HashMap<Window, List<HeavyWeightPopup>>(2);
                    SwingUtilities.appContextPut(heavyWeightPopupCacheKey,
                                                 cache);
                }
                return cache;
            }
        }
        private static void recycleHeavyWeightPopup(HeavyWeightPopup popup) {
            synchronized (HeavyWeightPopup.class) {
                List<HeavyWeightPopup> cache;
                Window window = SwingUtilities.getWindowAncestor(
                                     popup.getComponent());
                Map<Window, List<HeavyWeightPopup>> heavyPopupCache = getHeavyWeightPopupCache();
                if (window instanceof Popup.DefaultFrame ||
                                      !window.isVisible()) {
                    popup._dispose();
                    return;
                } else if (heavyPopupCache.containsKey(window)) {
                    cache = heavyPopupCache.get(window);
                } else {
                    cache = new ArrayList<HeavyWeightPopup>();
                    heavyPopupCache.put(window, cache);
                    final Window w = window;
                    w.addWindowListener(new WindowAdapter() {
                        public void windowClosed(WindowEvent e) {
                            List<HeavyWeightPopup> popups;
                            synchronized(HeavyWeightPopup.class) {
                                Map<Window, List<HeavyWeightPopup>> heavyPopupCache2 =
                                              getHeavyWeightPopupCache();
                                popups = heavyPopupCache2.remove(w);
                            }
                            if (popups != null) {
                                for (int counter = popups.size() - 1;
                                                   counter >= 0; counter--) {
                                    popups.get(counter)._dispose();
                                }
                            }
                        }
                    });
                }
                if(cache.size() < MAX_CACHE_SIZE) {
                    cache.add(popup);
                } else {
                    popup._dispose();
                }
            }
        }
        public void hide() {
            super.hide();
            recycleHeavyWeightPopup(this);
        }
        void dispose() {
        }
        void _dispose() {
            super.dispose();
        }
    }
    private static class ContainerPopup extends Popup {
        Component owner;
        int x;
        int y;
        public void hide() {
            Component component = getComponent();
            if (component != null) {
                Container parent = component.getParent();
                if (parent != null) {
                    Rectangle bounds = component.getBounds();
                    parent.remove(component);
                    parent.repaint(bounds.x, bounds.y, bounds.width,
                                   bounds.height);
                }
            }
            owner = null;
        }
        public void pack() {
            Component component = getComponent();
            if (component != null) {
                component.setSize(component.getPreferredSize());
            }
        }
        void reset(Component owner, Component contents, int ownerX,
                   int ownerY) {
            if ((owner instanceof JFrame) || (owner instanceof JDialog) ||
                                                 (owner instanceof JWindow)) {
                owner = ((RootPaneContainer)owner).getLayeredPane();
            }
            super.reset(owner, contents, ownerX, ownerY);
            x = ownerX;
            y = ownerY;
            this.owner = owner;
        }
        boolean overlappedByOwnedWindow() {
            Component component = getComponent();
            if(owner != null && component != null) {
                Window w = SwingUtilities.getWindowAncestor(owner);
                if (w == null) {
                    return false;
                }
                Window[] ownedWindows = w.getOwnedWindows();
                if(ownedWindows != null) {
                    Rectangle bnd = component.getBounds();
                    for (Window window : ownedWindows) {
                        if (window.isVisible() &&
                                bnd.intersects(window.getBounds())) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        boolean fitsOnScreen() {
            boolean result = false;
            Component component = getComponent();
            if (owner != null && component != null) {
                int popupWidth = component.getWidth();
                int popupHeight = component.getHeight();
                Container parent = (Container) SwingUtilities.getRoot(owner);
                if (parent instanceof JFrame ||
                    parent instanceof JDialog ||
                    parent instanceof JWindow) {
                    Rectangle parentBounds = parent.getBounds();
                    Insets i = parent.getInsets();
                    parentBounds.x += i.left;
                    parentBounds.y += i.top;
                    parentBounds.width -= i.left + i.right;
                    parentBounds.height -= i.top + i.bottom;
                    if (JPopupMenu.canPopupOverlapTaskBar()) {
                        GraphicsConfiguration gc =
                                parent.getGraphicsConfiguration();
                        Rectangle popupArea = getContainerPopupArea(gc);
                        result = parentBounds.intersection(popupArea)
                                .contains(x, y, popupWidth, popupHeight);
                    } else {
                        result = parentBounds
                                .contains(x, y, popupWidth, popupHeight);
                    }
                } else if (parent instanceof JApplet) {
                    Rectangle parentBounds = parent.getBounds();
                    Point p = parent.getLocationOnScreen();
                    parentBounds.x = p.x;
                    parentBounds.y = p.y;
                    result = parentBounds.contains(x, y, popupWidth, popupHeight);
                }
            }
            return result;
        }
        Rectangle getContainerPopupArea(GraphicsConfiguration gc) {
            Rectangle screenBounds;
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Insets insets;
            if(gc != null) {
                screenBounds = gc.getBounds();
                insets = toolkit.getScreenInsets(gc);
            } else {
                screenBounds = new Rectangle(toolkit.getScreenSize());
                insets = new Insets(0, 0, 0, 0);
            }
            screenBounds.x += insets.left;
            screenBounds.y += insets.top;
            screenBounds.width -= (insets.left + insets.right);
            screenBounds.height -= (insets.top + insets.bottom);
            return screenBounds;
        }
    }
    private static class HeadlessPopup extends ContainerPopup {
        static Popup getHeadlessPopup(Component owner, Component contents,
                                      int ownerX, int ownerY) {
            HeadlessPopup popup = new HeadlessPopup();
            popup.reset(owner, contents, ownerX, ownerY);
            return popup;
        }
        Component createComponent(Component owner) {
            return new Panel(new BorderLayout());
        }
        public void show() {
        }
        public void hide() {
        }
    }
    private static class LightWeightPopup extends ContainerPopup {
        private static final Object lightWeightPopupCacheKey =
                         new StringBuffer("PopupFactory.lightPopupCache");
        static Popup getLightWeightPopup(Component owner, Component contents,
                                         int ownerX, int ownerY) {
            LightWeightPopup popup = getRecycledLightWeightPopup();
            if (popup == null) {
                popup = new LightWeightPopup();
            }
            popup.reset(owner, contents, ownerX, ownerY);
            if (!popup.fitsOnScreen() ||
                 popup.overlappedByOwnedWindow()) {
                popup.hide();
                return null;
            }
            return popup;
        }
        private static List<LightWeightPopup> getLightWeightPopupCache() {
            List<LightWeightPopup> cache = (List<LightWeightPopup>)SwingUtilities.appContextGet(
                                   lightWeightPopupCacheKey);
            if (cache == null) {
                cache = new ArrayList<LightWeightPopup>();
                SwingUtilities.appContextPut(lightWeightPopupCacheKey, cache);
            }
            return cache;
        }
        private static void recycleLightWeightPopup(LightWeightPopup popup) {
            synchronized (LightWeightPopup.class) {
                List<LightWeightPopup> lightPopupCache = getLightWeightPopupCache();
                if (lightPopupCache.size() < MAX_CACHE_SIZE) {
                    lightPopupCache.add(popup);
                }
            }
        }
        private static LightWeightPopup getRecycledLightWeightPopup() {
            synchronized (LightWeightPopup.class) {
                List<LightWeightPopup> lightPopupCache = getLightWeightPopupCache();
                if (lightPopupCache.size() > 0) {
                    LightWeightPopup r = lightPopupCache.get(0);
                    lightPopupCache.remove(0);
                    return r;
                }
                return null;
            }
        }
        public void hide() {
            super.hide();
            Container component = (Container)getComponent();
            component.removeAll();
            recycleLightWeightPopup(this);
        }
        public void show() {
            Container parent = null;
            if (owner != null) {
                parent = (owner instanceof Container? (Container)owner : owner.getParent());
            }
            for (Container p = parent; p != null; p = p.getParent()) {
                if (p instanceof JRootPane) {
                    if (p.getParent() instanceof JInternalFrame) {
                        continue;
                    }
                    parent = ((JRootPane)p).getLayeredPane();
                } else if(p instanceof Window) {
                    if (parent == null) {
                        parent = p;
                    }
                    break;
                } else if (p instanceof JApplet) {
                    break;
                }
            }
            Point p = SwingUtilities.convertScreenLocationToParent(parent, x,
                                                                   y);
            Component component = getComponent();
            component.setLocation(p.x, p.y);
            if (parent instanceof JLayeredPane) {
                parent.add(component, JLayeredPane.POPUP_LAYER, 0);
            } else {
                parent.add(component);
            }
        }
        Component createComponent(Component owner) {
            JComponent component = new JPanel(new BorderLayout(), true);
            component.setOpaque(true);
            return component;
        }
        void reset(Component owner, Component contents, int ownerX,
                   int ownerY) {
            super.reset(owner, contents, ownerX, ownerY);
            JComponent component = (JComponent)getComponent();
            component.setOpaque(contents.isOpaque());
            component.setLocation(ownerX, ownerY);
            component.add(contents, BorderLayout.CENTER);
            contents.invalidate();
            pack();
        }
    }
    private static class MediumWeightPopup extends ContainerPopup {
        private static final Object mediumWeightPopupCacheKey =
                             new StringBuffer("PopupFactory.mediumPopupCache");
        private JRootPane rootPane;
        static Popup getMediumWeightPopup(Component owner, Component contents,
                                          int ownerX, int ownerY) {
            MediumWeightPopup popup = getRecycledMediumWeightPopup();
            if (popup == null) {
                popup = new MediumWeightPopup();
            }
            popup.reset(owner, contents, ownerX, ownerY);
            if (!popup.fitsOnScreen() ||
                 popup.overlappedByOwnedWindow()) {
                popup.hide();
                return null;
            }
            return popup;
        }
        private static List<MediumWeightPopup> getMediumWeightPopupCache() {
            List<MediumWeightPopup> cache = (List<MediumWeightPopup>)SwingUtilities.appContextGet(
                                    mediumWeightPopupCacheKey);
            if (cache == null) {
                cache = new ArrayList<MediumWeightPopup>();
                SwingUtilities.appContextPut(mediumWeightPopupCacheKey, cache);
            }
            return cache;
        }
        private static void recycleMediumWeightPopup(MediumWeightPopup popup) {
            synchronized (MediumWeightPopup.class) {
                List<MediumWeightPopup> mediumPopupCache = getMediumWeightPopupCache();
                if (mediumPopupCache.size() < MAX_CACHE_SIZE) {
                    mediumPopupCache.add(popup);
                }
            }
        }
        private static MediumWeightPopup getRecycledMediumWeightPopup() {
            synchronized (MediumWeightPopup.class) {
                List<MediumWeightPopup> mediumPopupCache = getMediumWeightPopupCache();
                if (mediumPopupCache.size() > 0) {
                    MediumWeightPopup r = mediumPopupCache.get(0);
                    mediumPopupCache.remove(0);
                    return r;
                }
                return null;
            }
        }
        public void hide() {
            super.hide();
            rootPane.getContentPane().removeAll();
            recycleMediumWeightPopup(this);
        }
        public void show() {
            Component component = getComponent();
            Container parent = null;
            if (owner != null) {
                parent = owner.getParent();
            }
            while (!(parent instanceof Window || parent instanceof Applet) &&
                   (parent!=null)) {
                parent = parent.getParent();
            }
            if (parent instanceof RootPaneContainer) {
                parent = ((RootPaneContainer)parent).getLayeredPane();
                Point p = SwingUtilities.convertScreenLocationToParent(parent,
                                                                       x, y);
                component.setVisible(false);
                component.setLocation(p.x, p.y);
                parent.add(component, JLayeredPane.POPUP_LAYER,
                                           0);
            } else {
                Point p = SwingUtilities.convertScreenLocationToParent(parent,
                                                                       x, y);
                component.setLocation(p.x, p.y);
                component.setVisible(false);
                parent.add(component);
            }
            component.setVisible(true);
        }
        Component createComponent(Component owner) {
            Panel component = new MediumWeightComponent();
            rootPane = new JRootPane();
            rootPane.setOpaque(true);
            component.add(rootPane, BorderLayout.CENTER);
            return component;
        }
        void reset(Component owner, Component contents, int ownerX,
                   int ownerY) {
            super.reset(owner, contents, ownerX, ownerY);
            Component component = getComponent();
            component.setLocation(ownerX, ownerY);
            rootPane.getContentPane().add(contents, BorderLayout.CENTER);
            contents.invalidate();
            component.validate();
            pack();
        }
        private static class MediumWeightComponent extends Panel implements
                                                           SwingHeavyWeight {
            MediumWeightComponent() {
                super(new BorderLayout());
            }
        }
    }
}
