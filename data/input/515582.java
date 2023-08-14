public final class HighRegisterPrefix extends VariableSizeInsn {
    private SimpleInsn[] insns;
    public HighRegisterPrefix(SourcePosition position,
                              RegisterSpecList registers) {
        super(position, registers);
        if (registers.size() == 0) {
            throw new IllegalArgumentException("registers.size() == 0");
        }
        insns = null;
    }
    @Override
    public int codeSize() {
        int result = 0;
        calculateInsnsIfNecessary();
        for (SimpleInsn insn : insns) {
            result += insn.codeSize();
        }
        return result;
    }
    @Override
    public void writeTo(AnnotatedOutput out) {
        calculateInsnsIfNecessary();
        for (SimpleInsn insn : insns) {
            insn.writeTo(out);
        }
    }
    private void calculateInsnsIfNecessary() {
        if (insns != null) {
            return;
        }
        RegisterSpecList registers = getRegisters();
        int sz = registers.size();
        insns = new SimpleInsn[sz];
        for (int i = 0, outAt = 0; i < sz; i++) {
            RegisterSpec src = registers.get(i);
            insns[i] = moveInsnFor(src, outAt);
            outAt += src.getCategory();
        }
    }
    @Override
    public DalvInsn withRegisters(RegisterSpecList registers) {
        return new HighRegisterPrefix(getPosition(), registers);
    }
    @Override
    protected String argString() {
        return null;
    }
    @Override
    protected String listingString0(boolean noteIndices) {
        RegisterSpecList registers = getRegisters();
        int sz = registers.size();
        StringBuffer sb = new StringBuffer(100);
        for (int i = 0, outAt = 0; i < sz; i++) {
            RegisterSpec src = registers.get(i);
            SimpleInsn insn = moveInsnFor(src, outAt);
            if (i != 0) {
                sb.append('\n');
            }
            sb.append(insn.listingString0(noteIndices));
            outAt += src.getCategory();
        }
        return sb.toString();
    }
    private static SimpleInsn moveInsnFor(RegisterSpec src, int destIndex) {
        return DalvInsn.makeMove(SourcePosition.NO_INFO,
                RegisterSpec.make(destIndex, src.getType()),
                src);
    }
}
