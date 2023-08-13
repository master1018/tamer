public class DoubleEditor extends NumberEditor {
    public void setAsText(String text) throws IllegalArgumentException {
        setValue((text == null) ? null : Double.valueOf(text));
    }
}
