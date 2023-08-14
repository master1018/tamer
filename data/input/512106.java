public class InputMethodEvent extends AWTEvent {
    private static final long serialVersionUID = 4727190874778922661L;
    public static final int INPUT_METHOD_FIRST = 1100;
    public static final int INPUT_METHOD_TEXT_CHANGED = 1100;
    public static final int CARET_POSITION_CHANGED = 1101;
    public static final int INPUT_METHOD_LAST = 1101;
    private AttributedCharacterIterator text;
    private TextHitInfo visiblePosition;
    private TextHitInfo caret;
    private int committedCharacterCount;
    private long when;
    public InputMethodEvent(Component src, int id,
                            TextHitInfo caret, 
                            TextHitInfo visiblePos) {
        this(src, id, null, 0, caret, visiblePos);
    }
    public InputMethodEvent(Component src, int id, 
                            AttributedCharacterIterator text,
                            int commitedCharCount,
                            TextHitInfo caret, 
                            TextHitInfo visiblePos) {
        this(src, id, 0l, text, commitedCharCount, caret, visiblePos);
    }
    public InputMethodEvent(Component src, int id, long when,
                            AttributedCharacterIterator text, 
                            int committedCharacterCount,
                            TextHitInfo caret,
                            TextHitInfo visiblePos) {
        super(src, id);
        if ((id < INPUT_METHOD_FIRST) || (id > INPUT_METHOD_LAST)) {
            throw new IllegalArgumentException(Messages.getString("awt.18E")); 
        }
        if ((id == CARET_POSITION_CHANGED) && (text != null)) {
            throw new IllegalArgumentException(Messages.getString("awt.18F")); 
        }
        if ((text != null) &&
                ((committedCharacterCount < 0) ||
                 (committedCharacterCount > 
                        (text.getEndIndex() - text.getBeginIndex())))) {
            throw new IllegalArgumentException(Messages.getString("awt.190")); 
        }
        this.when = when;
        this.text = text;
        this.caret = caret;
        this.visiblePosition = visiblePos;
        this.committedCharacterCount = committedCharacterCount;
    }
    public TextHitInfo getCaret() {
        return caret;
    }
    public int getCommittedCharacterCount() {
        return committedCharacterCount;
    }
    public AttributedCharacterIterator getText() {
        return text;
    }
    public TextHitInfo getVisiblePosition() {
        return visiblePosition;
    }
    public long getWhen() {
        return when;
    }
    @Override
    public void consume() {
        super.consume();
    }
    @Override
    public boolean isConsumed() {
        return super.isConsumed();
    }
    @Override
    public String paramString() {
        String typeString = null;
        switch (id) {
        case INPUT_METHOD_TEXT_CHANGED:
            typeString = "INPUT_METHOD_TEXT_CHANGED"; 
            break;
        case CARET_POSITION_CHANGED:
            typeString = "CARET_POSITION_CHANGED"; 
            break;
        default:
            typeString = "unknown type"; 
        }
        return typeString + ",text=" + text +  
                ",commitedCharCount=" + committedCharacterCount + 
                ",caret=" + caret + ",visiblePosition=" + visiblePosition; 
    }
}
