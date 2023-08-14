public class SwingUtilities3 {
    private static final Object DELEGATE_REPAINT_MANAGER_KEY =
        new StringBuilder("DelegateRepaintManagerKey");
    public static void setDelegateRepaintManager(JComponent component,
                                                RepaintManager repaintManager) {
        AppContext.getAppContext().put(DELEGATE_REPAINT_MANAGER_KEY,
                                       Boolean.TRUE);
        component.putClientProperty(DELEGATE_REPAINT_MANAGER_KEY,
                                    repaintManager);
    }
    private static final Map<Container, Boolean> vsyncedMap =
        Collections.synchronizedMap(new WeakHashMap<Container, Boolean>());
    public static void setVsyncRequested(Container rootContainer,
                                         boolean isRequested) {
        assert (rootContainer instanceof Applet) || (rootContainer instanceof Window);
        if (isRequested) {
            vsyncedMap.put(rootContainer, Boolean.TRUE);
        } else {
            vsyncedMap.remove(rootContainer);
        }
    }
    public static boolean isVsyncRequested(Container rootContainer) {
        assert (rootContainer instanceof Applet) || (rootContainer instanceof Window);
        return Boolean.TRUE == vsyncedMap.get(rootContainer);
    }
    public static RepaintManager getDelegateRepaintManager(Component
                                                            component) {
        RepaintManager delegate = null;
        if (Boolean.TRUE == AppContext.getAppContext().get(
                                               DELEGATE_REPAINT_MANAGER_KEY)) {
            while (delegate == null && component != null) {
                while (component != null
                         && ! (component instanceof JComponent)) {
                    component = component.getParent();
                }
                if (component != null) {
                    delegate = (RepaintManager)
                        ((JComponent) component)
                          .getClientProperty(DELEGATE_REPAINT_MANAGER_KEY);
                    component = component.getParent();
                }
            }
        }
        return delegate;
    }
    public static void setEventQueueDelegate(
            Map<String, Map<String, Object>> map) {
        EventQueueDelegate.setDelegate(new EventQueueDelegateFromMap(map));
    }
    private static class EventQueueDelegateFromMap
    implements EventQueueDelegate.Delegate {
        private final AWTEvent[] afterDispatchEventArgument;
        private final Object[] afterDispatchHandleArgument;
        private final Callable<Void> afterDispatchCallable;
        private final AWTEvent[] beforeDispatchEventArgument;
        private final Callable<Object> beforeDispatchCallable;
        private final EventQueue[] getNextEventEventQueueArgument;
        private final Callable<AWTEvent> getNextEventCallable;
        @SuppressWarnings("unchecked")
        public EventQueueDelegateFromMap(Map<String, Map<String, Object>> objectMap) {
            Map<String, Object> methodMap = objectMap.get("afterDispatch");
            afterDispatchEventArgument = (AWTEvent[]) methodMap.get("event");
            afterDispatchHandleArgument = (Object[]) methodMap.get("handle");
            afterDispatchCallable = (Callable<Void>) methodMap.get("method");
            methodMap = objectMap.get("beforeDispatch");
            beforeDispatchEventArgument = (AWTEvent[]) methodMap.get("event");
            beforeDispatchCallable = (Callable<Object>) methodMap.get("method");
            methodMap = objectMap.get("getNextEvent");
            getNextEventEventQueueArgument =
                (EventQueue[]) methodMap.get("eventQueue");
            getNextEventCallable = (Callable<AWTEvent>) methodMap.get("method");
        }
        @Override
        public void afterDispatch(AWTEvent event, Object handle) throws InterruptedException {
            afterDispatchEventArgument[0] = event;
            afterDispatchHandleArgument[0] = handle;
            try {
                afterDispatchCallable.call();
            } catch (InterruptedException e) {
                throw e;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public Object beforeDispatch(AWTEvent event) throws InterruptedException {
            beforeDispatchEventArgument[0] = event;
            try {
                return beforeDispatchCallable.call();
            } catch (InterruptedException e) {
                throw e;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public AWTEvent getNextEvent(EventQueue eventQueue) throws InterruptedException {
            getNextEventEventQueueArgument[0] = eventQueue;
            try {
                return getNextEventCallable.call();
            } catch (InterruptedException e) {
                throw e;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
