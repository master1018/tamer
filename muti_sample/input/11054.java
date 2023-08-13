public class EnumJvmMemManagerState extends Enumerated implements Serializable {
    protected static Hashtable<Integer, String> intTable =
            new Hashtable<Integer, String>();
    protected static Hashtable<String, Integer> stringTable =
            new Hashtable<String, Integer>();
    static  {
        intTable.put(new Integer(2), "valid");
        intTable.put(new Integer(1), "invalid");
        stringTable.put("valid", new Integer(2));
        stringTable.put("invalid", new Integer(1));
    }
    public EnumJvmMemManagerState(int valueIndex) throws IllegalArgumentException {
        super(valueIndex);
    }
    public EnumJvmMemManagerState(Integer valueIndex) throws IllegalArgumentException {
        super(valueIndex);
    }
    public EnumJvmMemManagerState() throws IllegalArgumentException {
        super();
    }
    public EnumJvmMemManagerState(String x) throws IllegalArgumentException {
        super(x);
    }
    protected Hashtable getIntTable() {
        return intTable ;
    }
    protected Hashtable getStringTable() {
        return stringTable ;
    }
}
