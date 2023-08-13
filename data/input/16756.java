class MethodHandleNatives {
    private MethodHandleNatives() { } 
    static native void init(MemberName self, Object ref);
    static native void expand(MemberName self);
    static native void resolve(MemberName self, Class<?> caller);
    static native int getMembers(Class<?> defc, String matchName, String matchSig,
            int matchFlags, Class<?> caller, int skip, MemberName[] results);
    static native void init(AdapterMethodHandle self, MethodHandle target, int argnum);
    static native void init(BoundMethodHandle self, Object target, int argnum);
    static native void init(DirectMethodHandle self, Object ref, boolean doDispatch, Class<?> caller);
    static native void init(MethodType self);
    static native void registerBootstrap(Class<?> caller, MethodHandle bootstrapMethod);
    static native MethodHandle getBootstrap(Class<?> caller);
    static native void setCallSiteTarget(CallSite site, MethodHandle target);
    static native Object getTarget(MethodHandle self, int format);
    static MemberName getMethodName(MethodHandle self) {
        return (MemberName) getTarget(self, ETF_METHOD_NAME);
    }
    static AccessibleObject getTargetMethod(MethodHandle self) {
        return (AccessibleObject) getTarget(self, ETF_REFLECT_METHOD);
    }
    static Object getTargetInfo(MethodHandle self) {
        return getTarget(self, ETF_HANDLE_OR_METHOD_NAME);
    }
    static Object[] makeTarget(Class<?> defc, String name, String sig, int mods, Class<?> refc) {
        return new Object[] { defc, name, sig, mods, refc };
    }
    static native int getConstant(int which);
    static final int JVM_PUSH_LIMIT;
    static final int JVM_STACK_MOVE_UNIT;
    static final int CONV_OP_IMPLEMENTED_MASK;
    static final boolean HAVE_RICOCHET_FRAMES;
    static final int OP_ROT_ARGS_DOWN_LIMIT_BIAS;
    private static native void registerNatives();
    static {
        registerNatives();
        int k;
        JVM_PUSH_LIMIT              = getConstant(Constants.GC_JVM_PUSH_LIMIT);
        JVM_STACK_MOVE_UNIT         = getConstant(Constants.GC_JVM_STACK_MOVE_UNIT);
        k                           = getConstant(Constants.GC_CONV_OP_IMPLEMENTED_MASK);
        CONV_OP_IMPLEMENTED_MASK    = (k != 0) ? k : DEFAULT_CONV_OP_IMPLEMENTED_MASK;
        k                           = getConstant(Constants.GC_OP_ROT_ARGS_DOWN_LIMIT_BIAS);
        OP_ROT_ARGS_DOWN_LIMIT_BIAS = (k != 0) ? (byte)k : -1;
        HAVE_RICOCHET_FRAMES        = (CONV_OP_IMPLEMENTED_MASK & (1<<OP_COLLECT_ARGS)) != 0;
    }
    static class Constants {
        Constants() { } 
        static final int 
                GC_JVM_PUSH_LIMIT = 0,
                GC_JVM_STACK_MOVE_UNIT = 1,
                GC_CONV_OP_IMPLEMENTED_MASK = 2,
                GC_OP_ROT_ARGS_DOWN_LIMIT_BIAS = 3;
        static final int
                ETF_HANDLE_OR_METHOD_NAME = 0, 
                ETF_DIRECT_HANDLE         = 1, 
                ETF_METHOD_NAME           = 2, 
                ETF_REFLECT_METHOD        = 3; 
        static final int
                MN_IS_METHOD           = 0x00010000, 
                MN_IS_CONSTRUCTOR      = 0x00020000, 
                MN_IS_FIELD            = 0x00040000, 
                MN_IS_TYPE             = 0x00080000, 
                MN_SEARCH_SUPERCLASSES = 0x00100000, 
                MN_SEARCH_INTERFACES   = 0x00200000, 
                VM_INDEX_UNINITIALIZED = -99;
        static final int
            ARG_SLOT_PUSH_SHIFT = 16,
            ARG_SLOT_MASK = (1<<ARG_SLOT_PUSH_SHIFT)-1;
        static final int
            OP_RETYPE_ONLY   = 0x0, 
            OP_RETYPE_RAW    = 0x1, 
            OP_CHECK_CAST    = 0x2, 
            OP_PRIM_TO_PRIM  = 0x3, 
            OP_REF_TO_PRIM   = 0x4, 
            OP_PRIM_TO_REF   = 0x5, 
            OP_SWAP_ARGS     = 0x6, 
            OP_ROT_ARGS      = 0x7, 
            OP_DUP_ARGS      = 0x8, 
            OP_DROP_ARGS     = 0x9, 
            OP_COLLECT_ARGS  = 0xA, 
            OP_SPREAD_ARGS   = 0xB, 
            OP_FOLD_ARGS     = 0xC, 
            CONV_OP_LIMIT    = 0xE; 
        static final int
            CONV_OP_MASK     = 0xF00, 
            CONV_TYPE_MASK   = 0x0F,  
            CONV_VMINFO_MASK = 0x0FF, 
            CONV_VMINFO_SHIFT     =  0, 
            CONV_OP_SHIFT         =  8, 
            CONV_DEST_TYPE_SHIFT  = 12, 
            CONV_SRC_TYPE_SHIFT   = 16, 
            CONV_STACK_MOVE_SHIFT = 20, 
            CONV_STACK_MOVE_MASK  = (1 << (32 - CONV_STACK_MOVE_SHIFT)) - 1;
        static final int DEFAULT_CONV_OP_IMPLEMENTED_MASK =
                ((1<<OP_RETYPE_ONLY)
                |(1<<OP_RETYPE_RAW)
                |(1<<OP_CHECK_CAST)
                |(1<<OP_PRIM_TO_PRIM)
                |(1<<OP_REF_TO_PRIM)
                |(1<<OP_SWAP_ARGS)
                |(1<<OP_ROT_ARGS)
                |(1<<OP_DUP_ARGS)
                |(1<<OP_DROP_ARGS)
                );
        static final int
            T_BOOLEAN  =  4,
            T_CHAR     =  5,
            T_FLOAT    =  6,
            T_DOUBLE   =  7,
            T_BYTE     =  8,
            T_SHORT    =  9,
            T_INT      = 10,
            T_LONG     = 11,
            T_OBJECT   = 12,
            T_VOID     = 14,
            T_ILLEGAL  = 99;
        static final int
            REF_getField                = 1,
            REF_getStatic               = 2,
            REF_putField                = 3,
            REF_putStatic               = 4,
            REF_invokeVirtual           = 5,
            REF_invokeStatic            = 6,
            REF_invokeSpecial           = 7,
            REF_newInvokeSpecial        = 8,
            REF_invokeInterface         = 9;
    }
    private static native int getNamedCon(int which, Object[] name);
    static boolean verifyConstants() {
        Object[] box = { null };
        for (int i = 0; ; i++) {
            box[0] = null;
            int vmval = getNamedCon(i, box);
            if (box[0] == null)  break;
            String name = (String) box[0];
            try {
                Field con = Constants.class.getDeclaredField(name);
                int jval = con.getInt(null);
                if (jval == vmval)  continue;
                String err = (name+": JVM has "+vmval+" while Java has "+jval);
                if (name.equals("CONV_OP_LIMIT")) {
                    System.err.println("warning: "+err);
                    continue;
                }
                throw new InternalError(err);
            } catch (Exception ex) {
                if (ex instanceof NoSuchFieldException) {
                    String err = (name+": JVM has "+vmval+" which Java does not define");
                    if (name.startsWith("OP_") || name.startsWith("GC_")) {
                        System.err.println("warning: "+err);
                        continue;
                    }
                }
                throw new InternalError(name+": access failed, got "+ex);
            }
        }
        return true;
    }
    static {
        assert(verifyConstants());
    }
    static CallSite makeDynamicCallSite(MethodHandle bootstrapMethod,
                                        String name, MethodType type,
                                        Object info,
                                        MemberName callerMethod, int callerBCI) {
        return CallSite.makeSite(bootstrapMethod, name, type, info, callerMethod, callerBCI);
    }
    static void checkSpreadArgument(Object av, int n) {
        MethodHandleStatics.checkSpreadArgument(av, n);
    }
    static MethodType findMethodHandleType(Class<?> rtype, Class<?>[] ptypes) {
        return MethodType.makeImpl(rtype, ptypes, true);
    }
    static void notifyGenericMethodType(MethodType type) {
        type.form().notifyGenericMethodType();
    }
    static void raiseException(int code, Object actual, Object required) {
        String message = null;
        switch (code) {
        case 190: 
            try {
                String reqLength = "";
                if (required instanceof AdapterMethodHandle) {
                    int conv = ((AdapterMethodHandle)required).getConversion();
                    int spChange = AdapterMethodHandle.extractStackMove(conv);
                    reqLength = " of length "+(spChange+1);
                }
                int actualLength = actual == null ? 0 : java.lang.reflect.Array.getLength(actual);
                message = "required array"+reqLength+", but encountered wrong length "+actualLength;
                break;
            } catch (IllegalArgumentException ex) {
            }
            required = Object[].class;  
            code = 192; 
            break;
        case 191: 
            if (required == BootstrapMethodError.class) {
                throw new BootstrapMethodError((Throwable) actual);
            }
            break;
        }
        if (message == null) {
            if (!(actual instanceof Class) && !(actual instanceof MethodType))
                actual = actual.getClass();
           if (actual != null)
               message = "required "+required+" but encountered "+actual;
           else
               message = "required "+required;
        }
        switch (code) {
        case 190: 
            throw new ArrayIndexOutOfBoundsException(message);
        case 50: 
            throw new ClassCastException(message);
        case 192: 
            throw new ClassCastException(message);
        default:
            throw new InternalError("unexpected code "+code+": "+message);
        }
    }
    static MethodHandle linkMethodHandleConstant(Class<?> callerClass, int refKind,
                                                 Class<?> defc, String name, Object type) {
        try {
            Lookup lookup = IMPL_LOOKUP.in(callerClass);
            return lookup.linkMethodHandleConstant(refKind, defc, name, type);
        } catch (ReflectiveOperationException ex) {
            Error err = new IncompatibleClassChangeError();
            err.initCause(ex);
            throw err;
        }
    }
    static boolean workaroundWithoutRicochetFrames() {
        assert(!HAVE_RICOCHET_FRAMES) : "this code should not be executed if `-XX:+UseRicochetFrames is enabled";
        return true;
    }
}
