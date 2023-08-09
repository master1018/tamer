public class SctpStdSocketOption<T>
    implements SctpSocketOption<T>
{
    public static final int SCTP_DISABLE_FRAGMENTS = 1;
    public static final int SCTP_EXPLICIT_COMPLETE = 2;
    public static final int SCTP_FRAGMENT_INTERLEAVE = 3;
    public static final int SCTP_NODELAY = 4;
    public static final int SO_SNDBUF = 5;
    public static final int SO_RCVBUF = 6;
    public static final int SO_LINGER = 7;
    private final String name;
    private final Class<T> type;
    private int constValue;
    public SctpStdSocketOption(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }
    public SctpStdSocketOption(String name, Class<T> type, int constValue) {
        this.name = name;
        this.type = type;
        this.constValue = constValue;
    }
    @Override
    public String name() {
        return name;
    }
    @Override
    public Class<T> type() {
        return type;
    }
    @Override
    public String toString() {
        return name;
    }
    int constValue() {
        return constValue;
    }
}
