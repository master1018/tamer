public class SnmpStatusException extends Exception implements SnmpDefinitions {
    private static final long serialVersionUID = 5809485694133115675L;
    public static final int noSuchName         = 2 ;
    public static final int badValue           = 3 ;
    public static final int readOnly           = 4 ;
    public static final int noAccess           = 6 ;
    public static final int noSuchInstance     = 0xE0;
    public static final int noSuchObject     = 0xE1;
    public SnmpStatusException(int status) {
        errorStatus = status ;
    }
    public SnmpStatusException(int status, int index) {
        errorStatus = status ;
        errorIndex = index ;
    }
    public SnmpStatusException(String s) {
        super(s);
    }
    public SnmpStatusException(SnmpStatusException x, int index) {
        super(x.getMessage());
        errorStatus= x.errorStatus;
        errorIndex= index;
    }
    public int getStatus() {
        return errorStatus ;
    }
    public int getErrorIndex() {
        return errorIndex;
    }
    private int errorStatus = 0 ;
    private int errorIndex= -1;
}
