class TextLayoutStrategy extends FlowView.FlowStrategy {
    public TextLayoutStrategy() {
        text = new AttributedSegment();
    }
    public void insertUpdate(FlowView fv, DocumentEvent e, Rectangle alloc) {
        sync(fv);
        super.insertUpdate(fv, e, alloc);
    }
    public void removeUpdate(FlowView fv, DocumentEvent e, Rectangle alloc) {
        sync(fv);
        super.removeUpdate(fv, e, alloc);
    }
    public void changedUpdate(FlowView fv, DocumentEvent e, Rectangle alloc) {
        sync(fv);
        super.changedUpdate(fv, e, alloc);
    }
    public void layout(FlowView fv) {
        super.layout(fv);
    }
    protected int layoutRow(FlowView fv, int rowIndex, int p0) {
        int p1 = super.layoutRow(fv, rowIndex, p0);
        View row = fv.getView(rowIndex);
        Document doc = fv.getDocument();
        Object i18nFlag = doc.getProperty(AbstractDocument.I18NProperty);
        if ((i18nFlag != null) && i18nFlag.equals(Boolean.TRUE)) {
            int n = row.getViewCount();
            if (n > 1) {
                AbstractDocument d = (AbstractDocument)fv.getDocument();
                Element bidiRoot = d.getBidiRootElement();
                byte[] levels = new byte[n];
                View[] reorder = new View[n];
                for( int i=0; i<n; i++ ) {
                    View v = row.getView(i);
                    int bidiIndex =bidiRoot.getElementIndex(v.getStartOffset());
                    Element bidiElem = bidiRoot.getElement( bidiIndex );
                    levels[i] = (byte)StyleConstants.getBidiLevel(bidiElem.getAttributes());
                    reorder[i] = v;
                }
                BidiUtils.reorderVisually( levels, reorder );
                row.replace(0, n, reorder);
            }
        }
        return p1;
    }
    protected void adjustRow(FlowView fv, int rowIndex, int desiredSpan, int x) {
    }
    protected View createView(FlowView fv, int startOffset, int spanLeft, int rowIndex) {
        View lv = getLogicalView(fv);
        View row = fv.getView(rowIndex);
        boolean requireNextWord = (viewBuffer.size() == 0) ? false : true;
        int childIndex = lv.getViewIndex(startOffset, Position.Bias.Forward);
        View v = lv.getView(childIndex);
        int endOffset = getLimitingOffset(v, startOffset, spanLeft, requireNextWord);
        if (endOffset == startOffset) {
            return null;
        }
        View frag;
        if ((startOffset==v.getStartOffset()) && (endOffset == v.getEndOffset())) {
            frag = v;
        } else {
            frag = v.createFragment(startOffset, endOffset);
        }
        if ((frag instanceof GlyphView) && (measurer != null)) {
            boolean isTab = false;
            int p0 = frag.getStartOffset();
            int p1 = frag.getEndOffset();
            if ((p1 - p0) == 1) {
                Segment s = ((GlyphView)frag).getText(p0, p1);
                char ch = s.first();
                if (ch == '\t') {
                    isTab = true;
                }
            }
            TextLayout tl = (isTab) ? null :
                measurer.nextLayout(spanLeft, text.toIteratorIndex(endOffset),
                                    requireNextWord);
            if (tl != null) {
                ((GlyphView)frag).setGlyphPainter(new GlyphPainter2(tl));
            }
        }
        return frag;
    }
    int getLimitingOffset(View v, int startOffset, int spanLeft, boolean requireNextWord) {
        int endOffset = v.getEndOffset();
        Document doc = v.getDocument();
        if (doc instanceof AbstractDocument) {
            AbstractDocument d = (AbstractDocument) doc;
            Element bidiRoot = d.getBidiRootElement();
            if( bidiRoot.getElementCount() > 1 ) {
                int bidiIndex = bidiRoot.getElementIndex( startOffset );
                Element bidiElem = bidiRoot.getElement( bidiIndex );
                endOffset = Math.min( bidiElem.getEndOffset(), endOffset );
            }
        }
        if (v instanceof GlyphView) {
            Segment s = ((GlyphView)v).getText(startOffset, endOffset);
            char ch = s.first();
            if (ch == '\t') {
                endOffset = startOffset + 1;
            } else {
                for (ch = s.next(); ch != Segment.DONE; ch = s.next()) {
                    if (ch == '\t') {
                        endOffset = startOffset + s.getIndex() - s.getBeginIndex();
                        break;
                    }
                }
            }
        }
        int limitIndex = text.toIteratorIndex(endOffset);
        if (measurer != null) {
            int index = text.toIteratorIndex(startOffset);
            if (measurer.getPosition() != index) {
                measurer.setPosition(index);
            }
            limitIndex = measurer.nextOffset(spanLeft, limitIndex, requireNextWord);
        }
        int pos = text.toModelPosition(limitIndex);
        return pos;
    }
    void sync(FlowView fv) {
        View lv = getLogicalView(fv);
        text.setView(lv);
        Container container = fv.getContainer();
        FontRenderContext frc = sun.swing.SwingUtilities2.
                                    getFontRenderContext(container);
        BreakIterator iter;
        Container c = fv.getContainer();
        if (c != null) {
            iter = BreakIterator.getLineInstance(c.getLocale());
        } else {
            iter = BreakIterator.getLineInstance();
        }
        Object shaper = null;
        if (c instanceof JComponent) {
            shaper = ((JComponent) c).getClientProperty(
                                            TextAttribute.NUMERIC_SHAPING);
        }
        text.setShaper(shaper);
        measurer = new LineBreakMeasurer(text, iter, frc);
        int n = lv.getViewCount();
        for( int i=0; i<n; i++ ) {
            View child = lv.getView(i);
            if( child instanceof GlyphView ) {
                int p0 = child.getStartOffset();
                int p1 = child.getEndOffset();
                measurer.setPosition(text.toIteratorIndex(p0));
                TextLayout layout
                    = measurer.nextLayout( Float.MAX_VALUE,
                                           text.toIteratorIndex(p1), false );
                ((GlyphView)child).setGlyphPainter(new GlyphPainter2(layout));
            }
        }
        measurer.setPosition(text.getBeginIndex());
    }
    private LineBreakMeasurer measurer;
    private AttributedSegment text;
    static class AttributedSegment extends Segment implements AttributedCharacterIterator {
        AttributedSegment() {
        }
        View getView() {
            return v;
        }
        void setView(View v) {
            this.v = v;
            Document doc = v.getDocument();
            int p0 = v.getStartOffset();
            int p1 = v.getEndOffset();
            try {
                doc.getText(p0, p1 - p0, this);
            } catch (BadLocationException bl) {
                throw new IllegalArgumentException("Invalid view");
            }
            first();
        }
        int getFontBoundary(int childIndex, int dir) {
            View child = v.getView(childIndex);
            Font f = getFont(childIndex);
            for (childIndex += dir; (childIndex >= 0) && (childIndex < v.getViewCount());
                 childIndex += dir) {
                Font next = getFont(childIndex);
                if (next != f) {
                    break;
                }
                child = v.getView(childIndex);
            }
            return (dir < 0) ? child.getStartOffset() : child.getEndOffset();
        }
        Font getFont(int childIndex) {
            View child = v.getView(childIndex);
            if (child instanceof GlyphView) {
                return ((GlyphView)child).getFont();
            }
            return null;
        }
        int toModelPosition(int index) {
            return v.getStartOffset() + (index - getBeginIndex());
        }
        int toIteratorIndex(int pos) {
            return pos - v.getStartOffset() + getBeginIndex();
        }
        private void setShaper(Object shaper) {
            this.shaper = shaper;
        }
        public int getRunStart() {
            int pos = toModelPosition(getIndex());
            int i = v.getViewIndex(pos, Position.Bias.Forward);
            View child = v.getView(i);
            return toIteratorIndex(child.getStartOffset());
        }
        public int getRunStart(AttributedCharacterIterator.Attribute attribute) {
            if (attribute instanceof TextAttribute) {
                int pos = toModelPosition(getIndex());
                int i = v.getViewIndex(pos, Position.Bias.Forward);
                if (attribute == TextAttribute.FONT) {
                    return toIteratorIndex(getFontBoundary(i, -1));
                }
            }
            return getBeginIndex();
        }
        public int getRunStart(Set<? extends Attribute> attributes) {
            int index = getBeginIndex();
            Object[] a = attributes.toArray();
            for (int i = 0; i < a.length; i++) {
                TextAttribute attr = (TextAttribute) a[i];
                index = Math.max(getRunStart(attr), index);
            }
            return Math.min(getIndex(), index);
        }
        public int getRunLimit() {
            int pos = toModelPosition(getIndex());
            int i = v.getViewIndex(pos, Position.Bias.Forward);
            View child = v.getView(i);
            return toIteratorIndex(child.getEndOffset());
        }
        public int getRunLimit(AttributedCharacterIterator.Attribute attribute) {
            if (attribute instanceof TextAttribute) {
                int pos = toModelPosition(getIndex());
                int i = v.getViewIndex(pos, Position.Bias.Forward);
                if (attribute == TextAttribute.FONT) {
                    return toIteratorIndex(getFontBoundary(i, 1));
                }
            }
            return getEndIndex();
        }
        public int getRunLimit(Set<? extends Attribute> attributes) {
            int index = getEndIndex();
            Object[] a = attributes.toArray();
            for (int i = 0; i < a.length; i++) {
                TextAttribute attr = (TextAttribute) a[i];
                index = Math.min(getRunLimit(attr), index);
            }
            return Math.max(getIndex(), index);
        }
        public Map<Attribute, Object> getAttributes() {
            Object[] ka = keys.toArray();
            Hashtable<Attribute, Object> h = new Hashtable<Attribute, Object>();
            for (int i = 0; i < ka.length; i++) {
                TextAttribute a = (TextAttribute) ka[i];
                Object value = getAttribute(a);
                if (value != null) {
                    h.put(a, value);
                }
            }
            return h;
        }
        public Object getAttribute(AttributedCharacterIterator.Attribute attribute) {
            int pos = toModelPosition(getIndex());
            int childIndex = v.getViewIndex(pos, Position.Bias.Forward);
            if (attribute == TextAttribute.FONT) {
                return getFont(childIndex);
            } else if( attribute == TextAttribute.RUN_DIRECTION ) {
                return
                    v.getDocument().getProperty(TextAttribute.RUN_DIRECTION);
            } else if (attribute == TextAttribute.NUMERIC_SHAPING) {
                return shaper;
            }
            return null;
        }
        public Set<Attribute> getAllAttributeKeys() {
            return keys;
        }
        View v;
        static Set<Attribute> keys;
        static {
            keys = new HashSet<Attribute>();
            keys.add(TextAttribute.FONT);
            keys.add(TextAttribute.RUN_DIRECTION);
            keys.add(TextAttribute.NUMERIC_SHAPING);
        }
        private Object shaper = null;
    }
}
