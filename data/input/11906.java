public class Kinds {
    private Kinds() {} 
    public final static int NIL = 0;
    public final static int PCK = 1 << 0;
    public final static int TYP = 1 << 1;
    public final static int VAR = 1 << 2;
    public final static int VAL = (1 << 3) | VAR;
    public final static int MTH = 1 << 4;
    public final static int ERR = (1 << 5) - 1;
    public final static int AllKinds = ERR;
    public static final int ERRONEOUS = 1 << 6;
    public static final int AMBIGUOUS    = ERRONEOUS+1; 
    public static final int HIDDEN       = ERRONEOUS+2; 
    public static final int STATICERR    = ERRONEOUS+3; 
    public static final int ABSENT_VAR   = ERRONEOUS+4; 
    public static final int WRONG_MTHS   = ERRONEOUS+5; 
    public static final int WRONG_MTH    = ERRONEOUS+6; 
    public static final int ABSENT_MTH   = ERRONEOUS+7; 
    public static final int ABSENT_TYP   = ERRONEOUS+8; 
    public enum KindName implements Formattable {
        ANNOTATION("kindname.annotation"),
        CONSTRUCTOR("kindname.constructor"),
        INTERFACE("kindname.interface"),
        ENUM("kindname.enum"),
        STATIC("kindname.static"),
        TYPEVAR("kindname.type.variable"),
        BOUND("kindname.type.variable.bound"),
        VAR("kindname.variable"),
        VAL("kindname.value"),
        METHOD("kindname.method"),
        CLASS("kindname.class"),
        PACKAGE("kindname.package");
        private String name;
        KindName(String name) {
            this.name = name;
        }
        public String toString() {
            return name;
        }
        public String getKind() {
            return "Kindname";
        }
        public String toString(Locale locale, Messages messages) {
            String s = toString();
            return messages.getLocalizedString(locale, "compiler.misc." + s);
        }
    }
    public static KindName kindName(int kind) {
        switch (kind) {
        case PCK: return KindName.PACKAGE;
        case TYP: return KindName.CLASS;
        case VAR: return KindName.VAR;
        case VAL: return KindName.VAL;
        case MTH: return KindName.METHOD;
            default : throw new AssertionError("Unexpected kind: "+kind);
        }
    }
    public static KindName kindName(Symbol sym) {
        switch (sym.getKind()) {
        case PACKAGE:
            return KindName.PACKAGE;
        case ENUM:
            return KindName.ENUM;
        case ANNOTATION_TYPE:
        case CLASS:
            return KindName.CLASS;
        case INTERFACE:
            return KindName.INTERFACE;
        case TYPE_PARAMETER:
            return KindName.TYPEVAR;
        case ENUM_CONSTANT:
        case FIELD:
        case PARAMETER:
        case LOCAL_VARIABLE:
        case EXCEPTION_PARAMETER:
        case RESOURCE_VARIABLE:
            return KindName.VAR;
        case CONSTRUCTOR:
            return KindName.CONSTRUCTOR;
        case METHOD:
        case STATIC_INIT:
        case INSTANCE_INIT:
            return KindName.METHOD;
        default:
            if (sym.kind == VAL)
                return KindName.VAL;
            else
                throw new AssertionError("Unexpected kind: "+sym.getKind());
        }
    }
    public static EnumSet<KindName> kindNames(int kind) {
        EnumSet<KindName> kinds = EnumSet.noneOf(KindName.class);
        if ((kind & VAL) != 0)
            kinds.add(((kind & VAL) == VAR) ? KindName.VAR : KindName.VAL);
        if ((kind & MTH) != 0) kinds.add(KindName.METHOD);
        if ((kind & TYP) != 0) kinds.add(KindName.CLASS);
        if ((kind & PCK) != 0) kinds.add(KindName.PACKAGE);
        return kinds;
    }
    public static KindName typeKindName(Type t) {
        if (t.tag == TYPEVAR ||
            t.tag == CLASS && (t.tsym.flags() & COMPOUND) != 0)
            return KindName.BOUND;
        else if (t.tag == PACKAGE)
            return KindName.PACKAGE;
        else if ((t.tsym.flags_field & ANNOTATION) != 0)
            return KindName.ANNOTATION;
        else if ((t.tsym.flags_field & INTERFACE) != 0)
            return KindName.INTERFACE;
        else
            return KindName.CLASS;
    }
    public static KindName absentKind(int kind) {
        switch (kind) {
        case ABSENT_VAR:
            return KindName.VAR;
        case WRONG_MTHS: case WRONG_MTH: case ABSENT_MTH:
            return KindName.METHOD;
        case ABSENT_TYP:
            return KindName.CLASS;
        default:
            throw new AssertionError("Unexpected kind: "+kind);
        }
    }
}
