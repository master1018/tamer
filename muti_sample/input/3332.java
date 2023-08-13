public class ConstantCallSite extends CallSite {
    private final boolean isFrozen;
    public ConstantCallSite(MethodHandle target) {
        super(target);
        isFrozen = true;
    }
    protected ConstantCallSite(MethodType targetType, MethodHandle createTargetHook) throws Throwable {
        super(targetType, createTargetHook);
        isFrozen = true;
    }
    @Override public final MethodHandle getTarget() {
        if (!isFrozen)  throw new IllegalStateException();
        return target;
    }
    @Override public final void setTarget(MethodHandle ignore) {
        throw new UnsupportedOperationException();
    }
    @Override
    public final MethodHandle dynamicInvoker() {
        return getTarget();
    }
}
