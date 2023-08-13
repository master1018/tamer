public class FloatEditor extends NumberEditor {
    public String getJavaInitializationString() {
        Object value = getValue();
        return (value != null)
                ? value + "F"
                : "null";
    }
    public void setAsText(String text) throws IllegalArgumentException {
        setValue((text == null) ? null : Float.valueOf(text));
    }
}
