public class SCCP {
    private static final int TOP = 0;
    private static final int CONSTANT = 1;
    private static final int VARYING = 2;
    private SsaMethod ssaMeth;
    private int regCount;
    private int[] latticeValues;
    private Constant[] latticeConstants;
    private ArrayList<SsaBasicBlock> cfgWorklist;
    private BitSet executableBlocks;
    private ArrayList<SsaInsn> ssaWorklist;
    private ArrayList<SsaInsn> varyingWorklist;
    private SCCP(SsaMethod ssaMeth) {
        this.ssaMeth = ssaMeth;
        this.regCount = ssaMeth.getRegCount();
        this.latticeValues = new int[this.regCount];
        this.latticeConstants = new Constant[this.regCount];
        this.cfgWorklist = new ArrayList<SsaBasicBlock>();
        this.executableBlocks = new BitSet(ssaMeth.getBlocks().size());
        this.ssaWorklist = new ArrayList<SsaInsn>();
        this.varyingWorklist = new ArrayList<SsaInsn>();
        for (int i = 0; i < this.regCount; i++) {
            latticeValues[i] = TOP;
            latticeConstants[i] = null;
        }
    }
    public static void process (SsaMethod ssaMethod) {
        new SCCP(ssaMethod).run();
    }
    private void addBlockToWorklist(SsaBasicBlock ssaBlock) {
        if (!executableBlocks.get(ssaBlock.getIndex())) {
            cfgWorklist.add(ssaBlock);
            executableBlocks.set(ssaBlock.getIndex());
        }
    }
    private void addUsersToWorklist(int reg, int latticeValue) {
        if (latticeValue == VARYING) {
            for (SsaInsn insn : ssaMeth.getUseListForRegister(reg)) {
                varyingWorklist.add(insn);
            }
        } else {
            for (SsaInsn insn : ssaMeth.getUseListForRegister(reg)) {
                ssaWorklist.add(insn);
            }
        }
    }
    private boolean setLatticeValueTo(int reg, int value, Constant cst) {
        if (value != CONSTANT) {
            if (latticeValues[reg] != value) {
                latticeValues[reg] = value;
                return true;
            }
            return false;
        } else {
            if (latticeValues[reg] != value
                    || !latticeConstants[reg].equals(cst)) {
                latticeValues[reg] = value;
                latticeConstants[reg] = cst;
                return true;
            }
            return false;
        }
    }
    private void simulatePhi(PhiInsn insn) {
        int phiResultReg = insn.getResult().getReg();
        if (latticeValues[phiResultReg] == VARYING) {
            return;
        }
        RegisterSpecList sources = insn.getSources();
        int phiResultValue = TOP;
        Constant phiConstant = null;
        int sourceSize = sources.size();
        for (int i = 0; i < sourceSize; i++) {
            int predBlockIndex = insn.predBlockIndexForSourcesIndex(i);
            int sourceReg = sources.get(i).getReg();
            int sourceRegValue = latticeValues[sourceReg];
            if (!executableBlocks.get(predBlockIndex)
                    || sourceRegValue == TOP) {
                continue;
            }
            if (sourceRegValue == CONSTANT) {
                if (phiConstant == null) {
                    phiConstant = latticeConstants[sourceReg];
                    phiResultValue = CONSTANT;
                 } else if (!latticeConstants[sourceReg].equals(phiConstant)){
                    phiResultValue = VARYING;
                    break;
                }
            } else if (sourceRegValue == VARYING) {
                phiResultValue = VARYING;
                break;
            }
        }
        if (setLatticeValueTo(phiResultReg, phiResultValue, phiConstant)) {
            addUsersToWorklist(phiResultReg, phiResultValue);
        }
    }
    private void simulateBlock(SsaBasicBlock block) {
        for (SsaInsn insn : block.getInsns()) {
            if (insn instanceof PhiInsn) {
                simulatePhi((PhiInsn) insn);
            } else {
                simulateStmt(insn);
            }
        }
    }
    private static String latticeValName(int latticeVal) {
        switch (latticeVal) {
            case TOP: return "TOP";
            case CONSTANT: return "CONSTANT";
            case VARYING: return "VARYING";
            default: return "UNKNOWN";
        }
    }
    private Insn simplifyJump (Insn insn) {
        return insn;
    }
    private Constant simulateMath(SsaInsn insn) {
        Insn ropInsn = insn.getOriginalRopInsn();
        int opcode = insn.getOpcode().getOpcode();
        RegisterSpecList sources = insn.getSources();
        int regA = sources.get(0).getReg();
        Constant cA;
        Constant cB;
        if (latticeValues[regA] != CONSTANT) {
            cA = null;
        } else {
            cA = latticeConstants[regA];
        }
        if (sources.size() == 1) {
            CstInsn cstInsn = (CstInsn) ropInsn;
            cB = cstInsn.getConstant();
        } else { 
            int regB = sources.get(1).getReg();
            if (latticeValues[regB] != CONSTANT) {
                cB = null;
            } else {
                cB = latticeConstants[regB];
            }
        }
        if (cA == null || cB == null) {
            return null;
        }
        switch (insn.getResult().getBasicType()) {
            case Type.BT_INT:
                int vR;
                boolean skip=false;
                int vA = ((CstInteger) cA).getValue();
                int vB = ((CstInteger) cB).getValue();
                switch (opcode) {
                    case RegOps.ADD:
                        vR = vA + vB;
                        break;
                    case RegOps.SUB:
                        vR = vA - vB;
                        break;
                    case RegOps.MUL:
                        vR = vA * vB;
                        break;
                    case RegOps.DIV:
                        if (vB == 0) {
                            skip = true;
                            vR = 0; 
                        } else {
                            vR = vA / vB;
                        }
                        break;
                    case RegOps.AND:
                        vR = vA & vB;
                        break;
                    case RegOps.OR:
                        vR = vA | vB;
                        break;
                    case RegOps.XOR:
                        vR = vA ^ vB;
                        break;
                    case RegOps.SHL:
                        vR = vA << vB;
                        break;
                    case RegOps.SHR:
                        vR = vA >> vB;
                        break;
                    case RegOps.USHR:
                        vR = vA >>> vB;
                        break;
                    case RegOps.REM:
                        vR = vA % vB;
                        break;
                    default:
                        throw new RuntimeException("Unexpected op");
                }
                return skip ? null : CstInteger.make(vR);
            default:
                return null;
        }
    }
    private void simulateStmt(SsaInsn insn) {
        Insn ropInsn = insn.getOriginalRopInsn();
        if (ropInsn.getOpcode().getBranchingness() != Rop.BRANCH_NONE
                || ropInsn.getOpcode().isCallLike()) {
            ropInsn = simplifyJump (ropInsn);
            SsaBasicBlock block = insn.getBlock();
            int successorSize = block.getSuccessorList().size();
            for (int i = 0; i < successorSize; i++) {
                int successor = block.getSuccessorList().get(i);
                addBlockToWorklist(ssaMeth.getBlocks().get(successor));
            }
        }
        if (insn.getResult() == null) {
            return;
        }
        int resultReg = insn.getResult().getReg();
        int resultValue = VARYING;
        Constant resultConstant = null;
        int opcode = insn.getOpcode().getOpcode();
        switch (opcode) {
            case RegOps.CONST: {
                CstInsn cstInsn = (CstInsn)ropInsn;
                resultValue = CONSTANT;
                resultConstant = cstInsn.getConstant();
                break;
            }
            case RegOps.MOVE: {
                if (insn.getSources().size() == 1) {
                    int sourceReg = insn.getSources().get(0).getReg();
                    resultValue = latticeValues[sourceReg];
                    resultConstant = latticeConstants[sourceReg];
                }
                break;
            }
            case RegOps.ADD:
            case RegOps.SUB:
            case RegOps.MUL:
            case RegOps.DIV:
            case RegOps.AND:
            case RegOps.OR:
            case RegOps.XOR:
            case RegOps.SHL:
            case RegOps.SHR:
            case RegOps.USHR:
            case RegOps.REM:
                resultConstant = simulateMath(insn);
                if (resultConstant == null) {
                    resultValue = VARYING;
                } else {
                    resultValue = CONSTANT;
                }
            break;
            default: {}
        }
        if (setLatticeValueTo(resultReg, resultValue, resultConstant)) {
            addUsersToWorklist(resultReg, resultValue);
        }
    }
    private void run() {
        SsaBasicBlock firstBlock = ssaMeth.getEntryBlock();
        addBlockToWorklist(firstBlock);
        while (!cfgWorklist.isEmpty()
                || !ssaWorklist.isEmpty()
                || !varyingWorklist.isEmpty()) {
            while (!cfgWorklist.isEmpty()) {
                int listSize = cfgWorklist.size() - 1;
                SsaBasicBlock block = cfgWorklist.remove(listSize);
                simulateBlock(block);
            }
            while (!varyingWorklist.isEmpty()) {
                int listSize = varyingWorklist.size() - 1;
                SsaInsn insn = varyingWorklist.remove(listSize);
                if (!executableBlocks.get(insn.getBlock().getIndex())) {
                    continue;
                }
                if (insn instanceof PhiInsn) {
                    simulatePhi((PhiInsn)insn);
                } else {
                    simulateStmt(insn);
                }
            }
            while (!ssaWorklist.isEmpty()) {
                int listSize = ssaWorklist.size() - 1;
                SsaInsn insn = ssaWorklist.remove(listSize);
                if (!executableBlocks.get(insn.getBlock().getIndex())) {
                    continue;
                }
                if (insn instanceof PhiInsn) {
                    simulatePhi((PhiInsn)insn);
                } else {
                    simulateStmt(insn);
                }
            }
        }
        replaceConstants();
    }
    private void replaceConstants() {
        for (int reg = 0; reg < regCount; reg++) {            
            if (latticeValues[reg] != CONSTANT) {
                continue;
            }
            if (!(latticeConstants[reg] instanceof TypedConstant)) {
                continue;
            }
            SsaInsn defn = ssaMeth.getDefinitionForRegister(reg);
            TypeBearer typeBearer = defn.getResult().getTypeBearer();
            if (typeBearer.isConstant()) {
                continue;
            }
            for (SsaInsn insn : ssaMeth.getUseListForRegister(reg)) {
                if (insn.isPhiOrMove()) {
                    continue;
                }
                NormalSsaInsn nInsn = (NormalSsaInsn) insn;
                RegisterSpecList sources = insn.getSources();
                int index = sources.indexOfRegister(reg);
                RegisterSpec spec = sources.get(index);
                RegisterSpec newSpec
                        = spec.withType((TypedConstant)latticeConstants[reg]);
                nInsn.changeOneSource(index, newSpec);
            }
        }
    }
}
