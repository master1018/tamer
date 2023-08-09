public class RouteSpecificPool {
    private final Log log = LogFactory.getLog(getClass());
    protected final HttpRoute route;
    protected final int maxEntries;
    protected final LinkedList<BasicPoolEntry> freeEntries;
    protected final Queue<WaitingThread> waitingThreads;
    protected int numEntries;
    public RouteSpecificPool(HttpRoute route, int maxEntries) {
        this.route = route;
        this.maxEntries = maxEntries;
        this.freeEntries = new LinkedList<BasicPoolEntry>();
        this.waitingThreads = new LinkedList<WaitingThread>();
        this.numEntries = 0;
    }
    public final HttpRoute getRoute() {
        return route;
    }
    public final int getMaxEntries() {
        return maxEntries;
    }
    public boolean isUnused() {
        return (numEntries < 1) && waitingThreads.isEmpty();
    }
    public int getCapacity() {
        return maxEntries - numEntries;
    }
    public final int getEntryCount() {
        return numEntries;
    }
    public BasicPoolEntry allocEntry(final Object state) {
        if (!freeEntries.isEmpty()) {
            ListIterator<BasicPoolEntry> it = freeEntries.listIterator(freeEntries.size());
            while (it.hasPrevious()) {
                BasicPoolEntry entry = it.previous();
                if (LangUtils.equals(state, entry.getState())) {
                    it.remove();
                    return entry;
                }
            }
        }
        if (!freeEntries.isEmpty()) {
            BasicPoolEntry entry = freeEntries.remove();   
            entry.setState(null);
            OperatedClientConnection conn = entry.getConnection();
            try {
                conn.close();
            } catch (IOException ex) {
                log.debug("I/O error closing connection", ex);
            }
            return entry;
        }
        return null;
    }
    public void freeEntry(BasicPoolEntry entry) {
        if (numEntries < 1) {
            throw new IllegalStateException
                ("No entry created for this pool. " + route);
        }
        if (numEntries <= freeEntries.size()) {
            throw new IllegalStateException
                ("No entry allocated from this pool. " + route);
        }
        freeEntries.add(entry);
    }
    public void createdEntry(BasicPoolEntry entry) {
        if (!route.equals(entry.getPlannedRoute())) {
            throw new IllegalArgumentException
                ("Entry not planned for this pool." +
                 "\npool: " + route +
                 "\nplan: " + entry.getPlannedRoute());
        }
        numEntries++;
    }
    public boolean deleteEntry(BasicPoolEntry entry) {
        final boolean found = freeEntries.remove(entry);
        if (found)
            numEntries--;
        return found;
    }
    public void dropEntry() {
        if (numEntries < 1) {
            throw new IllegalStateException
                ("There is no entry that could be dropped.");
        }
        numEntries--;
    }
    public void queueThread(WaitingThread wt) {
        if (wt == null) {
            throw new IllegalArgumentException
                ("Waiting thread must not be null.");
        }
        this.waitingThreads.add(wt);
    }
    public boolean hasThread() {
        return !this.waitingThreads.isEmpty();
    }
    public WaitingThread nextThread() {
        return this.waitingThreads.peek();
    }
    public void removeThread(WaitingThread wt) {
        if (wt == null)
            return;
        this.waitingThreads.remove(wt);
    }
} 
