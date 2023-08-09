public class StringEncoderComparator implements Comparator {
    private StringEncoder stringEncoder;
    public StringEncoderComparator() {
    }
    public StringEncoderComparator(StringEncoder stringEncoder) {
        this.stringEncoder = stringEncoder;
    }
    public int compare(Object o1, Object o2) {
        int compareCode = 0;
        try {
            Comparable s1 = (Comparable) ((Encoder) this.stringEncoder).encode(o1);
            Comparable s2 = (Comparable) ((Encoder) this.stringEncoder).encode(o2);
            compareCode = s1.compareTo(s2);
        } 
        catch (EncoderException ee) {
            compareCode = 0;
        }
        return compareCode;
    }
}
