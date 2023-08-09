public final class BasicBlock implements LabeledItem {
    private final int label;
    private final InsnList insns;
    private final IntList successors;
    private final int primarySuccessor;
    public BasicBlock(int label, InsnList insns, IntList successors,
                      int primarySuccessor) {
        if (label < 0) {
            throw new IllegalArgumentException("label < 0");
        }
        try {
            insns.throwIfMutable();
        } catch (NullPointerException ex) {
            throw new NullPointerException("insns == null");
        }
        int sz = insns.size();
        if (sz == 0) {
            throw new IllegalArgumentException("insns.size() == 0");
        }
        for (int i = sz - 2; i >= 0; i--) {
            Rop one = insns.get(i).getOpcode();
            if (one.getBranchingness() != Rop.BRANCH_NONE) {
                throw new IllegalArgumentException("insns[" + i + "] is a " +
                                                   "branch or can throw");
            }
        }
        Insn lastInsn = insns.get(sz - 1);
        if (lastInsn.getOpcode().getBranchingness() == Rop.BRANCH_NONE) {
            throw new IllegalArgumentException("insns does not end with " +
                                               "a branch or throwing " +
                                               "instruction");
        }
        try {
            successors.throwIfMutable();
        } catch (NullPointerException ex) {
            throw new NullPointerException("successors == null");
        }
        if (primarySuccessor < -1) {
            throw new IllegalArgumentException("primarySuccessor < -1");
        }
        if (primarySuccessor >= 0 && !successors.contains(primarySuccessor)) {
            throw new IllegalArgumentException(
                    "primarySuccessor not in successors");
        }
        this.label = label;
        this.insns = insns;
        this.successors = successors;
        this.primarySuccessor = primarySuccessor;
    }
    @Override
    public boolean equals(Object other) {
        return (this == other);
    }
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
    public int getLabel() {
        return label;
    }
    public InsnList getInsns() {
        return insns;
    }
    public IntList getSuccessors() {
        return successors;
    }
    public int getPrimarySuccessor() {
        return primarySuccessor;
    }
    public int getSecondarySuccessor() {
        if (successors.size() != 2) {
            throw new UnsupportedOperationException(
                    "block doesn't have exactly two successors");
        }
        int succ = successors.get(0);
        if (succ == primarySuccessor) {
            succ = successors.get(1);
        }
        return succ;
    }
    public Insn getFirstInsn() {
        return insns.get(0);
    }
    public Insn getLastInsn() {
        return insns.getLast();
    }
    public boolean canThrow() {
        return insns.getLast().canThrow();
    }
    public boolean hasExceptionHandlers() {
        Insn lastInsn = insns.getLast();
        return lastInsn.getCatches().size() != 0;
    }
    public TypeList getExceptionHandlerTypes() {
        Insn lastInsn = insns.getLast();
        return lastInsn.getCatches();
    }
    public BasicBlock withRegisterOffset(int delta) {
        return new BasicBlock(label, insns.withRegisterOffset(delta),
                              successors, primarySuccessor);
    }
    public String toString() {
        return '{' + Hex.u2(label) + '}';
    }
    public interface Visitor {
        public void visitBlock (BasicBlock b);
    }
}
