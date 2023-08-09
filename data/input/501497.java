public class LineMetricsImpl extends LineMetrics implements Cloneable{
    float[] baselineOffsets;
    int numChars;
    int baseLineIndex;
    float underlineThickness;
    float underlineOffset;
    float strikethroughThickness;
    float strikethroughOffset;
    float leading;
    float height;
    float ascent;
    float descent;
    float maxCharWidth;
    int lUnderlineThickness;
    int lUnderlineOffset;
    int lStrikethroughThickness;
    int lStrikethroughOffset;
    int lLeading;
    int lHeight;
    int lAscent;
    int lDescent;
    int lMaxCharWidth;
    int units_per_EM = 0;
    public LineMetricsImpl(int len, float[] metrics, float[] _baselineData){
        numChars = len;
        ascent = metrics[0];    
        descent = metrics[1];   
        leading = metrics[2];  
        height = metrics[0] + metrics[1] + metrics[2];  
    }
    public LineMetricsImpl(int _numChars, int _baseLineIndex,
            float[] _baselineOffsets, float _underlineThickness,
            float _underlineOffset, float _strikethroughThickness,
            float _strikethroughOffset, float _leading, float _height,
            float _ascent, float _descent, float _maxCharWidth) {
        numChars = _numChars;
        baseLineIndex = _baseLineIndex;
        underlineThickness = _underlineThickness;
        underlineOffset = _underlineOffset;
        strikethroughThickness = _strikethroughThickness;
        strikethroughOffset = _strikethroughOffset;
        leading = _leading;
        height = _height;
        ascent = _ascent;
        descent = _descent;
        baselineOffsets = _baselineOffsets;
        lUnderlineThickness = (int) underlineThickness;
        lUnderlineOffset = (int) underlineOffset;
        lStrikethroughThickness = (int) strikethroughThickness;
        lStrikethroughOffset = (int) strikethroughOffset;
        lLeading = (int) leading;
        lHeight = (int) height;
        lAscent = (int) ascent;
        lDescent = (int) descent;
        maxCharWidth = _maxCharWidth;
    }
    public LineMetricsImpl(){
    }
    public void scale(float scaleX, float scaleY){
        float absScaleX = Math.abs(scaleX);
        float absScaleY = Math.abs(scaleY);
        underlineThickness *= absScaleY;
        underlineOffset *= scaleY;
        strikethroughThickness *= absScaleY;
        strikethroughOffset *= scaleY;
        leading *= absScaleY;
        height *= absScaleY;
        ascent *= absScaleY;
        descent *= absScaleY;
        if(baselineOffsets == null) {
            getBaselineOffsets();
        }
        for (int i=0; i< baselineOffsets.length; i++){
            baselineOffsets[i] *= scaleY;
        }
        lUnderlineThickness *= absScaleY;
        lUnderlineOffset *= scaleY;
        lStrikethroughThickness *= absScaleY;
        lStrikethroughOffset *= scaleY;
        lLeading  *= absScaleY;
        lHeight *= absScaleY;
        lAscent *= absScaleY;
        lDescent *= absScaleY;
        maxCharWidth *= absScaleX;
    }
    @Override
    public float[] getBaselineOffsets() {
        return baselineOffsets;
    }
    @Override
    public int getNumChars() {
        return numChars;
    }
    @Override
    public int getBaselineIndex() {
        return baseLineIndex;
    }
    @Override
    public float getUnderlineThickness() {
        return underlineThickness;
    }
    @Override
    public float getUnderlineOffset() {
        return underlineOffset;
    }
    @Override
    public float getStrikethroughThickness() {
        return strikethroughThickness;
    }
    @Override
    public float getStrikethroughOffset() {
        return strikethroughOffset;
    }
    @Override
    public float getLeading() {
        return leading;
    }
    @Override
    public float getHeight() {
    	return ascent + descent + leading;
    }
    @Override
    public float getDescent() {
        return descent;
    }
    @Override
    public float getAscent() {
        return ascent;
    }
    public int getLogicalUnderlineThickness() {
        return lUnderlineThickness;
    }
    public int getLogicalUnderlineOffset() {
        return lUnderlineOffset;
    }
    public int getLogicalStrikethroughThickness() {
        return lStrikethroughThickness;
    }
    public int getLogicalStrikethroughOffset() {
        return lStrikethroughOffset;
    }
    public int getLogicalLeading() {
        return lLeading;
    }
    public int getLogicalHeight() {
        return lHeight; 
    }
    public int getLogicalDescent() {
        return lDescent;
    }
    public int getLogicalAscent() {
        return lAscent;
    }
    public int getLogicalMaxCharWidth() {
        return lMaxCharWidth;
    }
    public float getMaxCharWidth() {
        return maxCharWidth;
    }
    public void setNumChars(int num){
        numChars = num;
    }
    @Override
    public Object clone(){
        try{
            return super.clone();
        }catch (CloneNotSupportedException e){
            return null;
        }
    }
}