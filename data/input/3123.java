public abstract class EnumSyntax implements Serializable, Cloneable {
    private static final long serialVersionUID = -2739521845085831642L;
    private int value;
    protected EnumSyntax(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
    public Object clone() {
        return this;
    }
    public int hashCode() {
        return value;
    }
    public String toString() {
        String[] theTable = getStringTable();
        int theIndex = value - getOffset();
        return
            theTable != null && theIndex >= 0 && theIndex < theTable.length ?
            theTable[theIndex] :
            Integer.toString (value);
    }
    protected Object readResolve() throws ObjectStreamException {
        EnumSyntax[] theTable = getEnumValueTable();
        if (theTable == null) {
            throw new InvalidObjectException(
                                "Null enumeration value table for class " +
                                getClass());
        }
        int theOffset = getOffset();
        int theIndex = value - theOffset;
        if (0 > theIndex || theIndex >= theTable.length) {
            throw new InvalidObjectException
                ("Integer value = " +  value + " not in valid range " +
                 theOffset + ".." + (theOffset + theTable.length - 1) +
                 "for class " + getClass());
        }
        EnumSyntax result = theTable[theIndex];
        if (result == null) {
            throw new InvalidObjectException
                ("No enumeration value for integer value = " +
                 value + "for class " + getClass());
        }
        return result;
    }
    protected String[] getStringTable() {
        return null;
    }
    protected EnumSyntax[] getEnumValueTable() {
        return null;
    }
    protected int getOffset() {
        return 0;
    }
}
