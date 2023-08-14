public final class java_util_JumboEnumSet extends AbstractTest<Set<EnumPrivate>> {
    public static void main(String[] args) {
        new java_util_JumboEnumSet().test(true);
    }
    protected Set<EnumPrivate> getObject() {
        return EnumSet.noneOf(EnumPrivate.class);
    }
    protected Set<EnumPrivate> getAnotherObject() {
        Set<EnumPrivate> set = EnumSet.noneOf(EnumPrivate.class);
        set.add(EnumPrivate.A0);
        set.add(EnumPrivate.Z9);
        return set;
    }
}
