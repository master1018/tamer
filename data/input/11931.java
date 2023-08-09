public final class SunLayoutEngine implements LayoutEngine, LayoutEngineFactory {
    private static native void initGVIDs();
    static {
        FontManagerNativeLibrary.load();
        initGVIDs();
    }
    private LayoutEngineKey key;
    private static LayoutEngineFactory instance;
    public static LayoutEngineFactory instance() {
        if (instance == null) {
            instance = new SunLayoutEngine();
        }
        return instance;
    }
    private SunLayoutEngine() {
    }
    public LayoutEngine getEngine(Font2D font, int script, int lang) {
        return getEngine(new LayoutEngineKey(font, script, lang));
    }
    public LayoutEngine getEngine(LayoutEngineKey key) {
        HashMap cache = (HashMap)cacheref.get();
        if (cache == null) {
            cache = new HashMap();
            cacheref = new SoftReference(cache);
        }
        LayoutEngine e = (LayoutEngine)cache.get(key);
        if (e == null) {
            e = new SunLayoutEngine(key.copy());
            cache.put(key, e);
        }
        return e;
    }
    private SoftReference cacheref = new SoftReference(null);
    private SunLayoutEngine(LayoutEngineKey key) {
        this.key = key;
    }
    public void layout(FontStrikeDesc desc, float[] mat, int gmask,
                       int baseIndex, TextRecord tr, int typo_flags,
                       Point2D.Float pt, GVData data) {
        Font2D font = key.font();
        FontStrike strike = font.getStrike(desc);
        long layoutTables = 0;
        if (font instanceof TrueTypeFont) {
            layoutTables = ((TrueTypeFont) font).getLayoutTableCache();
        }
        nativeLayout(font, strike, mat, gmask, baseIndex,
             tr.text, tr.start, tr.limit, tr.min, tr.max,
             key.script(), key.lang(), typo_flags, pt, data,
             font.getUnitsPerEm(), layoutTables);
    }
    private static native void
        nativeLayout(Font2D font, FontStrike strike, float[] mat, int gmask,
             int baseIndex, char[] chars, int offset, int limit,
             int min, int max, int script, int lang, int typo_flags,
             Point2D.Float pt, GVData data, long upem, long layoutTables);
}
