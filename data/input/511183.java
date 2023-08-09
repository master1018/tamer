public abstract class FontPeerImpl implements FontPeer{
    int ascent;
    int descent;
    int leading;
    int maxAdvance;
    float height;
    int style;
    int size;
    int logicalHeight;
    String name;
    String fontFamilyName;
    String faceName;
    Rectangle2D maxCharBounds;
    float italicAngle = 0.0f;
    int numGlyphs = 0;
    long pFont;
    LineMetricsImpl nlm;
    String psName = null;
    public char defaultChar = (char)0xFFFF;
    boolean uniformLM = true;
    int fontType = FontManager.FONT_TYPE_UNDEF;
    private boolean createdFromStream = false;  
    private String tempFontFileName = null;     
    FontExtraMetrics extraMetrix = null;
    public abstract FontExtraMetrics getExtraMetrics();
    public abstract LineMetrics getLineMetrics(String str, FontRenderContext frc, AffineTransform at);
    public abstract String getPSName();
    public void setPSName(String name){
        this.psName = name;
    }
    public abstract int getMissingGlyphCode();
    public abstract Glyph getGlyph(char ch);
    public abstract void dispose();
    public abstract Glyph getDefaultGlyph();
    public abstract boolean canDisplay(char c);
    public String getFamily(Locale l){
        return this.getFamily();
    }
    public void setFamily(String familyName){
        this.fontFamilyName = familyName;
    }
    public String getFontName(Locale l){
        return this.getFontName();
    }
    public void setFontName(String fontName){
        this.faceName = fontName;
    }
    public boolean isCreatedFromStream(){
        return this.createdFromStream;
    }
    public void setCreatedFromStream(boolean value){
        this.createdFromStream = value;
    }
    public String getTempFontFileName(){
        return this.tempFontFileName;
    }
    public void setFontFileName(String value){
        this.tempFontFileName = value;
    }
    public int charWidth(char ch) {
    	Paint p;
    	AndroidGraphics2D g = AndroidGraphics2D.getInstance();
    	if(g == null) {
    		throw new RuntimeException("AndroidGraphics2D not instantiated!");
    	}
   		p = ((AndroidGraphics2D)g).getAndroidPaint();
   		char[] ca = {ch};
   		float[] fa = new float[1];
   		p.getTextWidths(ca, 0, 1, fa);
   		return (int)fa[0];
    }
    public int charWidth(int ind) {
        return charWidth((char)ind);
    }
    public Glyph[] getGlyphs(char uFirst, char uLast) {
        char i = uFirst;
        int len = uLast - uFirst;
        ArrayList<Glyph> lst = new ArrayList<Glyph>(len);
        if (size < 0) {
            throw new IllegalArgumentException(Messages.getString("awt.09")); 
        }
        while (i < uLast) {
            lst.add(this.getGlyph(i));
        }
        return (Glyph[]) lst.toArray();
    }
    public Glyph[] getGlyphs(char[] chars) {
        if (chars == null){
            return null;
        }
        Glyph[] result = new Glyph[chars.length];
        for (int i = 0; i < chars.length; i++) {
            result[i] = this.getGlyph(chars[i]);
        }
        return result;
    }
    public Glyph[] getGlyphs(String str) {
        char[] chars = str.toCharArray();
        return this.getGlyphs(chars);
    }
    public String getFamily() {
        return fontFamilyName;
    }
    public String getFontName() {
        if (this.fontType == FontManager.FONT_TYPE_T1){
            return this.fontFamilyName;
        }
        return faceName;
    }
    public int getLogicalHeight() {
        return logicalHeight;
    }
    public void setLogicalHeight(int newHeight) {
        logicalHeight = newHeight;
    }
    public int getSize() {
        return size;
    }
    public int getStyle() {
        return style;
    }
    public String getName() {
        return name;
    }
    public Rectangle2D getMaxCharBounds(FontRenderContext frc) {
        return maxCharBounds;
    }
    public int getNumGlyphs() {
        return  numGlyphs;
    }
    public float getItalicAngle() {
        return italicAngle;
    }
    public float getHeight(){
        return height;
    }
    public LineMetrics getLineMetrics(){
        return nlm;
    }
    public long getFontHandle(){
        return pFont;
    }
    public int getAscent(){
    	Paint p;
    	AndroidGraphics2D g = AndroidGraphics2D.getInstance();
    	if(g == null) {
    		throw new RuntimeException("AndroidGraphics2D not instantiated!");
    	}
   		p = ((AndroidGraphics2D)g).getAndroidPaint();
   		return (int)p.ascent();
    }
    public int getDescent(){
        return descent;
    }
    public int getLeading(){
        return leading;
    }
    public boolean hasUniformLineMetrics(){
        return uniformLM;
    }
    public int getFontType(){
        return fontType;
    }
    public void setFontType(int newType){
        if (newType == FontManager.FONT_TYPE_T1 || newType == FontManager.FONT_TYPE_TT){
            fontType = newType;
        }
    }
    @Override
    protected void finalize() throws Throwable {
      super.finalize();
      dispose();
    }
}
