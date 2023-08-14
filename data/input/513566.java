public final class ArrayData extends VariableSizeInsn {
    private final CodeAddress user;
    private final ArrayList<Constant> values;
    private final Constant arrayType;
    private final int elemWidth;
    private final int initLength;
    public ArrayData(SourcePosition position, CodeAddress user,
                     ArrayList<Constant> values,
                     Constant arrayType) {
        super(position, RegisterSpecList.EMPTY);
        if (user == null) {
            throw new NullPointerException("user == null");
        }
        if (values == null) {
            throw new NullPointerException("values == null");
        }
        int sz = values.size();
        if (sz <= 0) {
            throw new IllegalArgumentException("Illegal number of init values");
        }
        this.arrayType = arrayType;
        if (arrayType == CstType.BYTE_ARRAY ||
                arrayType == CstType.BOOLEAN_ARRAY) {
            elemWidth = 1;
        } else if (arrayType == CstType.SHORT_ARRAY ||
                arrayType == CstType.CHAR_ARRAY) {
            elemWidth = 2;
        } else if (arrayType == CstType.INT_ARRAY ||
                arrayType == CstType.FLOAT_ARRAY) {
            elemWidth = 4;
        } else if (arrayType == CstType.LONG_ARRAY ||
                arrayType == CstType.DOUBLE_ARRAY) {
            elemWidth = 8;
        } else {
            throw new IllegalArgumentException("Unexpected constant type");
        }
        this.user = user;
        this.values = values;
        initLength = values.size();
    }
    @Override
    public int codeSize() {
        int sz = initLength;
        return 4 + ((sz * elemWidth) + 1) / 2;
    }
    @Override
    public void writeTo(AnnotatedOutput out) {
        int sz = values.size();
        out.writeShort(0x300 | DalvOps.NOP);
        out.writeShort(elemWidth);
        out.writeInt(initLength);
        switch (elemWidth) {
            case 1: {
                for (int i = 0; i < sz; i++) {
                    Constant cst = values.get(i);
                    out.writeByte((byte) ((CstLiteral32) cst).getIntBits());
                }
                break;
            }
            case 2: {
                for (int i = 0; i < sz; i++) {
                    Constant cst = values.get(i);
                    out.writeShort((short) ((CstLiteral32) cst).getIntBits());
                }
                break;
            }
            case 4: {
                for (int i = 0; i < sz; i++) {
                    Constant cst = values.get(i);
                    out.writeInt(((CstLiteral32) cst).getIntBits());
                }
                break;
            }
            case 8: {
                for (int i = 0; i < sz; i++) {
                    Constant cst = values.get(i);
                    out.writeLong(((CstLiteral64) cst).getLongBits());
                }
                break;
            }
            default:
                break;
        }
        if (elemWidth == 1 && (sz % 2 != 0)) {
            out.writeByte(0x00);
        }
    }
    @Override
    public DalvInsn withRegisters(RegisterSpecList registers) {
        return new ArrayData(getPosition(), user, values, arrayType);
    }
    @Override
    protected String argString() {
        StringBuffer sb = new StringBuffer(100);
        int sz = values.size();
        for (int i = 0; i < sz; i++) {
            sb.append("\n    ");
            sb.append(i);
            sb.append(": ");
            sb.append(values.get(i).toHuman());
        }
        return sb.toString();
    }
    @Override
    protected String listingString0(boolean noteIndices) {
        int baseAddress = user.getAddress();
        StringBuffer sb = new StringBuffer(100);
        int sz = values.size();
        sb.append("array-data 
        sb.append(Hex.u2(baseAddress));
        for (int i = 0; i < sz; i++) {
            sb.append("\n  ");
            sb.append(i);
            sb.append(": ");
            sb.append(values.get(i).toHuman());
        }
        return sb.toString();
    }
}
