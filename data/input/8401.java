public class OneUniverse {
    private static final Field universeField;
    static {
        try {
            universeField = EnumSet.class.getDeclaredField("universe");
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
        universeField.setAccessible(true);
    }
    public static void main(String... args) {
        EnumSet<RoundingMode> noneSet = EnumSet.noneOf(RoundingMode.class);
        EnumSet<RoundingMode> allSet  = EnumSet.allOf(RoundingMode.class);
        if (getUniverse(noneSet) != getUniverse(allSet)) {
            throw new AssertionError(
                    "Multiple universes detected.  Inform the bridge!");
        }
    }
    private static <E extends Enum<E>> Enum<E>[] getUniverse(EnumSet<E> set) {
        try {
            return (Enum<E>[]) universeField.get(set);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }
}
