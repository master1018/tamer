public abstract class DalvInsn {
    private int address;
    private final Dop opcode;
    private final SourcePosition position;
    private final RegisterSpecList registers;
    public static SimpleInsn makeMove(SourcePosition position,
            RegisterSpec dest, RegisterSpec src) {
        boolean category1 = dest.getCategory() == 1;
        boolean reference = dest.getType().isReference();
        int destReg = dest.getReg();
        int srcReg = src.getReg();
        Dop opcode;
        if ((srcReg | destReg) < 16) {
            opcode = reference ? Dops.MOVE_OBJECT :
                (category1 ? Dops.MOVE : Dops.MOVE_WIDE);
        } else if (destReg < 256) {
            opcode = reference ? Dops.MOVE_OBJECT_FROM16 :
                (category1 ? Dops.MOVE_FROM16 : Dops.MOVE_WIDE_FROM16);
        } else {
            opcode = reference ? Dops.MOVE_OBJECT_16 :
                (category1 ? Dops.MOVE_16 : Dops.MOVE_WIDE_16);
        }
        return new SimpleInsn(opcode, position,
                              RegisterSpecList.make(dest, src));
    }
    public DalvInsn(Dop opcode, SourcePosition position,
                    RegisterSpecList registers) {
        if (opcode == null) {
            throw new NullPointerException("opcode == null");
        }
        if (position == null) {
            throw new NullPointerException("position == null");
        }
        if (registers == null) {
            throw new NullPointerException("registers == null");
        }
        this.address = -1;
        this.opcode = opcode;
        this.position = position;
        this.registers = registers;
    }
    @Override
    public final String toString() {
        StringBuffer sb = new StringBuffer(100);
        sb.append(identifierString());
        sb.append(' ');
        sb.append(position);
        sb.append(": ");
        sb.append(opcode.getName());
        boolean needComma = false;
        if (registers.size() != 0) {
            sb.append(registers.toHuman(" ", ", ", null));
            needComma = true;
        }
        String extra = argString();
        if (extra != null) {
            if (needComma) {
                sb.append(',');
            }
            sb.append(' ');
            sb.append(extra);
        }
        return sb.toString();
    }
    public final boolean hasAddress() {
        return (address >= 0);
    }
    public final int getAddress() {
        if (address < 0) {
            throw new RuntimeException("address not yet known");
        }
        return address;
    }
    public final Dop getOpcode() {
        return opcode;
    }
    public final SourcePosition getPosition() {
        return position;
    }
    public final RegisterSpecList getRegisters() {
        return registers;
    }
    public final boolean hasResult() {
        return opcode.hasResult();
    }
    public final int getMinimumRegisterRequirement() {
        boolean hasResult = hasResult();
        int regSz = registers.size();
        int resultRequirement = hasResult ? registers.get(0).getCategory() : 0;
        int sourceRequirement = 0;
        for (int i = hasResult ? 1 : 0; i < regSz; i++) {
            sourceRequirement += registers.get(i).getCategory();
        }
        return Math.max(sourceRequirement, resultRequirement);
    }
    public DalvInsn hrPrefix() {
        RegisterSpecList regs = registers;
        int sz = regs.size();
        if (hasResult()) {
            if (sz == 1) {
                return null;
            }
            regs = regs.withoutFirst();
        } else if (sz == 0) {
            return null;
        }
        return new HighRegisterPrefix(position, regs);
    }
    public DalvInsn hrSuffix() {
        if (hasResult()) {
            RegisterSpec r = registers.get(0);
            return makeMove(position, r, r.withReg(0));
        } else {
            return null;
        }
    }
    public DalvInsn hrVersion() {
        RegisterSpecList regs = 
            registers.withSequentialRegisters(0, hasResult());
        return withRegisters(regs);
    }
    public final String identifierString() {
        if (address != -1) {
            return String.format("%04x", address);
        }
        return Hex.u4(System.identityHashCode(this));
    }
    public final String listingString(String prefix, int width,
            boolean noteIndices) {
        String insnPerSe = listingString0(noteIndices);
        if (insnPerSe == null) {
            return null;
        }
        String addr = prefix + identifierString() + ": ";
        int w1 = addr.length();
        int w2 = (width == 0) ? insnPerSe.length() : (width - w1);
        return TwoColumnOutput.toString(addr, w1, "", insnPerSe, w2);
    }
    public final void setAddress(int address) {
        if (address < 0) {
            throw new IllegalArgumentException("address < 0");
        }
        this.address = address;
    }
    public final int getNextAddress() {
        return getAddress() + codeSize();
    }
    public abstract int codeSize();
    public abstract void writeTo(AnnotatedOutput out);
    public abstract DalvInsn withOpcode(Dop opcode);
    public abstract DalvInsn withRegisterOffset(int delta);
    public abstract DalvInsn withRegisters(RegisterSpecList registers);
    protected abstract String argString();
    protected abstract String listingString0(boolean noteIndices);
}
