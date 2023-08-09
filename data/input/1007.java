public class EnumJvmMemPoolType extends Enumerated implements Serializable {
    protected static Hashtable<Integer, String> intTable =
            new Hashtable<Integer, String>();
    protected static Hashtable<String, Integer> stringTable =
            new Hashtable<String, Integer>();
    static  {
        intTable.put(new Integer(2), "heap");
        intTable.put(new Integer(1), "nonheap");
        stringTable.put("heap", new Integer(2));
        stringTable.put("nonheap", new Integer(1));
    }
    public EnumJvmMemPoolType(int valueIndex) throws IllegalArgumentException {
        super(valueIndex);
    }
    public EnumJvmMemPoolType(Integer valueIndex) throws IllegalArgumentException {
        super(valueIndex);
    }
    public EnumJvmMemPoolType() throws IllegalArgumentException {
        super();
    }
    public EnumJvmMemPoolType(String x) throws IllegalArgumentException {
        super(x);
    }
    protected Hashtable getIntTable() {
        return intTable ;
    }
    protected Hashtable getStringTable() {
        return stringTable ;
    }
}
