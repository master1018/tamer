        flags = EnumSet.noneOf(TargetAttribute.class);
        for (TargetAttribute attr : attributes)
            flags.add(attr);
    }
    public boolean hasLocation() {
        return flags.contains(HasLocation);
    }
    public TargetType getGenericComplement() {
        if (hasLocation())
            return this;
        else
            return fromTargetTypeValue(targetTypeValue() + 1);
    }
    public boolean hasParameter() {
        return flags.contains(HasParameter);
    }
    public boolean hasBound() {
        return flags.contains(HasBound);
    }
    public boolean isLocal() {
        return flags.contains(IsLocal);
    }
    public int targetTypeValue() {
        return this.targetTypeValue;
    }
    private static TargetType[] targets = null;
    private static TargetType[] buildTargets() {
        TargetType[] targets = new TargetType[MAXIMUM_TARGET_TYPE_VALUE + 1];
        TargetType[] alltargets = values();
        for (TargetType target : alltargets) {
            if (target.targetTypeValue >= 0)
                targets[target.targetTypeValue] = target;
        }
        for (int i = 0; i <= MAXIMUM_TARGET_TYPE_VALUE; ++i) {
            if (targets[i] == null)
                targets[i] = UNKNOWN;
        }
        return targets;
    }
    public static boolean isValidTargetTypeValue(int tag) {
        if (targets == null)
            targets = buildTargets();
        if (((byte)tag) == ((byte)UNKNOWN.targetTypeValue))
            return true;
        return (tag >= 0 && tag < targets.length);
    }
    public static TargetType fromTargetTypeValue(int tag) {
        if (targets == null)
            targets = buildTargets();
        if (((byte)tag) == ((byte)UNKNOWN.targetTypeValue))
            return UNKNOWN;
        if (tag < 0 || tag >= targets.length)
            throw new IllegalArgumentException("Unknown TargetType: " + tag);
        return targets[tag];
    }
    static enum TargetAttribute {
        HasLocation, HasParameter, HasBound, IsLocal;
    }
}
