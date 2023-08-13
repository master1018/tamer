public final class SimpleInsn extends FixedSizeInsn {
    public SimpleInsn(Dop opcode, SourcePosition position,
                      RegisterSpecList registers) {
        super(opcode, position, registers);
    }
    @Override
    public DalvInsn withOpcode(Dop opcode) {
        return new SimpleInsn(opcode, getPosition(), getRegisters());
    }
    @Override
    public DalvInsn withRegisters(RegisterSpecList registers) {
        return new SimpleInsn(getOpcode(), getPosition(), registers);
    }
    @Override
    protected String argString() {
        return null;
    }
}
