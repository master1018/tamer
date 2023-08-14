public class GlyphView extends View implements TabableView, Cloneable {
    public GlyphView(Element elem) {
        super(elem);
        offset = 0;
        length = 0;
        Element parent = elem.getParentElement();
        AttributeSet attr = elem.getAttributes();
        impliedCR = (attr != null && attr.getAttribute(IMPLIED_CR) != null &&
                   parent != null && parent.getElementCount() > 1);
        skipWidth = elem.getName().equals("br");
    }
    protected final Object clone() {
        Object o;
        try {
            o = super.clone();
        } catch (CloneNotSupportedException cnse) {
            o = null;
        }
        return o;
    }
    public GlyphPainter getGlyphPainter() {
        return painter;
    }
    public void setGlyphPainter(GlyphPainter p) {
        painter = p;
    }
     public Segment getText(int p0, int p1) {
         Segment text = SegmentCache.getSharedSegment();
         try {
             Document doc = getDocument();
             doc.getText(p0, p1 - p0, text);
         } catch (BadLocationException bl) {
             throw new StateInvariantError("GlyphView: Stale view: " + bl);
         }
         return text;
     }
    public Color getBackground() {
        Document doc = getDocument();
        if (doc instanceof StyledDocument) {
            AttributeSet attr = getAttributes();
            if (attr.isDefined(StyleConstants.Background)) {
                return ((StyledDocument)doc).getBackground(attr);
            }
        }
        return null;
    }
    public Color getForeground() {
        Document doc = getDocument();
        if (doc instanceof StyledDocument) {
            AttributeSet attr = getAttributes();
            return ((StyledDocument)doc).getForeground(attr);
        }
        Component c = getContainer();
        if (c != null) {
            return c.getForeground();
        }
        return null;
    }
    public Font getFont() {
        Document doc = getDocument();
        if (doc instanceof StyledDocument) {
            AttributeSet attr = getAttributes();
            return ((StyledDocument)doc).getFont(attr);
        }
        Component c = getContainer();
        if (c != null) {
            return c.getFont();
        }
        return null;
    }
    public boolean isUnderline() {
        AttributeSet attr = getAttributes();
        return StyleConstants.isUnderline(attr);
    }
    public boolean isStrikeThrough() {
        AttributeSet attr = getAttributes();
        return StyleConstants.isStrikeThrough(attr);
    }
    public boolean isSubscript() {
        AttributeSet attr = getAttributes();
        return StyleConstants.isSubscript(attr);
    }
    public boolean isSuperscript() {
        AttributeSet attr = getAttributes();
        return StyleConstants.isSuperscript(attr);
    }
    public TabExpander getTabExpander() {
        return expander;
    }
    protected void checkPainter() {
        if (painter == null) {
            if (defaultPainter == null) {
                String classname = "javax.swing.text.GlyphPainter1";
                try {
                    Class c;
                    ClassLoader loader = getClass().getClassLoader();
                    if (loader != null) {
                        c = loader.loadClass(classname);
                    } else {
                        c = Class.forName(classname);
                    }
                    Object o = c.newInstance();
                    if (o instanceof GlyphPainter) {
                        defaultPainter = (GlyphPainter) o;
                    }
                } catch (Throwable e) {
                    throw new StateInvariantError("GlyphView: Can't load glyph painter: "
                                                  + classname);
                }
            }
            setGlyphPainter(defaultPainter.getPainter(this, getStartOffset(),
                                                      getEndOffset()));
        }
    }
    public float getTabbedSpan(float x, TabExpander e) {
        checkPainter();
        TabExpander old = expander;
        expander = e;
        if (expander != old) {
            preferenceChanged(null, true, false);
        }
        this.x = (int) x;
        int p0 = getStartOffset();
        int p1 = getEndOffset();
        float width = painter.getSpan(this, p0, p1, expander, x);
        return width;
    }
    public float getPartialSpan(int p0, int p1) {
        checkPainter();
        float width = painter.getSpan(this, p0, p1, expander, x);
        return width;
    }
    public int getStartOffset() {
        Element e = getElement();
        return (length > 0) ? e.getStartOffset() + offset : e.getStartOffset();
    }
    public int getEndOffset() {
        Element e = getElement();
        return (length > 0) ? e.getStartOffset() + offset + length : e.getEndOffset();
    }
    private void initSelections(int p0, int p1) {
        int viewPosCount = p1 - p0 + 1;
        if (selections == null || viewPosCount > selections.length) {
            selections = new byte[viewPosCount];
            return;
        }
        for (int i = 0; i < viewPosCount; selections[i++] = 0);
    }
    public void paint(Graphics g, Shape a) {
        checkPainter();
        boolean paintedText = false;
        Component c = getContainer();
        int p0 = getStartOffset();
        int p1 = getEndOffset();
        Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
        Color bg = getBackground();
        Color fg = getForeground();
        if (c != null && ! c.isEnabled()) {
            fg = (c instanceof JTextComponent ?
                ((JTextComponent)c).getDisabledTextColor() :
                UIManager.getColor("textInactiveText"));
        }
        if (bg != null) {
            g.setColor(bg);
            g.fillRect(alloc.x, alloc.y, alloc.width, alloc.height);
        }
        if (c instanceof JTextComponent) {
            JTextComponent tc = (JTextComponent) c;
            Highlighter h = tc.getHighlighter();
            if (h instanceof LayeredHighlighter) {
                ((LayeredHighlighter)h).paintLayeredHighlights
                    (g, p0, p1, a, tc, this);
            }
        }
        if (Utilities.isComposedTextElement(getElement())) {
            Utilities.paintComposedText(g, a.getBounds(), this);
            paintedText = true;
        } else if(c instanceof JTextComponent) {
            JTextComponent tc = (JTextComponent) c;
            Color selFG = tc.getSelectedTextColor();
            if (
                (tc.getHighlighter() != null) &&
                (selFG != null) && !selFG.equals(fg)) {
                Highlighter.Highlight[] h = tc.getHighlighter().getHighlights();
                if(h.length != 0) {
                    boolean initialized = false;
                    int viewSelectionCount = 0;
                    for (int i = 0; i < h.length; i++) {
                        Highlighter.Highlight highlight = h[i];
                        int hStart = highlight.getStartOffset();
                        int hEnd = highlight.getEndOffset();
                        if (hStart > p1 || hEnd < p0) {
                            continue;
                        }
                        if (!SwingUtilities2.useSelectedTextColor(highlight, tc)) {
                            continue;
                        }
                        if (hStart <= p0 && hEnd >= p1){
                            paintTextUsingColor(g, a, selFG, p0, p1);
                            paintedText = true;
                            break;
                        }
                        if (!initialized) {
                            initSelections(p0, p1);
                            initialized = true;
                        }
                        hStart = Math.max(p0, hStart);
                        hEnd = Math.min(p1, hEnd);
                        paintTextUsingColor(g, a, selFG, hStart, hEnd);
                        selections[hStart-p0]++;
                        selections[hEnd-p0]--;
                        viewSelectionCount++;
                    }
                    if (!paintedText && viewSelectionCount > 0) {
                        int curPos = -1;
                        int startPos = 0;
                        int viewLen = p1 - p0;
                        while (curPos++ < viewLen) {
                            while(curPos < viewLen &&
                                    selections[curPos] == 0) curPos++;
                            if (startPos != curPos) {
                                paintTextUsingColor(g, a, fg,
                                        p0 + startPos, p0 + curPos);
                            }
                            int checkSum = 0;
                            while (curPos < viewLen &&
                                    (checkSum += selections[curPos]) != 0) curPos++;
                            startPos = curPos;
                        }
                        paintedText = true;
                    }
                }
            }
        }
        if(!paintedText)
            paintTextUsingColor(g, a, fg, p0, p1);
    }
    final void paintTextUsingColor(Graphics g, Shape a, Color c, int p0, int p1) {
        g.setColor(c);
        painter.paint(this, g, a, p0, p1);
        boolean underline = isUnderline();
        boolean strike = isStrikeThrough();
        if (underline || strike) {
            Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
            View parent = getParent();
            if ((parent != null) && (parent.getEndOffset() == p1)) {
                Segment s = getText(p0, p1);
                while (Character.isWhitespace(s.last())) {
                    p1 -= 1;
                    s.count -= 1;
                }
                SegmentCache.releaseSharedSegment(s);
            }
            int x0 = alloc.x;
            int p = getStartOffset();
            if (p != p0) {
                x0 += (int) painter.getSpan(this, p, p0, getTabExpander(), x0);
            }
            int x1 = x0 + (int) painter.getSpan(this, p0, p1, getTabExpander(), x0);
            int y = alloc.y + alloc.height - (int) painter.getDescent(this);
            if (underline) {
                int yTmp = y + 1;
                g.drawLine(x0, yTmp, x1, yTmp);
            }
            if (strike) {
                int yTmp = y - (int) (painter.getAscent(this) * 0.3f);
                g.drawLine(x0, yTmp, x1, yTmp);
            }
        }
    }
    @Override
    public float getMinimumSpan(int axis) {
        switch (axis) {
            case View.X_AXIS:
                if (minimumSpan < 0) {
                    minimumSpan = 0;
                    int p0 = getStartOffset();
                    int p1 = getEndOffset();
                    while (p1 > p0) {
                        int breakSpot = getBreakSpot(p0, p1);
                        if (breakSpot == BreakIterator.DONE) {
                            breakSpot = p0;
                        }
                        minimumSpan = Math.max(minimumSpan,
                                getPartialSpan(breakSpot, p1));
                        p1 = breakSpot - 1;
                    }
                }
                return minimumSpan;
            case View.Y_AXIS:
                return super.getMinimumSpan(axis);
            default:
                throw new IllegalArgumentException("Invalid axis: " + axis);
        }
    }
    public float getPreferredSpan(int axis) {
        if (impliedCR) {
            return 0;
        }
        checkPainter();
        int p0 = getStartOffset();
        int p1 = getEndOffset();
        switch (axis) {
        case View.X_AXIS:
            if (skipWidth) {
                return 0;
            }
            return painter.getSpan(this, p0, p1, expander, this.x);
        case View.Y_AXIS:
            float h = painter.getHeight(this);
            if (isSuperscript()) {
                h += h/3;
            }
            return h;
        default:
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }
    }
    public float getAlignment(int axis) {
        checkPainter();
        if (axis == View.Y_AXIS) {
            boolean sup = isSuperscript();
            boolean sub = isSubscript();
            float h = painter.getHeight(this);
            float d = painter.getDescent(this);
            float a = painter.getAscent(this);
            float align;
            if (sup) {
                align = 1.0f;
            } else if (sub) {
                align = (h > 0) ? (h - (d + (a / 2))) / h : 0;
            } else {
                align = (h > 0) ? (h - d) / h : 0;
            }
            return align;
        }
        return super.getAlignment(axis);
    }
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        checkPainter();
        return painter.modelToView(this, pos, b, a);
    }
    public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
        checkPainter();
        return painter.viewToModel(this, x, y, a, biasReturn);
    }
    public int getBreakWeight(int axis, float pos, float len) {
        if (axis == View.X_AXIS) {
            checkPainter();
            int p0 = getStartOffset();
            int p1 = painter.getBoundedPosition(this, p0, pos, len);
            return p1 == p0 ? View.BadBreakWeight :
                   getBreakSpot(p0, p1) != BreakIterator.DONE ?
                            View.ExcellentBreakWeight : View.GoodBreakWeight;
        }
        return super.getBreakWeight(axis, pos, len);
    }
    public View breakView(int axis, int p0, float pos, float len) {
        if (axis == View.X_AXIS) {
            checkPainter();
            int p1 = painter.getBoundedPosition(this, p0, pos, len);
            int breakSpot = getBreakSpot(p0, p1);
            if (breakSpot != -1) {
                p1 = breakSpot;
            }
            if (p0 == getStartOffset() && p1 == getEndOffset()) {
                return this;
            }
            GlyphView v = (GlyphView) createFragment(p0, p1);
            v.x = (int) pos;
            return v;
        }
        return this;
    }
    private int getBreakSpot(int p0, int p1) {
        if (breakSpots == null) {
            int start = getStartOffset();
            int end = getEndOffset();
            int[] bs = new int[end + 1 - start];
            int ix = 0;
            Element parent = getElement().getParentElement();
            int pstart = (parent == null ? start : parent.getStartOffset());
            int pend = (parent == null ? end : parent.getEndOffset());
            Segment s = getText(pstart, pend);
            s.first();
            BreakIterator breaker = getBreaker();
            breaker.setText(s);
            int startFrom = end + (pend > end ? 1 : 0);
            for (;;) {
                startFrom = breaker.preceding(s.offset + (startFrom - pstart))
                          + (pstart - s.offset);
                if (startFrom > start) {
                    bs[ix++] = startFrom;
                } else {
                    break;
                }
            }
            SegmentCache.releaseSharedSegment(s);
            breakSpots = new int[ix];
            System.arraycopy(bs, 0, breakSpots, 0, ix);
        }
        int breakSpot = BreakIterator.DONE;
        for (int i = 0; i < breakSpots.length; i++) {
            int bsp = breakSpots[i];
            if (bsp <= p1) {
                if (bsp > p0) {
                    breakSpot = bsp;
                }
                break;
            }
        }
        return breakSpot;
    }
    private BreakIterator getBreaker() {
        Document doc = getDocument();
        if ((doc != null) && Boolean.TRUE.equals(
                    doc.getProperty(AbstractDocument.MultiByteProperty))) {
            Container c = getContainer();
            Locale locale = (c == null ? Locale.getDefault() : c.getLocale());
            return BreakIterator.getLineInstance(locale);
        } else {
            return new WhitespaceBasedBreakIterator();
        }
    }
    public View createFragment(int p0, int p1) {
        checkPainter();
        Element elem = getElement();
        GlyphView v = (GlyphView) clone();
        v.offset = p0 - elem.getStartOffset();
        v.length = p1 - p0;
        v.painter = painter.getPainter(v, p0, p1);
        v.justificationInfo = null;
        return v;
    }
    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a,
                                         int direction,
                                         Position.Bias[] biasRet)
        throws BadLocationException {
        return painter.getNextVisualPositionFrom(this, pos, b, a, direction, biasRet);
    }
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        justificationInfo = null;
        breakSpots = null;
        minimumSpan = -1;
        syncCR();
        preferenceChanged(null, true, false);
    }
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        justificationInfo = null;
        breakSpots = null;
        minimumSpan = -1;
        syncCR();
        preferenceChanged(null, true, false);
    }
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        minimumSpan = -1;
        syncCR();
        preferenceChanged(null, true, true);
    }
    private void syncCR() {
        if (impliedCR) {
            Element parent = getElement().getParentElement();
            impliedCR = (parent != null && parent.getElementCount() > 1);
        }
    }
    static class JustificationInfo {
        final int start;
        final int end;
        final int leadingSpaces;
        final int contentSpaces;
        final int trailingSpaces;
        final boolean hasTab;
        final BitSet spaceMap;
        JustificationInfo(int start, int end,
                          int leadingSpaces,
                          int contentSpaces,
                          int trailingSpaces,
                          boolean hasTab,
                          BitSet spaceMap) {
            this.start = start;
            this.end = end;
            this.leadingSpaces = leadingSpaces;
            this.contentSpaces = contentSpaces;
            this.trailingSpaces = trailingSpaces;
            this.hasTab = hasTab;
            this.spaceMap = spaceMap;
        }
    }
    JustificationInfo getJustificationInfo(int rowStartOffset) {
        if (justificationInfo != null) {
            return justificationInfo;
        }
        final int TRAILING = 0;
        final int CONTENT  = 1;
        final int SPACES   = 2;
        int startOffset = getStartOffset();
        int endOffset = getEndOffset();
        Segment segment = getText(startOffset, endOffset);
        int txtOffset = segment.offset;
        int txtEnd = segment.offset + segment.count - 1;
        int startContentPosition = txtEnd + 1;
        int endContentPosition = txtOffset - 1;
        int lastTabPosition = txtOffset - 1;
        int trailingSpaces = 0;
        int contentSpaces = 0;
        int leadingSpaces = 0;
        boolean hasTab = false;
        BitSet spaceMap = new BitSet(endOffset - startOffset + 1);
        for (int i = txtEnd, state = TRAILING; i >= txtOffset; i--) {
            if (' ' == segment.array[i]) {
                spaceMap.set(i - txtOffset);
                if (state == TRAILING) {
                    trailingSpaces++;
                } else if (state == CONTENT) {
                    state = SPACES;
                    leadingSpaces = 1;
                } else if (state == SPACES) {
                    leadingSpaces++;
                }
            } else if ('\t' == segment.array[i]) {
                hasTab = true;
                break;
            } else {
                if (state == TRAILING) {
                    if ('\n' != segment.array[i]
                          && '\r' != segment.array[i]) {
                        state = CONTENT;
                        endContentPosition = i;
                    }
                } else if (state == CONTENT) {
                } else if (state == SPACES) {
                    contentSpaces += leadingSpaces;
                    leadingSpaces = 0;
                }
                startContentPosition = i;
            }
        }
        SegmentCache.releaseSharedSegment(segment);
        int startJustifiableContent = -1;
        if (startContentPosition < txtEnd) {
            startJustifiableContent =
                startContentPosition - txtOffset;
        }
        int endJustifiableContent = -1;
        if (endContentPosition > txtOffset) {
            endJustifiableContent =
                endContentPosition - txtOffset;
        }
        justificationInfo =
            new JustificationInfo(startJustifiableContent,
                                  endJustifiableContent,
                                  leadingSpaces,
                                  contentSpaces,
                                  trailingSpaces,
                                  hasTab,
                                  spaceMap);
        return justificationInfo;
    }
    private byte[] selections = null;
    int offset;
    int length;
    boolean impliedCR;
    boolean skipWidth;
    TabExpander expander;
    private float minimumSpan = -1;
    private int[] breakSpots = null;
    int x;
    GlyphPainter painter;
    static GlyphPainter defaultPainter;
    private JustificationInfo justificationInfo = null;
    public static abstract class GlyphPainter {
        public abstract float getSpan(GlyphView v, int p0, int p1, TabExpander e, float x);
        public abstract float getHeight(GlyphView v);
        public abstract float getAscent(GlyphView v);
        public abstract float getDescent(GlyphView v);
        public abstract void paint(GlyphView v, Graphics g, Shape a, int p0, int p1);
        public abstract Shape modelToView(GlyphView v,
                                          int pos, Position.Bias bias,
                                          Shape a) throws BadLocationException;
        public abstract int viewToModel(GlyphView v,
                                        float x, float y, Shape a,
                                        Position.Bias[] biasReturn);
        public abstract int getBoundedPosition(GlyphView v, int p0, float x, float len);
        public GlyphPainter getPainter(GlyphView v, int p0, int p1) {
            return this;
        }
        public int getNextVisualPositionFrom(GlyphView v, int pos, Position.Bias b, Shape a,
                                             int direction,
                                             Position.Bias[] biasRet)
            throws BadLocationException {
            int startOffset = v.getStartOffset();
            int endOffset = v.getEndOffset();
            Segment text;
            switch (direction) {
            case View.NORTH:
            case View.SOUTH:
                if (pos != -1) {
                    return -1;
                }
                Container container = v.getContainer();
                if (container instanceof JTextComponent) {
                    Caret c = ((JTextComponent)container).getCaret();
                    Point magicPoint;
                    magicPoint = (c != null) ? c.getMagicCaretPosition() :null;
                    if (magicPoint == null) {
                        biasRet[0] = Position.Bias.Forward;
                        return startOffset;
                    }
                    int value = v.viewToModel(magicPoint.x, 0f, a, biasRet);
                    return value;
                }
                break;
            case View.EAST:
                if(startOffset == v.getDocument().getLength()) {
                    if(pos == -1) {
                        biasRet[0] = Position.Bias.Forward;
                        return startOffset;
                    }
                    return -1;
                }
                if(pos == -1) {
                    biasRet[0] = Position.Bias.Forward;
                    return startOffset;
                }
                if(pos == endOffset) {
                    return -1;
                }
                if(++pos == endOffset) {
                    return -1;
                }
                else {
                    biasRet[0] = Position.Bias.Forward;
                }
                return pos;
            case View.WEST:
                if(startOffset == v.getDocument().getLength()) {
                    if(pos == -1) {
                        biasRet[0] = Position.Bias.Forward;
                        return startOffset;
                    }
                    return -1;
                }
                if(pos == -1) {
                    biasRet[0] = Position.Bias.Forward;
                    return endOffset - 1;
                }
                if(pos == startOffset) {
                    return -1;
                }
                biasRet[0] = Position.Bias.Forward;
                return (pos - 1);
            default:
                throw new IllegalArgumentException("Bad direction: " + direction);
            }
            return pos;
        }
    }
}
