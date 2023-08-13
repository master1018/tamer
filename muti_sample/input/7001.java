public final class PropertyEditorFinder
        extends InstanceFinder<PropertyEditor> {
    private final WeakCache<Class<?>, Class<?>> registry;
    public PropertyEditorFinder() {
        super(PropertyEditor.class, false, "Editor", "sun.beans.editors");
        this.registry = new WeakCache<Class<?>, Class<?>>();
        this.registry.put(Byte.TYPE, ByteEditor.class);
        this.registry.put(Short.TYPE, ShortEditor.class);
        this.registry.put(Integer.TYPE, IntegerEditor.class);
        this.registry.put(Long.TYPE, LongEditor.class);
        this.registry.put(Boolean.TYPE, BooleanEditor.class);
        this.registry.put(Float.TYPE, FloatEditor.class);
        this.registry.put(Double.TYPE, DoubleEditor.class);
    }
    public void register(Class<?> type, Class<?> editor) {
        synchronized (this.registry) {
            this.registry.put(type, editor);
        }
    }
    @Override
    public PropertyEditor find(Class<?> type) {
        Class<?> predefined;
        synchronized (this.registry) {
            predefined = this.registry.get(type);
        }
        PropertyEditor editor = instantiate(predefined, null);
        if (editor == null) {
            editor = super.find(type);
            if ((editor == null) && (null != type.getEnumConstants())) {
                editor = new EnumEditor(type);
            }
        }
        return editor;
    }
}
