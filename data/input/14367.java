public class EnumJvmClassesVerboseLevel extends Enumerated implements Serializable {
    protected static Hashtable<Integer, String> intTable =
            new Hashtable<Integer, String>();
    protected static Hashtable<String, Integer> stringTable =
            new Hashtable<String, Integer>();
    static  {
        intTable.put(new Integer(2), "verbose");
        intTable.put(new Integer(1), "silent");
        stringTable.put("verbose", new Integer(2));
        stringTable.put("silent", new Integer(1));
    }
    public EnumJvmClassesVerboseLevel(int valueIndex) throws IllegalArgumentException {
        super(valueIndex);
    }
    public EnumJvmClassesVerboseLevel(Integer valueIndex) throws IllegalArgumentException {
        super(valueIndex);
    }
    public EnumJvmClassesVerboseLevel() throws IllegalArgumentException {
        super();
    }
    public EnumJvmClassesVerboseLevel(String x) throws IllegalArgumentException {
        super(x);
    }
    protected Hashtable getIntTable() {
        return intTable ;
    }
    protected Hashtable getStringTable() {
        return stringTable ;
    }
}
