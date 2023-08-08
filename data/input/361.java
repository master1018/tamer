public class IntegerEditor extends NumberEditor {
    public void setAsText(String text) throws IllegalArgumentException {
        setValue((text == null) ? null : Integer.decode(text));
    }
}
