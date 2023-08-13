public abstract class SsaInsn implements ToHuman, Cloneable {
    private final SsaBasicBlock block;
    private RegisterSpec result;
    protected SsaInsn(RegisterSpec result, SsaBasicBlock block) {
        if (block == null) {
            throw new NullPointerException("block == null");
        }
        this.block = block;
        this.result = result;
    }
    public static SsaInsn makeFromRop(Insn insn, SsaBasicBlock block) {
        return new NormalSsaInsn(insn, block);
    }
    @Override
    public SsaInsn clone() {
        try {
            return (SsaInsn)super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException ("unexpected", ex);
        }
    }
    public RegisterSpec getResult() {
        return result;
    }
    protected void setResult(RegisterSpec result) {
        if (result == null) {
            throw new NullPointerException("result == null");
        }
        this.result = result;
    }
    abstract public RegisterSpecList getSources();
    public SsaBasicBlock getBlock() {
        return block;
    }
    public boolean isResultReg(int reg) {
        return result != null && result.getReg() == reg;
    }
    public void changeResultReg(int reg) {
        if (result != null) {
            result = result.withReg(reg);
        }
    }
    public final void setResultLocal(LocalItem local) {
        LocalItem oldItem = result.getLocalItem();
        if (local != oldItem && (local == null
                || !local.equals(result.getLocalItem()))) {
            result = RegisterSpec.makeLocalOptional(
                    result.getReg(), result.getType(), local);
        }
    }
    public final void mapRegisters(RegisterMapper mapper) {
        RegisterSpec oldResult = result;
        result = mapper.map(result);
        block.getParent().updateOneDefinition(this, oldResult);
        mapSourceRegisters(mapper);        
    }
    abstract public void mapSourceRegisters(RegisterMapper mapper);
    abstract public Rop getOpcode();
    abstract public Insn getOriginalRopInsn();
    public RegisterSpec getLocalAssignment() {
        if (result != null && result.getLocalItem() != null) {
            return result;
        }
        return null;
    }
    public boolean isRegASource(int reg) {
        return null != getSources().specForRegister(reg);
    }
    public abstract Insn toRopInsn();
    public abstract boolean isPhiOrMove();
    public abstract boolean hasSideEffect();
    public boolean isNormalMoveInsn() {
        return false;
    }
    public boolean isMoveException() {
        return false;
    }
    abstract public boolean canThrow();
    public abstract void accept(Visitor v);
    public static interface Visitor {
        public void visitMoveInsn(NormalSsaInsn insn);
        public void visitPhiInsn(PhiInsn insn);
        public void visitNonMoveInsn(NormalSsaInsn insn);
    }
}
