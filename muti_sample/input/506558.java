public class CompositeFont extends FontPeerImpl{
    int numFonts;
    String family;
    String face;
    String[] fontNames;
    FontProperty[] fontProperties;
    public FontPeerImpl[] fPhysicalFonts;
    int missingGlyphCode = -1;
    LineMetricsImpl nlm = null;
    int cachedNumGlyphs = -1;
    public CompositeFont(String familyName, String faceName, int _style, int _size, FontProperty[] fProperties, FontPeerImpl[] physFonts){
        this.size = _size;
        this.name = faceName;
        this.family = familyName;
        this.style = _style;
        this.face = faceName;
        this.psName = faceName;
        this.fontProperties = fProperties;
        fPhysicalFonts = physFonts;
        numFonts = fPhysicalFonts.length; 
        setDefaultLineMetrics("", null); 
        this.uniformLM = false;
    }
    public int getCharFontIndex(char chr){
        for (int i = 0; i < numFonts; i++){
            if (fontProperties[i].isCharExcluded(chr)){
                continue;
            }
            if (fPhysicalFonts[i].canDisplay(chr)){
                return i;
            }
        }
        return -1;
    }
     public int getCharFontIndex(char chr, int defaultValue){
        for (int i = 0; i < numFonts; i++){
            if (fontProperties[i].isCharExcluded(chr)){
                continue;
            }
            if (fPhysicalFonts[i].canDisplay(chr)){
                return i;
            }
        }
        return defaultValue;
    }
    @Override
    public boolean canDisplay(char chr){
        return (getCharFontIndex(chr) != -1);
    }
    @Override
    public int getAscent(){
        return nlm.getLogicalAscent();
    }
     @Override
    public LineMetrics getLineMetrics(String str, FontRenderContext frc , AffineTransform at){
        LineMetricsImpl lm = (LineMetricsImpl)(this.nlm.clone());
        lm.setNumChars(str.length());
        if ((at != null) && (!at.isIdentity())){
            lm.scale((float)at.getScaleX(), (float)at.getScaleY());
        }
        return lm;
    }
    @Override
    public LineMetrics getLineMetrics(){
        if (nlm == null){
            setDefaultLineMetrics("", null); 
        }
        return this.nlm;
    }
    private void setDefaultLineMetrics(String str, FontRenderContext frc){
        LineMetrics lm = fPhysicalFonts[0].getLineMetrics(str, frc, null);
        float maxCharWidth = (float)fPhysicalFonts[0].getMaxCharBounds(frc).getWidth();
        if (numFonts == 1) {
            this.nlm = (LineMetricsImpl)lm;
            return;
        }
        float[] baselineOffsets = lm.getBaselineOffsets();
        int numChars = str.length();
        int baseLineIndex = lm.getBaselineIndex();
        float maxUnderlineThickness = lm.getUnderlineThickness();
        float maxUnderlineOffset = lm.getUnderlineOffset();
        float maxStrikethroughThickness = lm.getStrikethroughThickness();
        float minStrikethroughOffset = lm.getStrikethroughOffset();
        float maxLeading = lm.getLeading();  
        float maxHeight = lm.getHeight();   
        float maxAscent = lm.getAscent();   
        float maxDescent = lm.getDescent(); 
        for (int i = 1; i < numFonts; i++){
            lm = fPhysicalFonts[i].getLineMetrics(str, frc, null);
            if (maxUnderlineThickness < lm.getUnderlineThickness()){
                maxUnderlineThickness = lm.getUnderlineThickness();
            }
            if (maxUnderlineOffset < lm.getUnderlineOffset()){
                maxUnderlineOffset = lm.getUnderlineOffset();
            }
            if (maxStrikethroughThickness < lm.getStrikethroughThickness()){
                maxStrikethroughThickness = lm.getStrikethroughThickness();
            }
            if (minStrikethroughOffset > lm.getStrikethroughOffset()){
                minStrikethroughOffset = lm.getStrikethroughOffset();
            }
            if (maxLeading < lm.getLeading()){
                maxLeading = lm.getLeading();
            }
            if (maxAscent < lm.getAscent()){
                maxAscent = lm.getAscent();
            }
            if (maxDescent < lm.getDescent()){
                maxDescent = lm.getDescent();
            }
            float width = (float)fPhysicalFonts[i].getMaxCharBounds(frc).getWidth();
            if(maxCharWidth < width){
                maxCharWidth = width;
            }
            for (int j =0; j < baselineOffsets.length; j++){
                float[] offsets = lm.getBaselineOffsets();
                if (baselineOffsets[j] > offsets[j]){
                    baselineOffsets[j] = offsets[j];
                }
            }
        }
        maxHeight = maxAscent + maxDescent + maxLeading;
        this.nlm =  new LineMetricsImpl(
                numChars,
                baseLineIndex,
                baselineOffsets,
                maxUnderlineThickness,
                maxUnderlineOffset,
                maxStrikethroughThickness,
                minStrikethroughOffset,
                maxLeading,
                maxHeight,
                maxAscent,
                maxDescent,
                maxCharWidth);
    }
    @Override
    public int getNumGlyphs(){
        if (this.cachedNumGlyphs == -1){
            this.cachedNumGlyphs = 0;
            for (int i = 0; i < numFonts; i++){
                this.cachedNumGlyphs += fPhysicalFonts[i].getNumGlyphs();
            }
        }
        return this.cachedNumGlyphs;
    }
    @Override
    public float getItalicAngle(){
        return fPhysicalFonts[0].getItalicAngle();
    }
    public Rectangle2D getStringBounds(char[] chars, int start, int end, FontRenderContext frc){
        LineMetrics lm = getLineMetrics();
        float minY = -lm.getAscent();
        float minX = 0;
        float height = lm.getHeight();
        float width = 0;
        for (int i = start; i < end; i++){
            width += charWidth(chars[i]);
        }
        Rectangle2D rect2D = new Rectangle2D.Float(minX, minY, width, height);
        return rect2D;
    }
    @Override
    public Rectangle2D getMaxCharBounds(FontRenderContext frc){
        Rectangle2D rect2D = fPhysicalFonts[0].getMaxCharBounds(frc);
        float minY = (float)rect2D.getY();
        float maxWidth = (float)rect2D.getWidth();
        float maxHeight = (float)rect2D.getHeight();
        if (numFonts == 1){
            return rect2D;
        }
        for (int i = 1; i < numFonts; i++){
            if (fPhysicalFonts[i] != null){
                rect2D = fPhysicalFonts[i].getMaxCharBounds(frc);
                float y = (float)rect2D.getY();
                float mWidth = (float)rect2D.getWidth();
                float mHeight = (float)rect2D.getHeight();
                if (y < minY){
                    minY = y;
                }
                if (mWidth > maxWidth){
                    maxHeight = mWidth;
                }
                if (mHeight > maxHeight){
                    maxHeight = mHeight;
                }
            }
        }
        rect2D = new Rectangle2D.Float(0, minY, maxWidth, maxHeight);
        return rect2D;
    }
    @Override
    public String getFontName(){
        return face;
    }
    @Override
    public String getPSName(){
        return psName;
    }
    @Override
    public String getFamily(){
        return family;
    }
    @Override
    public int getMissingGlyphCode(){
        return fPhysicalFonts[0].getMissingGlyphCode();
    }
    @Override
    public Glyph getGlyph(char ch){
        for (int i = 0; i < numFonts; i++){
            if (fontProperties[i].isCharExcluded(ch)){
                    continue;
            }
            if ((ch < 0x20) || fPhysicalFonts[i].canDisplay(ch)){
                return fPhysicalFonts[i].getGlyph(ch);
            }
        }
        return getDefaultGlyph();
    }
    @Override
    public int charWidth(int ind){
        return charWidth((char)ind);
    }
    @Override
    public int charWidth(char c){
        Glyph gl = this.getGlyph(c);
        return (int)gl.getGlyphPointMetrics().getAdvanceX();
    }
    @Override
    public String toString(){
    return new String(this.getClass().getName() +
            "[name=" + this.name + 
            ",style="+ this.style + 
            ",fps=" + this.fontProperties + "]"); 
    }
    @Override
    public Glyph getDefaultGlyph(){
        return fPhysicalFonts[0].getDefaultGlyph();
    }
    @Override
    public FontExtraMetrics getExtraMetrics(){
        return fPhysicalFonts[0].getExtraMetrics();
    }
    @Override
    public void dispose() {
    }
}
