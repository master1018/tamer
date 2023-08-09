public class AudioSynthesizerPropertyInfo {
    public AudioSynthesizerPropertyInfo(String name, Object value) {
        this.name = name;
        if (value instanceof Class)
            valueClass = (Class)value;
        else
        {
            this.value = value;
            if (value != null)
                valueClass = value.getClass();
        }
    }
    public String name;
    public String description = null;
    public Object value = null;
    public Class valueClass = null;
    public Object[] choices = null;
}
