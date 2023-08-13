class EmulatedFieldsForLoading extends ObjectInputStream.GetField {
    private ObjectStreamClass streamClass;
    private EmulatedFields emulatedFields;
    EmulatedFieldsForLoading(ObjectStreamClass streamClass) {
        super();
        this.streamClass = streamClass;
        emulatedFields = new EmulatedFields(streamClass.getLoadFields(),
                streamClass.fields());
    }
    @Override
    public boolean defaulted(String name) throws IOException,
            IllegalArgumentException {
        return emulatedFields.defaulted(name);
    }
    EmulatedFields emulatedFields() {
        return emulatedFields;
    }
    @Override
    public byte get(String name, byte defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }
    @Override
    public char get(String name, char defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }
    @Override
    public double get(String name, double defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }
    @Override
    public float get(String name, float defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }
    @Override
    public int get(String name, int defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }
    @Override
    public long get(String name, long defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }
    @Override
    public Object get(String name, Object defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }
    @Override
    public short get(String name, short defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }
    @Override
    public boolean get(String name, boolean defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }
    @Override
    public ObjectStreamClass getObjectStreamClass() {
        return streamClass;
    }
}
