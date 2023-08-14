class DopInfo {
    static Hashtable<String, DopInfo> dopsTable;
    public String name; 
    public Dop opcode; 
    public String args; 
    public final static String ARG_REGISTER = "R";
    public final static String ARG_TYPE = "T";
    public final static String ARG_LITERAL = "I";
    public final static String ARG_ADDRESS = "A";
    public final static String ARG_FIELD = "F";
    public final static String ARG_METHOD = "M";
    public final static String ARG_INTFMETHOD = "Y";
    public final static String ARG_STRING = "S";
    public final static String ARG_REGLIST = "Z";
    public final static String ARG_REGRANGE = "G";
    public final static String ARG_REG_REG = ARG_REGISTER + ARG_REGISTER;
    public final static String ARG_REG_REG_REG = ARG_REGISTER + ARG_REGISTER
            + ARG_REGISTER;
    public final static String ARG_REG_LITERAL = ARG_REGISTER + ARG_LITERAL;
    public final static String ARG_REG_REG_LITERAL = ARG_REGISTER
            + ARG_REGISTER + ARG_LITERAL;
    public final static String ARG_REG_ADDRESS = ARG_REGISTER + ARG_ADDRESS;
    public final static String ARG_REG_REG_ADDRESS = ARG_REGISTER
            + ARG_REGISTER + ARG_ADDRESS;
    public final static String ARG_REG_TYPE = ARG_REGISTER + ARG_TYPE;
    public final static String ARG_REG_REG_TYPE = ARG_REGISTER + ARG_REGISTER
            + ARG_TYPE;
    public final static String ARG_REG_FIELD = ARG_REGISTER + ARG_FIELD;
    public final static String ARG_REG_REG_FIELD = ARG_REGISTER + ARG_REGISTER
            + ARG_FIELD;
    public final static String ARG_REG_STRING = ARG_REGISTER + ARG_STRING;
    public final static String ARG_REG_REG_STRING = ARG_REGISTER + ARG_REGISTER
            + ARG_STRING;
    public final static String ARG_REGLIST_TYPE = ARG_REGLIST + ARG_TYPE;
    public final static String ARG_REGLIST_METHOD = ARG_REGLIST + ARG_METHOD;
    public final static String ARG_REGLIST_INTFMETHOD = ARG_REGLIST
            + ARG_INTFMETHOD;
    public final static String ARG_REGRANGE_TYPE = ARG_REGRANGE + ARG_TYPE;
    public final static String ARG_REGRANGE_METHOD = ARG_REGRANGE + ARG_METHOD;
    public final static String ARG_REGRANGE_INTFMETHOD = ARG_REGRANGE
            + ARG_INTFMETHOD;
    public static DopInfo get(String name) {
        return (DopInfo) dopsTable.get(name);
    }
    public static boolean contains(String name) {
        return dopsTable.get(name) != null;
    }
    static private void add(String name, Dop opcode, String args) {
        DopInfo info = new DopInfo();
        info.name = name;
        info.opcode = opcode;
        info.args = args;
        dopsTable.put(name, info);
    }
    static private String getArgsFormat(Dop dop) {
        InsnFormat format = dop.getFormat();
        if (format instanceof Form10x) return "";
        if (format instanceof Form12x) return ARG_REG_REG;
        if (format instanceof Form11n) return ARG_REG_LITERAL;
        if (format instanceof Form11x) return ARG_REGISTER;
        if (format instanceof Form10t || format instanceof Form20t)
            return ARG_ADDRESS;
        if (format instanceof Form22x) return ARG_REG_REG;
        if (format instanceof Form21t) return ARG_REG_ADDRESS;
        if (format instanceof Form21s) return ARG_REG_LITERAL;
        if (format instanceof Form21h) return ARG_REG_LITERAL;
        if (format instanceof Form21c) {
            switch (dop.getOpcode()) {
            case DalvOps.CONST_CLASS:
            case DalvOps.CHECK_CAST:
            case DalvOps.NEW_INSTANCE:
                return ARG_REG_TYPE;
            case DalvOps.SGET:
            case DalvOps.SGET_WIDE:
            case DalvOps.SGET_OBJECT:
            case DalvOps.SGET_BOOLEAN:
            case DalvOps.SGET_BYTE:
            case DalvOps.SGET_CHAR:
            case DalvOps.SGET_SHORT:
            case DalvOps.SPUT:
            case DalvOps.SPUT_WIDE:
            case DalvOps.SPUT_OBJECT:
            case DalvOps.SPUT_BOOLEAN:
            case DalvOps.SPUT_BYTE:
            case DalvOps.SPUT_CHAR:
            case DalvOps.SPUT_SHORT:
                return ARG_REG_FIELD;
            default:
                return ARG_REG_STRING;
            }
        }
        if (format instanceof Form23x) return ARG_REG_REG_REG;
        if (format instanceof Form22b) return ARG_REG_REG_LITERAL;
        if (format instanceof Form22t) return ARG_REG_REG_ADDRESS;
        if (format instanceof Form22s) return ARG_REG_REG_LITERAL;
        if (format instanceof Form22c) {
            switch (dop.getOpcode()) {
            case DalvOps.INSTANCE_OF:
            case DalvOps.NEW_ARRAY:
                return ARG_REG_REG_TYPE;
            case DalvOps.IGET:
            case DalvOps.IGET_WIDE:
            case DalvOps.IGET_OBJECT:
            case DalvOps.IGET_BOOLEAN:
            case DalvOps.IGET_BYTE:
            case DalvOps.IGET_CHAR:
            case DalvOps.IGET_SHORT:
            case DalvOps.IPUT:
            case DalvOps.IPUT_WIDE:
            case DalvOps.IPUT_OBJECT:
            case DalvOps.IPUT_BOOLEAN:
            case DalvOps.IPUT_BYTE:
            case DalvOps.IPUT_CHAR:
            case DalvOps.IPUT_SHORT:
                return ARG_REG_REG_FIELD;
            default:
                return ARG_REG_REG_STRING;
            }
        }
        if (format instanceof Form30t) return ARG_ADDRESS;
        if (format instanceof Form32x) return ARG_REG_REG;
        if (format instanceof Form31i) return ARG_REG_LITERAL;
        if (format instanceof Form31t) return ARG_REG_ADDRESS;
        if (format instanceof Form31c) return ARG_REG_STRING;
        if (format instanceof Form35c) {
            switch (dop.getOpcode()) {
            case DalvOps.INVOKE_VIRTUAL:
            case DalvOps.INVOKE_SUPER:
            case DalvOps.INVOKE_DIRECT:
            case DalvOps.INVOKE_STATIC:
                return ARG_REGLIST_METHOD;
            case DalvOps.INVOKE_INTERFACE:
                return ARG_REGLIST_INTFMETHOD;
            default:
                return ARG_REGLIST_TYPE;
            }
        }
        if (format instanceof Form3rc) {
            switch (dop.getOpcode()) {
            case DalvOps.FILLED_NEW_ARRAY_RANGE:
                return ARG_REGRANGE_TYPE;
            case DalvOps.INVOKE_INTERFACE_RANGE:
                return ARG_REGRANGE_INTFMETHOD;
            default:
                return ARG_REGRANGE_METHOD;
            }
        }
        if (format instanceof Form51l) return ARG_REG_LITERAL; 
        return "";
    }
    static {
        dopsTable = new Hashtable<String, DopInfo>();
        for (int i = 0; i < DalvOps.MAX_VALUE - DalvOps.MIN_VALUE + 1; i++) {
            try {
                Dop dop = Dops.get(i);
                add(dop.getName(), dop, getArgsFormat(dop));
            } catch (Exception e) {
            }
        }
    }
};
