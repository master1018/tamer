public class TextEvent extends AWTEvent {
    private static final long serialVersionUID = 6269902291250941179L;
    public static final int TEXT_FIRST = 900;
    public static final int TEXT_LAST = 900;
    public static final int TEXT_VALUE_CHANGED = 900;
    public TextEvent(Object src, int id) {
        super(src, id);
    }
    @Override
    public String paramString() {
        return (id == TEXT_VALUE_CHANGED) ? 
                "TEXT_VALUE_CHANGED" : "unknown type"; 
    }
}
