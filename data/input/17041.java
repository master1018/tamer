public class LockInfo {
    private String className;
    private int    identityHashCode;
    @ConstructorProperties({"className", "identityHashCode"})
    public LockInfo(String className, int identityHashCode) {
        if (className == null) {
            throw new NullPointerException("Parameter className cannot be null");
        }
        this.className = className;
        this.identityHashCode = identityHashCode;
    }
    LockInfo(Object lock) {
        this.className = lock.getClass().getName();
        this.identityHashCode = System.identityHashCode(lock);
    }
    public String getClassName() {
        return className;
    }
    public int getIdentityHashCode() {
        return identityHashCode;
    }
    public String toString() {
        return className + '@' + Integer.toHexString(identityHashCode);
    }
}
