public final class FieldFinder {
    public static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        if (name == null) {
            throw new IllegalArgumentException("Field name is not set");
        }
        Field field = type.getField(name);
        if (!Modifier.isPublic(field.getModifiers())) {
            throw new NoSuchFieldException("Field '" + name + "' is not public");
        }
        if (!Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
            throw new NoSuchFieldException("Field '" + name + "' is not accessible");
        }
        return field;
    }
    public static Field findInstanceField(Class<?> type, String name) throws NoSuchFieldException {
        Field field = findField(type, name);
        if (Modifier.isStatic(field.getModifiers())) {
            throw new NoSuchFieldException("Field '" + name + "' is static");
        }
        return field;
    }
    public static Field findStaticField(Class<?> type, String name) throws NoSuchFieldException {
        Field field = findField(type, name);
        if (!Modifier.isStatic(field.getModifiers())) {
            throw new NoSuchFieldException("Field '" + name + "' is not static");
        }
        return field;
    }
    private FieldFinder() {
    }
}
