public abstract class CollationKey implements Comparable<CollationKey> {
    abstract public int compareTo(CollationKey target);
    public String getSourceString() {
        return source;
    }
    abstract public byte[] toByteArray();
    protected CollationKey(String source) {
        if (source==null){
            throw new NullPointerException();
        }
        this.source = source;
    }
    final private String source;
}
