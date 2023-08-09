class VerifyTreeSet<K> extends java.util.TreeSet {
    VerifyTreeSet() {
        super();
    }
    public VerifyTreeSet(Comparator c) {
        super(c);
    }
    public TreeSet<K> diff(TreeSet in) {
        TreeSet<K> delta = (TreeSet<K>) this.clone();
        delta.removeAll(in);
        return delta;
    }
}
