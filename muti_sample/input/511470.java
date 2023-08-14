public final class Dop {
    private final int opcode;
    private final int family;
    private final InsnFormat format;
    private final boolean hasResult;
    private final String name;
    public Dop(int opcode, int family, InsnFormat format,
               boolean hasResult, String name) {
        if ((opcode < DalvOps.MIN_VALUE) || (opcode > DalvOps.MAX_VALUE)) {
            throw new IllegalArgumentException("bogus opcode");
        }
        if ((family < DalvOps.MIN_VALUE) || (family > DalvOps.MAX_VALUE)) {
            throw new IllegalArgumentException("bogus family");
        }
        if (format == null) {
            throw new NullPointerException("format == null");
        }
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        this.opcode = opcode;
        this.family = family;
        this.format = format;
        this.hasResult = hasResult;
        this.name = name;
    }
    @Override
    public String toString() {
        return name;
    }
    public int getOpcode() {
        return opcode;
    }
    public int getFamily() {
        return family;
    }
    public InsnFormat getFormat() {
        return format;
    }
    public boolean hasResult() {
        return hasResult;
    }
    public String getName() {
        return name;
    }
    public Dop getOppositeTest() {
        switch (opcode) {
            case DalvOps.IF_EQ:  return Dops.IF_NE;
            case DalvOps.IF_NE:  return Dops.IF_EQ;
            case DalvOps.IF_LT:  return Dops.IF_GE;
            case DalvOps.IF_GE:  return Dops.IF_LT;
            case DalvOps.IF_GT:  return Dops.IF_LE;
            case DalvOps.IF_LE:  return Dops.IF_GT;
            case DalvOps.IF_EQZ: return Dops.IF_NEZ;
            case DalvOps.IF_NEZ: return Dops.IF_EQZ;
            case DalvOps.IF_LTZ: return Dops.IF_GEZ;
            case DalvOps.IF_GEZ: return Dops.IF_LTZ;
            case DalvOps.IF_GTZ: return Dops.IF_LEZ;
            case DalvOps.IF_LEZ: return Dops.IF_GTZ;
        }
        throw new IllegalArgumentException("bogus opcode: " + this);
    }
}
