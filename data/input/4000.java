public final class PrimitiveWrapperMap {
    static void replacePrimitivesWithWrappers(Class<?>[] types) {
        for (int i = 0; i < types.length; i++) {
            if (types[i] != null) {
                if (types[i].isPrimitive()) {
                    types[i] = getType(types[i].getName());
                }
            }
        }
    }
    public static Class<?> getType(String name) {
        return map.get(name);
    }
    private static final Map<String, Class<?>> map = new HashMap<String, Class<?>>(9);
    static {
        map.put(Boolean.TYPE.getName(), Boolean.class);
        map.put(Character.TYPE.getName(), Character.class);
        map.put(Byte.TYPE.getName(), Byte.class);
        map.put(Short.TYPE.getName(), Short.class);
        map.put(Integer.TYPE.getName(), Integer.class);
        map.put(Long.TYPE.getName(), Long.class);
        map.put(Float.TYPE.getName(), Float.class);
        map.put(Double.TYPE.getName(), Double.class);
        map.put(Void.TYPE.getName(), Void.class);
    }
    private PrimitiveWrapperMap() {
    }
}
