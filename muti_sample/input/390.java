public class ObjectReferenceImpl extends ValueImpl implements ObjectReference {
    private Oop  saObject;
    private long myID;
    private boolean monitorInfoCached = false;
    private ThreadReferenceImpl owningThread = null;
    private List waitingThreads = null; 
    private int entryCount = 0;
    private static long nextID = 0L;
    private static synchronized long nextID() {
        return nextID++;
    }
    ObjectReferenceImpl(VirtualMachine aVm, sun.jvm.hotspot.oops.Oop oRef) {
        super(aVm);
        saObject = oRef;
        myID = nextID();
    }
    protected Oop ref() {
        return saObject;
    }
    public Type type() {
        return referenceType();
    }
    public ReferenceType referenceType() {
        Klass myKlass = ref().getKlass();
        return vm.referenceType(myKlass);
    }
    public Value getValue(Field sig) {
        List list = new ArrayList(1);
        list.add(sig);
        Map map = getValues(list);
        return(Value)map.get(sig);
    }
    public Map getValues(List theFields) {
        List staticFields = new ArrayList(0);
        int size = theFields.size();
        List instanceFields = new ArrayList(size);
        for (int i=0; i<size; i++) {
            sun.jvm.hotspot.jdi.FieldImpl field =
                (sun.jvm.hotspot.jdi.FieldImpl)theFields.get(i);
            ((ReferenceTypeImpl)referenceType()).validateFieldAccess(field);
            if (field.isStatic()) {
                staticFields.add(field);
            } else {
                instanceFields.add(field);
            }
        }
        Map map;
        if (staticFields.size() > 0) {
            map = referenceType().getValues(staticFields);
        } else {
            map = new HashMap(size);
        }
        size = instanceFields.size();
        for (int ii=0; ii<size; ii++){
            FieldImpl fieldImpl = (FieldImpl)instanceFields.get(ii);
            map.put(fieldImpl, fieldImpl.getValue(saObject));
        }
        return map;
    }
    public void setValue(Field field, Value value)
                   throws InvalidTypeException, ClassNotLoadedException {
        vm.throwNotReadOnlyException("ObjectReference.setValue(...)");
    }
    public Value invokeMethod(ThreadReference threadIntf, Method methodIntf,
                              List arguments, int options)
                              throws InvalidTypeException,
                                     IncompatibleThreadStateException,
                                     InvocationException,
                                     ClassNotLoadedException {
        vm.throwNotReadOnlyException("ObjectReference.invokeMethod(...)");
        return null;
    }
    public void disableCollection() {
        vm.throwNotReadOnlyException("ObjectReference.disableCollection()");
    }
    public void enableCollection() {
        vm.throwNotReadOnlyException("ObjectReference.enableCollection()");
    }
    public boolean isCollected() {
        vm.throwNotReadOnlyException("ObjectReference.isCollected()");
        return false;
    }
    public long uniqueID() {
        return myID;
    }
    public List waitingThreads() throws IncompatibleThreadStateException {
        if (vm.canGetMonitorInfo() == false) {
            throw new UnsupportedOperationException();
        }
        if (! monitorInfoCached) {
            computeMonitorInfo();
        }
        return waitingThreads;
    }
    public ThreadReference owningThread() throws IncompatibleThreadStateException {
        if (vm.canGetMonitorInfo() == false) {
            throw new UnsupportedOperationException();
        }
        if (! monitorInfoCached) {
            computeMonitorInfo();
        }
        return owningThread;
    }
    public int entryCount() throws IncompatibleThreadStateException {
        if (vm.canGetMonitorInfo() == false) {
            throw new UnsupportedOperationException();
        }
        if (! monitorInfoCached) {
            computeMonitorInfo();
        }
        return entryCount;
    }
    public List referringObjects(long maxReferrers) {
        if (!vm.canGetInstanceInfo()) {
            throw new UnsupportedOperationException(
                      "target does not support getting instances");
        }
        if (maxReferrers < 0) {
            throw new IllegalArgumentException("maxReferrers is less than zero: "
                                              + maxReferrers);
        }
        final ObjectReference obj = this;
        final List objects = new ArrayList(0);
        final long max = maxReferrers;
                vm.saObjectHeap().iterate(new DefaultHeapVisitor() {
                private long refCount = 0;
                public boolean doObj(Oop oop) {
                                        try {
                                                ObjectReference objref = vm.objectMirror(oop);
                                                List fields = objref.referenceType().allFields();
                                                for (int i=0; i < fields.size(); i++) {
                                                        Field fld = (Field)fields.get(i);
                                                        if (objref.getValue(fld).equals(obj) && !objects.contains(objref)) {
                                                                objects.add(objref);
                                                                refCount++;
                                                        }
                                                }
                                                if (max > 0 && refCount >= max) {
                                                        return true;
                                                }
                                        } catch  (RuntimeException x) {
                                        }
                                        return false;
                }
            });
        return objects;
    }
    private int countLockedObjects(JavaThread jt, Oop obj) {
        int res = 0;
        JavaVFrame frame = jt.getLastJavaVFrameDbg();
        while (frame != null) {
            List monitors = frame.getMonitors();
            OopHandle givenHandle = obj.getHandle();
            for (Iterator itr = monitors.iterator(); itr.hasNext();) {
                MonitorInfo mi = (MonitorInfo) itr.next();
                if (mi.eliminated() && frame.isCompiledFrame()) continue; 
                if (givenHandle.equals(mi.owner())) {
                    res++;
                }
            }
            frame = (JavaVFrame) frame.javaSender();
        }
        return res;
    }
    private List getPendingThreads(ObjectMonitor mon) {
        return vm.saVM().getThreads().getPendingThreads(mon);
    }
    private List getWaitingThreads(ObjectMonitor mon) {
        return vm.saVM().getThreads().getWaitingThreads(mon);
    }
    private JavaThread owningThreadFromMonitor(Address addr) {
        return vm.saVM().getThreads().owningThreadFromMonitor(addr);
    }
    private void computeMonitorInfo() {
        monitorInfoCached = true;
        Mark mark = saObject.getMark();
        ObjectMonitor mon = null;
        Address owner = null;
        if (! mark.hasMonitor()) {
            if (mark.hasLocker()) {
                owner = mark.locker().getAddress(); 
            }
        } else {
            mon = mark.monitor();
            owner = mon.owner();
        }
        if (owner != null) {
            owningThread = vm.threadMirror(owningThreadFromMonitor(owner));
        }
        if (owningThread != null) {
            if (owningThread.getJavaThread().getAddress().equals(owner)) {
                if (Assert.ASSERTS_ENABLED) {
                    Assert.that(false, "must have heavyweight monitor with JavaThread * owner");
                }
                entryCount = (int) mark.monitor().recursions() + 1;
            } else {
                entryCount = countLockedObjects(owningThread.getJavaThread(), saObject);
            }
        }
        waitingThreads = new ArrayList();
        if (mon != null) {
            List pendingThreads = getPendingThreads(mon);
            for (Iterator itrPend = pendingThreads.iterator(); itrPend.hasNext();) {
                waitingThreads.add(vm.threadMirror((JavaThread) itrPend.next()));
            }
            List objWaitingThreads = getWaitingThreads(mon);
            for (Iterator itrWait = objWaitingThreads.iterator(); itrWait.hasNext();) {
                waitingThreads.add(vm.threadMirror((JavaThread) itrWait.next()));
            }
        }
    }
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof ObjectReferenceImpl)) {
            ObjectReferenceImpl other = (ObjectReferenceImpl)obj;
            return (ref().equals(other.ref())) &&
                   super.equals(obj);
        } else {
            return false;
        }
    }
    public int hashCode() {
        return saObject.hashCode();
    }
    public String toString() {
        return  "instance of " + referenceType().name() + "(id=" + uniqueID() + ")";
    }
}
