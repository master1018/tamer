public class SnmpGauge extends SnmpUnsignedInt {
    private static final long serialVersionUID = -8366622742122792945L;
    public SnmpGauge(int v) throws IllegalArgumentException {
        super(v) ;
    }
    public SnmpGauge(Integer v) throws IllegalArgumentException {
        super(v) ;
    }
    public SnmpGauge(long v) throws IllegalArgumentException {
        super(v) ;
    }
    public SnmpGauge(Long v) throws IllegalArgumentException {
        super(v) ;
    }
    final public String getTypeName() {
        return name ;
    }
    final static String name = "Gauge32" ;
}
