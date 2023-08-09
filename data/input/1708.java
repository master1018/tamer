class Invokers {
    private final MethodType targetType;
    private  MethodHandle exactInvoker;
    private  MethodHandle erasedInvoker;
     MethodHandle erasedInvokerWithDrops;  
    private  MethodHandle generalInvoker;
    private  MethodHandle varargsInvoker;
    private final  MethodHandle[] spreadInvokers;
    private  MethodHandle uninitializedCallSite;
     Invokers(MethodType targetType) {
        this.targetType = targetType;
        this.spreadInvokers = new MethodHandle[targetType.parameterCount()+1];
    }
     static MethodType invokerType(MethodType targetType) {
        return targetType.insertParameterTypes(0, MethodHandle.class);
    }
     MethodHandle exactInvoker() {
        MethodHandle invoker = exactInvoker;
        if (invoker != null)  return invoker;
        invoker = lookupInvoker("invokeExact");
        exactInvoker = invoker;
        return invoker;
    }
     MethodHandle generalInvoker() {
        MethodHandle invoker = generalInvoker;
        if (invoker != null)  return invoker;
        invoker = lookupInvoker("invoke");
        generalInvoker = invoker;
        return invoker;
    }
    private MethodHandle lookupInvoker(String name) {
        MethodHandle invoker;
        try {
            invoker = IMPL_LOOKUP.findVirtual(MethodHandle.class, name, targetType);
        } catch (ReflectiveOperationException ex) {
            throw new InternalError("JVM cannot find invoker for "+targetType);
        }
        assert(invokerType(targetType) == invoker.type());
        assert(!invoker.isVarargsCollector());
        return invoker;
    }
     MethodHandle erasedInvoker() {
        MethodHandle xinvoker = exactInvoker();
        MethodHandle invoker = erasedInvoker;
        if (invoker != null)  return invoker;
        MethodType erasedType = targetType.erase();
        invoker = xinvoker.asType(invokerType(erasedType));
        erasedInvoker = invoker;
        return invoker;
    }
     MethodHandle spreadInvoker(int leadingArgCount) {
        MethodHandle vaInvoker = spreadInvokers[leadingArgCount];
        if (vaInvoker != null)  return vaInvoker;
        MethodHandle gInvoker = generalInvoker();
        int spreadArgCount = targetType.parameterCount() - leadingArgCount;
        vaInvoker = gInvoker.asSpreader(Object[].class, spreadArgCount);
        spreadInvokers[leadingArgCount] = vaInvoker;
        return vaInvoker;
    }
     MethodHandle varargsInvoker() {
        MethodHandle vaInvoker = varargsInvoker;
        if (vaInvoker != null)  return vaInvoker;
        vaInvoker = spreadInvoker(0).asType(invokerType(MethodType.genericMethodType(0, true)));
        varargsInvoker = vaInvoker;
        return vaInvoker;
    }
    private static MethodHandle THROW_UCS = null;
     MethodHandle uninitializedCallSite() {
        MethodHandle invoker = uninitializedCallSite;
        if (invoker != null)  return invoker;
        if (targetType.parameterCount() > 0) {
            MethodType type0 = targetType.dropParameterTypes(0, targetType.parameterCount());
            Invokers invokers0 = type0.invokers();
            invoker = MethodHandles.dropArguments(invokers0.uninitializedCallSite(),
                                                  0, targetType.parameterList());
            assert(invoker.type().equals(targetType));
            uninitializedCallSite = invoker;
            return invoker;
        }
        if (THROW_UCS == null) {
            try {
                THROW_UCS = IMPL_LOOKUP
                    .findStatic(CallSite.class, "uninitializedCallSite",
                                MethodType.methodType(Empty.class));
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
        invoker = AdapterMethodHandle.makeRetypeRaw(targetType, THROW_UCS);
        assert(invoker.type().equals(targetType));
        uninitializedCallSite = invoker;
        return invoker;
    }
    public String toString() {
        return "Invokers"+targetType;
    }
}
