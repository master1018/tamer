public final class TargetInsn extends FixedSizeInsn {
    private CodeAddress target;
    public TargetInsn(Dop opcode, SourcePosition position,
                      RegisterSpecList registers, CodeAddress target) {
        super(opcode, position, registers);
        if (target == null) {
            throw new NullPointerException("target == null");
        }
        this.target = target;
    }
    @Override
    public DalvInsn withOpcode(Dop opcode) {
        return new TargetInsn(opcode, getPosition(), getRegisters(), target);
    }
    @Override
    public DalvInsn withRegisters(RegisterSpecList registers) {
        return new TargetInsn(getOpcode(), getPosition(), registers, target);
    }
    public TargetInsn withNewTargetAndReversed(CodeAddress target) {
        Dop opcode = getOpcode().getOppositeTest();
        return new TargetInsn(opcode, getPosition(), getRegisters(), target);
    }
    public CodeAddress getTarget() {
        return target;
    }
    public int getTargetAddress() {
        return target.getAddress();
    }
    public int getTargetOffset() {
        return target.getAddress() - getAddress();
    }
    public boolean hasTargetOffset() {
        return hasAddress() && target.hasAddress();
    }
    @Override
    protected String argString() {
        if (target == null) {
            return "????";
        }
        return target.identifierString();
    }
}
