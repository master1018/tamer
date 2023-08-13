public class JLabel extends JComponent implements SwingConstants, Accessible
{
    private static final String uiClassID = "LabelUI";
    private int mnemonic = '\0';
    private int mnemonicIndex = -1;
    private String text = "";         
    private Icon defaultIcon = null;
    private Icon disabledIcon = null;
    private boolean disabledIconSet = false;
    private int verticalAlignment = CENTER;
    private int horizontalAlignment = LEADING;
    private int verticalTextPosition = CENTER;
    private int horizontalTextPosition = TRAILING;
    private int iconTextGap = 4;
    protected Component labelFor = null;
    static final String LABELED_BY_PROPERTY = "labeledBy";
    public JLabel(String text, Icon icon, int horizontalAlignment) {
        setText(text);
        setIcon(icon);
        setHorizontalAlignment(horizontalAlignment);
        updateUI();
        setAlignmentX(LEFT_ALIGNMENT);
    }
    public JLabel(String text, int horizontalAlignment) {
        this(text, null, horizontalAlignment);
    }
    public JLabel(String text) {
        this(text, null, LEADING);
    }
    public JLabel(Icon image, int horizontalAlignment) {
        this(null, image, horizontalAlignment);
    }
    public JLabel(Icon image) {
        this(null, image, CENTER);
    }
    public JLabel() {
        this("", null, LEADING);
    }
    public LabelUI getUI() {
        return (LabelUI)ui;
    }
    public void setUI(LabelUI ui) {
        super.setUI(ui);
        if (!disabledIconSet && disabledIcon != null) {
            setDisabledIcon(null);
        }
    }
    public void updateUI() {
        setUI((LabelUI)UIManager.getUI(this));
    }
    public String getUIClassID() {
        return uiClassID;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        String oldAccessibleName = null;
        if (accessibleContext != null) {
            oldAccessibleName = accessibleContext.getAccessibleName();
        }
        String oldValue = this.text;
        this.text = text;
        firePropertyChange("text", oldValue, text);
        setDisplayedMnemonicIndex(
                      SwingUtilities.findDisplayedMnemonicIndex(
                                          text, getDisplayedMnemonic()));
        if ((accessibleContext != null)
            && (accessibleContext.getAccessibleName() != oldAccessibleName)) {
                accessibleContext.firePropertyChange(
                        AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                        oldAccessibleName,
                        accessibleContext.getAccessibleName());
        }
        if (text == null || oldValue == null || !text.equals(oldValue)) {
            revalidate();
            repaint();
        }
    }
    public Icon getIcon() {
        return defaultIcon;
    }
    public void setIcon(Icon icon) {
        Icon oldValue = defaultIcon;
        defaultIcon = icon;
        if ((defaultIcon != oldValue) && !disabledIconSet) {
            disabledIcon = null;
        }
        firePropertyChange("icon", oldValue, defaultIcon);
        if ((accessibleContext != null) && (oldValue != defaultIcon)) {
                accessibleContext.firePropertyChange(
                        AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                        oldValue, defaultIcon);
        }
        if (defaultIcon != oldValue) {
            if ((defaultIcon == null) ||
                (oldValue == null) ||
                (defaultIcon.getIconWidth() != oldValue.getIconWidth()) ||
                (defaultIcon.getIconHeight() != oldValue.getIconHeight())) {
                revalidate();
            }
            repaint();
        }
    }
    @Transient
    public Icon getDisabledIcon() {
        if (!disabledIconSet && disabledIcon == null && defaultIcon != null) {
            disabledIcon = UIManager.getLookAndFeel().getDisabledIcon(this, defaultIcon);
            if (disabledIcon != null) {
                firePropertyChange("disabledIcon", null, disabledIcon);
            }
        }
        return disabledIcon;
    }
    public void setDisabledIcon(Icon disabledIcon) {
        Icon oldValue = this.disabledIcon;
        this.disabledIcon = disabledIcon;
        disabledIconSet = (disabledIcon != null);
        firePropertyChange("disabledIcon", oldValue, disabledIcon);
        if (disabledIcon != oldValue) {
            if (disabledIcon == null || oldValue == null ||
                disabledIcon.getIconWidth() != oldValue.getIconWidth() ||
                disabledIcon.getIconHeight() != oldValue.getIconHeight()) {
                revalidate();
            }
            if (!isEnabled()) {
                repaint();
            }
        }
    }
    public void setDisplayedMnemonic(int key) {
        int oldKey = mnemonic;
        mnemonic = key;
        firePropertyChange("displayedMnemonic", oldKey, mnemonic);
        setDisplayedMnemonicIndex(
            SwingUtilities.findDisplayedMnemonicIndex(getText(), mnemonic));
        if (key != oldKey) {
            revalidate();
            repaint();
        }
    }
    public void setDisplayedMnemonic(char aChar) {
        int vk = java.awt.event.KeyEvent.getExtendedKeyCodeForChar(aChar);
        if (vk != java.awt.event.KeyEvent.VK_UNDEFINED) {
            setDisplayedMnemonic(vk);
        }
    }
    public int getDisplayedMnemonic() {
        return mnemonic;
    }
    public void setDisplayedMnemonicIndex(int index)
                                             throws IllegalArgumentException {
        int oldValue = mnemonicIndex;
        if (index == -1) {
            mnemonicIndex = -1;
        } else {
            String text = getText();
            int textLength = (text == null) ? 0 : text.length();
            if (index < -1 || index >= textLength) {  
                throw new IllegalArgumentException("index == " + index);
            }
        }
        mnemonicIndex = index;
        firePropertyChange("displayedMnemonicIndex", oldValue, index);
        if (index != oldValue) {
            revalidate();
            repaint();
        }
    }
    public int getDisplayedMnemonicIndex() {
        return mnemonicIndex;
    }
    protected int checkHorizontalKey(int key, String message) {
        if ((key == LEFT) ||
            (key == CENTER) ||
            (key == RIGHT) ||
            (key == LEADING) ||
            (key == TRAILING)) {
            return key;
        }
        else {
            throw new IllegalArgumentException(message);
        }
    }
    protected int checkVerticalKey(int key, String message) {
        if ((key == TOP) || (key == CENTER) || (key == BOTTOM)) {
            return key;
        }
        else {
            throw new IllegalArgumentException(message);
        }
    }
    public int getIconTextGap() {
        return iconTextGap;
    }
    public void setIconTextGap(int iconTextGap) {
        int oldValue = this.iconTextGap;
        this.iconTextGap = iconTextGap;
        firePropertyChange("iconTextGap", oldValue, iconTextGap);
        if (iconTextGap != oldValue) {
            revalidate();
            repaint();
        }
    }
    public int getVerticalAlignment() {
        return verticalAlignment;
    }
    public void setVerticalAlignment(int alignment) {
        if (alignment == verticalAlignment) return;
        int oldValue = verticalAlignment;
        verticalAlignment = checkVerticalKey(alignment, "verticalAlignment");
        firePropertyChange("verticalAlignment", oldValue, verticalAlignment);
        repaint();
    }
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }
    public void setHorizontalAlignment(int alignment) {
        if (alignment == horizontalAlignment) return;
        int oldValue = horizontalAlignment;
        horizontalAlignment = checkHorizontalKey(alignment,
                                                 "horizontalAlignment");
        firePropertyChange("horizontalAlignment",
                           oldValue, horizontalAlignment);
        repaint();
    }
    public int getVerticalTextPosition() {
        return verticalTextPosition;
    }
    public void setVerticalTextPosition(int textPosition) {
        if (textPosition == verticalTextPosition) return;
        int old = verticalTextPosition;
        verticalTextPosition = checkVerticalKey(textPosition,
                                                "verticalTextPosition");
        firePropertyChange("verticalTextPosition", old, verticalTextPosition);
        revalidate();
        repaint();
    }
    public int getHorizontalTextPosition() {
        return horizontalTextPosition;
    }
    public void setHorizontalTextPosition(int textPosition) {
        int old = horizontalTextPosition;
        this.horizontalTextPosition = checkHorizontalKey(textPosition,
                                                "horizontalTextPosition");
        firePropertyChange("horizontalTextPosition",
                           old, horizontalTextPosition);
        revalidate();
        repaint();
    }
    public boolean imageUpdate(Image img, int infoflags,
                               int x, int y, int w, int h) {
        if (!isShowing() ||
            !SwingUtilities.doesIconReferenceImage(getIcon(), img) &&
            !SwingUtilities.doesIconReferenceImage(disabledIcon, img)) {
            return false;
        }
        return super.imageUpdate(img, infoflags, x, y, w, h);
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
    protected String paramString() {
        String textString = (text != null ?
                             text : "");
        String defaultIconString = ((defaultIcon != null)
                                    && (defaultIcon != this)  ?
                                    defaultIcon.toString() : "");
        String disabledIconString = ((disabledIcon != null)
                                     && (disabledIcon != this) ?
                                     disabledIcon.toString() : "");
        String labelForString = (labelFor  != null ?
                                 labelFor.toString() : "");
        String verticalAlignmentString;
        if (verticalAlignment == TOP) {
            verticalAlignmentString = "TOP";
        } else if (verticalAlignment == CENTER) {
            verticalAlignmentString = "CENTER";
        } else if (verticalAlignment == BOTTOM) {
            verticalAlignmentString = "BOTTOM";
        } else verticalAlignmentString = "";
        String horizontalAlignmentString;
        if (horizontalAlignment == LEFT) {
            horizontalAlignmentString = "LEFT";
        } else if (horizontalAlignment == CENTER) {
            horizontalAlignmentString = "CENTER";
        } else if (horizontalAlignment == RIGHT) {
            horizontalAlignmentString = "RIGHT";
        } else if (horizontalAlignment == LEADING) {
            horizontalAlignmentString = "LEADING";
        } else if (horizontalAlignment == TRAILING) {
            horizontalAlignmentString = "TRAILING";
        } else horizontalAlignmentString = "";
        String verticalTextPositionString;
        if (verticalTextPosition == TOP) {
            verticalTextPositionString = "TOP";
        } else if (verticalTextPosition == CENTER) {
            verticalTextPositionString = "CENTER";
        } else if (verticalTextPosition == BOTTOM) {
            verticalTextPositionString = "BOTTOM";
        } else verticalTextPositionString = "";
        String horizontalTextPositionString;
        if (horizontalTextPosition == LEFT) {
            horizontalTextPositionString = "LEFT";
        } else if (horizontalTextPosition == CENTER) {
            horizontalTextPositionString = "CENTER";
        } else if (horizontalTextPosition == RIGHT) {
            horizontalTextPositionString = "RIGHT";
        } else if (horizontalTextPosition == LEADING) {
            horizontalTextPositionString = "LEADING";
        } else if (horizontalTextPosition == TRAILING) {
            horizontalTextPositionString = "TRAILING";
        } else horizontalTextPositionString = "";
        return super.paramString() +
        ",defaultIcon=" + defaultIconString +
        ",disabledIcon=" + disabledIconString +
        ",horizontalAlignment=" + horizontalAlignmentString +
        ",horizontalTextPosition=" + horizontalTextPositionString +
        ",iconTextGap=" + iconTextGap +
        ",labelFor=" + labelForString +
        ",text=" + textString +
        ",verticalAlignment=" + verticalAlignmentString +
        ",verticalTextPosition=" + verticalTextPositionString;
    }
    public Component getLabelFor() {
        return labelFor;
    }
    public void setLabelFor(Component c) {
        Component oldC = labelFor;
        labelFor = c;
        firePropertyChange("labelFor", oldC, c);
        if (oldC instanceof JComponent) {
            ((JComponent)oldC).putClientProperty(LABELED_BY_PROPERTY, null);
        }
        if (c instanceof JComponent) {
            ((JComponent)c).putClientProperty(LABELED_BY_PROPERTY, this);
        }
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJLabel();
        }
        return accessibleContext;
    }
    protected class AccessibleJLabel extends AccessibleJComponent
        implements AccessibleText, AccessibleExtendedComponent {
        public String getAccessibleName() {
            String name = accessibleName;
            if (name == null) {
                name = (String)getClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY);
            }
            if (name == null) {
                name = JLabel.this.getText();
            }
            if (name == null) {
                name = super.getAccessibleName();
            }
            return name;
        }
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.LABEL;
        }
        public AccessibleIcon [] getAccessibleIcon() {
            Icon icon = getIcon();
            if (icon instanceof Accessible) {
                AccessibleContext ac =
                ((Accessible)icon).getAccessibleContext();
                if (ac != null && ac instanceof AccessibleIcon) {
                    return new AccessibleIcon[] { (AccessibleIcon)ac };
                }
            }
            return null;
        }
        public AccessibleRelationSet getAccessibleRelationSet() {
            AccessibleRelationSet relationSet
                = super.getAccessibleRelationSet();
            if (!relationSet.contains(AccessibleRelation.LABEL_FOR)) {
                Component c = JLabel.this.getLabelFor();
                if (c != null) {
                    AccessibleRelation relation
                        = new AccessibleRelation(AccessibleRelation.LABEL_FOR);
                    relation.setTarget(c);
                    relationSet.add(relation);
                }
            }
            return relationSet;
        }
        public AccessibleText getAccessibleText() {
            View view = (View)JLabel.this.getClientProperty("html");
            if (view != null) {
                return this;
            } else {
                return null;
            }
        }
        public int getIndexAtPoint(Point p) {
            View view = (View) JLabel.this.getClientProperty("html");
            if (view != null) {
                Rectangle r = getTextRectangle();
                if (r == null) {
                    return -1;
                }
                Rectangle2D.Float shape =
                    new Rectangle2D.Float(r.x, r.y, r.width, r.height);
                Position.Bias bias[] = new Position.Bias[1];
                return view.viewToModel(p.x, p.y, shape, bias);
            } else {
                return -1;
            }
        }
        public Rectangle getCharacterBounds(int i) {
            View view = (View) JLabel.this.getClientProperty("html");
            if (view != null) {
                Rectangle r = getTextRectangle();
        if (r == null) {
            return null;
        }
                Rectangle2D.Float shape =
                    new Rectangle2D.Float(r.x, r.y, r.width, r.height);
                try {
                    Shape charShape =
                        view.modelToView(i, shape, Position.Bias.Forward);
                    return charShape.getBounds();
                } catch (BadLocationException e) {
                    return null;
                }
            } else {
                return null;
            }
        }
        public int getCharCount() {
            View view = (View) JLabel.this.getClientProperty("html");
            if (view != null) {
                Document d = view.getDocument();
                if (d instanceof StyledDocument) {
                    StyledDocument doc = (StyledDocument)d;
                    return doc.getLength();
                }
            }
            return accessibleContext.getAccessibleName().length();
        }
        public int getCaretPosition() {
            return -1;
        }
        public String getAtIndex(int part, int index) {
            if (index < 0 || index >= getCharCount()) {
                return null;
            }
            switch (part) {
            case AccessibleText.CHARACTER:
                try {
                    return getText(index, 1);
                } catch (BadLocationException e) {
                    return null;
                }
            case AccessibleText.WORD:
                try {
                    String s = getText(0, getCharCount());
                    BreakIterator words = BreakIterator.getWordInstance(getLocale());
                    words.setText(s);
                    int end = words.following(index);
                    return s.substring(words.previous(), end);
                } catch (BadLocationException e) {
                    return null;
                }
            case AccessibleText.SENTENCE:
                try {
                    String s = getText(0, getCharCount());
                    BreakIterator sentence =
                        BreakIterator.getSentenceInstance(getLocale());
                    sentence.setText(s);
                    int end = sentence.following(index);
                    return s.substring(sentence.previous(), end);
                } catch (BadLocationException e) {
                    return null;
                }
            default:
                return null;
            }
        }
        public String getAfterIndex(int part, int index) {
            if (index < 0 || index >= getCharCount()) {
                return null;
            }
            switch (part) {
            case AccessibleText.CHARACTER:
                if (index+1 >= getCharCount()) {
                   return null;
                }
                try {
                    return getText(index+1, 1);
                } catch (BadLocationException e) {
                    return null;
                }
            case AccessibleText.WORD:
                try {
                    String s = getText(0, getCharCount());
                    BreakIterator words = BreakIterator.getWordInstance(getLocale());
                    words.setText(s);
                    int start = words.following(index);
                    if (start == BreakIterator.DONE || start >= s.length()) {
                        return null;
                    }
                    int end = words.following(start);
                    if (end == BreakIterator.DONE || end >= s.length()) {
                        return null;
                    }
                    return s.substring(start, end);
                } catch (BadLocationException e) {
                    return null;
                }
            case AccessibleText.SENTENCE:
                try {
                    String s = getText(0, getCharCount());
                    BreakIterator sentence =
                        BreakIterator.getSentenceInstance(getLocale());
                    sentence.setText(s);
                    int start = sentence.following(index);
                    if (start == BreakIterator.DONE || start > s.length()) {
                        return null;
                    }
                    int end = sentence.following(start);
                    if (end == BreakIterator.DONE || end > s.length()) {
                        return null;
                    }
                    return s.substring(start, end);
                } catch (BadLocationException e) {
                    return null;
                }
            default:
                return null;
            }
        }
        public String getBeforeIndex(int part, int index) {
            if (index < 0 || index > getCharCount()-1) {
                return null;
            }
            switch (part) {
            case AccessibleText.CHARACTER:
                if (index == 0) {
                    return null;
                }
                try {
                    return getText(index-1, 1);
                } catch (BadLocationException e) {
                    return null;
                }
            case AccessibleText.WORD:
                try {
                    String s = getText(0, getCharCount());
                    BreakIterator words = BreakIterator.getWordInstance(getLocale());
                    words.setText(s);
                    int end = words.following(index);
                    end = words.previous();
                    int start = words.previous();
                    if (start == BreakIterator.DONE) {
                        return null;
                    }
                    return s.substring(start, end);
                } catch (BadLocationException e) {
                    return null;
                }
            case AccessibleText.SENTENCE:
                try {
                    String s = getText(0, getCharCount());
                    BreakIterator sentence =
                        BreakIterator.getSentenceInstance(getLocale());
                    sentence.setText(s);
                    int end = sentence.following(index);
                    end = sentence.previous();
                    int start = sentence.previous();
                    if (start == BreakIterator.DONE) {
                        return null;
                    }
                    return s.substring(start, end);
                } catch (BadLocationException e) {
                    return null;
                }
            default:
                return null;
            }
        }
        public AttributeSet getCharacterAttribute(int i) {
            View view = (View) JLabel.this.getClientProperty("html");
            if (view != null) {
                Document d = view.getDocument();
                if (d instanceof StyledDocument) {
                    StyledDocument doc = (StyledDocument)d;
                    Element elem = doc.getCharacterElement(i);
                    if (elem != null) {
                        return elem.getAttributes();
                    }
                }
            }
            return null;
        }
        public int getSelectionStart() {
            return -1;
        }
        public int getSelectionEnd() {
            return -1;
        }
        public String getSelectedText() {
            return null;
        }
        private String getText(int offset, int length)
            throws BadLocationException {
            View view = (View) JLabel.this.getClientProperty("html");
            if (view != null) {
                Document d = view.getDocument();
                if (d instanceof StyledDocument) {
                    StyledDocument doc = (StyledDocument)d;
                    return doc.getText(offset, length);
                }
            }
            return null;
        }
        private Rectangle getTextRectangle() {
            String text = JLabel.this.getText();
            Icon icon = (JLabel.this.isEnabled()) ? JLabel.this.getIcon() : JLabel.this.getDisabledIcon();
            if ((icon == null) && (text == null)) {
                return null;
            }
            Rectangle paintIconR = new Rectangle();
            Rectangle paintTextR = new Rectangle();
            Rectangle paintViewR = new Rectangle();
            Insets paintViewInsets = new Insets(0, 0, 0, 0);
            paintViewInsets = JLabel.this.getInsets(paintViewInsets);
            paintViewR.x = paintViewInsets.left;
            paintViewR.y = paintViewInsets.top;
            paintViewR.width = JLabel.this.getWidth() - (paintViewInsets.left + paintViewInsets.right);
            paintViewR.height = JLabel.this.getHeight() - (paintViewInsets.top + paintViewInsets.bottom);
            String clippedText = SwingUtilities.layoutCompoundLabel(
                (JComponent)JLabel.this,
                getFontMetrics(getFont()),
                text,
                icon,
                JLabel.this.getVerticalAlignment(),
                JLabel.this.getHorizontalAlignment(),
                JLabel.this.getVerticalTextPosition(),
                JLabel.this.getHorizontalTextPosition(),
                paintViewR,
                paintIconR,
                paintTextR,
                JLabel.this.getIconTextGap());
            return paintTextR;
        }
        AccessibleExtendedComponent getAccessibleExtendedComponent() {
            return this;
        }
        public String getToolTipText() {
            return JLabel.this.getToolTipText();
        }
        public String getTitledBorderText() {
            return super.getTitledBorderText();
        }
        public AccessibleKeyBinding getAccessibleKeyBinding() {
            int mnemonic = JLabel.this.getDisplayedMnemonic();
            if (mnemonic == 0) {
                return null;
            }
            return new LabelKeyBinding(mnemonic);
        }
        class LabelKeyBinding implements AccessibleKeyBinding {
            int mnemonic;
            LabelKeyBinding(int mnemonic) {
                this.mnemonic = mnemonic;
            }
            public int getAccessibleKeyBindingCount() {
                return 1;
            }
            public java.lang.Object getAccessibleKeyBinding(int i) {
                if (i != 0) {
                    throw new IllegalArgumentException();
                }
                return KeyStroke.getKeyStroke(mnemonic, 0);
            }
        }
    }  
}
