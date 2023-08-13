public class ExceptionSpec extends EventRequestSpec {
    boolean notifyCaught;
    boolean notifyUncaught;
    ExceptionSpec(EventRequestSpecList specs, ReferenceTypeSpec refSpec,
                  boolean notifyCaught, boolean notifyUncaught)
    {
        super(specs, refSpec);
        this.notifyCaught = notifyCaught;
        this.notifyUncaught = notifyUncaught;
    }
    @Override
    void notifySet(SpecListener listener, SpecEvent evt) {
        listener.exceptionInterceptSet(evt);
    }
    @Override
    void notifyDeferred(SpecListener listener, SpecEvent evt) {
        listener.exceptionInterceptDeferred(evt);
    }
    @Override
    void notifyResolved(SpecListener listener, SpecEvent evt) {
        listener.exceptionInterceptResolved(evt);
    }
    @Override
    void notifyDeleted(SpecListener listener, SpecEvent evt) {
        listener.exceptionInterceptDeleted(evt);
    }
    @Override
    void notifyError(SpecListener listener, SpecErrorEvent evt) {
        listener.exceptionInterceptError(evt);
    }
    @Override
    void resolve(ReferenceType refType) {
        setRequest(refType.virtualMachine().eventRequestManager()
                   .createExceptionRequest(refType,
                                           notifyCaught, notifyUncaught));
    }
    @Override
    public int hashCode() {
        return refSpec.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExceptionSpec) {
            ExceptionSpec es = (ExceptionSpec)obj;
            return refSpec.equals(es.refSpec);
        } else {
            return false;
        }
    }
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("exception catch ");
        buffer.append(refSpec.toString());
        return buffer.toString();
    }
}
