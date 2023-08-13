public class SsaConverter {
    public static final boolean DEBUG = false;
    public static SsaMethod convertToSsaMethod(RopMethod rmeth,
            int paramWidth, boolean isStatic) {
        SsaMethod result
            = SsaMethod.newFromRopMethod(rmeth, paramWidth, isStatic);
        edgeSplit(result);
        LocalVariableInfo localInfo = LocalVariableExtractor.extract(result);
        placePhiFunctions(result, localInfo);
        new SsaRenamer(result).run();
        result.makeExitBlock();
        return result;
    }
    public static SsaMethod testEdgeSplit (RopMethod rmeth, int paramWidth,
            boolean isStatic) {
        SsaMethod result;
        result = SsaMethod.newFromRopMethod(rmeth, paramWidth, isStatic);
        edgeSplit(result);
        return result;
    }
    public static SsaMethod testPhiPlacement (RopMethod rmeth, int paramWidth,
            boolean isStatic) {
        SsaMethod result;
        result = SsaMethod.newFromRopMethod(rmeth, paramWidth, isStatic);
        edgeSplit(result);
        LocalVariableInfo localInfo = LocalVariableExtractor.extract(result);
        placePhiFunctions(result, localInfo);
        return result;
    }
    private static void edgeSplit(SsaMethod result) {
        edgeSplitPredecessors(result);
        edgeSplitMoveExceptionsAndResults(result);
        edgeSplitSuccessors(result);
    }
    private static void edgeSplitPredecessors(SsaMethod result) {
        ArrayList<SsaBasicBlock> blocks = result.getBlocks();
        for (int i = blocks.size() - 1; i >= 0; i-- ) {
            SsaBasicBlock block = blocks.get(i);
            if (nodeNeedsUniquePredecessor(block)) {
                block.insertNewPredecessor();
            }
        }
    }
    private static boolean nodeNeedsUniquePredecessor(SsaBasicBlock block) {
        int countPredecessors = block.getPredecessors().cardinality();
        int countSuccessors = block.getSuccessors().cardinality();
        return  (countPredecessors > 1 && countSuccessors > 1);
    }
    private static void edgeSplitMoveExceptionsAndResults(SsaMethod ssaMeth) {
        ArrayList<SsaBasicBlock> blocks = ssaMeth.getBlocks();
        for (int i = blocks.size() - 1; i >= 0; i-- ) {
            SsaBasicBlock block = blocks.get(i);
            if (!block.isExitBlock()
                    && block.getPredecessors().cardinality() > 1
                    && block.getInsns().get(0).isMoveException()) {
                BitSet preds = (BitSet)block.getPredecessors().clone();
                for (int j = preds.nextSetBit(0); j >= 0;
                     j = preds.nextSetBit(j + 1)) {
                    SsaBasicBlock predecessor = blocks.get(j);
                    SsaBasicBlock zNode
                        = predecessor.insertNewSuccessor(block);
                    zNode.getInsns().add(0, block.getInsns().get(0).clone());
                }
                block.getInsns().remove(0);
            }
        }
    }
    private static void edgeSplitSuccessors(SsaMethod result) {
        ArrayList<SsaBasicBlock> blocks = result.getBlocks();
        for (int i = blocks.size() - 1; i >= 0; i-- ) {
            SsaBasicBlock block = blocks.get(i);
            BitSet successors = (BitSet)block.getSuccessors().clone();
            for (int j = successors.nextSetBit(0);
                 j >= 0; j = successors.nextSetBit(j+1)) {
                SsaBasicBlock succ = blocks.get(j);
                if (needsNewSuccessor(block, succ)) {
                    block.insertNewSuccessor(succ);
                }
            }
        }
    }
    private static boolean needsNewSuccessor(SsaBasicBlock block,
            SsaBasicBlock succ) {
        ArrayList<SsaInsn> insns = block.getInsns();
        SsaInsn lastInsn = insns.get(insns.size() - 1);
        return ((lastInsn.getResult() != null)
                    || (lastInsn.getSources().size() > 0))
                && succ.getPredecessors().cardinality() > 1;
    }
    private static void placePhiFunctions (SsaMethod ssaMeth,
            LocalVariableInfo localInfo) {
        ArrayList<SsaBasicBlock> ssaBlocks;
        int regCount;
        int blockCount;
        ssaBlocks = ssaMeth.getBlocks();
        blockCount = ssaBlocks.size();
        regCount = ssaMeth.getRegCount();
        DomFront df = new DomFront(ssaMeth);
        DomFront.DomInfo[] domInfos = df.run();
        BitSet[] defsites = new BitSet[regCount];
        BitSet[] phisites = new BitSet[regCount];
        for (int i = 0; i < regCount; i++) {
            defsites[i] = new BitSet(blockCount);
            phisites[i] = new BitSet(blockCount);
        }
        for (int bi = 0, s = ssaBlocks.size(); bi < s; bi++) {
            SsaBasicBlock b = ssaBlocks.get(bi);
            for (SsaInsn insn : b.getInsns()) {
                RegisterSpec rs = insn.getResult();
                if (rs != null) {
                    defsites[rs.getReg()].set(bi);
                }
            }
        }
        if (DEBUG) {
            System.out.println("defsites");
            for (int i = 0; i < regCount; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append('v').append(i).append(": ");
                sb.append(defsites[i].toString());
                System.out.println(sb);
            }
        }
        BitSet worklist;
        for (int reg = 0, s = ssaMeth.getRegCount() ; reg < s ; reg++ ) {
            int workBlockIndex;
            worklist = (BitSet) (defsites[reg].clone());
            while (0 <= (workBlockIndex = worklist.nextSetBit(0))) {
                worklist.clear(workBlockIndex);
                IntIterator dfIterator
                    = domInfos[workBlockIndex].dominanceFrontiers.iterator();
                while (dfIterator.hasNext()) {
                    int dfBlockIndex = dfIterator.next();
                    if (!phisites[reg].get(dfBlockIndex)) {
                        phisites[reg].set(dfBlockIndex);
                        RegisterSpec rs
                                = localInfo.getStarts(dfBlockIndex).get(reg);
                        if (rs == null) {
                            ssaBlocks.get(dfBlockIndex).addPhiInsnForReg(reg);
                        } else {
                            ssaBlocks.get(dfBlockIndex).addPhiInsnForReg(rs);
                        }
                        if (!defsites[reg].get(dfBlockIndex)) {
                            worklist.set(dfBlockIndex);
                        }
                    }
                }
            }
        }
        if (DEBUG) {
            System.out.println("phisites");
            for (int i = 0; i < regCount; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append('v').append(i).append(": ");
                sb.append(phisites[i].toString());
                System.out.println(sb);
            }
        }
    }
}
