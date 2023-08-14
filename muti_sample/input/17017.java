public class DefaultStyledDocument extends AbstractDocument implements StyledDocument {
    public DefaultStyledDocument(Content c, StyleContext styles) {
        super(c, styles);
        listeningStyles = new Vector<Style>();
        buffer = new ElementBuffer(createDefaultRoot());
        Style defaultStyle = styles.getStyle(StyleContext.DEFAULT_STYLE);
        setLogicalStyle(0, defaultStyle);
    }
    public DefaultStyledDocument(StyleContext styles) {
        this(new GapContent(BUFFER_SIZE_DEFAULT), styles);
    }
    public DefaultStyledDocument() {
        this(new GapContent(BUFFER_SIZE_DEFAULT), new StyleContext());
    }
    public Element getDefaultRootElement() {
        return buffer.getRootElement();
    }
    protected void create(ElementSpec[] data) {
        try {
            if (getLength() != 0) {
                remove(0, getLength());
            }
            writeLock();
            Content c = getContent();
            int n = data.length;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                ElementSpec es = data[i];
                if (es.getLength() > 0) {
                    sb.append(es.getArray(), es.getOffset(),  es.getLength());
                }
            }
            UndoableEdit cEdit = c.insertString(0, sb.toString());
            int length = sb.length();
            DefaultDocumentEvent evnt =
                new DefaultDocumentEvent(0, length, DocumentEvent.EventType.INSERT);
            evnt.addEdit(cEdit);
            buffer.create(length, data, evnt);
            super.insertUpdate(evnt, null);
            evnt.end();
            fireInsertUpdate(evnt);
            fireUndoableEditUpdate(new UndoableEditEvent(this, evnt));
        } catch (BadLocationException ble) {
            throw new StateInvariantError("problem initializing");
        } finally {
            writeUnlock();
        }
    }
    protected void insert(int offset, ElementSpec[] data) throws BadLocationException {
        if (data == null || data.length == 0) {
            return;
        }
        try {
            writeLock();
            Content c = getContent();
            int n = data.length;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                ElementSpec es = data[i];
                if (es.getLength() > 0) {
                    sb.append(es.getArray(), es.getOffset(),  es.getLength());
                }
            }
            if (sb.length() == 0) {
                return;
            }
            UndoableEdit cEdit = c.insertString(offset, sb.toString());
            int length = sb.length();
            DefaultDocumentEvent evnt =
                new DefaultDocumentEvent(offset, length, DocumentEvent.EventType.INSERT);
            evnt.addEdit(cEdit);
            buffer.insert(offset, length, data, evnt);
            super.insertUpdate(evnt, null);
            evnt.end();
            fireInsertUpdate(evnt);
            fireUndoableEditUpdate(new UndoableEditEvent(this, evnt));
        } finally {
            writeUnlock();
        }
    }
    public void removeElement(Element elem) {
        try {
            writeLock();
            removeElementImpl(elem);
        } finally {
            writeUnlock();
        }
    }
    private void removeElementImpl(Element elem) {
        if (elem.getDocument() != this) {
            throw new IllegalArgumentException("element doesn't belong to document");
        }
        BranchElement parent = (BranchElement) elem.getParentElement();
        if (parent == null) {
            throw new IllegalArgumentException("can't remove the root element");
        }
        int startOffset = elem.getStartOffset();
        int removeFrom = startOffset;
        int endOffset = elem.getEndOffset();
        int removeTo = endOffset;
        int lastEndOffset = getLength() + 1;
        Content content = getContent();
        boolean atEnd = false;
        boolean isComposedText = Utilities.isComposedTextElement(elem);
        if (endOffset >= lastEndOffset) {
            if (startOffset <= 0) {
                throw new IllegalArgumentException("can't remove the whole content");
            }
            removeTo = lastEndOffset - 1; 
            try {
                if (content.getString(startOffset - 1, 1).charAt(0) == '\n') {
                    removeFrom--; 
                }
            } catch (BadLocationException ble) { 
                throw new IllegalStateException(ble);
            }
            atEnd = true;
        }
        int length = removeTo - removeFrom;
        DefaultDocumentEvent dde = new DefaultDocumentEvent(removeFrom,
                length, DefaultDocumentEvent.EventType.REMOVE);
        UndoableEdit ue = null;
        while (parent.getElementCount() == 1) {
            elem = parent;
            parent = (BranchElement) parent.getParentElement();
            if (parent == null) { 
                throw new IllegalStateException("invalid element structure");
            }
        }
        Element[] removed = { elem };
        Element[] added = {};
        int index = parent.getElementIndex(startOffset);
        parent.replace(index, 1, added);
        dde.addEdit(new ElementEdit(parent, index, removed, added));
        if (length > 0) {
            try {
                ue = content.remove(removeFrom, length);
                if (ue != null) {
                    dde.addEdit(ue);
                }
            } catch (BadLocationException ble) {
                throw new IllegalStateException(ble);
            }
            lastEndOffset -= length;
        }
        if (atEnd) {
            Element prevLeaf = parent.getElement(parent.getElementCount() - 1);
            while ((prevLeaf != null) && !prevLeaf.isLeaf()) {
                prevLeaf = prevLeaf.getElement(prevLeaf.getElementCount() - 1);
            }
            if (prevLeaf == null) { 
                throw new IllegalStateException("invalid element structure");
            }
            int prevStartOffset = prevLeaf.getStartOffset();
            BranchElement prevParent = (BranchElement) prevLeaf.getParentElement();
            int prevIndex = prevParent.getElementIndex(prevStartOffset);
            Element newElem;
            newElem = createLeafElement(prevParent, prevLeaf.getAttributes(),
                                            prevStartOffset, lastEndOffset);
            Element[] prevRemoved = { prevLeaf };
            Element[] prevAdded = { newElem };
            prevParent.replace(prevIndex, 1, prevAdded);
            dde.addEdit(new ElementEdit(prevParent, prevIndex,
                                                    prevRemoved, prevAdded));
        }
        postRemoveUpdate(dde);
        dde.end();
        fireRemoveUpdate(dde);
        if (! (isComposedText && (ue != null))) {
            fireUndoableEditUpdate(new UndoableEditEvent(this, dde));
        }
    }
    public Style addStyle(String nm, Style parent) {
        StyleContext styles = (StyleContext) getAttributeContext();
        return styles.addStyle(nm, parent);
    }
    public void removeStyle(String nm) {
        StyleContext styles = (StyleContext) getAttributeContext();
        styles.removeStyle(nm);
    }
    public Style getStyle(String nm) {
        StyleContext styles = (StyleContext) getAttributeContext();
        return styles.getStyle(nm);
    }
    public Enumeration<?> getStyleNames() {
        return ((StyleContext) getAttributeContext()).getStyleNames();
    }
    public void setLogicalStyle(int pos, Style s) {
        Element paragraph = getParagraphElement(pos);
        if ((paragraph != null) && (paragraph instanceof AbstractElement)) {
            try {
                writeLock();
                StyleChangeUndoableEdit edit = new StyleChangeUndoableEdit((AbstractElement)paragraph, s);
                ((AbstractElement)paragraph).setResolveParent(s);
                int p0 = paragraph.getStartOffset();
                int p1 = paragraph.getEndOffset();
                DefaultDocumentEvent e =
                  new DefaultDocumentEvent(p0, p1 - p0, DocumentEvent.EventType.CHANGE);
                e.addEdit(edit);
                e.end();
                fireChangedUpdate(e);
                fireUndoableEditUpdate(new UndoableEditEvent(this, e));
            } finally {
                writeUnlock();
            }
        }
    }
    public Style getLogicalStyle(int p) {
        Style s = null;
        Element paragraph = getParagraphElement(p);
        if (paragraph != null) {
            AttributeSet a = paragraph.getAttributes();
            AttributeSet parent = a.getResolveParent();
            if (parent instanceof Style) {
                s = (Style) parent;
            }
        }
        return s;
    }
    public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
        if (length == 0) {
            return;
        }
        try {
            writeLock();
            DefaultDocumentEvent changes =
                new DefaultDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE);
            buffer.change(offset, length, changes);
            AttributeSet sCopy = s.copyAttributes();
            int lastEnd;
            for (int pos = offset; pos < (offset + length); pos = lastEnd) {
                Element run = getCharacterElement(pos);
                lastEnd = run.getEndOffset();
                if (pos == lastEnd) {
                    break;
                }
                MutableAttributeSet attr = (MutableAttributeSet) run.getAttributes();
                changes.addEdit(new AttributeUndoableEdit(run, sCopy, replace));
                if (replace) {
                    attr.removeAttributes(attr);
                }
                attr.addAttributes(s);
            }
            changes.end();
            fireChangedUpdate(changes);
            fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
        } finally {
            writeUnlock();
        }
    }
    public void setParagraphAttributes(int offset, int length, AttributeSet s,
                                       boolean replace) {
        try {
            writeLock();
            DefaultDocumentEvent changes =
                new DefaultDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE);
            AttributeSet sCopy = s.copyAttributes();
            Element section = getDefaultRootElement();
            int index0 = section.getElementIndex(offset);
            int index1 = section.getElementIndex(offset + ((length > 0) ? length - 1 : 0));
            boolean isI18N = Boolean.TRUE.equals(getProperty(I18NProperty));
            boolean hasRuns = false;
            for (int i = index0; i <= index1; i++) {
                Element paragraph = section.getElement(i);
                MutableAttributeSet attr = (MutableAttributeSet) paragraph.getAttributes();
                changes.addEdit(new AttributeUndoableEdit(paragraph, sCopy, replace));
                if (replace) {
                    attr.removeAttributes(attr);
                }
                attr.addAttributes(s);
                if (isI18N && !hasRuns) {
                    hasRuns = (attr.getAttribute(TextAttribute.RUN_DIRECTION) != null);
                }
            }
            if (hasRuns) {
                updateBidi( changes );
            }
            changes.end();
            fireChangedUpdate(changes);
            fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
        } finally {
            writeUnlock();
        }
    }
    public Element getParagraphElement(int pos) {
        Element e;
        for (e = getDefaultRootElement(); ! e.isLeaf(); ) {
            int index = e.getElementIndex(pos);
            e = e.getElement(index);
        }
        if(e != null)
            return e.getParentElement();
        return e;
    }
    public Element getCharacterElement(int pos) {
        Element e;
        for (e = getDefaultRootElement(); ! e.isLeaf(); ) {
            int index = e.getElementIndex(pos);
            e = e.getElement(index);
        }
        return e;
    }
    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
        int offset = chng.getOffset();
        int length = chng.getLength();
        if (attr == null) {
            attr = SimpleAttributeSet.EMPTY;
        }
        Element paragraph = getParagraphElement(offset + length);
        AttributeSet pattr = paragraph.getAttributes();
        Element pParagraph = getParagraphElement(offset);
        Element run = pParagraph.getElement(pParagraph.getElementIndex
                                            (offset));
        int endOffset = offset + length;
        boolean insertingAtBoundry = (run.getEndOffset() == endOffset);
        AttributeSet cattr = run.getAttributes();
        try {
            Segment s = new Segment();
            Vector<ElementSpec> parseBuffer = new Vector<ElementSpec>();
            ElementSpec lastStartSpec = null;
            boolean insertingAfterNewline = false;
            short lastStartDirection = ElementSpec.OriginateDirection;
            if (offset > 0) {
                getText(offset - 1, 1, s);
                if (s.array[s.offset] == '\n') {
                    insertingAfterNewline = true;
                    lastStartDirection = createSpecsForInsertAfterNewline
                                  (paragraph, pParagraph, pattr, parseBuffer,
                                   offset, endOffset);
                    for(int counter = parseBuffer.size() - 1; counter >= 0;
                        counter--) {
                        ElementSpec spec = parseBuffer.elementAt(counter);
                        if(spec.getType() == ElementSpec.StartTagType) {
                            lastStartSpec = spec;
                            break;
                        }
                    }
                }
            }
            if(!insertingAfterNewline)
                pattr = pParagraph.getAttributes();
            getText(offset, length, s);
            char[] txt = s.array;
            int n = s.offset + s.count;
            int lastOffset = s.offset;
            for (int i = s.offset; i < n; i++) {
                if (txt[i] == '\n') {
                    int breakOffset = i + 1;
                    parseBuffer.addElement(
                        new ElementSpec(attr, ElementSpec.ContentType,
                                               breakOffset - lastOffset));
                    parseBuffer.addElement(
                        new ElementSpec(null, ElementSpec.EndTagType));
                    lastStartSpec = new ElementSpec(pattr, ElementSpec.
                                                   StartTagType);
                    parseBuffer.addElement(lastStartSpec);
                    lastOffset = breakOffset;
                }
            }
            if (lastOffset < n) {
                parseBuffer.addElement(
                    new ElementSpec(attr, ElementSpec.ContentType,
                                           n - lastOffset));
            }
            ElementSpec first = parseBuffer.firstElement();
            int docLength = getLength();
            if(first.getType() == ElementSpec.ContentType &&
               cattr.isEqual(attr)) {
                first.setDirection(ElementSpec.JoinPreviousDirection);
            }
            if(lastStartSpec != null) {
                if(insertingAfterNewline) {
                    lastStartSpec.setDirection(lastStartDirection);
                }
                else if(pParagraph.getEndOffset() != endOffset) {
                    lastStartSpec.setDirection(ElementSpec.
                                               JoinFractureDirection);
                }
                else {
                    Element parent = pParagraph.getParentElement();
                    int pParagraphIndex = parent.getElementIndex(offset);
                    if((pParagraphIndex + 1) < parent.getElementCount() &&
                       !parent.getElement(pParagraphIndex + 1).isLeaf()) {
                        lastStartSpec.setDirection(ElementSpec.
                                                   JoinNextDirection);
                    }
                }
            }
            if(insertingAtBoundry && endOffset < docLength) {
                ElementSpec last = parseBuffer.lastElement();
                if(last.getType() == ElementSpec.ContentType &&
                   last.getDirection() != ElementSpec.JoinPreviousDirection &&
                   ((lastStartSpec == null && (paragraph == pParagraph ||
                                               insertingAfterNewline)) ||
                    (lastStartSpec != null && lastStartSpec.getDirection() !=
                     ElementSpec.OriginateDirection))) {
                    Element nextRun = paragraph.getElement(paragraph.
                                           getElementIndex(endOffset));
                    if(nextRun.isLeaf() &&
                       attr.isEqual(nextRun.getAttributes())) {
                        last.setDirection(ElementSpec.JoinNextDirection);
                    }
                }
            }
            else if(!insertingAtBoundry && lastStartSpec != null &&
                    lastStartSpec.getDirection() ==
                    ElementSpec.JoinFractureDirection) {
                ElementSpec last = parseBuffer.lastElement();
                if(last.getType() == ElementSpec.ContentType &&
                   last.getDirection() != ElementSpec.JoinPreviousDirection &&
                   attr.isEqual(cattr)) {
                    last.setDirection(ElementSpec.JoinNextDirection);
                }
            }
            if (Utilities.isComposedTextAttributeDefined(attr)) {
                MutableAttributeSet mattr = (MutableAttributeSet) attr;
                mattr.addAttributes(cattr);
                mattr.addAttribute(AbstractDocument.ElementNameAttribute,
                        AbstractDocument.ContentElementName);
                mattr.addAttribute(StyleConstants.NameAttribute,
                        AbstractDocument.ContentElementName);
                if (mattr.isDefined(IMPLIED_CR)) {
                    mattr.removeAttribute(IMPLIED_CR);
                }
            }
            ElementSpec[] spec = new ElementSpec[parseBuffer.size()];
            parseBuffer.copyInto(spec);
            buffer.insert(offset, length, spec, chng);
        } catch (BadLocationException bl) {
        }
        super.insertUpdate( chng, attr );
    }
    short createSpecsForInsertAfterNewline(Element paragraph,
            Element pParagraph, AttributeSet pattr, Vector<ElementSpec> parseBuffer,
                                                 int offset, int endOffset) {
        if(paragraph.getParentElement() == pParagraph.getParentElement()) {
            ElementSpec spec = new ElementSpec(pattr, ElementSpec.EndTagType);
            parseBuffer.addElement(spec);
            spec = new ElementSpec(pattr, ElementSpec.StartTagType);
            parseBuffer.addElement(spec);
            if(pParagraph.getEndOffset() != endOffset)
                return ElementSpec.JoinFractureDirection;
            Element parent = pParagraph.getParentElement();
            if((parent.getElementIndex(offset) + 1) < parent.getElementCount())
                return ElementSpec.JoinNextDirection;
        }
        else {
            Vector<Element> leftParents = new Vector<Element>();
            Vector<Element> rightParents = new Vector<Element>();
            Element e = pParagraph;
            while(e != null) {
                leftParents.addElement(e);
                e = e.getParentElement();
            }
            e = paragraph;
            int leftIndex = -1;
            while(e != null && (leftIndex = leftParents.indexOf(e)) == -1) {
                rightParents.addElement(e);
                e = e.getParentElement();
            }
            if(e != null) {
                for(int counter = 0; counter < leftIndex;
                    counter++) {
                    parseBuffer.addElement(new ElementSpec
                                              (null, ElementSpec.EndTagType));
                }
                ElementSpec spec;
                for(int counter = rightParents.size() - 1;
                    counter >= 0; counter--) {
                    spec = new ElementSpec(rightParents.elementAt(counter).getAttributes(),
                                   ElementSpec.StartTagType);
                    if(counter > 0)
                        spec.setDirection(ElementSpec.JoinNextDirection);
                    parseBuffer.addElement(spec);
                }
                if(rightParents.size() > 0)
                    return ElementSpec.JoinNextDirection;
                return ElementSpec.JoinFractureDirection;
            }
        }
        return ElementSpec.OriginateDirection;
    }
    protected void removeUpdate(DefaultDocumentEvent chng) {
        super.removeUpdate(chng);
        buffer.remove(chng.getOffset(), chng.getLength(), chng);
    }
    protected AbstractElement createDefaultRoot() {
        writeLock();
        BranchElement section = new SectionElement();
        BranchElement paragraph = new BranchElement(section, null);
        LeafElement brk = new LeafElement(paragraph, null, 0, 1);
        Element[] buff = new Element[1];
        buff[0] = brk;
        paragraph.replace(0, 0, buff);
        buff[0] = paragraph;
        section.replace(0, 0, buff);
        writeUnlock();
        return section;
    }
    public Color getForeground(AttributeSet attr) {
        StyleContext styles = (StyleContext) getAttributeContext();
        return styles.getForeground(attr);
    }
    public Color getBackground(AttributeSet attr) {
        StyleContext styles = (StyleContext) getAttributeContext();
        return styles.getBackground(attr);
    }
    public Font getFont(AttributeSet attr) {
        StyleContext styles = (StyleContext) getAttributeContext();
        return styles.getFont(attr);
    }
    protected void styleChanged(Style style) {
        if (getLength() != 0) {
            if (updateRunnable == null) {
                updateRunnable = new ChangeUpdateRunnable();
            }
            synchronized(updateRunnable) {
                if (!updateRunnable.isPending) {
                    SwingUtilities.invokeLater(updateRunnable);
                    updateRunnable.isPending = true;
                }
            }
        }
    }
    public void addDocumentListener(DocumentListener listener) {
        synchronized(listeningStyles) {
            int oldDLCount = listenerList.getListenerCount
                                          (DocumentListener.class);
            super.addDocumentListener(listener);
            if (oldDLCount == 0) {
                if (styleContextChangeListener == null) {
                    styleContextChangeListener =
                                      createStyleContextChangeListener();
                }
                if (styleContextChangeListener != null) {
                    StyleContext styles = (StyleContext)getAttributeContext();
                    List<ChangeListener> staleListeners =
                        AbstractChangeHandler.getStaleListeners(styleContextChangeListener);
                    for (ChangeListener l: staleListeners) {
                        styles.removeChangeListener(l);
                    }
                    styles.addChangeListener(styleContextChangeListener);
                }
                updateStylesListeningTo();
            }
        }
    }
    public void removeDocumentListener(DocumentListener listener) {
        synchronized(listeningStyles) {
            super.removeDocumentListener(listener);
            if (listenerList.getListenerCount(DocumentListener.class) == 0) {
                for (int counter = listeningStyles.size() - 1; counter >= 0;
                     counter--) {
                    listeningStyles.elementAt(counter).
                                    removeChangeListener(styleChangeListener);
                }
                listeningStyles.removeAllElements();
                if (styleContextChangeListener != null) {
                    StyleContext styles = (StyleContext)getAttributeContext();
                    styles.removeChangeListener(styleContextChangeListener);
                }
            }
        }
    }
    ChangeListener createStyleChangeListener() {
        return new StyleChangeHandler(this);
    }
    ChangeListener createStyleContextChangeListener() {
        return new StyleContextChangeHandler(this);
    }
    void updateStylesListeningTo() {
        synchronized(listeningStyles) {
            StyleContext styles = (StyleContext)getAttributeContext();
            if (styleChangeListener == null) {
                styleChangeListener = createStyleChangeListener();
            }
            if (styleChangeListener != null && styles != null) {
                Enumeration styleNames = styles.getStyleNames();
                Vector v = (Vector)listeningStyles.clone();
                listeningStyles.removeAllElements();
                List<ChangeListener> staleListeners =
                    AbstractChangeHandler.getStaleListeners(styleChangeListener);
                while (styleNames.hasMoreElements()) {
                    String name = (String)styleNames.nextElement();
                    Style aStyle = styles.getStyle(name);
                    int index = v.indexOf(aStyle);
                    listeningStyles.addElement(aStyle);
                    if (index == -1) {
                        for (ChangeListener l: staleListeners) {
                            aStyle.removeChangeListener(l);
                        }
                        aStyle.addChangeListener(styleChangeListener);
                    }
                    else {
                        v.removeElementAt(index);
                    }
                }
                for (int counter = v.size() - 1; counter >= 0; counter--) {
                    Style aStyle = (Style)v.elementAt(counter);
                    aStyle.removeChangeListener(styleChangeListener);
                }
                if (listeningStyles.size() == 0) {
                    styleChangeListener = null;
                }
            }
        }
    }
    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException {
        listeningStyles = new Vector<Style>();
        s.defaultReadObject();
        if (styleContextChangeListener == null &&
            listenerList.getListenerCount(DocumentListener.class) > 0) {
            styleContextChangeListener = createStyleContextChangeListener();
            if (styleContextChangeListener != null) {
                StyleContext styles = (StyleContext)getAttributeContext();
                styles.addChangeListener(styleContextChangeListener);
            }
            updateStylesListeningTo();
        }
    }
    public static final int BUFFER_SIZE_DEFAULT = 4096;
    protected ElementBuffer buffer;
    private transient Vector<Style> listeningStyles;
    private transient ChangeListener styleChangeListener;
    private transient ChangeListener styleContextChangeListener;
    private transient ChangeUpdateRunnable updateRunnable;
    protected class SectionElement extends BranchElement {
        public SectionElement() {
            super(null, null);
        }
        public String getName() {
            return SectionElementName;
        }
    }
    public static class ElementSpec {
        public static final short StartTagType = 1;
        public static final short EndTagType = 2;
        public static final short ContentType = 3;
        public static final short JoinPreviousDirection = 4;
        public static final short JoinNextDirection = 5;
        public static final short OriginateDirection = 6;
        public static final short JoinFractureDirection = 7;
        public ElementSpec(AttributeSet a, short type) {
            this(a, type, null, 0, 0);
        }
        public ElementSpec(AttributeSet a, short type, int len) {
            this(a, type, null, 0, len);
        }
        public ElementSpec(AttributeSet a, short type, char[] txt,
                                  int offs, int len) {
            attr = a;
            this.type = type;
            this.data = txt;
            this.offs = offs;
            this.len = len;
            this.direction = OriginateDirection;
        }
        public void setType(short type) {
            this.type = type;
        }
        public short getType() {
            return type;
        }
        public void setDirection(short direction) {
            this.direction = direction;
        }
        public short getDirection() {
            return direction;
        }
        public AttributeSet getAttributes() {
            return attr;
        }
        public char[] getArray() {
            return data;
        }
        public int getOffset() {
            return offs;
        }
        public int getLength() {
            return len;
        }
        public String toString() {
            String tlbl = "??";
            String plbl = "??";
            switch(type) {
            case StartTagType:
                tlbl = "StartTag";
                break;
            case ContentType:
                tlbl = "Content";
                break;
            case EndTagType:
                tlbl = "EndTag";
                break;
            }
            switch(direction) {
            case JoinPreviousDirection:
                plbl = "JoinPrevious";
                break;
            case JoinNextDirection:
                plbl = "JoinNext";
                break;
            case OriginateDirection:
                plbl = "Originate";
                break;
            case JoinFractureDirection:
                plbl = "Fracture";
                break;
            }
            return tlbl + ":" + plbl + ":" + getLength();
        }
        private AttributeSet attr;
        private int len;
        private short type;
        private short direction;
        private int offs;
        private char[] data;
    }
    public class ElementBuffer implements Serializable {
        public ElementBuffer(Element root) {
            this.root = root;
            changes = new Vector<ElemChanges>();
            path = new Stack<ElemChanges>();
        }
        public Element getRootElement() {
            return root;
        }
        public void insert(int offset, int length, ElementSpec[] data,
                                 DefaultDocumentEvent de) {
            if (length == 0) {
                return;
            }
            insertOp = true;
            beginEdits(offset, length);
            insertUpdate(data);
            endEdits(de);
            insertOp = false;
        }
        void create(int length, ElementSpec[] data, DefaultDocumentEvent de) {
            insertOp = true;
            beginEdits(offset, length);
            Element elem = root;
            int index = elem.getElementIndex(0);
            while (! elem.isLeaf()) {
                Element child = elem.getElement(index);
                push(elem, index);
                elem = child;
                index = elem.getElementIndex(0);
            }
            ElemChanges ec = path.peek();
            Element child = ec.parent.getElement(ec.index);
            ec.added.addElement(createLeafElement(ec.parent,
                                child.getAttributes(), getLength(),
                                child.getEndOffset()));
            ec.removed.addElement(child);
            while (path.size() > 1) {
                pop();
            }
            int n = data.length;
            AttributeSet newAttrs = null;
            if (n > 0 && data[0].getType() == ElementSpec.StartTagType) {
                newAttrs = data[0].getAttributes();
            }
            if (newAttrs == null) {
                newAttrs = SimpleAttributeSet.EMPTY;
            }
            MutableAttributeSet attr = (MutableAttributeSet)root.
                                       getAttributes();
            de.addEdit(new AttributeUndoableEdit(root, newAttrs, true));
            attr.removeAttributes(attr);
            attr.addAttributes(newAttrs);
            for (int i = 1; i < n; i++) {
                insertElement(data[i]);
            }
            while (path.size() != 0) {
                pop();
            }
            endEdits(de);
            insertOp = false;
        }
        public void remove(int offset, int length, DefaultDocumentEvent de) {
            beginEdits(offset, length);
            removeUpdate();
            endEdits(de);
        }
        public void change(int offset, int length, DefaultDocumentEvent de) {
            beginEdits(offset, length);
            changeUpdate();
            endEdits(de);
        }
        protected void insertUpdate(ElementSpec[] data) {
            Element elem = root;
            int index = elem.getElementIndex(offset);
            while (! elem.isLeaf()) {
                Element child = elem.getElement(index);
                push(elem, (child.isLeaf() ? index : index+1));
                elem = child;
                index = elem.getElementIndex(offset);
            }
            insertPath = new ElemChanges[path.size()];
            path.copyInto(insertPath);
            createdFracture = false;
            int i;
            recreateLeafs = false;
            if(data[0].getType() == ElementSpec.ContentType) {
                insertFirstContent(data);
                pos += data[0].getLength();
                i = 1;
            }
            else {
                fractureDeepestLeaf(data);
                i = 0;
            }
            int n = data.length;
            for (; i < n; i++) {
                insertElement(data[i]);
            }
            if(!createdFracture)
                fracture(-1);
            while (path.size() != 0) {
                pop();
            }
            if(offsetLastIndex && offsetLastIndexOnReplace) {
                insertPath[insertPath.length - 1].index++;
            }
            for(int counter = insertPath.length - 1; counter >= 0;
                counter--) {
                ElemChanges change = insertPath[counter];
                if(change.parent == fracturedParent)
                    change.added.addElement(fracturedChild);
                if((change.added.size() > 0 ||
                    change.removed.size() > 0) && !changes.contains(change)) {
                    changes.addElement(change);
                }
            }
            if (offset == 0 && fracturedParent != null &&
                data[0].getType() == ElementSpec.EndTagType) {
                int counter = 0;
                while (counter < data.length &&
                       data[counter].getType() == ElementSpec.EndTagType) {
                    counter++;
                }
                ElemChanges change = insertPath[insertPath.length -
                                               counter - 1];
                change.removed.insertElementAt(change.parent.getElement
                                               (--change.index), 0);
            }
        }
        protected void removeUpdate() {
            removeElements(root, offset, offset + length);
        }
        protected void changeUpdate() {
            boolean didEnd = split(offset, length);
            if (! didEnd) {
                while (path.size() != 0) {
                    pop();
                }
                split(offset + length, 0);
            }
            while (path.size() != 0) {
                pop();
            }
        }
        boolean split(int offs, int len) {
            boolean splitEnd = false;
            Element e = root;
            int index = e.getElementIndex(offs);
            while (! e.isLeaf()) {
                push(e, index);
                e = e.getElement(index);
                index = e.getElementIndex(offs);
            }
            ElemChanges ec = path.peek();
            Element child = ec.parent.getElement(ec.index);
            if (child.getStartOffset() < offs && offs < child.getEndOffset()) {
                int index0 = ec.index;
                int index1 = index0;
                if (((offs + len) < ec.parent.getEndOffset()) && (len != 0)) {
                    index1 = ec.parent.getElementIndex(offs+len);
                    if (index1 == index0) {
                        ec.removed.addElement(child);
                        e = createLeafElement(ec.parent, child.getAttributes(),
                                              child.getStartOffset(), offs);
                        ec.added.addElement(e);
                        e = createLeafElement(ec.parent, child.getAttributes(),
                                          offs, offs + len);
                        ec.added.addElement(e);
                        e = createLeafElement(ec.parent, child.getAttributes(),
                                              offs + len, child.getEndOffset());
                        ec.added.addElement(e);
                        return true;
                    } else {
                        child = ec.parent.getElement(index1);
                        if ((offs + len) == child.getStartOffset()) {
                            index1 = index0;
                        }
                    }
                    splitEnd = true;
                }
                pos = offs;
                child = ec.parent.getElement(index0);
                ec.removed.addElement(child);
                e = createLeafElement(ec.parent, child.getAttributes(),
                                      child.getStartOffset(), pos);
                ec.added.addElement(e);
                e = createLeafElement(ec.parent, child.getAttributes(),
                                      pos, child.getEndOffset());
                ec.added.addElement(e);
                for (int i = index0 + 1; i < index1; i++) {
                    child = ec.parent.getElement(i);
                    ec.removed.addElement(child);
                    ec.added.addElement(child);
                }
                if (index1 != index0) {
                    child = ec.parent.getElement(index1);
                    pos = offs + len;
                    ec.removed.addElement(child);
                    e = createLeafElement(ec.parent, child.getAttributes(),
                                          child.getStartOffset(), pos);
                    ec.added.addElement(e);
                    e = createLeafElement(ec.parent, child.getAttributes(),
                                          pos, child.getEndOffset());
                    ec.added.addElement(e);
                }
            }
            return splitEnd;
        }
        void endEdits(DefaultDocumentEvent de) {
            int n = changes.size();
            for (int i = 0; i < n; i++) {
                ElemChanges ec = changes.elementAt(i);
                Element[] removed = new Element[ec.removed.size()];
                ec.removed.copyInto(removed);
                Element[] added = new Element[ec.added.size()];
                ec.added.copyInto(added);
                int index = ec.index;
                ((BranchElement) ec.parent).replace(index, removed.length, added);
                ElementEdit ee = new ElementEdit(ec.parent, index, removed, added);
                de.addEdit(ee);
            }
            changes.removeAllElements();
            path.removeAllElements();
        }
        void beginEdits(int offset, int length) {
            this.offset = offset;
            this.length = length;
            this.endOffset = offset + length;
            pos = offset;
            if (changes == null) {
                changes = new Vector<ElemChanges>();
            } else {
                changes.removeAllElements();
            }
            if (path == null) {
                path = new Stack<ElemChanges>();
            } else {
                path.removeAllElements();
            }
            fracturedParent = null;
            fracturedChild = null;
            offsetLastIndex = offsetLastIndexOnReplace = false;
        }
        void push(Element e, int index, boolean isFracture) {
            ElemChanges ec = new ElemChanges(e, index, isFracture);
            path.push(ec);
        }
        void push(Element e, int index) {
            push(e, index, false);
        }
        void pop() {
            ElemChanges ec = path.peek();
            path.pop();
            if ((ec.added.size() > 0) || (ec.removed.size() > 0)) {
                changes.addElement(ec);
            } else if (! path.isEmpty()) {
                Element e = ec.parent;
                if(e.getElementCount() == 0) {
                    ec = path.peek();
                    ec.added.removeElement(e);
                }
            }
        }
        void advance(int n) {
            pos += n;
        }
        void insertElement(ElementSpec es) {
            ElemChanges ec = path.peek();
            switch(es.getType()) {
            case ElementSpec.StartTagType:
                switch(es.getDirection()) {
                case ElementSpec.JoinNextDirection:
                    Element parent = ec.parent.getElement(ec.index);
                    if(parent.isLeaf()) {
                        if((ec.index + 1) < ec.parent.getElementCount())
                            parent = ec.parent.getElement(ec.index + 1);
                        else
                            throw new StateInvariantError("Join next to leaf");
                    }
                    push(parent, 0, true);
                    break;
                case ElementSpec.JoinFractureDirection:
                    if(!createdFracture) {
                        fracture(path.size() - 1);
                    }
                    if(!ec.isFracture) {
                        push(fracturedChild, 0, true);
                    }
                    else
                        push(ec.parent.getElement(0), 0, true);
                    break;
                default:
                    Element belem = createBranchElement(ec.parent,
                                                        es.getAttributes());
                    ec.added.addElement(belem);
                    push(belem, 0);
                    break;
                }
                break;
            case ElementSpec.EndTagType:
                pop();
                break;
            case ElementSpec.ContentType:
              int len = es.getLength();
                if (es.getDirection() != ElementSpec.JoinNextDirection) {
                    Element leaf = createLeafElement(ec.parent, es.getAttributes(),
                                                     pos, pos + len);
                    ec.added.addElement(leaf);
                }
                else {
                    if(!ec.isFracture) {
                        Element first = null;
                        if(insertPath != null) {
                            for(int counter = insertPath.length - 1;
                                counter >= 0; counter--) {
                                if(insertPath[counter] == ec) {
                                    if(counter != (insertPath.length - 1))
                                        first = ec.parent.getElement(ec.index);
                                    break;
                                }
                            }
                        }
                        if(first == null)
                            first = ec.parent.getElement(ec.index + 1);
                        Element leaf = createLeafElement(ec.parent, first.
                                 getAttributes(), pos, first.getEndOffset());
                        ec.added.addElement(leaf);
                        ec.removed.addElement(first);
                    }
                    else {
                        Element first = ec.parent.getElement(0);
                        Element leaf = createLeafElement(ec.parent, first.
                                 getAttributes(), pos, first.getEndOffset());
                        ec.added.addElement(leaf);
                        ec.removed.addElement(first);
                    }
                }
                pos += len;
                break;
            }
        }
        boolean removeElements(Element elem, int rmOffs0, int rmOffs1) {
            if (! elem.isLeaf()) {
                int index0 = elem.getElementIndex(rmOffs0);
                int index1 = elem.getElementIndex(rmOffs1);
                push(elem, index0);
                ElemChanges ec = path.peek();
                if (index0 == index1) {
                    Element child0 = elem.getElement(index0);
                    if(rmOffs0 <= child0.getStartOffset() &&
                       rmOffs1 >= child0.getEndOffset()) {
                        ec.removed.addElement(child0);
                    }
                    else if(removeElements(child0, rmOffs0, rmOffs1)) {
                        ec.removed.addElement(child0);
                    }
                } else {
                    Element child0 = elem.getElement(index0);
                    Element child1 = elem.getElement(index1);
                    boolean containsOffs1 = (rmOffs1 < elem.getEndOffset());
                    if (containsOffs1 && canJoin(child0, child1)) {
                        for (int i = index0; i <= index1; i++) {
                            ec.removed.addElement(elem.getElement(i));
                        }
                        Element e = join(elem, child0, child1, rmOffs0, rmOffs1);
                        ec.added.addElement(e);
                    } else {
                        int rmIndex0 = index0 + 1;
                        int rmIndex1 = index1 - 1;
                        if (child0.getStartOffset() == rmOffs0 ||
                            (index0 == 0 &&
                             child0.getStartOffset() > rmOffs0 &&
                             child0.getEndOffset() <= rmOffs1)) {
                            child0 = null;
                            rmIndex0 = index0;
                        }
                        if (!containsOffs1) {
                            child1 = null;
                            rmIndex1++;
                        }
                        else if (child1.getStartOffset() == rmOffs1) {
                            child1 = null;
                        }
                        if (rmIndex0 <= rmIndex1) {
                            ec.index = rmIndex0;
                        }
                        for (int i = rmIndex0; i <= rmIndex1; i++) {
                            ec.removed.addElement(elem.getElement(i));
                        }
                        if (child0 != null) {
                            if(removeElements(child0, rmOffs0, rmOffs1)) {
                                ec.removed.insertElementAt(child0, 0);
                                ec.index = index0;
                            }
                        }
                        if (child1 != null) {
                            if(removeElements(child1, rmOffs0, rmOffs1)) {
                                ec.removed.addElement(child1);
                            }
                        }
                    }
                }
                pop();
                if(elem.getElementCount() == (ec.removed.size() -
                                              ec.added.size())) {
                    return true;
                }
            }
            return false;
        }
        boolean canJoin(Element e0, Element e1) {
            if ((e0 == null) || (e1 == null)) {
                return false;
            }
            boolean leaf0 = e0.isLeaf();
            boolean leaf1 = e1.isLeaf();
            if(leaf0 != leaf1) {
                return false;
            }
            if (leaf0) {
                return e0.getAttributes().isEqual(e1.getAttributes());
            }
            String name0 = e0.getName();
            String name1 = e1.getName();
            if (name0 != null) {
                return name0.equals(name1);
            }
            if (name1 != null) {
                return name1.equals(name0);
            }
            return true;
        }
        Element join(Element p, Element left, Element right, int rmOffs0, int rmOffs1) {
            if (left.isLeaf() && right.isLeaf()) {
                return createLeafElement(p, left.getAttributes(), left.getStartOffset(),
                                         right.getEndOffset());
            } else if ((!left.isLeaf()) && (!right.isLeaf())) {
                Element to = createBranchElement(p, left.getAttributes());
                int ljIndex = left.getElementIndex(rmOffs0);
                int rjIndex = right.getElementIndex(rmOffs1);
                Element lj = left.getElement(ljIndex);
                if (lj.getStartOffset() >= rmOffs0) {
                    lj = null;
                }
                Element rj = right.getElement(rjIndex);
                if (rj.getStartOffset() == rmOffs1) {
                    rj = null;
                }
                Vector<Element> children = new Vector<Element>();
                for (int i = 0; i < ljIndex; i++) {
                    children.addElement(clone(to, left.getElement(i)));
                }
                if (canJoin(lj, rj)) {
                    Element e = join(to, lj, rj, rmOffs0, rmOffs1);
                    children.addElement(e);
                } else {
                    if (lj != null) {
                        children.addElement(cloneAsNecessary(to, lj, rmOffs0, rmOffs1));
                    }
                    if (rj != null) {
                        children.addElement(cloneAsNecessary(to, rj, rmOffs0, rmOffs1));
                    }
                }
                int n = right.getElementCount();
                for (int i = (rj == null) ? rjIndex : rjIndex + 1; i < n; i++) {
                    children.addElement(clone(to, right.getElement(i)));
                }
                Element[] c = new Element[children.size()];
                children.copyInto(c);
                ((BranchElement)to).replace(0, 0, c);
                return to;
            } else {
                throw new StateInvariantError(
                    "No support to join leaf element with non-leaf element");
            }
        }
        public Element clone(Element parent, Element clonee) {
            if (clonee.isLeaf()) {
                return createLeafElement(parent, clonee.getAttributes(),
                                         clonee.getStartOffset(),
                                         clonee.getEndOffset());
            }
            Element e = createBranchElement(parent, clonee.getAttributes());
            int n = clonee.getElementCount();
            Element[] children = new Element[n];
            for (int i = 0; i < n; i++) {
                children[i] = clone(e, clonee.getElement(i));
            }
            ((BranchElement)e).replace(0, 0, children);
            return e;
        }
        Element cloneAsNecessary(Element parent, Element clonee, int rmOffs0, int rmOffs1) {
            if (clonee.isLeaf()) {
                return createLeafElement(parent, clonee.getAttributes(),
                                         clonee.getStartOffset(),
                                         clonee.getEndOffset());
            }
            Element e = createBranchElement(parent, clonee.getAttributes());
            int n = clonee.getElementCount();
            ArrayList<Element> childrenList = new ArrayList<Element>(n);
            for (int i = 0; i < n; i++) {
                Element elem = clonee.getElement(i);
                if (elem.getStartOffset() < rmOffs0 || elem.getEndOffset() > rmOffs1) {
                    childrenList.add(cloneAsNecessary(e, elem, rmOffs0, rmOffs1));
                }
            }
            Element[] children = new Element[childrenList.size()];
            children = childrenList.toArray(children);
            ((BranchElement)e).replace(0, 0, children);
            return e;
        }
        void fracture(int depth) {
            int cLength = insertPath.length;
            int lastIndex = -1;
            boolean needRecreate = recreateLeafs;
            ElemChanges lastChange = insertPath[cLength - 1];
            boolean childAltered = ((lastChange.index + 1) <
                                    lastChange.parent.getElementCount());
            int deepestAlteredIndex = (needRecreate) ? cLength : -1;
            int lastAlteredIndex = cLength - 1;
            createdFracture = true;
            for(int counter = cLength - 2; counter >= 0; counter--) {
                ElemChanges change = insertPath[counter];
                if(change.added.size() > 0 || counter == depth) {
                    lastIndex = counter;
                    if(!needRecreate && childAltered) {
                        needRecreate = true;
                        if(deepestAlteredIndex == -1)
                            deepestAlteredIndex = lastAlteredIndex + 1;
                    }
                }
                if(!childAltered && change.index <
                   change.parent.getElementCount()) {
                    childAltered = true;
                    lastAlteredIndex = counter;
                }
            }
            if(needRecreate) {
                if(lastIndex == -1)
                    lastIndex = cLength - 1;
                fractureFrom(insertPath, lastIndex, deepestAlteredIndex);
            }
        }
        void fractureFrom(ElemChanges[] changed, int startIndex,
                          int endFractureIndex) {
            ElemChanges change = changed[startIndex];
            Element child;
            Element newChild;
            int changeLength = changed.length;
            if((startIndex + 1) == changeLength)
                child = change.parent.getElement(change.index);
            else
                child = change.parent.getElement(change.index - 1);
            if(child.isLeaf()) {
                newChild = createLeafElement(change.parent,
                               child.getAttributes(), Math.max(endOffset,
                               child.getStartOffset()), child.getEndOffset());
            }
            else {
                newChild = createBranchElement(change.parent,
                                               child.getAttributes());
            }
            fracturedParent = change.parent;
            fracturedChild = newChild;
            Element parent = newChild;
            while(++startIndex < endFractureIndex) {
                boolean isEnd = ((startIndex + 1) == endFractureIndex);
                boolean isEndLeaf = ((startIndex + 1) == changeLength);
                change = changed[startIndex];
                if(isEnd) {
                    if(offsetLastIndex || !isEndLeaf)
                        child = null;
                    else
                        child = change.parent.getElement(change.index);
                }
                else {
                    child = change.parent.getElement(change.index - 1);
                }
                if(child != null) {
                    if(child.isLeaf()) {
                        newChild = createLeafElement(parent,
                               child.getAttributes(), Math.max(endOffset,
                               child.getStartOffset()), child.getEndOffset());
                    }
                    else {
                        newChild = createBranchElement(parent,
                                                   child.getAttributes());
                    }
                }
                else
                    newChild = null;
                int kidsToMove = change.parent.getElementCount() -
                                 change.index;
                Element[] kids;
                int moveStartIndex;
                int kidStartIndex = 1;
                if(newChild == null) {
                    if(isEndLeaf) {
                        kidsToMove--;
                        moveStartIndex = change.index + 1;
                    }
                    else {
                        moveStartIndex = change.index;
                    }
                    kidStartIndex = 0;
                    kids = new Element[kidsToMove];
                }
                else {
                    if(!isEnd) {
                        kidsToMove++;
                        moveStartIndex = change.index;
                    }
                    else {
                        moveStartIndex = change.index + 1;
                    }
                    kids = new Element[kidsToMove];
                    kids[0] = newChild;
                }
                for(int counter = kidStartIndex; counter < kidsToMove;
                    counter++) {
                    Element toMove =change.parent.getElement(moveStartIndex++);
                    kids[counter] = recreateFracturedElement(parent, toMove);
                    change.removed.addElement(toMove);
                }
                ((BranchElement)parent).replace(0, 0, kids);
                parent = newChild;
            }
        }
        Element recreateFracturedElement(Element parent, Element toDuplicate) {
            if(toDuplicate.isLeaf()) {
                return createLeafElement(parent, toDuplicate.getAttributes(),
                                         Math.max(toDuplicate.getStartOffset(),
                                                  endOffset),
                                         toDuplicate.getEndOffset());
            }
            Element newParent = createBranchElement(parent, toDuplicate.
                                                    getAttributes());
            int childCount = toDuplicate.getElementCount();
            Element[] newKids = new Element[childCount];
            for(int counter = 0; counter < childCount; counter++) {
                newKids[counter] = recreateFracturedElement(newParent,
                                             toDuplicate.getElement(counter));
            }
            ((BranchElement)newParent).replace(0, 0, newKids);
            return newParent;
        }
        void fractureDeepestLeaf(ElementSpec[] specs) {
            ElemChanges ec = path.peek();
            Element child = ec.parent.getElement(ec.index);
            if (offset != 0) {
                Element newChild = createLeafElement(ec.parent,
                                                 child.getAttributes(),
                                                 child.getStartOffset(),
                                                 offset);
                ec.added.addElement(newChild);
            }
            ec.removed.addElement(child);
            if(child.getEndOffset() != endOffset)
                recreateLeafs = true;
            else
                offsetLastIndex = true;
        }
        void insertFirstContent(ElementSpec[] specs) {
            ElementSpec firstSpec = specs[0];
            ElemChanges ec = path.peek();
            Element child = ec.parent.getElement(ec.index);
            int firstEndOffset = offset + firstSpec.getLength();
            boolean isOnlyContent = (specs.length == 1);
            switch(firstSpec.getDirection()) {
            case ElementSpec.JoinPreviousDirection:
                if(child.getEndOffset() != firstEndOffset &&
                    !isOnlyContent) {
                    Element newE = createLeafElement(ec.parent,
                            child.getAttributes(), child.getStartOffset(),
                            firstEndOffset);
                    ec.added.addElement(newE);
                    ec.removed.addElement(child);
                    if(child.getEndOffset() != endOffset)
                        recreateLeafs = true;
                    else
                        offsetLastIndex = true;
                }
                else {
                    offsetLastIndex = true;
                    offsetLastIndexOnReplace = true;
                }
                break;
            case ElementSpec.JoinNextDirection:
                if(offset != 0) {
                    Element newE = createLeafElement(ec.parent,
                            child.getAttributes(), child.getStartOffset(),
                            offset);
                    ec.added.addElement(newE);
                    Element nextChild = ec.parent.getElement(ec.index + 1);
                    if(isOnlyContent)
                        newE = createLeafElement(ec.parent, nextChild.
                            getAttributes(), offset, nextChild.getEndOffset());
                    else
                        newE = createLeafElement(ec.parent, nextChild.
                            getAttributes(), offset, firstEndOffset);
                    ec.added.addElement(newE);
                    ec.removed.addElement(child);
                    ec.removed.addElement(nextChild);
                }
                break;
            default:
                if(child.getStartOffset() != offset) {
                    Element newE = createLeafElement(ec.parent,
                            child.getAttributes(), child.getStartOffset(),
                            offset);
                    ec.added.addElement(newE);
                }
                ec.removed.addElement(child);
                Element newE = createLeafElement(ec.parent,
                                                 firstSpec.getAttributes(),
                                                 offset, firstEndOffset);
                ec.added.addElement(newE);
                if(child.getEndOffset() != endOffset) {
                    recreateLeafs = true;
                }
                else {
                    offsetLastIndex = true;
                }
                break;
            }
        }
        Element root;
        transient int pos;          
        transient int offset;
        transient int length;
        transient int endOffset;
        transient Vector<ElemChanges> changes;
        transient Stack<ElemChanges> path;
        transient boolean insertOp;
        transient boolean recreateLeafs; 
        transient ElemChanges[] insertPath;
        transient boolean createdFracture;
        transient Element fracturedParent;
        transient Element fracturedChild;
        transient boolean offsetLastIndex;
        transient boolean offsetLastIndexOnReplace;
        class ElemChanges {
            ElemChanges(Element parent, int index, boolean isFracture) {
                this.parent = parent;
                this.index = index;
                this.isFracture = isFracture;
                added = new Vector<Element>();
                removed = new Vector<Element>();
            }
            public String toString() {
                return "added: " + added + "\nremoved: " + removed + "\n";
            }
            Element parent;
            int index;
            Vector<Element> added;
            Vector<Element> removed;
            boolean isFracture;
        }
    }
    public static class AttributeUndoableEdit extends AbstractUndoableEdit {
        public AttributeUndoableEdit(Element element, AttributeSet newAttributes,
                              boolean isReplacing) {
            super();
            this.element = element;
            this.newAttributes = newAttributes;
            this.isReplacing = isReplacing;
            copy = element.getAttributes().copyAttributes();
        }
        public void redo() throws CannotRedoException {
            super.redo();
            MutableAttributeSet as = (MutableAttributeSet)element
                                     .getAttributes();
            if(isReplacing)
                as.removeAttributes(as);
            as.addAttributes(newAttributes);
        }
        public void undo() throws CannotUndoException {
            super.undo();
            MutableAttributeSet as = (MutableAttributeSet)element.getAttributes();
            as.removeAttributes(as);
            as.addAttributes(copy);
        }
        protected AttributeSet newAttributes;
        protected AttributeSet copy;
        protected boolean isReplacing;
        protected Element element;
    }
    static class StyleChangeUndoableEdit extends AbstractUndoableEdit {
        public StyleChangeUndoableEdit(AbstractElement element,
                                       Style newStyle) {
            super();
            this.element = element;
            this.newStyle = newStyle;
            oldStyle = element.getResolveParent();
        }
        public void redo() throws CannotRedoException {
            super.redo();
            element.setResolveParent(newStyle);
        }
        public void undo() throws CannotUndoException {
            super.undo();
            element.setResolveParent(oldStyle);
        }
        protected AbstractElement element;
        protected Style newStyle;
        protected AttributeSet oldStyle;
    }
    abstract static class AbstractChangeHandler implements ChangeListener {
        private class DocReference extends WeakReference<DefaultStyledDocument> {
            DocReference(DefaultStyledDocument d, ReferenceQueue<DefaultStyledDocument> q) {
                super(d, q);
            }
            ChangeListener getListener() {
                return AbstractChangeHandler.this;
            }
        }
        private final static Map<Class, ReferenceQueue<DefaultStyledDocument>> queueMap
                = new HashMap<Class, ReferenceQueue<DefaultStyledDocument>>();
        private DocReference doc;
        AbstractChangeHandler(DefaultStyledDocument d) {
            Class c = getClass();
            ReferenceQueue<DefaultStyledDocument> q;
            synchronized (queueMap) {
                q = queueMap.get(c);
                if (q == null) {
                    q = new ReferenceQueue<DefaultStyledDocument>();
                    queueMap.put(c, q);
                }
            }
            doc = new DocReference(d, q);
        }
        static List<ChangeListener> getStaleListeners(ChangeListener l) {
            List<ChangeListener> staleListeners = new ArrayList<ChangeListener>();
            ReferenceQueue<DefaultStyledDocument> q = queueMap.get(l.getClass());
            if (q != null) {
                DocReference r;
                synchronized (q) {
                    while ((r = (DocReference) q.poll()) != null) {
                        staleListeners.add(r.getListener());
                    }
                }
            }
            return staleListeners;
        }
        public void stateChanged(ChangeEvent e) {
            DefaultStyledDocument d = doc.get();
            if (d != null) {
                fireStateChanged(d, e);
            }
        }
        abstract void fireStateChanged(DefaultStyledDocument d, ChangeEvent e);
    }
    static class StyleChangeHandler extends AbstractChangeHandler {
        StyleChangeHandler(DefaultStyledDocument d) {
            super(d);
        }
        void fireStateChanged(DefaultStyledDocument d, ChangeEvent e) {
            Object source = e.getSource();
            if (source instanceof Style) {
                d.styleChanged((Style) source);
            } else {
                d.styleChanged(null);
            }
        }
    }
    static class StyleContextChangeHandler extends AbstractChangeHandler {
        StyleContextChangeHandler(DefaultStyledDocument d) {
            super(d);
        }
        void fireStateChanged(DefaultStyledDocument d, ChangeEvent e) {
            d.updateStylesListeningTo();
        }
    }
    class ChangeUpdateRunnable implements Runnable {
        boolean isPending = false;
        public void run() {
            synchronized(this) {
                isPending = false;
            }
            try {
                writeLock();
                DefaultDocumentEvent dde = new DefaultDocumentEvent(0,
                                              getLength(),
                                              DocumentEvent.EventType.CHANGE);
                dde.end();
                fireChangedUpdate(dde);
            } finally {
                writeUnlock();
            }
        }
    }
}
