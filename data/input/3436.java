class ByteArrayCounterSnapshot extends AbstractCounter
       implements ByteArrayCounter {
    byte[] value;
    ByteArrayCounterSnapshot(String name, Units u, Variability v, int flags,
                             int vectorLength, byte[] value) {
        super(name, u, v, flags, vectorLength);
        this.value = value;
    }
    public Object getValue() {
        return value;
    }
    public byte[] byteArrayValue() {
        return value;
    }
    public byte byteAt(int index) {
        return value[index];
    }
    private static final long serialVersionUID = 1444793459838438979L;
}
