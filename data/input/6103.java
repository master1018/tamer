class NamedWeakReference extends java.lang.ref.WeakReference {
    private final String name;
    NamedWeakReference(Object referent, String name) {
        super(referent);
        this.name = name;
    }
    String getName() {
        return name;
    }
}
