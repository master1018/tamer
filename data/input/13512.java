public abstract class AbstractDocument implements Document, Serializable {
    protected AbstractDocument(Content data) {
        this(data, StyleContext.getDefaultStyleContext());
    }
    protected AbstractDocument(Content data, AttributeContext context) {
        this.data = data;
        this.context = context;
        bidiRoot = new BidiRootElement();
        if (defaultI18NProperty == null) {
            String o = java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<String>() {
                    public String run() {
                        return System.getProperty(I18NProperty);
                    }
                }
            );
            if (o != null) {
                defaultI18NProperty = Boolean.valueOf(o);
            } else {
                defaultI18NProperty = Boolean.FALSE;
            }
        }
        putProperty( I18NProperty, defaultI18NProperty);
        writeLock();
        try {
            Element[] p = new Element[1];
            p[0] = new BidiElement( bidiRoot, 0, 1, 0 );
            bidiRoot.replace(0,0,p);
        } finally {
            writeUnlock();
        }
    }
    public Dictionary<Object,Object> getDocumentProperties() {
        if (documentProperties == null) {
            documentProperties = new Hashtable<Object, Object>(2);
        }
        return documentProperties;
    }
    public void setDocumentProperties(Dictionary<Object,Object> x) {
        documentProperties = x;
    }
    protected void fireInsertUpdate(DocumentEvent e) {
        notifyingListeners = true;
        try {
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==DocumentListener.class) {
                    ((DocumentListener)listeners[i+1]).insertUpdate(e);
                }
            }
        } finally {
            notifyingListeners = false;
        }
    }
    protected void fireChangedUpdate(DocumentEvent e) {
        notifyingListeners = true;
        try {
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==DocumentListener.class) {
                    ((DocumentListener)listeners[i+1]).changedUpdate(e);
                }
            }
        } finally {
            notifyingListeners = false;
        }
    }
    protected void fireRemoveUpdate(DocumentEvent e) {
        notifyingListeners = true;
        try {
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==DocumentListener.class) {
                    ((DocumentListener)listeners[i+1]).removeUpdate(e);
                }
            }
        } finally {
            notifyingListeners = false;
        }
    }
    protected void fireUndoableEditUpdate(UndoableEditEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==UndoableEditListener.class) {
                ((UndoableEditListener)listeners[i+1]).undoableEditHappened(e);
            }
        }
    }
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        return listenerList.getListeners(listenerType);
    }
    public int getAsynchronousLoadPriority() {
        Integer loadPriority = (Integer)
            getProperty(AbstractDocument.AsyncLoadPriority);
        if (loadPriority != null) {
            return loadPriority.intValue();
        }
        return -1;
    }
    public void setAsynchronousLoadPriority(int p) {
        Integer loadPriority = (p >= 0) ? Integer.valueOf(p) : null;
        putProperty(AbstractDocument.AsyncLoadPriority, loadPriority);
    }
    public void setDocumentFilter(DocumentFilter filter) {
        documentFilter = filter;
    }
    public DocumentFilter getDocumentFilter() {
        return documentFilter;
    }
    public void render(Runnable r) {
        readLock();
        try {
            r.run();
        } finally {
            readUnlock();
        }
    }
    public int getLength() {
        return data.length() - 1;
    }
    public void addDocumentListener(DocumentListener listener) {
        listenerList.add(DocumentListener.class, listener);
    }
    public void removeDocumentListener(DocumentListener listener) {
        listenerList.remove(DocumentListener.class, listener);
    }
    public DocumentListener[] getDocumentListeners() {
        return listenerList.getListeners(DocumentListener.class);
    }
    public void addUndoableEditListener(UndoableEditListener listener) {
        listenerList.add(UndoableEditListener.class, listener);
    }
    public void removeUndoableEditListener(UndoableEditListener listener) {
        listenerList.remove(UndoableEditListener.class, listener);
    }
    public UndoableEditListener[] getUndoableEditListeners() {
        return listenerList.getListeners(UndoableEditListener.class);
    }
    public final Object getProperty(Object key) {
        return getDocumentProperties().get(key);
    }
    public final void putProperty(Object key, Object value) {
        if (value != null) {
            getDocumentProperties().put(key, value);
        } else {
            getDocumentProperties().remove(key);
        }
        if( key == TextAttribute.RUN_DIRECTION
            && Boolean.TRUE.equals(getProperty(I18NProperty)) )
        {
            writeLock();
            try {
                DefaultDocumentEvent e
                    = new DefaultDocumentEvent(0, getLength(),
                                               DocumentEvent.EventType.INSERT);
                updateBidi( e );
            } finally {
                writeUnlock();
            }
        }
    }
    public void remove(int offs, int len) throws BadLocationException {
        DocumentFilter filter = getDocumentFilter();
        writeLock();
        try {
            if (filter != null) {
                filter.remove(getFilterBypass(), offs, len);
            }
            else {
                handleRemove(offs, len);
            }
        } finally {
            writeUnlock();
        }
    }
    void handleRemove(int offs, int len) throws BadLocationException {
        if (len > 0) {
            if (offs < 0 || (offs + len) > getLength()) {
                throw new BadLocationException("Invalid remove",
                                               getLength() + 1);
            }
            DefaultDocumentEvent chng =
                    new DefaultDocumentEvent(offs, len, DocumentEvent.EventType.REMOVE);
            boolean isComposedTextElement;
            isComposedTextElement = Utilities.isComposedTextElement(this, offs);
            removeUpdate(chng);
            UndoableEdit u = data.remove(offs, len);
            if (u != null) {
                chng.addEdit(u);
            }
            postRemoveUpdate(chng);
            chng.end();
            fireRemoveUpdate(chng);
            if ((u != null) && !isComposedTextElement) {
                fireUndoableEditUpdate(new UndoableEditEvent(this, chng));
            }
        }
    }
    public void replace(int offset, int length, String text,
                        AttributeSet attrs) throws BadLocationException {
        if (length == 0 && (text == null || text.length() == 0)) {
            return;
        }
        DocumentFilter filter = getDocumentFilter();
        writeLock();
        try {
            if (filter != null) {
                filter.replace(getFilterBypass(), offset, length, text,
                               attrs);
            }
            else {
                if (length > 0) {
                    remove(offset, length);
                }
                if (text != null && text.length() > 0) {
                    insertString(offset, text, attrs);
                }
            }
        } finally {
            writeUnlock();
        }
    }
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if ((str == null) || (str.length() == 0)) {
            return;
        }
        DocumentFilter filter = getDocumentFilter();
        writeLock();
        try {
            if (filter != null) {
                filter.insertString(getFilterBypass(), offs, str, a);
            }
            else {
                handleInsertString(offs, str, a);
            }
        } finally {
            writeUnlock();
        }
    }
    void handleInsertString(int offs, String str, AttributeSet a)
                         throws BadLocationException {
        if ((str == null) || (str.length() == 0)) {
            return;
        }
        UndoableEdit u = data.insertString(offs, str);
        DefaultDocumentEvent e =
            new DefaultDocumentEvent(offs, str.length(), DocumentEvent.EventType.INSERT);
        if (u != null) {
            e.addEdit(u);
        }
        if( getProperty(I18NProperty).equals( Boolean.FALSE ) ) {
            Object d = getProperty(TextAttribute.RUN_DIRECTION);
            if ((d != null) && (d.equals(TextAttribute.RUN_DIRECTION_RTL))) {
                putProperty( I18NProperty, Boolean.TRUE);
            } else {
                char[] chars = str.toCharArray();
                if (SwingUtilities2.isComplexLayout(chars, 0, chars.length)) {
                    putProperty( I18NProperty, Boolean.TRUE);
                }
            }
        }
        insertUpdate(e, a);
        e.end();
        fireInsertUpdate(e);
        if (u != null &&
            (a == null || !a.isDefined(StyleConstants.ComposedTextAttribute))) {
            fireUndoableEditUpdate(new UndoableEditEvent(this, e));
        }
    }
    public String getText(int offset, int length) throws BadLocationException {
        if (length < 0) {
            throw new BadLocationException("Length must be positive", length);
        }
        String str = data.getString(offset, length);
        return str;
    }
    public void getText(int offset, int length, Segment txt) throws BadLocationException {
        if (length < 0) {
            throw new BadLocationException("Length must be positive", length);
        }
        data.getChars(offset, length, txt);
    }
    public synchronized Position createPosition(int offs) throws BadLocationException {
        return data.createPosition(offs);
    }
    public final Position getStartPosition() {
        Position p;
        try {
            p = createPosition(0);
        } catch (BadLocationException bl) {
            p = null;
        }
        return p;
    }
    public final Position getEndPosition() {
        Position p;
        try {
            p = createPosition(data.length());
        } catch (BadLocationException bl) {
            p = null;
        }
        return p;
    }
    public Element[] getRootElements() {
        Element[] elems = new Element[2];
        elems[0] = getDefaultRootElement();
        elems[1] = getBidiRootElement();
        return elems;
    }
    public abstract Element getDefaultRootElement();
    private DocumentFilter.FilterBypass getFilterBypass() {
        if (filterBypass == null) {
            filterBypass = new DefaultFilterBypass();
        }
        return filterBypass;
    }
    public Element getBidiRootElement() {
        return bidiRoot;
    }
    boolean isLeftToRight(int p0, int p1) {
        if(!getProperty(I18NProperty).equals(Boolean.TRUE)) {
            return true;
        }
        Element bidiRoot = getBidiRootElement();
        int index = bidiRoot.getElementIndex(p0);
        Element bidiElem = bidiRoot.getElement(index);
        if(bidiElem.getEndOffset() >= p1) {
            AttributeSet bidiAttrs = bidiElem.getAttributes();
            return ((StyleConstants.getBidiLevel(bidiAttrs) % 2) == 0);
        }
        return true;
    }
    public abstract Element getParagraphElement(int pos);
    protected final AttributeContext getAttributeContext() {
        return context;
    }
    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
        if( getProperty(I18NProperty).equals( Boolean.TRUE ) )
            updateBidi( chng );
        if (chng.type == DocumentEvent.EventType.INSERT &&
                        chng.getLength() > 0 &&
                        !Boolean.TRUE.equals(getProperty(MultiByteProperty))) {
            Segment segment = SegmentCache.getSharedSegment();
            try {
                getText(chng.getOffset(), chng.getLength(), segment);
                segment.first();
                do {
                    if ((int)segment.current() > 255) {
                        putProperty(MultiByteProperty, Boolean.TRUE);
                        break;
                    }
                } while (segment.next() != Segment.DONE);
            } catch (BadLocationException ble) {
            }
            SegmentCache.releaseSharedSegment(segment);
        }
    }
    protected void removeUpdate(DefaultDocumentEvent chng) {
    }
    protected void postRemoveUpdate(DefaultDocumentEvent chng) {
        if( getProperty(I18NProperty).equals( Boolean.TRUE ) )
            updateBidi( chng );
    }
    void updateBidi( DefaultDocumentEvent chng ) {
        int firstPStart;
        int lastPEnd;
        if( chng.type == DocumentEvent.EventType.INSERT
            || chng.type == DocumentEvent.EventType.CHANGE )
        {
            int chngStart = chng.getOffset();
            int chngEnd =  chngStart + chng.getLength();
            firstPStart = getParagraphElement(chngStart).getStartOffset();
            lastPEnd = getParagraphElement(chngEnd).getEndOffset();
        } else if( chng.type == DocumentEvent.EventType.REMOVE ) {
            Element paragraph = getParagraphElement( chng.getOffset() );
            firstPStart = paragraph.getStartOffset();
            lastPEnd = paragraph.getEndOffset();
        } else {
            throw new Error("Internal error: unknown event type.");
        }
        byte levels[] = calculateBidiLevels( firstPStart, lastPEnd );
        Vector<Element> newElements = new Vector<Element>();
        int firstSpanStart = firstPStart;
        int removeFromIndex = 0;
        if( firstSpanStart > 0 ) {
            int prevElemIndex = bidiRoot.getElementIndex(firstPStart-1);
            removeFromIndex = prevElemIndex;
            Element prevElem = bidiRoot.getElement(prevElemIndex);
            int prevLevel=StyleConstants.getBidiLevel(prevElem.getAttributes());
            if( prevLevel==levels[0] ) {
                firstSpanStart = prevElem.getStartOffset();
            } else if( prevElem.getEndOffset() > firstPStart ) {
                newElements.addElement(new BidiElement(bidiRoot,
                                                       prevElem.getStartOffset(),
                                                       firstPStart, prevLevel));
            } else {
                removeFromIndex++;
            }
        }
        int firstSpanEnd = 0;
        while((firstSpanEnd<levels.length) && (levels[firstSpanEnd]==levels[0]))
            firstSpanEnd++;
        int lastSpanEnd = lastPEnd;
        Element newNextElem = null;
        int removeToIndex = bidiRoot.getElementCount() - 1;
        if( lastSpanEnd <= getLength() ) {
            int nextElemIndex = bidiRoot.getElementIndex( lastPEnd );
            removeToIndex = nextElemIndex;
            Element nextElem = bidiRoot.getElement( nextElemIndex );
            int nextLevel = StyleConstants.getBidiLevel(nextElem.getAttributes());
            if( nextLevel == levels[levels.length-1] ) {
                lastSpanEnd = nextElem.getEndOffset();
            } else if( nextElem.getStartOffset() < lastPEnd ) {
                newNextElem = new BidiElement(bidiRoot, lastPEnd,
                                              nextElem.getEndOffset(),
                                              nextLevel);
            } else {
                removeToIndex--;
            }
        }
        int lastSpanStart = levels.length;
        while( (lastSpanStart>firstSpanEnd)
               && (levels[lastSpanStart-1]==levels[levels.length-1]) )
            lastSpanStart--;
        if((firstSpanEnd==lastSpanStart)&&(levels[0]==levels[levels.length-1])){
            newElements.addElement(new BidiElement(bidiRoot, firstSpanStart,
                                                   lastSpanEnd, levels[0]));
        } else {
            newElements.addElement(new BidiElement(bidiRoot, firstSpanStart,
                                                   firstSpanEnd+firstPStart,
                                                   levels[0]));
            for( int i=firstSpanEnd; i<lastSpanStart; ) {
                int j;
                for( j=i;  (j<levels.length) && (levels[j] == levels[i]); j++ );
                newElements.addElement(new BidiElement(bidiRoot, firstPStart+i,
                                                       firstPStart+j,
                                                       (int)levels[i]));
                i=j;
            }
            newElements.addElement(new BidiElement(bidiRoot,
                                                   lastSpanStart+firstPStart,
                                                   lastSpanEnd,
                                                   levels[levels.length-1]));
        }
        if( newNextElem != null )
            newElements.addElement( newNextElem );
        int removedElemCount = 0;
        if( bidiRoot.getElementCount() > 0 ) {
            removedElemCount = removeToIndex - removeFromIndex + 1;
        }
        Element[] removedElems = new Element[removedElemCount];
        for( int i=0; i<removedElemCount; i++ ) {
            removedElems[i] = bidiRoot.getElement(removeFromIndex+i);
        }
        Element[] addedElems = new Element[ newElements.size() ];
        newElements.copyInto( addedElems );
        ElementEdit ee = new ElementEdit( bidiRoot, removeFromIndex,
                                          removedElems, addedElems );
        chng.addEdit( ee );
        bidiRoot.replace( removeFromIndex, removedElems.length, addedElems );
    }
    private byte[] calculateBidiLevels( int firstPStart, int lastPEnd ) {
        byte levels[] = new byte[ lastPEnd - firstPStart ];
        int  levelsEnd = 0;
        Boolean defaultDirection = null;
        Object d = getProperty(TextAttribute.RUN_DIRECTION);
        if (d instanceof Boolean) {
            defaultDirection = (Boolean) d;
        }
        for(int o=firstPStart; o<lastPEnd; ) {
            Element p = getParagraphElement( o );
            int pStart = p.getStartOffset();
            int pEnd = p.getEndOffset();
            Boolean direction = defaultDirection;
            d = p.getAttributes().getAttribute(TextAttribute.RUN_DIRECTION);
            if (d instanceof Boolean) {
                direction = (Boolean) d;
            }
            Segment seg = SegmentCache.getSharedSegment();
            try {
                getText(pStart, pEnd-pStart, seg);
            } catch (BadLocationException e ) {
                throw new Error("Internal error: " + e.toString());
            }
            Bidi bidiAnalyzer;
            int bidiflag = Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT;
            if (direction != null) {
                if (TextAttribute.RUN_DIRECTION_LTR.equals(direction)) {
                    bidiflag = Bidi.DIRECTION_LEFT_TO_RIGHT;
                } else {
                    bidiflag = Bidi.DIRECTION_RIGHT_TO_LEFT;
                }
            }
            bidiAnalyzer = new Bidi(seg.array, seg.offset, null, 0, seg.count,
                    bidiflag);
            BidiUtils.getLevels(bidiAnalyzer, levels, levelsEnd);
            levelsEnd += bidiAnalyzer.getLength();
            o =  p.getEndOffset();
            SegmentCache.releaseSharedSegment(seg);
        }
        if( levelsEnd != levels.length )
            throw new Error("levelsEnd assertion failed.");
        return levels;
    }
    public void dump(PrintStream out) {
        Element root = getDefaultRootElement();
        if (root instanceof AbstractElement) {
            ((AbstractElement)root).dump(out, 0);
        }
        bidiRoot.dump(out,0);
    }
    protected final Content getContent() {
        return data;
    }
    protected Element createLeafElement(Element parent, AttributeSet a, int p0, int p1) {
        return new LeafElement(parent, a, p0, p1);
    }
    protected Element createBranchElement(Element parent, AttributeSet a) {
        return new BranchElement(parent, a);
    }
    protected synchronized final Thread getCurrentWriter() {
        return currWriter;
    }
    protected synchronized final void writeLock() {
        try {
            while ((numReaders > 0) || (currWriter != null)) {
                if (Thread.currentThread() == currWriter) {
                    if (notifyingListeners) {
                        throw new IllegalStateException(
                                      "Attempt to mutate in notification");
                    }
                    numWriters++;
                    return;
                }
                wait();
            }
            currWriter = Thread.currentThread();
            numWriters = 1;
        } catch (InterruptedException e) {
            throw new Error("Interrupted attempt to aquire write lock");
        }
    }
    protected synchronized final void writeUnlock() {
        if (--numWriters <= 0) {
            numWriters = 0;
            currWriter = null;
            notifyAll();
        }
    }
    public synchronized final void readLock() {
        try {
            while (currWriter != null) {
                if (currWriter == Thread.currentThread()) {
                    return;
                }
                wait();
            }
            numReaders += 1;
        } catch (InterruptedException e) {
            throw new Error("Interrupted attempt to aquire read lock");
        }
    }
    public synchronized final void readUnlock() {
        if (currWriter == Thread.currentThread()) {
            return;
        }
        if (numReaders <= 0) {
            throw new StateInvariantError(BAD_LOCK_STATE);
        }
        numReaders -= 1;
        notify();
    }
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException
    {
        s.defaultReadObject();
        listenerList = new EventListenerList();
        bidiRoot = new BidiRootElement();
        try {
            writeLock();
            Element[] p = new Element[1];
            p[0] = new BidiElement( bidiRoot, 0, 1, 0 );
            bidiRoot.replace(0,0,p);
        } finally {
            writeUnlock();
        }
        s.registerValidation(new ObjectInputValidation() {
            public void validateObject() {
                try {
                    writeLock();
                    DefaultDocumentEvent e = new DefaultDocumentEvent
                                   (0, getLength(),
                                    DocumentEvent.EventType.INSERT);
                    updateBidi( e );
                }
                finally {
                    writeUnlock();
                }
            }
        }, 0);
    }
    private transient int numReaders;
    private transient Thread currWriter;
    private transient int numWriters;
    private transient boolean notifyingListeners;
    private static Boolean defaultI18NProperty;
    private Dictionary<Object,Object> documentProperties = null;
    protected EventListenerList listenerList = new EventListenerList();
    private Content data;
    private AttributeContext context;
    private transient BranchElement bidiRoot;
    private DocumentFilter documentFilter;
    private transient DocumentFilter.FilterBypass filterBypass;
    private static final String BAD_LOCK_STATE = "document lock failure";
    protected static final String BAD_LOCATION = "document location failure";
    public static final String ParagraphElementName = "paragraph";
    public static final String ContentElementName = "content";
    public static final String SectionElementName = "section";
    public static final String BidiElementName = "bidi level";
    public static final String ElementNameAttribute = "$ename";
    static final String I18NProperty = "i18n";
    static final Object MultiByteProperty = "multiByte";
    static final String AsyncLoadPriority = "load priority";
    public interface Content {
        public Position createPosition(int offset) throws BadLocationException;
        public int length();
        public UndoableEdit insertString(int where, String str) throws BadLocationException;
        public UndoableEdit remove(int where, int nitems) throws BadLocationException;
        public String getString(int where, int len) throws BadLocationException;
        public void getChars(int where, int len, Segment txt) throws BadLocationException;
    }
    public interface AttributeContext {
        public AttributeSet addAttribute(AttributeSet old, Object name, Object value);
        public AttributeSet addAttributes(AttributeSet old, AttributeSet attr);
        public AttributeSet removeAttribute(AttributeSet old, Object name);
        public AttributeSet removeAttributes(AttributeSet old, Enumeration<?> names);
        public AttributeSet removeAttributes(AttributeSet old, AttributeSet attrs);
        public AttributeSet getEmptySet();
        public void reclaim(AttributeSet a);
    }
    public abstract class AbstractElement implements Element, MutableAttributeSet, Serializable, TreeNode {
        public AbstractElement(Element parent, AttributeSet a) {
            this.parent = parent;
            attributes = getAttributeContext().getEmptySet();
            if (a != null) {
                addAttributes(a);
            }
        }
        private final void indent(PrintWriter out, int n) {
            for (int i = 0; i < n; i++) {
                out.print("  ");
            }
        }
        public void dump(PrintStream psOut, int indentAmount) {
            PrintWriter out;
            try {
                out = new PrintWriter(new OutputStreamWriter(psOut,"JavaEsc"),
                                      true);
            } catch (UnsupportedEncodingException e){
                out = new PrintWriter(psOut,true);
            }
            indent(out, indentAmount);
            if (getName() == null) {
                out.print("<??");
            } else {
                out.print("<" + getName());
            }
            if (getAttributeCount() > 0) {
                out.println("");
                Enumeration names = attributes.getAttributeNames();
                while (names.hasMoreElements()) {
                    Object name = names.nextElement();
                    indent(out, indentAmount + 1);
                    out.println(name + "=" + getAttribute(name));
                }
                indent(out, indentAmount);
            }
            out.println(">");
            if (isLeaf()) {
                indent(out, indentAmount+1);
                out.print("[" + getStartOffset() + "," + getEndOffset() + "]");
                Content c = getContent();
                try {
                    String contentStr = c.getString(getStartOffset(),
                                                    getEndOffset() - getStartOffset());
                    if (contentStr.length() > 40) {
                        contentStr = contentStr.substring(0, 40) + "...";
                    }
                    out.println("["+contentStr+"]");
                } catch (BadLocationException e) {
                }
            } else {
                int n = getElementCount();
                for (int i = 0; i < n; i++) {
                    AbstractElement e = (AbstractElement) getElement(i);
                    e.dump(psOut, indentAmount+1);
                }
            }
        }
        public int getAttributeCount() {
            return attributes.getAttributeCount();
        }
        public boolean isDefined(Object attrName) {
            return attributes.isDefined(attrName);
        }
        public boolean isEqual(AttributeSet attr) {
            return attributes.isEqual(attr);
        }
        public AttributeSet copyAttributes() {
            return attributes.copyAttributes();
        }
        public Object getAttribute(Object attrName) {
            Object value = attributes.getAttribute(attrName);
            if (value == null) {
                AttributeSet a = (parent != null) ? parent.getAttributes() : null;
                if (a != null) {
                    value = a.getAttribute(attrName);
                }
            }
            return value;
        }
        public Enumeration<?> getAttributeNames() {
            return attributes.getAttributeNames();
        }
        public boolean containsAttribute(Object name, Object value) {
            return attributes.containsAttribute(name, value);
        }
        public boolean containsAttributes(AttributeSet attrs) {
            return attributes.containsAttributes(attrs);
        }
        public AttributeSet getResolveParent() {
            AttributeSet a = attributes.getResolveParent();
            if ((a == null) && (parent != null)) {
                a = parent.getAttributes();
            }
            return a;
        }
        public void addAttribute(Object name, Object value) {
            checkForIllegalCast();
            AttributeContext context = getAttributeContext();
            attributes = context.addAttribute(attributes, name, value);
        }
        public void addAttributes(AttributeSet attr) {
            checkForIllegalCast();
            AttributeContext context = getAttributeContext();
            attributes = context.addAttributes(attributes, attr);
        }
        public void removeAttribute(Object name) {
            checkForIllegalCast();
            AttributeContext context = getAttributeContext();
            attributes = context.removeAttribute(attributes, name);
        }
        public void removeAttributes(Enumeration<?> names) {
            checkForIllegalCast();
            AttributeContext context = getAttributeContext();
            attributes = context.removeAttributes(attributes, names);
        }
        public void removeAttributes(AttributeSet attrs) {
            checkForIllegalCast();
            AttributeContext context = getAttributeContext();
            if (attrs == this) {
                attributes = context.getEmptySet();
            } else {
                attributes = context.removeAttributes(attributes, attrs);
            }
        }
        public void setResolveParent(AttributeSet parent) {
            checkForIllegalCast();
            AttributeContext context = getAttributeContext();
            if (parent != null) {
                attributes =
                    context.addAttribute(attributes, StyleConstants.ResolveAttribute,
                                         parent);
            } else {
                attributes =
                    context.removeAttribute(attributes, StyleConstants.ResolveAttribute);
            }
        }
        private final void checkForIllegalCast() {
            Thread t = getCurrentWriter();
            if ((t == null) || (t != Thread.currentThread())) {
                throw new StateInvariantError("Illegal cast to MutableAttributeSet");
            }
        }
        public Document getDocument() {
            return AbstractDocument.this;
        }
        public Element getParentElement() {
            return parent;
        }
        public AttributeSet getAttributes() {
            return this;
        }
        public String getName() {
            if (attributes.isDefined(ElementNameAttribute)) {
                return (String) attributes.getAttribute(ElementNameAttribute);
            }
            return null;
        }
        public abstract int getStartOffset();
        public abstract int getEndOffset();
        public abstract Element getElement(int index);
        public abstract int getElementCount();
        public abstract int getElementIndex(int offset);
        public abstract boolean isLeaf();
        public TreeNode getChildAt(int childIndex) {
            return (TreeNode)getElement(childIndex);
        }
        public int getChildCount() {
            return getElementCount();
        }
        public TreeNode getParent() {
            return (TreeNode)getParentElement();
        }
        public int getIndex(TreeNode node) {
            for(int counter = getChildCount() - 1; counter >= 0; counter--)
                if(getChildAt(counter) == node)
                    return counter;
            return -1;
        }
        public abstract boolean getAllowsChildren();
        public abstract Enumeration children();
        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            StyleContext.writeAttributeSet(s, attributes);
        }
        private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException
        {
            s.defaultReadObject();
            MutableAttributeSet attr = new SimpleAttributeSet();
            StyleContext.readAttributeSet(s, attr);
            AttributeContext context = getAttributeContext();
            attributes = context.addAttributes(SimpleAttributeSet.EMPTY, attr);
        }
        private Element parent;
        private transient AttributeSet attributes;
    }
    public class BranchElement extends AbstractElement {
        public BranchElement(Element parent, AttributeSet a) {
            super(parent, a);
            children = new AbstractElement[1];
            nchildren = 0;
            lastIndex = -1;
        }
        public Element positionToElement(int pos) {
            int index = getElementIndex(pos);
            Element child = children[index];
            int p0 = child.getStartOffset();
            int p1 = child.getEndOffset();
            if ((pos >= p0) && (pos < p1)) {
                return child;
            }
            return null;
        }
        public void replace(int offset, int length, Element[] elems) {
            int delta = elems.length - length;
            int src = offset + length;
            int nmove = nchildren - src;
            int dest = src + delta;
            if ((nchildren + delta) >= children.length) {
                int newLength = Math.max(2*children.length, nchildren + delta);
                AbstractElement[] newChildren = new AbstractElement[newLength];
                System.arraycopy(children, 0, newChildren, 0, offset);
                System.arraycopy(elems, 0, newChildren, offset, elems.length);
                System.arraycopy(children, src, newChildren, dest, nmove);
                children = newChildren;
            } else {
                System.arraycopy(children, src, children, dest, nmove);
                System.arraycopy(elems, 0, children, offset, elems.length);
            }
            nchildren = nchildren + delta;
        }
        public String toString() {
            return "BranchElement(" + getName() + ") " + getStartOffset() + "," +
                getEndOffset() + "\n";
        }
        public String getName() {
            String nm = super.getName();
            if (nm == null) {
                nm = ParagraphElementName;
            }
            return nm;
        }
        public int getStartOffset() {
            return children[0].getStartOffset();
        }
        public int getEndOffset() {
            Element child =
                (nchildren > 0) ? children[nchildren - 1] : children[0];
            return child.getEndOffset();
        }
        public Element getElement(int index) {
            if (index < nchildren) {
                return children[index];
            }
            return null;
        }
        public int getElementCount()  {
            return nchildren;
        }
        public int getElementIndex(int offset) {
            int index;
            int lower = 0;
            int upper = nchildren - 1;
            int mid = 0;
            int p0 = getStartOffset();
            int p1;
            if (nchildren == 0) {
                return 0;
            }
            if (offset >= getEndOffset()) {
                return nchildren - 1;
            }
            if ((lastIndex >= lower) && (lastIndex <= upper)) {
                Element lastHit = children[lastIndex];
                p0 = lastHit.getStartOffset();
                p1 = lastHit.getEndOffset();
                if ((offset >= p0) && (offset < p1)) {
                    return lastIndex;
                }
                if (offset < p0) {
                    upper = lastIndex;
                } else  {
                    lower = lastIndex;
                }
            }
            while (lower <= upper) {
                mid = lower + ((upper - lower) / 2);
                Element elem = children[mid];
                p0 = elem.getStartOffset();
                p1 = elem.getEndOffset();
                if ((offset >= p0) && (offset < p1)) {
                    index = mid;
                    lastIndex = index;
                    return index;
                } else if (offset < p0) {
                    upper = mid - 1;
                } else {
                    lower = mid + 1;
                }
            }
            if (offset < p0) {
                index = mid;
            } else {
                index = mid + 1;
            }
            lastIndex = index;
            return index;
        }
        public boolean isLeaf() {
            return false;
        }
        public boolean getAllowsChildren() {
            return true;
        }
        public Enumeration children() {
            if(nchildren == 0)
                return null;
            Vector<AbstractElement> tempVector = new Vector<AbstractElement>(nchildren);
            for(int counter = 0; counter < nchildren; counter++)
                tempVector.addElement(children[counter]);
            return tempVector.elements();
        }
        private AbstractElement[] children;
        private int nchildren;
        private int lastIndex;
    }
    public class LeafElement extends AbstractElement {
        public LeafElement(Element parent, AttributeSet a, int offs0, int offs1) {
            super(parent, a);
            try {
                p0 = createPosition(offs0);
                p1 = createPosition(offs1);
            } catch (BadLocationException e) {
                p0 = null;
                p1 = null;
                throw new StateInvariantError("Can't create Position references");
            }
        }
        public String toString() {
            return "LeafElement(" + getName() + ") " + p0 + "," + p1 + "\n";
        }
        public int getStartOffset() {
            return p0.getOffset();
        }
        public int getEndOffset() {
            return p1.getOffset();
        }
        public String getName() {
            String nm = super.getName();
            if (nm == null) {
                nm = ContentElementName;
            }
            return nm;
        }
        public int getElementIndex(int pos) {
            return -1;
        }
        public Element getElement(int index) {
            return null;
        }
        public int getElementCount()  {
            return 0;
        }
        public boolean isLeaf() {
            return true;
        }
        public boolean getAllowsChildren() {
            return false;
        }
        public Enumeration children() {
            return null;
        }
        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            s.writeInt(p0.getOffset());
            s.writeInt(p1.getOffset());
        }
        private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException
        {
            s.defaultReadObject();
            int off0 = s.readInt();
            int off1 = s.readInt();
            try {
                p0 = createPosition(off0);
                p1 = createPosition(off1);
            } catch (BadLocationException e) {
                p0 = null;
                p1 = null;
                throw new IOException("Can't restore Position references");
            }
        }
        private transient Position p0;
        private transient Position p1;
    }
    class BidiRootElement extends BranchElement {
        BidiRootElement() {
            super( null, null );
        }
        public String getName() {
            return "bidi root";
        }
    }
    class BidiElement extends LeafElement {
        BidiElement(Element parent, int start, int end, int level) {
            super(parent, new SimpleAttributeSet(), start, end);
            addAttribute(StyleConstants.BidiLevel, Integer.valueOf(level));
        }
        public String getName() {
            return BidiElementName;
        }
        int getLevel() {
            Integer o = (Integer) getAttribute(StyleConstants.BidiLevel);
            if (o != null) {
                return o.intValue();
            }
            return 0;  
        }
        boolean isLeftToRight() {
            return ((getLevel() % 2) == 0);
        }
    }
    public class DefaultDocumentEvent extends CompoundEdit implements DocumentEvent {
        public DefaultDocumentEvent(int offs, int len, DocumentEvent.EventType type) {
            super();
            offset = offs;
            length = len;
            this.type = type;
        }
        public String toString() {
            return edits.toString();
        }
        public boolean addEdit(UndoableEdit anEdit) {
            if ((changeLookup == null) && (edits.size() > 10)) {
                changeLookup = new Hashtable<Element, ElementChange>();
                int n = edits.size();
                for (int i = 0; i < n; i++) {
                    Object o = edits.elementAt(i);
                    if (o instanceof DocumentEvent.ElementChange) {
                        DocumentEvent.ElementChange ec = (DocumentEvent.ElementChange) o;
                        changeLookup.put(ec.getElement(), ec);
                    }
                }
            }
            if ((changeLookup != null) && (anEdit instanceof DocumentEvent.ElementChange)) {
                DocumentEvent.ElementChange ec = (DocumentEvent.ElementChange) anEdit;
                changeLookup.put(ec.getElement(), ec);
            }
            return super.addEdit(anEdit);
        }
        public void redo() throws CannotRedoException {
            writeLock();
            try {
                super.redo();
                UndoRedoDocumentEvent ev = new UndoRedoDocumentEvent(this, false);
                if (type == DocumentEvent.EventType.INSERT) {
                    fireInsertUpdate(ev);
                } else if (type == DocumentEvent.EventType.REMOVE) {
                    fireRemoveUpdate(ev);
                } else {
                    fireChangedUpdate(ev);
                }
            } finally {
                writeUnlock();
            }
        }
        public void undo() throws CannotUndoException {
            writeLock();
            try {
                super.undo();
                UndoRedoDocumentEvent ev = new UndoRedoDocumentEvent(this, true);
                if (type == DocumentEvent.EventType.REMOVE) {
                    fireInsertUpdate(ev);
                } else if (type == DocumentEvent.EventType.INSERT) {
                    fireRemoveUpdate(ev);
                } else {
                    fireChangedUpdate(ev);
                }
            } finally {
                writeUnlock();
            }
        }
        public boolean isSignificant() {
            return true;
        }
        public String getPresentationName() {
            DocumentEvent.EventType type = getType();
            if(type == DocumentEvent.EventType.INSERT)
                return UIManager.getString("AbstractDocument.additionText");
            if(type == DocumentEvent.EventType.REMOVE)
                return UIManager.getString("AbstractDocument.deletionText");
            return UIManager.getString("AbstractDocument.styleChangeText");
        }
        public String getUndoPresentationName() {
            return UIManager.getString("AbstractDocument.undoText") + " " +
                getPresentationName();
        }
        public String getRedoPresentationName() {
            return UIManager.getString("AbstractDocument.redoText") + " " +
                getPresentationName();
        }
        public DocumentEvent.EventType getType() {
            return type;
        }
        public int getOffset() {
            return offset;
        }
        public int getLength() {
            return length;
        }
        public Document getDocument() {
            return AbstractDocument.this;
        }
        public DocumentEvent.ElementChange getChange(Element elem) {
            if (changeLookup != null) {
                return changeLookup.get(elem);
            }
            int n = edits.size();
            for (int i = 0; i < n; i++) {
                Object o = edits.elementAt(i);
                if (o instanceof DocumentEvent.ElementChange) {
                    DocumentEvent.ElementChange c = (DocumentEvent.ElementChange) o;
                    if (elem.equals(c.getElement())) {
                        return c;
                    }
                }
            }
            return null;
        }
        private int offset;
        private int length;
        private Hashtable<Element, ElementChange> changeLookup;
        private DocumentEvent.EventType type;
    }
    class UndoRedoDocumentEvent implements DocumentEvent {
        private DefaultDocumentEvent src = null;
        private boolean isUndo;
        private EventType type = null;
        public UndoRedoDocumentEvent(DefaultDocumentEvent src, boolean isUndo) {
            this.src = src;
            this.isUndo = isUndo;
            if(isUndo) {
                if(src.getType().equals(EventType.INSERT)) {
                    type = EventType.REMOVE;
                } else if(src.getType().equals(EventType.REMOVE)) {
                    type = EventType.INSERT;
                } else {
                    type = src.getType();
                }
            } else {
                type = src.getType();
            }
        }
        public DefaultDocumentEvent getSource() {
            return src;
        }
        public int getOffset() {
            return src.getOffset();
        }
        public int getLength() {
            return src.getLength();
        }
        public Document getDocument() {
            return src.getDocument();
        }
        public DocumentEvent.EventType getType() {
            return type;
        }
        public DocumentEvent.ElementChange getChange(Element elem) {
            return src.getChange(elem);
        }
    }
    public static class ElementEdit extends AbstractUndoableEdit implements DocumentEvent.ElementChange {
        public ElementEdit(Element e, int index, Element[] removed, Element[] added) {
            super();
            this.e = e;
            this.index = index;
            this.removed = removed;
            this.added = added;
        }
        public Element getElement() {
            return e;
        }
        public int getIndex() {
            return index;
        }
        public Element[] getChildrenRemoved() {
            return removed;
        }
        public Element[] getChildrenAdded() {
            return added;
        }
        public void redo() throws CannotRedoException {
            super.redo();
            Element[] tmp = removed;
            removed = added;
            added = tmp;
            ((AbstractDocument.BranchElement)e).replace(index, removed.length, added);
        }
        public void undo() throws CannotUndoException {
            super.undo();
            ((AbstractDocument.BranchElement)e).replace(index, added.length, removed);
            Element[] tmp = removed;
            removed = added;
            added = tmp;
        }
        private Element e;
        private int index;
        private Element[] removed;
        private Element[] added;
    }
    private class DefaultFilterBypass extends DocumentFilter.FilterBypass {
        public Document getDocument() {
            return AbstractDocument.this;
        }
        public void remove(int offset, int length) throws
            BadLocationException {
            handleRemove(offset, length);
        }
        public void insertString(int offset, String string,
                                 AttributeSet attr) throws
                                        BadLocationException {
            handleInsertString(offset, string, attr);
        }
        public void replace(int offset, int length, String text,
                            AttributeSet attrs) throws BadLocationException {
            handleRemove(offset, length);
            handleInsertString(offset, text, attrs);
        }
    }
}
