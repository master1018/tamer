public final class LocalEnd extends ZeroSizeInsn {
    private final RegisterSpec local;
    public LocalEnd(SourcePosition position, RegisterSpec local) {
        super(position);
        if (local == null) {
            throw new NullPointerException("local == null");
        }
        this.local = local;
    }
    @Override
    public DalvInsn withRegisterOffset(int delta) {
        return new LocalEnd(getPosition(), local.withOffset(delta));
    }
    @Override
    public DalvInsn withRegisters(RegisterSpecList registers) {
        return new LocalEnd(getPosition(), local);
    }
    public RegisterSpec getLocal() {
        return local;
    }
    @Override
    protected String argString() {
        return local.toString();
    }
    @Override
    protected String listingString0(boolean noteIndices) {
        return "local-end " + LocalStart.localString(local);
    }
}
