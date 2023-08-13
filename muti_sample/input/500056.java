public final class Form10t extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form10t();
    private Form10t() {
    }
    @Override
    public String insnArgString(DalvInsn insn) {
        return branchString(insn);
    }
    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        return branchComment(insn);
    }
    @Override
    public int codeSize() {
        return 1;
    }
    @Override
    public boolean isCompatible(DalvInsn insn) {
        if (!((insn instanceof TargetInsn) &&
              (insn.getRegisters().size() == 0))) {
            return false;
        }
        TargetInsn ti = (TargetInsn) insn;
        return ti.hasTargetOffset() ? branchFits(ti) : true;
    }
    @Override
    public boolean branchFits(TargetInsn insn) {
        int offset = insn.getTargetOffset();
        return (offset != 0) && signedFitsInByte(offset);
    }
    @Override
    public InsnFormat nextUp() {
        return Form20t.THE_ONE;
    }
    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        int offset = ((TargetInsn) insn).getTargetOffset();
        write(out, opcodeUnit(insn, (offset & 0xff)));
    }
}
