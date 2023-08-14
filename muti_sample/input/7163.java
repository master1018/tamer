public class ThreadReferenceImpl extends ObjectReferenceImpl
             implements ThreadReference,  JVMTIThreadState {
    private JavaThread myJavaThread;
    private ArrayList frames;    
    private List ownedMonitors; 
    private List ownedMonitorsInfo; 
    private ObjectReferenceImpl currentContendingMonitor;
    ThreadReferenceImpl(VirtualMachine aVm, sun.jvm.hotspot.runtime.JavaThread aRef) {
        super(aVm, (Instance)aRef.getThreadObj());
        myJavaThread = aRef;
    }
    ThreadReferenceImpl(VirtualMachine vm, Instance oRef) {
        super(vm, oRef);
        myJavaThread = OopUtilities.threadOopGetJavaThread(oRef);
    }
    JavaThread getJavaThread() {
        return myJavaThread;
    }
    protected String description() {
        return "ThreadReference " + uniqueID();
    }
    public String name() {
        return OopUtilities.threadOopGetName(ref());
    }
    public void suspend() {
        vm.throwNotReadOnlyException("ThreadReference.suspend()");
    }
    public void resume() {
        vm.throwNotReadOnlyException("ThreadReference.resume()");
    }
    public int suspendCount() {
        return 1;
    }
    public void stop(ObjectReference throwable) throws InvalidTypeException {
        vm.throwNotReadOnlyException("ThreadReference.stop()");
    }
    public void interrupt() {
        vm.throwNotReadOnlyException("ThreadReference.interrupt()");
    }
    private int jvmtiGetThreadState() {
        int state = OopUtilities.threadOopGetThreadStatus(ref());
        if (myJavaThread != null) {
            JavaThreadState jts = myJavaThread.getThreadState();
            if (myJavaThread.isBeingExtSuspended()) {
                state |= JVMTI_THREAD_STATE_SUSPENDED;
            }
            if (jts == JavaThreadState.IN_NATIVE) {
                state |= JVMTI_THREAD_STATE_IN_NATIVE;
            }
            OSThread osThread = myJavaThread.getOSThread();
            if (osThread != null && osThread.interrupted()) {
                state |= JVMTI_THREAD_STATE_INTERRUPTED;
            }
        }
        return state;
    }
    public int status() {
        int state = jvmtiGetThreadState();
        int status = THREAD_STATUS_UNKNOWN;
        if (! ((state & JVMTI_THREAD_STATE_ALIVE) != 0) ) {
            if ((state & JVMTI_THREAD_STATE_TERMINATED) != 0) {
                status = THREAD_STATUS_ZOMBIE;
            } else {
                status = THREAD_STATUS_NOT_STARTED;
            }
        } else {
            if ((state & JVMTI_THREAD_STATE_SLEEPING) != 0) {
                status = THREAD_STATUS_SLEEPING;
            } else if ((state & JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER) != 0) {
                status = THREAD_STATUS_MONITOR;
            } else if ((state & JVMTI_THREAD_STATE_WAITING) != 0) {
                status = THREAD_STATUS_WAIT;
            } else if ((state & JVMTI_THREAD_STATE_RUNNABLE) != 0) {
                status = THREAD_STATUS_RUNNING;
            }
        }
        return status;
    }
    public boolean isSuspended() { 
        return true;
    }
    public boolean isAtBreakpoint() { 
        return false;
    }
    public ThreadGroupReference threadGroup() {
        return (ThreadGroupReferenceImpl)vm.threadGroupMirror(
               (Instance)OopUtilities.threadOopGetThreadGroup(ref()));
    }
    public int frameCount() throws IncompatibleThreadStateException  { 
        privateFrames(0, -1);
        return frames.size();
    }
    public List frames() throws IncompatibleThreadStateException  {
        return privateFrames(0, -1);
    }
    public StackFrame frame(int index) throws IncompatibleThreadStateException  {
        List list = privateFrames(index, 1);
        return (StackFrame)list.get(0);
    }
    public List frames(int start, int length)
                              throws IncompatibleThreadStateException  {
        if (length < 0) {
            throw new IndexOutOfBoundsException(
                "length must be greater than or equal to zero");
        }
        return privateFrames(start, length);
    }
    private List privateFrames(int start, int length)
                              throws IncompatibleThreadStateException  {
        if (myJavaThread == null) {
            throw new IncompatibleThreadStateException();
        }
        if (frames == null) {
            frames = new ArrayList(10);
            JavaVFrame myvf = myJavaThread.getLastJavaVFrameDbg();
            while (myvf != null) {
                StackFrame myFrame = new StackFrameImpl(vm, this, myvf);
                frames.add(myFrame);
                myvf = (JavaVFrame)myvf.javaSender();
            }
        }
        List retVal;
        if (frames.size() == 0) {
            retVal = new ArrayList(0);
        } else {
            int toIndex = start + length;
            if (length == -1) {
                toIndex = frames.size();
            }
            retVal = frames.subList(start, toIndex);
        }
        return Collections.unmodifiableList(retVal);
    }
    public List ownedMonitors()  throws IncompatibleThreadStateException {
        if (vm.canGetOwnedMonitorInfo() == false) {
            throw new UnsupportedOperationException();
        }
        if (myJavaThread == null) {
           throw new IncompatibleThreadStateException();
        }
        if (ownedMonitors != null) {
            return ownedMonitors;
        }
        ownedMonitorsWithStackDepth();
        for (Iterator omi = ownedMonitorsInfo.iterator(); omi.hasNext(); ) {
            ownedMonitors.add(((MonitorInfoImpl)omi.next()).monitor());
        }
        return ownedMonitors;
    }
    public List ownedMonitorsAndFrames() throws IncompatibleThreadStateException {
        if (!vm.canGetMonitorFrameInfo()) {
            throw new UnsupportedOperationException(
                "target does not support getting Monitor Frame Info");
        }
        if (myJavaThread == null) {
           throw new IncompatibleThreadStateException();
        }
        if (ownedMonitorsInfo != null) {
            return ownedMonitorsInfo;
        }
        ownedMonitorsWithStackDepth();
        return ownedMonitorsInfo;
    }
    private void ownedMonitorsWithStackDepth() {
        ownedMonitorsInfo = new ArrayList();
        List lockedObjects = new ArrayList(); 
        List stackDepth = new ArrayList(); 
        ObjectMonitor waitingMonitor = myJavaThread.getCurrentWaitingMonitor();
        ObjectMonitor pendingMonitor = myJavaThread.getCurrentPendingMonitor();
        OopHandle waitingObj = null;
        if (waitingMonitor != null) {
            waitingObj = waitingMonitor.object();
        }
        OopHandle pendingObj = null;
        if (pendingMonitor != null) {
            pendingObj = pendingMonitor.object();
        }
        JavaVFrame frame = myJavaThread.getLastJavaVFrameDbg();
        int depth=0;
        while (frame != null) {
            List frameMonitors = frame.getMonitors();  
            for (Iterator miItr = frameMonitors.iterator(); miItr.hasNext(); ) {
                sun.jvm.hotspot.runtime.MonitorInfo mi = (sun.jvm.hotspot.runtime.MonitorInfo) miItr.next();
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
                stackDepth.add(new Integer(depth));
            }
            frame = (JavaVFrame) frame.javaSender();
            depth++;
        }
        ObjectHeap heap = vm.saObjectHeap();
        Iterator stk = stackDepth.iterator();
        for (Iterator loItr = lockedObjects.iterator(); loItr.hasNext(); ) {
            Oop obj = heap.newOop((OopHandle)loItr.next());
            ownedMonitorsInfo.add(new MonitorInfoImpl(vm, vm.objectMirror(obj), this,
                                                              ((Integer)stk.next()).intValue()));
        }
    }
    public ObjectReference currentContendedMonitor()
                              throws IncompatibleThreadStateException  {
        if (vm.canGetCurrentContendedMonitor() == false) {
            throw new UnsupportedOperationException();
        }
        if (myJavaThread == null) {
           throw new IncompatibleThreadStateException();
        }
        ObjectMonitor mon = myJavaThread.getCurrentWaitingMonitor();
        if (mon == null) {
           mon = myJavaThread.getCurrentPendingMonitor();
           if (mon != null) {
               OopHandle handle = mon.object();
               return vm.objectMirror(vm.saObjectHeap().newOop(handle));
           } else {
               return null;
           }
        } else {
           OopHandle handle = mon.object();
           if (Assert.ASSERTS_ENABLED) {
               Assert.that(handle != null, "Object.wait() should have an object");
           }
           Oop obj = vm.saObjectHeap().newOop(handle);
           return vm.objectMirror(obj);
        }
    }
    public void popFrames(StackFrame frame) throws IncompatibleThreadStateException {
        vm.throwNotReadOnlyException("ThreadReference.popFrames()");
    }
    public void forceEarlyReturn(Value returnValue) throws IncompatibleThreadStateException {
        vm.throwNotReadOnlyException("ThreadReference.forceEarlyReturn()");
    }
    public String toString() {
        return "instance of " + referenceType().name() +
               "(name='" + name() + "', " + "id=" + uniqueID() + ")";
    }
}
