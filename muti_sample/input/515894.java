public class CaretManager {
    private TextRunBreaker breaker;
    public CaretManager(TextRunBreaker breaker) {
        this.breaker = breaker;
    }
    private void checkHit(TextHitInfo info) {
        int idx = info.getInsertionIndex();
        if (idx < 0 || idx > breaker.getCharCount()) {
            throw new IllegalArgumentException(Messages.getString("awt.42")); 
        }
    }
    private int getVisualFromHitInfo(TextHitInfo hitInfo) {
        final int idx = hitInfo.getCharIndex();
        if (idx >= 0 && idx < breaker.getCharCount()) {
            int visual = breaker.getVisualFromLogical(idx);
            if (hitInfo.isLeadingEdge() ^ ((breaker.getLevel(idx) & 0x1) == 0x0)) {
                visual++;
            }
            return visual;
        } else if (idx < 0) {
            return breaker.isLTR() ? 0: breaker.getCharCount();
        } else {
            return breaker.isLTR() ? breaker.getCharCount() : 0;
        }
    }
    private TextHitInfo getHitInfoFromVisual(int visual) {
        final boolean first = visual == 0;
        if (!(first || visual == breaker.getCharCount())) {
            int logical = breaker.getLogicalFromVisual(visual);
            return (breaker.getLevel(logical) & 0x1) == 0x0 ?
                    TextHitInfo.leading(logical) : 
                    TextHitInfo.trailing(logical); 
        } else if (first) {
            return breaker.isLTR() ?
                    TextHitInfo.trailing(-1) :
                    TextHitInfo.leading(breaker.getCharCount());
        } else { 
            return breaker.isLTR() ?
                    TextHitInfo.leading(breaker.getCharCount()) :
                    TextHitInfo.trailing(-1);
        }
    }
    public float[] getCaretInfo(TextHitInfo hitInfo) {
        checkHit(hitInfo);
        float res[] = new float[2];
        int visual = getVisualFromHitInfo(hitInfo);
        float advance, angle;
        TextRunSegment seg;
        if (visual < breaker.getCharCount()) {
            int logIdx = breaker.getLogicalFromVisual(visual);
            int segmentIdx = breaker.logical2segment[logIdx];
            seg = breaker.runSegments.get(segmentIdx);
            advance = seg.x + seg.getAdvanceDelta(seg.getStart(), logIdx);
            angle = seg.metrics.italicAngle;
        } else { 
            int logIdx = breaker.getLogicalFromVisual(visual-1);
            int segmentIdx = breaker.logical2segment[logIdx];
            seg = breaker.runSegments.get(segmentIdx);
            advance = seg.x + seg.getAdvanceDelta(seg.getStart(), logIdx+1);
        }
        angle = seg.metrics.italicAngle;
        res[0] = advance;
        res[1] = angle;
        return res;
    }
    public TextHitInfo getNextRightHit(TextHitInfo hitInfo) {
        checkHit(hitInfo);
        int visual = getVisualFromHitInfo(hitInfo);
        if (visual == breaker.getCharCount()) {
            return null;
        }
        TextHitInfo newInfo;
        while(visual <= breaker.getCharCount()) {
            visual++;
            newInfo = getHitInfoFromVisual(visual);
            if (newInfo.getCharIndex() >= breaker.logical2segment.length) {
                return newInfo;
            }
            if (hitInfo.getCharIndex() >= 0) { 
                if (
                        breaker.logical2segment[newInfo.getCharIndex()] !=
                        breaker.logical2segment[hitInfo.getCharIndex()]
                ) {
                    return newInfo; 
                }
            }
            TextRunSegment seg = breaker.runSegments.get(breaker.logical2segment[newInfo
                    .getCharIndex()]);
            if (!seg.charHasZeroAdvance(newInfo.getCharIndex())) {
                return newInfo;
            }
        }
        return null;
    }
    public TextHitInfo getNextLeftHit(TextHitInfo hitInfo) {
        checkHit(hitInfo);
        int visual = getVisualFromHitInfo(hitInfo);
        if (visual == 0) {
            return null;
        }
        TextHitInfo newInfo;
        while(visual >= 0) {
            visual--;
            newInfo = getHitInfoFromVisual(visual);
            if (newInfo.getCharIndex() < 0) {
                return newInfo;
            }
            if (hitInfo.getCharIndex() < breaker.logical2segment.length) {
                if (
                        breaker.logical2segment[newInfo.getCharIndex()] !=
                        breaker.logical2segment[hitInfo.getCharIndex()]
                ) {
                    return newInfo; 
                }
            }
            TextRunSegment seg = breaker.runSegments.get(breaker.logical2segment[newInfo
                    .getCharIndex()]);
            if (!seg.charHasZeroAdvance(newInfo.getCharIndex())) {
                return newInfo;
            }
        }
        return null;
    }
    public TextHitInfo getVisualOtherHit(TextHitInfo hitInfo) {
        checkHit(hitInfo);
        int idx = hitInfo.getCharIndex();
        int resIdx;
        boolean resIsLeading;
        if (idx >= 0 && idx < breaker.getCharCount()) { 
            int visual = breaker.getVisualFromLogical(idx);
            if (((breaker.getLevel(idx) & 0x1) == 0x0) ^ hitInfo.isLeadingEdge()) {
                visual++;
                if (visual == breaker.getCharCount()) {
                    if (breaker.isLTR()) {
                        resIdx = breaker.getCharCount();
                        resIsLeading = true;
                    } else {
                        resIdx = -1;
                        resIsLeading = false;
                    }
                } else {
                    resIdx = breaker.getLogicalFromVisual(visual);
                    if ((breaker.getLevel(resIdx) & 0x1) == 0x0) {
                        resIsLeading = true;
                    } else {
                        resIsLeading = false;
                    }
                }
            } else {
                visual--;
                if (visual == -1) {
                    if (breaker.isLTR()) {
                        resIdx = -1;
                        resIsLeading = false;
                    } else {
                        resIdx = breaker.getCharCount();
                        resIsLeading = true;
                    }
                } else {
                    resIdx = breaker.getLogicalFromVisual(visual);
                    if ((breaker.getLevel(resIdx) & 0x1) == 0x0) {
                        resIsLeading = false;
                    } else {
                        resIsLeading = true;
                    }
                }
            }
        } else if (idx < 0) { 
            if (breaker.isLTR()) {
                resIdx = breaker.getLogicalFromVisual(0);
                resIsLeading = (breaker.getLevel(resIdx) & 0x1) == 0x0; 
            } else {
                resIdx = breaker.getLogicalFromVisual(breaker.getCharCount() - 1);
                resIsLeading = (breaker.getLevel(resIdx) & 0x1) != 0x0; 
            }
        } else { 
            if (breaker.isLTR()) {
                resIdx = breaker.getLogicalFromVisual(breaker.getCharCount() - 1);
                resIsLeading = (breaker.getLevel(resIdx) & 0x1) != 0x0; 
            } else {
                resIdx = breaker.getLogicalFromVisual(0);
                resIsLeading = (breaker.getLevel(resIdx) & 0x1) == 0x0; 
            }
        }
        return resIsLeading ? TextHitInfo.leading(resIdx) : TextHitInfo.trailing(resIdx);
    }
    public Line2D getCaretShape(TextHitInfo hitInfo, TextLayout layout) {
        return getCaretShape(hitInfo, layout, true, false, null);
    }
    public Line2D getCaretShape(
            TextHitInfo hitInfo, TextLayout layout,
            boolean useItalic, boolean useBounds, Rectangle2D bounds
    ) {
        checkHit(hitInfo);
        float x1, x2, y1, y2;
        int charIdx = hitInfo.getCharIndex();
        if (charIdx >= 0 && charIdx < breaker.getCharCount()) {
            TextRunSegment segment = breaker.runSegments.get(breaker.logical2segment[charIdx]);
            y1 = segment.metrics.descent;
            y2 = - segment.metrics.ascent - segment.metrics.leading;
            x1 = x2 = segment.getCharPosition(charIdx) + (hitInfo.isLeadingEdge() ?
                    0 : segment.getCharAdvance(charIdx));
        } else {
            y1 = layout.getDescent();
            y2 = - layout.getAscent() - layout.getLeading();
            x1 = x2 = ((breaker.getBaseLevel() & 0x1) == 0 ^ charIdx < 0) ?
                    layout.getAdvance() : 0;
        }
        if (useBounds) {
            y1 = (float) bounds.getMaxY();
            y2 = (float) bounds.getMinY();
            if (x2 > bounds.getMaxX()) {
                x1 = x2 = (float) bounds.getMaxX();
            }
            if (x1 < bounds.getMinX()) {
                x1 = x2 = (float) bounds.getMinX();
            }
        }
        return new Line2D.Float(x1, y1, x2, y2);
    }
    public Shape[] getCaretShapes(
            int offset, Rectangle2D bounds,
            TextLayout.CaretPolicy policy, TextLayout layout
    ) {
        TextHitInfo hit1 = TextHitInfo.afterOffset(offset);
        TextHitInfo hit2 = getVisualOtherHit(hit1);
        Shape caret1 = getCaretShape(hit1, layout);
        if (getVisualFromHitInfo(hit1) == getVisualFromHitInfo(hit2)) {
            return new Shape[] {caret1, null};
        }
        Shape caret2 = getCaretShape(hit2, layout);
        TextHitInfo strongHit = policy.getStrongCaret(hit1, hit2, layout);
        return strongHit.equals(hit1) ?
                new Shape[] {caret1, caret2} :
                new Shape[] {caret2, caret1};
    }
    GeneralPath connectCarets(Line2D caret1, Line2D caret2) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        path.moveTo((float) caret1.getX1(), (float) caret1.getY1());
        path.lineTo((float) caret2.getX1(), (float) caret2.getY1());
        path.lineTo((float) caret2.getX2(), (float) caret2.getY2());
        path.lineTo((float) caret1.getX2(), (float) caret1.getY2());
        path.closePath();
        return path;
    }
    public Shape getVisualHighlightShape(
            TextHitInfo hit1, TextHitInfo hit2,
            Rectangle2D bounds, TextLayout layout
    ) {
        checkHit(hit1);
        checkHit(hit2);
        Line2D caret1 = getCaretShape(hit1, layout, false, true, bounds);
        Line2D caret2 = getCaretShape(hit2, layout, false, true, bounds);
        return connectCarets(caret1, caret2);
    }
    public int[] getLogicalRangesForVisualSelection(TextHitInfo hit1, TextHitInfo hit2) {
        checkHit(hit1);
        checkHit(hit2);
        int visual1 = getVisualFromHitInfo(hit1);
        int visual2 = getVisualFromHitInfo(hit2);
        if (visual1 > visual2) {
            int tmp = visual2;
            visual2 = visual1;
            visual1 = tmp;
        }
        int results[] = new int[512];
        int prevLogical, logical, runStart, numRuns = 0;
        logical = runStart = prevLogical = breaker.getLogicalFromVisual(visual1);
        for (int i=visual1+1; i<=visual2; i++) {
            logical = breaker.getLogicalFromVisual(i);
            int diff = logical-prevLogical;
            if (diff > 1 || diff < -1) {
                results[(numRuns)*2] = Math.min(runStart, prevLogical);
                results[(numRuns)*2 + 1] = Math.max(runStart, prevLogical);
                numRuns++;
                runStart = logical;
            }
            prevLogical = logical;
        }
        results[(numRuns)*2] = Math.min(runStart, logical);
        results[(numRuns)*2 + 1] = Math.max(runStart, logical);
        numRuns++;
        int retval[] = new int[numRuns*2];
        System.arraycopy(results, 0, retval, 0, numRuns*2);
        return retval;
    }
    public Shape getLogicalHighlightShape(
            int firstEndpoint, int secondEndpoint,
            Rectangle2D bounds, TextLayout layout
    ) {
        GeneralPath res = new GeneralPath();
        for (int i=firstEndpoint; i<=secondEndpoint; i++) {
            int endRun = breaker.getLevelRunLimit(i, secondEndpoint);
            TextHitInfo hit1 = TextHitInfo.leading(i);
            TextHitInfo hit2 = TextHitInfo.trailing(endRun-1);
            Line2D caret1 = getCaretShape(hit1, layout, false, true, bounds);
            Line2D caret2 = getCaretShape(hit2, layout, false, true, bounds);
            res.append(connectCarets(caret1, caret2), false);
            i = endRun;
        }
        return res;
    }
}
