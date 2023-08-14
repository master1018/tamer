class AccessSourcer {
    private final Output mOutput;
    public static int IS_CLASS  = 1;
    public static int IS_FIELD  = 2;
    public static int IS_METHOD = 4;
    private enum Flag {
        ACC_PUBLIC(Opcodes.ACC_PUBLIC               , IS_CLASS | IS_FIELD | IS_METHOD),
        ACC_PRIVATE(Opcodes.ACC_PRIVATE             , IS_CLASS | IS_FIELD | IS_METHOD),
        ACC_PROTECTED(Opcodes.ACC_PROTECTED         , IS_CLASS | IS_FIELD | IS_METHOD),
        ACC_STATIC(Opcodes.ACC_STATIC               , IS_FIELD | IS_METHOD),
        ACC_FINAL(Opcodes.ACC_FINAL                 , IS_CLASS | IS_FIELD | IS_METHOD),
        ACC_SUPER(Opcodes.ACC_SUPER                 , IS_CLASS),
        ACC_SYNCHRONIZED(Opcodes.ACC_SYNCHRONIZED   , IS_METHOD),
        ACC_VOLATILE(Opcodes.ACC_VOLATILE           , IS_FIELD),
        ACC_BRIDGE(Opcodes.ACC_BRIDGE               , IS_METHOD),
        ACC_VARARGS(Opcodes.ACC_VARARGS             , IS_METHOD),
        ACC_TRANSIENT(Opcodes.ACC_TRANSIENT         , IS_FIELD),
        ACC_NATIVE(Opcodes.ACC_NATIVE               , IS_METHOD),
        ACC_INTERFACE(Opcodes.ACC_INTERFACE         , IS_CLASS),
        ACC_ABSTRACT(Opcodes.ACC_ABSTRACT           , IS_CLASS | IS_METHOD),
        ACC_STRICT(Opcodes.ACC_STRICT               , IS_METHOD),
        ACC_SYNTHETIC(Opcodes.ACC_SYNTHETIC         , IS_CLASS | IS_FIELD | IS_METHOD),
        ACC_ANNOTATION(Opcodes.ACC_ANNOTATION       , IS_CLASS),
        ACC_ENUM(Opcodes.ACC_ENUM                   , IS_CLASS),
        ACC_DEPRECATED(Opcodes.ACC_DEPRECATED       , IS_CLASS | IS_FIELD | IS_METHOD)
        ;
        private final int mValue;
        private final int mFilter;
        private Flag(int value, int filter) {
            mValue = value;
            mFilter = filter;
        }
        public int getValue() {
            return mValue;
        }
        public int getFilter() {
            return mFilter;
        }
        @Override
        public String toString() {
            return super.toString().substring(4).toLowerCase();
        }
    }
    public AccessSourcer(Output output) {
        mOutput = output;
    }
    public void write(int access, int filter) {
        boolean need_sep = false;
        for (Flag f : Flag.values()) {
            if ((f.getFilter() & filter) != 0 && (access & f.getValue()) != 0) {
                if (need_sep) {
                    mOutput.write(" ");
                }
                mOutput.write(f.toString());
                need_sep = true;
            }
        }
    }
}
