public abstract class Font2D {
    public static final int FONT_CONFIG_RANK   = 2;
    public static final int JRE_RANK     = 2;
    public static final int TTF_RANK     = 3;
    public static final int TYPE1_RANK   = 4;
    public static final int NATIVE_RANK  = 5;
    public static final int UNKNOWN_RANK = 6;
    public static final int DEFAULT_RANK = 4;
    private static final String[] boldNames = {
        "bold", "demibold", "demi-bold", "demi bold", "negreta", "demi", };
    private static final String[] italicNames = {
        "italic", "cursiva", "oblique", "inclined", };
    private static final String[] boldItalicNames = {
          "bolditalic", "bold-italic", "bold italic",
          "boldoblique", "bold-oblique", "bold oblique",
          "demibold italic", "negreta cursiva","demi oblique", };
    private static final FontRenderContext DEFAULT_FRC =
        new FontRenderContext(null, false, false);
    public Font2DHandle handle;
    protected String familyName;           
    protected String fullName;             
    protected int style = Font.PLAIN;
    protected FontFamily family;
    protected int fontRank = DEFAULT_RANK;
    protected CharToGlyphMapper mapper;
    protected ConcurrentHashMap<FontStrikeDesc, Reference>
        strikeCache = new ConcurrentHashMap<FontStrikeDesc, Reference>();
    protected Reference lastFontStrike = new SoftReference(null);
    public int getStyle() {
        return style;
    }
    protected void setStyle() {
        String fName = fullName.toLowerCase();
        for (int i=0; i < boldItalicNames.length; i++) {
            if (fName.indexOf(boldItalicNames[i]) != -1) {
                style = Font.BOLD|Font.ITALIC;
                return;
            }
        }
        for (int i=0; i < italicNames.length; i++) {
            if (fName.indexOf(italicNames[i]) != -1) {
                style = Font.ITALIC;
                return;
            }
        }
        for (int i=0; i < boldNames.length; i++) {
            if (fName.indexOf(boldNames[i]) != -1 ) {
                style = Font.BOLD;
                return;
            }
        }
    }
    int getRank() {
        return fontRank;
    }
    void setRank(int rank) {
        fontRank = rank;
    }
    abstract CharToGlyphMapper getMapper();
    protected int getValidatedGlyphCode(int glyphCode) {
        if (glyphCode < 0 || glyphCode >= getMapper().getNumGlyphs()) {
            glyphCode = getMapper().getMissingGlyphCode();
        }
        return glyphCode;
    }
    abstract FontStrike createStrike(FontStrikeDesc desc);
    public FontStrike getStrike(Font font) {
        FontStrike strike = (FontStrike)lastFontStrike.get();
        if (strike != null) {
            return strike;
        } else {
            return getStrike(font, DEFAULT_FRC);
        }
    }
    public FontStrike getStrike(Font font, AffineTransform devTx,
                                int aa, int fm) {
        double ptSize = font.getSize2D();
        AffineTransform glyphTx = (AffineTransform)devTx.clone();
        glyphTx.scale(ptSize, ptSize);
        if (font.isTransformed()) {
            glyphTx.concatenate(font.getTransform());
        }
        if (glyphTx.getTranslateX() != 0 || glyphTx.getTranslateY() != 0) {
            glyphTx.setTransform(glyphTx.getScaleX(),
                                 glyphTx.getShearY(),
                                 glyphTx.getShearX(),
                                 glyphTx.getScaleY(),
                                 0.0, 0.0);
        }
        FontStrikeDesc desc = new FontStrikeDesc(devTx, glyphTx,
                                                 font.getStyle(), aa, fm);
        return getStrike(desc, false);
    }
    public FontStrike getStrike(Font font, AffineTransform devTx,
                                AffineTransform glyphTx,
                                int aa, int fm) {
        FontStrikeDesc desc = new FontStrikeDesc(devTx, glyphTx,
                                                 font.getStyle(), aa, fm);
        return getStrike(desc, false);
    }
    public FontStrike getStrike(Font font, FontRenderContext frc) {
        AffineTransform at = frc.getTransform();
        double ptSize = font.getSize2D();
        at.scale(ptSize, ptSize);
        if (font.isTransformed()) {
            at.concatenate(font.getTransform());
            if (at.getTranslateX() != 0 || at.getTranslateY() != 0) {
                at.setTransform(at.getScaleX(),
                                at.getShearY(),
                                at.getShearX(),
                                at.getScaleY(),
                                0.0, 0.0);
            }
        }
        int aa = FontStrikeDesc.getAAHintIntVal(this, font, frc);
        int fm = FontStrikeDesc.getFMHintIntVal(frc.getFractionalMetricsHint());
        FontStrikeDesc desc = new FontStrikeDesc(frc.getTransform(),
                                                 at, font.getStyle(),
                                                 aa, fm);
        return getStrike(desc, false);
    }
    FontStrike getStrike(FontStrikeDesc desc) {
        return getStrike(desc, true);
    }
    private FontStrike getStrike(FontStrikeDesc desc, boolean copy) {
        FontStrike strike = (FontStrike)lastFontStrike.get();
        if (strike != null && desc.equals(strike.desc)) {
            return strike;
        } else {
            Reference strikeRef = strikeCache.get(desc);
            if (strikeRef != null) {
                strike = (FontStrike)strikeRef.get();
                if (strike != null) {
                    lastFontStrike = new SoftReference(strike);
                    StrikeCache.refStrike(strike);
                    return strike;
                }
            }
            if (copy) {
                desc = new FontStrikeDesc(desc);
            }
            strike = createStrike(desc);
            int txType = desc.glyphTx.getType();
            if (txType == AffineTransform.TYPE_GENERAL_TRANSFORM ||
                (txType & AffineTransform.TYPE_GENERAL_ROTATION) != 0 &&
                strikeCache.size() > 10) {
                strikeRef = StrikeCache.getStrikeRef(strike, true);
            } else {
                strikeRef = StrikeCache.getStrikeRef(strike);
            }
            strikeCache.put(desc, strikeRef);
            lastFontStrike = new SoftReference(strike);
            StrikeCache.refStrike(strike);
            return strike;
        }
    }
    void removeFromCache(FontStrikeDesc desc) {
        Reference ref = strikeCache.get(desc);
        if (ref != null) {
            Object o = ref.get();
            if (o == null) {
                strikeCache.remove(desc);
            }
        }
    }
    public void getFontMetrics(Font font, AffineTransform at,
                               Object aaHint, Object fmHint,
                               float metrics[]) {
        int aa = FontStrikeDesc.getAAHintIntVal(aaHint, this, font.getSize());
        int fm = FontStrikeDesc.getFMHintIntVal(fmHint);
        FontStrike strike = getStrike(font, at, aa, fm);
        StrikeMetrics strikeMetrics = strike.getFontMetrics();
        metrics[0] = strikeMetrics.getAscent();
        metrics[1] = strikeMetrics.getDescent();
        metrics[2] = strikeMetrics.getLeading();
        metrics[3] = strikeMetrics.getMaxAdvance();
        getStyleMetrics(font.getSize2D(), metrics, 4);
    }
    public void getStyleMetrics(float pointSize, float[] metrics, int offset) {
        metrics[offset] = -metrics[0] / 2.5f;
        metrics[offset+1] = pointSize / 12;
        metrics[offset+2] = metrics[offset+1] / 1.5f;
        metrics[offset+3] = metrics[offset+1];
    }
    public void getFontMetrics(Font font, FontRenderContext frc,
                               float metrics[]) {
        StrikeMetrics strikeMetrics = getStrike(font, frc).getFontMetrics();
        metrics[0] = strikeMetrics.getAscent();
        metrics[1] = strikeMetrics.getDescent();
        metrics[2] = strikeMetrics.getLeading();
        metrics[3] = strikeMetrics.getMaxAdvance();
    }
    byte[] getTableBytes(int tag) {
        return null;
    }
    protected long getUnitsPerEm() {
        return 2048;
    }
    boolean supportsEncoding(String encoding) {
        return false;
    }
    public boolean canDoStyle(int style) {
        return (style == this.style);
    }
    public boolean useAAForPtSize(int ptsize) {
        return true;
    }
    public boolean hasSupplementaryChars() {
        return false;
    }
    public String getPostscriptName() {
        return fullName;
    }
    public String getFontName(Locale l) {
        return fullName;
    }
    public String getFamilyName(Locale l) {
        return familyName;
    }
    public int getNumGlyphs() {
        return getMapper().getNumGlyphs();
    }
    public int charToGlyph(int wchar) {
        return getMapper().charToGlyph(wchar);
    }
    public int getMissingGlyphCode() {
        return getMapper().getMissingGlyphCode();
    }
    public boolean canDisplay(char c) {
        return getMapper().canDisplay(c);
    }
    public boolean canDisplay(int cp) {
        return getMapper().canDisplay(cp);
    }
    public byte getBaselineFor(char c) {
        return Font.ROMAN_BASELINE;
    }
    public float getItalicAngle(Font font, AffineTransform at,
                                Object aaHint, Object fmHint) {
        int aa = FontStrikeDesc.getAAHintIntVal(aaHint, this, 12);
        int fm = FontStrikeDesc.getFMHintIntVal(fmHint);
        FontStrike strike = getStrike(font, at, aa, fm);
        StrikeMetrics metrics = strike.getFontMetrics();
        if (metrics.ascentY == 0 || metrics.ascentX == 0) {
            return 0f;
        } else {
            return metrics.ascentX/-metrics.ascentY;
        }
    }
}
