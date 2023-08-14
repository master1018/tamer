public final class SsaMethod {
    private ArrayList<SsaBasicBlock> blocks;
    private int entryBlockIndex;
    private int exitBlockIndex;
    private int registerCount;
    private int spareRegisterBase;
    private int borrowedSpareRegisters;
    private int maxLabel;
    private final int paramWidth;
    private final boolean isStatic;
    private SsaInsn[] definitionList;
    private ArrayList<SsaInsn>[] useList;
    private List<SsaInsn>[] unmodifiableUseList;
    private boolean backMode;
    public static SsaMethod newFromRopMethod(RopMethod ropMethod,
            int paramWidth, boolean isStatic) {
        SsaMethod result = new SsaMethod(ropMethod, paramWidth, isStatic);
        result.convertRopToSsaBlocks(ropMethod);
        return result;
    }
    private SsaMethod(RopMethod ropMethod, int paramWidth, boolean isStatic) {
        this.paramWidth = paramWidth;
        this.isStatic = isStatic;
        this.backMode = false;
        this.maxLabel = ropMethod.getBlocks().getMaxLabel();
        this.registerCount = ropMethod.getBlocks().getRegCount();
        this.spareRegisterBase = registerCount;
    }
    static BitSet bitSetFromLabelList(BasicBlockList blocks,
            IntList labelList) {
        BitSet result = new BitSet(blocks.size());
        for (int i = 0, sz = labelList.size(); i < sz; i++) {
            result.set(blocks.indexOfLabel(labelList.get(i)));
        }
        return result;
    }
    public static IntList indexListFromLabelList(BasicBlockList ropBlocks,
            IntList labelList) {
        IntList result = new IntList(labelList.size());
        for (int i = 0, sz = labelList.size(); i < sz; i++) {
            result.add(ropBlocks.indexOfLabel(labelList.get(i)));
        }
        return result;
    }
    private void convertRopToSsaBlocks(RopMethod rmeth) {
        BasicBlockList ropBlocks = rmeth.getBlocks();
        int sz = ropBlocks.size();
        blocks = new ArrayList<SsaBasicBlock>(sz + 2);
        for (int i = 0; i < sz; i++) {
            SsaBasicBlock sbb = SsaBasicBlock.newFromRop(rmeth, i, this);
            blocks.add(sbb);
        }
        int origEntryBlockIndex = rmeth.getBlocks()
                .indexOfLabel(rmeth.getFirstLabel());
        SsaBasicBlock entryBlock
                = blocks.get(origEntryBlockIndex).insertNewPredecessor();
        entryBlockIndex = entryBlock.getIndex();
        exitBlockIndex = -1; 
    }
     void makeExitBlock() {
        if (exitBlockIndex >= 0) {
            throw new RuntimeException("must be called at most once");
        }
        exitBlockIndex = blocks.size();
        SsaBasicBlock exitBlock
                = new SsaBasicBlock(exitBlockIndex, maxLabel++, this);
        blocks.add(exitBlock);
        for (SsaBasicBlock block : blocks) {
            block.exitBlockFixup(exitBlock);
        }
        if (exitBlock.getPredecessors().cardinality() == 0) {
            blocks.remove(exitBlockIndex);
            exitBlockIndex = -1;
            maxLabel--;
        }
    }
    private static SsaInsn getGoto(SsaBasicBlock block) {
        return new NormalSsaInsn (
                new PlainInsn(Rops.GOTO, SourcePosition.NO_INFO,
                    null, RegisterSpecList.EMPTY), block);
    }
    public SsaBasicBlock makeNewGotoBlock() {
        int newIndex = blocks.size();
        SsaBasicBlock newBlock = new SsaBasicBlock(newIndex, maxLabel++, this);
        newBlock.getInsns().add(getGoto(newBlock));
        blocks.add(newBlock);
        return newBlock;
    }
    public int getEntryBlockIndex() {
        return entryBlockIndex;
    }
    public SsaBasicBlock getEntryBlock() {
        return blocks.get(entryBlockIndex);
    }
    public int getExitBlockIndex() {
        return exitBlockIndex;
    }
    public SsaBasicBlock getExitBlock() {
        return exitBlockIndex < 0 ? null : blocks.get(exitBlockIndex);
    }
    public int blockIndexToRopLabel(int bi) {
        if (bi < 0) {
            return -1;
        }
        return blocks.get(bi).getRopLabel();
    }
    public int getRegCount() {
        return registerCount;
    }
    public int getParamWidth() {
        return paramWidth;
    }
    public boolean isStatic() {
        return isStatic;
    }
    public int borrowSpareRegister(int category) {
        int result = spareRegisterBase + borrowedSpareRegisters;
        borrowedSpareRegisters += category;
        registerCount = Math.max(registerCount, result + category);
        return result;
    }
    public void returnSpareRegisters() {
        borrowedSpareRegisters = 0;
    }
    public ArrayList<SsaBasicBlock> getBlocks() {
        return blocks;
    }
    public int getCountReachableBlocks() {
        int ret = 0;
        for (SsaBasicBlock b : blocks) {
            if (b.isReachable()) {
                ret++;
            }
        }
        return ret;
    }
    public void mapRegisters(RegisterMapper mapper) {
        for (SsaBasicBlock block : getBlocks()) {
            for (SsaInsn insn : block.getInsns()) {
                insn.mapRegisters(mapper);
            }
        }
        registerCount = mapper.getNewRegisterCount();
        spareRegisterBase = registerCount;
    }
    public SsaInsn getDefinitionForRegister(int reg) {
        if (backMode) {
            throw new RuntimeException("No def list in back mode");
        }
        if (definitionList != null) {
            return definitionList[reg];
        }
        definitionList = new SsaInsn[getRegCount()];
        forEachInsn(new SsaInsn.Visitor() {
            public void visitMoveInsn (NormalSsaInsn insn) {
                definitionList[insn.getResult().getReg()] = insn;
            }
            public void visitPhiInsn (PhiInsn phi) {
                definitionList[phi.getResult().getReg()] = phi;
            }
            public void visitNonMoveInsn (NormalSsaInsn insn) {
                RegisterSpec result = insn.getResult();
                if (result != null) {
                    definitionList[insn.getResult().getReg()] = insn;
                }
            }
        });
        return definitionList[reg];
    }
    private void buildUseList() {
        if (backMode) {
            throw new RuntimeException("No use list in back mode");
        }
        useList = new ArrayList[registerCount];
        for (int i = 0; i < registerCount; i++) {
            useList[i] = new ArrayList();
        }
        forEachInsn(new SsaInsn.Visitor() {
            public void visitMoveInsn (NormalSsaInsn insn) {
                addToUses(insn);
            }
            public void visitPhiInsn (PhiInsn phi) {
                addToUses(phi);
            }
            public void visitNonMoveInsn (NormalSsaInsn insn) {
                addToUses(insn);
            }
            private void addToUses(SsaInsn insn) {
                RegisterSpecList rl = insn.getSources();
                int sz = rl.size();
                for (int i = 0; i < sz; i++) {
                    useList[rl.get(i).getReg()].add(insn);
                }
            }
        });
        unmodifiableUseList = new List[registerCount];
        for (int i = 0; i < registerCount; i++) {
            unmodifiableUseList[i] = Collections.unmodifiableList(useList[i]);
        }
    }
     void onSourceChanged(SsaInsn insn,
            RegisterSpec oldSource, RegisterSpec newSource) {
        if (useList == null) return;
        if (oldSource != null) {
            int reg = oldSource.getReg();
            useList[reg].remove(insn);
        }
        int reg = newSource.getReg();
        if (useList.length <= reg) {
            useList = null;
            return;
        }
        useList[reg].add(insn);
    }
     void onSourcesChanged(SsaInsn insn,
            RegisterSpecList oldSources) {
        if (useList == null) return;
        if (oldSources != null) {
            removeFromUseList(insn, oldSources);
        }
        RegisterSpecList sources = insn.getSources();
        int szNew = sources.size();
        for (int i = 0; i < szNew; i++) {
            int reg = sources.get(i).getReg();
            useList[reg].add(insn);
        }
    }
    private void removeFromUseList(SsaInsn insn, RegisterSpecList oldSources) {
        if (oldSources == null) {
            return;
        }
        int szNew = oldSources.size();
        for (int i = 0; i < szNew; i++) {
            if (!useList[oldSources.get(i).getReg()].remove(insn)) {
                throw new RuntimeException("use not found");
            }
        }
    }
     void onInsnAdded(SsaInsn insn) {
        onSourcesChanged(insn, null);
        updateOneDefinition(insn, null);
    }
     void onInsnRemoved(SsaInsn insn) {
        if (useList != null) {
            removeFromUseList(insn, insn.getSources());
        }
        RegisterSpec resultReg = insn.getResult();
        if (definitionList != null && resultReg != null) {
            definitionList[resultReg.getReg()] = null;
        }
    }
    public void onInsnsChanged() {
        definitionList = null;
        useList = null;
        unmodifiableUseList = null;
    }
     void updateOneDefinition(SsaInsn insn,
            RegisterSpec oldResult) {
        if (definitionList == null) return;
        if (oldResult != null) {
            int reg = oldResult.getReg();
            definitionList[reg] = null;
        }
        RegisterSpec resultReg = insn.getResult();
        if (resultReg != null) {
            int reg = resultReg.getReg();
            if (definitionList[reg] != null) {
                throw new RuntimeException("Duplicate add of insn");
            } else {
                definitionList[resultReg.getReg()] = insn;
            }
        }
    }
    public List<SsaInsn> getUseListForRegister(int reg) {
        if (unmodifiableUseList == null) {
            buildUseList();
        }
        return unmodifiableUseList[reg];
    }
    public ArrayList<SsaInsn>[] getUseListCopy() {
        if (useList == null) {
            buildUseList();
        }
        ArrayList<SsaInsn>[] useListCopy
                = (ArrayList<SsaInsn>[])(new ArrayList[registerCount]);
        for (int i = 0; i < registerCount; i++) {
            useListCopy[i] = (ArrayList<SsaInsn>)(new ArrayList(useList[i]));
        }
        return useListCopy;
    }
    public boolean isRegALocal(RegisterSpec spec) {
        SsaInsn defn = getDefinitionForRegister(spec.getReg());
        if (defn == null) {
            return false;
        }
        if (defn.getLocalAssignment() != null) return true;
        for (SsaInsn use : getUseListForRegister(spec.getReg())) {
            Insn insn = use.getOriginalRopInsn();
            if (insn != null
                    && insn.getOpcode().getOpcode() == RegOps.MARK_LOCAL) {
                return true;
            }
        }
        return false;
    }
     void setNewRegCount(int newRegCount) {
        registerCount = newRegCount;
        spareRegisterBase = registerCount;
        onInsnsChanged();
    }
    public int makeNewSsaReg() {
        int reg = registerCount++;
        spareRegisterBase = registerCount;
        onInsnsChanged();
        return reg;
    }
    public void forEachInsn(SsaInsn.Visitor visitor) {
        for (SsaBasicBlock block : blocks) {
            block.forEachInsn(visitor);
        }
    }
    public void forEachPhiInsn(PhiInsn.Visitor v) {
        for (SsaBasicBlock block : blocks) {
            block.forEachPhiInsn(v);
        }
    }
    public void forEachBlockDepthFirst(boolean reverse,
            SsaBasicBlock.Visitor v) {
        BitSet visited = new BitSet(blocks.size());
        Stack<SsaBasicBlock> stack = new Stack<SsaBasicBlock>();
        SsaBasicBlock rootBlock = reverse ? getExitBlock() : getEntryBlock();
        if (rootBlock == null) {
            return;
        }
        stack.add(null);    
        stack.add(rootBlock);
        while (stack.size() > 0) {
            SsaBasicBlock cur = stack.pop();
            SsaBasicBlock parent = stack.pop();
            if (!visited.get(cur.getIndex())) {
                BitSet children
                    = reverse ? cur.getPredecessors() : cur.getSuccessors();
                for (int i = children.nextSetBit(0); i >= 0
                        ; i = children.nextSetBit(i + 1)) {
                    stack.add(cur);
                    stack.add(blocks.get(i));
                }
                visited.set(cur.getIndex());
                v.visitBlock(cur, parent);
            }
        }
    }
    public void forEachBlockDepthFirstDom(SsaBasicBlock.Visitor v) {
        BitSet visited = new BitSet(getBlocks().size());
        Stack<SsaBasicBlock> stack = new Stack<SsaBasicBlock>();
        stack.add(getEntryBlock());
        while (stack.size() > 0) {
            SsaBasicBlock cur = stack.pop();
            ArrayList<SsaBasicBlock> curDomChildren = cur.getDomChildren();
            if (!visited.get(cur.getIndex())) {
                for (int i = curDomChildren.size() - 1; i >= 0; i--) {
                    SsaBasicBlock child = curDomChildren.get(i);
                    stack.add(child);
                }
                visited.set(cur.getIndex());
                v.visitBlock(cur, null);
            }
        }
    }
    public void deleteInsns(Set<SsaInsn> deletedInsns) {
        for (SsaBasicBlock block : getBlocks()) {
            ArrayList<SsaInsn> insns = block.getInsns();
            for (int i = insns.size() - 1; i >= 0; i--) {
                SsaInsn insn = insns.get(i);
                if (deletedInsns.contains(insn)) {
                    onInsnRemoved(insn);
                    insns.remove(i);
                }
            }
            int insnsSz = insns.size();
            SsaInsn lastInsn = (insnsSz == 0) ? null : insns.get(insnsSz - 1);
            if (block != getExitBlock() && (insnsSz == 0
                    || lastInsn.getOriginalRopInsn() == null
                    || lastInsn.getOriginalRopInsn().getOpcode()
                        .getBranchingness() == Rop.BRANCH_NONE)) {
                Insn gotoInsn = new PlainInsn(Rops.GOTO,
                        SourcePosition.NO_INFO, null, RegisterSpecList.EMPTY);
                insns.add(SsaInsn.makeFromRop(gotoInsn, block));
            }
        }
    }
    public void setBackMode() {
        backMode = true;
        useList = null;
        definitionList = null;
    }
}
