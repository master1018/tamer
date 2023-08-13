final class EventSupport {
    final static private boolean debug = false;
    private LdapCtx ctx;
    private Hashtable notifiers = new Hashtable(11);
    private Vector unsolicited = null;
    EventSupport(LdapCtx ctx) {
        this.ctx = ctx;
    }
    synchronized void addNamingListener(String nm, int scope,
        NamingListener l) throws NamingException {
        if (l instanceof ObjectChangeListener ||
            l instanceof NamespaceChangeListener) {
            NotifierArgs args = new NotifierArgs(nm, scope, l);
            NamingEventNotifier notifier =
                (NamingEventNotifier) notifiers.get(args);
            if (notifier == null) {
                notifier = new NamingEventNotifier(this, ctx, args, l);
                notifiers.put(args, notifier);
            } else {
                notifier.addNamingListener(l);
            }
        }
        if (l instanceof UnsolicitedNotificationListener) {
            if (unsolicited == null) {
                unsolicited = new Vector(3);
            }
            unsolicited.addElement(l);
        }
    }
    synchronized void addNamingListener(String nm, String filter,
        SearchControls ctls, NamingListener l) throws NamingException {
        if (l instanceof ObjectChangeListener ||
            l instanceof NamespaceChangeListener) {
            NotifierArgs args = new NotifierArgs(nm, filter, ctls, l);
            NamingEventNotifier notifier =
                (NamingEventNotifier) notifiers.get(args);
            if (notifier == null) {
                notifier = new NamingEventNotifier(this, ctx, args, l);
                notifiers.put(args, notifier);
            } else {
                notifier.addNamingListener(l);
            }
        }
        if (l instanceof UnsolicitedNotificationListener) {
            if (unsolicited == null) {
                unsolicited = new Vector(3);
            }
            unsolicited.addElement(l);
        }
    }
    synchronized void removeNamingListener(NamingListener l) {
        Enumeration allnotifiers = notifiers.elements();
        NamingEventNotifier notifier;
        if (debug) System.err.println("EventSupport removing listener");
        while (allnotifiers.hasMoreElements()) {
            notifier = (NamingEventNotifier)allnotifiers.nextElement();
            if (notifier != null) {
                if (debug)
                    System.err.println("EventSupport removing listener from notifier");
                notifier.removeNamingListener(l);
                if (!notifier.hasNamingListeners()) {
                    if (debug)
                        System.err.println("EventSupport stopping notifier");
                    notifier.stop();
                    notifiers.remove(notifier.info);
                }
            }
        }
        if (debug) System.err.println("EventSupport removing unsolicited: " +
            unsolicited);
        if (unsolicited != null) {
            unsolicited.removeElement(l);
        }
    }
    synchronized boolean hasUnsolicited() {
        return (unsolicited != null && unsolicited.size() > 0);
    }
    synchronized void removeDeadNotifier(NotifierArgs info) {
        if (debug) {
            System.err.println("EventSupport.removeDeadNotifier: " + info.name);
        }
        notifiers.remove(info);
    }
    synchronized void fireUnsolicited(Object obj) {
        if (debug) {
            System.err.println("EventSupport.fireUnsolicited: " + obj + " "
                + unsolicited);
        }
        if (unsolicited == null || unsolicited.size() == 0) {
            return;
        }
        if (obj instanceof UnsolicitedNotification) {
            UnsolicitedNotificationEvent evt =
                new UnsolicitedNotificationEvent(ctx, (UnsolicitedNotification)obj);
            queueEvent(evt, unsolicited);
        } else if (obj instanceof NamingException) {
            NamingExceptionEvent evt =
                new NamingExceptionEvent(ctx, (NamingException)obj);
            queueEvent(evt, unsolicited);
            unsolicited = null;
        }
    }
    synchronized void cleanup() {
        if (debug) System.err.println("EventSupport clean up");
        if (notifiers != null) {
            for (Enumeration ns = notifiers.elements(); ns.hasMoreElements(); ) {
                ((NamingEventNotifier) ns.nextElement()).stop();
            }
            notifiers = null;
        }
        if (eventQueue != null) {
            eventQueue.stop();
            eventQueue = null;
        }
    }
    private EventQueue eventQueue;
    synchronized void queueEvent(EventObject event, Vector vector) {
        if (eventQueue == null)
            eventQueue = new EventQueue();
        Vector v = (Vector)vector.clone();
        eventQueue.enqueue(event, v);
    }
}
