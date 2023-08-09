public final class java_util_RegularEnumSet extends AbstractTest<Set<EnumPublic>> {
    public static void main(String[] args) {
        new java_util_RegularEnumSet().test(true);
    }
    protected Set<EnumPublic> getObject() {
        return EnumSet.noneOf(EnumPublic.class);
    }
    protected Set<EnumPublic> getAnotherObject() {
        Set<EnumPublic> set = EnumSet.noneOf(EnumPublic.class);
        set.add(EnumPublic.A);
        set.add(EnumPublic.Z);
        return set;
    }
}
