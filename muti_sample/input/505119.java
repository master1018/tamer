public class FirstFitLocalCombiningAllocator extends RegisterAllocator {
    private static final boolean DEBUG = false;
    private final Map<LocalItem, ArrayList<RegisterSpec>> localVariables;
    private final ArrayList<NormalSsaInsn> moveResultPseudoInsns;
    private final ArrayList<NormalSsaInsn> invokeRangeInsns;
    private final BitSet ssaRegsMapped;
    private final InterferenceRegisterMapper mapper;
    private final int paramRangeEnd;
    private final BitSet reservedRopRegs;
    private final BitSet usedRopRegs;
    private final boolean minimizeRegisters;
    public FirstFitLocalCombiningAllocator(
            SsaMethod ssaMeth, InterferenceGraph interference,
            boolean minimizeRegisters) {
        super(ssaMeth, interference);
        ssaRegsMapped = new BitSet(ssaMeth.getRegCount());
        mapper = new InterferenceRegisterMapper(
                interference, ssaMeth.getRegCount());
        this.minimizeRegisters = minimizeRegisters;
        paramRangeEnd = ssaMeth.getParamWidth();
        reservedRopRegs = new BitSet(paramRangeEnd * 2);
        reservedRopRegs.set(0, paramRangeEnd);
        usedRopRegs = new BitSet(paramRangeEnd * 2);
        localVariables = new TreeMap<LocalItem, ArrayList<RegisterSpec>>();
        moveResultPseudoInsns = new ArrayList<NormalSsaInsn>();
        invokeRangeInsns = new ArrayList<NormalSsaInsn>();
    }
    @Override
    public boolean wantsParamsMovedHigh() {
        return true;
    }
    @Override
    public RegisterMapper allocateRegisters() {
        analyzeInstructions();
        if (DEBUG) {
            printLocalVars();
        }
        if (DEBUG) System.out.println("--->Mapping local-associated params");
        handleLocalAssociatedParams();
        if (DEBUG) System.out.println("--->Mapping other params");
        handleUnassociatedParameters();
        if (DEBUG) System.out.println("--->Mapping invoke-range");
        handleInvokeRangeInsns();
        if (DEBUG) {
            System.out.println("--->Mapping local-associated non-params");
        }
        handleLocalAssociatedOther();
        if (DEBUG) System.out.println("--->Mapping check-cast results");
        handleCheckCastResults();
        if (DEBUG) System.out.println("--->Mapping others");
        handleNormalUnassociated();
        return mapper;
    }
    private void printLocalVars() {
        System.out.println("Printing local vars");
        for (Map.Entry<LocalItem, ArrayList<RegisterSpec>> e :
                localVariables.entrySet()) {
            StringBuilder regs = new StringBuilder();
            regs.append('{');
            regs.append(' ');
            for (RegisterSpec reg : e.getValue()) {
                regs.append('v');
                regs.append(reg.getReg());
                regs.append(' ');
            }
            regs.append('}');
            System.out.printf("Local: %s Registers: %s\n", e.getKey(), regs);
        }
    }
    private void handleLocalAssociatedParams() {
        for (ArrayList<RegisterSpec> ssaRegs : localVariables.values()) {
            int sz = ssaRegs.size();
            int paramIndex = -1;
            int paramCategory = 0;
            for (int i = 0; i < sz; i++) {
                RegisterSpec ssaSpec = ssaRegs.get(i);
                int ssaReg = ssaSpec.getReg();
                paramIndex = getParameterIndexForReg(ssaReg);
                if (paramIndex >= 0) {
                    paramCategory = ssaSpec.getCategory();
                    addMapping(ssaSpec, paramIndex);
                    break;
                }
            }
            if (paramIndex < 0) {
                continue;
            }
            tryMapRegs(ssaRegs, paramIndex, paramCategory, true);
        }
    }
    private int getParameterIndexForReg(int ssaReg) {
        SsaInsn defInsn = ssaMeth.getDefinitionForRegister(ssaReg);
        if (defInsn == null) {
            return -1;
        }
        Rop opcode = defInsn.getOpcode();
        if (opcode != null && opcode.getOpcode() == RegOps.MOVE_PARAM) {
            CstInsn origInsn = (CstInsn) defInsn.getOriginalRopInsn();
            return  ((CstInteger) origInsn.getConstant()).getValue();
        }
        return -1;
    }
    private void handleLocalAssociatedOther() {
        for (ArrayList<RegisterSpec> specs : localVariables.values()) {
            int ropReg = 0;
            boolean done;
            do {
                int maxCategory = 1;
                int sz = specs.size();
                for (int i = 0; i < sz; i++) {
                    RegisterSpec ssaSpec = specs.get(i);
                    int category = ssaSpec.getCategory();
                    if (!ssaRegsMapped.get(ssaSpec.getReg())
                            && category > maxCategory) {
                        maxCategory = category;
                    }
                }
                ropReg = findRopRegForLocal(ropReg, maxCategory);
                done = tryMapRegs(specs, ropReg, maxCategory, true);
                ropReg++;
            } while (!done);
        }
    }
    private boolean tryMapRegs(
            ArrayList<RegisterSpec> specs, int ropReg,
            int maxAllowedCategory, boolean markReserved) {
        boolean remaining = false;
        for (RegisterSpec spec : specs) {
            if (ssaRegsMapped.get(spec.getReg())) {
                continue;
            }
            boolean succeeded;
            succeeded = tryMapReg(spec, ropReg, maxAllowedCategory);
            remaining = !succeeded || remaining;
            if (succeeded && markReserved) {
                markReserved(ropReg, spec.getCategory());
            }
        }
        return !remaining;
    }
    private boolean tryMapReg(RegisterSpec ssaSpec, int ropReg,
            int maxAllowedCategory) {
        if (ssaSpec.getCategory() <= maxAllowedCategory
                && !ssaRegsMapped.get(ssaSpec.getReg())
                && canMapReg(ssaSpec, ropReg)) {
            addMapping(ssaSpec, ropReg);
            return true;
        }
        return false;
    }
    private void markReserved(int ropReg, int category) {
        reservedRopRegs.set(ropReg, ropReg + category, true);
    }
    private boolean rangeContainsReserved(int ropRangeStart, int width) {
        for (int i = ropRangeStart; i < (ropRangeStart + width); i++) {
            if (reservedRopRegs.get(i)) {
                return true;
            }
        }
        return false;
    }
    private boolean isThisPointerReg(int startReg) {
        return startReg == 0 && !ssaMeth.isStatic();
    }
    private int findNextUnreservedRopReg(int startReg, int width) {
        if (minimizeRegisters && !isThisPointerReg(startReg)) {
            return startReg;
        }
        int reg;
        reg = reservedRopRegs.nextClearBit(startReg);
        while (true) {
            int i = 1;
            while (i < width && !reservedRopRegs.get(reg + i)) {
                i++;
            }
            if (i == width) {
                return reg;
            }
            reg = reservedRopRegs.nextClearBit(reg + i);
        }
    }
    private int findRopRegForLocal(int startReg, int width) {
        if (minimizeRegisters && !isThisPointerReg(startReg)) {
            return startReg;
        }
        int reg;
        reg = usedRopRegs.nextClearBit(startReg);
        while (true) {
            int i = 1;
            while (i < width && !usedRopRegs.get(reg + i)) {
                i++;
            }
            if (i == width) {
                return reg;
            }
            reg = usedRopRegs.nextClearBit(reg + i);
        }
    }
    private void handleUnassociatedParameters() {
        int szSsaRegs = ssaMeth.getRegCount();
        for (int ssaReg = 0; ssaReg < szSsaRegs; ssaReg++) {
            if (ssaRegsMapped.get(ssaReg)) {
                continue;
            }
            int paramIndex = getParameterIndexForReg(ssaReg);
            RegisterSpec ssaSpec = getDefinitionSpecForSsaReg(ssaReg);
            if (paramIndex >= 0) {
                addMapping(ssaSpec, paramIndex);
            }
        }
    }
    private void handleInvokeRangeInsns() {
        for (NormalSsaInsn insn : invokeRangeInsns) {
            adjustAndMapSourceRangeRange(insn);
        }
    }
    private void handleCheckCastResults() {
        for (NormalSsaInsn insn : moveResultPseudoInsns) {
            RegisterSpec moveRegSpec = insn.getResult();
            int moveReg = moveRegSpec.getReg();
            BitSet predBlocks = insn.getBlock().getPredecessors();
            if (predBlocks.cardinality() != 1) {
                continue;
            }
            SsaBasicBlock predBlock =
                    ssaMeth.getBlocks().get(predBlocks.nextSetBit(0));
            ArrayList<SsaInsn> insnList = predBlock.getInsns();
            SsaInsn checkCastInsn = insnList.get(insnList.size() - 1);
            if (checkCastInsn.getOpcode().getOpcode() != RegOps.CHECK_CAST) {
                continue;
            }
            RegisterSpec checkRegSpec = checkCastInsn.getSources().get(0);
            int checkReg = checkRegSpec.getReg();
            int ropReg = 0;
            if (ssaRegsMapped.get(moveReg)) {
                ropReg = mapper.oldToNew(moveReg);
            } else if (ssaRegsMapped.get(checkReg)) {
                ropReg = mapper.oldToNew(checkReg);
            }
            ArrayList<RegisterSpec> ssaRegs = new ArrayList<RegisterSpec>(2);
            ssaRegs.add(moveRegSpec);
            ssaRegs.add(checkRegSpec);
            int category = checkRegSpec.getCategory();
            while (!tryMapRegs(ssaRegs, ropReg, category, false)) {
                ropReg = findNextUnreservedRopReg(ropReg + 1, category);
            }
        }
    }
    private void handleNormalUnassociated() {
        int szSsaRegs = ssaMeth.getRegCount();
        for (int ssaReg = 0; ssaReg < szSsaRegs; ssaReg++) {
            if (ssaRegsMapped.get(ssaReg)) {
                continue;
            }
            RegisterSpec ssaSpec = getDefinitionSpecForSsaReg(ssaReg);
            if (ssaSpec == null) continue;
            int category = ssaSpec.getCategory();
            int ropReg = findNextUnreservedRopReg(0, category);
            while (!canMapReg(ssaSpec, ropReg)) {
                ropReg = findNextUnreservedRopReg(ropReg + 1, category);
            }
            addMapping(ssaSpec, ropReg);
        }
    }
    private boolean canMapReg(RegisterSpec ssaSpec, int ropReg) {
        int category = ssaSpec.getCategory();
        return !(spansParamRange(ropReg, category)
                || mapper.interferes(ssaSpec, ropReg));
    }
    private boolean spansParamRange(int ssaReg, int category) {
        return ((ssaReg < paramRangeEnd)
                && ((ssaReg + category) > paramRangeEnd));
    }
    private void analyzeInstructions() {
        ssaMeth.forEachInsn(new SsaInsn.Visitor() {
            public void visitMoveInsn(NormalSsaInsn insn) {
                processInsn(insn);
            }
            public void visitPhiInsn(PhiInsn insn) {
                processInsn(insn);
            }
            public void visitNonMoveInsn(NormalSsaInsn insn) {
                processInsn(insn);
            }
            private void processInsn(SsaInsn insn) {
                RegisterSpec assignment;
                assignment = insn.getLocalAssignment();
                if (assignment != null) {
                    LocalItem local = assignment.getLocalItem();
                    ArrayList<RegisterSpec> regList
                        = localVariables.get(local);
                    if (regList == null) {
                        regList = new ArrayList<RegisterSpec>();
                        localVariables.put(local, regList);
                    }
                    regList.add(assignment);
                }
                if (insn instanceof NormalSsaInsn) {
                    if (insn.getOpcode().getOpcode() ==
                            RegOps.MOVE_RESULT_PSEUDO) {
                        moveResultPseudoInsns.add((NormalSsaInsn) insn);
                    } else if (Optimizer.getAdvice().requiresSourcesInOrder(
                            insn.getOriginalRopInsn().getOpcode(),
                            insn.getSources())) {
                        invokeRangeInsns.add((NormalSsaInsn) insn);
                    }
                }
            }
        });
    }
    private void addMapping(RegisterSpec ssaSpec, int ropReg) {
        int ssaReg = ssaSpec.getReg();
        if (ssaRegsMapped.get(ssaReg) || !canMapReg(ssaSpec, ropReg)) {
            throw new RuntimeException(
                    "attempt to add invalid register mapping");
        }
        if (DEBUG) {
            System.out.printf("Add mapping s%d -> v%d c:%d\n",
                    ssaSpec.getReg(), ropReg, ssaSpec.getCategory());
        }
        int category = ssaSpec.getCategory();
        mapper.addMapping(ssaSpec.getReg(), ropReg, category);
        ssaRegsMapped.set(ssaReg);
        usedRopRegs.set(ropReg, ropReg + category);
    }
    private void adjustAndMapSourceRangeRange(NormalSsaInsn insn) {
        int newRegStart = findRangeAndAdjust(insn);
        RegisterSpecList sources = insn.getSources();
        int szSources = sources.size();
        int nextRopReg = newRegStart;
        for (int i = 0; i < szSources; i++) {
            RegisterSpec source = sources.get(i);
            int sourceReg = source.getReg();
            int category = source.getCategory();
            int curRopReg = nextRopReg;
            nextRopReg += category;
            if (ssaRegsMapped.get(sourceReg)) {
                continue;
            }
            LocalItem localItem = getLocalItemForReg(sourceReg);
            addMapping(source, curRopReg);
            if (localItem != null) {
                markReserved(curRopReg, category);
                ArrayList<RegisterSpec> similarRegisters
                        = localVariables.get(localItem);
                int szSimilar = similarRegisters.size();
                for (int j = 0; j < szSimilar; j++) {
                    RegisterSpec similarSpec = similarRegisters.get(j);
                    int similarReg = similarSpec.getReg();
                    if (-1 != sources.indexOfRegister(similarReg)) {
                        continue;
                    }
                    tryMapReg(similarSpec, curRopReg, category);
                }
            }
        }
    }
    private int findRangeAndAdjust(NormalSsaInsn insn) {
        RegisterSpecList sources = insn.getSources();
        int szSources = sources.size();
        int categoriesForIndex[] = new int[szSources];
        int rangeLength = 0;
        for (int i = 0; i < szSources; i++) {
            int category = sources.get(i).getCategory();
            categoriesForIndex[i] = category;
            rangeLength += categoriesForIndex[i];
        }
        int maxScore = Integer.MIN_VALUE;
        int resultRangeStart = -1;
        BitSet resultMovesRequired = null;
        int rangeStartOffset = 0;
        for (int i = 0; i < szSources; i++) {
            int ssaCenterReg = sources.get(i).getReg();
            if (i != 0) {
                rangeStartOffset -= categoriesForIndex[i - 1];
            }
            if (!ssaRegsMapped.get(ssaCenterReg)) {
                continue;
            }
            int rangeStart = mapper.oldToNew(ssaCenterReg) + rangeStartOffset;
            if (rangeStart < 0 || spansParamRange(rangeStart, rangeLength)) {
                continue;
            }
            BitSet curMovesRequired = new BitSet(szSources);
            int fitWidth
                    = fitPlanForRange(rangeStart, insn, categoriesForIndex,
                    curMovesRequired);
            if (fitWidth < 0) {
                continue;
            }
            int score = fitWidth - curMovesRequired.cardinality();
            if (score > maxScore) {
                maxScore = score;
                resultRangeStart = rangeStart;
                resultMovesRequired = curMovesRequired;
            }
            if (fitWidth == rangeLength) {
                break;
            }
        }
        if (resultRangeStart == -1) {
            resultMovesRequired = new BitSet(szSources);
            resultRangeStart = findAnyFittingRange(insn, rangeLength,
                    categoriesForIndex, resultMovesRequired);
        }
        for (int i = resultMovesRequired.nextSetBit(0); i >= 0;
             i = resultMovesRequired.nextSetBit(i+1)) {
            insn.changeOneSource(i, insertMoveBefore(insn, sources.get(i)));
        }
        return resultRangeStart;
    }
    private int findAnyFittingRange(NormalSsaInsn insn, int rangeLength,
            int[] categoriesForIndex, BitSet outMovesRequired) {
        int rangeStart = 0;
        while (true) {
            rangeStart = findNextUnreservedRopReg(rangeStart, rangeLength);
            int fitWidth
                    = fitPlanForRange(rangeStart, insn,
                    categoriesForIndex, outMovesRequired);
            if (fitWidth >= 0) {
                break;
            }
            rangeStart++;
            outMovesRequired.clear();
        }
        return rangeStart;
    }
    private int fitPlanForRange(int ropReg, NormalSsaInsn insn,
            int[] categoriesForIndex, BitSet outMovesRequired) {
        RegisterSpecList sources = insn.getSources();
        int szSources = sources.size();
        int fitWidth = 0;
        IntSet liveOut = insn.getBlock().getLiveOutRegs();
        RegisterSpecList liveOutSpecs = ssaSetToSpecs(liveOut);
        BitSet seen = new BitSet(ssaMeth.getRegCount());
        for (int i = 0; i < szSources ; i++) {
            RegisterSpec ssaSpec = sources.get(i);
            int ssaReg = ssaSpec.getReg();
            int category = categoriesForIndex[i];
            if (i != 0) {
                ropReg += categoriesForIndex[i-1];
            }
            if (ssaRegsMapped.get(ssaReg)
                    && mapper.oldToNew(ssaReg) == ropReg) {
                fitWidth += category;
            } else if (rangeContainsReserved(ropReg, category)) {
                fitWidth = -1;
                break;
            } else if (!ssaRegsMapped.get(ssaReg)
                    && canMapReg(ssaSpec, ropReg)
                    && !seen.get(ssaReg)) {
                fitWidth += category;
            } else if (!mapper.areAnyPinned(liveOutSpecs, ropReg, category)
                    && !mapper.areAnyPinned(sources, ropReg, category)) {
                outMovesRequired.set(i);
            } else {
                fitWidth = -1;
                break;
            }
            seen.set(ssaReg);
        }
        return fitWidth;
    }
    RegisterSpecList ssaSetToSpecs(IntSet ssaSet) {
        RegisterSpecList result = new RegisterSpecList(ssaSet.elements());
        IntIterator iter = ssaSet.iterator();
        int i = 0;
        while (iter.hasNext()) {
            result.set(i++, getDefinitionSpecForSsaReg(iter.next()));
        }
        return result;
    }
    private LocalItem getLocalItemForReg(int ssaReg) {
        for (Map.Entry<LocalItem, ArrayList<RegisterSpec>> entry :
                 localVariables.entrySet()) {
            for (RegisterSpec spec : entry.getValue()) {
                if (spec.getReg() == ssaReg) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
}
