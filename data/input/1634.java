class LongArrayCounterSnapshot extends AbstractCounter
       implements LongArrayCounter {
    long[] value;
    LongArrayCounterSnapshot(String name, Units u, Variability v, int flags,
                             int vectorLength, long[] value) {
        super(name, u, v, flags, vectorLength);
        this.value = value;
    }
    public Object getValue() {
        return value;
    }
    public long[] longArrayValue() {
        return value;
    }
    public long longAt(int index) {
        return value[index];
    }
    private static final long serialVersionUID = 3585870271405924292L;
}
