public class FontInfo implements Cloneable {
    public Font font;
    public Font2D font2D;
    public FontStrike fontStrike;
    public double[] devTx;
    public double[] glyphTx;
    public int pixelHeight;
    public float originX;
    public float originY;
    public int aaHint;
    public boolean lcdRGBOrder;
    public boolean lcdSubPixPos;
    public String mtx(double[] matrix) {
        return ("["+
                matrix[0]+", "+
                matrix[1]+", "+
                matrix[2]+", "+
                matrix[3]+
                "]");
    }
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
    public String toString() {
        return ("FontInfo["+
                "font="+font+", "+
                "devTx="+mtx(devTx)+", "+
                "glyphTx="+mtx(glyphTx)+", "+
                "pixelHeight="+pixelHeight+", "+
                "origin=("+originX+","+originY+"), "+
                "aaHint="+aaHint+", "+
                "lcdRGBOrder="+(lcdRGBOrder ? "RGB" : "BGR")+
                "lcdSubPixPos="+lcdSubPixPos+
                "]");
    }
}
