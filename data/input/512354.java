class Dispatcher {
    final MouseGrabManager mouseGrabManager = new MouseGrabManager();
    final MouseDispatcher mouseDispatcher;
    private final ComponentDispatcher componentDispatcher = new ComponentDispatcher();
    private final KeyDispatcher keyDispatcher = new KeyDispatcher();
    private final Toolkit toolkit;
    int clickInterval = 250;
    Dispatcher(Toolkit toolkit) {
        this.toolkit = toolkit;
        mouseDispatcher = new MouseDispatcher(mouseGrabManager, toolkit);
    }
    public boolean onEvent(NativeEvent event) {
        int eventId = event.getEventId();
        if (eventId == NativeEvent.ID_CREATED) {
            return toolkit.onWindowCreated(event.getWindowId());
        } else if (eventId == NativeEvent.ID_MOUSE_GRAB_CANCELED) {
            return mouseGrabManager.onGrabCanceled();
        } else {
            Component src = toolkit.getComponentById(event.getWindowId());
            if (src != null) {
                if (((eventId >= ComponentEvent.COMPONENT_FIRST) && (eventId <= ComponentEvent.COMPONENT_LAST))
                        || ((eventId >= WindowEvent.WINDOW_FIRST) && (eventId <= WindowEvent.WINDOW_LAST))
                        || (eventId == NativeEvent.ID_INSETS_CHANGED)
                        || (eventId == NativeEvent.ID_BOUNDS_CHANGED)
                        || (eventId == NativeEvent.ID_THEME_CHANGED)) {
                    return componentDispatcher.dispatch(src, event);
                } else if ((eventId >= MouseEvent.MOUSE_FIRST)
                        && (eventId <= MouseEvent.MOUSE_LAST)) {
                    return mouseDispatcher.dispatch(src, event);
                } else if (eventId == PaintEvent.PAINT) {
                    return true;
                }
            }
            if ((eventId >= FocusEvent.FOCUS_FIRST)
                    && (eventId <= FocusEvent.FOCUS_LAST)) {
                return false;
            } else if ((eventId >= KeyEvent.KEY_FIRST)
                    && (eventId <= KeyEvent.KEY_LAST)) {
                return keyDispatcher.dispatch(src, event);
            }
        }
        return false;
    }
    final class ComponentDispatcher {
        boolean dispatch(Component src, NativeEvent event) {
            int id = event.getEventId();
            if ((id == NativeEvent.ID_INSETS_CHANGED)
                    || (id == NativeEvent.ID_THEME_CHANGED)) {
                return dispatchInsets(event, src);
            } else if ((id >= WindowEvent.WINDOW_FIRST)
                    && (id <= WindowEvent.WINDOW_LAST)) {
                return dispatchWindow(event, src);
            } else {
                return dispatchPureComponent(event, src);
            }
        }
        boolean dispatchInsets(NativeEvent event, Component src) {
            return false;
        }
        boolean dispatchWindow(NativeEvent event, Component src) {
            return false;
        }
        private boolean dispatchPureComponent(NativeEvent event, Component src) {
            Rectangle rect = event.getWindowRect();
            Point loc = rect.getLocation();
            int mask;
            switch (event.getEventId()) {
            case NativeEvent.ID_BOUNDS_CHANGED:
                mask = 0;
                break;
            case ComponentEvent.COMPONENT_MOVED:
                mask = NativeWindow.BOUNDS_NOSIZE;
                break;
            case ComponentEvent.COMPONENT_RESIZED:
                mask = NativeWindow.BOUNDS_NOMOVE;
                break;
            default:
                throw new RuntimeException(Messages.getString("awt.12E")); 
            }
            return false;
        }
    }
    final class KeyDispatcher {
        boolean dispatch(Component src, NativeEvent event) {
            int id = event.getEventId();
            int modifiers = event.getInputModifiers();
            int location = event.getKeyLocation();
            int code = event.getVKey();
            StringBuffer chars = event.getKeyChars();
            int charsLength = chars.length();
            long time = event.getTime();
            char keyChar = event.getLastChar();
            EventQueue eventQueue = toolkit.getSystemEventQueueImpl();
            if (src != null) {
                eventQueue.postEvent(new KeyEvent(src, id, time, modifiers,
                        code, keyChar, location));
                if (id == KeyEvent.KEY_PRESSED) {
                    for (int i = 0; i < charsLength; i++) {
                        keyChar = chars.charAt(i);
                        if (keyChar != KeyEvent.CHAR_UNDEFINED) {
                            eventQueue.postEvent(new KeyEvent(src,
                                    KeyEvent.KEY_TYPED, time, modifiers,
                                    KeyEvent.VK_UNDEFINED, keyChar,
                                    KeyEvent.KEY_LOCATION_UNKNOWN));
                        }
                    }
                }
            }
            return false;
        }
    }
    static final class MouseGrabManager {
        private Component syntheticGrabOwner = null;
        private Component lastSyntheticGrabOwner = null;
        private int syntheticGrabDepth = 0;
        private Runnable whenCanceled;
        void endGrab() {
        }
        boolean onGrabCanceled() {
            endGrab();
            resetSyntheticGrab();
            return false;
        }
        Component onMousePressed(Component source) {
            if (syntheticGrabDepth == 0) {
                syntheticGrabOwner = source;
                lastSyntheticGrabOwner = source;
            }
            syntheticGrabDepth++;
            return syntheticGrabOwner;
        }
        Component onMouseReleased(Component source) {
            Component ret = source;
            syntheticGrabDepth--;
            if (syntheticGrabDepth <= 0) {
                resetSyntheticGrab();
                lastSyntheticGrabOwner = null;
            }
            return ret;
        }
        void preprocessEvent(NativeEvent event) {
            int id = event.getEventId();
            switch (id) {
            case MouseEvent.MOUSE_MOVED:
                if (syntheticGrabOwner != null) {
                    syntheticGrabOwner = null;
                    syntheticGrabDepth = 0;
                }
                if (lastSyntheticGrabOwner != null) {
                    lastSyntheticGrabOwner = null;
                }
            case MouseEvent.MOUSE_DRAGGED:
                if (syntheticGrabOwner == null
                        && lastSyntheticGrabOwner != null) {
                    syntheticGrabOwner = lastSyntheticGrabOwner;
                    syntheticGrabDepth = 0;
                    int mask = event.getInputModifiers();
                    syntheticGrabDepth += (mask & InputEvent.BUTTON1_DOWN_MASK) != 0 ? 1
                            : 0;
                    syntheticGrabDepth += (mask & InputEvent.BUTTON2_DOWN_MASK) != 0 ? 1
                            : 0;
                    syntheticGrabDepth += (mask & InputEvent.BUTTON3_DOWN_MASK) != 0 ? 1
                            : 0;
                }
            }
        }
        Component getSyntheticGrabOwner() {
            return syntheticGrabOwner;
        }
        private void resetSyntheticGrab() {
            syntheticGrabOwner = null;
            syntheticGrabDepth = 0;
        }
    }
}