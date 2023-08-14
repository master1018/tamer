class NativeStrikeDisposer extends FontStrikeDisposer {
    long pNativeScalerContext;
    public NativeStrikeDisposer(Font2D font2D, FontStrikeDesc desc,
                              long pContext, int[] images) {
        super(font2D, desc, 0L, images);
        pNativeScalerContext = pContext;
    }
    public NativeStrikeDisposer(Font2D font2D, FontStrikeDesc desc,
                              long pContext, long[] images) {
        super(font2D, desc, 0L, images);
        pNativeScalerContext = pContext;
    }
    public NativeStrikeDisposer(Font2D font2D, FontStrikeDesc desc,
                              long pContext) {
        super(font2D, desc, 0L);
        pNativeScalerContext = pContext;
    }
    public NativeStrikeDisposer(Font2D font2D, FontStrikeDesc desc) {
        super(font2D, desc);
    }
    public synchronized void dispose() {
        if (!disposed) {
            if (pNativeScalerContext != 0L) {
                freeNativeScalerContext(pNativeScalerContext);
            }
            super.dispose();
        }
    }
    private native void freeNativeScalerContext(long pContext);
}
