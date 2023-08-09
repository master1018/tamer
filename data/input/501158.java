class ReverseComparator implements Comparator, Serializable {
    private final Comparator delegate;
    public ReverseComparator(Comparator delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate comparator is missing");
        }
        this.delegate = delegate;
    }
    public int compare(Object obj1, Object obj2) {
        return delegate.compare(obj2, obj1); 
    }
}
