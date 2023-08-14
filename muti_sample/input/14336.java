public class CodePointInputMethod implements InputMethod {
    private static final int UNSET = 0;
    private static final int ESCAPE = 1; 
    private static final int SPECIAL_ESCAPE = 2; 
    private static final int SURROGATE_PAIR = 3; 
    private InputMethodContext context;
    private Locale locale;
    private StringBuffer buffer;
    private int insertionPoint;
    private int format = UNSET;
    public CodePointInputMethod() throws IOException {
    }
    public void dispatchEvent(AWTEvent event) {
        if (!(event instanceof KeyEvent)) {
            return;
        }
        KeyEvent e = (KeyEvent) event;
        int eventID = event.getID();
        boolean notInCompositionMode = buffer.length() == 0;
        if (eventID == KeyEvent.KEY_PRESSED) {
            if (notInCompositionMode) {
                return;
            }
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    moveCaretLeft();
                    break;
                case KeyEvent.VK_RIGHT:
                    moveCaretRight();
                    break;
            }
        } else if (eventID == KeyEvent.KEY_TYPED) {
            char c = e.getKeyChar();
            if (notInCompositionMode) {
                if (c != '\\') {
                    return;
                }
                startComposition();     
            } else {
                switch (c) {
                    case ' ':       
                        finishComposition();
                        break;
                    case '\u007f':  
                        deleteCharacter();
                        break;
                    case '\b':      
                        deletePreviousCharacter();
                        break;
                    case '\u001b':  
                        cancelComposition();
                        break;
                    case '\n':      
                    case '\t':      
                        sendCommittedText();
                        break;
                    default:
                        composeUnicodeEscape(c);
                        break;
                }
            }
        } else {  
            if (notInCompositionMode) {
                return;
            }
        }
        e.consume();
    }
    private void composeUnicodeEscape(char c) {
        switch (buffer.length()) {
            case 1:  
                waitEscapeCharacter(c);
                break;
            case 2:  
            case 3:  
            case 4:  
                waitDigit(c);
                break;
            case 5:  
                if (format == SPECIAL_ESCAPE) {
                    waitDigit(c);
                } else {
                    waitDigit2(c);
                }
                break;
            case 6:  
                if (format == SPECIAL_ESCAPE) {
                    waitDigit(c);
                } else if (format == SURROGATE_PAIR) {
                    waitBackSlashOrLowSurrogate(c);
                } else {
                    beep();
                }
                break;
            case 7:  
                waitDigit(c);
                break;
            case 8:  
            case 9:  
            case 10: 
            case 11: 
                if (format == SURROGATE_PAIR) {
                    waitDigit(c);
                } else {
                    beep();
                }
                break;
            default:
                beep();
                break;
        }
    }
    private void waitEscapeCharacter(char c) {
        if (c == 'u' || c == 'U') {
            buffer.append(c);
            insertionPoint++;
            sendComposedText();
            format = (c == 'u') ? ESCAPE : SPECIAL_ESCAPE;
        } else {
            if (c != '\\') {
                buffer.append(c);
                insertionPoint++;
            }
            sendCommittedText();
        }
    }
    private void waitDigit(char c) {
        if (Character.digit(c, 16) != -1) {
            buffer.insert(insertionPoint++, c);
            sendComposedText();
        } else {
            beep();
        }
    }
    private void waitDigit2(char c) {
        if (Character.digit(c, 16) != -1) {
            buffer.insert(insertionPoint++, c);
            char codePoint = (char) getCodePoint(buffer, 2, 5);
            if (Character.isHighSurrogate(codePoint)) {
                format = SURROGATE_PAIR;
                buffer.append("\\u");
                insertionPoint = 8;
            } else {
                format = ESCAPE;
            }
            sendComposedText();
        } else {
            beep();
        }
    }
    private void waitBackSlashOrLowSurrogate(char c) {
        if (insertionPoint == 6) {
            if (c == '\\') {
                buffer.append(c);
                buffer.append('u');
                insertionPoint = 8;
                sendComposedText();
            } else if (Character.digit(c, 16) != -1) {
                buffer.append("\\u");
                buffer.append(c);
                insertionPoint = 9;
                sendComposedText();
            } else {
                beep();
            }
        } else {
            beep();
        }
    }
    private void sendComposedText() {
        AttributedString as = new AttributedString(buffer.toString());
        as.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT,
                InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT);
        context.dispatchInputMethodEvent(
                InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                as.getIterator(), 0,
                TextHitInfo.leading(insertionPoint), null);
    }
    private void sendCommittedText() {
        AttributedString as = new AttributedString(buffer.toString());
        context.dispatchInputMethodEvent(
                InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                as.getIterator(), buffer.length(),
                TextHitInfo.leading(insertionPoint), null);
        buffer.setLength(0);
        insertionPoint = 0;
        format = UNSET;
    }
    private void moveCaretLeft() {
        int len = buffer.length();
        if (--insertionPoint < 2) {
            insertionPoint++;
            beep();
        } else if (format == SURROGATE_PAIR && insertionPoint == 7) {
            insertionPoint = 8;
            beep();
        }
        context.dispatchInputMethodEvent(
                InputMethodEvent.CARET_POSITION_CHANGED,
                null, 0,
                TextHitInfo.leading(insertionPoint), null);
    }
    private void moveCaretRight() {
        int len = buffer.length();
        if (++insertionPoint > len) {
            insertionPoint = len;
            beep();
        }
        context.dispatchInputMethodEvent(
                InputMethodEvent.CARET_POSITION_CHANGED,
                null, 0,
                TextHitInfo.leading(insertionPoint), null);
    }
    private void deletePreviousCharacter() {
        if (insertionPoint == 2) {
            if (buffer.length() == 2) {
                cancelComposition();
            } else {
                beep();
            }
        } else if (insertionPoint == 8) {
            if (buffer.length() == 8) {
                if (format == SURROGATE_PAIR) {
                    buffer.deleteCharAt(--insertionPoint);
                }
                buffer.deleteCharAt(--insertionPoint);
                sendComposedText();
            } else {
                beep();
            }
        } else {
            buffer.deleteCharAt(--insertionPoint);
            if (buffer.length() == 0) {
                sendCommittedText();
            } else {
                sendComposedText();
            }
        }
    }
    private void deleteCharacter() {
        if (insertionPoint < buffer.length()) {
            buffer.deleteCharAt(insertionPoint);
            sendComposedText();
        } else {
            beep();
        }
    }
    private void startComposition() {
        buffer.append('\\');
        insertionPoint = 1;
        sendComposedText();
    }
    private void cancelComposition() {
        buffer.setLength(0);
        insertionPoint = 0;
        sendCommittedText();
    }
    private void finishComposition() {
        int len = buffer.length();
        if (len == 6 && format != SPECIAL_ESCAPE) {
            char codePoint = (char) getCodePoint(buffer, 2, 5);
            if (Character.isValidCodePoint(codePoint) && codePoint != 0xFFFF) {
                buffer.setLength(0);
                buffer.append(codePoint);
                sendCommittedText();
                return;
            }
        } else if (len == 8 && format == SPECIAL_ESCAPE) {
            int codePoint = getCodePoint(buffer, 2, 7);
            if (Character.isValidCodePoint(codePoint) && codePoint != 0xFFFF) {
                buffer.setLength(0);
                buffer.appendCodePoint(codePoint);
                sendCommittedText();
                return;
            }
        } else if (len == 12 && format == SURROGATE_PAIR) {
            char[] codePoint = {
                (char) getCodePoint(buffer, 2, 5),
                (char) getCodePoint(buffer, 8, 11)
            };
            if (Character.isHighSurrogate(codePoint[0]) && Character.
                    isLowSurrogate(codePoint[1])) {
                buffer.setLength(0);
                buffer.append(codePoint);
                sendCommittedText();
                return;
            }
        }
        beep();
    }
    private int getCodePoint(StringBuffer sb, int from, int to) {
        int value = 0;
        for (int i = from; i <= to; i++) {
            value = (value << 4) + Character.digit(sb.charAt(i), 16);
        }
        return value;
    }
    private static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }
    public void activate() {
        if (buffer == null) {
            buffer = new StringBuffer(12);
            insertionPoint = 0;
        }
    }
    public void deactivate(boolean isTemporary) {
        if (!isTemporary) {
            buffer = null;
        }
    }
    public void dispose() {
    }
    public Object getControlObject() {
        return null;
    }
    public void endComposition() {
        sendCommittedText();
    }
    public Locale getLocale() {
        return locale;
    }
    public void hideWindows() {
    }
    public boolean isCompositionEnabled() {
        return true;
    }
    public void notifyClientWindowChange(Rectangle location) {
    }
    public void reconvert() {
        throw new UnsupportedOperationException();
    }
    public void removeNotify() {
    }
    public void setCharacterSubsets(Character.Subset[] subsets) {
    }
    public void setCompositionEnabled(boolean enable) {
        throw new UnsupportedOperationException();
    }
    public void setInputMethodContext(InputMethodContext context) {
        this.context = context;
    }
    public boolean setLocale(Locale locale) {
        this.locale = locale;
        return true;
    }
}
