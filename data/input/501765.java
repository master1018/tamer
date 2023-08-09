public final class BlockAddresses {
    private final CodeAddress[] starts;
    private final CodeAddress[] lasts;
    private final CodeAddress[] ends;
    public BlockAddresses(RopMethod method) {
        BasicBlockList blocks = method.getBlocks();
        int maxLabel = blocks.getMaxLabel();
        this.starts = new CodeAddress[maxLabel];
        this.lasts = new CodeAddress[maxLabel];
        this.ends = new CodeAddress[maxLabel];
        setupArrays(method);
    }
    public CodeAddress getStart(BasicBlock block) {
        return starts[block.getLabel()];
    }
    public CodeAddress getStart(int label) {
        return starts[label];
    }
    public CodeAddress getLast(BasicBlock block) {
        return lasts[block.getLabel()];
    }
    public CodeAddress getLast(int label) {
        return lasts[label];
    }
    public CodeAddress getEnd(BasicBlock block) {
        return ends[block.getLabel()];
    }
    public CodeAddress getEnd(int label) {
        return ends[label];
    }
    private void setupArrays(RopMethod method) {
        BasicBlockList blocks = method.getBlocks();
        int sz = blocks.size();
        for (int i = 0; i < sz; i++) {
            BasicBlock one = blocks.get(i);
            int label = one.getLabel();
            Insn insn = one.getInsns().get(0);
            starts[label] = new CodeAddress(insn.getPosition());
            SourcePosition pos = one.getLastInsn().getPosition();
            lasts[label] = new CodeAddress(pos);
            ends[label] = new CodeAddress(pos);
        }
    }
}
