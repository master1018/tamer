public class TextMetricsCalculator {
    TextRunBreaker breaker; 
    float ascent = 0;
    float descent = 0;
    float leading = 0;
    float advance = 0;
    private float baselineOffsets[];
    int baselineIndex;
    public TextMetricsCalculator(TextRunBreaker breaker) {
        this.breaker = breaker;
        checkBaselines();
    }
    float getBaselineOffset(int baselineIndex) {
        if (baselineIndex >= 0) {
            return baselineOffsets[baselineIndex];
        } else if (baselineIndex == GraphicAttribute.BOTTOM_ALIGNMENT) {
            return descent;
        } else if (baselineIndex == GraphicAttribute.TOP_ALIGNMENT) {
            return -ascent;
        } else {
            throw new IllegalArgumentException(Messages.getString("awt.3F")); 
        }
    }
    public float[] getBaselineOffsets() {
        float ret[] = new float[baselineOffsets.length];
        System.arraycopy(baselineOffsets, 0, ret, 0, baselineOffsets.length);
        return ret;
    }
    public void checkBaselines() {
        HashMap<Integer, Font> fonts = breaker.fonts;
        Object val = fonts.get(new Integer(0));
        if (val instanceof Font) {
            Font firstFont = (Font) val;
            LineMetrics lm = firstFont.getLineMetrics(breaker.text, 0, 1, breaker.frc);
            baselineOffsets = lm.getBaselineOffsets();
            baselineIndex = lm.getBaselineIndex();
        } else if (val instanceof GraphicAttribute) {
            GraphicAttribute ga = (GraphicAttribute) val;
            int align = ga.getAlignment();
            if (
                    align == GraphicAttribute.TOP_ALIGNMENT ||
                    align == GraphicAttribute.BOTTOM_ALIGNMENT
            ) {
                baselineIndex = GraphicAttribute.ROMAN_BASELINE;
            } else {
                baselineIndex = align;
            }
            baselineOffsets = new float[3];
            baselineOffsets[0] = 0;
            baselineOffsets[1] = (ga.getDescent() - ga.getAscent()) / 2.f;
            baselineOffsets[2] = -ga.getAscent();
        } else { 
            baselineIndex = GraphicAttribute.ROMAN_BASELINE;
            baselineOffsets = new float[3];
        }
        if (baselineOffsets[baselineIndex] != 0) {
            float baseOffset = baselineOffsets[baselineIndex];
            for (int i = 0; i < baselineOffsets.length; i++) {
                baselineOffsets[i] -= baseOffset;
            }
        }
    }
    void computeMetrics() {
        ArrayList<TextRunSegment> segments = breaker.runSegments;
        float maxHeight = 0;
        float maxHeightLeading = 0;
        for (int i = 0; i < segments.size(); i++) {
            TextRunSegment segment = segments.get(i);
            BasicMetrics metrics = segment.metrics;
            int baseline = metrics.baseLineIndex;
            if (baseline >= 0) {
                float baselineOffset = baselineOffsets[metrics.baseLineIndex];
                float fixedDescent = metrics.descent + baselineOffset;
                ascent = Math.max(ascent, metrics.ascent - baselineOffset);
                descent = Math.max(descent, fixedDescent);
                leading = Math.max(leading, fixedDescent + metrics.leading);
            } else { 
                float height = metrics.ascent + metrics.descent;
                maxHeight = Math.max(maxHeight, height);
                maxHeightLeading = Math.max(maxHeightLeading, height + metrics.leading);
            }
        }
        if (maxHeightLeading != 0) {
            descent = Math.max(descent, maxHeight - ascent);
            leading = Math.max(leading, maxHeightLeading - ascent);
        }
        leading -= descent;
        BasicMetrics currMetrics;
        float currAdvance = 0;
        for (int i = 0; i < segments.size(); i++) {
            TextRunSegment segment = segments.get(breaker.getSegmentFromVisualOrder(i));
            currMetrics = segment.metrics;
            segment.y = getBaselineOffset(currMetrics.baseLineIndex)
                    + currMetrics.superScriptOffset;
            segment.x = currAdvance;
            currAdvance += segment.getAdvance();
        }
        advance = currAdvance;
    }
    public BasicMetrics createMetrics() {
        computeMetrics();
        return new BasicMetrics(this);
    }
    public void correctAdvance(BasicMetrics metrics) {
        ArrayList<TextRunSegment> segments = breaker.runSegments;
        TextRunSegment segment = segments.get(breaker
                .getSegmentFromVisualOrder(segments.size() - 1));
        advance = segment.x + segment.getAdvance();
        metrics.advance = advance;
    }
}
