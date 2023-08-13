public final class SpecialFormat extends InsnFormat {
    public static final InsnFormat THE_ONE = new SpecialFormat();
    private SpecialFormat() {
    }
    @Override
    public String insnArgString(DalvInsn insn) {
        throw new RuntimeException("unsupported");
    }
    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        throw new RuntimeException("unsupported");
    }
    @Override
    public int codeSize() {
        throw new RuntimeException("unsupported");
    }
    @Override
    public boolean isCompatible(DalvInsn insn) {
        return true;
    }
    @Override
    public InsnFormat nextUp() {
        throw new RuntimeException("unsupported");
    }
    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        throw new RuntimeException("unsupported");
    }
}
