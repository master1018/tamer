public class DefaultFormatter extends JFormattedTextField.AbstractFormatter
                    implements Cloneable, Serializable {
    private boolean allowsInvalid;
    private boolean overwriteMode;
    private boolean commitOnEdit;
    private Class<?> valueClass;
    private NavigationFilter navigationFilter;
    private DocumentFilter documentFilter;
    transient ReplaceHolder replaceHolder;
    public DefaultFormatter() {
        overwriteMode = true;
        allowsInvalid = true;
    }
    public void install(JFormattedTextField ftf) {
        super.install(ftf);
        positionCursorAtInitialLocation();
    }
    public void setCommitsOnValidEdit(boolean commit) {
        commitOnEdit = commit;
    }
    public boolean getCommitsOnValidEdit() {
        return commitOnEdit;
    }
    public void setOverwriteMode(boolean overwriteMode) {
        this.overwriteMode = overwriteMode;
    }
    public boolean getOverwriteMode() {
        return overwriteMode;
    }
    public void setAllowsInvalid(boolean allowsInvalid) {
        this.allowsInvalid = allowsInvalid;
    }
    public boolean getAllowsInvalid() {
        return allowsInvalid;
    }
    public void setValueClass(Class<?> valueClass) {
        this.valueClass = valueClass;
    }
    public Class<?> getValueClass() {
        return valueClass;
    }
    public Object stringToValue(String string) throws ParseException {
        Class<?> vc = getValueClass();
        JFormattedTextField ftf = getFormattedTextField();
        if (vc == null && ftf != null) {
            Object value = ftf.getValue();
            if (value != null) {
                vc = value.getClass();
            }
        }
        if (vc != null) {
            Constructor cons;
            try {
                cons = vc.getConstructor(new Class[] { String.class });
            } catch (NoSuchMethodException nsme) {
                cons = null;
            }
            if (cons != null) {
                try {
                    return cons.newInstance(new Object[] { string });
                } catch (Throwable ex) {
                    throw new ParseException("Error creating instance", 0);
                }
            }
        }
        return string;
    }
    public String valueToString(Object value) throws ParseException {
        if (value == null) {
            return "";
        }
        return value.toString();
    }
    protected DocumentFilter getDocumentFilter() {
        if (documentFilter == null) {
            documentFilter = new DefaultDocumentFilter();
        }
        return documentFilter;
    }
    protected NavigationFilter getNavigationFilter() {
        if (navigationFilter == null) {
            navigationFilter = new DefaultNavigationFilter();
        }
        return navigationFilter;
    }
    public Object clone() throws CloneNotSupportedException {
        DefaultFormatter formatter = (DefaultFormatter)super.clone();
        formatter.navigationFilter = null;
        formatter.documentFilter = null;
        formatter.replaceHolder = null;
        return formatter;
    }
    void positionCursorAtInitialLocation() {
        JFormattedTextField ftf = getFormattedTextField();
        if (ftf != null) {
            ftf.setCaretPosition(getInitialVisualPosition());
        }
    }
    int getInitialVisualPosition() {
        return getNextNavigatableChar(0, 1);
    }
    boolean isNavigatable(int offset) {
        return true;
    }
    boolean isLegalInsertText(String text) {
        return true;
    }
    private int getNextNavigatableChar(int offset, int direction) {
        int max = getFormattedTextField().getDocument().getLength();
        while (offset >= 0 && offset < max) {
            if (isNavigatable(offset)) {
                return offset;
            }
            offset += direction;
        }
        return offset;
    }
    String getReplaceString(int offset, int deleteLength,
                            String replaceString) {
        String string = getFormattedTextField().getText();
        String result;
        result = string.substring(0, offset);
        if (replaceString != null) {
            result += replaceString;
        }
        if (offset + deleteLength < string.length()) {
            result += string.substring(offset + deleteLength);
        }
        return result;
    }
    boolean isValidEdit(ReplaceHolder rh) {
        if (!getAllowsInvalid()) {
            String newString = getReplaceString(rh.offset, rh.length, rh.text);
            try {
                rh.value = stringToValue(newString);
                return true;
            } catch (ParseException pe) {
                return false;
            }
        }
        return true;
    }
    void commitEdit() throws ParseException {
        JFormattedTextField ftf = getFormattedTextField();
        if (ftf != null) {
            ftf.commitEdit();
        }
    }
    void updateValue() {
        updateValue(null);
    }
    void updateValue(Object value) {
        try {
            if (value == null) {
                String string = getFormattedTextField().getText();
                value = stringToValue(string);
            }
            if (getCommitsOnValidEdit()) {
                commitEdit();
            }
            setEditValid(true);
        } catch (ParseException pe) {
            setEditValid(false);
        }
    }
    int getNextCursorPosition(int offset, int direction) {
        int newOffset = getNextNavigatableChar(offset, direction);
        int max = getFormattedTextField().getDocument().getLength();
        if (!getAllowsInvalid()) {
            if (direction == -1 && offset == newOffset) {
                newOffset = getNextNavigatableChar(newOffset, 1);
                if (newOffset >= max) {
                    newOffset = offset;
                }
            }
            else if (direction == 1 && newOffset >= max) {
                newOffset = getNextNavigatableChar(max - 1, -1);
                if (newOffset < max) {
                    newOffset++;
                }
            }
        }
        return newOffset;
    }
    void repositionCursor(int offset, int direction) {
        getFormattedTextField().getCaret().setDot(getNextCursorPosition
                                                  (offset, direction));
    }
    int getNextVisualPositionFrom(JTextComponent text, int pos,
                                  Position.Bias bias, int direction,
                                  Position.Bias[] biasRet)
                                           throws BadLocationException {
        int value = text.getUI().getNextVisualPositionFrom(text, pos, bias,
                                                           direction, biasRet);
        if (value == -1) {
            return -1;
        }
        if (!getAllowsInvalid() && (direction == SwingConstants.EAST ||
                                    direction == SwingConstants.WEST)) {
            int last = -1;
            while (!isNavigatable(value) && value != last) {
                last = value;
                value = text.getUI().getNextVisualPositionFrom(
                              text, value, bias, direction,biasRet);
            }
            int max = getFormattedTextField().getDocument().getLength();
            if (last == value || value == max) {
                if (value == 0) {
                    biasRet[0] = Position.Bias.Forward;
                    value = getInitialVisualPosition();
                }
                if (value >= max && max > 0) {
                    biasRet[0] = Position.Bias.Forward;
                    value = getNextNavigatableChar(max - 1, -1) + 1;
                }
            }
        }
        return value;
    }
    boolean canReplace(ReplaceHolder rh) {
        return isValidEdit(rh);
    }
    void replace(DocumentFilter.FilterBypass fb, int offset,
                     int length, String text,
                     AttributeSet attrs) throws BadLocationException {
        ReplaceHolder rh = getReplaceHolder(fb, offset, length, text, attrs);
        replace(rh);
    }
    boolean replace(ReplaceHolder rh) throws BadLocationException {
        boolean valid = true;
        int direction = 1;
        if (rh.length > 0 && (rh.text == null || rh.text.length() == 0) &&
               (getFormattedTextField().getSelectionStart() != rh.offset ||
                   rh.length > 1)) {
            direction = -1;
        }
        if (getOverwriteMode() && rh.text != null &&
            getFormattedTextField().getSelectedText() == null)
        {
            rh.length = Math.min(Math.max(rh.length, rh.text.length()),
                                 rh.fb.getDocument().getLength() - rh.offset);
        }
        if ((rh.text != null && !isLegalInsertText(rh.text)) ||
            !canReplace(rh) ||
            (rh.length == 0 && (rh.text == null || rh.text.length() == 0))) {
            valid = false;
        }
        if (valid) {
            int cursor = rh.cursorPosition;
            rh.fb.replace(rh.offset, rh.length, rh.text, rh.attrs);
            if (cursor == -1) {
                cursor = rh.offset;
                if (direction == 1 && rh.text != null) {
                    cursor = rh.offset + rh.text.length();
                }
            }
            updateValue(rh.value);
            repositionCursor(cursor, direction);
            return true;
        }
        else {
            invalidEdit();
        }
        return false;
    }
    void setDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias){
        fb.setDot(dot, bias);
    }
    void moveDot(NavigationFilter.FilterBypass fb, int dot,
                 Position.Bias bias) {
        fb.moveDot(dot, bias);
    }
    ReplaceHolder getReplaceHolder(DocumentFilter.FilterBypass fb, int offset,
                                   int length, String text,
                                   AttributeSet attrs) {
        if (replaceHolder == null) {
            replaceHolder = new ReplaceHolder();
        }
        replaceHolder.reset(fb, offset, length, text, attrs);
        return replaceHolder;
    }
    static class ReplaceHolder {
        DocumentFilter.FilterBypass fb;
        int offset;
        int length;
        String text;
        AttributeSet attrs;
        Object value;
        int cursorPosition;
        void reset(DocumentFilter.FilterBypass fb, int offset, int length,
                   String text, AttributeSet attrs) {
            this.fb = fb;
            this.offset = offset;
            this.length = length;
            this.text = text;
            this.attrs = attrs;
            this.value = null;
            cursorPosition = -1;
        }
    }
    private class DefaultNavigationFilter extends NavigationFilter
                             implements Serializable {
        public void setDot(FilterBypass fb, int dot, Position.Bias bias) {
            JTextComponent tc = DefaultFormatter.this.getFormattedTextField();
            if (tc.composedTextExists()) {
                fb.setDot(dot, bias);
            } else {
                DefaultFormatter.this.setDot(fb, dot, bias);
            }
        }
        public void moveDot(FilterBypass fb, int dot, Position.Bias bias) {
            JTextComponent tc = DefaultFormatter.this.getFormattedTextField();
            if (tc.composedTextExists()) {
                fb.moveDot(dot, bias);
            } else {
                DefaultFormatter.this.moveDot(fb, dot, bias);
            }
        }
        public int getNextVisualPositionFrom(JTextComponent text, int pos,
                                             Position.Bias bias,
                                             int direction,
                                             Position.Bias[] biasRet)
                                           throws BadLocationException {
            if (text.composedTextExists()) {
                return text.getUI().getNextVisualPositionFrom(
                        text, pos, bias, direction, biasRet);
            } else {
                return DefaultFormatter.this.getNextVisualPositionFrom(
                        text, pos, bias, direction, biasRet);
            }
        }
    }
    private class DefaultDocumentFilter extends DocumentFilter implements
                             Serializable {
        public void remove(FilterBypass fb, int offset, int length) throws
                              BadLocationException {
            JTextComponent tc = DefaultFormatter.this.getFormattedTextField();
            if (tc.composedTextExists()) {
                fb.remove(offset, length);
            } else {
                DefaultFormatter.this.replace(fb, offset, length, null, null);
            }
        }
        public void insertString(FilterBypass fb, int offset,
                                 String string, AttributeSet attr) throws
                              BadLocationException {
            JTextComponent tc = DefaultFormatter.this.getFormattedTextField();
            if (tc.composedTextExists() ||
                Utilities.isComposedTextAttributeDefined(attr)) {
                fb.insertString(offset, string, attr);
            } else {
                DefaultFormatter.this.replace(fb, offset, 0, string, attr);
            }
        }
        public void replace(FilterBypass fb, int offset, int length,
                                 String text, AttributeSet attr) throws
                              BadLocationException {
            JTextComponent tc = DefaultFormatter.this.getFormattedTextField();
            if (tc.composedTextExists() ||
                Utilities.isComposedTextAttributeDefined(attr)) {
                fb.replace(offset, length, text, attr);
            } else {
                DefaultFormatter.this.replace(fb, offset, length, text, attr);
            }
        }
    }
}
