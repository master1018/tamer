final class EventQueueCore {
    private final LinkedList<EventQueue> queueStack = new LinkedList<EventQueue>();
    private final LinkedList<AWTEvent> events = new LinkedList<AWTEvent>();
    private Toolkit toolkit;
    private EventQueue activeQueue;
    private Thread dispatchThread;
    AWTEvent currentEvent;
    long mostRecentEventTime = System.currentTimeMillis();
    EventQueueCore(EventQueue eq) {
        synchronized (this) {
            queueStack.addLast(eq);
            activeQueue = eq;
        }
    }
    EventQueueCore(EventQueue eq, Toolkit t) {
        synchronized (this) {
            queueStack.addLast(eq);
            activeQueue = eq;
            setToolkit(t);
        }
    }
    synchronized long getMostRecentEventTime() {
        return mostRecentEventTime;
    }
    synchronized AWTEvent getCurrentEvent() {
        return currentEvent;
    }
    synchronized boolean isSystemEventQueue() {
        return toolkit != null;
    }
    private void setToolkit(Toolkit t) {
        toolkit = t;
        if (toolkit != null) {
            toolkit.setSystemEventQueueCore(this);
            dispatchThread = toolkit.dispatchThread;
        }
    }
    synchronized void postEvent(AWTEvent event) {
    }
    void notifyEventMonitor(Toolkit t) {
        Object em = t.getNativeEventQueue().getEventMonitor();
        synchronized (em) {
            em.notifyAll();
        }
    }
    synchronized AWTEvent getNextEvent() throws InterruptedException {
        while (events.isEmpty()) {
            wait();
        }
        AWTEvent event = events.removeFirst();
        return event;
    }    
    synchronized AWTEvent peekEvent() {
        return events.isEmpty() ? null : events.getFirst();
    }
    synchronized AWTEvent peekEvent(int id) {
        for (AWTEvent event : events) {
            if (event.getID() == id) {
                return event;
            }
        }
        return null;
    }
    synchronized void dispatchEvent(AWTEvent event) {
        updateCurrentEventAndTime(event);
        try {
            activeQueue.dispatchEvent(event);
        } finally {
            currentEvent = null;
        }
    }
    void dispatchEventImpl(AWTEvent event) {
        if (event instanceof ActiveEvent) {
            updateCurrentEventAndTime(event);
            try {
                ((ActiveEvent) event).dispatch();
            } finally {
                currentEvent = null;
            }
            return;
        }
        Object src = event.getSource();
        if (src instanceof Component) {
            if (preprocessComponentEvent(event)) {
                ((Component) src).dispatchEvent(event);
            }
        } else {
            if (toolkit != null) {
                toolkit.dispatchAWTEvent(event);
            }
            if (src instanceof MenuComponent) {
                ((MenuComponent) src).dispatchEvent(event);
            }
        }
    }
    private final boolean preprocessComponentEvent(AWTEvent event) {
      if (event instanceof MouseEvent) {
          return preprocessMouseEvent((MouseEvent)event);
      }
      return true;
    }
    private final boolean preprocessMouseEvent(MouseEvent event) {
        return true;
    }
    private void updateCurrentEventAndTime(AWTEvent event) {
        currentEvent = event;
        long when = 0;
        if (event instanceof ActionEvent) {
            when = ((ActionEvent) event).getWhen();
        } else if (event instanceof InputEvent) {
            when = ((InputEvent) event).getWhen();
        } else if (event instanceof InputMethodEvent) {
            when = ((InputMethodEvent) event).getWhen();
        } else if (event instanceof InvocationEvent) {
            when = ((InvocationEvent) event).getWhen();
        }
        if (when != 0) {
            mostRecentEventTime = when;
        }
    }
    synchronized void push(EventQueue newEventQueue) {
        if (queueStack.isEmpty()) {
            throw new IllegalStateException(Messages.getString("awt.6B")); 
        }
        queueStack.addLast(newEventQueue);
        activeQueue = newEventQueue;
        activeQueue.setCore(this);
    }
    synchronized void pop() {
        EventQueue removed = queueStack.removeLast();
        if (removed != activeQueue) {
            throw new IllegalStateException(Messages.getString("awt.6C")); 
        }
        activeQueue = queueStack.getLast();
        removed.setCore(null);
    }
    synchronized AWTEvent getNextEventNoWait() {
        try {
            return events.isEmpty() ? null : activeQueue.getNextEvent();
        } catch (InterruptedException e) {
            return null;
        }
    }
    synchronized boolean isEmpty() {
        return (currentEvent == null) && events.isEmpty();
    }
    synchronized boolean isEmpty(long timeout) {
        if (!isEmpty()) {
            return false;
        }
        try {
            wait(timeout);
        } catch (InterruptedException e) {}
        return isEmpty();
    }
    synchronized EventQueue getActiveEventQueue() {
        return activeQueue;
    }
}
