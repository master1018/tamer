public class TextRunSegmentImpl {
    public static class TextSegmentInfo {
        Font font;
        FontRenderContext frc;
        char text[];
        int start;
        int end;
        int length;
        int flags = 0;
        byte level = 0;
        TextSegmentInfo(
                byte level,
                Font font, FontRenderContext frc,
                char text[], int start, int end
        ) {
            this.font = font;
            this.frc = frc;
            this.text = text;
            this.start = start;
            this.end = end;
            this.level = level;
            length = end - start;
        }
    }
    public static class TextRunSegmentCommon extends TextRunSegment {
        TextSegmentInfo info;
        private GlyphVector gv;
        private float advanceIncrements[];
        private int char2glyph[];
        private GlyphJustificationInfo gjis[]; 
        TextRunSegmentCommon(TextSegmentInfo i, TextDecorator.Decoration d) {
            i.flags &= ~0x09; 
            if ((i.level & 0x1) != 0) {
                i.flags |= Font.LAYOUT_RIGHT_TO_LEFT;
            }
            info = i;
            this.decoration = d;
            LineMetrics lm = i.font.getLineMetrics(i.text, i.start, i.end, i.frc);
            this.metrics = new BasicMetrics(lm, i.font);
            if (lm.getNumChars() != i.length) { 
                throw new UnsupportedOperationException(
                        Messages.getString("awt.41")); 
            }
        }
        @Override
        public Object clone() {
            return new TextRunSegmentCommon(info, decoration);
        }
        private GlyphVector getGlyphVector() {
            if (gv==null) {
                gv = info.font.layoutGlyphVector(
                        info.frc,
                        info.text,
                        info.start,
                        info.end - info.start, 
                        info.flags
                );
            }
            return gv;
        }
        @Override
        void draw(Graphics2D g2d, float xOffset, float yOffset) {
            if (decoration == null) {
                g2d.drawGlyphVector(getGlyphVector(), xOffset + x, yOffset + y);
            } else {
                TextDecorator.prepareGraphics(this, g2d, xOffset, yOffset);
                g2d.drawGlyphVector(getGlyphVector(), xOffset + x, yOffset + y);
                TextDecorator.drawTextDecorations(this, g2d, xOffset, yOffset);
                TextDecorator.restoreGraphics(decoration, g2d);
            }
        }
        @Override
        Rectangle2D getVisualBounds() {
            if (visualBounds == null) {
                visualBounds =
                        TextDecorator.extendVisualBounds(
                                this,
                                getGlyphVector().getVisualBounds(),
                                decoration
                        );
                visualBounds.setRect(
                        x + visualBounds.getX(),
                        y + visualBounds.getY(),
                        visualBounds.getWidth(),
                        visualBounds.getHeight()
                );
            }
            return (Rectangle2D) visualBounds.clone();
        }
        @Override
        Rectangle2D getLogicalBounds() {
            if (logicalBounds == null) {
                logicalBounds = getGlyphVector().getLogicalBounds();
                logicalBounds.setRect(
                        x + logicalBounds.getX(),
                        y + logicalBounds.getY(),
                        logicalBounds.getWidth(),
                        logicalBounds.getHeight()
                );
            }
            return (Rectangle2D) logicalBounds.clone();
        }
        @Override
        float getAdvance() {
            return (float) getLogicalBounds().getWidth();
        }
        void initAdvanceMapping() {
            GlyphVector gv = getGlyphVector();
            int charIndicies[] = gv.getGlyphCharIndices(0, gv.getNumGlyphs(), null);
            advanceIncrements = new float[info.length];
            for (int i=0; i<charIndicies.length; i++) {
                advanceIncrements[charIndicies[i]] = gv.getGlyphMetrics(i).getAdvance();
            }
        }
        @Override
        float getAdvanceDelta(int start, int end) {
            start -= info.start;
            end -= info.start;
            if (advanceIncrements == null) {
                initAdvanceMapping();
            }
            if (start < 0) {
                start = 0;
            }
            if (end > info.length) {
                end = info.length;
            }
            float sum = 0;
            for (int i=start; i<end; i++) {
                sum += advanceIncrements[i];
            }
            return sum;
        }
        @Override
        int getCharIndexFromAdvance(float advance, int start) {
            if (advanceIncrements == null) {
                initAdvanceMapping();
            }
            start -= info.start;
            if (start < 0) {
                start = 0;
            }
            int i = start;
            for (; i<info.length; i++) {
                advance -= advanceIncrements[i];
                if (advance < 0) {
                    break;
                }
            }
            return i + info.start;
        }
        @Override
        int getStart() {
            return info.start;
        }
        @Override
        int getEnd() {
            return info.end;
        }
        @Override
        int getLength() {
            return info.length;
        }
        private int[] getChar2Glyph() {
            if (char2glyph == null) {
                GlyphVector gv = getGlyphVector();
                char2glyph = new int[info.length];
                Arrays.fill(char2glyph, -1);
                int charIndicies[] = gv.getGlyphCharIndices(0, gv.getNumGlyphs(), null);
                for (int i=0; i<charIndicies.length; i++) {
                    char2glyph[charIndicies[i]] = i;
                }
                int currIndex = 0;
                for (int i=0; i<char2glyph.length; i++) {
                    if (char2glyph[i] < 0) {
                        char2glyph[i] = currIndex;
                    } else {
                        currIndex = char2glyph[i];
                    }
                }
            }
            return char2glyph;
        }
        @Override
        Shape getCharsBlackBoxBounds(int start, int limit) {
            start -= info.start;
            limit -= info.start;
            if (limit > info.length) {
                limit = info.length;
            }
            GeneralPath result = new GeneralPath();
            int glyphIndex = 0;
            for (int i=start; i<limit; i++) {
                glyphIndex = getChar2Glyph()[i];
                result.append(getGlyphVector().getGlyphVisualBounds(glyphIndex), false);
            }
            result.transform(AffineTransform.getTranslateInstance(x, y));
            return result;
        }
        @Override
        float getCharPosition(int index) {
            index -= info.start;
            if (index > info.length) {
                index = info.length;
            }
            float result = 0;
            int glyphIndex = getChar2Glyph()[index];
            result = (float) getGlyphVector().getGlyphPosition(glyphIndex).getX();
            result += x;
            return result;
        }
        @Override
        float getCharAdvance(int index) {
            if (advanceIncrements == null) {
                initAdvanceMapping();
            }
            return advanceIncrements[index - this.getStart()];
        }
        @Override
        Shape getOutline() {
            AffineTransform t = AffineTransform.getTranslateInstance(x, y);
            return t.createTransformedShape(
                    TextDecorator.extendOutline(
                            this,
                            getGlyphVector().getOutline(),
                            decoration
                    )
            );
        }
        @Override
        boolean charHasZeroAdvance(int index) {
            if (advanceIncrements == null) {
                initAdvanceMapping();
            }
            return advanceIncrements[index - this.getStart()] == 0;
        }
        @Override
        TextHitInfo hitTest(float hitX, float hitY) {
            hitX -= x;
            float glyphPositions[] =
                    getGlyphVector().getGlyphPositions(0, info.length+1, null);
            int glyphIdx;
            boolean leading = false;
            for (glyphIdx = 1; glyphIdx <= info.length; glyphIdx++) {
                if (glyphPositions[(glyphIdx)*2] >= hitX) {
                    float advance =
                            glyphPositions[(glyphIdx)*2] - glyphPositions[(glyphIdx-1)*2];
                    leading = glyphPositions[(glyphIdx-1)*2] + advance/2 > hitX ? true : false;
                    glyphIdx--;
                    break;
                }
            }
            if (glyphIdx == info.length) {
                glyphIdx--;
            }
            int charIdx = getGlyphVector().getGlyphCharIndex(glyphIdx);
            return (leading) ^ ((info.level & 0x1) == 0x1)?
                    TextHitInfo.leading(charIdx + info.start) :
                    TextHitInfo.trailing(charIdx + info.start);
        }
        private GlyphJustificationInfo[] getGlyphJustificationInfos() {
            if (gjis == null) {
                GlyphVector gv = getGlyphVector();
                int nGlyphs = gv.getNumGlyphs();
                int charIndicies[] = gv.getGlyphCharIndices(0, nGlyphs, null);
                gjis = new GlyphJustificationInfo[nGlyphs];
                float fontSize = info.font.getSize2D();
                GlyphJustificationInfo defaultInfo =
                        new GlyphJustificationInfo(
                                0, 
                                false, GlyphJustificationInfo.PRIORITY_NONE, 0, 0, 
                                false, GlyphJustificationInfo.PRIORITY_NONE, 0, 0); 
                GlyphJustificationInfo spaceInfo = new GlyphJustificationInfo(
                        fontSize, 
                        true, GlyphJustificationInfo.PRIORITY_WHITESPACE, 0, fontSize, 
                        true, GlyphJustificationInfo.PRIORITY_WHITESPACE, 0, fontSize); 
                for (int i = 0; i < nGlyphs; i++) {
                    char c = info.text[charIndicies[i] + info.start];
                    if (Character.isWhitespace(c)) {
                        gjis[i] = spaceInfo;
                    } else {
                        gjis[i] = defaultInfo;
                    }
                }
            }
            return gjis;
        }
        @Override
        void updateJustificationInfo(TextRunBreaker.JustificationInfo jInfo) {
            int lastChar = Math.min(jInfo.lastIdx, info.end) - info.start;
            boolean haveFirst = info.start <= jInfo.firstIdx;
            boolean haveLast = info.end >= (jInfo.lastIdx + 1);
            int prevGlyphIdx = -1;
            int currGlyphIdx;
            if (jInfo.grow) { 
                for (int i=0; i<lastChar; i++) {
                    currGlyphIdx = getChar2Glyph()[i];
                    if (currGlyphIdx == prevGlyphIdx) {
                        continue;
                    }
                    prevGlyphIdx = currGlyphIdx;
                    GlyphJustificationInfo gji = getGlyphJustificationInfos()[currGlyphIdx];
                    if (gji.growPriority == jInfo.priority) {
                        jInfo.weight += gji.weight * 2;
                        jInfo.growLimit += gji.growLeftLimit;
                        jInfo.growLimit += gji.growRightLimit;
                        if (gji.growAbsorb) {
                            jInfo.absorbedWeight += gji.weight * 2;
                        }
                    }
                }
            } else {
                for (int i=0; i<lastChar; i++) {
                    currGlyphIdx = getChar2Glyph()[i];
                    if (currGlyphIdx == prevGlyphIdx) {
                        continue;
                    }
                    prevGlyphIdx = currGlyphIdx;
                    GlyphJustificationInfo gji = getGlyphJustificationInfos()[currGlyphIdx];
                    if (gji.shrinkPriority == jInfo.priority) {
                        jInfo.weight += gji.weight * 2;
                        jInfo.growLimit -= gji.shrinkLeftLimit;
                        jInfo.growLimit -= gji.shrinkRightLimit;
                        if (gji.shrinkAbsorb) {
                            jInfo.absorbedWeight += gji.weight * 2;
                        }
                    }
                }
            }
            if (haveFirst) {  
                GlyphJustificationInfo gji = getGlyphJustificationInfos()[getChar2Glyph()[0]];
                jInfo.weight -= gji.weight;
                if (jInfo.grow) {
                    jInfo.growLimit -= gji.growLeftLimit;
                    if (gji.growAbsorb) {
                        jInfo.absorbedWeight -= gji.weight;
                    }
                } else {
                    jInfo.growLimit += gji.shrinkLeftLimit;
                    if (gji.shrinkAbsorb) {
                        jInfo.absorbedWeight -= gji.weight;
                    }
                }
            }
            if (haveLast) {   
                GlyphJustificationInfo gji =
                        getGlyphJustificationInfos()[getChar2Glyph()[lastChar]];
                jInfo.weight -= gji.weight;
                if (jInfo.grow) {
                    jInfo.growLimit -= gji.growRightLimit;
                    if (gji.growAbsorb) {
                        jInfo.absorbedWeight -= gji.weight;
                    }
                } else {
                    jInfo.growLimit += gji.shrinkRightLimit;
                    if (gji.shrinkAbsorb) {
                        jInfo.absorbedWeight -= gji.weight;
                    }
                }
            }
        }
        @Override
        float doJustification(TextRunBreaker.JustificationInfo jInfos[]) {
            int lastPriority =
                    jInfos[jInfos.length-1] == null ?
                    -1 : jInfos[jInfos.length-1].priority;
            int highestPriority = 0;
            for (; highestPriority<jInfos.length; highestPriority++) {
                if (jInfos[highestPriority] != null) {
                    break;
                }
            }
            if (highestPriority == jInfos.length) {
                return 0;
            }
            TextRunBreaker.JustificationInfo firstInfo = jInfos[highestPriority];
            TextRunBreaker.JustificationInfo lastInfo =
                    lastPriority > 0 ? jInfos[lastPriority] : null;
            boolean haveFirst = info.start <= firstInfo.firstIdx;
            boolean haveLast = info.end >= (firstInfo.lastIdx + 1);
            int firstGlyph = haveFirst ?
                    getChar2Glyph()[firstInfo.firstIdx - info.start] :
                    getChar2Glyph()[0];
            int lastGlyph = haveLast ?
                    getChar2Glyph()[firstInfo.lastIdx - info.start] :
                    getChar2Glyph()[info.length - 1];
            if (haveLast) {
                lastGlyph--;
            }
            TextRunBreaker.JustificationInfo currInfo;
            float glyphOffset = 0;
            float positionIncrement = 0;
            float sideIncrement = 0;
            if (haveFirst) {  
                GlyphJustificationInfo gji = getGlyphJustificationInfos()[firstGlyph];
                currInfo = jInfos[gji.growPriority];
                if (currInfo != null) {
                    if (currInfo.useLimits) {
                        if (currInfo.absorb) {
                            glyphOffset += gji.weight * currInfo.absorbedGapPerUnit;
                        } else if (
                                lastInfo != null &&
                                lastInfo.priority == currInfo.priority
                        ) {
                            glyphOffset += gji.weight * lastInfo.absorbedGapPerUnit;
                        }
                        glyphOffset +=
                                firstInfo.grow ?
                                gji.growRightLimit :
                                -gji.shrinkRightLimit;
                    } else {
                        glyphOffset += gji.weight * currInfo.gapPerUnit;
                    }
                }
                firstGlyph++;
            }
            if (firstInfo.grow) {
                for (int i=firstGlyph; i<=lastGlyph; i++) {
                    GlyphJustificationInfo gji = getGlyphJustificationInfos()[i];
                    currInfo = jInfos[gji.growPriority];
                    if (currInfo == null) {
                        Point2D glyphPos = getGlyphVector().getGlyphPosition(i);
                        glyphPos.setLocation(glyphPos.getX() + glyphOffset, glyphPos.getY());
                        getGlyphVector().setGlyphPosition(i, glyphPos);
                        continue;
                    }
                    if (currInfo.useLimits) {
                        glyphOffset += gji.growLeftLimit;
                        if (currInfo.absorb) {
                            sideIncrement = gji.weight * currInfo.absorbedGapPerUnit;
                            glyphOffset += sideIncrement;
                            positionIncrement = glyphOffset;
                            glyphOffset += sideIncrement;
                        } else if (lastInfo != null && lastInfo.priority == currInfo.priority) {
                            sideIncrement = gji.weight * lastInfo.absorbedGapPerUnit;
                            glyphOffset += sideIncrement;
                            positionIncrement = glyphOffset;
                            glyphOffset += sideIncrement;
                        } else {
                            positionIncrement = glyphOffset;
                        }
                        glyphOffset += gji.growRightLimit;
                    } else {
                        sideIncrement = gji.weight * currInfo.gapPerUnit;
                        glyphOffset += sideIncrement;
                        positionIncrement = glyphOffset;
                        glyphOffset += sideIncrement;
                    }
                    Point2D glyphPos = getGlyphVector().getGlyphPosition(i);
                    glyphPos.setLocation(glyphPos.getX() + positionIncrement, glyphPos.getY());
                    getGlyphVector().setGlyphPosition(i, glyphPos);
                }
            } else {
                for (int i=firstGlyph; i<=lastGlyph; i++) {
                    GlyphJustificationInfo gji = getGlyphJustificationInfos()[i];
                    currInfo = jInfos[gji.shrinkPriority];
                    if (currInfo == null) {
                        Point2D glyphPos = getGlyphVector().getGlyphPosition(i);
                        glyphPos.setLocation(glyphPos.getX() + glyphOffset, glyphPos.getY());
                        getGlyphVector().setGlyphPosition(i, glyphPos);
                        continue;
                    }
                    if (currInfo.useLimits) {
                        glyphOffset -= gji.shrinkLeftLimit;
                        if (currInfo.absorb) {
                            sideIncrement = gji.weight * currInfo.absorbedGapPerUnit;
                            glyphOffset += sideIncrement;
                            positionIncrement = glyphOffset;
                            glyphOffset += sideIncrement;
                        } else if (lastInfo != null && lastInfo.priority == currInfo.priority) {
                            sideIncrement = gji.weight * lastInfo.absorbedGapPerUnit;
                            glyphOffset += sideIncrement;
                            positionIncrement = glyphOffset;
                            glyphOffset += sideIncrement;
                        } else {
                            positionIncrement = glyphOffset;
                        }
                        glyphOffset -= gji.shrinkRightLimit;
                    } else {
                        sideIncrement =  gji.weight * currInfo.gapPerUnit;
                        glyphOffset += sideIncrement;
                        positionIncrement = glyphOffset;
                        glyphOffset += sideIncrement;
                    }
                    Point2D glyphPos = getGlyphVector().getGlyphPosition(i);
                    glyphPos.setLocation(glyphPos.getX() + positionIncrement, glyphPos.getY());
                    getGlyphVector().setGlyphPosition(i, glyphPos);
                }
            }
            if (haveLast) {   
                lastGlyph++;
                GlyphJustificationInfo gji = getGlyphJustificationInfos()[lastGlyph];
                currInfo = jInfos[gji.growPriority];
                if (currInfo != null) {
                    if (currInfo.useLimits) {
                        glyphOffset += firstInfo.grow ? gji.growLeftLimit : -gji.shrinkLeftLimit;
                        if (currInfo.absorb) {
                            glyphOffset += gji.weight * currInfo.absorbedGapPerUnit;
                        } else if (lastInfo != null && lastInfo.priority == currInfo.priority) {
                            glyphOffset += gji.weight * lastInfo.absorbedGapPerUnit;
                        }
                    } else {
                        glyphOffset += gji.weight * currInfo.gapPerUnit;
                    }
                }
                for (int i=lastGlyph; i<getGlyphVector().getNumGlyphs()+1; i++) {
                    Point2D glyphPos = getGlyphVector().getGlyphPosition(i);
                    glyphPos.setLocation(glyphPos.getX() + glyphOffset, glyphPos.getY());
                    getGlyphVector().setGlyphPosition(i, glyphPos);
                }
            } else { 
                Point2D glyphPos = getGlyphVector().getGlyphPosition(lastGlyph+1);
                glyphPos.setLocation(glyphPos.getX() + glyphOffset, glyphPos.getY());
                getGlyphVector().setGlyphPosition(lastGlyph+1, glyphPos);
            }
            gjis = null; 
            this.visualBounds = null;
            this.logicalBounds = null;
            return glyphOffset; 
        }
    }
    public static class TextRunSegmentGraphic extends TextRunSegment {
        GraphicAttribute ga;
        int start;
        int length;
        float fullAdvance;
        TextRunSegmentGraphic(GraphicAttribute attr, int len, int start) {
            this.start = start;
            length = len;
            ga = attr;
            metrics = new BasicMetrics(ga);
            fullAdvance = ga.getAdvance() * length;
        }
        @Override
        public Object clone() {
            return new TextRunSegmentGraphic(ga, length, start);
        }
        @Override
        void draw(Graphics2D g2d, float xOffset, float yOffset) {
            if (decoration != null) {
                TextDecorator.prepareGraphics(this, g2d, xOffset, yOffset);
            }
            float xPos = x + xOffset;
            float yPos = y + yOffset;
            for (int i=0; i < length; i++) {
                ga.draw(g2d, xPos, yPos);
                xPos += ga.getAdvance();
            }
            if (decoration != null) {
                TextDecorator.drawTextDecorations(this, g2d, xOffset, yOffset);
                TextDecorator.restoreGraphics(decoration, g2d);
            }
        }
        @Override
        Rectangle2D getVisualBounds() {
            if (visualBounds == null) {
                Rectangle2D bounds = ga.getBounds();
                bounds.setRect(
                        bounds.getMinX() + x,
                        bounds.getMinY() + y,
                        bounds.getWidth() - ga.getAdvance() + getAdvance(),
                        bounds.getHeight()
                );
                visualBounds = TextDecorator.extendVisualBounds(this, bounds, decoration);
            }
            return (Rectangle2D) visualBounds.clone();
        }
        @Override
        Rectangle2D getLogicalBounds() {
            if (logicalBounds == null) {
                logicalBounds =
                        new Rectangle2D.Float(
                                x, y - metrics.ascent,
                                getAdvance(), metrics.ascent + metrics.descent
                        );
            }
            return (Rectangle2D) logicalBounds.clone();
        }
        @Override
        float getAdvance() {
            return fullAdvance;
        }
        @Override
        float getAdvanceDelta(int start, int end) {
            return ga.getAdvance() * (end - start);
        }
        @Override
        int getCharIndexFromAdvance(float advance, int start) {
            start -= this.start;
            if (start < 0) {
                start = 0;
            }
            int charOffset = (int) (advance/ga.getAdvance());
            if (charOffset + start > length) {
                return length + this.start;
            }
            return charOffset + start + this.start;
        }
        @Override
        int getStart() {
            return start;
        }
        @Override
        int getEnd() {
            return start + length;
        }
        @Override
        int getLength() {
            return length;
        }
        @Override
        Shape getCharsBlackBoxBounds(int start, int limit) {
            start -= this.start;
            limit -= this.start;
            if (limit > length) {
                limit = length;
            }
            Rectangle2D charBounds = ga.getBounds();
            charBounds.setRect(
                    charBounds.getX() + ga.getAdvance() * start + x,
                    charBounds.getY() + y,
                    charBounds.getWidth() + ga.getAdvance() * (limit - start),
                    charBounds.getHeight()
            );
            return charBounds;
        }
        @Override
        float getCharPosition(int index) {
            index -= start;
            if (index > length) {
                index = length;
            }
            return ga.getAdvance() * index + x;
        }
        @Override
        float getCharAdvance(int index) {
            return ga.getAdvance();
        }
        @Override
        Shape getOutline() {
            AffineTransform t = AffineTransform.getTranslateInstance(x, y);
            return t.createTransformedShape(
                    TextDecorator.extendOutline(this, getVisualBounds(), decoration)
            );
        }
        @Override
        boolean charHasZeroAdvance(int index) {
            return false;
        }
        @Override
        TextHitInfo hitTest(float hitX, float hitY) {
            hitX -= x;
            float tmp = hitX / ga.getAdvance();
            int hitIndex = Math.round(tmp);
            if (tmp > hitIndex) {
                return TextHitInfo.leading(hitIndex + this.start);
            }
            return TextHitInfo.trailing(hitIndex + this.start);
        }
        @Override
        void updateJustificationInfo(TextRunBreaker.JustificationInfo jInfo) {
        }
        @Override
        float doJustification(TextRunBreaker.JustificationInfo jInfos[]) {
            return 0;
        }
    }
}
