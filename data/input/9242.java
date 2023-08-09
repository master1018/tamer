class StringCounterSnapshot extends AbstractCounter
       implements StringCounter {
    String value;
    StringCounterSnapshot(String name, Units u, Variability v, int flags,
                          String value) {
        super(name, u, v, flags);
        this.value = value;
    }
    public Object getValue() {
        return value;
    }
    public String stringValue() {
        return value;
    }
    private static final long serialVersionUID = 1132921539085572034L;
}
