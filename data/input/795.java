public class AclPrivilegedMode {
    private static ThreadLocal<Boolean> privilegedMode = new ThreadLocal<Boolean>();
    public static boolean set() {
        boolean isSet = isSet();
        privilegedMode.set(Boolean.TRUE);
        return isSet;
    }
    public static void clear() {
        privilegedMode.set(Boolean.FALSE);
    }
    public static boolean isSet() {
        return (Boolean.TRUE.equals(privilegedMode.get()));
    }
}
