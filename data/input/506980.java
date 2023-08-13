public class TableMaskFilter extends MaskFilter {
    public TableMaskFilter(byte[] table) {
        if (table.length < 256) {
            throw new RuntimeException("table.length must be >= 256");
        }
        native_instance = nativeNewTable(table);
    }
    private TableMaskFilter(int ni) {
        native_instance = ni;
    }
    public static TableMaskFilter CreateClipTable(int min, int max) {
        return new TableMaskFilter(nativeNewClip(min, max));
    }
    public static TableMaskFilter CreateGammaTable(float gamma) {
        return new TableMaskFilter(nativeNewGamma(gamma));
    }
    private static native int nativeNewTable(byte[] table);
    private static native int nativeNewClip(int min, int max);
    private static native int nativeNewGamma(float gamma);
}
