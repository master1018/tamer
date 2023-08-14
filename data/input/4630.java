public class NativeFont extends PhysicalFont {
    String encoding;
    private int numGlyphs = -1;
    boolean isBitmapDelegate;
    PhysicalFont delegateFont;
    public NativeFont(String platName, boolean bitmapDelegate)
        throws FontFormatException {
        super(platName, null);
        isBitmapDelegate = bitmapDelegate;
        if (GraphicsEnvironment.isHeadless()) {
            throw new FontFormatException("Native font in headless toolkit");
        }
        fontRank = Font2D.NATIVE_RANK;
        initNames();
        if (getNumGlyphs() == 0) {
          throw new FontFormatException("Couldn't locate font" + platName);
        }
    }
    private void initNames() throws FontFormatException {
        int[] hPos = new int[14];
        int hyphenCnt = 1;
        int pos = 1;
        String xlfd = platName.toLowerCase(Locale.ENGLISH);
        if (xlfd.startsWith("-")) {
            while (pos != -1 && hyphenCnt < 14) {
                pos = xlfd.indexOf('-', pos);
                if (pos != -1) {
                    hPos[hyphenCnt++] = pos;
                    pos++;
                }
            }
        }
        if (hyphenCnt == 14 && pos != -1) {
            String tmpFamily = xlfd.substring(hPos[1]+1, hPos[2]);
            StringBuilder sBuffer = new StringBuilder(tmpFamily);
            char ch = Character.toUpperCase(sBuffer.charAt(0));
            sBuffer.replace(0, 1, String.valueOf(ch));
            for (int i=1;i<sBuffer.length()-1; i++) {
                if (sBuffer.charAt(i) == ' ') {
                    ch = Character.toUpperCase(sBuffer.charAt(i+1));
                    sBuffer.replace(i+1, i+2, String.valueOf(ch));
                }
            }
            familyName = sBuffer.toString();
            String tmpWeight = xlfd.substring(hPos[2]+1, hPos[3]);
            String tmpSlant = xlfd.substring(hPos[3]+1, hPos[4]);
            String styleStr = null;
            if (tmpWeight.indexOf("bold") >= 0 ||
                tmpWeight.indexOf("demi") >= 0) {
                style |= Font.BOLD;
                styleStr = "Bold";
            }
            if (tmpSlant.equals("i") ||
                tmpSlant.indexOf("italic") >= 0) {
                style |= Font.ITALIC;
                if (styleStr == null) {
                    styleStr = "Italic";
                } else {
                    styleStr = styleStr + " Italic";
                }
            }
            else if (tmpSlant.equals("o") ||
                tmpSlant.indexOf("oblique") >= 0) {
                style |= Font.ITALIC;
                if (styleStr == null) {
                    styleStr = "Oblique";
                } else {
                    styleStr = styleStr + " Oblique";
                }
            }
            if (styleStr == null) {
                fullName = familyName;
            } else {
                fullName = familyName + " " + styleStr;
            }
            encoding = xlfd.substring(hPos[12]+1);
            if (encoding.startsWith("-")) {
                encoding = xlfd.substring(hPos[13]+1);
            }
            if (encoding.indexOf("fontspecific") >= 0) {
                if (tmpFamily.indexOf("dingbats") >= 0) {
                    encoding = "dingbats";
                } else if (tmpFamily.indexOf("symbol") >= 0) {
                    encoding = "symbol";
                } else {
                    encoding = "iso8859-1";
                }
            }
        } else {
            throw new FontFormatException("Bad native name " + platName);
        }
    }
    static boolean hasExternalBitmaps(String platName) {
        StringBuilder sb = new StringBuilder(platName);
        int pos = sb.indexOf("-0-");
        while (pos >=0) {
            sb.replace(pos+1, pos+2, "*");
            pos = sb.indexOf("-0-", pos);
        };
        String xlfd = sb.toString();
        byte[] bytes = null;
        try {
            bytes = xlfd.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            bytes = xlfd.getBytes();
        }
        return haveBitmapFonts(bytes);
    }
    public static boolean fontExists(String xlfd) {
        byte[] bytes = null;
        try {
            bytes = xlfd.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            bytes = xlfd.getBytes();
        }
        return fontExists(bytes);
    }
    private static native boolean haveBitmapFonts(byte[] xlfd);
    private static native boolean fontExists(byte[] xlfd);
    public CharToGlyphMapper getMapper() {
        if (mapper == null) {
            if (isBitmapDelegate) {
                mapper = new NativeGlyphMapper(this);
            } else {
                SunFontManager fm = SunFontManager.getInstance();
                delegateFont = fm.getDefaultPhysicalFont();
                mapper = delegateFont.getMapper();
            }
        }
        return mapper;
    }
    FontStrike createStrike(FontStrikeDesc desc) {
        if (isBitmapDelegate) {
            return new NativeStrike(this, desc);
        } else {
            if (delegateFont == null) {
                SunFontManager fm = SunFontManager.getInstance();
                delegateFont = fm.getDefaultPhysicalFont();
            }
            if (delegateFont instanceof NativeFont) {
                return new NativeStrike((NativeFont)delegateFont, desc);
            }
            FontStrike delegate = delegateFont.createStrike(desc);
            return new DelegateStrike(this, desc, delegate);
        }
    }
    public Rectangle2D getMaxCharBounds(FontRenderContext frc) {
            return null;
    }
    native StrikeMetrics getFontMetrics(long pScalerContext);
    native float getGlyphAdvance(long pContext, int glyphCode);
    Rectangle2D.Float getGlyphOutlineBounds(long pScalerContext,
                                            int glyphCode) {
        return new Rectangle2D.Float(0f, 0f, 0f, 0f);
    }
    public GeneralPath getGlyphOutline(long pScalerContext,
                                       int glyphCode,
                                       float x,
                                       float y) {
        return null;
    }
    native long getGlyphImage(long pScalerContext, int glyphCode);
    native long getGlyphImageNoDefault(long pScalerContext, int glyphCode);
    void getGlyphMetrics(long pScalerContext, int glyphCode,
                        Point2D.Float metrics) {
        throw new RuntimeException("this should be called on the strike");
    }
    public  GeneralPath getGlyphVectorOutline(long pScalerContext,
                                              int[] glyphs, int numGlyphs,
                                              float x,  float y) {
        return null;
    }
    private native int countGlyphs(byte[] platformNameBytes, int ptSize);
    public int getNumGlyphs() {
        if (numGlyphs == -1) {
            byte[] bytes = getPlatformNameBytes(8);
            numGlyphs = countGlyphs(bytes, 8);
        }
        return numGlyphs;
    }
    PhysicalFont getDelegateFont() {
        if (delegateFont == null) {
            SunFontManager fm = SunFontManager.getInstance();
            delegateFont = fm.getDefaultPhysicalFont();
        }
        return delegateFont;
    }
    byte[] getPlatformNameBytes(int ptSize) {
        int[] hPos = new int[14];
        int hyphenCnt = 1;
        int pos = 1;
        while (pos != -1 && hyphenCnt < 14) {
            pos = platName.indexOf('-', pos);
            if (pos != -1) {
                hPos[hyphenCnt++] = pos;
                    pos++;
            }
        }
        String sizeStr = Integer.toString((int)Math.abs(ptSize)*10);
        StringBuilder sb = new StringBuilder(platName);
        sb.replace(hPos[11]+1, hPos[12], "*");
        sb.replace(hPos[9]+1, hPos[10], "72");
        sb.replace(hPos[8]+1, hPos[9], "72");
        sb.replace(hPos[7]+1, hPos[8], sizeStr);
        sb.replace(hPos[6]+1, hPos[7], "*");
        if (hPos[0] == 0 && hPos[1] == 1) {
           sb.replace(hPos[0]+1, hPos[1], "*");
        }
        String xlfd = sb.toString();
        byte[] bytes = null;
        try {
            bytes = xlfd.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            bytes = xlfd.getBytes();
        }
        return bytes;
    }
    public String toString() {
        return " ** Native Font: Family="+familyName+ " Name="+fullName+
            " style="+style+" nativeName="+platName;
    }
}
