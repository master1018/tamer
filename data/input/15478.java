public class SnmpCounter extends SnmpUnsignedInt {
    private static final long serialVersionUID = 4655264728839396879L;
    public SnmpCounter(int v) throws IllegalArgumentException {
        super(v) ;
    }
    public SnmpCounter(Integer v) throws IllegalArgumentException {
        super(v) ;
    }
    public SnmpCounter(long v) throws IllegalArgumentException {
        super(v) ;
    }
    public SnmpCounter(Long v) throws IllegalArgumentException {
        super(v) ;
    }
    final public String getTypeName() {
        return name ;
    }
    final static String name = "Counter32" ;
}
