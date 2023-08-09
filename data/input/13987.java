public final class java_util_EnumMap extends AbstractTest<Map<EnumPublic, String>> {
    public static void main(String[] args) {
        new java_util_EnumMap().test(true);
    }
    protected Map<EnumPublic, String> getObject() {
        return new EnumMap<EnumPublic, String>(EnumPublic.class);
    }
    protected Map<EnumPublic, String> getAnotherObject() {
        Map<EnumPublic, String> map = new EnumMap<EnumPublic, String>(EnumPublic.class);
        map.put(EnumPublic.A, "value");
        map.put(EnumPublic.Z, null);
        return map;
    }
}
