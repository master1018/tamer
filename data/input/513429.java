public final class StdFieldList extends FixedSizeList implements FieldList {
    public StdFieldList(int size) {
        super(size);
    }
    public Field get(int n) {
        return (Field) get0(n);
    }
    public void set(int n, Field field) {
        set0(n, field);
    }
}
