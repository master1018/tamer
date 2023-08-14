public final class LocalSnapshot extends ZeroSizeInsn {
    private final RegisterSpecSet locals;
    public LocalSnapshot(SourcePosition position, RegisterSpecSet locals) {
        super(position);
        if (locals == null) {
            throw new NullPointerException("locals == null");
        }
        this.locals = locals;
    }
    @Override
    public DalvInsn withRegisterOffset(int delta) {
        return new LocalSnapshot(getPosition(), locals.withOffset(delta));
    }
    @Override
    public DalvInsn withRegisters(RegisterSpecList registers) {
        return new LocalSnapshot(getPosition(), locals);
    }
    public RegisterSpecSet getLocals() {
        return locals;
    }
    @Override
    protected String argString() {
        return locals.toString();
    }
    @Override
    protected String listingString0(boolean noteIndices) {
        int sz = locals.size();
        int max = locals.getMaxSize();
        StringBuffer sb = new StringBuffer(100 + sz * 40);
        sb.append("local-snapshot");
        for (int i = 0; i < max; i++) {
            RegisterSpec spec = locals.get(i);
            if (spec != null) {
                sb.append("\n  ");
                sb.append(LocalStart.localString(spec));
            }
        }
        return sb.toString();
    }
}
