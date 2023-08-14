public class TypeTags {
    private TypeTags() {} 
    public static final int BYTE = 1;
    public static final int CHAR = BYTE+1;
    public static final int SHORT = CHAR+1;
    public static final int INT = SHORT+1;
    public static final int LONG = INT+1;
    public static final int FLOAT = LONG+1;
    public static final int DOUBLE = FLOAT+1;
    public static final int BOOLEAN = DOUBLE+1;
    public static final int VOID = BOOLEAN+1;
    public static final int CLASS = VOID+1;
    public static final int ARRAY = CLASS+1;
    public static final int METHOD = ARRAY+1;
    public static final int PACKAGE = METHOD+1;
    public static final int TYPEVAR = PACKAGE+1;
    public static final int WILDCARD = TYPEVAR+1;
    public static final int FORALL = WILDCARD+1;
    public static final int BOT = FORALL+1;
    public static final int NONE = BOT+1;
    public static final int ERROR = NONE+1;
    public static final int UNKNOWN = ERROR+1;
    public static final int UNDETVAR = UNKNOWN+1;
    public static final int TypeTagCount = UNDETVAR+1;
    public static final int lastBaseTag = BOOLEAN;
    public static final int firstPartialTag = ERROR;
}
