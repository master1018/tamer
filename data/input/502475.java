public abstract class SoundbankResource {
    private Soundbank soundbank;
    private String name;
    private Class<?> dataClass;
    protected SoundbankResource(Soundbank soundbank, String name, Class<?> dataClass) {
        super();
        this.soundbank = soundbank;
        this.name = name;
        this.dataClass = dataClass;
    }
    public abstract Object getData();
    public Class<?> getDataClass() {
        return dataClass;
    }
    public String getName() {
        return name;
    }
    public Soundbank getSoundbank() {
        return soundbank;
    }
}
