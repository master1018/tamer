public final class RopTranslator {
    private final RopMethod method;
    private final int positionInfo;
    private final LocalVariableInfo locals;
    private final BlockAddresses addresses;
    private final OutputCollector output;
    private final TranslationVisitor translationVisitor;
    private final int regCount;
    private int[] order;
    private final int paramSize;
    private boolean paramsAreInOrder;
    public static DalvCode translate(RopMethod method, int positionInfo,
                                     LocalVariableInfo locals, int paramSize) {
        RopTranslator translator =
            new RopTranslator(method, positionInfo, locals,
                    paramSize);
        return translator.translateAndGetResult();
    }
    private RopTranslator(RopMethod method, int positionInfo,
                          LocalVariableInfo locals, int paramSize) {
        this.method = method;
        this.positionInfo = positionInfo;
        this.locals = locals;
        this.addresses = new BlockAddresses(method);
        this.paramSize = paramSize;
        this.order = null;
        this.paramsAreInOrder = calculateParamsAreInOrder(method, paramSize);
        BasicBlockList blocks = method.getBlocks();
        int bsz = blocks.size();
        int maxInsns = (bsz * 3) + blocks.getInstructionCount();
        if (locals != null) {
            maxInsns += bsz + locals.getAssignmentCount();
        }
        this.regCount = blocks.getRegCount()
                + (paramsAreInOrder ? 0 : this.paramSize);
        this.output = new OutputCollector(maxInsns, bsz * 3, regCount);
        if (locals != null) {
            this.translationVisitor =
                new LocalVariableAwareTranslationVisitor(output, locals);
        } else {
            this.translationVisitor = new TranslationVisitor(output);
        }
    }
    private static boolean calculateParamsAreInOrder(RopMethod method,
            final int paramSize) {
        final boolean[] paramsAreInOrder = { true };
        final int initialRegCount = method.getBlocks().getRegCount();
        method.getBlocks().forEachInsn(new Insn.BaseVisitor() {
            public void visitPlainCstInsn(PlainCstInsn insn) {
                if (insn.getOpcode().getOpcode()== RegOps.MOVE_PARAM) {
                    int param =
                        ((CstInteger) insn.getConstant()).getValue();
                    paramsAreInOrder[0] = paramsAreInOrder[0]
                            && ((initialRegCount - paramSize + param)
                                == insn.getResult().getReg());
                }
            }
        });
        return paramsAreInOrder[0];
    }
    private DalvCode translateAndGetResult() {
        pickOrder();
        outputInstructions();
        StdCatchBuilder catches =
            new StdCatchBuilder(method, order, addresses);
        return new DalvCode(positionInfo, output.getFinisher(), catches);
    }
    private void outputInstructions() {
        BasicBlockList blocks = method.getBlocks();
        int[] order = this.order;
        int len = order.length;
        for (int i = 0; i < len; i++) {
            int nextI = i + 1;
            int nextLabel = (nextI == order.length) ? -1 : order[nextI];
            outputBlock(blocks.labelToBlock(order[i]), nextLabel);
        }
    }
    private void outputBlock(BasicBlock block, int nextLabel) {
        CodeAddress startAddress = addresses.getStart(block);
        output.add(startAddress);
        if (locals != null) {
            RegisterSpecSet starts = locals.getStarts(block);
            output.add(new LocalSnapshot(startAddress.getPosition(),
                                         starts));
        }
        translationVisitor.setBlock(block, addresses.getLast(block));
        block.getInsns().forEach(translationVisitor);
        output.add(addresses.getEnd(block));
        int succ = block.getPrimarySuccessor();
        Insn lastInsn = block.getLastInsn();
        if ((succ >= 0) && (succ != nextLabel)) {
            Rop lastRop = lastInsn.getOpcode();
            if ((lastRop.getBranchingness() == Rop.BRANCH_IF) &&
                    (block.getSecondarySuccessor() == nextLabel)) {
                output.reverseBranch(1, addresses.getStart(succ));
            } else {
                TargetInsn insn =
                    new TargetInsn(Dops.GOTO, lastInsn.getPosition(),
                            RegisterSpecList.EMPTY,
                            addresses.getStart(succ));
                output.add(insn);
            }
        }
    }
    private void pickOrder() {
        BasicBlockList blocks = method.getBlocks();
        int sz = blocks.size();
        int maxLabel = blocks.getMaxLabel();
        int[] workSet = Bits.makeBitSet(maxLabel);
        int[] tracebackSet = Bits.makeBitSet(maxLabel);
        for (int i = 0; i < sz; i++) {
            BasicBlock one = blocks.get(i);
            Bits.set(workSet, one.getLabel());
        }
        int[] order = new int[sz];
        int at = 0;
        for (int label = method.getFirstLabel();
             label != -1;
             label = Bits.findFirst(workSet, 0)) {
            traceBack:
            for (;;) {
                IntList preds = method.labelToPredecessors(label);
                int psz = preds.size();
                for (int i = 0; i < psz; i++) {
                    int predLabel = preds.get(i);
                    if (Bits.get(tracebackSet, predLabel)) {
                        break;
                    }
                    if (!Bits.get(workSet, predLabel)) {
                        continue;
                    }
                    BasicBlock pred = blocks.labelToBlock(predLabel);
                    if (pred.getPrimarySuccessor() == label) {
                        label = predLabel;
                        Bits.set(tracebackSet, label);
                        continue traceBack;
                    }
                }
                break;
            }
            while (label != -1) {
                Bits.clear(workSet, label);
                Bits.clear(tracebackSet, label);
                order[at] = label;
                at++;
                BasicBlock one = blocks.labelToBlock(label);
                BasicBlock preferredBlock = blocks.preferredSuccessorOf(one);
                if (preferredBlock == null) {
                    break;
                }
                int preferred = preferredBlock.getLabel();
                int primary = one.getPrimarySuccessor();
                if (Bits.get(workSet, preferred)) {
                    label = preferred;
                } else if ((primary != preferred) && (primary >= 0)
                        && Bits.get(workSet, primary)) {
                    label = primary;
                } else {
                    IntList successors = one.getSuccessors();
                    int ssz = successors.size();
                    label = -1;
                    for (int i = 0; i < ssz; i++) {
                        int candidate = successors.get(i);
                        if (Bits.get(workSet, candidate)) {
                            label = candidate;
                            break;
                        }
                    }
                }
            }
        }        
        if (at != sz) {
            throw new RuntimeException("shouldn't happen");
        }
        this.order = order;
    }
    private static RegisterSpecList getRegs(Insn insn) {
        return getRegs(insn, insn.getResult());
    }
    private static RegisterSpecList getRegs(Insn insn,
            RegisterSpec resultReg) {
        RegisterSpecList regs = insn.getSources();
        if (insn.getOpcode().isCommutative()
                && (regs.size() == 2)
                && (resultReg.getReg() == regs.get(1).getReg())) {
            regs = RegisterSpecList.make(regs.get(1), regs.get(0));
        }
        if (resultReg == null) {
            return regs;
        }
        return regs.withFirst(resultReg);
    }
    private class TranslationVisitor implements Insn.Visitor {
        private final OutputCollector output;
        private BasicBlock block;
        private CodeAddress lastAddress;
        public TranslationVisitor(OutputCollector output) {
            this.output = output;
        }
        public void setBlock(BasicBlock block, CodeAddress lastAddress) {
            this.block = block;
            this.lastAddress = lastAddress;
        }
        public void visitPlainInsn(PlainInsn insn) {
            Rop rop = insn.getOpcode();
            if (rop.getOpcode() == RegOps.MARK_LOCAL) {
                return;
            }
            if (rop.getOpcode() == RegOps.MOVE_RESULT_PSEUDO) {
                return;
            }
            SourcePosition pos = insn.getPosition();
            Dop opcode = RopToDop.dopFor(insn);
            DalvInsn di;
            switch (rop.getBranchingness()) {
                case Rop.BRANCH_NONE:
                case Rop.BRANCH_RETURN:
                case Rop.BRANCH_THROW: {
                    di = new SimpleInsn(opcode, pos, getRegs(insn));
                    break;
                }
                case Rop.BRANCH_GOTO: {
                    return;
                }
                case Rop.BRANCH_IF: {
                    int target = block.getSuccessors().get(1);
                    di = new TargetInsn(opcode, pos, getRegs(insn),
                                        addresses.getStart(target));
                    break;
                }
                default: {
                    throw new RuntimeException("shouldn't happen");
                }
            }
            addOutput(di);
        }
        public void visitPlainCstInsn(PlainCstInsn insn) {
            SourcePosition pos = insn.getPosition();
            Dop opcode = RopToDop.dopFor(insn);
            Rop rop = insn.getOpcode();
            int ropOpcode = rop.getOpcode();
            DalvInsn di;
            if (rop.getBranchingness() != Rop.BRANCH_NONE) {
                throw new RuntimeException("shouldn't happen");
            }
            if (ropOpcode == RegOps.MOVE_PARAM) {
                if (!paramsAreInOrder) {
                    RegisterSpec dest = insn.getResult();
                    int param =
                        ((CstInteger) insn.getConstant()).getValue();
                    RegisterSpec source =
                        RegisterSpec.make(regCount - paramSize + param,
                                dest.getType());
                    di = new SimpleInsn(opcode, pos,
                                        RegisterSpecList.make(dest, source));
                    addOutput(di);
                }
            } else {
                RegisterSpecList regs = getRegs(insn);
                di = new CstInsn(opcode, pos, regs, insn.getConstant());
                addOutput(di);
            }
        }
        public void visitSwitchInsn(SwitchInsn insn) {
            SourcePosition pos = insn.getPosition();
            IntList cases = insn.getCases();
            IntList successors = block.getSuccessors();
            int casesSz = cases.size();
            int succSz = successors.size();
            int primarySuccessor = block.getPrimarySuccessor();
            if ((casesSz != (succSz - 1)) ||
                (primarySuccessor != successors.get(casesSz))) {
                throw new RuntimeException("shouldn't happen");
            }
            CodeAddress[] switchTargets = new CodeAddress[casesSz];
            for (int i = 0; i < casesSz; i++) {
                int label = successors.get(i);
                switchTargets[i] = addresses.getStart(label);
            }
            CodeAddress dataAddress = new CodeAddress(pos);
            SwitchData dataInsn =
                new SwitchData(pos, lastAddress, cases, switchTargets);
            Dop opcode = dataInsn.isPacked() ?
                Dops.PACKED_SWITCH : Dops.SPARSE_SWITCH;
            TargetInsn switchInsn =
                new TargetInsn(opcode, pos, getRegs(insn), dataAddress);
            addOutput(lastAddress);
            addOutput(switchInsn);
            addOutputSuffix(new OddSpacer(pos));
            addOutputSuffix(dataAddress);
            addOutputSuffix(dataInsn);
        }
        private RegisterSpec getNextMoveResultPseudo()
        {
            int label = block.getPrimarySuccessor();
            if (label < 0) {
                return null;
            }
            Insn insn
                    = method.getBlocks().labelToBlock(label).getInsns().get(0);
            if (insn.getOpcode().getOpcode() != RegOps.MOVE_RESULT_PSEUDO) {
                return null;
            } else {
                return insn.getResult();
            }            
        }
        public void visitThrowingCstInsn(ThrowingCstInsn insn) {
            SourcePosition pos = insn.getPosition();
            Dop opcode = RopToDop.dopFor(insn);
            Rop rop = insn.getOpcode();
            Constant cst = insn.getConstant();
            if (rop.getBranchingness() != Rop.BRANCH_THROW) {
                throw new RuntimeException("shouldn't happen");
            }
            addOutput(lastAddress);
            if (rop.isCallLike()) {
                RegisterSpecList regs = insn.getSources();
                DalvInsn di = new CstInsn(opcode, pos, regs, cst);
                addOutput(di);
            } else {
                RegisterSpec realResult = getNextMoveResultPseudo();
                RegisterSpecList regs = getRegs(insn, realResult);
                DalvInsn di;
                boolean hasResult = opcode.hasResult()
                        || (rop.getOpcode() == RegOps.CHECK_CAST);
                if (hasResult != (realResult != null)) {
                    throw new RuntimeException(
                            "Insn with result/move-result-pseudo mismatch " +
                            insn);
                }
                if ((rop.getOpcode() == RegOps.NEW_ARRAY) &&
                    (opcode.getOpcode() != DalvOps.NEW_ARRAY)) {
                    di = new SimpleInsn(opcode, pos, regs);
                } else {
                    di = new CstInsn(opcode, pos, regs, cst);
                }
                addOutput(di);
            }
        }
        public void visitThrowingInsn(ThrowingInsn insn) {
            SourcePosition pos = insn.getPosition();
            Dop opcode = RopToDop.dopFor(insn);
            Rop rop = insn.getOpcode();
            RegisterSpec realResult;
            if (rop.getBranchingness() != Rop.BRANCH_THROW) {
                throw new RuntimeException("shouldn't happen");
            }
            realResult = getNextMoveResultPseudo();
            if (opcode.hasResult() != (realResult != null)) {
                throw new RuntimeException(
                        "Insn with result/move-result-pseudo mismatch" + insn);
            }
            addOutput(lastAddress);
            DalvInsn di = new SimpleInsn(opcode, pos,
                    getRegs(insn, realResult));
            addOutput(di);
        }
        public void visitFillArrayDataInsn(FillArrayDataInsn insn) {
            SourcePosition pos = insn.getPosition();
            Constant cst = insn.getConstant();
            ArrayList<Constant> values = insn.getInitValues();
            Rop rop = insn.getOpcode();
            if (rop.getBranchingness() != Rop.BRANCH_NONE) {
                throw new RuntimeException("shouldn't happen");
            }
            CodeAddress dataAddress = new CodeAddress(pos);
            ArrayData dataInsn =
                new ArrayData(pos, lastAddress, values, cst);
            TargetInsn fillArrayDataInsn =
                new TargetInsn(Dops.FILL_ARRAY_DATA, pos, getRegs(insn),
                        dataAddress);
            addOutput(lastAddress);
            addOutput(fillArrayDataInsn);
            addOutputSuffix(new OddSpacer(pos));
            addOutputSuffix(dataAddress);
            addOutputSuffix(dataInsn);
        }
        protected void addOutput(DalvInsn insn) {
            output.add(insn);
        }
        protected void addOutputSuffix(DalvInsn insn) {
            output.addSuffix(insn);
        }
    }
    private class LocalVariableAwareTranslationVisitor
            extends TranslationVisitor {
        private LocalVariableInfo locals;
        public LocalVariableAwareTranslationVisitor(OutputCollector output,
                                                    LocalVariableInfo locals) {
            super(output);
            this.locals = locals;
        }
        @Override
        public void visitPlainInsn(PlainInsn insn) {
            super.visitPlainInsn(insn);
            addIntroductionIfNecessary(insn);
        }
        @Override
        public void visitPlainCstInsn(PlainCstInsn insn) {
            super.visitPlainCstInsn(insn);
            addIntroductionIfNecessary(insn);
        }
        @Override
        public void visitSwitchInsn(SwitchInsn insn) {
            super.visitSwitchInsn(insn);
            addIntroductionIfNecessary(insn);
        }
        @Override
        public void visitThrowingCstInsn(ThrowingCstInsn insn) {
            super.visitThrowingCstInsn(insn);
            addIntroductionIfNecessary(insn);
        }
        @Override
        public void visitThrowingInsn(ThrowingInsn insn) {
            super.visitThrowingInsn(insn);
            addIntroductionIfNecessary(insn);
        }
        public void addIntroductionIfNecessary(Insn insn) {
            RegisterSpec spec = locals.getAssignment(insn);
            if (spec != null) {
                addOutput(new LocalStart(insn.getPosition(), spec));
            }
        }
    }    
}
