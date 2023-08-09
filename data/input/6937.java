class CompositionAreaHandler implements InputMethodListener,
                                                 InputMethodRequests {
    private static CompositionArea compositionArea;
    private static Object compositionAreaLock = new Object();
    private static CompositionAreaHandler compositionAreaOwner; 
    private AttributedCharacterIterator composedText;
    private TextHitInfo caret = null;
    private Component clientComponent = null;
    private InputMethodContext inputMethodContext;
    CompositionAreaHandler(InputMethodContext context) {
        inputMethodContext = context;
    }
    private void createCompositionArea() {
        synchronized(compositionAreaLock) {
            compositionArea = new CompositionArea();
            if (compositionAreaOwner != null) {
                compositionArea.setHandlerInfo(compositionAreaOwner, inputMethodContext);
            }
            if(clientComponent!=null){
                InputMethodRequests req = clientComponent.getInputMethodRequests();
                if (req != null && inputMethodContext.useBelowTheSpotInput()) {
                    setCompositionAreaUndecorated(true);
                }
            }
        }
    }
    void setClientComponent(Component clientComponent) {
        this.clientComponent = clientComponent;
    }
    void grabCompositionArea(boolean doUpdate) {
        synchronized (compositionAreaLock) {
            if (compositionAreaOwner != this) {
                compositionAreaOwner = this;
                if (compositionArea != null) {
                    compositionArea.setHandlerInfo(this, inputMethodContext);
                }
                if (doUpdate) {
                    if ((composedText != null) && (compositionArea == null)) {
                        createCompositionArea();
                    }
                    if (compositionArea != null) {
                        compositionArea.setText(composedText, caret);
                    }
                }
            }
        }
    }
    void releaseCompositionArea() {
        synchronized (compositionAreaLock) {
            if (compositionAreaOwner == this) {
                compositionAreaOwner = null;
                if (compositionArea != null) {
                    compositionArea.setHandlerInfo(null, null);
                    compositionArea.setText(null, null);
                }
            }
        }
    }
    static void closeCompositionArea() {
        if (compositionArea != null) {
            synchronized (compositionAreaLock) {
                compositionAreaOwner = null;
                compositionArea.setHandlerInfo(null, null);
                compositionArea.setText(null, null);
            }
        }
    }
    boolean isCompositionAreaVisible() {
        if (compositionArea != null) {
            return compositionArea.isCompositionAreaVisible();
        }
        return false;
    }
    void setCompositionAreaVisible(boolean visible) {
        if (compositionArea != null) {
            compositionArea.setCompositionAreaVisible(visible);
        }
    }
    void processInputMethodEvent(InputMethodEvent event) {
        if (event.getID() == InputMethodEvent.INPUT_METHOD_TEXT_CHANGED) {
            inputMethodTextChanged(event);
        } else {
            caretPositionChanged(event);
        }
    }
    void setCompositionAreaUndecorated(boolean undecorated) {
        if (compositionArea != null) {
            compositionArea.setCompositionAreaUndecorated(undecorated);
        }
    }
    private static final Attribute[] IM_ATTRIBUTES =
            { TextAttribute.INPUT_METHOD_HIGHLIGHT };
    public void inputMethodTextChanged(InputMethodEvent event) {
        AttributedCharacterIterator text = event.getText();
        int committedCharacterCount = event.getCommittedCharacterCount();
        composedText = null;
        caret = null;
        if (text != null
                && committedCharacterCount < text.getEndIndex() - text.getBeginIndex()) {
            if (compositionArea == null) {
                 createCompositionArea();
            }
            AttributedString composedTextString;
            composedTextString = new AttributedString(text,
                    text.getBeginIndex() + committedCharacterCount, 
                    text.getEndIndex(), IM_ATTRIBUTES);
            composedTextString.addAttribute(TextAttribute.FONT, compositionArea.getFont());
            composedText = composedTextString.getIterator();
            caret = event.getCaret();
        }
        if (compositionArea != null) {
            compositionArea.setText(composedText, caret);
        }
        if (committedCharacterCount > 0) {
            inputMethodContext.dispatchCommittedText(((Component) event.getSource()),
                                                     text, committedCharacterCount);
            if (isCompositionAreaVisible()) {
                compositionArea.updateWindowLocation();
            }
        }
        event.consume();
    }
    public void caretPositionChanged(InputMethodEvent event) {
        if (compositionArea != null) {
            compositionArea.setCaret(event.getCaret());
        }
        event.consume();
    }
    InputMethodRequests getClientInputMethodRequests() {
        if (clientComponent != null) {
            return (InputMethodRequests) clientComponent.getInputMethodRequests();
        }
        return null;
    }
    public Rectangle getTextLocation(TextHitInfo offset) {
        synchronized (compositionAreaLock) {
            if (compositionAreaOwner == this && isCompositionAreaVisible()) {
                return compositionArea.getTextLocation(offset);
            } else if (composedText != null) {
                return new Rectangle(0, 0, 0, 10);
            } else {
                InputMethodRequests requests = getClientInputMethodRequests();
                if (requests != null) {
                    return requests.getTextLocation(offset);
                } else {
                    return new Rectangle(0, 0, 0, 10);
                }
            }
        }
    }
    public TextHitInfo getLocationOffset(int x, int y) {
        synchronized (compositionAreaLock) {
            if (compositionAreaOwner == this && isCompositionAreaVisible()) {
                return compositionArea.getLocationOffset(x, y);
            } else {
                return null;
            }
        }
    }
    public int getInsertPositionOffset() {
        InputMethodRequests req = getClientInputMethodRequests();
        if (req != null) {
            return req.getInsertPositionOffset();
        }
        return 0;
    }
    private static final AttributedCharacterIterator EMPTY_TEXT =
            (new AttributedString("")).getIterator();
    public AttributedCharacterIterator getCommittedText(int beginIndex,
                                                       int endIndex,
                                                       Attribute[] attributes) {
        InputMethodRequests req = getClientInputMethodRequests();
        if(req != null) {
            return req.getCommittedText(beginIndex, endIndex, attributes);
        }
        return EMPTY_TEXT;
    }
    public int getCommittedTextLength() {
        InputMethodRequests req = getClientInputMethodRequests();
        if(req != null) {
            return req.getCommittedTextLength();
        }
        return 0;
    }
    public AttributedCharacterIterator cancelLatestCommittedText(Attribute[] attributes) {
        InputMethodRequests req = getClientInputMethodRequests();
        if(req != null) {
            return req.cancelLatestCommittedText(attributes);
        }
        return null;
    }
    public AttributedCharacterIterator getSelectedText(Attribute[] attributes) {
        InputMethodRequests req = getClientInputMethodRequests();
        if(req != null) {
            return req.getSelectedText(attributes);
        }
        return EMPTY_TEXT;
    }
}
