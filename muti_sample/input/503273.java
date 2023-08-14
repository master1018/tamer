 final class EmptyEnumeration implements Enumeration<URL>, Serializable {
    private static final EmptyEnumeration mInst = new EmptyEnumeration();
    private EmptyEnumeration() {}
    public static EmptyEnumeration getInstance() {
        return mInst;
    }
    public boolean hasMoreElements() {
        return false;
    }
    public URL nextElement() {
        throw new NoSuchElementException();
    }
}
