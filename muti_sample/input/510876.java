public final class NormalSsaInsn extends SsaInsn implements Cloneable {
    private Insn insn;
    NormalSsaInsn(final Insn insn, final SsaBasicBlock block) {
        super(insn.getResult(), block);
        this.insn = insn;
    }
    @Override
    public final void mapSourceRegisters(RegisterMapper mapper) {
        RegisterSpecList oldSources = insn.getSources();
        RegisterSpecList newSources = mapper.map(oldSources);
        if (newSources != oldSources) {
            insn = insn.withNewRegisters(getResult(), newSources);
            getBlock().getParent().onSourcesChanged(this, oldSources);
        }
    }
    public final void changeOneSource(int index, RegisterSpec newSpec) {
        RegisterSpecList origSources = insn.getSources();
        int sz = origSources.size();
        RegisterSpecList newSources = new RegisterSpecList(sz);
        for (int i = 0; i < sz; i++) {
            newSources.set(i, i == index ? newSpec : origSources.get(i));
        }
        newSources.setImmutable();
        RegisterSpec origSpec = origSources.get(index);
        if (origSpec.getReg() != newSpec.getReg()) {
            getBlock().getParent().onSourceChanged(this, origSpec, newSpec);
        }
        insn = insn.withNewRegisters(getResult(), newSources);
    }
    public final void setNewSources (RegisterSpecList newSources) {
        RegisterSpecList origSources = insn.getSources();
        if (origSources.size() != newSources.size()) {
            throw new RuntimeException("Sources counts don't match");
        }
        insn = insn.withNewRegisters(getResult(), newSources);
    }
    @Override
    public NormalSsaInsn clone() {
        return (NormalSsaInsn) super.clone();
    }
    public RegisterSpecList getSources() {
        return insn.getSources();
    }
    public String toHuman() {
        return toRopInsn().toHuman();
    }
    @Override
    public Insn toRopInsn() {
        return insn.withNewRegisters(getResult(), insn.getSources());
    }
    @Override
    public Rop getOpcode() {
        return insn.getOpcode();
    }
    @Override
    public Insn getOriginalRopInsn() {
        return insn;
    }
    public RegisterSpec getLocalAssignment() {
        RegisterSpec assignment;
        if (insn.getOpcode().getOpcode() == RegOps.MARK_LOCAL) {
            assignment = insn.getSources().get(0);
        } else {
            assignment = getResult();
        }
        if (assignment == null) {
            return null;
        }
        LocalItem local = assignment.getLocalItem();
        if (local == null) {
            return null;
        }
        return assignment;
    }
    public void upgradeToLiteral() {
        RegisterSpecList oldSources = insn.getSources();
        insn = insn.withLastSourceLiteral();
        getBlock().getParent().onSourcesChanged(this, oldSources);
    }
    @Override
    public boolean isNormalMoveInsn() {
        return insn.getOpcode().getOpcode() == RegOps.MOVE;
    }
    @Override
    public boolean isMoveException() {
        return insn.getOpcode().getOpcode() == RegOps.MOVE_EXCEPTION;
    }
    @Override
    public boolean canThrow() {
        return insn.canThrow();
    }
    @Override
    public void accept(Visitor v) {
        if (isNormalMoveInsn()) {
            v.visitMoveInsn(this);
        } else {
            v.visitNonMoveInsn(this);
        }
    }
    @Override
    public  boolean isPhiOrMove() {
        return isNormalMoveInsn();
    }
    @Override
    public boolean hasSideEffect() {
        Rop opcode = getOpcode();
        if (opcode.getBranchingness() != Rop.BRANCH_NONE) {
            return true;
        }
        boolean hasLocalSideEffect
            = Optimizer.getPreserveLocals() && getLocalAssignment() != null;
        switch (opcode.getOpcode()) {
            case RegOps.MOVE_RESULT:
            case RegOps.MOVE:
            case RegOps.CONST:
                return hasLocalSideEffect;
            default:
                return true;
        }
    }
}
