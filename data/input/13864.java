public final class AWTAccessor {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private AWTAccessor() {
    }
    public interface ComponentAccessor {
        void setBackgroundEraseDisabled(Component comp, boolean disabled);
        boolean getBackgroundEraseDisabled(Component comp);
        Rectangle getBounds(Component comp);
        void setMixingCutoutShape(Component comp, Shape shape);
        void setGraphicsConfiguration(Component comp, GraphicsConfiguration gc);
        boolean requestFocus(Component comp, CausedFocusEvent.Cause cause);
        boolean canBeFocusOwner(Component comp);
        boolean isVisible(Component comp);
        void setRequestFocusController(RequestFocusController requestController);
        AppContext getAppContext(Component comp);
        void setAppContext(Component comp, AppContext appContext);
        Container getParent(Component comp);
        void setParent(Component comp, Container parent);
        void setSize(Component comp, int width, int height);
        Point getLocation(Component comp);
        void setLocation(Component comp, int x, int y);
        boolean isEnabled(Component comp);
        boolean isDisplayable(Component comp);
        Cursor getCursor(Component comp);
        ComponentPeer getPeer(Component comp);
        void setPeer(Component comp, ComponentPeer peer);
        boolean isLightweight(Component comp);
        boolean getIgnoreRepaint(Component comp);
        int getWidth(Component comp);
        int getHeight(Component comp);
        int getX(Component comp);
        int getY(Component comp);
        Color getForeground(Component comp);
        Color getBackground(Component comp);
        void setBackground(Component comp, Color background);
        Font getFont(Component comp);
        void processEvent(Component comp, AWTEvent e);
        AccessControlContext getAccessControlContext(Component comp);
    }
    public interface ContainerAccessor {
        void validateUnconditionally(Container cont);
    }
    public interface WindowAccessor {
        float getOpacity(Window window);
        void setOpacity(Window window, float opacity);
        Shape getShape(Window window);
        void setShape(Window window, Shape shape);
        void setOpaque(Window window, boolean isOpaque);
        void updateWindow(Window window);
        Dimension getSecurityWarningSize(Window w);
        void setSecurityWarningSize(Window w, int width, int height);
        void setSecurityWarningPosition(Window w, Point2D point,
                float alignmentX, float alignmentY);
        Point2D calculateSecurityWarningPosition(Window window,
                double x, double y, double w, double h);
        void setLWRequestStatus(Window changed, boolean status);
        boolean isAutoRequestFocus(Window w);
        boolean isTrayIconWindow(Window w);
        void setTrayIconWindow(Window w, boolean isTrayIconWindow);
    }
    public interface AWTEventAccessor {
        void setPosted(AWTEvent ev);
        void setSystemGenerated(AWTEvent ev);
        boolean isSystemGenerated(AWTEvent ev);
        AccessControlContext getAccessControlContext(AWTEvent ev);
    }
    public interface InputEventAccessor {
        int[] getButtonDownMasks();
    }
    public interface FrameAccessor {
        void setExtendedState(Frame frame, int state);
       int getExtendedState(Frame frame);
       Rectangle getMaximizedBounds(Frame frame);
    }
    public interface KeyboardFocusManagerAccessor {
        int shouldNativelyFocusHeavyweight(Component heavyweight,
                                           Component descendant,
                                           boolean temporary,
                                           boolean focusedWindowChangeAllowed,
                                           long time,
                                           CausedFocusEvent.Cause cause);
        boolean processSynchronousLightweightTransfer(Component heavyweight,
                                                      Component descendant,
                                                      boolean temporary,
                                                      boolean focusedWindowChangeAllowed,
                                                      long time);
        void removeLastFocusRequest(Component heavyweight);
        void setMostRecentFocusOwner(Window window, Component component);
    }
    public interface MenuComponentAccessor {
        AppContext getAppContext(MenuComponent menuComp);
        void setAppContext(MenuComponent menuComp, AppContext appContext);
        MenuContainer getParent(MenuComponent menuComp);
    }
    public interface EventQueueAccessor {
        Thread getDispatchThread(EventQueue eventQueue);
        public boolean isDispatchThreadImpl(EventQueue eventQueue);
    }
    public interface PopupMenuAccessor {
        boolean isTrayIconPopup(PopupMenu popupMenu);
    }
    public interface FileDialogAccessor {
        void setFiles(FileDialog fileDialog, String directory, String files[]);
        void setFile(FileDialog fileDialog, String file);
        void setDirectory(FileDialog fileDialog, String directory);
        boolean isMultipleMode(FileDialog fileDialog);
    }
    private static ComponentAccessor componentAccessor;
    private static ContainerAccessor containerAccessor;
    private static WindowAccessor windowAccessor;
    private static AWTEventAccessor awtEventAccessor;
    private static InputEventAccessor inputEventAccessor;
    private static FrameAccessor frameAccessor;
    private static KeyboardFocusManagerAccessor kfmAccessor;
    private static MenuComponentAccessor menuComponentAccessor;
    private static EventQueueAccessor eventQueueAccessor;
    private static PopupMenuAccessor popupMenuAccessor;
    private static FileDialogAccessor fileDialogAccessor;
    public static void setComponentAccessor(ComponentAccessor ca) {
        componentAccessor = ca;
    }
    public static ComponentAccessor getComponentAccessor() {
        if (componentAccessor == null) {
            unsafe.ensureClassInitialized(Component.class);
        }
        return componentAccessor;
    }
    public static void setContainerAccessor(ContainerAccessor ca) {
        containerAccessor = ca;
    }
    public static ContainerAccessor getContainerAccessor() {
        if (containerAccessor == null) {
            unsafe.ensureClassInitialized(Container.class);
        }
        return containerAccessor;
    }
    public static void setWindowAccessor(WindowAccessor wa) {
        windowAccessor = wa;
    }
    public static WindowAccessor getWindowAccessor() {
        if (windowAccessor == null) {
            unsafe.ensureClassInitialized(Window.class);
        }
        return windowAccessor;
    }
    public static void setAWTEventAccessor(AWTEventAccessor aea) {
        awtEventAccessor = aea;
    }
    public static AWTEventAccessor getAWTEventAccessor() {
        if (awtEventAccessor == null) {
            unsafe.ensureClassInitialized(AWTEvent.class);
        }
        return awtEventAccessor;
    }
    public static void setInputEventAccessor(InputEventAccessor iea) {
        inputEventAccessor = iea;
    }
    public static InputEventAccessor getInputEventAccessor() {
        if (inputEventAccessor == null) {
            unsafe.ensureClassInitialized(InputEvent.class);
        }
        return inputEventAccessor;
    }
    public static void setFrameAccessor(FrameAccessor fa) {
        frameAccessor = fa;
    }
    public static FrameAccessor getFrameAccessor() {
        if (frameAccessor == null) {
            unsafe.ensureClassInitialized(Frame.class);
        }
        return frameAccessor;
    }
    public static void setKeyboardFocusManagerAccessor(KeyboardFocusManagerAccessor kfma) {
        kfmAccessor = kfma;
    }
    public static KeyboardFocusManagerAccessor getKeyboardFocusManagerAccessor() {
        if (kfmAccessor == null) {
            unsafe.ensureClassInitialized(KeyboardFocusManager.class);
        }
        return kfmAccessor;
    }
    public static void setMenuComponentAccessor(MenuComponentAccessor mca) {
        menuComponentAccessor = mca;
    }
    public static MenuComponentAccessor getMenuComponentAccessor() {
        if (menuComponentAccessor == null) {
            unsafe.ensureClassInitialized(MenuComponent.class);
        }
        return menuComponentAccessor;
    }
    public static void setEventQueueAccessor(EventQueueAccessor eqa) {
        eventQueueAccessor = eqa;
    }
    public static EventQueueAccessor getEventQueueAccessor() {
        if (eventQueueAccessor == null) {
            unsafe.ensureClassInitialized(EventQueue.class);
        }
        return eventQueueAccessor;
    }
    public static void setPopupMenuAccessor(PopupMenuAccessor pma) {
        popupMenuAccessor = pma;
    }
    public static PopupMenuAccessor getPopupMenuAccessor() {
        if (popupMenuAccessor == null) {
            unsafe.ensureClassInitialized(PopupMenu.class);
        }
        return popupMenuAccessor;
    }
    public static void setFileDialogAccessor(FileDialogAccessor fda) {
        fileDialogAccessor = fda;
    }
    public static FileDialogAccessor getFileDialogAccessor() {
        if (fileDialogAccessor == null) {
            unsafe.ensureClassInitialized(FileDialog.class);
        }
        return fileDialogAccessor;
    }
}
