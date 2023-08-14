final class NamingEventNotifier implements Runnable {
    private final static boolean debug = false;
    private Vector namingListeners;
    private Thread worker;
    private LdapCtx context;
    private EventContext eventSrc;
    private EventSupport support;
    private NamingEnumeration results;
    NotifierArgs info;
    NamingEventNotifier(EventSupport support, LdapCtx ctx, NotifierArgs info,
        NamingListener firstListener) throws NamingException {
        this.info = info;
        this.support = support;
        Control psearch;
        try {
            psearch = new PersistentSearchControl(
                info.mask,
                true ,
                true ,
                Control.CRITICAL);
        } catch (java.io.IOException e) {
            NamingException ne = new NamingException(
                "Problem creating persistent search control");
            ne.setRootCause(e);
            throw ne;
        }
        context = (LdapCtx)ctx.newInstance(new Control[]{psearch});
        eventSrc = ctx;
        namingListeners = new Vector();
        namingListeners.addElement(firstListener);
        worker = Obj.helper.createThread(this);
        worker.setDaemon(true);  
        worker.start();
    }
    void addNamingListener(NamingListener l) {
        namingListeners.addElement(l);
    }
    void removeNamingListener(NamingListener l) {
        namingListeners.removeElement(l);
    }
    boolean hasNamingListeners() {
        return namingListeners.size() > 0;
    }
    public void run() {
        try {
            Continuation cont = new Continuation();
            cont.setError(this, info.name);
            Name nm = (info.name == null || info.name.equals("")) ?
                new CompositeName() : new CompositeName().add(info.name);
            results = context.searchAux(nm, info.filter, info.controls,
                true, false, cont);
            ((LdapSearchEnumeration)results).setStartName(context.currentParsedDN);
            SearchResult si;
            Control[] respctls;
            EntryChangeResponseControl ec;
            long changeNum;
            while (results.hasMore()) {
                si = (SearchResult)results.next();
                respctls = (si instanceof HasControls) ?
                    ((HasControls) si).getControls() : null;
                if (debug) {
                    System.err.println("notifier: " + si);
                    System.err.println("respCtls: " + respctls);
                }
                if (respctls != null) {
                    for (int i = 0; i < respctls.length; i++) {
                        if (respctls[i] instanceof EntryChangeResponseControl) {
                            ec = (EntryChangeResponseControl)respctls[i];
                            changeNum = ec.getChangeNumber();
                            switch (ec.getChangeType()) {
                            case EntryChangeResponseControl.ADD:
                                fireObjectAdded(si, changeNum);
                                break;
                            case EntryChangeResponseControl.DELETE:
                                fireObjectRemoved(si, changeNum);
                                break;
                            case EntryChangeResponseControl.MODIFY:
                                fireObjectChanged(si, changeNum);
                                break;
                            case EntryChangeResponseControl.RENAME:
                                fireObjectRenamed(si, ec.getPreviousDN(),
                                    changeNum);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        } catch (InterruptedNamingException e) {
            if (debug) System.err.println("NamingEventNotifier Interrupted");
        } catch (NamingException e) {
            fireNamingException(e);
            support.removeDeadNotifier(info);
        } finally {
            cleanup();
        }
        if (debug) System.err.println("NamingEventNotifier finished");
    }
    private void cleanup() {
        if (debug) System.err.println("NamingEventNotifier cleanup");
        try {
            if (results != null) {
                if (debug) System.err.println("NamingEventNotifier enum closing");
                results.close(); 
                results = null;
            }
            if (context != null) {
                if (debug) System.err.println("NamingEventNotifier ctx closing");
                context.close();
                context = null;
            }
        } catch (NamingException e) {}
    }
    void stop() {
        if (debug) System.err.println("NamingEventNotifier being stopping");
        if (worker != null) {
            worker.interrupt(); 
            worker = null;
        }
    }
    private void fireObjectAdded(Binding newBd, long changeID) {
        if (namingListeners == null || namingListeners.size() == 0)
            return;
        NamingEvent e = new NamingEvent(eventSrc, NamingEvent.OBJECT_ADDED,
            newBd, null, new Long(changeID));
        support.queueEvent(e, namingListeners);
    }
    private void fireObjectRemoved(Binding oldBd, long changeID) {
        if (namingListeners == null || namingListeners.size() == 0)
            return;
        NamingEvent e = new NamingEvent(eventSrc, NamingEvent.OBJECT_REMOVED,
            null, oldBd, new Long(changeID));
        support.queueEvent(e, namingListeners);
    }
    private void fireObjectChanged(Binding newBd, long changeID) {
        if (namingListeners == null || namingListeners.size() == 0)
            return;
        Binding oldBd = new Binding(newBd.getName(), null, newBd.isRelative());
        NamingEvent e = new NamingEvent(
            eventSrc, NamingEvent.OBJECT_CHANGED, newBd, oldBd, new Long(changeID));
        support.queueEvent(e, namingListeners);
    }
    private void fireObjectRenamed(Binding newBd, String oldDN, long changeID) {
        if (namingListeners == null || namingListeners.size() == 0)
            return;
        Binding oldBd = null;
        try {
            LdapName dn = new LdapName(oldDN);
            if (dn.startsWith(context.currentParsedDN)) {
                String relDN = dn.getSuffix(context.currentParsedDN.size()).toString();
                oldBd = new Binding(relDN, null);
            }
        } catch (NamingException e) {}
        if (oldBd == null) {
            oldBd = new Binding(oldDN, null, false );
        }
        NamingEvent e = new NamingEvent(
            eventSrc, NamingEvent.OBJECT_RENAMED, newBd, oldBd, new Long(changeID));
        support.queueEvent(e, namingListeners);
    }
    private void fireNamingException(NamingException e) {
        if (namingListeners == null || namingListeners.size() == 0)
            return;
        NamingExceptionEvent evt = new NamingExceptionEvent(eventSrc, e);
        support.queueEvent(evt, namingListeners);
    }
}
