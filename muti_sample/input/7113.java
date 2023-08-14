public class JSJavaThread extends JSJavaInstance {
    public JSJavaThread(Instance threadOop, JSJavaFactory fac) {
        super(threadOop, fac);
        this.jthread = OopUtilities.threadOopGetJavaThread(threadOop);
    }
    public JSJavaThread(JavaThread jt, JSJavaFactory fac) {
        super((Instance) jt.getThreadObj(), fac);
        this.jthread = jt;
    }
    public String toString() {
        String name = getName();
        StringBuffer buf = new StringBuffer();
        buf.append("Thread (address=");
        buf.append(getOop().getHandle());
        buf.append(", name=");
        if (name != null) {
            buf.append(name);
        } else {
            buf.append("<unnamed>");
        }
        buf.append(')');
        return buf.toString();
    }
    protected Object getFieldValue(String name) {
        if (name.equals("name")) {
            return getName();
        } else if (name.equals("frames")) {
            return getFrames();
        } else if (name.equals("monitors")) {
            return getOwnedMonitors();
        } else {
            return super.getFieldValue(name);
        }
    }
    protected String[] getFieldNames() {
        String[] flds = super.getFieldNames();
        String[] res = new String[flds.length + 2];
        System.arraycopy(flds, 0, res, 0, flds.length);
        res[flds.length] = "frames";
        res[flds.length + 1] = "monitors";
        return res;
    }
    protected boolean hasField(String name) {
        if (name.equals("frames") || name.equals("monitors")) {
            return true;
        } else {
            return super.hasField(name);
        }
    }
    private String getName() {
        return OopUtilities.threadOopGetName(getOop());
    }
    private synchronized JSList getFrames() {
        if (framesCache == null) {
            final List list = new ArrayList(0);
            if (jthread != null) {
                JavaVFrame jvf = jthread.getLastJavaVFrameDbg();
                while (jvf != null) {
                    list.add(jvf);
                    jvf = jvf.javaSender();
                }
            }
            framesCache = factory.newJSList(list);
        }
        return framesCache;
    }
    private synchronized JSList getOwnedMonitors() {
        if (monitorsCache == null) {
            final List ownedMonitors = new ArrayList(0);
            if (jthread != null) {
                List lockedObjects = new ArrayList(); 
                ObjectMonitor waitingMonitor = jthread.getCurrentWaitingMonitor();
                OopHandle waitingObj = null;
                if (waitingMonitor != null) {
                   waitingObj = waitingMonitor.object();
                }
                ObjectMonitor pendingMonitor = jthread.getCurrentPendingMonitor();
                OopHandle pendingObj = null;
                if (pendingMonitor != null) {
                    pendingObj = pendingMonitor.object();
                }
                JavaVFrame frame = jthread.getLastJavaVFrameDbg();
                while (frame != null) {
                    List frameMonitors = frame.getMonitors();  
                    for (Iterator miItr = frameMonitors.iterator(); miItr.hasNext(); ) {
                        MonitorInfo mi = (MonitorInfo) miItr.next();
                        if (mi.eliminated() && frame.isCompiledFrame()) {
                          continue; 
                        }
                        OopHandle obj = mi.owner();
                        if (obj == null) {
                            continue;
                        }
                        if (obj.equals(waitingObj)) {
                           continue;
                        }
                        if (obj.equals(pendingObj)) {
                            continue;
                        }
                        boolean found = false;
                        for (Iterator loItr = lockedObjects.iterator(); loItr.hasNext(); ) {
                            if (obj.equals(loItr.next())) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            continue;
                        }
                        lockedObjects.add(obj);
                    }
                    frame = (JavaVFrame) frame.javaSender();
                }
                ObjectHeap heap = VM.getVM().getObjectHeap();
                for (Iterator loItr = lockedObjects.iterator(); loItr.hasNext(); ) {
                    ownedMonitors.add(heap.newOop((OopHandle)loItr.next()));
                }
            }
            monitorsCache = factory.newJSList(ownedMonitors);
        }
        return monitorsCache;
    }
    private JavaThread jthread;
    private JSList framesCache;
    private JSList monitorsCache;
}
