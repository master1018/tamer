public class Object {
    public Object() {
    }
    protected Object clone() throws CloneNotSupportedException {
        if (!(this instanceof Cloneable)) {
            throw new CloneNotSupportedException("Class doesn't implement Cloneable");
        }
        return internalClone((Cloneable) this);
    }
    private native Object internalClone(Cloneable o);
    public boolean equals(Object o) {
        return this == o;
    }
    protected void finalize() throws Throwable {
    }
    public final native Class<? extends Object> getClass();
    public native int hashCode();
    public final native void notify();
    public final native void notifyAll();
    public String toString() {
        return getClass().getName() + '@' + Integer.toHexString(hashCode());
    }
    public final void wait() throws InterruptedException {
        wait(0 ,0);
    }
    public final void wait(long millis) throws InterruptedException {
        wait(millis, 0);
    }
    public final native void wait(long millis, int nanos) throws InterruptedException;
}
