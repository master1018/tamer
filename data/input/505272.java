public abstract class TimezoneGetter {
    private static TimezoneGetter instance;
    public static TimezoneGetter getInstance() {
        return instance;
    }
    public static void setInstance(TimezoneGetter getter) {
        if (instance != null) {
            throw new UnsupportedOperationException("TimezoneGetter instance already set");
        }
        instance = getter;
    }
    public abstract String getId();
}
