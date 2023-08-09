class VMState {
    private final VirtualMachineImpl vm;
    private final List<WeakReference> listeners = new ArrayList<WeakReference>(); 
    private boolean notifyingListeners = false;  
    private int lastCompletedCommandId = 0;   
    private int lastResumeCommandId = 0;      
    private static class Cache {
        List<ThreadGroupReference> groups = null;  
        List<ThreadReference> threads = null; 
    }
    private Cache cache = null;               
    private static final Cache markerCache = new Cache();
    private void disableCache() {
        synchronized (this) {
            cache = null;
        }
    }
    private void enableCache() {
        synchronized (this) {
            cache = markerCache;
        }
    }
    private Cache getCache() {
        synchronized (this) {
            if (cache == markerCache) {
                cache = new Cache();
            }
            return cache;
        }
    }
    VMState(VirtualMachineImpl vm) {
        this.vm = vm;
    }
    boolean isSuspended() {
        return cache != null;
    }
    synchronized void notifyCommandComplete(int id) {
        lastCompletedCommandId = id;
    }
    synchronized void freeze() {
        if (cache == null && (lastCompletedCommandId >= lastResumeCommandId)) {
            processVMAction(new VMAction(vm, VMAction.VM_SUSPENDED));
            enableCache();
        }
    }
    synchronized PacketStream thawCommand(CommandSender sender) {
        PacketStream stream = sender.send();
        lastResumeCommandId = stream.id();
        thaw();
        return stream;
    }
    void thaw() {
        thaw(null);
    }
    synchronized void thaw(ThreadReference resumingThread) {
        if (cache != null) {
            if ((vm.traceFlags & vm.TRACE_OBJREFS) != 0) {
                vm.printTrace("Clearing VM suspended cache");
            }
            disableCache();
        }
        processVMAction(new VMAction(vm, resumingThread, VMAction.VM_NOT_SUSPENDED));
    }
    private synchronized void processVMAction(VMAction action) {
        if (!notifyingListeners) {
            notifyingListeners = true;
            Iterator iter = listeners.iterator();
            while (iter.hasNext()) {
                WeakReference ref = (WeakReference)iter.next();
                VMListener listener = (VMListener)ref.get();
                if (listener != null) {
                    boolean keep = true;
                    switch (action.id()) {
                        case VMAction.VM_SUSPENDED:
                            keep = listener.vmSuspended(action);
                            break;
                        case VMAction.VM_NOT_SUSPENDED:
                            keep = listener.vmNotSuspended(action);
                            break;
                    }
                    if (!keep) {
                        iter.remove();
                    }
                } else {
                    iter.remove();
                }
            }
            notifyingListeners = false;
        }
    }
    synchronized void addListener(VMListener listener) {
        listeners.add(new WeakReference<VMListener>(listener));
    }
    synchronized boolean hasListener(VMListener listener) {
        return listeners.contains(listener);
    }
    synchronized void removeListener(VMListener listener) {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            WeakReference ref = (WeakReference)iter.next();
            if (listener.equals(ref.get())) {
                iter.remove();
                break;
            }
        }
    }
    List<ThreadReference> allThreads() {
        List<ThreadReference> threads = null;
        try {
            Cache local = getCache();
            if (local != null) {
                threads = local.threads;
            }
            if (threads == null) {
                threads = Arrays.asList((ThreadReference[])JDWP.VirtualMachine.AllThreads.
                                        process(vm).threads);
                if (local != null) {
                    local.threads = threads;
                    if ((vm.traceFlags & vm.TRACE_OBJREFS) != 0) {
                        vm.printTrace("Caching all threads (count = " +
                                      threads.size() + ") while VM suspended");
                    }
                }
            }
        } catch (JDWPException exc) {
            throw exc.toJDIException();
        }
        return threads;
    }
    List<ThreadGroupReference> topLevelThreadGroups() {
        List<ThreadGroupReference> groups = null;
        try {
            Cache local = getCache();
            if (local != null) {
                groups = local.groups;
            }
            if (groups == null) {
                groups = Arrays.asList(
                                (ThreadGroupReference[])JDWP.VirtualMachine.TopLevelThreadGroups.
                                       process(vm).groups);
                if (local != null) {
                    local.groups = groups;
                    if ((vm.traceFlags & vm.TRACE_OBJREFS) != 0) {
                        vm.printTrace(
                          "Caching top level thread groups (count = " +
                          groups.size() + ") while VM suspended");
                    }
                }
            }
        } catch (JDWPException exc) {
            throw exc.toJDIException();
        }
        return groups;
    }
}
