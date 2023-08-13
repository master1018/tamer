class MouseDispatcher {
    private static final int clickDelta = 5;
    private final long[] lastPressTime = new long[] {0l, 0l, 0l};
    private final Point[] lastPressPos = new Point[] {null, null, null};
    private final boolean[] buttonPressed = new boolean[] {false, false, false};
    private final int[] clickCount = new int[] {0, 0, 0};
    private Component lastUnderPointer = null;
    private final Point lastScreenPos = new Point(-1, -1);
    private Component lastUnderMotion = null;
    private Point lastLocalPos = new Point(-1, -1);
    private final MouseGrabManager mouseGrabManager;
    private final Toolkit toolkit;
    static Point convertPoint(Component src, int x, int y, Component dest) {
        Point srcPoint = getAbsLocation(src);
        Point destPoint = getAbsLocation(dest);
        return new Point(x + (srcPoint.x - destPoint.x),
                         y + (srcPoint.y - destPoint.y));
    }
    static Point convertPoint(Component src, Point p, Component dst) {
        return convertPoint(src, p.x, p.y, dst);
    }
    private static Point getAbsLocation(Component comp) {
        Point location = new Point(0, 0);
        return location;
    }
    MouseDispatcher(MouseGrabManager mouseGrabManager,
                    Toolkit toolkit) {
        this.mouseGrabManager = mouseGrabManager;
        this.toolkit = toolkit;
    }
    Point getPointerPos() {
        return lastScreenPos;
    }
    boolean dispatch(Component src, NativeEvent event) {
        int id = event.getEventId();
        lastScreenPos.setLocation(event.getScreenPos());
        checkMouseEnterExit(event.getInputModifiers(), event.getTime());
        if (id == MouseEvent.MOUSE_WHEEL) {
        } else if ((id != MouseEvent.MOUSE_ENTERED) &&
                   (id != MouseEvent.MOUSE_EXITED)) {
            PointerInfo info = new PointerInfo(src, event.getLocalPos());
            mouseGrabManager.preprocessEvent(event);
            findEventSource(info);
            if ((id == MouseEvent.MOUSE_PRESSED) ||
                (id == MouseEvent.MOUSE_RELEASED)) {
                dispatchButtonEvent(info, event);
            } else if ((id == MouseEvent.MOUSE_MOVED) ||
                       (id == MouseEvent.MOUSE_DRAGGED)) {
                dispatchMotionEvent(info, event);
            }
        }
        return false;
    }
    private void checkMouseEnterExit(int modifiers, long when) {
    }
    private void setCursor(Component comp) {
        if (comp == null) {
            return;
        }
        Component grabOwner = mouseGrabManager.getSyntheticGrabOwner();
        Component cursorComp = ((grabOwner != null) &&
                                 grabOwner.isShowing() ? grabOwner : comp);
        cursorComp.setCursor();
    }
    private void postMouseEnterExit(int id, int mod, long when,
                                    int x, int y, Component comp) {
        if (comp.isIndirectlyEnabled()) {
            toolkit.getSystemEventQueueImpl().postEvent(
                    new MouseEvent(comp, id, when, mod, x, y, 0, false));
            comp.setMouseExitedExpected(id == MouseEvent.MOUSE_ENTERED);
        } else {
            comp.setMouseExitedExpected(false);
        }
    }
    private void findEventSource(PointerInfo info) {
        Component grabOwner = mouseGrabManager.getSyntheticGrabOwner();
        if (grabOwner != null && grabOwner.isShowing()) {
            info.position = convertPoint(info.src, info.position, grabOwner);
            info.src = grabOwner;
        } else {
        }
    }
    private void dispatchButtonEvent(PointerInfo info, NativeEvent event) {
        int button = event.getMouseButton();
        long time = event.getTime();
        int id = event.getEventId();
        int index = button - 1;
        boolean clickRequired = false;
        propagateEvent(info, AWTEvent.MOUSE_EVENT_MASK,
                       MouseListener.class, false);
        if (id == MouseEvent.MOUSE_PRESSED) {
            int clickInterval = toolkit.dispatcher.clickInterval;
            mouseGrabManager.onMousePressed(info.src);
            buttonPressed[index] = true;
            clickCount[index] = (!deltaExceeded(index, info) &&
                    ((time - lastPressTime[index]) <= clickInterval)) ?
                    clickCount[index] + 1 : 1;
            lastPressTime[index] = time;
            lastPressPos[index] = info.position;
        } else {
            mouseGrabManager.onMouseReleased(info.src);
            if (buttonPressed[index]) {
                buttonPressed[index] = false;
                clickRequired = !deltaExceeded(index, info);
            } else {
                clickCount[index] = 0;
            }
        }
        if (info.src.isIndirectlyEnabled()) {
            final Point pos = info.position;
            final int mod = event.getInputModifiers();
            toolkit.getSystemEventQueueImpl().postEvent(
                            new MouseEvent(info.src, id, time, mod, pos.x,
                            pos.y, clickCount[index],
                            event.getTrigger(), button));
            if (clickRequired) {
                toolkit.getSystemEventQueueImpl().postEvent(
                            new MouseEvent(info.src,
                            MouseEvent.MOUSE_CLICKED,
                            time, mod, pos.x, pos.y,
                            clickCount[index], false,
                            button));
            }
        }
    }
    private boolean deltaExceeded(int index, PointerInfo info) {
        final Point lastPos = lastPressPos[index];
        if (lastPos == null) {
            return true;
        }
        return ((Math.abs(lastPos.x - info.position.x) > clickDelta) ||
                (Math.abs(lastPos.y - info.position.y) > clickDelta));
    }
    private void dispatchMotionEvent(PointerInfo info, NativeEvent event) {
        propagateEvent(info, AWTEvent.MOUSE_MOTION_EVENT_MASK,
                       MouseMotionListener.class, false);
        final Point pos = info.position;
        if ((lastUnderMotion != info.src) ||
            !lastLocalPos.equals(pos)) {
            lastUnderMotion = info.src;
            lastLocalPos = pos;
            if (info.src.isIndirectlyEnabled()) {
                toolkit.getSystemEventQueueImpl().postEvent(
                            new MouseEvent(info.src, event.getEventId(),
                            event.getTime(),
                            event.getInputModifiers(),
                            pos.x, pos.y, 0, false));
            }
        }
    }
    MouseWheelEvent createWheelEvent(Component src, NativeEvent event,
                                     Point where) {
        Integer scrollAmountProperty =
            (Integer)toolkit.getDesktopProperty("awt.wheelScrollingSize"); 
        int amount = 1;
        int type = MouseWheelEvent.WHEEL_UNIT_SCROLL;
        if (scrollAmountProperty != null) {
            amount = scrollAmountProperty.intValue();
            if (amount == -1) {
                type = MouseWheelEvent.WHEEL_BLOCK_SCROLL;
                amount = 1;
            }
        }
        return new MouseWheelEvent(src, event.getEventId(),
                event.getTime(), event.getInputModifiers(),
                where.x, where.y, 0, false, type, amount,
                event.getWheelRotation());
    }
    private PointerInfo propagateEvent(PointerInfo info, long mask,
                                       Class<? extends EventListener> type, boolean pierceHW) {
        Component src = info.src;
        while ((src != null) &&
               (src.isLightweight() || pierceHW) &&
              !(src.isMouseEventEnabled(mask) ||
               (src.getListeners(type).length > 0))) {
            info.position.translate(src.x, src.y);
            info.src = src;
        }
        return info;
    }
    private class PointerInfo {
        Component src;
        Point position;
        PointerInfo(Component src, Point position) {
            this.src = src;
            this.position = position;
        }
    }
}
