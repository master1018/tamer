public class MutableCallSite extends CallSite {
    public MutableCallSite(MethodType type) {
        super(type);
    }
    public MutableCallSite(MethodHandle target) {
        super(target);
    }
    @Override public final MethodHandle getTarget() {
        return target;
    }
    @Override public void setTarget(MethodHandle newTarget) {
        checkTargetChange(this.target, newTarget);
        setTargetNormal(newTarget);
    }
    @Override
    public final MethodHandle dynamicInvoker() {
        return makeDynamicInvoker();
    }
    public static void syncAll(MutableCallSite[] sites) {
        if (sites.length == 0)  return;
        STORE_BARRIER.lazySet(0);
        for (int i = 0; i < sites.length; i++) {
            sites[i].getClass();  
        }
    }
    private static final AtomicInteger STORE_BARRIER = new AtomicInteger();
}
