public final class RegisterSpec
        implements TypeBearer, ToHuman, Comparable<RegisterSpec> {
    public static final String PREFIX = "v";
    private static final HashMap<Object, RegisterSpec> theInterns =
        new HashMap<Object, RegisterSpec>(1000);
    private static final ForComparison theInterningItem = new ForComparison();
    private final int reg;
    private final TypeBearer type;
    private final LocalItem local;
    private static RegisterSpec intern(int reg, TypeBearer type,
            LocalItem local) {
        theInterningItem.set(reg, type, local);
        RegisterSpec found = theInterns.get(theInterningItem);
        if (found != null) {
            return found;
        }
        found = theInterningItem.toRegisterSpec();
        theInterns.put(found, found);
        return found;
    }
    public static RegisterSpec make(int reg, TypeBearer type) {
        return intern(reg, type, null);
    }
    public static RegisterSpec make(int reg, TypeBearer type,
            LocalItem local) {
        if (local == null) {
            throw new NullPointerException("local  == null");
        }
        return intern(reg, type, local);
    }
    public static RegisterSpec makeLocalOptional(
            int reg, TypeBearer type, LocalItem local) {
        return intern(reg, type, local);
    }
    public static String regString(int reg) {
        return PREFIX + reg;
    }
    private RegisterSpec(int reg, TypeBearer type, LocalItem local) {
        if (reg < 0) {
            throw new IllegalArgumentException("reg < 0");
        }
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        this.reg = reg;
        this.type = type;
        this.local = local;
    }
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof RegisterSpec)) {
            if (other instanceof ForComparison) {
                ForComparison fc = (ForComparison) other;
                return equals(fc.reg, fc.type, fc.local);
            }
            return false;
        }
        RegisterSpec spec = (RegisterSpec) other;
        return equals(spec.reg, spec.type, spec.local);
    }
    public boolean equalsUsingSimpleType(RegisterSpec other) {
        if (!matchesVariable(other)) {
            return false;
        }
        return (reg == other.reg);
    }
    public boolean matchesVariable(RegisterSpec other) {
        if (other == null) {
            return false;
        }
        return type.getType().equals(other.type.getType())
            && ((local == other.local)
                    || ((local != null) && local.equals(other.local)));
    }
    private boolean equals(int reg, TypeBearer type, LocalItem local) {
        return (this.reg == reg)
            && this.type.equals(type)
            && ((this.local == local)
                    || ((this.local != null) && this.local.equals(local)));
    }
    public int compareTo(RegisterSpec other) {
        if (this.reg < other.reg) {
            return -1;
        } else if (this.reg > other.reg) {
            return 1;
        }
        int compare = type.getType().compareTo(other.type.getType());
        if (compare != 0) {
            return compare;
        }
        if (this.local == null) {
            return (other.local == null) ? 0 : -1;
        } else if (other.local == null) {
            return 1;
        }
        return this.local.compareTo(other.local);
    }    
    @Override
    public int hashCode() {
        return hashCodeOf(reg, type, local);
    }
    private static int hashCodeOf(int reg, TypeBearer type, LocalItem local) {
        int hash = (local != null) ? local.hashCode() : 0;
        hash = (hash * 31 + type.hashCode()) * 31 + reg;
        return hash;
    }
    @Override
    public String toString() {
        return toString0(false);
    }
    public String toHuman() {
        return toString0(true);
    }
    public Type getType() {
        return type.getType();
    }
    public TypeBearer getFrameType() {
        return type.getFrameType();
    }
    public final int getBasicType() {
        return type.getBasicType();
    }
    public final int getBasicFrameType() {
        return type.getBasicFrameType();
    }
    public final boolean isConstant() {
        return false;
    }
    public int getReg() {
        return reg;
    }
    public TypeBearer getTypeBearer() {
        return type;
    }
    public LocalItem getLocalItem() {
        return local;
    }
    public int getNextReg() {
        return reg + getCategory();
    }
    public int getCategory() {
        return type.getType().getCategory();
    }
    public boolean isCategory1() {
        return type.getType().isCategory1();
    }
    public boolean isCategory2() {
        return type.getType().isCategory2();
    }
    public String regString() {
        return regString(reg);
    }
    public RegisterSpec intersect(RegisterSpec other, boolean localPrimary) {
        if (this == other) {
            return this;
        }
        if ((other == null) || (reg != other.getReg())) {
            return null;
        }
        LocalItem resultLocal =
            ((local == null) || !local.equals(other.getLocalItem()))
            ? null : local;
        boolean sameName = (resultLocal == local);
        if (localPrimary && !sameName) {
            return null;
        }
        Type thisType = getType();
        Type otherType = other.getType();
        if (thisType != otherType) {
            return null;
        }
        TypeBearer resultTypeBearer =
            type.equals(other.getTypeBearer()) ? type : thisType;
        if ((resultTypeBearer == type) && sameName) {
            return this;
        }
        return (resultLocal == null) ? make(reg, resultTypeBearer) :
            make(reg, resultTypeBearer, resultLocal);
    }
    public RegisterSpec withReg(int newReg) {
        if (reg == newReg) {
            return this;
        }
        return makeLocalOptional(newReg, type, local);
    }
    public RegisterSpec withType(TypeBearer newType) {
        return makeLocalOptional(reg, newType, local);
    }
    public RegisterSpec withOffset(int delta) {
        if (delta == 0) {
            return this;
        }
        return withReg(reg + delta);
    }
    public RegisterSpec withSimpleType() {
        TypeBearer orig = type;
        Type newType;
        if (orig instanceof Type) {
            newType = (Type) orig;
        } else {
            newType = orig.getType();
        }
        if (newType.isUninitialized()) {
            newType = newType.getInitializedType();
        }
        if (newType == orig) {
            return this;
        }
        return makeLocalOptional(reg, newType, local);
    }
    public RegisterSpec withLocalItem(LocalItem local) {
        if ((this.local== local)
                    || ((this.local != null) && this.local.equals(local))) {
            return this;
        }
        return makeLocalOptional(reg, type, local);
    }
    private String toString0(boolean human) {
        StringBuffer sb = new StringBuffer(40);
        sb.append(regString());
        sb.append(":");
        if (local != null) {
            sb.append(local.toString());
        }
        Type justType = type.getType();
        sb.append(justType);
        if (justType != type) {
            sb.append("=");
            if (human && (type instanceof Constant)) {
                sb.append(((Constant) type).toHuman());
            } else {
                sb.append(type);
            }
        }
        return sb.toString();
    }
    private static class ForComparison {
        private int reg;
        private TypeBearer type;
        private LocalItem local;
        public void set(int reg, TypeBearer type, LocalItem local) {
            this.reg = reg;
            this.type = type;
            this.local = local;
        }
        public RegisterSpec toRegisterSpec() {
            return new RegisterSpec(reg, type, local);
        }
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof RegisterSpec)) {
                return false;
            }
            RegisterSpec spec = (RegisterSpec) other;
            return spec.equals(reg, type, local);
        }
        @Override
        public int hashCode() {
            return hashCodeOf(reg, type, local);
        }
    }
}
