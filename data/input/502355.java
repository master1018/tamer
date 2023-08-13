public final class FillArrayDataInsn
        extends Insn {
    private final ArrayList<Constant> initValues;
    private final Constant arrayType;
    public FillArrayDataInsn(Rop opcode, SourcePosition position,
                             RegisterSpecList sources,
                             ArrayList<Constant> initValues,
                             Constant cst) {
        super(opcode, position, null, sources);
        if (opcode.getBranchingness() != Rop.BRANCH_NONE) {
            throw new IllegalArgumentException("bogus branchingness");
        }
        this.initValues = initValues;
        this.arrayType = cst;
    }
    @Override
    public TypeList getCatches() {
        return StdTypeList.EMPTY;
    }
    public ArrayList<Constant> getInitValues() {
        return initValues;
    }
    public Constant getConstant() {
        return arrayType;
    }
    @Override
    public void accept(Visitor visitor) {
        visitor.visitFillArrayDataInsn(this);
    }
    @Override
    public Insn withAddedCatch(Type type) {
        throw new  UnsupportedOperationException("unsupported");
    }
    @Override
    public Insn withRegisterOffset(int delta) {
        return new FillArrayDataInsn(getOpcode(), getPosition(),
                                     getSources().withOffset(delta),
                                     initValues, arrayType);
    }
    @Override
    public Insn withNewRegisters(RegisterSpec result,
            RegisterSpecList sources) {
        return new FillArrayDataInsn(getOpcode(), getPosition(),
                                     sources, initValues, arrayType);
    }
}
