abstract class AbstractWatchKey implements WatchKey {
    static final int MAX_EVENT_LIST_SIZE    = 512;
    static final Event<Object> OVERFLOW_EVENT =
        new Event<Object>(StandardWatchEventKinds.OVERFLOW, null);
    private static enum State { READY, SIGNALLED };
    private final AbstractWatchService watcher;
    private final Path dir;
    private State state;
    private List<WatchEvent<?>> events;
    private Map<Object,WatchEvent<?>> lastModifyEvents;
    protected AbstractWatchKey(Path dir, AbstractWatchService watcher) {
        this.watcher = watcher;
        this.dir = dir;
        this.state = State.READY;
        this.events = new ArrayList<WatchEvent<?>>();
        this.lastModifyEvents = new HashMap<Object,WatchEvent<?>>();
    }
    final AbstractWatchService watcher() {
        return watcher;
    }
    @Override
    public Path watchable() {
        return dir;
    }
    final void signal() {
        synchronized (this) {
            if (state == State.READY) {
                state = State.SIGNALLED;
                watcher.enqueueKey(this);
            }
        }
    }
    @SuppressWarnings("unchecked")
    final void signalEvent(WatchEvent.Kind<?> kind, Object context) {
        boolean isModify = (kind == StandardWatchEventKinds.ENTRY_MODIFY);
        synchronized (this) {
            int size = events.size();
            if (size > 0) {
                WatchEvent<?> prev = events.get(size-1);
                if ((prev.kind() == StandardWatchEventKinds.OVERFLOW) ||
                    ((kind == prev.kind() &&
                     Objects.equals(context, prev.context()))))
                {
                    ((Event<?>)prev).increment();
                    return;
                }
                if (!lastModifyEvents.isEmpty()) {
                    if (isModify) {
                        WatchEvent<?> ev = lastModifyEvents.get(context);
                        if (ev != null) {
                            assert ev.kind() == StandardWatchEventKinds.ENTRY_MODIFY;
                            ((Event<?>)ev).increment();
                            return;
                        }
                    } else {
                        lastModifyEvents.remove(context);
                    }
                }
                if (size >= MAX_EVENT_LIST_SIZE) {
                    kind = StandardWatchEventKinds.OVERFLOW;
                    isModify = false;
                    context = null;
                }
            }
            Event<Object> ev =
                new Event<Object>((WatchEvent.Kind<Object>)kind, context);
            if (isModify) {
                lastModifyEvents.put(context, ev);
            } else if (kind == StandardWatchEventKinds.OVERFLOW) {
                events.clear();
                lastModifyEvents.clear();
            }
            events.add(ev);
            signal();
        }
    }
    @Override
    public final List<WatchEvent<?>> pollEvents() {
        synchronized (this) {
            List<WatchEvent<?>> result = events;
            events = new ArrayList<WatchEvent<?>>();
            lastModifyEvents.clear();
            return result;
        }
    }
    @Override
    public final boolean reset() {
        synchronized (this) {
            if (state == State.SIGNALLED && isValid()) {
                if (events.isEmpty()) {
                    state = State.READY;
                } else {
                    watcher.enqueueKey(this);
                }
            }
            return isValid();
        }
    }
    private static class Event<T> implements WatchEvent<T> {
        private final WatchEvent.Kind<T> kind;
        private final T context;
        private int count;
        Event(WatchEvent.Kind<T> type, T context) {
            this.kind = type;
            this.context = context;
            this.count = 1;
        }
        @Override
        public WatchEvent.Kind<T> kind() {
            return kind;
        }
        @Override
        public T context() {
            return context;
        }
        @Override
        public int count() {
            return count;
        }
        void increment() {
            count++;
        }
    }
}
