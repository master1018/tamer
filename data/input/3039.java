class Flag {
    private String name;
    private Object value;
    private Origin origin;
    private boolean writeable;
    private boolean external;
    Flag(String name, Object value, boolean writeable,
         boolean external, Origin origin) {
        this.name = name;
        this.value = value == null ? "" : value ;
        this.origin = origin;
        this.writeable = writeable;
        this.external = external;
    }
    Object getValue() {
        return value;
    }
    boolean isWriteable() {
        return writeable;
    }
    boolean isExternal() {
        return external;
    }
    VMOption getVMOption() {
        return new VMOption(name, value.toString(), writeable, origin);
    }
    static Flag getFlag(String name) {
        String[] names = new String[1];
        names[0] = name;
        List<Flag> flags = getFlags(names, 1);
        if (flags.isEmpty()) {
            return null;
        } else {
            return flags.get(0);
        }
    }
    static List<Flag> getAllFlags() {
        int numFlags = getInternalFlagCount();
        return getFlags(null, numFlags);
    }
    private static List<Flag> getFlags(String[] names, int numFlags) {
        Flag[] flags = new Flag[numFlags];
        int count = getFlags(names, flags, numFlags);
        List<Flag> result = new ArrayList<Flag>();
        for (Flag f : flags) {
            if (f != null) {
                result.add(f);
            }
        }
        return result;
    }
    private static native String[] getAllFlagNames();
    private static native int getFlags(String[] names, Flag[] flags, int count);
    private static native int getInternalFlagCount();
    static synchronized native void setLongValue(String name, long value);
    static synchronized native void setBooleanValue(String name, boolean value);
    static synchronized native void setStringValue(String name, String value);
    static {
        initialize();
    }
    private static native void initialize();
}
