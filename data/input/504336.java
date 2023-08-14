public class AndroidLineMetrics extends LineMetricsImpl {
    public AndroidLineMetrics(    AndroidFont fnt,
                                FontRenderContext frc,
                                String str){
        numChars = str.length();
        baseLineIndex = 0;
        ascent = fnt.ascent;    
        descent = -fnt.descent;  
        leading = fnt.leading;  
        height = ascent + descent + leading;    
        underlineThickness = 0.0f;
        underlineOffset = 0.0f;
        strikethroughThickness = 0.0f;
        strikethroughOffset = 0.0f;
        maxCharWidth = 0.0f;
        lAscent = (int)Math.ceil(fnt.ascent);
        lDescent = -(int)Math.ceil(fnt.descent);
        lLeading = (int)Math.ceil(leading);  
        lHeight = lAscent + lDescent + lLeading;    
        lUnderlineThickness = Math.round(underlineThickness);
        if (underlineOffset >= 0){
            lUnderlineOffset = (int)Math.ceil(underlineOffset);
        } else {
            lUnderlineOffset = (int)Math.floor(underlineOffset);
        }
        lStrikethroughThickness = Math.round(strikethroughThickness); 
        if (strikethroughOffset >= 0){
            lStrikethroughOffset = (int)Math.ceil(strikethroughOffset);
        } else {
            lStrikethroughOffset = (int)Math.floor(strikethroughOffset);
        }
        lMaxCharWidth = (int)Math.ceil(maxCharWidth); 
        units_per_EM = 0;
    }
    public float[] getBaselineOffsets() {
        if (baselineOffsets == null){
            float[] baselineData = null;
                baseLineIndex = 0;
                baselineOffsets = new float[]{0, (-ascent+descent)/2, -ascent};
        }
        return baselineOffsets;
    }
    public int getBaselineIndex() {
        if (baselineOffsets == null){
            getBaselineOffsets();
        }
        return baseLineIndex;
    }
}
