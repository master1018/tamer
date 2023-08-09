public class MutableInteger {
    private int value;
    public MutableInteger(int value) {
        this.setValue(value);
    }
    public int hashCode() {
        return getValue();
    }
    public boolean equals(Object o) {
        return (o instanceof MutableInteger) &&
               (((MutableInteger) o).getValue() == getValue());
    }
    public void setValue(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
