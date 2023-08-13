public final class CstInsn extends FixedSizeInsn {
    private final Constant constant;
    private int index;
    private int classIndex;
    public CstInsn(Dop opcode, SourcePosition position,
                   RegisterSpecList registers, Constant constant) {
        super(opcode, position, registers);
        if (constant == null) {
            throw new NullPointerException("constant == null");
        }
        this.constant = constant;
        this.index = -1;
        this.classIndex = -1;
    }
    @Override
    public DalvInsn withOpcode(Dop opcode) {
        CstInsn result = 
            new CstInsn(opcode, getPosition(), getRegisters(), constant);
        if (index >= 0) {
            result.setIndex(index);
        }
        if (classIndex >= 0) {
            result.setClassIndex(classIndex);
        }
        return result;
    }
    @Override
    public DalvInsn withRegisters(RegisterSpecList registers) {
        CstInsn result =
            new CstInsn(getOpcode(), getPosition(), registers, constant);
        if (index >= 0) {
            result.setIndex(index);
        }
        if (classIndex >= 0) {
            result.setClassIndex(classIndex);
        }
        return result;
    }
    public Constant getConstant() {
        return constant;
    }
    public int getIndex() {
        if (index < 0) {
            throw new RuntimeException("index not yet set for " + constant);
        }
        return index;
    }
    public boolean hasIndex() {
        return (index >= 0);
    }
    public void setIndex(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index < 0");
        }
        if (this.index >= 0) {
            throw new RuntimeException("index already set");
        }
        this.index = index;
    }
    public int getClassIndex() {
        if (classIndex < 0) {
            throw new RuntimeException("class index not yet set");
        }
        return classIndex;
    }
    public boolean hasClassIndex() {
        return (classIndex >= 0);
    }
    public void setClassIndex(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index < 0");
        }
        if (this.classIndex >= 0) {
            throw new RuntimeException("class index already set");
        }
        this.classIndex = index;
    }
    @Override
    protected String argString() {
        return constant.toHuman();
    }
}
