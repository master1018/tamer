class AccessibleHTML implements Accessible {
    private JEditorPane editor;
    private Document model;
    private DocumentListener docListener;
    private PropertyChangeListener propChangeListener;
    private ElementInfo rootElementInfo;
    private RootHTMLAccessibleContext rootHTMLAccessibleContext;
    public AccessibleHTML(JEditorPane pane) {
        editor = pane;
        propChangeListener = new PropertyChangeHandler();
        setDocument(editor.getDocument());
        docListener = new DocumentHandler();
    }
    private void setDocument(Document document) {
        if (model != null) {
            model.removeDocumentListener(docListener);
        }
        if (editor != null) {
            editor.removePropertyChangeListener(propChangeListener);
        }
        this.model = document;
        if (model != null) {
            if (rootElementInfo != null) {
                rootElementInfo.invalidate(false);
            }
            buildInfo();
            model.addDocumentListener(docListener);
        }
        else {
            rootElementInfo = null;
        }
        if (editor != null) {
            editor.addPropertyChangeListener(propChangeListener);
        }
    }
    private Document getDocument() {
        return model;
    }
    private JEditorPane getTextComponent() {
        return editor;
    }
    private ElementInfo getRootInfo() {
        return rootElementInfo;
    }
    private View getRootView() {
        return getTextComponent().getUI().getRootView(getTextComponent());
    }
    private Rectangle getRootEditorRect() {
        Rectangle alloc = getTextComponent().getBounds();
        if ((alloc.width > 0) && (alloc.height > 0)) {
            alloc.x = alloc.y = 0;
            Insets insets = editor.getInsets();
            alloc.x += insets.left;
            alloc.y += insets.top;
            alloc.width -= insets.left + insets.right;
            alloc.height -= insets.top + insets.bottom;
            return alloc;
        }
        return null;
    }
    private Object lock() {
        Document document = getDocument();
        if (document instanceof AbstractDocument) {
            ((AbstractDocument)document).readLock();
            return document;
        }
        return null;
    }
    private void unlock(Object key) {
        if (key != null) {
            ((AbstractDocument)key).readUnlock();
        }
    }
    private void buildInfo() {
        Object lock = lock();
        try {
            Document doc = getDocument();
            Element root = doc.getDefaultRootElement();
            rootElementInfo = new ElementInfo(root);
            rootElementInfo.validate();
        } finally {
            unlock(lock);
        }
    }
    ElementInfo createElementInfo(Element e, ElementInfo parent) {
        AttributeSet attrs = e.getAttributes();
        if (attrs != null) {
            Object name = attrs.getAttribute(StyleConstants.NameAttribute);
            if (name == HTML.Tag.IMG) {
                return new IconElementInfo(e, parent);
            }
            else if (name == HTML.Tag.CONTENT || name == HTML.Tag.CAPTION) {
                return new TextElementInfo(e, parent);
            }
            else if (name == HTML.Tag.TABLE) {
                return new TableElementInfo(e, parent);
            }
        }
        return null;
    }
    public AccessibleContext getAccessibleContext() {
        if (rootHTMLAccessibleContext == null) {
            rootHTMLAccessibleContext =
                new RootHTMLAccessibleContext(rootElementInfo);
        }
        return rootHTMLAccessibleContext;
    }
    private class RootHTMLAccessibleContext extends HTMLAccessibleContext {
        public RootHTMLAccessibleContext(ElementInfo elementInfo) {
            super(elementInfo);
        }
        public String getAccessibleName() {
            if (model != null) {
                return (String)model.getProperty(Document.TitleProperty);
            } else {
                return null;
            }
        }
        public String getAccessibleDescription() {
            return editor.getContentType();
        }
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TEXT;
        }
    }
    protected abstract class HTMLAccessibleContext extends AccessibleContext
        implements Accessible, AccessibleComponent {
        protected ElementInfo elementInfo;
        public HTMLAccessibleContext(ElementInfo elementInfo) {
            this.elementInfo = elementInfo;
        }
        public AccessibleContext getAccessibleContext() {
            return this;
        }
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = new AccessibleStateSet();
            Component comp = getTextComponent();
            if (comp.isEnabled()) {
                states.add(AccessibleState.ENABLED);
            }
            if (comp instanceof JTextComponent &&
                ((JTextComponent)comp).isEditable()) {
                states.add(AccessibleState.EDITABLE);
                states.add(AccessibleState.FOCUSABLE);
            }
            if (comp.isVisible()) {
                states.add(AccessibleState.VISIBLE);
            }
            if (comp.isShowing()) {
                states.add(AccessibleState.SHOWING);
            }
            return states;
        }
        public int getAccessibleIndexInParent() {
            return elementInfo.getIndexInParent();
        }
        public int getAccessibleChildrenCount() {
            return elementInfo.getChildCount();
        }
        public Accessible getAccessibleChild(int i) {
            ElementInfo childInfo = elementInfo.getChild(i);
            if (childInfo != null && childInfo instanceof Accessible) {
                return (Accessible)childInfo;
            } else {
                return null;
            }
        }
        public Locale getLocale() throws IllegalComponentStateException {
            return editor.getLocale();
        }
        public AccessibleComponent getAccessibleComponent() {
            return this;
        }
        public Color getBackground() {
            return getTextComponent().getBackground();
        }
        public void setBackground(Color c) {
            getTextComponent().setBackground(c);
        }
        public Color getForeground() {
            return getTextComponent().getForeground();
        }
        public void setForeground(Color c) {
            getTextComponent().setForeground(c);
        }
        public Cursor getCursor() {
            return getTextComponent().getCursor();
        }
        public void setCursor(Cursor cursor) {
            getTextComponent().setCursor(cursor);
        }
        public Font getFont() {
            return getTextComponent().getFont();
        }
        public void setFont(Font f) {
            getTextComponent().setFont(f);
        }
        public FontMetrics getFontMetrics(Font f) {
            return getTextComponent().getFontMetrics(f);
        }
        public boolean isEnabled() {
            return getTextComponent().isEnabled();
        }
        public void setEnabled(boolean b) {
            getTextComponent().setEnabled(b);
        }
        public boolean isVisible() {
            return getTextComponent().isVisible();
        }
        public void setVisible(boolean b) {
            getTextComponent().setVisible(b);
        }
        public boolean isShowing() {
            return getTextComponent().isShowing();
        }
        public boolean contains(Point p) {
            Rectangle r = getBounds();
            if (r != null) {
                return r.contains(p.x, p.y);
            } else {
                return false;
            }
        }
        public Point getLocationOnScreen() {
            Point editorLocation = getTextComponent().getLocationOnScreen();
            Rectangle r = getBounds();
            if (r != null) {
                return new Point(editorLocation.x + r.x,
                                 editorLocation.y + r.y);
            } else {
                return null;
            }
        }
        public Point getLocation() {
            Rectangle r = getBounds();
            if (r != null) {
                return new Point(r.x, r.y);
            } else {
                return null;
            }
        }
        public void setLocation(Point p) {
        }
        public Rectangle getBounds() {
            return elementInfo.getBounds();
        }
        public void setBounds(Rectangle r) {
        }
        public Dimension getSize() {
            Rectangle r = getBounds();
            if (r != null) {
                return new Dimension(r.width, r.height);
            } else {
                return null;
            }
        }
        public void setSize(Dimension d) {
            Component comp = getTextComponent();
            comp.setSize(d);
        }
        public Accessible getAccessibleAt(Point p) {
            ElementInfo innerMostElement = getElementInfoAt(rootElementInfo, p);
            if (innerMostElement instanceof Accessible) {
                return (Accessible)innerMostElement;
            } else {
                return null;
            }
        }
        private ElementInfo getElementInfoAt(ElementInfo elementInfo, Point p) {
            if (elementInfo.getBounds() == null) {
                return null;
            }
            if (elementInfo.getChildCount() == 0 &&
                elementInfo.getBounds().contains(p)) {
                return elementInfo;
            } else {
                if (elementInfo instanceof TableElementInfo) {
                    ElementInfo captionInfo =
                        ((TableElementInfo)elementInfo).getCaptionInfo();
                    if (captionInfo != null) {
                        Rectangle bounds = captionInfo.getBounds();
                        if (bounds != null && bounds.contains(p)) {
                            return captionInfo;
                        }
                    }
                }
                for (int i = 0; i < elementInfo.getChildCount(); i++)
{
                    ElementInfo childInfo = elementInfo.getChild(i);
                    ElementInfo retValue = getElementInfoAt(childInfo, p);
                    if (retValue != null) {
                        return retValue;
                    }
                }
            }
            return null;
        }
        public boolean isFocusTraversable() {
            Component comp = getTextComponent();
            if (comp instanceof JTextComponent) {
                if (((JTextComponent)comp).isEditable()) {
                    return true;
                }
            }
            return false;
        }
        public void requestFocus() {
            if (! isFocusTraversable()) {
                return;
            }
            Component comp = getTextComponent();
            if (comp instanceof JTextComponent) {
                comp.requestFocusInWindow();
                try {
                    if (elementInfo.validateIfNecessary()) {
                        Element elem = elementInfo.getElement();
                        ((JTextComponent)comp).setCaretPosition(elem.getStartOffset());
                        AccessibleContext ac = editor.getAccessibleContext();
                        PropertyChangeEvent pce = new PropertyChangeEvent(this,
                            AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                            null, AccessibleState.FOCUSED);
                        ac.firePropertyChange(
                            AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                            null, pce);
                    }
                } catch (IllegalArgumentException e) {
                }
            }
        }
        public void addFocusListener(FocusListener l) {
            getTextComponent().addFocusListener(l);
        }
        public void removeFocusListener(FocusListener l) {
            getTextComponent().removeFocusListener(l);
        }
    } 
    class TextElementInfo extends ElementInfo implements Accessible {
        TextElementInfo(Element element, ElementInfo parent) {
            super(element, parent);
        }
        private AccessibleContext accessibleContext;
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new TextAccessibleContext(this);
            }
            return accessibleContext;
        }
        public class TextAccessibleContext extends HTMLAccessibleContext
            implements AccessibleText {
            public TextAccessibleContext(ElementInfo elementInfo) {
                super(elementInfo);
            }
            public AccessibleText getAccessibleText() {
                return this;
            }
            public String getAccessibleName() {
                if (model != null) {
                    return (String)model.getProperty(Document.TitleProperty);
                } else {
                    return null;
                }
            }
            public String getAccessibleDescription() {
                return editor.getContentType();
            }
            public AccessibleRole getAccessibleRole() {
                return AccessibleRole.TEXT;
            }
            public int getIndexAtPoint(Point p) {
                View v = getView();
                if (v != null) {
                    return v.viewToModel(p.x, p.y, getBounds());
                } else {
                    return -1;
                }
            }
            public Rectangle getCharacterBounds(int i) {
                try {
                    return editor.getUI().modelToView(editor, i);
                } catch (BadLocationException e) {
                    return null;
                }
            }
            public int getCharCount() {
                if (validateIfNecessary()) {
                    Element elem = elementInfo.getElement();
                    return elem.getEndOffset() - elem.getStartOffset();
                }
                return 0;
            }
            public int getCaretPosition() {
                View v = getView();
                if (v == null) {
                    return -1;
                }
                Container c = v.getContainer();
                if (c == null) {
                    return -1;
                }
                if (c instanceof JTextComponent) {
                    return ((JTextComponent)c).getCaretPosition();
                } else {
                    return -1;
                }
            }
            private class IndexedSegment extends Segment {
                public int modelOffset;
            }
            public String getAtIndex(int part, int index) {
                return getAtIndex(part, index, 0);
            }
            public String getAfterIndex(int part, int index) {
                return getAtIndex(part, index, 1);
            }
            public String getBeforeIndex(int part, int index) {
                return getAtIndex(part, index, -1);
            }
            private String getAtIndex(int part, int index, int direction) {
                if (model instanceof AbstractDocument) {
                    ((AbstractDocument)model).readLock();
                }
                try {
                    if (index < 0 || index >= model.getLength()) {
                        return null;
                    }
                    switch (part) {
                    case AccessibleText.CHARACTER:
                        if (index + direction < model.getLength() &&
                            index + direction >= 0) {
                            return model.getText(index + direction, 1);
                        }
                        break;
                    case AccessibleText.WORD:
                    case AccessibleText.SENTENCE:
                        IndexedSegment seg = getSegmentAt(part, index);
                        if (seg != null) {
                            if (direction != 0) {
                                int next;
                                if (direction < 0) {
                                    next = seg.modelOffset - 1;
                                }
                                else {
                                    next = seg.modelOffset + direction * seg.count;
                                }
                                if (next >= 0 && next <= model.getLength()) {
                                    seg = getSegmentAt(part, next);
                                }
                                else {
                                    seg = null;
                                }
                            }
                            if (seg != null) {
                                return new String(seg.array, seg.offset,
                                                  seg.count);
                            }
                        }
                        break;
                    default:
                        break;
                    }
                } catch (BadLocationException e) {
                } finally {
                    if (model instanceof AbstractDocument) {
                        ((AbstractDocument)model).readUnlock();
                    }
                }
                return null;
            }
            private Element getParagraphElement(int index) {
                if (model instanceof PlainDocument ) {
                    PlainDocument sdoc = (PlainDocument)model;
                    return sdoc.getParagraphElement(index);
                } else if (model instanceof StyledDocument) {
                    StyledDocument sdoc = (StyledDocument)model;
                    return sdoc.getParagraphElement(index);
                } else {
                    Element para;
                    for (para = model.getDefaultRootElement(); ! para.isLeaf(); ) {
                        int pos = para.getElementIndex(index);
                        para = para.getElement(pos);
                    }
                    if (para == null) {
                        return null;
                    }
                    return para.getParentElement();
                }
            }
            private IndexedSegment getParagraphElementText(int index)
                throws BadLocationException {
                Element para = getParagraphElement(index);
                if (para != null) {
                    IndexedSegment segment = new IndexedSegment();
                    try {
                        int length = para.getEndOffset() - para.getStartOffset();
                        model.getText(para.getStartOffset(), length, segment);
                    } catch (BadLocationException e) {
                        return null;
                    }
                    segment.modelOffset = para.getStartOffset();
                    return segment;
                }
                return null;
            }
            private IndexedSegment getSegmentAt(int part, int index)
                throws BadLocationException {
                IndexedSegment seg = getParagraphElementText(index);
                if (seg == null) {
                    return null;
                }
                BreakIterator iterator;
                switch (part) {
                case AccessibleText.WORD:
                    iterator = BreakIterator.getWordInstance(getLocale());
                    break;
                case AccessibleText.SENTENCE:
                    iterator = BreakIterator.getSentenceInstance(getLocale());
                    break;
                default:
                    return null;
                }
                seg.first();
                iterator.setText(seg);
                int end = iterator.following(index - seg.modelOffset + seg.offset);
                if (end == BreakIterator.DONE) {
                    return null;
                }
                if (end > seg.offset + seg.count) {
                    return null;
                }
                int begin = iterator.previous();
                if (begin == BreakIterator.DONE ||
                    begin >= seg.offset + seg.count) {
                    return null;
                }
                seg.modelOffset = seg.modelOffset + begin - seg.offset;
                seg.offset = begin;
                seg.count = end - begin;
                return seg;
            }
            public AttributeSet getCharacterAttribute(int i) {
                if (model instanceof StyledDocument) {
                    StyledDocument doc = (StyledDocument)model;
                    Element elem = doc.getCharacterElement(i);
                    if (elem != null) {
                        return elem.getAttributes();
                    }
                }
                return null;
            }
            public int getSelectionStart() {
                return editor.getSelectionStart();
            }
            public int getSelectionEnd() {
                return editor.getSelectionEnd();
            }
            public String getSelectedText() {
                return editor.getSelectedText();
            }
            private String getText(int offset, int length)
                throws BadLocationException {
                if (model != null && model instanceof StyledDocument) {
                    StyledDocument doc = (StyledDocument)model;
                    return model.getText(offset, length);
                } else {
                    return null;
                }
            }
        }
    }
    private class IconElementInfo extends ElementInfo implements Accessible {
        private int width = -1;
        private int height = -1;
        IconElementInfo(Element element, ElementInfo parent) {
            super(element, parent);
        }
        protected void invalidate(boolean first) {
            super.invalidate(first);
            width = height = -1;
        }
        private int getImageSize(Object key) {
            if (validateIfNecessary()) {
                int size = getIntAttr(getAttributes(), key, -1);
                if (size == -1) {
                    View v = getView();
                    size = 0;
                    if (v instanceof ImageView) {
                        Image img = ((ImageView)v).getImage();
                        if (img != null) {
                            if (key == HTML.Attribute.WIDTH) {
                                size = img.getWidth(null);
                            }
                            else {
                                size = img.getHeight(null);
                            }
                        }
                    }
                }
                return size;
            }
            return 0;
        }
        private AccessibleContext accessibleContext;
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new IconAccessibleContext(this);
            }
            return accessibleContext;
        }
        protected class IconAccessibleContext extends HTMLAccessibleContext
            implements AccessibleIcon  {
            public IconAccessibleContext(ElementInfo elementInfo) {
                super(elementInfo);
            }
            public String getAccessibleName() {
                return getAccessibleIconDescription();
            }
            public String getAccessibleDescription() {
                return editor.getContentType();
            }
            public AccessibleRole getAccessibleRole() {
                return AccessibleRole.ICON;
            }
            public AccessibleIcon [] getAccessibleIcon() {
                AccessibleIcon [] icons = new AccessibleIcon[1];
                icons[0] = this;
                return icons;
            }
            public String getAccessibleIconDescription() {
                return ((ImageView)getView()).getAltText();
            }
            public void setAccessibleIconDescription(String description) {
            }
            public int getAccessibleIconWidth() {
                if (width == -1) {
                    width = getImageSize(HTML.Attribute.WIDTH);
                }
                return width;
            }
            public int getAccessibleIconHeight() {
                if (height == -1) {
                    height = getImageSize(HTML.Attribute.HEIGHT);
                }
                return height;
            }
        }
    }
    private class TableElementInfo extends ElementInfo
        implements Accessible {
        protected ElementInfo caption;
        private TableCellElementInfo[][] grid;
        TableElementInfo(Element e, ElementInfo parent) {
            super(e, parent);
        }
        public ElementInfo getCaptionInfo() {
            return caption;
        }
        protected void validate() {
            super.validate();
            updateGrid();
        }
        protected void loadChildren(Element e) {
            for (int counter = 0; counter < e.getElementCount(); counter++) {
                Element child = e.getElement(counter);
                AttributeSet attrs = child.getAttributes();
                if (attrs.getAttribute(StyleConstants.NameAttribute) ==
                                       HTML.Tag.TR) {
                    addChild(new TableRowElementInfo(child, this, counter));
                } else if (attrs.getAttribute(StyleConstants.NameAttribute) ==
                                       HTML.Tag.CAPTION) {
                    caption = createElementInfo(child, this);
                }
            }
        }
        private void updateGrid() {
            int delta = 0;
            int maxCols = 0;
            int rows;
            for (int counter = 0; counter < getChildCount(); counter++) {
                TableRowElementInfo row = getRow(counter);
                int prev = 0;
                for (int y = 0; y < delta; y++) {
                    prev = Math.max(prev, getRow(counter - y - 1).
                                    getColumnCount(y + 2));
                }
                delta = Math.max(row.getRowCount(), delta);
                delta--;
                maxCols = Math.max(maxCols, row.getColumnCount() + prev);
            }
            rows = getChildCount() + delta;
            grid = new TableCellElementInfo[rows][];
            for (int counter = 0; counter < rows; counter++) {
                grid[counter] = new TableCellElementInfo[maxCols];
            }
            for (int counter = 0; counter < rows; counter++) {
                getRow(counter).updateGrid(counter);
            }
        }
        public TableRowElementInfo getRow(int index) {
            return (TableRowElementInfo)getChild(index);
        }
        public TableCellElementInfo getCell(int r, int c) {
            if (validateIfNecessary() && r < grid.length &&
                                         c < grid[0].length) {
                return grid[r][c];
            }
            return null;
        }
        public int getRowExtentAt(int r, int c) {
            TableCellElementInfo cell = getCell(r, c);
            if (cell != null) {
                int rows = cell.getRowCount();
                int delta = 1;
                while ((r - delta) >= 0 && grid[r - delta][c] == cell) {
                    delta++;
                }
                return rows - delta + 1;
            }
            return 0;
        }
        public int getColumnExtentAt(int r, int c) {
            TableCellElementInfo cell = getCell(r, c);
            if (cell != null) {
                int cols = cell.getColumnCount();
                int delta = 1;
                while ((c - delta) >= 0 && grid[r][c - delta] == cell) {
                    delta++;
                }
                return cols - delta + 1;
            }
            return 0;
        }
        public int getRowCount() {
            if (validateIfNecessary()) {
                return grid.length;
            }
            return 0;
        }
        public int getColumnCount() {
            if (validateIfNecessary() && grid.length > 0) {
                return grid[0].length;
            }
            return 0;
        }
        private AccessibleContext accessibleContext;
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new TableAccessibleContext(this);
            }
            return accessibleContext;
        }
        public class TableAccessibleContext extends HTMLAccessibleContext
            implements AccessibleTable {
            private AccessibleHeadersTable rowHeadersTable;
            public TableAccessibleContext(ElementInfo elementInfo) {
                super(elementInfo);
            }
            public String getAccessibleName() {
                return getAccessibleRole().toString();
            }
            public String getAccessibleDescription() {
                return editor.getContentType();
            }
            public AccessibleRole getAccessibleRole() {
                return AccessibleRole.TABLE;
            }
            public int getAccessibleIndexInParent() {
                return elementInfo.getIndexInParent();
            }
            public int getAccessibleChildrenCount() {
                return ((TableElementInfo)elementInfo).getRowCount() *
                    ((TableElementInfo)elementInfo).getColumnCount();
            }
            public Accessible getAccessibleChild(int i) {
                int rowCount = ((TableElementInfo)elementInfo).getRowCount();
                int columnCount = ((TableElementInfo)elementInfo).getColumnCount();
                int r = i / rowCount;
                int c = i % columnCount;
                if (r < 0 || r >= rowCount || c < 0 || c >= columnCount) {
                    return null;
                } else {
                    return getAccessibleAt(r, c);
                }
            }
            public AccessibleTable getAccessibleTable() {
                return this;
            }
            public Accessible getAccessibleCaption() {
                ElementInfo captionInfo = getCaptionInfo();
                if (captionInfo instanceof Accessible) {
                    return (Accessible)caption;
                } else {
                    return null;
                }
            }
            public void setAccessibleCaption(Accessible a) {
            }
            public Accessible getAccessibleSummary() {
                return null;
            }
            public void setAccessibleSummary(Accessible a) {
            }
            public int getAccessibleRowCount() {
                return ((TableElementInfo)elementInfo).getRowCount();
            }
            public int getAccessibleColumnCount() {
                return ((TableElementInfo)elementInfo).getColumnCount();
            }
            public Accessible getAccessibleAt(int r, int c) {
                TableCellElementInfo cellInfo = getCell(r, c);
                if (cellInfo != null) {
                    return cellInfo.getAccessible();
                } else {
                    return null;
                }
            }
            public int getAccessibleRowExtentAt(int r, int c) {
                return ((TableElementInfo)elementInfo).getRowExtentAt(r, c);
            }
            public int getAccessibleColumnExtentAt(int r, int c) {
                return ((TableElementInfo)elementInfo).getColumnExtentAt(r, c);
            }
            public AccessibleTable getAccessibleRowHeader() {
                return rowHeadersTable;
            }
            public void setAccessibleRowHeader(AccessibleTable table) {
            }
            public AccessibleTable getAccessibleColumnHeader() {
                return null;
            }
            public void setAccessibleColumnHeader(AccessibleTable table) {
            }
            public Accessible getAccessibleRowDescription(int r) {
                return null;
            }
            public void setAccessibleRowDescription(int r, Accessible a) {
            }
            public Accessible getAccessibleColumnDescription(int c) {
                return null;
            }
            public void setAccessibleColumnDescription(int c, Accessible a) {
            }
            public boolean isAccessibleSelected(int r, int c) {
                if (validateIfNecessary()) {
                    if (r < 0 || r >= getAccessibleRowCount() ||
                        c < 0 || c >= getAccessibleColumnCount()) {
                        return false;
                    }
                    TableCellElementInfo cell = getCell(r, c);
                    if (cell != null) {
                        Element elem = cell.getElement();
                        int start = elem.getStartOffset();
                        int end = elem.getEndOffset();
                        return start >= editor.getSelectionStart() &&
                            end <= editor.getSelectionEnd();
                    }
                }
                return false;
            }
            public boolean isAccessibleRowSelected(int r) {
                if (validateIfNecessary()) {
                    if (r < 0 || r >= getAccessibleRowCount()) {
                        return false;
                    }
                    int nColumns = getAccessibleColumnCount();
                    TableCellElementInfo startCell = getCell(r, 0);
                    if (startCell == null) {
                        return false;
                    }
                    int start = startCell.getElement().getStartOffset();
                    TableCellElementInfo endCell = getCell(r, nColumns-1);
                    if (endCell == null) {
                        return false;
                    }
                    int end = endCell.getElement().getEndOffset();
                    return start >= editor.getSelectionStart() &&
                        end <= editor.getSelectionEnd();
                }
                return false;
            }
            public boolean isAccessibleColumnSelected(int c) {
                if (validateIfNecessary()) {
                    if (c < 0 || c >= getAccessibleColumnCount()) {
                        return false;
                    }
                    int nRows = getAccessibleRowCount();
                    TableCellElementInfo startCell = getCell(0, c);
                    if (startCell == null) {
                        return false;
                    }
                    int start = startCell.getElement().getStartOffset();
                    TableCellElementInfo endCell = getCell(nRows-1, c);
                    if (endCell == null) {
                        return false;
                    }
                    int end = endCell.getElement().getEndOffset();
                    return start >= editor.getSelectionStart() &&
                        end <= editor.getSelectionEnd();
                }
                return false;
            }
            public int [] getSelectedAccessibleRows() {
                if (validateIfNecessary()) {
                    int nRows = getAccessibleRowCount();
                    Vector<Integer> vec = new Vector<Integer>();
                    for (int i = 0; i < nRows; i++) {
                        if (isAccessibleRowSelected(i)) {
                            vec.addElement(Integer.valueOf(i));
                        }
                    }
                    int retval[] = new int[vec.size()];
                    for (int i = 0; i < retval.length; i++) {
                        retval[i] = vec.elementAt(i).intValue();
                    }
                    return retval;
                }
                return new int[0];
            }
            public int [] getSelectedAccessibleColumns() {
                if (validateIfNecessary()) {
                    int nColumns = getAccessibleRowCount();
                    Vector<Integer> vec = new Vector<Integer>();
                    for (int i = 0; i < nColumns; i++) {
                        if (isAccessibleColumnSelected(i)) {
                            vec.addElement(Integer.valueOf(i));
                        }
                    }
                    int retval[] = new int[vec.size()];
                    for (int i = 0; i < retval.length; i++) {
                        retval[i] = vec.elementAt(i).intValue();
                    }
                    return retval;
                }
                return new int[0];
            }
            public int getAccessibleRow(int index) {
                if (validateIfNecessary()) {
                    int numCells = getAccessibleColumnCount() *
                        getAccessibleRowCount();
                    if (index >= numCells) {
                        return -1;
                    } else {
                        return index / getAccessibleColumnCount();
                    }
                }
                return -1;
            }
            public int getAccessibleColumn(int index) {
                if (validateIfNecessary()) {
                    int numCells = getAccessibleColumnCount() *
                        getAccessibleRowCount();
                    if (index >= numCells) {
                        return -1;
                    } else {
                        return index % getAccessibleColumnCount();
                    }
                }
                return -1;
            }
            public int getAccessibleIndex(int r, int c) {
                if (validateIfNecessary()) {
                    if (r >= getAccessibleRowCount() ||
                        c >= getAccessibleColumnCount()) {
                        return -1;
                    } else {
                        return r * getAccessibleColumnCount() + c;
                    }
                }
                return -1;
            }
            public String getAccessibleRowHeader(int r) {
                if (validateIfNecessary()) {
                    TableCellElementInfo cellInfo = getCell(r, 0);
                    if (cellInfo.isHeaderCell()) {
                        View v = cellInfo.getView();
                        if (v != null && model != null) {
                            try {
                                return model.getText(v.getStartOffset(),
                                                     v.getEndOffset() -
                                                     v.getStartOffset());
                            } catch (BadLocationException e) {
                                return null;
                            }
                        }
                    }
                }
                return null;
            }
            public String getAccessibleColumnHeader(int c) {
                if (validateIfNecessary()) {
                    TableCellElementInfo cellInfo = getCell(0, c);
                    if (cellInfo.isHeaderCell()) {
                        View v = cellInfo.getView();
                        if (v != null && model != null) {
                            try {
                                return model.getText(v.getStartOffset(),
                                                     v.getEndOffset() -
                                                     v.getStartOffset());
                            } catch (BadLocationException e) {
                                return null;
                            }
                        }
                    }
                }
                return null;
            }
            public void addRowHeader(TableCellElementInfo cellInfo, int rowNumber) {
                if (rowHeadersTable == null) {
                    rowHeadersTable = new AccessibleHeadersTable();
                }
                rowHeadersTable.addHeader(cellInfo, rowNumber);
            }
            protected class AccessibleHeadersTable implements AccessibleTable {
                private Hashtable<Integer, ArrayList<TableCellElementInfo>> headers =
                        new Hashtable<Integer, ArrayList<TableCellElementInfo>>();
                private int rowCount = 0;
                private int columnCount = 0;
                public void addHeader(TableCellElementInfo cellInfo, int rowNumber) {
                    Integer rowInteger = Integer.valueOf(rowNumber);
                    ArrayList<TableCellElementInfo> list = headers.get(rowInteger);
                    if (list == null) {
                        list = new ArrayList<TableCellElementInfo>();
                        headers.put(rowInteger, list);
                    }
                    list.add(cellInfo);
                }
                public Accessible getAccessibleCaption() {
                    return null;
                }
                public void setAccessibleCaption(Accessible a) {
                }
                public Accessible getAccessibleSummary() {
                    return null;
                }
                public void setAccessibleSummary(Accessible a) {
                }
                public int getAccessibleRowCount() {
                    return rowCount;
                }
                public int getAccessibleColumnCount() {
                    return columnCount;
                }
                private TableCellElementInfo getElementInfoAt(int r, int c) {
                    ArrayList<TableCellElementInfo> list = headers.get(Integer.valueOf(r));
                    if (list != null) {
                        return list.get(c);
                    } else {
                        return null;
                    }
                }
                public Accessible getAccessibleAt(int r, int c) {
                    ElementInfo elementInfo = getElementInfoAt(r, c);
                    if (elementInfo instanceof Accessible) {
                        return (Accessible)elementInfo;
                    } else {
                        return null;
                    }
                }
                public int getAccessibleRowExtentAt(int r, int c) {
                    TableCellElementInfo elementInfo = getElementInfoAt(r, c);
                    if (elementInfo != null) {
                        return elementInfo.getRowCount();
                    } else {
                        return 0;
                    }
                }
                public int getAccessibleColumnExtentAt(int r, int c) {
                    TableCellElementInfo elementInfo = getElementInfoAt(r, c);
                    if (elementInfo != null) {
                        return elementInfo.getRowCount();
                    } else {
                        return 0;
                    }
                }
                public AccessibleTable getAccessibleRowHeader() {
                    return null;
                }
                public void setAccessibleRowHeader(AccessibleTable table) {
                }
                public AccessibleTable getAccessibleColumnHeader() {
                    return null;
                }
                public void setAccessibleColumnHeader(AccessibleTable table) {
                }
                public Accessible getAccessibleRowDescription(int r) {
                    return null;
                }
                public void setAccessibleRowDescription(int r, Accessible a) {
                }
                public Accessible getAccessibleColumnDescription(int c) {
                    return null;
                }
                public void setAccessibleColumnDescription(int c, Accessible a) {
                }
                public boolean isAccessibleSelected(int r, int c) {
                    return false;
                }
                public boolean isAccessibleRowSelected(int r) {
                    return false;
                }
                public boolean isAccessibleColumnSelected(int c) {
                    return false;
                }
                public int [] getSelectedAccessibleRows() {
                    return new int [0];
                }
                public int [] getSelectedAccessibleColumns() {
                    return new int [0];
                }
            }
        } 
        private class TableRowElementInfo extends ElementInfo {
            private TableElementInfo parent;
            private int rowNumber;
            TableRowElementInfo(Element e, TableElementInfo parent, int rowNumber) {
                super(e, parent);
                this.parent = parent;
                this.rowNumber = rowNumber;
            }
            protected void loadChildren(Element e) {
                for (int x = 0; x < e.getElementCount(); x++) {
                    AttributeSet attrs = e.getElement(x).getAttributes();
                    if (attrs.getAttribute(StyleConstants.NameAttribute) ==
                            HTML.Tag.TH) {
                        TableCellElementInfo headerElementInfo =
                            new TableCellElementInfo(e.getElement(x), this, true);
                        addChild(headerElementInfo);
                        AccessibleTable at =
                            parent.getAccessibleContext().getAccessibleTable();
                        TableAccessibleContext tableElement =
                            (TableAccessibleContext)at;
                        tableElement.addRowHeader(headerElementInfo, rowNumber);
                    } else if (attrs.getAttribute(StyleConstants.NameAttribute) ==
                            HTML.Tag.TD) {
                        addChild(new TableCellElementInfo(e.getElement(x), this,
                                                          false));
                    }
                }
            }
            public int getRowCount() {
                int rowCount = 1;
                if (validateIfNecessary()) {
                    for (int counter = 0; counter < getChildCount();
                         counter++) {
                        TableCellElementInfo cell = (TableCellElementInfo)
                                                    getChild(counter);
                        if (cell.validateIfNecessary()) {
                            rowCount = Math.max(rowCount, cell.getRowCount());
                        }
                    }
                }
                return rowCount;
            }
            public int getColumnCount() {
                int colCount = 0;
                if (validateIfNecessary()) {
                    for (int counter = 0; counter < getChildCount();
                         counter++) {
                        TableCellElementInfo cell = (TableCellElementInfo)
                                                    getChild(counter);
                        if (cell.validateIfNecessary()) {
                            colCount += cell.getColumnCount();
                        }
                    }
                }
                return colCount;
            }
            protected void invalidate(boolean first) {
                super.invalidate(first);
                getParent().invalidate(true);
            }
            private void updateGrid(int row) {
                if (validateIfNecessary()) {
                    boolean emptyRow = false;
                    while (!emptyRow) {
                        for (int counter = 0; counter < grid[row].length;
                                 counter++) {
                            if (grid[row][counter] == null) {
                                emptyRow = true;
                                break;
                            }
                        }
                        if (!emptyRow) {
                            row++;
                        }
                    }
                    for (int col = 0, counter = 0; counter < getChildCount();
                             counter++) {
                        TableCellElementInfo cell = (TableCellElementInfo)
                                                    getChild(counter);
                        while (grid[row][col] != null) {
                            col++;
                        }
                        for (int rowCount = cell.getRowCount() - 1;
                             rowCount >= 0; rowCount--) {
                            for (int colCount = cell.getColumnCount() - 1;
                                 colCount >= 0; colCount--) {
                                grid[row + rowCount][col + colCount] = cell;
                            }
                        }
                        col += cell.getColumnCount();
                    }
                }
            }
            private int getColumnCount(int rowspan) {
                if (validateIfNecessary()) {
                    int cols = 0;
                    for (int counter = 0; counter < getChildCount();
                         counter++) {
                        TableCellElementInfo cell = (TableCellElementInfo)
                                                    getChild(counter);
                        if (cell.getRowCount() >= rowspan) {
                            cols += cell.getColumnCount();
                        }
                    }
                    return cols;
                }
                return 0;
            }
        }
        private class TableCellElementInfo extends ElementInfo {
            private Accessible accessible;
            private boolean isHeaderCell;
            TableCellElementInfo(Element e, ElementInfo parent) {
                super(e, parent);
                this.isHeaderCell = false;
            }
            TableCellElementInfo(Element e, ElementInfo parent,
                                 boolean isHeaderCell) {
                super(e, parent);
                this.isHeaderCell = isHeaderCell;
            }
            public boolean isHeaderCell() {
                return this.isHeaderCell;
            }
            public Accessible getAccessible() {
                accessible = null;
                getAccessible(this);
                return accessible;
            }
            private void getAccessible(ElementInfo elementInfo) {
                if (elementInfo instanceof Accessible) {
                    accessible = (Accessible)elementInfo;
                } else {
                    for (int i = 0; i < elementInfo.getChildCount(); i++) {
                        getAccessible(elementInfo.getChild(i));
                    }
                }
            }
            public int getRowCount() {
                if (validateIfNecessary()) {
                    return Math.max(1, getIntAttr(getAttributes(),
                                                  HTML.Attribute.ROWSPAN, 1));
                }
                return 0;
            }
            public int getColumnCount() {
                if (validateIfNecessary()) {
                    return Math.max(1, getIntAttr(getAttributes(),
                                                  HTML.Attribute.COLSPAN, 1));
                }
                return 0;
            }
            protected void invalidate(boolean first) {
                super.invalidate(first);
                getParent().invalidate(true);
            }
        }
    }
    private class ElementInfo {
        private ArrayList<ElementInfo> children;
        private Element element;
        private ElementInfo parent;
        private boolean isValid;
        private boolean canBeValid;
        ElementInfo(Element element) {
            this(element, null);
        }
        ElementInfo(Element element, ElementInfo parent) {
            this.element = element;
            this.parent = parent;
            isValid = false;
            canBeValid = true;
        }
        protected void validate() {
            isValid = true;
            loadChildren(getElement());
        }
        protected void loadChildren(Element parent) {
            if (!parent.isLeaf()) {
                for (int counter = 0, maxCounter = parent.getElementCount();
                    counter < maxCounter; counter++) {
                    Element e = parent.getElement(counter);
                    ElementInfo childInfo = createElementInfo(e, this);
                    if (childInfo != null) {
                        addChild(childInfo);
                    }
                    else {
                        loadChildren(e);
                    }
                }
            }
        }
        public int getIndexInParent() {
            if (parent == null || !parent.isValid()) {
                return -1;
            }
            return parent.indexOf(this);
        }
        public Element getElement() {
            return element;
        }
        public ElementInfo getParent() {
            return parent;
        }
        public int indexOf(ElementInfo child) {
            ArrayList children = this.children;
            if (children != null) {
                return children.indexOf(child);
            }
            return -1;
        }
        public ElementInfo getChild(int index) {
            if (validateIfNecessary()) {
                ArrayList<ElementInfo> children = this.children;
                if (children != null && index >= 0 &&
                                        index < children.size()) {
                    return children.get(index);
                }
            }
            return null;
        }
        public int getChildCount() {
            validateIfNecessary();
            return (children == null) ? 0 : children.size();
        }
        protected void addChild(ElementInfo child) {
            if (children == null) {
                children = new ArrayList<ElementInfo>();
            }
            children.add(child);
        }
        protected View getView() {
            if (!validateIfNecessary()) {
                return null;
            }
            Object lock = lock();
            try {
                View rootView = getRootView();
                Element e = getElement();
                int start = e.getStartOffset();
                if (rootView != null) {
                    return getView(rootView, e, start);
                }
                return null;
            } finally {
                unlock(lock);
            }
        }
        public Rectangle getBounds() {
            if (!validateIfNecessary()) {
                return null;
            }
            Object lock = lock();
            try {
                Rectangle bounds = getRootEditorRect();
                View rootView = getRootView();
                Element e = getElement();
                if (bounds != null && rootView != null) {
                    try {
                        return rootView.modelToView(e.getStartOffset(),
                                                    Position.Bias.Forward,
                                                    e.getEndOffset(),
                                                    Position.Bias.Backward,
                                                    bounds).getBounds();
                    } catch (BadLocationException ble) { }
                }
            } finally {
                unlock(lock);
            }
            return null;
        }
        protected boolean isValid() {
            return isValid;
        }
        protected AttributeSet getAttributes() {
            if (validateIfNecessary()) {
                return getElement().getAttributes();
            }
            return null;
        }
        protected AttributeSet getViewAttributes() {
            if (validateIfNecessary()) {
                View view = getView();
                if (view != null) {
                    return view.getElement().getAttributes();
                }
                return getElement().getAttributes();
            }
            return null;
        }
        protected int getIntAttr(AttributeSet attrs, Object key, int deflt) {
            if (attrs != null && attrs.isDefined(key)) {
                int i;
                String val = (String)attrs.getAttribute(key);
                if (val == null) {
                    i = deflt;
                }
                else {
                    try {
                        i = Math.max(0, Integer.parseInt(val));
                    } catch (NumberFormatException x) {
                        i = deflt;
                    }
                }
                return i;
            }
            return deflt;
        }
        protected boolean validateIfNecessary() {
            if (!isValid() && canBeValid) {
                children = null;
                Object lock = lock();
                try {
                    validate();
                } finally {
                    unlock(lock);
                }
            }
            return isValid();
        }
        protected void invalidate(boolean first) {
            if (!isValid()) {
                if (canBeValid && !first) {
                    canBeValid = false;
                }
                return;
            }
            isValid = false;
            canBeValid = first;
            if (children != null) {
                for (ElementInfo child : children) {
                    child.invalidate(false);
                }
                children = null;
            }
        }
        private View getView(View parent, Element e, int start) {
            if (parent.getElement() == e) {
                return parent;
            }
            int index = parent.getViewIndex(start, Position.Bias.Forward);
            if (index != -1 && index < parent.getViewCount()) {
                return getView(parent.getView(index), e, start);
            }
            return null;
        }
        private int getClosestInfoIndex(int index) {
            for (int counter = 0; counter < getChildCount(); counter++) {
                ElementInfo info = getChild(counter);
                if (index < info.getElement().getEndOffset() ||
                    index == info.getElement().getStartOffset()) {
                    return counter;
                }
            }
            return -1;
        }
        private void update(DocumentEvent e) {
            if (!isValid()) {
                return;
            }
            ElementInfo parent = getParent();
            Element element = getElement();
            do {
                DocumentEvent.ElementChange ec = e.getChange(element);
                if (ec != null) {
                    if (element == getElement()) {
                        invalidate(true);
                    }
                    else if (parent != null) {
                        parent.invalidate(parent == getRootInfo());
                    }
                    return;
                }
                element = element.getParentElement();
            } while (parent != null && element != null &&
                     element != parent.getElement());
            if (getChildCount() > 0) {
                Element elem = getElement();
                int pos = e.getOffset();
                int index0 = getClosestInfoIndex(pos);
                if (index0 == -1 &&
                    e.getType() == DocumentEvent.EventType.REMOVE &&
                    pos >= elem.getEndOffset()) {
                    index0 = getChildCount() - 1;
                }
                ElementInfo info = (index0 >= 0) ? getChild(index0) : null;
                if (info != null &&
                    (info.getElement().getStartOffset() == pos) && (pos > 0)) {
                    index0 = Math.max(index0 - 1, 0);
                }
                int index1;
                if (e.getType() != DocumentEvent.EventType.REMOVE) {
                    index1 = getClosestInfoIndex(pos + e.getLength());
                    if (index1 < 0) {
                        index1 = getChildCount() - 1;
                    }
                }
                else {
                    index1 = index0;
                    while ((index1 + 1) < getChildCount() &&
                           getChild(index1 + 1).getElement().getEndOffset() ==
                           getChild(index1 + 1).getElement().getStartOffset()){
                        index1++;
                    }
                }
                index0 = Math.max(index0, 0);
                for (int i = index0; i <= index1 && isValid(); i++) {
                    getChild(i).update(e);
                }
            }
        }
    }
    private class DocumentHandler implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            getRootInfo().update(e);
        }
        public void removeUpdate(DocumentEvent e) {
            getRootInfo().update(e);
        }
        public void changedUpdate(DocumentEvent e) {
            getRootInfo().update(e);
        }
    }
    private class PropertyChangeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("document")) {
                setDocument(editor.getDocument());
            }
        }
    }
}
