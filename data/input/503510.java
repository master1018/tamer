public class DeadCodeRemover {
    private final SsaMethod ssaMeth;
    private final int regCount;
    private final BitSet worklist;
    private final ArrayList<SsaInsn>[] useList;
    public static void process(SsaMethod ssaMethod) {
        DeadCodeRemover dc = new DeadCodeRemover(ssaMethod);
        dc.run();
    }
    private DeadCodeRemover(SsaMethod ssaMethod) {
        this.ssaMeth = ssaMethod;
        regCount = ssaMethod.getRegCount();
        worklist = new BitSet(regCount);
        useList = ssaMeth.getUseListCopy();
    }
    private void run() {
        HashSet<SsaInsn> deletedInsns = (HashSet<SsaInsn>) new HashSet();
        ssaMeth.forEachInsn(new NoSideEffectVisitor(worklist));
        int regV;
        while ( 0 <= (regV = worklist.nextSetBit(0)) ) {
            worklist.clear(regV);
            if (useList[regV].size() == 0
                    || isCircularNoSideEffect(regV, null)) {
                SsaInsn insnS = ssaMeth.getDefinitionForRegister(regV);
                if (deletedInsns.contains(insnS)) {
                    continue;
                }
                RegisterSpecList sources = insnS.getSources();
                int sz = sources.size();
                for (int i = 0; i < sz; i++) {
                    RegisterSpec source = sources.get(i);
                    useList[source.getReg()].remove(insnS);
                    if (!hasSideEffect(
                            ssaMeth.getDefinitionForRegister(
                                    source.getReg()))) {
                        worklist.set(source.getReg());
                    }
                }
                deletedInsns.add(insnS);
            }
        }
        ssaMeth.deleteInsns(deletedInsns);
    }
    private boolean isCircularNoSideEffect(int regV, BitSet set) {
        if ((set != null) && set.get(regV)) {
            return true;
        }
        for (SsaInsn use : useList[regV]) {
            if (hasSideEffect(use)) {
                return false;
            }
        }
        if (set == null) {
            set = new BitSet(regCount);
        }
        set.set(regV);
        for (SsaInsn use : useList[regV]) {
            RegisterSpec result = use.getResult();
            if (result == null
                    || !isCircularNoSideEffect(result.getReg(), set)) {
                return false;
            }
        }
        return true;
    }
    private static boolean hasSideEffect(SsaInsn insn) {
        if (insn == null) {
            return true;
        }
        return insn.hasSideEffect();        
    }
    static private class NoSideEffectVisitor implements SsaInsn.Visitor {
        BitSet noSideEffectRegs;
        public NoSideEffectVisitor(BitSet noSideEffectRegs) {
            this.noSideEffectRegs = noSideEffectRegs;
        }
        public void visitMoveInsn (NormalSsaInsn insn) {
            if (!hasSideEffect(insn)) {
                noSideEffectRegs.set(insn.getResult().getReg());
            }
        }
        public void visitPhiInsn (PhiInsn phi) {
            if (!hasSideEffect(phi)) {
                noSideEffectRegs.set(phi.getResult().getReg());
            }
        }
        public void visitNonMoveInsn (NormalSsaInsn insn) {
            RegisterSpec result = insn.getResult();
            if (!hasSideEffect(insn) && result != null) {
                noSideEffectRegs.set(result.getReg());
            }
        }
    }
}
