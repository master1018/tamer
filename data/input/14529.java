public class ImageWriteParam extends IIOParam {
    public static final int MODE_DISABLED = 0;
    public static final int MODE_DEFAULT = 1;
    public static final int MODE_EXPLICIT = 2;
    public static final int MODE_COPY_FROM_METADATA = 3;
    private static final int MAX_MODE = MODE_COPY_FROM_METADATA;
    protected boolean canWriteTiles = false;
    protected int tilingMode = MODE_COPY_FROM_METADATA;
    protected Dimension[] preferredTileSizes = null;
    protected boolean tilingSet = false;
    protected int tileWidth = 0;
    protected int tileHeight = 0;
    protected boolean canOffsetTiles = false;
    protected int tileGridXOffset = 0;
    protected int tileGridYOffset = 0;
    protected boolean canWriteProgressive = false;
    protected int progressiveMode = MODE_COPY_FROM_METADATA;
    protected boolean canWriteCompressed = false;
    protected int compressionMode = MODE_COPY_FROM_METADATA;
    protected String[] compressionTypes = null;
    protected String compressionType = null;
    protected float compressionQuality = 1.0F;
    protected Locale locale = null;
    protected ImageWriteParam() {}
    public ImageWriteParam(Locale locale) {
        this.locale = locale;
    }
    private static Dimension[] clonePreferredTileSizes(Dimension[] sizes) {
        if (sizes == null) {
            return null;
        }
        Dimension[] temp = new Dimension[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            temp[i] = new Dimension(sizes[i]);
        }
        return temp;
    }
    public Locale getLocale() {
        return locale;
    }
    public boolean canWriteTiles() {
        return canWriteTiles;
    }
    public boolean canOffsetTiles() {
        return canOffsetTiles;
    }
    public void setTilingMode(int mode) {
        if (canWriteTiles() == false) {
            throw new UnsupportedOperationException("Tiling not supported!");
        }
        if (mode < MODE_DISABLED || mode > MAX_MODE) {
            throw new IllegalArgumentException("Illegal value for mode!");
        }
        this.tilingMode = mode;
        if (mode == MODE_EXPLICIT) {
            unsetTiling();
        }
    }
    public int getTilingMode() {
        if (!canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported");
        }
        return tilingMode;
    }
    public Dimension[] getPreferredTileSizes() {
        if (!canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported");
        }
        return clonePreferredTileSizes(preferredTileSizes);
    }
    public void setTiling(int tileWidth,
                          int tileHeight,
                          int tileGridXOffset,
                          int tileGridYOffset) {
        if (!canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported!");
        }
        if (getTilingMode() != MODE_EXPLICIT) {
            throw new IllegalStateException("Tiling mode not MODE_EXPLICIT!");
        }
        if (tileWidth <= 0 || tileHeight <= 0) {
            throw new IllegalArgumentException
                ("tile dimensions are non-positive!");
        }
        boolean tilesOffset = (tileGridXOffset != 0) || (tileGridYOffset != 0);
        if (!canOffsetTiles() && tilesOffset) {
            throw new UnsupportedOperationException("Can't offset tiles!");
        }
        if (preferredTileSizes != null) {
            boolean ok = true;
            for (int i = 0; i < preferredTileSizes.length; i += 2) {
                Dimension min = preferredTileSizes[i];
                Dimension max = preferredTileSizes[i+1];
                if ((tileWidth < min.width) ||
                    (tileWidth > max.width) ||
                    (tileHeight < min.height) ||
                    (tileHeight > max.height)) {
                    ok = false;
                    break;
                }
            }
            if (!ok) {
                throw new IllegalArgumentException("Illegal tile size!");
            }
        }
        this.tilingSet = true;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tileGridXOffset = tileGridXOffset;
        this.tileGridYOffset = tileGridYOffset;
    }
    public void unsetTiling() {
        if (!canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported!");
        }
        if (getTilingMode() != MODE_EXPLICIT) {
            throw new IllegalStateException("Tiling mode not MODE_EXPLICIT!");
        }
        this.tilingSet = false;
        this.tileWidth = 0;
        this.tileHeight = 0;
        this.tileGridXOffset = 0;
        this.tileGridYOffset = 0;
    }
    public int getTileWidth() {
        if (!canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported!");
        }
        if (getTilingMode() != MODE_EXPLICIT) {
            throw new IllegalStateException("Tiling mode not MODE_EXPLICIT!");
        }
        if (!tilingSet) {
            throw new IllegalStateException("Tiling parameters not set!");
        }
        return tileWidth;
    }
    public int getTileHeight() {
        if (!canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported!");
        }
        if (getTilingMode() != MODE_EXPLICIT) {
            throw new IllegalStateException("Tiling mode not MODE_EXPLICIT!");
        }
        if (!tilingSet) {
            throw new IllegalStateException("Tiling parameters not set!");
        }
        return tileHeight;
    }
    public int getTileGridXOffset() {
        if (!canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported!");
        }
        if (getTilingMode() != MODE_EXPLICIT) {
            throw new IllegalStateException("Tiling mode not MODE_EXPLICIT!");
        }
        if (!tilingSet) {
            throw new IllegalStateException("Tiling parameters not set!");
        }
        return tileGridXOffset;
    }
    public int getTileGridYOffset() {
        if (!canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported!");
        }
        if (getTilingMode() != MODE_EXPLICIT) {
            throw new IllegalStateException("Tiling mode not MODE_EXPLICIT!");
        }
        if (!tilingSet) {
            throw new IllegalStateException("Tiling parameters not set!");
        }
        return tileGridYOffset;
    }
    public boolean canWriteProgressive() {
        return canWriteProgressive;
    }
    public void setProgressiveMode(int mode) {
        if (!canWriteProgressive()) {
            throw new UnsupportedOperationException(
                "Progressive output not supported");
        }
        if (mode < MODE_DISABLED || mode > MAX_MODE) {
            throw new IllegalArgumentException("Illegal value for mode!");
        }
        if (mode == MODE_EXPLICIT) {
            throw new IllegalArgumentException(
                "MODE_EXPLICIT not supported for progressive output");
        }
        this.progressiveMode = mode;
    }
    public int getProgressiveMode() {
        if (!canWriteProgressive()) {
            throw new UnsupportedOperationException
                ("Progressive output not supported");
        }
        return progressiveMode;
    }
    public boolean canWriteCompressed() {
        return canWriteCompressed;
    }
    public void setCompressionMode(int mode) {
        if (!canWriteCompressed()) {
            throw new UnsupportedOperationException(
                "Compression not supported.");
        }
        if (mode < MODE_DISABLED || mode > MAX_MODE) {
            throw new IllegalArgumentException("Illegal value for mode!");
        }
        this.compressionMode = mode;
        if (mode == MODE_EXPLICIT) {
            unsetCompression();
        }
    }
    public int getCompressionMode() {
        if (!canWriteCompressed()) {
            throw new UnsupportedOperationException(
                "Compression not supported.");
        }
        return compressionMode;
    }
    public String[] getCompressionTypes() {
        if (!canWriteCompressed()) {
            throw new UnsupportedOperationException(
                "Compression not supported");
        }
        if (compressionTypes == null) {
            return null;
        }
        return (String[])compressionTypes.clone();
    }
    public void setCompressionType(String compressionType) {
        if (!canWriteCompressed()) {
            throw new UnsupportedOperationException(
                "Compression not supported");
        }
        if (getCompressionMode() != MODE_EXPLICIT) {
            throw new IllegalStateException
                ("Compression mode not MODE_EXPLICIT!");
        }
        String[] legalTypes = getCompressionTypes();
        if (legalTypes == null) {
            throw new UnsupportedOperationException(
                "No settable compression types");
        }
        if (compressionType != null) {
            boolean found = false;
            if (legalTypes != null) {
                for (int i = 0; i < legalTypes.length; i++) {
                    if (compressionType.equals(legalTypes[i])) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Unknown compression type!");
            }
        }
        this.compressionType = compressionType;
    }
    public String getCompressionType() {
        if (!canWriteCompressed()) {
            throw new UnsupportedOperationException(
                "Compression not supported.");
        }
        if (getCompressionMode() != MODE_EXPLICIT) {
            throw new IllegalStateException
                ("Compression mode not MODE_EXPLICIT!");
        }
        return compressionType;
    }
    public void unsetCompression() {
        if (!canWriteCompressed()) {
            throw new UnsupportedOperationException(
                "Compression not supported");
        }
        if (getCompressionMode() != MODE_EXPLICIT) {
            throw new IllegalStateException
                ("Compression mode not MODE_EXPLICIT!");
        }
        this.compressionType = null;
        this.compressionQuality = 1.0F;
    }
    public String getLocalizedCompressionTypeName() {
        if (!canWriteCompressed()) {
            throw new UnsupportedOperationException(
                "Compression not supported.");
        }
        if (getCompressionMode() != MODE_EXPLICIT) {
            throw new IllegalStateException
                ("Compression mode not MODE_EXPLICIT!");
        }
        if (getCompressionType() == null) {
            throw new IllegalStateException("No compression type set!");
        }
        return getCompressionType();
    }
    public boolean isCompressionLossless() {
        if (!canWriteCompressed()) {
            throw new UnsupportedOperationException(
                "Compression not supported");
        }
        if (getCompressionMode() != MODE_EXPLICIT) {
            throw new IllegalStateException
                ("Compression mode not MODE_EXPLICIT!");
        }
        if ((getCompressionTypes() != null) &&
            (getCompressionType() == null)) {
            throw new IllegalStateException("No compression type set!");
        }
        return true;
    }
    public void setCompressionQuality(float quality) {
        if (!canWriteCompressed()) {
            throw new UnsupportedOperationException(
                "Compression not supported");
        }
        if (getCompressionMode() != MODE_EXPLICIT) {
            throw new IllegalStateException
                ("Compression mode not MODE_EXPLICIT!");
        }
        if (getCompressionTypes() != null && getCompressionType() == null) {
            throw new IllegalStateException("No compression type set!");
        }
        if (quality < 0.0F || quality > 1.0F) {
            throw new IllegalArgumentException("Quality out-of-bounds!");
        }
        this.compressionQuality = quality;
    }
    public float getCompressionQuality() {
        if (!canWriteCompressed()) {
            throw new UnsupportedOperationException(
                "Compression not supported.");
        }
        if (getCompressionMode() != MODE_EXPLICIT) {
            throw new IllegalStateException
                ("Compression mode not MODE_EXPLICIT!");
        }
        if ((getCompressionTypes() != null) &&
            (getCompressionType() == null)) {
            throw new IllegalStateException("No compression type set!");
        }
        return compressionQuality;
    }
    public float getBitRate(float quality) {
        if (!canWriteCompressed()) {
            throw new UnsupportedOperationException(
                "Compression not supported.");
        }
        if (getCompressionMode() != MODE_EXPLICIT) {
            throw new IllegalStateException
                ("Compression mode not MODE_EXPLICIT!");
        }
        if ((getCompressionTypes() != null) &&
            (getCompressionType() == null)) {
            throw new IllegalStateException("No compression type set!");
        }
        if (quality < 0.0F || quality > 1.0F) {
            throw new IllegalArgumentException("Quality out-of-bounds!");
        }
        return -1.0F;
    }
    public String[] getCompressionQualityDescriptions() {
        if (!canWriteCompressed()) {
            throw new UnsupportedOperationException(
                "Compression not supported.");
        }
        if (getCompressionMode() != MODE_EXPLICIT) {
            throw new IllegalStateException
                ("Compression mode not MODE_EXPLICIT!");
        }
        if ((getCompressionTypes() != null) &&
            (getCompressionType() == null)) {
            throw new IllegalStateException("No compression type set!");
        }
        return null;
    }
    public float[] getCompressionQualityValues() {
        if (!canWriteCompressed()) {
            throw new UnsupportedOperationException(
                "Compression not supported.");
        }
        if (getCompressionMode() != MODE_EXPLICIT) {
            throw new IllegalStateException
                ("Compression mode not MODE_EXPLICIT!");
        }
        if ((getCompressionTypes() != null) &&
            (getCompressionType() == null)) {
            throw new IllegalStateException("No compression type set!");
        }
        return null;
    }
}
