public final class LocalStart extends ZeroSizeInsn {
    private final RegisterSpec local;
    public static String localString(RegisterSpec spec) {
        return spec.regString() + ' ' + spec.getLocalItem().toString() + ": " +
            spec.getTypeBearer().toHuman();
    }
    public LocalStart(SourcePosition position, RegisterSpec local) {
        super(position);
        if (local == null) {
            throw new NullPointerException("local == null");
        }
        this.local = local;
    }
    @Override
    public DalvInsn withRegisterOffset(int delta) {
        return new LocalStart(getPosition(), local.withOffset(delta));
    }
    @Override
    public DalvInsn withRegisters(RegisterSpecList registers) {
        return new LocalStart(getPosition(), local);
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
        return "local-start " + localString(local);
    }
}
