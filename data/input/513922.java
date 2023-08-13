public class TextRunBreaker implements Cloneable {
    AttributedCharacterIterator aci;
    FontRenderContext frc;
    char[] text;
    byte[] levels;
    HashMap<Integer, Font> fonts;
    HashMap<Integer, Decoration> decorations;
    int forcedFontRunStarts[];
    ArrayList<TextRunSegment> runSegments = new ArrayList<TextRunSegment>();
    int logical2segment[];
    int segment2visual[]; 
    int visual2segment[];
    int logical2visual[];
    int visual2logical[];
    SegmentsInfo storedSegments;
    private boolean haveAllSegments = false;
    int segmentsStart, segmentsEnd;
    float justification = 1.0f;
    public TextRunBreaker(AttributedCharacterIterator aci, FontRenderContext frc) {
        this.aci = aci;
        this.frc = frc;
        segmentsStart = aci.getBeginIndex();
        segmentsEnd = aci.getEndIndex();
        int len = segmentsEnd - segmentsStart;
        text = new char[len];
        aci.setIndex(segmentsEnd);
        while (len-- != 0) { 
            text[len] = aci.previous();
        }
        createStyleRuns();
    }
    int getVisualFromSegmentOrder(int segmentNum) {
        return (segment2visual == null) ? segmentNum : segment2visual[segmentNum];
    }
    int getSegmentFromVisualOrder(int visual) {
        return (visual2segment == null) ? visual : visual2segment[visual];
    }
    int getVisualFromLogical(int logical) {
        return (logical2visual == null) ? logical : logical2visual[logical];
    }
    int getLogicalFromVisual(int visual) {
        return (visual2logical == null) ? visual : visual2logical[visual];
    }
    int getLevelRunLimit(int runStart, int runEnd) {
        if (levels == null) {
            return runEnd;
        }
        int endLevelRun = runStart + 1;
        byte level = levels[runStart];
        while (endLevelRun <= runEnd && levels[endLevelRun] == level) {
            endLevelRun++;
        }
        return endLevelRun;
    }
    Map<? extends Attribute, ?> unpackAttributes(Map<? extends Attribute, ?> attrs) {
        if (attrs.containsKey(TextAttribute.INPUT_METHOD_HIGHLIGHT)) {
            Map<TextAttribute, ?> styles = null;
            Object val = attrs.get(TextAttribute.INPUT_METHOD_HIGHLIGHT);
            if (val instanceof Annotation) {
                val = ((Annotation) val).getValue();
            }
            if (val instanceof InputMethodHighlight) {
                InputMethodHighlight ihl = ((InputMethodHighlight) val);
                styles = ihl.getStyle();
                if (styles == null) {
                    Toolkit tk = Toolkit.getDefaultToolkit();
                    styles = tk.mapInputMethodHighlight(ihl);
                }
            }
            if (styles != null) {
                HashMap<Attribute, Object> newAttrs = new HashMap<Attribute, Object>();
                newAttrs.putAll(attrs);
                newAttrs.putAll(styles);
                return newAttrs;
            }
        }
        return attrs;
    }
    void createStyleRuns() {
        fonts = new HashMap<Integer, Font>();
        decorations = new HashMap<Integer, Decoration>();
        ArrayList<Integer> forcedFontRunStartsList = null;
        Map<? extends Attribute, ?> attributes = null;
        Object val = aci.getAttribute(TextAttribute.JUSTIFICATION);
        if (val != null) {
            justification = ((Float) val).floatValue();
        }
        for (
            int index = segmentsStart, nextRunStart = segmentsStart;
            index < segmentsEnd;
            index = nextRunStart, aci.setIndex(index)
           )  {
            nextRunStart = aci.getRunLimit();
            attributes = unpackAttributes(aci.getAttributes());
            TextDecorator.Decoration d = TextDecorator.getDecoration(attributes);
            decorations.put(new Integer(index), d);
            Font value = (Font)attributes.get(TextAttribute.CHAR_REPLACEMENT);
            if (value == null) {
                value = (Font)attributes.get(TextAttribute.FONT);
                if (value == null) {
                    if (attributes.get(TextAttribute.FAMILY) != null) {
                        value = Font.getFont(attributes);
                    }
                    if (value == null) {
                        if (forcedFontRunStartsList == null) {
                            forcedFontRunStartsList = new ArrayList<Integer>();
                        }
                        FontFinder.findFonts(
                                text,
                                index,
                                nextRunStart,
                                forcedFontRunStartsList,
                                fonts
                        );
                        value = fonts.get(new Integer(index));
                    }
                }
            }
            fonts.put(new Integer(index), value);
        }
        if (forcedFontRunStartsList != null) {
            forcedFontRunStarts = new int[forcedFontRunStartsList.size()];
            for (int i=0; i<forcedFontRunStartsList.size(); i++) {
                forcedFontRunStarts[i] =
                        forcedFontRunStartsList.get(i).intValue();
            }
        }
    }
    int getStyleRunLimit(int runStart, int maxPos) {
        try {
            aci.setIndex(runStart);
        } catch(IllegalArgumentException e) { 
            if (runStart < segmentsStart) {
                aci.first();
            } else {
                aci.last();
            }
        }
        if (forcedFontRunStarts != null) {
            for (int element : forcedFontRunStarts) {
                if (element > runStart) {
                    maxPos = Math.min(element, maxPos);
                    break;
                }
            }
        }
        return Math.min(aci.getRunLimit(), maxPos);
    }
    public void createSegments(int runStart, int runEnd) {
        int endStyleRun, endLevelRun;
        int pos = runStart, levelPos;
        aci.setIndex(pos);
        final int firstRunStart = aci.getRunStart();
        Object tdd = decorations.get(new Integer(firstRunStart));
        Object fontOrGAttr = fonts.get(new Integer(firstRunStart));
        logical2segment = new int[runEnd - runStart];
        do {
            endStyleRun = getStyleRunLimit(pos, runEnd);
            int ajustedPos = pos - runStart;
            int ajustedEndStyleRun = endStyleRun - runStart;
            levelPos = ajustedPos;
            do {
                endLevelRun = getLevelRunLimit(levelPos, ajustedEndStyleRun);
                if (fontOrGAttr instanceof GraphicAttribute) {
                    runSegments.add(
                        new TextRunSegmentImpl.TextRunSegmentGraphic(
                                (GraphicAttribute)fontOrGAttr,
                                endLevelRun - levelPos,
                                levelPos + runStart)
                    );
                    Arrays.fill(logical2segment, levelPos, endLevelRun, runSegments.size()-1);
                } else {
                    TextRunSegmentImpl.TextSegmentInfo i =
                            new TextRunSegmentImpl.TextSegmentInfo(
                                    levels == null ? 0 : levels[ajustedPos],
                                    (Font) fontOrGAttr,
                                    frc,
                                    text,
                                    levelPos + runStart,
                                    endLevelRun + runStart
                            );
                    runSegments.add(
                            new TextRunSegmentImpl.TextRunSegmentCommon(
                                    i,
                                    (TextDecorator.Decoration) tdd
                            )
                    );
                    Arrays.fill(logical2segment, levelPos, endLevelRun, runSegments.size()-1);
                }
                levelPos = endLevelRun;
            } while (levelPos < ajustedEndStyleRun);
            pos = endStyleRun;
            tdd = decorations.get(new Integer(pos));
            fontOrGAttr = fonts.get(new Integer(pos));
        } while (pos < runEnd);
    }
    public void createAllSegments() {
        if ( !haveAllSegments &&
            (logical2segment == null ||
             logical2segment.length != segmentsEnd - segmentsStart)
        ) { 
            resetSegments();
            createSegments(segmentsStart, segmentsEnd);
        }
        haveAllSegments = true;
    }
    public int getLineBreakIndex(int start, float maxAdvance) {
        int breakIndex;
        TextRunSegment s = null;
        for (
                int segmentIndex = logical2segment[start];
                segmentIndex < runSegments.size();
                segmentIndex++
           ) {
            s = runSegments.get(segmentIndex);
            breakIndex = s.getCharIndexFromAdvance(maxAdvance, start);
            if (breakIndex < s.getEnd()) {
                return breakIndex;
            }
            maxAdvance -= s.getAdvanceDelta(start, s.getEnd());
            start = s.getEnd();
        }
        return s.getEnd();
    }
    public void insertChar(AttributedCharacterIterator newParagraph, int insertPos) {
        aci = newParagraph;
        char insChar = aci.setIndex(insertPos);
        Integer key = new Integer(insertPos);
        insertPos -= aci.getBeginIndex();
        char newText[] = new char[text.length + 1];
        System.arraycopy(text, 0, newText, 0, insertPos);
        newText[insertPos] = insChar;
        System.arraycopy(text, insertPos, newText, insertPos+1, text.length - insertPos);
        text = newText;
        if (aci.getRunStart() == key.intValue() && aci.getRunLimit() == key.intValue() + 1) {
            createStyleRuns(); 
        } else {
            shiftStyleRuns(key, 1);
        }
        resetSegments();
        segmentsEnd++;
    }
    public void deleteChar(AttributedCharacterIterator newParagraph, int deletePos) {
        aci = newParagraph;
        Integer key = new Integer(deletePos);
        deletePos -= aci.getBeginIndex();
        char newText[] = new char[text.length - 1];
        System.arraycopy(text, 0, newText, 0, deletePos);
        System.arraycopy(text, deletePos+1, newText, deletePos, newText.length - deletePos);
        text = newText;
        if (fonts.get(key) != null) {
            fonts.remove(key);
        }
        shiftStyleRuns(key, -1);
        resetSegments();
        segmentsEnd--;
    }
    private void shiftStyleRuns(Integer pos, final int shift) {
        ArrayList<Integer> keys = new ArrayList<Integer>();
        Integer key, oldkey;
        for (Iterator<Integer> it = fonts.keySet().iterator(); it.hasNext(); ) {
            oldkey = it.next();
            if (oldkey.intValue() > pos.intValue()) {
                keys.add(oldkey);
            }
        }
        for (int i=0; i<keys.size(); i++) {
            oldkey = keys.get(i);
            key = new Integer(shift + oldkey.intValue());
            fonts.put(key, fonts.remove(oldkey));
            decorations.put(key, decorations.remove(oldkey));
        }
    }
    private void resetSegments() {
        runSegments = new ArrayList<TextRunSegment>();
        logical2segment = null;
        segment2visual = null;
        visual2segment = null;
        levels = null;
        haveAllSegments = false;
    }
    private class SegmentsInfo {
        ArrayList<TextRunSegment> runSegments;
        int logical2segment[];
        int segment2visual[];
        int visual2segment[];
        byte levels[];
        int segmentsStart;
        int segmentsEnd;
    }
    public void pushSegments(int newSegStart, int newSegEnd) {
        storedSegments = new SegmentsInfo();
        storedSegments.runSegments = this.runSegments;
        storedSegments.logical2segment = this.logical2segment;
        storedSegments.segment2visual = this.segment2visual;
        storedSegments.visual2segment = this.visual2segment;
        storedSegments.levels = this.levels;
        storedSegments.segmentsStart = segmentsStart;
        storedSegments.segmentsEnd = segmentsEnd;
        resetSegments();
        segmentsStart = newSegStart;
        segmentsEnd = newSegEnd;
    }
    public void popSegments() {
        if (storedSegments == null) {
            return;
        }
        this.runSegments = storedSegments.runSegments;
        this.logical2segment = storedSegments.logical2segment;
        this.segment2visual = storedSegments.segment2visual;
        this.visual2segment = storedSegments.visual2segment;
        this.levels = storedSegments.levels;
        this.segmentsStart = storedSegments.segmentsStart;
        this.segmentsEnd = storedSegments.segmentsEnd;
        storedSegments = null;
        if (runSegments.size() == 0 && logical2segment == null) {
            haveAllSegments = false;
        } else {
            haveAllSegments = true;
        }
    }
    @Override
    public Object clone() {
        try {
            TextRunBreaker res = (TextRunBreaker) super.clone();
            res.storedSegments = null;
            ArrayList<TextRunSegment> newSegments = new ArrayList<TextRunSegment>(runSegments.size());
            for (int i = 0; i < runSegments.size(); i++) {
                TextRunSegment seg =  runSegments.get(i);
                newSegments.add((TextRunSegment)seg.clone());
            }
            res.runSegments = newSegments;
            return res;
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException(Messages.getString("awt.3E")); 
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TextRunBreaker)) {
            return false;
        }
        TextRunBreaker br = (TextRunBreaker) obj;
        if (br.getACI().equals(aci) && br.frc.equals(frc)) {
            return true;
        }
        return false;
    }
    @Override
    public int hashCode() {
        return HashCode.combine(aci.hashCode(), frc.hashCode());
    }
    public void drawSegments(Graphics2D g2d, float xOffset, float yOffset) {
        for (int i=0; i<runSegments.size(); i++) {
            runSegments.get(i).draw(g2d, xOffset, yOffset);
        }
    }
    public Shape getBlackBoxBounds(int firstEndpoint, int secondEndpoint) {
        GeneralPath bounds = new GeneralPath();
        TextRunSegment segment;
        for (int idx = firstEndpoint; idx < secondEndpoint; idx=segment.getEnd()) {
            segment = runSegments.get(logical2segment[idx]);
            bounds.append(segment.getCharsBlackBoxBounds(idx, secondEndpoint), false);
        }
        return bounds;
    }
    public Rectangle2D getVisualBounds() {
        Rectangle2D bounds = null;
        for (int i=0; i<runSegments.size(); i++) {
            TextRunSegment s = runSegments.get(i);
            if (bounds != null) {
                Rectangle2D.union(bounds, s.getVisualBounds(), bounds);
            } else {
                bounds = s.getVisualBounds();
            }
        }
        return bounds;
    }
    public Rectangle2D getLogicalBounds() {
        Rectangle2D bounds = null;
        for (int i=0; i<runSegments.size(); i++) {
            TextRunSegment s = runSegments.get(i);
            if (bounds != null) {
                Rectangle2D.union(bounds, s.getLogicalBounds(), bounds);
            } else {
                bounds = s.getLogicalBounds();
            }
        }
        return bounds;
    }
    public int getCharCount() {
        return segmentsEnd - segmentsStart;
    }
    public byte getLevel(int idx) {
        if (levels == null) {
            return 0;
        }
        return levels[idx];
    }
    public int getBaseLevel() {
        return 0;
    }
    public boolean isLTR() {
        return true;
    }
    public char getChar(int index) {
        return text[index];
    }
    public AttributedCharacterIterator getACI() {
        return aci;
    }
    public GeneralPath getOutline() {
        GeneralPath outline = new GeneralPath();
        TextRunSegment segment;
        for (int i = 0; i < runSegments.size(); i++) {
            segment = runSegments.get(i);
            outline.append(segment.getOutline(), false);
        }
        return outline;
    }
    public TextHitInfo hitTest(float x, float y) {
        TextRunSegment segment;
        double endOfPrevSeg = -1;
        for (int i = 0; i < runSegments.size(); i++) {
            segment = runSegments.get(i);
            Rectangle2D bounds = segment.getVisualBounds();
            if ((bounds.getMinX() <= x && bounds.getMaxX() >= x) || 
               (endOfPrevSeg < x && bounds.getMinX() > x)) { 
                return segment.hitTest(x,y);
            }
            endOfPrevSeg = bounds.getMaxX();
        }
        return isLTR() ? TextHitInfo.trailing(text.length) : TextHitInfo.leading(0);
    }
    public float getJustification() {
        return justification;
    }
    public int getLastNonWhitespace() {
        int lastNonWhitespace = text.length;
        while (lastNonWhitespace >= 0) {
            lastNonWhitespace--;
            if (!Character.isWhitespace(text[lastNonWhitespace])) {
                break;
            }
        }
        return lastNonWhitespace;
    }
    public void justify(float gap) {
        int firstIdx = segmentsStart;
        int lastIdx = getLastNonWhitespace() + segmentsStart;
        JustificationInfo jInfos[] = new JustificationInfo[5];
        float gapLeft = gap;
        int highestPriority = -1;
        for (int priority = 0; priority <= GlyphJustificationInfo.PRIORITY_NONE + 1; priority++) {
            JustificationInfo jInfo = new JustificationInfo();
            jInfo.lastIdx = lastIdx;
            jInfo.firstIdx = firstIdx;
            jInfo.grow = gap > 0;
            jInfo.gapToFill = gapLeft;
            if (priority <= GlyphJustificationInfo.PRIORITY_NONE) {
                jInfo.priority = priority;
            } else {
                jInfo.priority = highestPriority; 
            }
            for (int i = 0; i < runSegments.size(); i++) {
                TextRunSegment segment = runSegments.get(i);
                if (segment.getStart() <= lastIdx) {
                    segment.updateJustificationInfo(jInfo);
                }
            }
            if (jInfo.priority == highestPriority) {
                jInfo.absorb = true;
                jInfo.absorbedWeight = jInfo.weight;
            }
            if (jInfo.weight != 0) {
                if (highestPriority < 0) {
                    highestPriority = priority;
                }
                jInfos[priority] = jInfo;
            } else {
                continue;
            }
            gapLeft -= jInfo.growLimit;
            if (((gapLeft > 0) ^ jInfo.grow) || gapLeft == 0) {
                gapLeft = 0;
                jInfo.gapPerUnit = jInfo.gapToFill/jInfo.weight;
                break;
            }
            jInfo.useLimits = true;
            if (jInfo.absorbedWeight > 0) {
                jInfo.absorb = true;
                jInfo.absorbedGapPerUnit =
                        (jInfo.gapToFill-jInfo.growLimit)/jInfo.absorbedWeight;
                break;
            }
        }
        float currJustificationOffset = 0;
        for (int i = 0; i < runSegments.size(); i++) {
            TextRunSegment segment =
                    runSegments.get(getSegmentFromVisualOrder(i));
            segment.x += currJustificationOffset;
            currJustificationOffset += segment.doJustification(jInfos);
        }
        justification = -1; 
    }
    class JustificationInfo {
        boolean grow;
        boolean absorb = false;
        boolean useLimits = false;
        int priority = 0;
        float weight = 0;
        float absorbedWeight = 0;
        float growLimit = 0;
        int lastIdx;
        int firstIdx;
        float gapToFill;
        float gapPerUnit = 0; 
        float absorbedGapPerUnit = 0; 
    }
}
