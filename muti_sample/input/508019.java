public final class PhiInsn extends SsaInsn {
    private final int ropResultReg;
    private final ArrayList<Operand> operands = new ArrayList<Operand>();
    private RegisterSpecList sources;
    public PhiInsn(RegisterSpec resultReg, SsaBasicBlock block) {
        super(resultReg, block);
        ropResultReg = resultReg.getReg();
    }
    public PhiInsn(final int resultReg, final SsaBasicBlock block) {
        super(RegisterSpec.make(resultReg, Type.VOID), block);
        ropResultReg = resultReg;
    }
    public PhiInsn clone() {
        throw new UnsupportedOperationException("can't clone phi");
    }
    public void updateSourcesToDefinitions(SsaMethod ssaMeth) {
        for (Operand o : operands) {
            RegisterSpec def 
                = ssaMeth.getDefinitionForRegister(
                    o.regSpec.getReg()).getResult();
            o.regSpec = o.regSpec.withType(def.getType());
        }
        sources = null;
    }
    public void changeResultType(TypeBearer type, LocalItem local) {
        setResult(RegisterSpec.makeLocalOptional(
                          getResult().getReg(), type, local));
    }
    public int getRopResultReg() {
        return ropResultReg;
    }
    public void addPhiOperand(RegisterSpec registerSpec,
            SsaBasicBlock predBlock) {
        operands.add(new Operand(registerSpec, predBlock.getIndex(),
                predBlock.getRopLabel()));
        sources = null;
    }
    public int predBlockIndexForSourcesIndex(int sourcesIndex) {
        return operands.get(sourcesIndex).blockIndex;
    }
    @Override
    public Rop getOpcode() {
        return null;
    }
    @Override
    public Insn getOriginalRopInsn() {
        return null;
    }
    @Override
    public boolean canThrow() {
        return false;
    }
    public RegisterSpecList getSources() {
        if (sources != null) {
            return sources;
        }
        if (operands.size() == 0) {
            return RegisterSpecList.EMPTY;
        }
        int szSources = operands.size();
        sources = new RegisterSpecList(szSources);
        for (int i = 0; i < szSources; i++) {
            Operand o = operands.get(i);
            sources.set(i, o.regSpec);
        }
        sources.setImmutable();
        return sources;
    }
    @Override
    public boolean isRegASource(int reg) {
        for (Operand o : operands) {
            if (o.regSpec.getReg() == reg) {
                return true;
            }
        }
        return false;
    }
    public boolean areAllOperandsEqual() {
        if (operands.size() == 0 ) {
            return true;
        }
        int firstReg = operands.get(0).regSpec.getReg();
        for (Operand o : operands) {
            if (firstReg != o.regSpec.getReg()) {
                return false;
            }
        }
        return true;
    }
    @Override
    public final void mapSourceRegisters(RegisterMapper mapper) {
        for (Operand o : operands) {
            RegisterSpec old = o.regSpec;
            o.regSpec = mapper.map(old);
            if (old != o.regSpec) {
                getBlock().getParent().onSourceChanged(this, old, o.regSpec);
            }
        }
        sources = null;
    }
    @Override
    public Insn toRopInsn() {
        throw new IllegalArgumentException(
                "Cannot convert phi insns to rop form");
    }
    public List<SsaBasicBlock> predBlocksForReg(int reg, SsaMethod ssaMeth) {
        ArrayList<SsaBasicBlock> ret = new ArrayList<SsaBasicBlock>();
        for (Operand o : operands) {
            if (o.regSpec.getReg() == reg) {
                ret.add(ssaMeth.getBlocks().get(o.blockIndex));
            }
        }
        return ret;
    }
    @Override
    public boolean isPhiOrMove() {
        return true;    
    }
    @Override
    public boolean hasSideEffect() {
        return Optimizer.getPreserveLocals() && getLocalAssignment() != null;
    }
    @Override
    public void accept(SsaInsn.Visitor v) {
        v.visitPhiInsn(this);
    }
    public String toHuman() {
        return toHumanWithInline(null);
    }
    protected final String toHumanWithInline(String extra) {
        StringBuffer sb = new StringBuffer(80);
        sb.append(SourcePosition.NO_INFO);
        sb.append(": phi");       
        if (extra != null) {
            sb.append("(");
            sb.append(extra);
            sb.append(")");
        }
        RegisterSpec result = getResult();
        if (result == null) {
            sb.append(" .");
        } else {
            sb.append(" ");
            sb.append(result.toHuman());
        }
        sb.append(" <-");
        int sz = getSources().size();
        if (sz == 0) {
            sb.append(" .");
        } else {
            for (int i = 0; i < sz; i++) {
                sb.append(" ");
                sb.append(sources.get(i).toHuman()
                        + "[b="
                        + Hex.u2(operands.get(i).ropLabel)  + "]");
            }
        }
        return sb.toString();
    }
    private static class Operand {
        public RegisterSpec regSpec;
        public final int blockIndex;
        public final int ropLabel;       
        public Operand(RegisterSpec regSpec, int blockIndex, int ropLabel) {
            this.regSpec = regSpec;
            this.blockIndex = blockIndex;
            this.ropLabel = ropLabel;
        }
    }
    public static interface Visitor {
        public void visitPhiInsn(PhiInsn insn);
    }
}
