public abstract class JavaValue extends JavaThing {
    protected JavaValue() {
    }
    public boolean isHeapAllocated() {
        return false;
    }
    abstract public String toString();
    public int getSize() {
        return 0;
    }
}
