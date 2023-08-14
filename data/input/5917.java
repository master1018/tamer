class GlyphPainter1 extends GlyphView.GlyphPainter {
    public float getSpan(GlyphView v, int p0, int p1,
                         TabExpander e, float x) {
        sync(v);
        Segment text = v.getText(p0, p1);
        int[] justificationData = getJustificationData(v);
        int width = Utilities.getTabbedTextWidth(v, text, metrics, (int) x, e, p0,
                                                 justificationData);
        SegmentCache.releaseSharedSegment(text);
        return width;
    }
    public float getHeight(GlyphView v) {
        sync(v);
        return metrics.getHeight();
    }
    public float getAscent(GlyphView v) {
        sync(v);
        return metrics.getAscent();
    }
    public float getDescent(GlyphView v) {
        sync(v);
        return metrics.getDescent();
    }
    public void paint(GlyphView v, Graphics g, Shape a, int p0, int p1) {
        sync(v);
        Segment text;
        TabExpander expander = v.getTabExpander();
        Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
        int x = alloc.x;
        int p = v.getStartOffset();
        int[] justificationData = getJustificationData(v);
        if (p != p0) {
            text = v.getText(p, p0);
            int width = Utilities.getTabbedTextWidth(v, text, metrics, x, expander, p,
                                                     justificationData);
            x += width;
            SegmentCache.releaseSharedSegment(text);
        }
        int y = alloc.y + metrics.getHeight() - metrics.getDescent();
        text = v.getText(p0, p1);
        g.setFont(metrics.getFont());
        Utilities.drawTabbedText(v, text, x, y, g, expander,p0,
                                 justificationData);
        SegmentCache.releaseSharedSegment(text);
    }
    public Shape modelToView(GlyphView v, int pos, Position.Bias bias,
                             Shape a) throws BadLocationException {
        sync(v);
        Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
        int p0 = v.getStartOffset();
        int p1 = v.getEndOffset();
        TabExpander expander = v.getTabExpander();
        Segment text;
        if(pos == p1) {
            return new Rectangle(alloc.x + alloc.width, alloc.y, 0,
                                 metrics.getHeight());
        }
        if ((pos >= p0) && (pos <= p1)) {
            text = v.getText(p0, pos);
            int[] justificationData = getJustificationData(v);
            int width = Utilities.getTabbedTextWidth(v, text, metrics, alloc.x, expander, p0,
                                                     justificationData);
            SegmentCache.releaseSharedSegment(text);
            return new Rectangle(alloc.x + width, alloc.y, 0, metrics.getHeight());
        }
        throw new BadLocationException("modelToView - can't convert", p1);
    }
    public int viewToModel(GlyphView v, float x, float y, Shape a,
                           Position.Bias[] biasReturn) {
        sync(v);
        Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
        int p0 = v.getStartOffset();
        int p1 = v.getEndOffset();
        TabExpander expander = v.getTabExpander();
        Segment text = v.getText(p0, p1);
        int[] justificationData = getJustificationData(v);
        int offs = Utilities.getTabbedTextOffset(v, text, metrics,
                                                 alloc.x, (int) x, expander, p0,
                                                 justificationData);
        SegmentCache.releaseSharedSegment(text);
        int retValue = p0 + offs;
        if(retValue == p1) {
            retValue--;
        }
        biasReturn[0] = Position.Bias.Forward;
        return retValue;
    }
    public int getBoundedPosition(GlyphView v, int p0, float x, float len) {
        sync(v);
        TabExpander expander = v.getTabExpander();
        Segment s = v.getText(p0, v.getEndOffset());
        int[] justificationData = getJustificationData(v);
        int index = Utilities.getTabbedTextOffset(v, s, metrics, (int)x, (int)(x+len),
                                                  expander, p0, false,
                                                  justificationData);
        SegmentCache.releaseSharedSegment(s);
        int p1 = p0 + index;
        return p1;
    }
    void sync(GlyphView v) {
        Font f = v.getFont();
        if ((metrics == null) || (! f.equals(metrics.getFont()))) {
            Container c = v.getContainer();
            metrics = (c != null) ? c.getFontMetrics(f) :
                Toolkit.getDefaultToolkit().getFontMetrics(f);
        }
    }
    private int[] getJustificationData(GlyphView v) {
        View parent = v.getParent();
        int [] ret = null;
        if (parent instanceof ParagraphView.Row) {
            ParagraphView.Row row = ((ParagraphView.Row) parent);
            ret = row.justificationData;
        }
        return ret;
    }
    FontMetrics metrics;
}
