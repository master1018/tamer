public class EnumJvmMemoryGCCall extends Enumerated implements Serializable {
    protected static Hashtable<Integer, String> intTable =
            new Hashtable<Integer, String>();
    protected static Hashtable<String, Integer> stringTable =
            new Hashtable<String, Integer>();
    static  {
        intTable.put(new Integer(2), "supported");
        intTable.put(new Integer(5), "failed");
        intTable.put(new Integer(4), "started");
        intTable.put(new Integer(1), "unsupported");
        intTable.put(new Integer(3), "start");
        stringTable.put("supported", new Integer(2));
        stringTable.put("failed", new Integer(5));
        stringTable.put("started", new Integer(4));
        stringTable.put("unsupported", new Integer(1));
        stringTable.put("start", new Integer(3));
    }
    public EnumJvmMemoryGCCall(int valueIndex) throws IllegalArgumentException {
        super(valueIndex);
    }
    public EnumJvmMemoryGCCall(Integer valueIndex) throws IllegalArgumentException {
        super(valueIndex);
    }
    public EnumJvmMemoryGCCall() throws IllegalArgumentException {
        super();
    }
    public EnumJvmMemoryGCCall(String x) throws IllegalArgumentException {
        super(x);
    }
    protected Hashtable getIntTable() {
        return intTable ;
    }
    protected Hashtable getStringTable() {
        return stringTable ;
    }
}
