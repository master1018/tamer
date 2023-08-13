public abstract class ComponentInternals {
    public static ComponentInternals getComponentInternals() {
        return ContextStorage.getComponentInternals();
    }
    public static void setComponentInternals(ComponentInternals internals) {
        ContextStorage.setComponentInternals(internals);
    }
    public abstract void shutdown();
    public abstract void setDesktopProperty(String name, Object value);
}
