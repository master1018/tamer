public class LocalVariableExtractor {
    private final SsaMethod method;
    private final ArrayList<SsaBasicBlock> blocks;
    private final LocalVariableInfo resultInfo;
    private final BitSet workSet;
    public static LocalVariableInfo extract(SsaMethod method) {
        LocalVariableExtractor lve = new LocalVariableExtractor(method);
        return lve.doit();
    }
    private LocalVariableExtractor(SsaMethod method) {
        if (method == null) {
            throw new NullPointerException("method == null");
        }
        ArrayList<SsaBasicBlock> blocks = method.getBlocks();
        this.method = method;
        this.blocks = blocks;
        this.resultInfo = new LocalVariableInfo(method);
        this.workSet = new BitSet(blocks.size());
    }
    private LocalVariableInfo doit() {
        if (method.getRegCount() > 0 ) {
            for (int bi = method.getEntryBlockIndex();
                 bi >= 0;
                 bi = workSet.nextSetBit(0)) {
                workSet.clear(bi);
                processBlock(bi);
            }
        }
        resultInfo.setImmutable();
        return resultInfo;
    }
    private void processBlock(int blockIndex) {
        RegisterSpecSet primaryState
                = resultInfo.mutableCopyOfStarts(blockIndex);
        SsaBasicBlock block = blocks.get(blockIndex);
        List<SsaInsn> insns = block.getInsns();
        int insnSz = insns.size();
        if (blockIndex == method.getExitBlockIndex()) {
            return;
        }
        SsaInsn lastInsn = insns.get(insnSz - 1);
        boolean hasExceptionHandlers
                = lastInsn.getOriginalRopInsn().getCatches().size() !=0 ;
        boolean canThrowDuringLastInsn = hasExceptionHandlers
                && (lastInsn.getResult() != null);
        int freezeSecondaryStateAt = insnSz - 1;
        RegisterSpecSet secondaryState = primaryState;
        for (int i = 0; i < insnSz; i++) {
            if (canThrowDuringLastInsn && (i == freezeSecondaryStateAt)) {
                primaryState.setImmutable();
                primaryState = primaryState.mutableCopy();
            }
            SsaInsn insn = insns.get(i);
            RegisterSpec result;
            result = insn.getLocalAssignment();
            if (result == null) {
                result = insn.getResult();
                if (result != null && primaryState.get(result.getReg()) != null) {
                    primaryState.remove(primaryState.get(result.getReg()));
                }
                continue;
            }
            result = result.withSimpleType();
            RegisterSpec already = primaryState.get(result);
            if (!result.equals(already)) {
                RegisterSpec previous
                        = primaryState.localItemToSpec(result.getLocalItem());
                if (previous != null
                        && (previous.getReg() != result.getReg())) {
                    primaryState.remove(previous);
                }
                resultInfo.addAssignment(insn, result);
                primaryState.put(result);
            }
        }
        primaryState.setImmutable();
        IntList successors = block.getSuccessorList();
        int succSz = successors.size();
        int primarySuccessor = block.getPrimarySuccessorIndex();
        for (int i = 0; i < succSz; i++) {
            int succ = successors.get(i);
            RegisterSpecSet state = (succ == primarySuccessor) ?
                primaryState : secondaryState;
            if (resultInfo.mergeStarts(succ, state)) {
                workSet.set(succ);
            }
        }
    }
}
