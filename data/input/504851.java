public class BasicMetrics {
    int baseLineIndex;
    float ascent;   
    float descent;  
    float leading;  
    float advance;
    float italicAngle;
    float superScriptOffset;
    float underlineOffset;
    float underlineThickness;
    float strikethroughOffset;
    float strikethroughThickness;
    BasicMetrics(LineMetrics lm, Font font) {
        ascent = lm.getAscent();
        descent = lm.getDescent();
        leading = lm.getLeading();
        underlineOffset = lm.getUnderlineOffset();
        underlineThickness = lm.getUnderlineThickness();
        strikethroughOffset = lm.getStrikethroughOffset();
        strikethroughThickness = lm.getStrikethroughThickness();
        baseLineIndex = lm.getBaselineIndex();
        italicAngle = font.getItalicAngle();
        superScriptOffset = (float) font.getTransform().getTranslateY();
    }
    BasicMetrics(GraphicAttribute ga) {
        ascent = ga.getAscent();
        descent = ga.getDescent();
        leading = 2;
        baseLineIndex = ga.getAlignment();
        italicAngle = 0;
        superScriptOffset = 0;
        underlineOffset = Math.max(descent/2, 1);
        underlineThickness = Math.max(ascent/13, 1);
        strikethroughOffset = -ascent/2; 
        strikethroughThickness = underlineThickness;
    }
    BasicMetrics(TextMetricsCalculator tmc) {
        ascent = tmc.ascent;
        descent = tmc.descent;
        leading = tmc.leading;
        advance = tmc.advance;
        baseLineIndex = tmc.baselineIndex;
    }
    public float getAscent() {
        return ascent;
    }
    public float getDescent() {
        return descent;
    }
    public float getLeading() {
        return leading;
    }
    public float getAdvance() {
        return advance;
    }
    public int getBaseLineIndex() {
        return baseLineIndex;
    }
}
