public class CodeObserver implements BytecodeArray.Visitor {
    private final ByteArray bytes;
    private final ParseObserver observer;
    public CodeObserver(ByteArray bytes, ParseObserver observer) {
        if (bytes == null) {
            throw new NullPointerException("bytes == null");
        }
        if (observer == null) {
            throw new NullPointerException("observer == null");
        }
        this.bytes = bytes;
        this.observer = observer;
    }
    public void visitInvalid(int opcode, int offset, int length) {
        observer.parsed(bytes, offset, length, header(offset));
    }
    public void visitNoArgs(int opcode, int offset, int length, Type type) {
        observer.parsed(bytes, offset, length, header(offset));
    }
    public void visitLocal(int opcode, int offset, int length,
            int idx, Type type, int value) {
        String idxStr = (length <= 3) ? Hex.u1(idx) : Hex.u2(idx);
        boolean argComment = (length == 1);
        String valueStr = "";
        if (opcode == ByteOps.IINC) {
            valueStr = ", #" +
                ((length <= 3) ? Hex.s1(value) : Hex.s2(value));
        }
        String catStr = "";
        if (type.isCategory2()) {
            catStr = (argComment ? "," : " 
        }
        observer.parsed(bytes, offset, length,
                        header(offset) + (argComment ? " 
                        idxStr + valueStr + catStr);
    }
    public void visitConstant(int opcode, int offset, int length,
            Constant cst, int value) {
        if (cst instanceof CstKnownNull) {
            visitNoArgs(opcode, offset, length, null);
            return;
        }
        if (cst instanceof CstInteger) {
            visitLiteralInt(opcode, offset, length, value);
            return;
        }
        if (cst instanceof CstLong) {
            visitLiteralLong(opcode, offset, length,
                             ((CstLong) cst).getValue());
            return;
        }
        if (cst instanceof CstFloat) {
            visitLiteralFloat(opcode, offset, length,
                              ((CstFloat) cst).getIntBits());
            return;
        }
        if (cst instanceof CstDouble) {
            visitLiteralDouble(opcode, offset, length,
                             ((CstDouble) cst).getLongBits());
            return;
        }
        String valueStr = "";
        if (value != 0) {
            valueStr = ", ";
            if (opcode == ByteOps.MULTIANEWARRAY) {
                valueStr += Hex.u1(value);
            } else {
                valueStr += Hex.u2(value);
            }
        }
        observer.parsed(bytes, offset, length,
                        header(offset) + " " + cst + valueStr);
    }
    public void visitBranch(int opcode, int offset, int length,
                            int target) {
        String targetStr = (length <= 3) ? Hex.u2(target) : Hex.u4(target);
        observer.parsed(bytes, offset, length,
                        header(offset) + " " + targetStr);
    }
    public void visitSwitch(int opcode, int offset, int length,
            SwitchList cases, int padding) {
        int sz = cases.size();
        StringBuffer sb = new StringBuffer(sz * 20 + 100);
        sb.append(header(offset));
        if (padding != 0) {
            sb.append(" 
        }
        sb.append('\n');
        for (int i = 0; i < sz; i++) {
            sb.append("  ");
            sb.append(Hex.s4(cases.getValue(i)));
            sb.append(": ");
            sb.append(Hex.u2(cases.getTarget(i)));
            sb.append('\n');
        }
        sb.append("  default: ");
        sb.append(Hex.u2(cases.getDefaultTarget()));
        observer.parsed(bytes, offset, length, sb.toString());
    }
    public void visitNewarray(int offset, int length, CstType cst,
            ArrayList<Constant> intVals) {
        String commentOrSpace = (length == 1) ? " 
        String typeName = cst.getClassType().getComponentType().toHuman();
        observer.parsed(bytes, offset, length,
                        header(offset) + commentOrSpace + typeName);
    }
    public void setPreviousOffset(int offset) {
    }
    public int getPreviousOffset() {
        return -1;
    }
    private String header(int offset) {
        int opcode = bytes.getUnsignedByte(offset);
        String name = ByteOps.opName(opcode);
        if (opcode == ByteOps.WIDE) {
            opcode = bytes.getUnsignedByte(offset + 1);
            name += " " + ByteOps.opName(opcode);
        }
        return Hex.u2(offset) + ": " + name;
    }
    private void visitLiteralInt(int opcode, int offset, int length,
            int value) {
        String commentOrSpace = (length == 1) ? " 
        String valueStr;
        opcode = bytes.getUnsignedByte(offset); 
        if ((length == 1) || (opcode == ByteOps.BIPUSH)) {
            valueStr = "#" + Hex.s1(value);
        } else if (opcode == ByteOps.SIPUSH) {
            valueStr = "#" + Hex.s2(value);
        } else {
            valueStr = "#" + Hex.s4(value);
        }
        observer.parsed(bytes, offset, length,
                        header(offset) + commentOrSpace + valueStr);
    }
    private void visitLiteralLong(int opcode, int offset, int length,
            long value) {
        String commentOrLit = (length == 1) ? " 
        String valueStr;
        if (length == 1) {
            valueStr = Hex.s1((int) value);
        } else {
            valueStr = Hex.s8(value);
        }
        observer.parsed(bytes, offset, length,
                        header(offset) + commentOrLit + valueStr);
    }
    private void visitLiteralFloat(int opcode, int offset, int length,
            int bits) {
        String optArg = (length != 1) ? " #" + Hex.u4(bits) : "";
        observer.parsed(bytes, offset, length,
                        header(offset) + optArg + " 
                        Float.intBitsToFloat(bits));
    }
    private void visitLiteralDouble(int opcode, int offset, int length,
            long bits) {
        String optArg = (length != 1) ? " #" + Hex.u8(bits) : "";
        observer.parsed(bytes, offset, length,
                        header(offset) + optArg + " 
                        Double.longBitsToDouble(bits));
    }
}
