public final class PlainInsn
        extends Insn {
    public PlainInsn(Rop opcode, SourcePosition position,
                     RegisterSpec result, RegisterSpecList sources) {
        super(opcode, position, result, sources);
        switch (opcode.getBranchingness()) {
            case Rop.BRANCH_SWITCH:
            case Rop.BRANCH_THROW: {
                throw new IllegalArgumentException("bogus branchingness");
            }
        }
        if (result != null && opcode.getBranchingness() != Rop.BRANCH_NONE) {
            throw new IllegalArgumentException
                    ("can't mix branchingness with result");            
        }
    }
    public PlainInsn(Rop opcode, SourcePosition position, RegisterSpec result,
                     RegisterSpec source) {
        this(opcode, position, result, RegisterSpecList.make(source));
    }
    @Override
    public TypeList getCatches() {
        return StdTypeList.EMPTY;
    }
    @Override
    public void accept(Visitor visitor) {
        visitor.visitPlainInsn(this);
    }
    @Override
    public Insn withAddedCatch(Type type) {
        throw new UnsupportedOperationException("unsupported");
    }
    @Override
    public Insn withRegisterOffset(int delta) {
        return new PlainInsn(getOpcode(), getPosition(),
                             getResult().withOffset(delta),
                             getSources().withOffset(delta));
    }
    @Override
    public Insn withLastSourceLiteral() {
        RegisterSpecList sources = getSources();
        int szSources = sources.size();
        if (szSources == 0) {
            return this;
        }
        TypeBearer lastType = sources.get(szSources - 1).getTypeBearer();
        if (!lastType.isConstant()) {
            return this;
        }
        Constant cst = (Constant) lastType;
        RegisterSpecList newSources = sources.withoutLast();
        Rop newRop;
        try {
            newRop = Rops.ropFor(getOpcode().getOpcode(),
                    getResult(), newSources, (Constant)lastType);
        } catch (IllegalArgumentException ex) {
            return this;
        }
        return new PlainCstInsn(newRop, getPosition(),
                getResult(), newSources, cst);
    }
    @Override
    public Insn withNewRegisters(RegisterSpec result,
            RegisterSpecList sources) {
        return new PlainInsn(getOpcode(), getPosition(),
                             result,
                             sources);
    }
}
