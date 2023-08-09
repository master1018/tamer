public class EnumJvmJITCompilerTimeMonitoring extends Enumerated implements Serializable {
    protected static Hashtable<Integer, String> intTable =
            new Hashtable<Integer, String>();
    protected static Hashtable<String, Integer> stringTable =
            new Hashtable<String, Integer>();
    static  {
        intTable.put(new Integer(2), "supported");
        intTable.put(new Integer(1), "unsupported");
        stringTable.put("supported", new Integer(2));
        stringTable.put("unsupported", new Integer(1));
    }
    public EnumJvmJITCompilerTimeMonitoring(int valueIndex) throws IllegalArgumentException {
        super(valueIndex);
    }
    public EnumJvmJITCompilerTimeMonitoring(Integer valueIndex) throws IllegalArgumentException {
        super(valueIndex);
    }
    public EnumJvmJITCompilerTimeMonitoring() throws IllegalArgumentException {
        super();
    }
    public EnumJvmJITCompilerTimeMonitoring(String x) throws IllegalArgumentException {
        super(x);
    }
    protected Hashtable getIntTable() {
        return intTable ;
    }
    protected Hashtable getStringTable() {
        return stringTable ;
    }
}
