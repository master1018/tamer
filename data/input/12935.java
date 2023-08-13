public class SwitchPoint {
    private static final MethodHandle
        K_true  = MethodHandles.constant(boolean.class, true),
        K_false = MethodHandles.constant(boolean.class, false);
    private final MutableCallSite mcs;
    private final MethodHandle mcsInvoker;
    public SwitchPoint() {
        this.mcs = new MutableCallSite(K_true);
        this.mcsInvoker = mcs.dynamicInvoker();
    }
    public boolean hasBeenInvalidated() {
        return (mcs.getTarget() != K_true);
    }
    public MethodHandle guardWithTest(MethodHandle target, MethodHandle fallback) {
        if (mcs.getTarget() == K_false)
            return fallback;  
        return MethodHandles.guardWithTest(mcsInvoker, target, fallback);
    }
    public static void invalidateAll(SwitchPoint[] switchPoints) {
        if (switchPoints.length == 0)  return;
        MutableCallSite[] sites = new MutableCallSite[switchPoints.length];
        for (int i = 0; i < switchPoints.length; i++) {
            SwitchPoint spt = switchPoints[i];
            if (spt == null)  break;  
            sites[i] = spt.mcs;
            spt.mcs.setTarget(K_false);
        }
        MutableCallSite.syncAll(sites);
    }
}
