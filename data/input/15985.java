public final class FontDesignMetrics extends FontMetrics {
    static final long serialVersionUID = 4480069578560887773L;
    private static final float UNKNOWN_WIDTH = -1;
    private static final int CURRENT_VERSION = 1;
    private static float roundingUpValue = 0.95f;
    private Font  font;
    private float ascent;
    private float descent;
    private float leading;
    private float maxAdvance;
    private double[] matrix;
    private int[] cache; 
    private int serVersion = 0;  
    private boolean isAntiAliased;
    private boolean usesFractionalMetrics;
    private AffineTransform frcTx;
    private transient float[] advCache; 
    private transient int height = -1;
    private transient FontRenderContext frc;
    private transient double[] devmatrix = null;
    private transient FontStrike fontStrike;
    private static FontRenderContext DEFAULT_FRC = null;
    private static FontRenderContext getDefaultFrc() {
        if (DEFAULT_FRC == null) {
            AffineTransform tx;
            if (GraphicsEnvironment.isHeadless()) {
                tx = new AffineTransform();
            } else {
                tx =  GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration()
                    .getDefaultTransform();
            }
            DEFAULT_FRC = new FontRenderContext(tx, false, false);
        }
        return DEFAULT_FRC;
    }
    private static class KeyReference extends SoftReference
        implements DisposerRecord, Disposer.PollDisposable {
        static ReferenceQueue queue = Disposer.getQueue();
        Object key;
        KeyReference(Object key, Object value) {
            super(value, queue);
            this.key = key;
            Disposer.addReference(this, this);
        }
        public void dispose() {
            if (metricsCache.get(key) == this) {
                metricsCache.remove(key);
            }
        }
    }
    private static class MetricsKey {
        Font font;
        FontRenderContext frc;
        int hash;
        MetricsKey() {
        }
        MetricsKey(Font font, FontRenderContext frc) {
            init(font, frc);
        }
        void init(Font font, FontRenderContext frc) {
            this.font = font;
            this.frc = frc;
            this.hash = font.hashCode() + frc.hashCode();
        }
        public boolean equals(Object key) {
            if (!(key instanceof MetricsKey)) {
                return false;
            }
            return
                font.equals(((MetricsKey)key).font) &&
                frc.equals(((MetricsKey)key).frc);
        }
        public int hashCode() {
            return hash;
        }
        static final MetricsKey key = new MetricsKey();
    }
    private static final ConcurrentHashMap<Object, KeyReference>
        metricsCache = new ConcurrentHashMap<Object, KeyReference>();
    private static final int MAXRECENT = 5;
    private static final FontDesignMetrics[]
        recentMetrics = new FontDesignMetrics[MAXRECENT];
    private static int recentIndex = 0;
    public static FontDesignMetrics getMetrics(Font font) {
        return getMetrics(font, getDefaultFrc());
     }
    public static FontDesignMetrics getMetrics(Font font,
                                               FontRenderContext frc) {
        SunFontManager fm = SunFontManager.getInstance();
        if (fm.maybeUsingAlternateCompositeFonts() &&
            FontUtilities.getFont2D(font) instanceof CompositeFont) {
            return new FontDesignMetrics(font, frc);
        }
        FontDesignMetrics m = null;
        KeyReference r;
        boolean usefontkey = frc.equals(getDefaultFrc());
        if (usefontkey) {
            r = metricsCache.get(font);
        } else  {
            synchronized (MetricsKey.class) {
                MetricsKey.key.init(font, frc);
                r = metricsCache.get(MetricsKey.key);
            }
        }
        if (r != null) {
            m = (FontDesignMetrics)r.get();
        }
        if (m == null) {
            m = new FontDesignMetrics(font, frc);
            if (usefontkey) {
                metricsCache.put(font, new KeyReference(font, m));
            } else  {
                MetricsKey newKey = new MetricsKey(font, frc);
                metricsCache.put(newKey, new KeyReference(newKey, m));
            }
        }
        for (int i=0; i<recentMetrics.length; i++) {
            if (recentMetrics[i]==m) {
                return m;
            }
        }
        synchronized (recentMetrics) {
            recentMetrics[recentIndex++] = m;
            if (recentIndex == MAXRECENT) {
                recentIndex = 0;
            }
        }
        return m;
    }
    private FontDesignMetrics(Font font) {
        this(font, getDefaultFrc());
    }
    private FontDesignMetrics(Font font, FontRenderContext frc) {
      super(font);
      this.font = font;
      this.frc = frc;
      this.isAntiAliased = frc.isAntiAliased();
      this.usesFractionalMetrics = frc.usesFractionalMetrics();
      frcTx = frc.getTransform();
      matrix = new double[4];
      initMatrixAndMetrics();
      initAdvCache();
    }
    private void initMatrixAndMetrics() {
        Font2D font2D = FontUtilities.getFont2D(font);
        fontStrike = font2D.getStrike(font, frc);
        StrikeMetrics metrics = fontStrike.getFontMetrics();
        this.ascent = metrics.getAscent();
        this.descent = metrics.getDescent();
        this.leading = metrics.getLeading();
        this.maxAdvance = metrics.getMaxAdvance();
        devmatrix = new double[4];
        frcTx.getMatrix(devmatrix);
    }
    private void initAdvCache() {
        advCache = new float[256];
        for (int i = 0; i < 256; i++) {
            advCache[i] = UNKNOWN_WIDTH;
        }
    }
    private void readObject(ObjectInputStream in) throws IOException,
                                                  ClassNotFoundException {
        in.defaultReadObject();
        if (serVersion != CURRENT_VERSION) {
            frc = getDefaultFrc();
            isAntiAliased = frc.isAntiAliased();
            usesFractionalMetrics = frc.usesFractionalMetrics();
            frcTx = frc.getTransform();
        }
        else {
            frc = new FontRenderContext(frcTx, isAntiAliased, usesFractionalMetrics);
        }
        height = -1;
        cache = null;
        initMatrixAndMetrics();
        initAdvCache();
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        cache = new int[256];
        for (int i=0; i < 256; i++) {
            cache[i] = -1;
        }
        serVersion = CURRENT_VERSION;
        out.defaultWriteObject();
        cache = null;
    }
    private float handleCharWidth(int ch) {
        return fontStrike.getCodePointAdvance(ch); 
    }
    private float getLatinCharWidth(char ch) {
        float w = advCache[ch];
        if (w == UNKNOWN_WIDTH) {
            w = handleCharWidth(ch);
            advCache[ch] = w;
        }
        return w;
    }
    public FontRenderContext getFontRenderContext() {
        return frc;
    }
    public int charWidth(char ch) {
        float w;
        if (ch < 0x100) {
            w = getLatinCharWidth(ch);
        }
        else {
            w = handleCharWidth(ch);
        }
        return (int)(0.5 + w);
    }
    public int charWidth(int ch) {
        if (!Character.isValidCodePoint(ch)) {
            ch = 0xffff;
        }
        float w = handleCharWidth(ch);
        return (int)(0.5 + w);
    }
    public int stringWidth(String str) {
        float width = 0;
        if (font.hasLayoutAttributes()) {
            if (str == null) {
                throw new NullPointerException("str is null");
            }
            if (str.length() == 0) {
                return 0;
            }
            width = new TextLayout(str, font, frc).getAdvance();
        } else {
            int length = str.length();
            for (int i=0; i < length; i++) {
                char ch = str.charAt(i);
                if (ch < 0x100) {
                    width += getLatinCharWidth(ch);
                } else if (FontUtilities.isNonSimpleChar(ch)) {
                    width = new TextLayout(str, font, frc).getAdvance();
                    break;
                } else {
                    width += handleCharWidth(ch);
                }
            }
        }
        return (int) (0.5 + width);
    }
    public int charsWidth(char data[], int off, int len) {
        float width = 0;
        if (font.hasLayoutAttributes()) {
            if (len == 0) {
                return 0;
            }
            String str = new String(data, off, len);
            width = new TextLayout(str, font, frc).getAdvance();
        } else {
            if (len < 0) {
                throw new IndexOutOfBoundsException("len="+len);
            }
            int limit = off + len;
            for (int i=off; i < limit; i++) {
                char ch = data[i];
                if (ch < 0x100) {
                    width += getLatinCharWidth(ch);
                } else if (FontUtilities.isNonSimpleChar(ch)) {
                    String str = new String(data, off, len);
                    width = new TextLayout(str, font, frc).getAdvance();
                    break;
                } else {
                    width += handleCharWidth(ch);
                }
            }
        }
        return (int) (0.5 + width);
    }
    public int[] getWidths() {
        int[] widths = new int[256];
        for (char ch = 0 ; ch < 256 ; ch++) {
            float w = advCache[ch];
            if (w == UNKNOWN_WIDTH) {
                w = advCache[ch] = handleCharWidth(ch);
            }
            widths[ch] = (int) (0.5 + w);
        }
        return widths;
    }
    public int getMaxAdvance() {
        return (int)(0.99f + this.maxAdvance);
    }
    public int getAscent() {
        return (int)(roundingUpValue + this.ascent);
    }
    public int getDescent() {
        return (int)(roundingUpValue + this.descent);
    }
    public int getLeading() {
        return
            (int)(roundingUpValue + descent + leading) -
            (int)(roundingUpValue + descent);
    }
    public int getHeight() {
        if (height < 0) {
            height = getAscent() + (int)(roundingUpValue + descent + leading);
        }
        return height;
    }
}
