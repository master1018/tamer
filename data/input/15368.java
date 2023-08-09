class LongCounterSnapshot extends AbstractCounter
       implements LongCounter {
    long value;
    LongCounterSnapshot(String name, Units u, Variability v, int flags,
                        long value) {
        super(name, u, v, flags);
        this.value = value;
    }
    public Object getValue() {
        return new Long(value);
    }
    public long longValue() {
        return value;
    }
    private static final long serialVersionUID = 2054263861474565758L;
}
