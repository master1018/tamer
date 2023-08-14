final class PrimitiveTypeMap {
    static Class<?> getType(String name) {
        return map.get(name);
    }
    private static final Map<String, Class<?>> map = new HashMap<String, Class<?>>(9);
    static {
        map.put(boolean.class.getName(), boolean.class);
        map.put(char.class.getName(), char.class);
        map.put(byte.class.getName(), byte.class);
        map.put(short.class.getName(), short.class);
        map.put(int.class.getName(), int.class);
        map.put(long.class.getName(), long.class);
        map.put(float.class.getName(), float.class);
        map.put(double.class.getName(), double.class);
        map.put(void.class.getName(), void.class);
    }
    private PrimitiveTypeMap() {
    }
}
