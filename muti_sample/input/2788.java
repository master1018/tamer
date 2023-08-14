public class FontStrikeDesc {
    static final int AA_ON             = 0x0010;
    static final int AA_LCD_H          = 0x0020;
    static final int AA_LCD_V          = 0x0040;
    static final int FRAC_METRICS_ON   = 0x0100;
    static final int FRAC_METRICS_SP   = 0x0200;
    AffineTransform devTx;
    AffineTransform glyphTx; 
    int style;
    int aaHint;
    int fmHint;
    private int hashCode;
    private int valuemask;
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = glyphTx.hashCode() + devTx.hashCode() + valuemask;
        }
        return hashCode;
    }
    public boolean equals(Object obj) {
        try {
            FontStrikeDesc desc = (FontStrikeDesc)obj;
            return (desc.valuemask == this.valuemask &&
                    desc.glyphTx.equals(this.glyphTx) &&
                    desc.devTx.equals(this.devTx));
        } catch (Exception e) {
            return false;
        }
    }
    FontStrikeDesc() {
    }
    public static int getAAHintIntVal(Object aa, Font2D font2D, int ptSize) {
        if (aa == VALUE_TEXT_ANTIALIAS_OFF ||
            aa == VALUE_TEXT_ANTIALIAS_DEFAULT) {
            return INTVAL_TEXT_ANTIALIAS_OFF;
        } else if (aa == VALUE_TEXT_ANTIALIAS_ON) {
            return INTVAL_TEXT_ANTIALIAS_ON;
        } else if (aa == VALUE_TEXT_ANTIALIAS_GASP) {
            if (font2D.useAAForPtSize(ptSize)) {
                return INTVAL_TEXT_ANTIALIAS_ON;
            } else {
                return INTVAL_TEXT_ANTIALIAS_OFF;
            }
        } else if (aa == VALUE_TEXT_ANTIALIAS_LCD_HRGB ||
                   aa == VALUE_TEXT_ANTIALIAS_LCD_HBGR) {
            return INTVAL_TEXT_ANTIALIAS_LCD_HRGB;
        } else if (aa == VALUE_TEXT_ANTIALIAS_LCD_VRGB ||
                   aa == VALUE_TEXT_ANTIALIAS_LCD_VBGR) {
            return INTVAL_TEXT_ANTIALIAS_LCD_VRGB;
        } else {
            return INTVAL_TEXT_ANTIALIAS_OFF;
        }
    }
    public static int getAAHintIntVal(Font2D font2D, Font font,
                                      FontRenderContext frc) {
        Object aa = frc.getAntiAliasingHint();
        if (aa == VALUE_TEXT_ANTIALIAS_OFF ||
            aa == VALUE_TEXT_ANTIALIAS_DEFAULT) {
            return INTVAL_TEXT_ANTIALIAS_OFF;
        } else if (aa == VALUE_TEXT_ANTIALIAS_ON) {
            return INTVAL_TEXT_ANTIALIAS_ON;
        } else if (aa == VALUE_TEXT_ANTIALIAS_GASP) {
            int ptSize;
            AffineTransform tx = frc.getTransform();
            if (tx.isIdentity() && !font.isTransformed()) {
                ptSize = font.getSize();
            } else {
                float size = font.getSize2D();
                if (tx.isIdentity()) {
                    tx = font.getTransform();
                    tx.scale(size, size);
                } else {
                    tx.scale(size, size);
                    if (font.isTransformed()) {
                        tx.concatenate(font.getTransform());
                    }
                }
                double shearx = tx.getShearX();
                double scaley = tx.getScaleY();
                if (shearx != 0) {
                    scaley = Math.sqrt(shearx * shearx + scaley * scaley);
                }
                ptSize = (int)(Math.abs(scaley)+0.5);
            }
            if (font2D.useAAForPtSize(ptSize)) {
                return INTVAL_TEXT_ANTIALIAS_ON;
            } else {
                return INTVAL_TEXT_ANTIALIAS_OFF;
            }
        } else if (aa == VALUE_TEXT_ANTIALIAS_LCD_HRGB ||
                   aa == VALUE_TEXT_ANTIALIAS_LCD_HBGR) {
            return INTVAL_TEXT_ANTIALIAS_LCD_HRGB;
        } else if (aa == VALUE_TEXT_ANTIALIAS_LCD_VRGB ||
                   aa == VALUE_TEXT_ANTIALIAS_LCD_VBGR) {
            return INTVAL_TEXT_ANTIALIAS_LCD_VRGB;
        } else {
            return INTVAL_TEXT_ANTIALIAS_OFF;
        }
    }
    public static int getFMHintIntVal(Object fm) {
        if (fm == VALUE_FRACTIONALMETRICS_OFF ||
            fm == VALUE_FRACTIONALMETRICS_DEFAULT) {
            return INTVAL_FRACTIONALMETRICS_OFF;
        } else {
            return INTVAL_FRACTIONALMETRICS_ON;
        }
    }
    public FontStrikeDesc(AffineTransform devAt, AffineTransform at,
                          int fStyle, int aa, int fm) {
        devTx = devAt;
        glyphTx = at; 
        style = fStyle;
        aaHint = aa;
        fmHint = fm;
        valuemask = fStyle;
        switch (aa) {
           case INTVAL_TEXT_ANTIALIAS_OFF :
                break;
           case INTVAL_TEXT_ANTIALIAS_ON  :
                valuemask |= AA_ON;
                break;
           case INTVAL_TEXT_ANTIALIAS_LCD_HRGB :
           case INTVAL_TEXT_ANTIALIAS_LCD_HBGR :
                valuemask |= AA_LCD_H;
                break;
           case INTVAL_TEXT_ANTIALIAS_LCD_VRGB :
           case INTVAL_TEXT_ANTIALIAS_LCD_VBGR :
                valuemask |= AA_LCD_V;
                break;
           default: break;
        }
        if (fm == INTVAL_FRACTIONALMETRICS_ON) {
           valuemask |= FRAC_METRICS_ON;
        }
    }
    FontStrikeDesc(FontStrikeDesc desc) {
        devTx = desc.devTx;
        glyphTx = (AffineTransform)desc.glyphTx.clone();
        style = desc.style;
        aaHint = desc.aaHint;
        fmHint = desc.fmHint;
        hashCode = desc.hashCode;
        valuemask = desc.valuemask;
    }
    public String toString() {
        return "FontStrikeDesc: Style="+style+ " AA="+aaHint+ " FM="+fmHint+
            " devTx="+devTx+ " devTx.FontTx.ptSize="+glyphTx;
    }
}
