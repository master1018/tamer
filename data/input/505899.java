public abstract class InsnFormat {
    public final String listingString(DalvInsn insn, boolean noteIndices) {
        String op = insn.getOpcode().getName();
        String arg = insnArgString(insn);
        String comment = insnCommentString(insn, noteIndices);
        StringBuilder sb = new StringBuilder(100);
        sb.append(op);
        if (arg.length() != 0) {
            sb.append(' ');
            sb.append(arg);
        }
        if (comment.length() != 0) {
            sb.append(" 
            sb.append(comment);
        }
        return sb.toString();
    }
    public abstract String insnArgString(DalvInsn insn);
    public abstract String insnCommentString(DalvInsn insn,
            boolean noteIndices);
    public abstract int codeSize();
    public abstract boolean isCompatible(DalvInsn insn);
    public boolean branchFits(TargetInsn insn) {
        return false;
    }
    public abstract InsnFormat nextUp();
    public abstract void writeTo(AnnotatedOutput out, DalvInsn insn);
    protected static String regListString(RegisterSpecList list) {
        int sz = list.size();
        StringBuffer sb = new StringBuffer(sz * 5 + 2);
        sb.append('{');
        for (int i = 0; i < sz; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(list.get(i).regString());
        }
        sb.append('}');
        return sb.toString();
    }
    protected static String literalBitsString(CstLiteralBits value) {
        StringBuffer sb = new StringBuffer(100);
        sb.append('#');
        if (value instanceof CstKnownNull) {
            sb.append("null");
        } else {
            sb.append(value.typeName());
            sb.append(' ');
            sb.append(value.toHuman());
        }
        return sb.toString();
    }
    protected static String literalBitsComment(CstLiteralBits value,
            int width) {
        StringBuffer sb = new StringBuffer(20);
        sb.append("#");
        long bits;
        if (value instanceof CstLiteral64) {
            bits = ((CstLiteral64) value).getLongBits();
        } else {
            bits = value.getIntBits();
        }
        switch (width) {
            case 4:  sb.append(Hex.uNibble((int) bits)); break;
            case 8:  sb.append(Hex.u1((int) bits));      break;
            case 16: sb.append(Hex.u2((int) bits));      break;
            case 32: sb.append(Hex.u4((int) bits));      break;
            case 64: sb.append(Hex.u8(bits));            break;
            default: {
                throw new RuntimeException("shouldn't happen");
            }
        }
        return sb.toString();
    }
    protected static String branchString(DalvInsn insn) {
        TargetInsn ti = (TargetInsn) insn;
        int address = ti.getTargetAddress();
        return (address == (char) address) ? Hex.u2(address) : Hex.u4(address);
    }
    protected static String branchComment(DalvInsn insn) {
        TargetInsn ti = (TargetInsn) insn;
        int offset = ti.getTargetOffset();
        return (offset == (short) offset) ? Hex.s2(offset) : Hex.s4(offset);
    }
    protected static String cstString(DalvInsn insn) {
        CstInsn ci = (CstInsn) insn;
        Constant cst = ci.getConstant();
        return cst.toHuman();
    }
    protected static String cstComment(DalvInsn insn) {
        CstInsn ci = (CstInsn) insn;
        if (! ci.hasIndex()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(20);
        int index = ci.getIndex();
        sb.append(ci.getConstant().typeName());
        sb.append('@');
        if (index < 65536) {
            sb.append(Hex.u2(index));
        } else {
            sb.append(Hex.u4(index));
        }
        return sb.toString();
    }
    protected static boolean signedFitsInNibble(int value) {
        return (value >= -8) && (value <= 7);
    }
    protected static boolean unsignedFitsInNibble(int value) {
        return value == (value & 0xf);
    }
    protected static boolean signedFitsInByte(int value) {
        return (byte) value == value;
    }
    protected static boolean unsignedFitsInByte(int value) {
        return value == (value & 0xff);
    }
    protected static boolean signedFitsInShort(int value) {
        return (short) value == value;
    }
    protected static boolean unsignedFitsInShort(int value) {
        return value == (value & 0xffff);
    }
    protected static boolean signedFitsIn3Bytes(int value) {
        return value == ((value << 8) >> 8);
    }
    protected static int argIndex(DalvInsn insn) {
        int arg = ((CstInteger) ((CstInsn) insn).getConstant()).getValue();
        if (arg < 0) {
            throw new IllegalArgumentException("bogus insn");
        }
        return arg;
    }
    protected static short opcodeUnit(DalvInsn insn, int arg) {
        if ((arg & 0xff) != arg) {
            throw new IllegalArgumentException("arg out of range 0..255");
        }
        int opcode = insn.getOpcode().getOpcode();
        if ((opcode & 0xff) != opcode) {
            throw new IllegalArgumentException("opcode out of range 0..255");
        }
        return (short) (opcode | (arg << 8));
    }
    protected static short codeUnit(int low, int high) {
        if ((low & 0xff) != low) {
            throw new IllegalArgumentException("low out of range 0..255");
        }
        if ((high & 0xff) != high) {
            throw new IllegalArgumentException("high out of range 0..255");
        }
        return (short) (low | (high << 8));
    }
    protected static short codeUnit(int n0, int n1, int n2, int n3) {
        if ((n0 & 0xf) != n0) {
            throw new IllegalArgumentException("n0 out of range 0..15");
        }
        if ((n1 & 0xf) != n1) {
            throw new IllegalArgumentException("n1 out of range 0..15");
        }
        if ((n2 & 0xf) != n2) {
            throw new IllegalArgumentException("n2 out of range 0..15");
        }
        if ((n3 & 0xf) != n3) {
            throw new IllegalArgumentException("n3 out of range 0..15");
        }
        return (short) (n0 | (n1 << 4) | (n2 << 8) | (n3 << 12));
    }
    protected static int makeByte(int low, int high) {
        if ((low & 0xf) != low) {
            throw new IllegalArgumentException("low out of range 0..15");
        }
        if ((high & 0xf) != high) {
            throw new IllegalArgumentException("high out of range 0..15");
        }
        return low | (high << 4);
    }
    protected static void write(AnnotatedOutput out, short c0) {
        out.writeShort(c0);
    }
    protected static void write(AnnotatedOutput out, short c0, short c1) {
        out.writeShort(c0);
        out.writeShort(c1);
    }
    protected static void write(AnnotatedOutput out, short c0, short c1,
                                short c2) {
        out.writeShort(c0);
        out.writeShort(c1);
        out.writeShort(c2);
    }
    protected static void write(AnnotatedOutput out, short c0, short c1,
                                short c2, short c3) {
        out.writeShort(c0);
        out.writeShort(c1);
        out.writeShort(c2);
        out.writeShort(c3);
    }
    protected static void write(AnnotatedOutput out, short c0, short c1,
                                short c2, short c3, short c4) {
        out.writeShort(c0);
        out.writeShort(c1);
        out.writeShort(c2);
        out.writeShort(c3);
        out.writeShort(c4);
    }
    protected static void write(AnnotatedOutput out, short c0, short c1,
                                short c2, short c3, short c4, short c5) {
        out.writeShort(c0);
        out.writeShort(c1);
        out.writeShort(c2);
        out.writeShort(c3);
        out.writeShort(c4);
        out.writeShort(c5);
    }
}
