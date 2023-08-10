public class CompactSparseIntegerVector implements SparseIntegerVector, Serializable, Iterable<IntegerEntry> {
    private static final long serialVersionUID = 1L;
    private final SparseIntArray intArray;
    private double magnitude;
    public CompactSparseIntegerVector(int length) {
        intArray = new SparseIntArray(length);
        magnitude = 0;
    }
    public CompactSparseIntegerVector(IntegerVector v) {
        intArray = new SparseIntArray(v.length());
        if (v instanceof SparseVector) {
            SparseVector sv = (SparseVector) v;
            for (int i : sv.getNonZeroIndices()) intArray.set(i, v.get(i));
        } else {
            for (int i = 0; i < v.length(); ++i) intArray.set(i, v.get(i));
        }
        magnitude = -1;
    }
    public CompactSparseIntegerVector(int[] values) {
        intArray = new SparseIntArray(values);
        magnitude = -1;
    }
    public int add(int index, int delta) {
        magnitude = -1;
        return intArray.addPrimitive(index, delta);
    }
    public int get(int index) {
        return intArray.getPrimitive(index);
    }
    public Integer getValue(int index) {
        return get(index);
    }
    public int[] getNonZeroIndices() {
        return intArray.getElementIndices();
    }
    public Iterator<IntegerEntry> iterator() {
        return intArray.iterator();
    }
    public int length() {
        return intArray.length();
    }
    public double magnitude() {
        if (magnitude < 0) {
            double m = 0;
            for (int nz : getNonZeroIndices()) {
                int i = intArray.get(nz);
                m += i * i;
            }
            magnitude = Math.sqrt(m);
        }
        return magnitude;
    }
    public void set(int index, int value) {
        intArray.set(index, value);
        magnitude = -1;
    }
    public void set(int index, Number value) {
        set(index, value.intValue());
        magnitude = -1;
    }
    public int[] toArray() {
        int[] array = new int[intArray.length()];
        for (int i : intArray.getElementIndices()) array[i] = intArray.getPrimitive(i);
        return array;
    }
}
