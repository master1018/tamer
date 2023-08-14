class DirectMethodHandle extends MethodHandle {
    private final int  vmindex;     
    { vmindex = VM_INDEX_UNINITIALIZED; }  
    DirectMethodHandle(MethodType mtype, MemberName m, boolean doDispatch, Class<?> lookupClass) {
        super(mtype);
        assert(m.isMethod() || !doDispatch && m.isConstructor());
        if (!m.isResolved())
            throw new InternalError();
        MethodHandleNatives.init(this, (Object) m, doDispatch, lookupClass);
    }
    boolean isValid() {
        return (vmindex != VM_INDEX_UNINITIALIZED);
    }
}
