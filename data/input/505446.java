public final class ReturnAddress implements TypeBearer {
    private final int subroutineAddress;
    public ReturnAddress(int subroutineAddress) {
        if (subroutineAddress < 0) {
            throw new IllegalArgumentException("subroutineAddress < 0");
        }
        this.subroutineAddress = subroutineAddress;
    }
    @Override
    public String toString() {
        return ("<addr:" + Hex.u2(subroutineAddress) + ">");
    }
    public String toHuman() {
        return toString();
    }
    public Type getType() {
        return Type.RETURN_ADDRESS;
    }
    public TypeBearer getFrameType() {
        return this;
    }
    public int getBasicType() {
        return Type.RETURN_ADDRESS.getBasicType();
    }
    public int getBasicFrameType() {
        return Type.RETURN_ADDRESS.getBasicFrameType();
    }
    public boolean isConstant() {
        return false;
    }
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ReturnAddress)) {
            return false;
        }
        return subroutineAddress == ((ReturnAddress) other).subroutineAddress;
    }
    @Override
    public int hashCode() {
        return subroutineAddress;
    }
    public int getSubroutineAddress() {
        return subroutineAddress;
    }
}
