public final class ThrowingInsn
        extends Insn {
    private final TypeList catches;
    public static String toCatchString(TypeList catches) {
        StringBuffer sb = new StringBuffer(100);
        sb.append("catch");
        int sz = catches.size();
        for (int i = 0; i < sz; i++) {
            sb.append(" ");
            sb.append(catches.getType(i).toHuman());
        }
        return sb.toString();
    }
    public ThrowingInsn(Rop opcode, SourcePosition position,
                        RegisterSpecList sources,
                        TypeList catches) {
        super(opcode, position, null, sources);
        if (opcode.getBranchingness() != Rop.BRANCH_THROW) {
            throw new IllegalArgumentException("bogus branchingness");
        }
        if (catches == null) {
            throw new NullPointerException("catches == null");
        }
        this.catches = catches;
    }
    @Override
    public String getInlineString() {
        return toCatchString(catches);
    }
    @Override
    public TypeList getCatches() {
        return catches;
    }
    @Override
    public void accept(Visitor visitor) {
        visitor.visitThrowingInsn(this);
    }
    @Override
    public Insn withAddedCatch(Type type) {
        return new ThrowingInsn(getOpcode(), getPosition(),
                                getSources(), catches.withAddedType(type));
    }
    @Override
    public Insn withRegisterOffset(int delta) {
        return new ThrowingInsn(getOpcode(), getPosition(),
                                getSources().withOffset(delta),
                                catches);
    }
    @Override
    public Insn withNewRegisters(RegisterSpec result,
            RegisterSpecList sources) {
        return new ThrowingInsn(getOpcode(), getPosition(),
                                sources,
                                catches);
    }
}
