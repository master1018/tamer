public class Root {
    private long id;            
    private long refererId;     
    private int index = -1;             
    private int type;
    private String description;
    private JavaHeapObject referer = null;
    private StackTrace stackTrace = null;
    public final static int INVALID_TYPE = 0;
    public final static int UNKNOWN = 1;
    public final static int SYSTEM_CLASS = 2;
    public final static int NATIVE_LOCAL = 3;
    public final static int NATIVE_STATIC = 4;
    public final static int THREAD_BLOCK = 5;
    public final static int BUSY_MONITOR = 6;
    public final static int JAVA_LOCAL = 7;
    public final static int NATIVE_STACK = 8;
    public final static int JAVA_STATIC = 9;
    public Root(long id, long refererId, int type, String description) {
        this(id, refererId, type, description, null);
    }
    public Root(long id, long refererId, int type, String description,
                StackTrace stackTrace) {
        this.id = id;
        this.refererId = refererId;
        this.type = type;
        this.description = description;
        this.stackTrace = stackTrace;
    }
    public long getId() {
        return id;
    }
    public String getIdString() {
        return Misc.toHex(id);
    }
    public String getDescription() {
        if ("".equals(description)) {
            return getTypeName() + " Reference";
        } else {
            return description;
        }
    }
    public int getType() {
        return type;
    }
    public String getTypeName() {
        switch(type) {
            case INVALID_TYPE:          return "Invalid (?!?)";
            case UNKNOWN:               return "Unknown";
            case SYSTEM_CLASS:          return "System Class";
            case NATIVE_LOCAL:          return "JNI Local";
            case NATIVE_STATIC:         return "JNI Global";
            case THREAD_BLOCK:          return "Thread Block";
            case BUSY_MONITOR:          return "Busy Monitor";
            case JAVA_LOCAL:            return "Java Local";
            case NATIVE_STACK:          return "Native Stack (possibly Java local)";
            case JAVA_STATIC:           return "Java Static";
            default:                    return "??";
        }
    }
    public Root mostInteresting(Root other) {
        if (other.type > this.type) {
            return other;
        } else {
            return this;
        }
    }
    public JavaHeapObject getReferer() {
        return referer;
    }
    public StackTrace getStackTrace() {
        return stackTrace;
    }
    public int getIndex() {
        return index;
    }
    void resolve(Snapshot ss) {
        if (refererId != 0) {
            referer = ss.findThing(refererId);
        }
        if (stackTrace != null) {
            stackTrace.resolve(ss);
        }
    }
    void setIndex(int i) {
        index = i;
    }
}
