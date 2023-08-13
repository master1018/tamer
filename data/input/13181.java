public class TextComponent extends Component implements Accessible {
    String text;
    boolean editable = true;
    int selectionStart;
    int selectionEnd;
    boolean backgroundSetByClientCode = false;
    transient private boolean canAccessClipboard;
    transient protected TextListener textListener;
    private static final long serialVersionUID = -2214773872412987419L;
    TextComponent(String text) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        this.text = (text != null) ? text : "";
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        checkSystemClipboardAccess();
    }
    private void enableInputMethodsIfNecessary() {
        if (checkForEnableIM) {
            checkForEnableIM = false;
            try {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                boolean shouldEnable = false;
                if (toolkit instanceof InputMethodSupport) {
                    shouldEnable = ((InputMethodSupport)toolkit)
                      .enableInputMethodsForTextComponent();
                }
                enableInputMethods(shouldEnable);
            } catch (Exception e) {
            }
        }
    }
    public void enableInputMethods(boolean enable) {
        checkForEnableIM = false;
        super.enableInputMethods(enable);
    }
    boolean areInputMethodsEnabled() {
        if (checkForEnableIM) {
            enableInputMethodsIfNecessary();
        }
        return (eventMask & AWTEvent.INPUT_METHODS_ENABLED_MASK) != 0;
    }
    public InputMethodRequests getInputMethodRequests() {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) return peer.getInputMethodRequests();
        else return null;
    }
    public void addNotify() {
        super.addNotify();
        enableInputMethodsIfNecessary();
    }
    public void removeNotify() {
        synchronized (getTreeLock()) {
            TextComponentPeer peer = (TextComponentPeer)this.peer;
            if (peer != null) {
                text = peer.getText();
                selectionStart = peer.getSelectionStart();
                selectionEnd = peer.getSelectionEnd();
            }
            super.removeNotify();
        }
    }
    public synchronized void setText(String t) {
        text = (t != null) ? t : "";
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            peer.setText(text);
        }
    }
    public synchronized String getText() {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            text = peer.getText();
        }
        return text;
    }
    public synchronized String getSelectedText() {
        return getText().substring(getSelectionStart(), getSelectionEnd());
    }
    public boolean isEditable() {
        return editable;
    }
    public synchronized void setEditable(boolean b) {
        if (editable == b) {
            return;
        }
        editable = b;
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            peer.setEditable(b);
        }
    }
    public Color getBackground() {
        if (!editable && !backgroundSetByClientCode) {
            return SystemColor.control;
        }
        return super.getBackground();
    }
    public void setBackground(Color c) {
        backgroundSetByClientCode = true;
        super.setBackground(c);
    }
    public synchronized int getSelectionStart() {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            selectionStart = peer.getSelectionStart();
        }
        return selectionStart;
    }
    public synchronized void setSelectionStart(int selectionStart) {
        select(selectionStart, getSelectionEnd());
    }
    public synchronized int getSelectionEnd() {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            selectionEnd = peer.getSelectionEnd();
        }
        return selectionEnd;
    }
    public synchronized void setSelectionEnd(int selectionEnd) {
        select(getSelectionStart(), selectionEnd);
    }
    public synchronized void select(int selectionStart, int selectionEnd) {
        String text = getText();
        if (selectionStart < 0) {
            selectionStart = 0;
        }
        if (selectionStart > text.length()) {
            selectionStart = text.length();
        }
        if (selectionEnd > text.length()) {
            selectionEnd = text.length();
        }
        if (selectionEnd < selectionStart) {
            selectionEnd = selectionStart;
        }
        this.selectionStart = selectionStart;
        this.selectionEnd = selectionEnd;
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            peer.select(selectionStart, selectionEnd);
        }
    }
    public synchronized void selectAll() {
        this.selectionStart = 0;
        this.selectionEnd = getText().length();
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            peer.select(selectionStart, selectionEnd);
        }
    }
    public synchronized void setCaretPosition(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("position less than zero.");
        }
        int maxposition = getText().length();
        if (position > maxposition) {
            position = maxposition;
        }
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            peer.setCaretPosition(position);
        } else {
            select(position, position);
        }
    }
    public synchronized int getCaretPosition() {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        int position = 0;
        if (peer != null) {
            position = peer.getCaretPosition();
        } else {
            position = selectionStart;
        }
        int maxposition = getText().length();
        if (position > maxposition) {
            position = maxposition;
        }
        return position;
    }
    public synchronized void addTextListener(TextListener l) {
        if (l == null) {
            return;
        }
        textListener = AWTEventMulticaster.add(textListener, l);
        newEventsOnly = true;
    }
    public synchronized void removeTextListener(TextListener l) {
        if (l == null) {
            return;
        }
        textListener = AWTEventMulticaster.remove(textListener, l);
    }
    public synchronized TextListener[] getTextListeners() {
        return (TextListener[])(getListeners(TextListener.class));
    }
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if  (listenerType == TextListener.class) {
            l = textListener;
        } else {
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l, listenerType);
    }
    boolean eventEnabled(AWTEvent e) {
        if (e.id == TextEvent.TEXT_VALUE_CHANGED) {
            if ((eventMask & AWTEvent.TEXT_EVENT_MASK) != 0 ||
                textListener != null) {
                return true;
            }
            return false;
        }
        return super.eventEnabled(e);
    }
    protected void processEvent(AWTEvent e) {
        if (e instanceof TextEvent) {
            processTextEvent((TextEvent)e);
            return;
        }
        super.processEvent(e);
    }
    protected void processTextEvent(TextEvent e) {
        TextListener listener = textListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
            case TextEvent.TEXT_VALUE_CHANGED:
                listener.textValueChanged(e);
                break;
            }
        }
    }
    protected String paramString() {
        String str = super.paramString() + ",text=" + getText();
        if (editable) {
            str += ",editable";
        }
        return str + ",selection=" + getSelectionStart() + "-" + getSelectionEnd();
    }
    private void checkSystemClipboardAccess() {
        canAccessClipboard = true;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                sm.checkSystemClipboardAccess();
            }
            catch (SecurityException e) {
                canAccessClipboard = false;
            }
        }
    }
    private int textComponentSerializedDataVersion = 1;
    private void writeObject(java.io.ObjectOutputStream s)
      throws IOException
    {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            text = peer.getText();
            selectionStart = peer.getSelectionStart();
            selectionEnd = peer.getSelectionEnd();
        }
        s.defaultWriteObject();
        AWTEventMulticaster.save(s, textListenerK, textListener);
        s.writeObject(null);
    }
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException, HeadlessException
    {
        GraphicsEnvironment.checkHeadless();
        s.defaultReadObject();
        this.text = (text != null) ? text : "";
        select(selectionStart, selectionEnd);
        Object keyOrNull;
        while(null != (keyOrNull = s.readObject())) {
            String key = ((String)keyOrNull).intern();
            if (textListenerK == key) {
                addTextListener((TextListener)(s.readObject()));
            } else {
                s.readObject();
            }
        }
        enableInputMethodsIfNecessary();
        checkSystemClipboardAccess();
    }
    int getIndexAtPoint(Point p) {
        return -1;
    }
    Rectangle getCharacterBounds(int i) {
        return null;
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTTextComponent();
        }
        return accessibleContext;
    }
    protected class AccessibleAWTTextComponent extends AccessibleAWTComponent
        implements AccessibleText, TextListener
    {
        private static final long serialVersionUID = 3631432373506317811L;
        public AccessibleAWTTextComponent() {
            TextComponent.this.addTextListener(this);
        }
        public void textValueChanged(TextEvent textEvent)  {
            Integer cpos = Integer.valueOf(TextComponent.this.getCaretPosition());
            firePropertyChange(ACCESSIBLE_TEXT_PROPERTY, null, cpos);
        }
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (TextComponent.this.isEditable()) {
                states.add(AccessibleState.EDITABLE);
            }
            return states;
        }
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TEXT;
        }
        public AccessibleText getAccessibleText() {
            return this;
        }
        public int getIndexAtPoint(Point p) {
            return TextComponent.this.getIndexAtPoint(p);
        }
        public Rectangle getCharacterBounds(int i) {
            return TextComponent.this.getCharacterBounds(i);
        }
        public int getCharCount() {
            return TextComponent.this.getText().length();
        }
        public int getCaretPosition() {
            return TextComponent.this.getCaretPosition();
        }
        public AttributeSet getCharacterAttribute(int i) {
            return null; 
        }
        public int getSelectionStart() {
            return TextComponent.this.getSelectionStart();
        }
        public int getSelectionEnd() {
            return TextComponent.this.getSelectionEnd();
        }
        public String getSelectedText() {
            String selText = TextComponent.this.getSelectedText();
            if (selText == null || selText.equals("")) {
                return null;
            }
            return selText;
        }
        public String getAtIndex(int part, int index) {
            if (index < 0 || index >= TextComponent.this.getText().length()) {
                return null;
            }
            switch (part) {
            case AccessibleText.CHARACTER:
                return TextComponent.this.getText().substring(index, index+1);
            case AccessibleText.WORD:  {
                    String s = TextComponent.this.getText();
                    BreakIterator words = BreakIterator.getWordInstance();
                    words.setText(s);
                    int end = words.following(index);
                    return s.substring(words.previous(), end);
                }
            case AccessibleText.SENTENCE:  {
                    String s = TextComponent.this.getText();
                    BreakIterator sentence = BreakIterator.getSentenceInstance();
                    sentence.setText(s);
                    int end = sentence.following(index);
                    return s.substring(sentence.previous(), end);
                }
            default:
                return null;
            }
        }
        private static final boolean NEXT = true;
        private static final boolean PREVIOUS = false;
        private int findWordLimit(int index, BreakIterator words, boolean direction,
                                         String s) {
            int last = (direction == NEXT) ? words.following(index)
                                           : words.preceding(index);
            int current = (direction == NEXT) ? words.next()
                                              : words.previous();
            while (current != BreakIterator.DONE) {
                for (int p = Math.min(last, current); p < Math.max(last, current); p++) {
                    if (Character.isLetter(s.charAt(p))) {
                        return last;
                    }
                }
                last = current;
                current = (direction == NEXT) ? words.next()
                                              : words.previous();
            }
            return BreakIterator.DONE;
        }
        public String getAfterIndex(int part, int index) {
            if (index < 0 || index >= TextComponent.this.getText().length()) {
                return null;
            }
            switch (part) {
            case AccessibleText.CHARACTER:
                if (index+1 >= TextComponent.this.getText().length()) {
                   return null;
                }
                return TextComponent.this.getText().substring(index+1, index+2);
            case AccessibleText.WORD:  {
                    String s = TextComponent.this.getText();
                    BreakIterator words = BreakIterator.getWordInstance();
                    words.setText(s);
                    int start = findWordLimit(index, words, NEXT, s);
                    if (start == BreakIterator.DONE || start >= s.length()) {
                        return null;
                    }
                    int end = words.following(start);
                    if (end == BreakIterator.DONE || end >= s.length()) {
                        return null;
                    }
                    return s.substring(start, end);
                }
            case AccessibleText.SENTENCE:  {
                    String s = TextComponent.this.getText();
                    BreakIterator sentence = BreakIterator.getSentenceInstance();
                    sentence.setText(s);
                    int start = sentence.following(index);
                    if (start == BreakIterator.DONE || start >= s.length()) {
                        return null;
                    }
                    int end = sentence.following(start);
                    if (end == BreakIterator.DONE || end >= s.length()) {
                        return null;
                    }
                    return s.substring(start, end);
                }
            default:
                return null;
            }
        }
        public String getBeforeIndex(int part, int index) {
            if (index < 0 || index > TextComponent.this.getText().length()-1) {
                return null;
            }
            switch (part) {
            case AccessibleText.CHARACTER:
                if (index == 0) {
                    return null;
                }
                return TextComponent.this.getText().substring(index-1, index);
            case AccessibleText.WORD:  {
                    String s = TextComponent.this.getText();
                    BreakIterator words = BreakIterator.getWordInstance();
                    words.setText(s);
                    int end = findWordLimit(index, words, PREVIOUS, s);
                    if (end == BreakIterator.DONE) {
                        return null;
                    }
                    int start = words.preceding(end);
                    if (start == BreakIterator.DONE) {
                        return null;
                    }
                    return s.substring(start, end);
                }
            case AccessibleText.SENTENCE:  {
                    String s = TextComponent.this.getText();
                    BreakIterator sentence = BreakIterator.getSentenceInstance();
                    sentence.setText(s);
                    int end = sentence.following(index);
                    end = sentence.previous();
                    int start = sentence.previous();
                    if (start == BreakIterator.DONE) {
                        return null;
                    }
                    return s.substring(start, end);
                }
            default:
                return null;
            }
        }
    }  
    private boolean checkForEnableIM = true;
}
