public abstract class FileFont extends PhysicalFont {
    protected boolean useJavaRasterizer = true;
    protected int fileSize;
    protected FontScaler scaler;
    protected boolean checkedNatives;
    protected boolean useNatives;
    protected NativeFont[] nativeFonts;
    protected char[] glyphToCharMap;
    FileFont(String platname, Object nativeNames)
        throws FontFormatException {
        super(platname, nativeNames);
    }
    FontStrike createStrike(FontStrikeDesc desc) {
        if (!checkedNatives) {
           checkUseNatives();
        }
        return new FileFontStrike(this, desc);
    }
    protected boolean checkUseNatives() {
        checkedNatives = true;
        return useNatives;
    }
    protected abstract void close();
    abstract ByteBuffer readBlock(int offset, int length);
    public boolean canDoStyle(int style) {
        return true;
    }
    void setFileToRemove(File file, CreatedFontTracker tracker) {
        Disposer.addObjectRecord(this,
                         new CreatedFontFileDisposerRecord(file, tracker));
    }
    synchronized void deregisterFontAndClearStrikeCache() {
        SunFontManager fm = SunFontManager.getInstance();
        fm.deRegisterBadFont(this);
        for (Reference strikeRef : strikeCache.values()) {
            if (strikeRef != null) {
                FileFontStrike strike = (FileFontStrike)strikeRef.get();
                if (strike != null && strike.pScalerContext != 0L) {
                    scaler.invalidateScalerContext(strike.pScalerContext);
                }
            }
        }
        scaler.dispose();
        scaler = FontScaler.getNullScaler();
    }
    StrikeMetrics getFontMetrics(long pScalerContext) {
        try {
            return getScaler().getFontMetrics(pScalerContext);
        } catch (FontScalerException fe) {
            scaler = FontScaler.getNullScaler();
            return getFontMetrics(pScalerContext);
        }
    }
    float getGlyphAdvance(long pScalerContext, int glyphCode) {
        try {
            return getScaler().getGlyphAdvance(pScalerContext, glyphCode);
        } catch (FontScalerException fe) {
            scaler = FontScaler.getNullScaler();
            return getGlyphAdvance(pScalerContext, glyphCode);
        }
    }
    void getGlyphMetrics(long pScalerContext, int glyphCode, Point2D.Float metrics) {
        try {
            getScaler().getGlyphMetrics(pScalerContext, glyphCode, metrics);
        } catch (FontScalerException fe) {
            scaler = FontScaler.getNullScaler();
            getGlyphMetrics(pScalerContext, glyphCode, metrics);
        }
    }
    long getGlyphImage(long pScalerContext, int glyphCode) {
        try {
            return getScaler().getGlyphImage(pScalerContext, glyphCode);
        } catch (FontScalerException fe) {
            scaler = FontScaler.getNullScaler();
            return getGlyphImage(pScalerContext, glyphCode);
        }
    }
    Rectangle2D.Float getGlyphOutlineBounds(long pScalerContext, int glyphCode) {
        try {
            return getScaler().getGlyphOutlineBounds(pScalerContext, glyphCode);
        } catch (FontScalerException fe) {
            scaler = FontScaler.getNullScaler();
            return getGlyphOutlineBounds(pScalerContext, glyphCode);
        }
    }
    GeneralPath getGlyphOutline(long pScalerContext, int glyphCode, float x, float y) {
        try {
            return getScaler().getGlyphOutline(pScalerContext, glyphCode, x, y);
        } catch (FontScalerException fe) {
            scaler = FontScaler.getNullScaler();
            return getGlyphOutline(pScalerContext, glyphCode, x, y);
        }
    }
    GeneralPath getGlyphVectorOutline(long pScalerContext, int[] glyphs, int numGlyphs, float x, float y) {
        try {
            return getScaler().getGlyphVectorOutline(pScalerContext, glyphs, numGlyphs, x, y);
        } catch (FontScalerException fe) {
            scaler = FontScaler.getNullScaler();
            return getGlyphVectorOutline(pScalerContext, glyphs, numGlyphs, x, y);
        }
    }
    protected abstract FontScaler getScaler();
    protected long getUnitsPerEm() {
        return getScaler().getUnitsPerEm();
    }
    private static class CreatedFontFileDisposerRecord
        implements DisposerRecord {
        File fontFile = null;
        CreatedFontTracker tracker;
        private CreatedFontFileDisposerRecord(File file,
                                              CreatedFontTracker tracker) {
            fontFile = file;
            this.tracker = tracker;
        }
        public void dispose() {
            java.security.AccessController.doPrivileged(
                 new java.security.PrivilegedAction() {
                      public Object run() {
                          if (fontFile != null) {
                              try {
                                  if (tracker != null) {
                                      tracker.subBytes((int)fontFile.length());
                                  }
                                  fontFile.delete();
                                  SunFontManager.getInstance().tmpFontFiles.remove(fontFile);
                              } catch (Exception e) {
                              }
                          }
                          return null;
                      }
            });
        }
    }
    protected String getPublicFileName() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return platName;
        }
        boolean canReadProperty = true;
        try {
            sm.checkPropertyAccess("java.io.tmpdir");
        } catch (SecurityException e) {
            canReadProperty = false;
        }
        if (canReadProperty) {
            return platName;
        }
        final File f = new File(platName);
        Boolean isTmpFile = Boolean.FALSE;
        try {
            isTmpFile = AccessController.doPrivileged(
                new PrivilegedExceptionAction<Boolean>() {
                    public Boolean run() {
                        File tmp = new File(System.getProperty("java.io.tmpdir"));
                        try {
                            String tpath = tmp.getCanonicalPath();
                            String fpath = f.getCanonicalPath();
                            return (fpath == null) || fpath.startsWith(tpath);
                        } catch (IOException e) {
                            return Boolean.TRUE;
                        }
                    }
                }
            );
        } catch (PrivilegedActionException e) {
            isTmpFile = Boolean.TRUE;
        }
        return  isTmpFile ? "temp file" : platName;
    }
}
