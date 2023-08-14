public class ConstCollector {
    private static final int MAX_COLLECTED_CONSTANTS = 5;
    private static boolean COLLECT_STRINGS = false;
    private static boolean COLLECT_ONE_LOCAL = false;
    private final SsaMethod ssaMeth;
    public static void process(SsaMethod ssaMethod) {
        ConstCollector cc = new ConstCollector(ssaMethod);
        cc.run();
    }
    private ConstCollector(SsaMethod ssaMethod) {
        this.ssaMeth = ssaMethod;
    }
    private void run() {
        int regSz = ssaMeth.getRegCount();
        ArrayList<TypedConstant> constantList
                = getConstsSortedByCountUse();
        int toCollect = Math.min(constantList.size(), MAX_COLLECTED_CONSTANTS);
        SsaBasicBlock start = ssaMeth.getEntryBlock();
        HashMap<TypedConstant, RegisterSpec> newRegs
                = new HashMap<TypedConstant, RegisterSpec> (toCollect);
        for (int i = 0; i < toCollect; i++) {
            TypedConstant cst = constantList.get(i);
            RegisterSpec result
                    = RegisterSpec.make(ssaMeth.makeNewSsaReg(), cst);
            Rop constRop = Rops.opConst(cst);
            if (constRop.getBranchingness() == Rop.BRANCH_NONE) {
                start.addInsnToHead(
                        new PlainCstInsn(Rops.opConst(cst),
                                SourcePosition.NO_INFO, result,
                                RegisterSpecList.EMPTY, cst));
            } else {
                SsaBasicBlock entryBlock = ssaMeth.getEntryBlock();
                SsaBasicBlock successorBlock
                        = entryBlock.getPrimarySuccessor();
                SsaBasicBlock constBlock
                        = entryBlock.insertNewSuccessor(successorBlock);
                constBlock.replaceLastInsn(
                        new ThrowingCstInsn(constRop, SourcePosition.NO_INFO,
                                RegisterSpecList.EMPTY,
                                StdTypeList.EMPTY, cst));
                SsaBasicBlock resultBlock
                        = constBlock.insertNewSuccessor(successorBlock);
                PlainInsn insn 
                    = new PlainInsn(
                            Rops.opMoveResultPseudo(result.getTypeBearer()),
                            SourcePosition.NO_INFO,
                            result, RegisterSpecList.EMPTY);
                resultBlock.addInsnToHead(insn);
            }
            newRegs.put(cst, result);
        }
        updateConstUses(newRegs, regSz);
    }
    private ArrayList<TypedConstant> getConstsSortedByCountUse() {
        int regSz = ssaMeth.getRegCount();
        final HashMap<TypedConstant, Integer> countUses
                = new HashMap<TypedConstant, Integer>();
        final HashSet<TypedConstant> usedByLocal
                = new HashSet<TypedConstant>();
        for (int i = 0; i < regSz; i++) {
            SsaInsn insn = ssaMeth.getDefinitionForRegister(i);
            if (insn == null) continue;
            RegisterSpec result = insn.getResult();
            TypeBearer typeBearer = result.getTypeBearer();
            if (!typeBearer.isConstant()) continue;
            TypedConstant cst = (TypedConstant) typeBearer;
            if (insn.canThrow()) {
                if (!(cst instanceof CstString) || !COLLECT_STRINGS) {
                    continue;
                }
                if (insn.getBlock().getSuccessors().cardinality() > 1) {
                    continue;
                }
            }
            if (ssaMeth.isRegALocal(result)) {
                if (!COLLECT_ONE_LOCAL) {
                    continue;
                } else {
                    if (usedByLocal.contains(cst)) {
                        continue;
                    } else {
                        usedByLocal.add(cst);
                    }
                }
            }
            Integer has = countUses.get(cst);
            if (has == null) {
                countUses.put(cst, 1);
            } else {
                countUses.put(cst, has + 1);
            }
        }
        ArrayList<TypedConstant> constantList = new ArrayList<TypedConstant>();
        for (Map.Entry<TypedConstant, Integer> entry : countUses.entrySet()) {
            if (entry.getValue() > 1) {
                constantList.add(entry.getKey());
            }
        }
        Collections.sort(constantList, new Comparator<Constant>() {
            public int compare(Constant a, Constant b) {
                int ret;
                ret = countUses.get(b) - countUses.get(a);
                if (ret == 0) {
                    ret = a.compareTo(b);
                }
                return ret;
            }
            public boolean equals (Object obj) {
                return obj == this;
            }
        });
        return constantList;
    }
    private void fixLocalAssignment(RegisterSpec origReg,
            RegisterSpec newReg) {
        for (SsaInsn use : ssaMeth.getUseListForRegister(origReg.getReg())) {
            RegisterSpec localAssignment = use.getLocalAssignment();
            if (localAssignment == null) {
                continue;
            }
            if (use.getResult() == null) {
                continue;
            }
            LocalItem local = localAssignment.getLocalItem();
            use.setResultLocal(null);
            newReg = newReg.withLocalItem(local);
            SsaInsn newInsn
                    = SsaInsn.makeFromRop(
                        new PlainInsn(Rops.opMarkLocal(newReg),
                        SourcePosition.NO_INFO, null,
                                RegisterSpecList.make(newReg)),
                    use.getBlock());
            ArrayList<SsaInsn> insns = use.getBlock().getInsns();
            insns.add(insns.indexOf(use) + 1, newInsn);
        }
    }
    private void updateConstUses(HashMap<TypedConstant, RegisterSpec> newRegs,
            int origRegCount) {
        final HashSet<TypedConstant> usedByLocal
                = new HashSet<TypedConstant>();
        final ArrayList<SsaInsn>[] useList = ssaMeth.getUseListCopy();
        for (int i = 0; i < origRegCount; i++) {
            SsaInsn insn = ssaMeth.getDefinitionForRegister(i);
            if (insn == null) {
                continue;
            }
            final RegisterSpec origReg = insn.getResult();
            TypeBearer typeBearer = insn.getResult().getTypeBearer();
            if (!typeBearer.isConstant()) continue;
            TypedConstant cst = (TypedConstant) typeBearer;
            final RegisterSpec newReg = newRegs.get(cst);
            if (newReg == null) {
                continue;
            }
            if (ssaMeth.isRegALocal(origReg)) {
                if (!COLLECT_ONE_LOCAL) {
                    continue;                    
                } else {
                    if (usedByLocal.contains(cst)) {
                        continue;
                    } else {
                        usedByLocal.add(cst);
                        fixLocalAssignment(origReg, newRegs.get(cst));
                    }
                }
            }
            RegisterMapper mapper = new RegisterMapper() {
                @Override
                public int getNewRegisterCount() {
                    return ssaMeth.getRegCount();
                }
                @Override
                public RegisterSpec map(RegisterSpec registerSpec) {
                    if (registerSpec.getReg() == origReg.getReg()) {
                        return newReg.withLocalItem(
                                registerSpec.getLocalItem());
                    }
                    return registerSpec;
                }
            };
            for (SsaInsn use : useList[origReg.getReg()]) {
                if (use.canThrow()
                        && use.getBlock().getSuccessors().cardinality() > 1) {
                    continue;
                }
                use.mapSourceRegisters(mapper);
            }
        }
    }
}
