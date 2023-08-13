class WeakRef extends WeakReference {
    private int hashValue;
    private Object strongRef = null;
    public WeakRef(Object obj) {
        super(obj);
        setHashValue(obj);      
    }
    public WeakRef(Object obj, ReferenceQueue q) {
        super(obj, q);
        setHashValue(obj);      
    }
    public synchronized void pin() {
        if (strongRef == null) {
            strongRef = get();
            if (DGCImpl.dgcLog.isLoggable(Log.VERBOSE)) {
                DGCImpl.dgcLog.log(Log.VERBOSE,
                                   "strongRef = " + strongRef);
            }
        }
    }
    public synchronized void unpin() {
        if (strongRef != null) {
            if (DGCImpl.dgcLog.isLoggable(Log.VERBOSE)) {
                DGCImpl.dgcLog.log(Log.VERBOSE,
                                   "strongRef = " + strongRef);
            }
            strongRef = null;
        }
    }
    private void setHashValue(Object obj) {
        if (obj != null) {
            hashValue = System.identityHashCode(obj);
        } else {
            hashValue = 0;
        }
    }
    public int hashCode() {
        return hashValue;
    }
    public boolean equals(Object obj) {
        if (obj instanceof WeakRef) {
            if (obj == this)
                return true;
            Object referent = get();
            return (referent != null) && (referent == ((WeakRef) obj).get());
        } else {
            return false;
        }
    }
}
