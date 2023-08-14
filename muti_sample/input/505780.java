public final class Ropper {
    private static final int PARAM_ASSIGNMENT = -1;
    private static final int RETURN = -2;
    private static final int SYNCH_RETURN = -3;
    private static final int SYNCH_SETUP_1 = -4;
    private static final int SYNCH_SETUP_2 = -5;
    private static final int SYNCH_CATCH_1 = -6;
    private static final int SYNCH_CATCH_2 = -7;
    private static final int SPECIAL_LABEL_COUNT = 7;
    private final ConcreteMethod method;
    private final ByteBlockList blocks;
    private final int maxLocals;
    private final int maxLabel;
    private final RopperMachine machine;
    private final Simulator sim;
    private final Frame[] startFrames;
    private final ArrayList<BasicBlock> result;
    private final ArrayList<IntList> resultSubroutines;
    private final Type[] catchTypes;
    private boolean synchNeedsExceptionHandler;
    private final Subroutine[] subroutines;
    private boolean hasSubroutines;
    private class Subroutine {
        private BitSet callerBlocks;
        private BitSet retBlocks;
        private int startBlock;
        Subroutine(int startBlock) {
            this.startBlock = startBlock;
            retBlocks = new BitSet(maxLabel);
            callerBlocks = new BitSet(maxLabel);
            hasSubroutines = true;
        }
        Subroutine(int startBlock, int retBlock) {
            this(startBlock);
            addRetBlock(retBlock);
        }
        int getStartBlock() {
            return startBlock;
        }
        void addRetBlock(int retBlock) {
            retBlocks.set(retBlock);
        }
        void addCallerBlock(int label) {
            callerBlocks.set(label);
        }
        IntList getSuccessors() {
            IntList successors = new IntList(callerBlocks.size());
            for (int label = callerBlocks.nextSetBit(0); label >= 0;
                 label = callerBlocks.nextSetBit(label+1)) {
                BasicBlock subCaller = labelToBlock(label);
                successors.add(subCaller.getSuccessors().get(0));
            }
            successors.setImmutable();
            return successors;
        }
        void mergeToSuccessors(Frame frame, int[] workSet) {
            for (int label = callerBlocks.nextSetBit(0); label >= 0;
                 label = callerBlocks.nextSetBit(label+1)) {
                BasicBlock subCaller = labelToBlock(label);
                int succLabel = subCaller.getSuccessors().get(0);
                Frame subFrame = frame.subFrameForLabel(startBlock, label);
                if (subFrame != null) {
                    mergeAndWorkAsNecessary(succLabel, -1, null,
                            subFrame, workSet);
                } else {
                    Bits.set(workSet, label);
                }
            }
        }
    }
    public static RopMethod convert(ConcreteMethod method,
            TranslationAdvice advice) {
        try {
            Ropper r = new Ropper(method, advice);
            r.doit();
            return r.getRopMethod();
        } catch (SimException ex) {
            ex.addContext("...while working on method " +
                          method.getNat().toHuman());
            throw ex;
        }
    }
    private Ropper(ConcreteMethod method, TranslationAdvice advice) {
        if (method == null) {
            throw new NullPointerException("method == null");
        }
        if (advice == null) {
            throw new NullPointerException("advice == null");
        }
        this.method = method;
        this.blocks = BasicBlocker.identifyBlocks(method);
        this.maxLabel = blocks.getMaxLabel();
        this.maxLocals = method.getMaxLocals();
        this.machine = new RopperMachine(this, method, advice);
        this.sim = new Simulator(machine, method);
        this.startFrames = new Frame[maxLabel];
        this.subroutines = new Subroutine[maxLabel];
        this.result = new ArrayList<BasicBlock>(blocks.size() * 2 + 10);
        this.resultSubroutines =
            new ArrayList<IntList>(blocks.size() * 2 + 10);
        this.catchTypes = new Type[maxLabel];
        this.synchNeedsExceptionHandler = false;
        startFrames[0] = new Frame(maxLocals, method.getMaxStack());
    }
     int getFirstTempStackReg() {
        int regCount = getNormalRegCount();
        return isSynchronized() ? regCount + 1 : regCount;
    }
    private int getExceptionSetupLabel(int label) {
        return maxLabel + label;
    }
    private int getSpecialLabel(int label) {
        return (maxLabel * 2) + ~label;
    }
    private int getMinimumUnreservedLabel() {
        return (maxLabel * 2) + SPECIAL_LABEL_COUNT;
    }
    private int getAvailableLabel() {
        int candidate = getMinimumUnreservedLabel();
        for (BasicBlock bb : result) {
            int label = bb.getLabel();
            if (label >= candidate) {
                candidate = label + 1;
            }
        }
        return candidate;
    }
    private boolean isSynchronized() {
        int accessFlags = method.getAccessFlags();
        return (accessFlags & AccessFlags.ACC_SYNCHRONIZED) != 0;
    }
    private boolean isStatic() {
        int accessFlags = method.getAccessFlags();
        return (accessFlags & AccessFlags.ACC_STATIC) != 0;
    }
    private int getNormalRegCount() {
        return maxLocals + method.getMaxStack();
    }
    private RegisterSpec getSynchReg() {
        int reg = getNormalRegCount();
        return RegisterSpec.make((reg < 1) ? 1 : reg, Type.OBJECT);
    }
    private int labelToResultIndex(int label) {
        int sz = result.size();
        for (int i = 0; i < sz; i++) {
            BasicBlock one = result.get(i);
            if (one.getLabel() == label) {
                return i;
            }
        }
        return -1;
    }
    private BasicBlock labelToBlock(int label) {
        int idx = labelToResultIndex(label);
        if (idx < 0) {
            throw new IllegalArgumentException("no such label " +
                    Hex.u2(label));
        }
        return result.get(idx);
    }
    private void addBlock(BasicBlock block, IntList subroutines) {
        if (block == null) {
            throw new NullPointerException("block == null");
        }
        result.add(block);
        subroutines.throwIfMutable();
        resultSubroutines.add(subroutines);
    }
    private boolean addOrReplaceBlock(BasicBlock block, IntList subroutines) {
        if (block == null) {
            throw new NullPointerException("block == null");
        }
        int idx = labelToResultIndex(block.getLabel());
        boolean ret;
        if (idx < 0) {
            ret = false;
        } else {
            removeBlockAndSpecialSuccessors(idx);
            ret = true;
        }
        result.add(block);
        subroutines.throwIfMutable();
        resultSubroutines.add(subroutines);
        return ret;
    }
    private boolean addOrReplaceBlockNoDelete(BasicBlock block,
            IntList subroutines) {
        if (block == null) {
            throw new NullPointerException("block == null");
        }
        int idx = labelToResultIndex(block.getLabel());
        boolean ret;
        if (idx < 0) {
            ret = false;
        } else {
            result.remove(idx);
            resultSubroutines.remove(idx);
            ret = true;
        }
        result.add(block);
        subroutines.throwIfMutable();
        resultSubroutines.add(subroutines);
        return ret;
    }
    private void removeBlockAndSpecialSuccessors(int idx) {
        int minLabel = getMinimumUnreservedLabel();
        BasicBlock block = result.get(idx);
        IntList successors = block.getSuccessors();
        int sz = successors.size();
        result.remove(idx);
        resultSubroutines.remove(idx);
        for (int i = 0; i < sz; i++) {
            int label = successors.get(i);
            if (label >= minLabel) {
                idx = labelToResultIndex(label);
                if (idx < 0) {
                    throw new RuntimeException("Invalid label "
                            + Hex.u2(label));
                }
                removeBlockAndSpecialSuccessors(idx);
            }
        }
    }
    private RopMethod getRopMethod() {
        int sz = result.size();
        BasicBlockList bbl = new BasicBlockList(sz);
        for (int i = 0; i < sz; i++) {
            bbl.set(i, result.get(i));
        }
        bbl.setImmutable();
        return new RopMethod(bbl, getSpecialLabel(PARAM_ASSIGNMENT));
    }
    private void doit() {
        int[] workSet = Bits.makeBitSet(maxLabel);
        Bits.set(workSet, 0);
        addSetupBlocks();
        setFirstFrame();
        for (;;) {
            int offset = Bits.findFirst(workSet, 0);
            if (offset < 0) {
                break;
            }
            Bits.clear(workSet, offset);
            ByteBlock block = blocks.labelToBlock(offset);
            Frame frame = startFrames[offset];
            try {
                processBlock(block, frame, workSet);
            } catch (SimException ex) {
                ex.addContext("...while working on block " + Hex.u2(offset));
                throw ex;
            }
        }
        addReturnBlock();
        addSynchExceptionHandlerBlock();
        addExceptionSetupBlocks();
        if (hasSubroutines) {
            inlineSubroutines();
        }
    }
    private void setFirstFrame() {
        Prototype desc = method.getEffectiveDescriptor();
        startFrames[0].initializeWithParameters(desc.getParameterTypes());
        startFrames[0].setImmutable();
    }
    private void processBlock(ByteBlock block, Frame frame, int[] workSet) {
        ByteCatchList catches = block.getCatches();
        machine.startBlock(catches.toRopCatchList());
        frame = frame.copy();
        sim.simulate(block, frame);
        frame.setImmutable();
        int extraBlockCount = machine.getExtraBlockCount();
        ArrayList<Insn> insns = machine.getInsns();
        int insnSz = insns.size();
        int catchSz = catches.size();
        IntList successors = block.getSuccessors();
        int startSuccessorIndex;
        Subroutine calledSubroutine = null;
        if (machine.hasJsr()) {
            startSuccessorIndex = 1;
            int subroutineLabel = successors.get(1);
            if (subroutines[subroutineLabel] == null) {
                subroutines[subroutineLabel] =
                    new Subroutine (subroutineLabel);
            }
            subroutines[subroutineLabel].addCallerBlock(block.getLabel());
            calledSubroutine = subroutines[subroutineLabel];
        } else if (machine.hasRet()) {
            ReturnAddress ra = machine.getReturnAddress();
            int subroutineLabel = ra.getSubroutineAddress();
            if (subroutines[subroutineLabel] == null) {
                subroutines[subroutineLabel]
                        = new Subroutine (subroutineLabel, block.getLabel());
            } else {
                subroutines[subroutineLabel].addRetBlock(block.getLabel());
            }
            successors = subroutines[subroutineLabel].getSuccessors();
            subroutines[subroutineLabel]
                    .mergeToSuccessors(frame, workSet);
            startSuccessorIndex = successors.size();
        } else if (machine.wereCatchesUsed()) {
            startSuccessorIndex = catchSz;
        } else {
            startSuccessorIndex = 0;
        }
        int succSz = successors.size();
        for (int i = startSuccessorIndex; i < succSz;
             i++) {
            int succ = successors.get(i);
            try {
                mergeAndWorkAsNecessary(succ, block.getLabel(),
                        calledSubroutine, frame, workSet);
            } catch (SimException ex) {
                ex.addContext("...while merging to block " + Hex.u2(succ));
                throw ex;
            }
        }
        if ((succSz == 0) && machine.returns()) {
            successors = IntList.makeImmutable(getSpecialLabel(RETURN));
            succSz = 1;
        }
        int primarySucc;
        if (succSz == 0) {
            primarySucc = -1;
        } else {
            primarySucc = machine.getPrimarySuccessorIndex();
            if (primarySucc >= 0) {
                primarySucc = successors.get(primarySucc);
            }
        }
        boolean synch = isSynchronized() && machine.canThrow();
        if (synch || (catchSz != 0)) {
            boolean catchesAny = false;
            IntList newSucc = new IntList(succSz);
            for (int i = 0; i < catchSz; i++) {
                ByteCatchList.Item one = catches.get(i);
                CstType exceptionClass = one.getExceptionClass();
                int targ = one.getHandlerPc();
                catchesAny |= (exceptionClass == CstType.OBJECT);
                Frame f = frame.makeExceptionHandlerStartFrame(exceptionClass);
                try {
                    mergeAndWorkAsNecessary(targ, block.getLabel(),
                            null, f, workSet);
                } catch (SimException ex) {
                    ex.addContext("...while merging exception to block " +
                                  Hex.u2(targ));
                    throw ex;
                }
                Type already = catchTypes[targ];
                if (already == null) {
                    catchTypes[targ] = exceptionClass.getClassType();
                } else if (already != exceptionClass.getClassType()) {
                    catchTypes[targ] = Type.OBJECT;
                }
                newSucc.add(getExceptionSetupLabel(targ));
            }
            if (synch && !catchesAny) {
                newSucc.add(getSpecialLabel(SYNCH_CATCH_1));
                synchNeedsExceptionHandler = true;
                for (int i = insnSz - extraBlockCount - 1; i < insnSz; i++) {
                    Insn insn = insns.get(i);
                    if (insn.canThrow()) {
                        insn = insn.withAddedCatch(Type.OBJECT);
                        insns.set(i, insn);
                    }
                }
            }
            if (primarySucc >= 0) {
                newSucc.add(primarySucc);
            }
            newSucc.setImmutable();
            successors = newSucc;
        }
        int primarySuccListIndex = successors.indexOf(primarySucc);
        for (; extraBlockCount > 0; extraBlockCount--) {
            Insn extraInsn = insns.get(--insnSz);
            boolean needsGoto
                    = extraInsn.getOpcode().getBranchingness()
                        == Rop.BRANCH_NONE;
            InsnList il = new InsnList(needsGoto ? 2 : 1);
            IntList extraBlockSuccessors = successors;
            il.set(0, extraInsn);
            if (needsGoto) {
                il.set(1, new PlainInsn(Rops.GOTO,
                        extraInsn.getPosition(), null,
                        RegisterSpecList.EMPTY));
                extraBlockSuccessors = IntList.makeImmutable(primarySucc);
            }
            il.setImmutable();
            int label = getAvailableLabel();
            BasicBlock bb = new BasicBlock(label, il, extraBlockSuccessors,
                    primarySucc);
            addBlock(bb, frame.getSubroutines());
            successors = successors.mutableCopy();
            successors.set(primarySuccListIndex, label);
            successors.setImmutable();
            primarySucc = label;
        }
        Insn lastInsn = (insnSz == 0) ? null : insns.get(insnSz - 1);
        if ((lastInsn == null) ||
            (lastInsn.getOpcode().getBranchingness() == Rop.BRANCH_NONE)) {
            SourcePosition pos = (lastInsn == null) ? SourcePosition.NO_INFO :
                lastInsn.getPosition();
            insns.add(new PlainInsn(Rops.GOTO, pos, null,
                                    RegisterSpecList.EMPTY));
            insnSz++;
        }
        InsnList il = new InsnList(insnSz);
        for (int i = 0; i < insnSz; i++) {
            il.set(i, insns.get(i));
        }
        il.setImmutable();
        BasicBlock bb =
            new BasicBlock(block.getLabel(), il, successors, primarySucc);
        addOrReplaceBlock(bb, frame.getSubroutines());
    }
    private void mergeAndWorkAsNecessary(int label, int pred,
            Subroutine calledSubroutine, Frame frame, int[] workSet) {
        Frame existing = startFrames[label];
        Frame merged;
        if (existing != null) {
            if (calledSubroutine != null) {
                merged = existing.mergeWithSubroutineCaller(frame,
                        calledSubroutine.getStartBlock(), pred);
            } else {
                merged = existing.mergeWith(frame);
            }
            if (merged != existing) {
                startFrames[label] = merged;
                Bits.set(workSet, label);
            }
        } else {
            if (calledSubroutine != null) {
                startFrames[label]
                        = frame.makeNewSubroutineStartFrame(label, pred);
            } else {
                startFrames[label] = frame;
            }
            Bits.set(workSet, label);
        }
    }
    private void addSetupBlocks() {
        LocalVariableList localVariables = method.getLocalVariables();
        SourcePosition pos = method.makeSourcePosistion(0);
        Prototype desc = method.getEffectiveDescriptor();
        StdTypeList params = desc.getParameterTypes();
        int sz = params.size();
        InsnList insns = new InsnList(sz + 1);
        int at = 0;
        for (int i = 0; i < sz; i++) {
            Type one = params.get(i);
            LocalVariableList.Item local =
                localVariables.pcAndIndexToLocal(0, at);
            RegisterSpec result = (local == null) ?
                RegisterSpec.make(at, one) :
                RegisterSpec.makeLocalOptional(at, one, local.getLocalItem());
            Insn insn = new PlainCstInsn(Rops.opMoveParam(one), pos, result,
                                         RegisterSpecList.EMPTY,
                                         CstInteger.make(at));
            insns.set(i, insn);
            at += one.getCategory();
        }
        insns.set(sz, new PlainInsn(Rops.GOTO, pos, null,
                                    RegisterSpecList.EMPTY));
        insns.setImmutable();
        boolean synch = isSynchronized();
        int label = synch ? getSpecialLabel(SYNCH_SETUP_1) : 0;
        BasicBlock bb =
            new BasicBlock(getSpecialLabel(PARAM_ASSIGNMENT), insns,
                           IntList.makeImmutable(label), label);
        addBlock(bb, IntList.EMPTY);
        if (synch) {
            RegisterSpec synchReg = getSynchReg();
            Insn insn;
            if (isStatic()) {
                insn = new ThrowingCstInsn(Rops.CONST_OBJECT, pos,
                                           RegisterSpecList.EMPTY,
                                           StdTypeList.EMPTY,
                                           method.getDefiningClass());
                insns = new InsnList(1);
                insns.set(0, insn);
            } else {
                insns = new InsnList(2);
                insn = new PlainCstInsn(Rops.MOVE_PARAM_OBJECT, pos,
                                        synchReg, RegisterSpecList.EMPTY,
                                        CstInteger.VALUE_0);
                insns.set(0, insn);
                insns.set(1, new PlainInsn(Rops.GOTO, pos, null,
                                           RegisterSpecList.EMPTY));
            }
            int label2 = getSpecialLabel(SYNCH_SETUP_2);
            insns.setImmutable();
            bb = new BasicBlock(label, insns,
                                IntList.makeImmutable(label2), label2);
            addBlock(bb, IntList.EMPTY);
            insns = new InsnList(isStatic() ? 2 : 1);
            if (isStatic()) {
                insns.set(0, new PlainInsn(Rops.opMoveResultPseudo(synchReg),
                        pos, synchReg, RegisterSpecList.EMPTY));
            }
            insn = new ThrowingInsn(Rops.MONITOR_ENTER, pos,
                                    RegisterSpecList.make(synchReg),
                                    StdTypeList.EMPTY);
            insns.set(isStatic() ? 1 :0, insn);
            insns.setImmutable();
            bb = new BasicBlock(label2, insns, IntList.makeImmutable(0), 0);
            addBlock(bb, IntList.EMPTY);
        }
    }
    private void addReturnBlock() {
        Rop returnOp = machine.getReturnOp();
        if (returnOp == null) {
            return;
        }
        SourcePosition returnPos = machine.getReturnPosition();
        int label = getSpecialLabel(RETURN);
        if (isSynchronized()) {
            InsnList insns = new InsnList(1);
            Insn insn = new ThrowingInsn(Rops.MONITOR_EXIT, returnPos,
                                         RegisterSpecList.make(getSynchReg()),
                                         StdTypeList.EMPTY);
            insns.set(0, insn);
            insns.setImmutable();
            int nextLabel = getSpecialLabel(SYNCH_RETURN);
            BasicBlock bb =
                new BasicBlock(label, insns,
                               IntList.makeImmutable(nextLabel), nextLabel);
            addBlock(bb, IntList.EMPTY);
            label = nextLabel;
        }
        InsnList insns = new InsnList(1);
        TypeList sourceTypes = returnOp.getSources();
        RegisterSpecList sources;
        if (sourceTypes.size() == 0) {
            sources = RegisterSpecList.EMPTY;
        } else {
            RegisterSpec source = RegisterSpec.make(0, sourceTypes.getType(0));
            sources = RegisterSpecList.make(source);
        }
        Insn insn = new PlainInsn(returnOp, returnPos, null, sources);
        insns.set(0, insn);
        insns.setImmutable();
        BasicBlock bb = new BasicBlock(label, insns, IntList.EMPTY, -1);
        addBlock(bb, IntList.EMPTY);
    }
    private void addSynchExceptionHandlerBlock() {
        if (!synchNeedsExceptionHandler) {
            return;
        }
        SourcePosition pos = method.makeSourcePosistion(0);
        RegisterSpec exReg = RegisterSpec.make(0, Type.THROWABLE);
        BasicBlock bb;
        Insn insn;
        InsnList insns = new InsnList(2);
        insn = new PlainInsn(Rops.opMoveException(Type.THROWABLE), pos,
                             exReg, RegisterSpecList.EMPTY);
        insns.set(0, insn);
        insn = new ThrowingInsn(Rops.MONITOR_EXIT, pos,
                                RegisterSpecList.make(getSynchReg()),
                                StdTypeList.EMPTY);
        insns.set(1, insn);
        insns.setImmutable();
        int label2 = getSpecialLabel(SYNCH_CATCH_2);
        bb = new BasicBlock(getSpecialLabel(SYNCH_CATCH_1), insns,
                            IntList.makeImmutable(label2), label2);
        addBlock(bb, IntList.EMPTY);
        insns = new InsnList(1);
        insn = new ThrowingInsn(Rops.THROW, pos,
                                RegisterSpecList.make(exReg),
                                StdTypeList.EMPTY);
        insns.set(0, insn);
        insns.setImmutable();
        bb = new BasicBlock(label2, insns, IntList.EMPTY, -1);
        addBlock(bb, IntList.EMPTY);
    }
    private void addExceptionSetupBlocks() {
        int len = catchTypes.length;
        for (int i = 0; i < len; i++) {
            Type one = catchTypes[i];
            if (one != null) {
                Insn proto = labelToBlock(i).getFirstInsn();
                SourcePosition pos = proto.getPosition();
                InsnList il = new InsnList(2);
                Insn insn = new PlainInsn(Rops.opMoveException(one),
                                          pos,
                                          RegisterSpec.make(maxLocals, one),
                                          RegisterSpecList.EMPTY);
                il.set(0, insn);
                insn = new PlainInsn(Rops.GOTO, pos, null,
                                     RegisterSpecList.EMPTY);
                il.set(1, insn);
                il.setImmutable();
                BasicBlock bb = new BasicBlock(getExceptionSetupLabel(i),
                                               il,
                                               IntList.makeImmutable(i),
                                               i);
                addBlock(bb, startFrames[i].getSubroutines());
            }
        }
    }
    private boolean isSubroutineCaller(BasicBlock bb) {
        IntList successors = bb.getSuccessors();
        if (successors.size() < 2) return false;
        int subLabel = successors.get(1);
        return (subLabel < subroutines.length)
                && (subroutines[subLabel] != null);
    }
    private void inlineSubroutines() {
        final IntList reachableSubroutineCallerLabels = new IntList(4);
        forEachNonSubBlockDepthFirst(0, new BasicBlock.Visitor() {
            public void visitBlock(BasicBlock b) {
                if (isSubroutineCaller(b)) {
                    reachableSubroutineCallerLabels.add(b.getLabel());
                }
            }
        });
        int largestAllocedLabel = getAvailableLabel();
        ArrayList<IntList> labelToSubroutines
                = new ArrayList<IntList>(largestAllocedLabel);
        for (int i = 0; i < largestAllocedLabel; i++) {
            labelToSubroutines.add(null);
        }
        for (int i = 0; i < result.size(); i++) {
            BasicBlock b = result.get(i);
            if (b == null) {
                continue;
            }
            IntList subroutineList = resultSubroutines.get(i);
            labelToSubroutines.set(b.getLabel(), subroutineList);
        }
        int sz = reachableSubroutineCallerLabels.size();
        for (int i = 0 ; i < sz ; i++) {
            int label = reachableSubroutineCallerLabels.get(i);
            new SubroutineInliner(
                    new LabelAllocator(getAvailableLabel()),
                    labelToSubroutines)
                    .inlineSubroutineCalledFrom(labelToBlock(label));
        }
        deleteUnreachableBlocks();
    }
    private void deleteUnreachableBlocks() {
        final IntList reachableLabels = new IntList(result.size());
        resultSubroutines.clear();
        forEachNonSubBlockDepthFirst(getSpecialLabel(PARAM_ASSIGNMENT),
                new BasicBlock.Visitor() {
            public void visitBlock(BasicBlock b) {
                reachableLabels.add(b.getLabel());
            }
        });
        reachableLabels.sort();
        for (int i = result.size() - 1 ; i >= 0 ; i--) {
            if (reachableLabels.indexOf(result.get(i).getLabel()) < 0) {
                result.remove(i);
            }
        }
    }
    private static class LabelAllocator {
        int nextAvailableLabel;
        LabelAllocator(int startLabel) {
            nextAvailableLabel = startLabel;
        }
        int getNextLabel() {
            return nextAvailableLabel++;
        }
    }
    private class SubroutineInliner {
        private final HashMap<Integer, Integer> origLabelToCopiedLabel;
        private final BitSet workList;
        private int subroutineStart;
        private int subroutineSuccessor;
        private final LabelAllocator labelAllocator;
        private final ArrayList<IntList> labelToSubroutines;
        SubroutineInliner(final LabelAllocator labelAllocator,
                ArrayList<IntList> labelToSubroutines) {
            origLabelToCopiedLabel = new HashMap<Integer, Integer>();
            workList = new BitSet(maxLabel);
            this.labelAllocator = labelAllocator;
            this.labelToSubroutines = labelToSubroutines;
        }
        void inlineSubroutineCalledFrom(final BasicBlock b) {
            subroutineSuccessor = b.getSuccessors().get(0);
            subroutineStart = b.getSuccessors().get(1);
            int newSubStartLabel = mapOrAllocateLabel(subroutineStart);
            for (int label = workList.nextSetBit(0); label >= 0;
                 label = workList.nextSetBit(0)) {
                workList.clear(label);
                int newLabel = origLabelToCopiedLabel.get(label);
                copyBlock(label, newLabel);
                if (isSubroutineCaller(labelToBlock(label))) {
                    new SubroutineInliner(labelAllocator, labelToSubroutines)
                        .inlineSubroutineCalledFrom(labelToBlock(newLabel));
                }
            }
            addOrReplaceBlockNoDelete(
                new BasicBlock(b.getLabel(), b.getInsns(),
                    IntList.makeImmutable (newSubStartLabel),
                            newSubStartLabel),
                labelToSubroutines.get(b.getLabel()));
       }
       private void copyBlock(int origLabel, int newLabel) {
            BasicBlock origBlock = labelToBlock(origLabel);
            final IntList origSuccessors = origBlock.getSuccessors();
            IntList successors;
            int primarySuccessor = -1;
            Subroutine subroutine;
            if (isSubroutineCaller(origBlock)) {
                successors = IntList.makeImmutable(
                        mapOrAllocateLabel(origSuccessors.get(0)),
                        origSuccessors.get(1));
            } else if (null
                    != (subroutine = subroutineFromRetBlock(origLabel))) {
                if (subroutine.startBlock != subroutineStart) {
                    throw new RuntimeException (
                            "ret instruction returns to label "
                            + Hex.u2 (subroutine.startBlock)
                            + " expected: " + Hex.u2(subroutineStart));
                }
                successors = IntList.makeImmutable(subroutineSuccessor);
                primarySuccessor = subroutineSuccessor;
            } else {
                int origPrimary = origBlock.getPrimarySuccessor();
                int sz = origSuccessors.size();
                successors = new IntList(sz);
                for (int i = 0 ; i < sz ; i++) {
                    int origSuccLabel = origSuccessors.get(i);
                    int newSuccLabel =  mapOrAllocateLabel(origSuccLabel);
                    successors.add(newSuccLabel);
                    if (origPrimary == origSuccLabel) {
                        primarySuccessor = newSuccLabel;
                    }
                }
                successors.setImmutable();
            }
            addBlock (
                new BasicBlock(newLabel,
                    filterMoveReturnAddressInsns(origBlock.getInsns()),
                    successors, primarySuccessor),
                    labelToSubroutines.get(newLabel));
        }
        private boolean involvedInSubroutine(int label, int subroutineStart) {
            IntList subroutinesList = labelToSubroutines.get(label);
            return (subroutinesList.size() > 0
                    && subroutinesList.top() == subroutineStart);
        }
        private int mapOrAllocateLabel(int origLabel) {
            int resultLabel;
            Integer mappedLabel = origLabelToCopiedLabel.get(origLabel);
            if (mappedLabel != null) {
                resultLabel = mappedLabel;
            } else if (!involvedInSubroutine(origLabel,subroutineStart)) {
                resultLabel = origLabel;
            } else {
                resultLabel = labelAllocator.getNextLabel();
                workList.set(origLabel);
                origLabelToCopiedLabel.put(origLabel, resultLabel);
                while (labelToSubroutines.size() <= resultLabel) {
                    labelToSubroutines.add(null);
                }
                labelToSubroutines.set(resultLabel,
                        labelToSubroutines.get(origLabel));
            }
            return resultLabel;
        }
    }
    private Subroutine subroutineFromRetBlock(int label) {
        for (int i = subroutines.length - 1 ; i >= 0 ; i--) {
            if (subroutines[i] != null) {
                Subroutine subroutine = subroutines[i];
                if (subroutine.retBlocks.get(label)) {
                    return subroutine;
                }
            }
        }
        return null;
    }
    private InsnList filterMoveReturnAddressInsns(InsnList insns) {
        int sz;
        int newSz = 0;
        sz = insns.size();
        for (int i = 0; i < sz; i++) {
            if (insns.get(i).getOpcode() != Rops.MOVE_RETURN_ADDRESS) {
                newSz++;
            }
        }
        if (newSz == sz) {
            return insns;
        }
        InsnList newInsns = new InsnList(newSz);
        int newIndex = 0;
        for (int i = 0; i < sz; i++) {
            Insn insn = insns.get(i);
            if (insn.getOpcode() != Rops.MOVE_RETURN_ADDRESS) {
                newInsns.set(newIndex++, insn);
            }
        }
        newInsns.setImmutable();
        return newInsns;
    }
    private void forEachNonSubBlockDepthFirst(int firstLabel,
            BasicBlock.Visitor v) {
        forEachNonSubBlockDepthFirst0(labelToBlock(firstLabel),
                v, new BitSet(maxLabel));
    }
    private void forEachNonSubBlockDepthFirst0(
            BasicBlock next, BasicBlock.Visitor v, BitSet visited) {
        v.visitBlock(next);
        visited.set(next.getLabel());
        IntList successors = next.getSuccessors();
        int sz = successors.size();
        for (int i = 0; i < sz; i++) {
            int succ = successors.get(i);
            if (visited.get(succ)) {
                continue;
            }
            if (isSubroutineCaller(next) && i > 0) {
                continue;
            }
            int idx = labelToResultIndex(succ);
            if (idx >= 0) {
                forEachNonSubBlockDepthFirst0(result.get(idx), v, visited);
            }
        }
    }
}
