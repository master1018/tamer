public abstract class JavaThing {
    protected JavaThing() {
    }
    public JavaThing dereference(Snapshot shapshot, JavaField field) {
        return this;
    }
    public boolean isSameTypeAs(JavaThing other) {
        return getClass() == other.getClass();
    }
    abstract public boolean isHeapAllocated();
    abstract public int getSize();
    abstract public String toString();
    public int compareTo(JavaThing other) {
        return toString().compareTo(other.toString());
    }
}
