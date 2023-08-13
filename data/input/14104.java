public abstract class BreakpointSpec extends EventRequestSpec {
    BreakpointSpec(EventRequestSpecList specs, ReferenceTypeSpec refSpec) {
        super(specs, refSpec);
    }
    @Override
    void notifySet(SpecListener listener, SpecEvent evt) {
        listener.breakpointSet(evt);
    }
    @Override
    void notifyDeferred(SpecListener listener, SpecEvent evt) {
        listener.breakpointDeferred(evt);
    }
    @Override
    void notifyResolved(SpecListener listener, SpecEvent evt) {
        listener.breakpointResolved(evt);
    }
    @Override
    void notifyDeleted(SpecListener listener, SpecEvent evt) {
        listener.breakpointDeleted(evt);
    }
    @Override
    void notifyError(SpecListener listener, SpecErrorEvent evt) {
        listener.breakpointError(evt);
    }
}
