class FontStrikeDisposer
    implements DisposerRecord, Disposer.PollDisposable {
    Font2D font2D;
    FontStrikeDesc desc;
    long[] longGlyphImages;
    int [] intGlyphImages;
    int [][] segIntGlyphImages;
    long[][] segLongGlyphImages;
    long pScalerContext = 0L;
    boolean disposed = false;
    boolean comp = false;
    public FontStrikeDisposer(Font2D font2D, FontStrikeDesc desc,
                              long pContext, int[] images) {
        this.font2D = font2D;
        this.desc = desc;
        this.pScalerContext = pContext;
        this.intGlyphImages = images;
    }
    public FontStrikeDisposer(Font2D font2D, FontStrikeDesc desc,
                              long pContext, long[] images) {
        this.font2D = font2D;
        this.desc = desc;
        this.pScalerContext = pContext;
        this.longGlyphImages = images;
    }
    public FontStrikeDisposer(Font2D font2D, FontStrikeDesc desc,
                              long pContext) {
        this.font2D = font2D;
        this.desc = desc;
        this.pScalerContext = pContext;
    }
    public FontStrikeDisposer(Font2D font2D, FontStrikeDesc desc) {
        this.font2D = font2D;
        this.desc = desc;
        this.comp = true;
    }
    public synchronized void dispose() {
        if (!disposed) {
            font2D.removeFromCache(desc);
            StrikeCache.disposeStrike(this);
            disposed = true;
        }
    }
}
