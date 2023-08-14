public abstract class Glyph{
    char glChar;
    GlyphMetrics glMetrics;
    GlyphMetrics glPointMetrics;
    int glCode;
    GlyphJustificationInfo glJustInfo;
    long pFont;
    int fontSize;
    byte[] bitmap = null;
    BufferedImage image;
    Shape glOutline = null;
    public int bmp_top = 0;
    public int bmp_left = 0;
    public int bmp_pitch;
    public int bmp_rows;
    public int bmp_width;
    public long getPFont(){
        return this.pFont;
    }
    public char getChar(){
        return glChar;
    }
    public int getWidth(){
        return Math.round((float)glMetrics.getBounds2D().getWidth());
    }
    public int getHeight(){
        return Math.round((float)glMetrics.getBounds2D().getHeight());
    }
    public int getGlyphCode(){
        return glCode;
    }
    public GlyphMetrics getGlyphMetrics(){
        return glMetrics;
    }
    public GlyphMetrics getGlyphPointMetrics(){
        return glPointMetrics;
    }
    public GlyphJustificationInfo getGlyphJustificationInfo(){
        return glJustInfo;
    }
    public void setGlyphJustificationInfo(GlyphJustificationInfo newJustInfo){
        this.glJustInfo = newJustInfo;
    }
    public int[] getABC(){
        int[] abc = new int[3];
        abc[0] = (int)glMetrics.getLSB();
        abc[1] = (int)glMetrics.getBounds2D().getWidth();
        abc[2] = (int)glMetrics.getRSB();
        return abc;
    }
    public void setImage(BufferedImage newImage){
        this.image = newImage;
    }
    @Override
    public boolean equals(Object obj){
         if (obj == this) {
            return true;
        }
        if (obj != null) {
          try {
            Glyph gl = (Glyph)obj;
            return  ((this.getChar() == gl.getChar())
              && (this.getGlyphMetrics().equals(gl.getGlyphMetrics()))
              && (this.getGlyphCode() == gl.getGlyphCode()));
          } catch (ClassCastException e) {
          }
        }
        return false;
    }
    public int getPointHeight(){
        return (int)glPointMetrics.getBounds2D().getHeight();
    }
    public int getPointWidth(){
        return (int)glPointMetrics.getBounds2D().getWidth();
    }
    public Shape getShape(){
        if (glOutline == null){
            glOutline = initOutline(this.glChar);
        }
        return glOutline;
    }
    public BufferedImage getImage(){
        return null;
    }
    public abstract byte[] getBitmap();
    public abstract Shape initOutline(char c);
}
