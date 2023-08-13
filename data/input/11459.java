public final class Bean {
    public static Object DEFAULT;
    private Object value = DEFAULT;
    public Object getValue() {
        return this.value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
}
