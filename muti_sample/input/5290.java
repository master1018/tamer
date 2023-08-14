public class JPasswordField extends JTextField {
    public JPasswordField() {
        this(null,null,0);
    }
    public JPasswordField(String text) {
        this(null, text, 0);
    }
    public JPasswordField(int columns) {
        this(null, null, columns);
    }
    public JPasswordField(String text, int columns) {
        this(null, text, columns);
    }
    public JPasswordField(Document doc, String txt, int columns) {
        super(doc, txt, columns);
        enableInputMethods(false);
    }
    public String getUIClassID() {
        return uiClassID;
    }
    public void updateUI() {
        if(!echoCharSet) {
            echoChar = '*';
        }
        super.updateUI();
    }
    public char getEchoChar() {
        return echoChar;
    }
    public void setEchoChar(char c) {
        echoChar = c;
        echoCharSet = true;
        repaint();
        revalidate();
    }
    public boolean echoCharIsSet() {
        return echoChar != 0;
    }
    public void cut() {
        if (getClientProperty("JPasswordField.cutCopyAllowed") != Boolean.TRUE) {
            UIManager.getLookAndFeel().provideErrorFeedback(this);
        } else {
            super.cut();
        }
    }
    public void copy() {
        if (getClientProperty("JPasswordField.cutCopyAllowed") != Boolean.TRUE) {
            UIManager.getLookAndFeel().provideErrorFeedback(this);
        } else {
            super.copy();
        }
    }
    @Deprecated
    public String getText() {
        return super.getText();
    }
    @Deprecated
    public String getText(int offs, int len) throws BadLocationException {
        return super.getText(offs, len);
    }
    public char[] getPassword() {
        Document doc = getDocument();
        Segment txt = new Segment();
        try {
            doc.getText(0, doc.getLength(), txt); 
        } catch (BadLocationException e) {
            return null;
        }
        char[] retValue = new char[txt.count];
        System.arraycopy(txt.array, txt.offset, retValue, 0, txt.count);
        return retValue;
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this, --count);
            if (count == 0 && ui != null) {
                ui.installUI(this);
            }
        }
    }
    private static final String uiClassID = "PasswordFieldUI";
    private char echoChar;
    private boolean echoCharSet = false;
    protected String paramString() {
        return super.paramString() +
        ",echoChar=" + echoChar;
    }
    boolean customSetUIProperty(String propertyName, Object value) {
        if (propertyName == "echoChar") {
            if (!echoCharSet) {
                setEchoChar((Character)value);
                echoCharSet = false;
            }
            return true;
        }
        return false;
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJPasswordField();
        }
        return accessibleContext;
    }
    protected class AccessibleJPasswordField extends AccessibleJTextField {
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PASSWORD_TEXT;
        }
        public AccessibleText getAccessibleText() {
            return this;
        }
        private String getEchoString(String str) {
            if (str == null) {
                return null;
            }
            char[] buffer = new char[str.length()];
            Arrays.fill(buffer, getEchoChar());
            return new String(buffer);
        }
        public String getAtIndex(int part, int index) {
           String str = null;
            if (part == AccessibleText.CHARACTER) {
                str = super.getAtIndex(part, index);
            } else {
                char password[] = getPassword();
                if (password == null ||
                    index < 0 || index >= password.length) {
                    return null;
                }
                str = new String(password);
            }
            return getEchoString(str);
        }
        public String getAfterIndex(int part, int index) {
            if (part == AccessibleText.CHARACTER) {
                String str = super.getAfterIndex(part, index);
                return getEchoString(str);
            } else {
                return null;
            }
        }
        public String getBeforeIndex(int part, int index) {
            if (part == AccessibleText.CHARACTER) {
                String str = super.getBeforeIndex(part, index);
                return getEchoString(str);
            } else {
                return null;
            }
        }
        public String getTextRange(int startIndex, int endIndex) {
            String str = super.getTextRange(startIndex, endIndex);
            return getEchoString(str);
        }
        public AccessibleTextSequence getTextSequenceAt(int part, int index) {
            if (part == AccessibleText.CHARACTER) {
                AccessibleTextSequence seq = super.getTextSequenceAt(part, index);
                if (seq == null) {
                    return null;
                }
                return new AccessibleTextSequence(seq.startIndex, seq.endIndex,
                                                  getEchoString(seq.text));
            } else {
                char password[] = getPassword();
                if (password == null ||
                    index < 0 || index >= password.length) {
                    return null;
                }
                String text = new String(password);
                return new AccessibleTextSequence(0, password.length - 1,
                                                  getEchoString(text));
            }
        }
        public AccessibleTextSequence getTextSequenceAfter(int part, int index) {
            if (part == AccessibleText.CHARACTER) {
                AccessibleTextSequence seq = super.getTextSequenceAfter(part, index);
                if (seq == null) {
                    return null;
                }
                return new AccessibleTextSequence(seq.startIndex, seq.endIndex,
                                                  getEchoString(seq.text));
            } else {
                return null;
            }
        }
        public AccessibleTextSequence getTextSequenceBefore(int part, int index) {
            if (part == AccessibleText.CHARACTER) {
                AccessibleTextSequence seq = super.getTextSequenceBefore(part, index);
                if (seq == null) {
                    return null;
                }
                return new AccessibleTextSequence(seq.startIndex, seq.endIndex,
                                                  getEchoString(seq.text));
            } else {
                return null;
            }
        }
    }
}
