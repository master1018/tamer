public final class BidiWrapper {
    public static final int UBIDI_DEFAULT_LTR = 0xfe;
    public static final int UBIDI_DEFAULT_RTL = 0xff;
    public static final int UBIDI_MAX_EXPLICIT_LEVEL = 61;
    public static final int UBIDI_LEVEL_OVERRIDE = 0x80;
    public static final int UBIDI_KEEP_BASE_COMBINING = 1;
    public static final int UBIDI_DO_MIRRORING = 2;
    public static final int UBIDI_INSERT_LRM_FOR_NUMERIC = 4;
    public static final int UBIDI_REMOVE_BIDI_CONTROLS = 8;
    public static final int UBIDI_OUTPUT_REVERSE = 16;
    public static final int UBiDiDirection_UBIDI_LTR = 0;
    public static final int UBiDiDirection_UBIDI_RTL = 1;
    public static final int UBiDiDirection_UBIDI_MIXED = 2;
    public static native long ubidi_open();
    public static native void ubidi_close(long pBiDi);
    public static native void ubidi_setPara(long pBiDi, char[] text,
            int length, byte paraLevel, byte[] embeddingLevels);
    public static native long ubidi_setLine(final long pParaBiDi, int start,
            int limit);
    public static native int ubidi_getDirection(final long pBiDi);
    public static native int ubidi_getLength(final long pBiDi);
    public static native byte ubidi_getParaLevel(final long pBiDi);
    public static native byte[] ubidi_getLevels(long pBiDi);
    public static native int ubidi_countRuns(long pBiDi);
    public static native BidiRun[] ubidi_getRuns(long pBidi);
    public static native int[] ubidi_reorderVisual(byte[] levels, int length);
}
