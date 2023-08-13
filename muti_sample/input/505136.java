public final class OutputCollector {
    private final OutputFinisher finisher;
    private ArrayList<DalvInsn> suffix;
    public OutputCollector(int initialCapacity, int suffixInitialCapacity,
            int regCount) {
        this.finisher = new OutputFinisher(initialCapacity, regCount);
        this.suffix = new ArrayList<DalvInsn>(suffixInitialCapacity);
    }
    public void add(DalvInsn insn) {
        finisher.add(insn);
    }
    public void reverseBranch(int which, CodeAddress newTarget) {
        finisher.reverseBranch(which, newTarget);
    }
    public void addSuffix(DalvInsn insn) {
        suffix.add(insn);
    }
    public OutputFinisher getFinisher() {
        if (suffix == null) {
            throw new UnsupportedOperationException("already processed");
        }
        appendSuffixToOutput();
        return finisher;
    }
    private void appendSuffixToOutput() {
        int size = suffix.size();
        for (int i = 0; i < size; i++) {
            finisher.add(suffix.get(i));
        }
        suffix = null;
    }
}
