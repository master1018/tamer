class InvokeGeneric {
    private final MethodType erasedCallerType;
    private final MethodHandle initialInvoker;
     InvokeGeneric(MethodType erasedCallerType) throws ReflectiveOperationException {
        assert(erasedCallerType.equals(erasedCallerType.erase()));
        this.erasedCallerType = erasedCallerType;
        this.initialInvoker = makeInitialInvoker();
        assert initialInvoker.type().equals(erasedCallerType
                                            .insertParameterTypes(0, MethodType.class, MethodHandle.class))
            : initialInvoker.type();
    }
    private static MethodHandles.Lookup lookup() {
        return IMPL_LOOKUP;
    }
     static MethodHandle generalInvokerOf(MethodType erasedCallerType) throws ReflectiveOperationException {
        InvokeGeneric gen = new InvokeGeneric(erasedCallerType);
        return gen.initialInvoker;
    }
    private MethodHandle makeInitialInvoker() throws ReflectiveOperationException {
        MethodHandle postDispatch = makePostDispatchInvoker();
        MethodHandle invoker;
        if (returnConversionPossible()) {
            invoker = MethodHandles.foldArguments(postDispatch,
                                                  dispatcher("dispatchWithConversion"));
        } else {
            invoker = MethodHandles.foldArguments(postDispatch, dispatcher("dispatch"));
        }
        return invoker;
    }
    private static final Class<?>[] EXTRA_ARGS = { MethodType.class, MethodHandle.class };
    private MethodHandle makePostDispatchInvoker() {
        MethodType invokerType = erasedCallerType.insertParameterTypes(0, EXTRA_ARGS);
        return invokerType.invokers().exactInvoker();
    }
    private MethodHandle dropDispatchArguments(MethodHandle targetInvoker) {
        assert(targetInvoker.type().parameterType(0) == MethodHandle.class);
        return MethodHandles.dropArguments(targetInvoker, 1, EXTRA_ARGS);
    }
    private MethodHandle dispatcher(String dispatchName) throws ReflectiveOperationException {
        return lookup().bind(this, dispatchName,
                             MethodType.methodType(MethodHandle.class,
                                                   MethodType.class, MethodHandle.class));
    }
    static final boolean USE_AS_TYPE_PATH = true;
    private MethodHandle dispatch(MethodType callerType, MethodHandle target) {
        MethodType targetType = target.type();
        if (USE_AS_TYPE_PATH || target.isVarargsCollector()) {
            MethodHandle newTarget = target.asType(callerType);
            targetType = callerType;
            Invokers invokers = targetType.invokers();
            MethodHandle invoker = invokers.erasedInvokerWithDrops;
            if (invoker == null) {
                invokers.erasedInvokerWithDrops = invoker =
                    dropDispatchArguments(invokers.erasedInvoker());
            }
            return invoker.bindTo(newTarget);
        }
        throw new RuntimeException("NYI");
    }
    private MethodHandle dispatchWithConversion(MethodType callerType, MethodHandle target) {
        MethodHandle finisher = dispatch(callerType, target);
        if (returnConversionNeeded(callerType, target))
            finisher = addReturnConversion(finisher, callerType.returnType());  
        return finisher;
    }
    private boolean returnConversionPossible() {
        Class<?> needType = erasedCallerType.returnType();
        return !needType.isPrimitive();
    }
    private boolean returnConversionNeeded(MethodType callerType, MethodHandle target) {
        Class<?> needType = callerType.returnType();
        if (needType == erasedCallerType.returnType())
            return false;  
        Class<?> haveType = target.type().returnType();
        if (VerifyType.isNullConversion(haveType, needType) && !needType.isInterface())
            return false;
        return true;
    }
    private MethodHandle addReturnConversion(MethodHandle finisher, Class<?> type) {
        MethodType finisherType = finisher.type();
        MethodHandle caster = ValueConversions.identity(type);
        caster = caster.asType(caster.type().changeParameterType(0, finisherType.returnType()));
        finisher = MethodHandles.filterReturnValue(finisher, caster);
        return finisher.asType(finisherType);
    }
    public String toString() {
        return "InvokeGeneric"+erasedCallerType;
    }
}
