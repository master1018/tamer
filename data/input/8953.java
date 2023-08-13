public class VolatileCallSite extends CallSite {
    public VolatileCallSite(MethodType type) {
        super(type);
    }
    public VolatileCallSite(MethodHandle target) {
        super(target);
    }
    @Override public final MethodHandle getTarget() {
        return getTargetVolatile();
    }
    @Override public void setTarget(MethodHandle newTarget) {
        checkTargetChange(getTargetVolatile(), newTarget);
        setTargetVolatile(newTarget);
    }
    @Override
    public final MethodHandle dynamicInvoker() {
        return makeDynamicInvoker();
    }
}
