public class LongEditor extends NumberEditor {
    public String getJavaInitializationString() {
        Object value = getValue();
        return (value != null)
                ? value + "L"
                : "null";
    }
    public void setAsText(String text) throws IllegalArgumentException {
        setValue((text == null) ? null : Long.decode(text));
    }
}
