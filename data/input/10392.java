public abstract class AbstractEventSet extends EventObject implements EventSet {
    private static final long serialVersionUID = 2772717574222076977L;
    private final EventSet jdiEventSet;
    final Event oneEvent;
    AbstractEventSet(EventSet jdiEventSet) {
        super(jdiEventSet.virtualMachine());
        this.jdiEventSet = jdiEventSet;
        this.oneEvent = eventIterator().nextEvent();
    }
    public static AbstractEventSet toSpecificEventSet(EventSet jdiEventSet) {
        Event evt = jdiEventSet.eventIterator().nextEvent();
        if (evt instanceof LocatableEvent) {
            if (evt instanceof ExceptionEvent) {
                return new ExceptionEventSet(jdiEventSet);
            } else if (evt instanceof WatchpointEvent) {
                if (evt instanceof AccessWatchpointEvent) {
                    return new AccessWatchpointEventSet(jdiEventSet);
                } else {
                    return new ModificationWatchpointEventSet(jdiEventSet);
                }
            } else {
                return new LocationTriggerEventSet(jdiEventSet);
            }
        } else if (evt instanceof ClassPrepareEvent) {
            return new ClassPrepareEventSet(jdiEventSet);
        } else if (evt instanceof ClassUnloadEvent) {
            return new ClassUnloadEventSet(jdiEventSet);
        } else if (evt instanceof ThreadDeathEvent) {
            return new ThreadDeathEventSet(jdiEventSet);
        } else if (evt instanceof ThreadStartEvent) {
            return new ThreadStartEventSet(jdiEventSet);
        } else if (evt instanceof VMDeathEvent) {
            return new VMDeathEventSet(jdiEventSet);
        } else if (evt instanceof VMDisconnectEvent) {
            return new VMDisconnectEventSet(jdiEventSet);
        } else if (evt instanceof VMStartEvent) {
            return new VMStartEventSet(jdiEventSet);
        } else {
            throw new IllegalArgumentException("Unknown event " + evt);
        }
    }
    public abstract void notify(JDIListener listener);
    @Override
    public VirtualMachine virtualMachine() {
        return jdiEventSet.virtualMachine();
    }
    public VirtualMachine getVirtualMachine() {
        return jdiEventSet.virtualMachine();
    }
    public int getSuspendPolicy() {
        return jdiEventSet.suspendPolicy();
    }
    @Override
    public void resume() {
        jdiEventSet.resume();
    }
    @Override
    public int suspendPolicy() {
        return jdiEventSet.suspendPolicy();
    }
    public boolean suspendedAll() {
        return jdiEventSet.suspendPolicy() == EventRequest.SUSPEND_ALL;
    }
    public boolean suspendedEventThread() {
        return jdiEventSet.suspendPolicy() == EventRequest.SUSPEND_EVENT_THREAD;
    }
    public boolean suspendedNone() {
        return jdiEventSet.suspendPolicy() == EventRequest.SUSPEND_NONE;
    }
    @Override
    public EventIterator eventIterator() {
        return jdiEventSet.eventIterator();
    }
    @Override
    public int size() {
        return jdiEventSet.size();
    }
    @Override
    public boolean isEmpty() {
        return jdiEventSet.isEmpty();
    }
    @Override
    public boolean contains(Object o) {
        return jdiEventSet.contains(o);
    }
    @Override
    public Iterator<Event> iterator() {
        return jdiEventSet.iterator();
    }
    @Override
    public Object[] toArray() {
        return jdiEventSet.toArray();
    }
    @Override
    public <T> T[] toArray(T a[]) {
        return jdiEventSet.toArray(a);
    }
    @Override
    public boolean containsAll(Collection<?> c) {
        return jdiEventSet.containsAll(c);
    }
    @Override
    public boolean add(Event e){
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean addAll(Collection<? extends Event> coll) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean removeAll(Collection<?> coll) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean retainAll(Collection<?> coll) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
