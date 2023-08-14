public final class BasicBlocker implements BytecodeArray.Visitor {
    private final ConcreteMethod method;
    private final int[] workSet;
    private final int[] liveSet;
    private final int[] blockSet;
    private final IntList[] targetLists;
    private final ByteCatchList[] catchLists;
    private int previousOffset;
    public static ByteBlockList identifyBlocks(ConcreteMethod method) {
        BasicBlocker bb = new BasicBlocker(method);
        bb.doit();
        return bb.getBlockList();
    }
    private BasicBlocker(ConcreteMethod method) {
        if (method == null) {
            throw new NullPointerException("method == null");
        }
        this.method = method;
        int sz = method.getCode().size() + 1;
        workSet = Bits.makeBitSet(sz);
        liveSet = Bits.makeBitSet(sz);
        blockSet = Bits.makeBitSet(sz);
        targetLists = new IntList[sz];
        catchLists = new ByteCatchList[sz];
        previousOffset = -1;
    }
    public void visitInvalid(int opcode, int offset, int length) {
        visitCommon(offset, length, true);
    }
    public void visitNoArgs(int opcode, int offset, int length, Type type) {
        switch (opcode) {
            case ByteOps.IRETURN:
            case ByteOps.RETURN: {
                visitCommon(offset, length, false);
                targetLists[offset] = IntList.EMPTY;
                break;
            }
            case ByteOps.ATHROW: {
                visitCommon(offset, length, false);
                visitThrowing(offset, length, false);
                break;
            }
            case ByteOps.IALOAD:
            case ByteOps.LALOAD:
            case ByteOps.FALOAD:
            case ByteOps.DALOAD:
            case ByteOps.AALOAD:
            case ByteOps.BALOAD:
            case ByteOps.CALOAD:
            case ByteOps.SALOAD:
            case ByteOps.IASTORE:
            case ByteOps.LASTORE:
            case ByteOps.FASTORE:
            case ByteOps.DASTORE:
            case ByteOps.AASTORE:
            case ByteOps.BASTORE:
            case ByteOps.CASTORE:
            case ByteOps.SASTORE:
            case ByteOps.ARRAYLENGTH:
            case ByteOps.MONITORENTER:
            case ByteOps.MONITOREXIT: {
                visitCommon(offset, length, true);
                visitThrowing(offset, length, true);
                break;
            }
            case ByteOps.IDIV:
            case ByteOps.IREM: {
                visitCommon(offset, length, true);
                if ((type == Type.INT) || (type == Type.LONG)) {
                    visitThrowing(offset, length, true);
                }
                break;                
            }
            default: {
                visitCommon(offset, length, true);
                break;
            }
        }
    }
    public void visitLocal(int opcode, int offset, int length,
            int idx, Type type, int value) {
        if (opcode == ByteOps.RET) {
            visitCommon(offset, length, false);
            targetLists[offset] = IntList.EMPTY;
        } else {
            visitCommon(offset, length, true);
        }
    }
    public void visitConstant(int opcode, int offset, int length,
            Constant cst, int value) {
        visitCommon(offset, length, true);
        if ((cst instanceof CstMemberRef) || (cst instanceof CstType) ||
            (cst instanceof CstString)) {
            visitThrowing(offset, length, true);
        }
    }
    public void visitBranch(int opcode, int offset, int length,
            int target) {
        switch (opcode) {
            case ByteOps.GOTO: {
                visitCommon(offset, length, false);
                targetLists[offset] = IntList.makeImmutable(target);
                break;
            }
            case ByteOps.JSR: {
                addWorkIfNecessary(offset, true);
            }
            default: {
                int next = offset + length;
                visitCommon(offset, length, true);
                addWorkIfNecessary(next, true);
                targetLists[offset] = IntList.makeImmutable(next, target);
                break;
            }
        }
        addWorkIfNecessary(target, true);
    }
    public void visitSwitch(int opcode, int offset, int length,
            SwitchList cases, int padding) {
        visitCommon(offset, length, false);
        addWorkIfNecessary(cases.getDefaultTarget(), true);
        int sz = cases.size();
        for (int i = 0; i < sz; i++) {
            addWorkIfNecessary(cases.getTarget(i), true);
        }
        targetLists[offset] = cases.getTargets();
    }
    public void visitNewarray(int offset, int length, CstType type,
            ArrayList<Constant> intVals) {
        visitCommon(offset, length, true);
        visitThrowing(offset, length, true);
    }
    private ByteBlockList getBlockList() {
        BytecodeArray bytes = method.getCode();
        ByteBlock[] bbs = new ByteBlock[bytes.size()];
        int count = 0;
        for (int at = 0, next; ; at = next) {
            next = Bits.findFirst(blockSet, at + 1);
            if (next < 0) {
                break;
            }
            if (Bits.get(liveSet, at)) {
                IntList targets = null;
                int targetsAt = -1;
                ByteCatchList blockCatches;
                for (int i = next - 1; i >= at; i--) {
                    targets = targetLists[i];
                    if (targets != null) {
                        targetsAt = i;
                        break;
                    }
                }
                if (targets == null) {
                    targets = IntList.makeImmutable(next);
                    blockCatches = ByteCatchList.EMPTY;
                } else {
                    blockCatches = catchLists[targetsAt];
                    if (blockCatches == null) {
                        blockCatches = ByteCatchList.EMPTY;
                    }
                }
                bbs[count] =
                    new ByteBlock(at, at, next, targets, blockCatches);
                count++;
            }
        }
        ByteBlockList result = new ByteBlockList(count);
        for (int i = 0; i < count; i++) {
            result.set(i, bbs[i]);
        }
        return result;
    }
    private void doit() {
        BytecodeArray bytes = method.getCode();
        ByteCatchList catches = method.getCatches();
        int catchSz = catches.size();
        Bits.set(workSet, 0);
        Bits.set(blockSet, 0);
        while (!Bits.isEmpty(workSet)) {
            try {
                bytes.processWorkSet(workSet, this);
            } catch (IllegalArgumentException ex) {
                throw new SimException("flow of control falls off " +
                                       "end of method",
                                       ex);
            }
            for (int i = 0; i < catchSz; i++) {
                ByteCatchList.Item item = catches.get(i);
                int start = item.getStartPc();
                int end = item.getEndPc();
                if (Bits.anyInRange(liveSet, start, end)) {
                    Bits.set(blockSet, start);
                    Bits.set(blockSet, end);
                    addWorkIfNecessary(item.getHandlerPc(), true);
                }
            }
        }
    }
    private void addWorkIfNecessary(int offset, boolean blockStart) {
        if (!Bits.get(liveSet, offset)) {
            Bits.set(workSet, offset);
        }
        if (blockStart) {
            Bits.set(blockSet, offset);
        }
    }
    private void visitCommon(int offset, int length, boolean nextIsLive) {
        Bits.set(liveSet, offset);
        if (nextIsLive) {
            addWorkIfNecessary(offset + length, false);
        } else {
            Bits.set(blockSet, offset + length);
        }
    }
    private void visitThrowing(int offset, int length, boolean nextIsLive) {
        int next = offset + length;
        if (nextIsLive) {
            addWorkIfNecessary(next, true);
        }
        ByteCatchList catches = method.getCatches().listFor(offset);
        catchLists[offset] = catches;
        targetLists[offset] = catches.toTargetList(nextIsLive ? next : -1);
    }
    public void setPreviousOffset(int offset) {
        previousOffset = offset;
    }
    public int getPreviousOffset() {
        return previousOffset;
    }
}
