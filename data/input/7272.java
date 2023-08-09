public class Font implements java.io.Serializable
{
    private static class FontAccessImpl extends FontAccess {
        public Font2D getFont2D(Font font) {
            return font.getFont2D();
        }
        public void setFont2D(Font font, Font2DHandle handle) {
            font.font2DHandle = handle;
        }
        public void setCreatedFont(Font font) {
            font.createdFont = true;
        }
        public boolean isCreatedFont(Font font) {
            return font.createdFont;
        }
    }
    static {
        Toolkit.loadLibraries();
        initIDs();
        FontAccess.setFontAccess(new FontAccessImpl());
    }
    private Hashtable fRequestedAttributes;
    public static final String DIALOG = "Dialog";
    public static final String DIALOG_INPUT = "DialogInput";
    public static final String SANS_SERIF = "SansSerif";
    public static final String SERIF = "Serif";
    public static final String MONOSPACED = "Monospaced";
    public static final int PLAIN       = 0;
    public static final int BOLD        = 1;
    public static final int ITALIC      = 2;
    public static final int ROMAN_BASELINE = 0;
    public static final int CENTER_BASELINE = 1;
    public static final int HANGING_BASELINE = 2;
    public static final int TRUETYPE_FONT = 0;
    public static final int TYPE1_FONT = 1;
    protected String name;
    protected int style;
    protected int size;
    protected float pointSize;
    private transient FontPeer peer;
    private transient long pData;       
    private transient Font2DHandle font2DHandle;
    private transient AttributeValues values;
    private transient boolean hasLayoutAttributes;
    private transient boolean createdFont = false;
    private transient boolean nonIdentityTx;
    private static final AffineTransform identityTx = new AffineTransform();
    private static final long serialVersionUID = -4206021311591459213L;
    @Deprecated
    public FontPeer getPeer(){
        return getPeer_NoClientCode();
    }
    final FontPeer getPeer_NoClientCode() {
        if(peer == null) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            this.peer = tk.getFontPeer(name, style);
        }
        return peer;
    }
    private AttributeValues getAttributeValues() {
        if (values == null) {
            AttributeValues valuesTmp = new AttributeValues();
            valuesTmp.setFamily(name);
            valuesTmp.setSize(pointSize); 
            if ((style & BOLD) != 0) {
                valuesTmp.setWeight(2); 
            }
            if ((style & ITALIC) != 0) {
                valuesTmp.setPosture(.2f); 
            }
            valuesTmp.defineAll(PRIMARY_MASK); 
            values = valuesTmp;
        }
        return values;
    }
    private Font2D getFont2D() {
        FontManager fm = FontManagerFactory.getInstance();
        if (fm.usingPerAppContextComposites() &&
            font2DHandle != null &&
            font2DHandle.font2D instanceof CompositeFont &&
            ((CompositeFont)(font2DHandle.font2D)).isStdComposite()) {
            return fm.findFont2D(name, style,
                                          FontManager.LOGICAL_FALLBACK);
        } else if (font2DHandle == null) {
            font2DHandle =
                fm.findFont2D(name, style,
                              FontManager.LOGICAL_FALLBACK).handle;
        }
        return font2DHandle.font2D;
    }
    public Font(String name, int style, int size) {
        this.name = (name != null) ? name : "Default";
        this.style = (style & ~0x03) == 0 ? style : 0;
        this.size = size;
        this.pointSize = size;
    }
    private Font(String name, int style, float sizePts) {
        this.name = (name != null) ? name : "Default";
        this.style = (style & ~0x03) == 0 ? style : 0;
        this.size = (int)(sizePts + 0.5);
        this.pointSize = sizePts;
    }
    private Font(String name, int style, float sizePts,
                 boolean created, Font2DHandle handle) {
        this(name, style, sizePts);
        this.createdFont = created;
        if (created) {
            if (handle.font2D instanceof CompositeFont &&
                handle.font2D.getStyle() != style) {
                FontManager fm = FontManagerFactory.getInstance();
                this.font2DHandle = fm.getNewComposite(null, style, handle);
            } else {
                this.font2DHandle = handle;
            }
        }
    }
    private Font(File fontFile, int fontFormat,
                 boolean isCopy, CreatedFontTracker tracker)
        throws FontFormatException {
        this.createdFont = true;
        FontManager fm = FontManagerFactory.getInstance();
        this.font2DHandle = fm.createFont2D(fontFile, fontFormat, isCopy,
                                            tracker).handle;
        this.name = this.font2DHandle.font2D.getFontName(Locale.getDefault());
        this.style = Font.PLAIN;
        this.size = 1;
        this.pointSize = 1f;
    }
    private Font(AttributeValues values, String oldName, int oldStyle,
                 boolean created, Font2DHandle handle) {
        this.createdFont = created;
        if (created) {
            this.font2DHandle = handle;
            String newName = null;
            if (oldName != null) {
                newName = values.getFamily();
                if (oldName.equals(newName)) newName = null;
            }
            int newStyle = 0;
            if (oldStyle == -1) {
                newStyle = -1;
            } else {
                if (values.getWeight() >= 2f)   newStyle  = BOLD;
                if (values.getPosture() >= .2f) newStyle |= ITALIC;
                if (oldStyle == newStyle)       newStyle  = -1;
            }
            if (handle.font2D instanceof CompositeFont) {
                if (newStyle != -1 || newName != null) {
                    FontManager fm = FontManagerFactory.getInstance();
                    this.font2DHandle =
                        fm.getNewComposite(newName, newStyle, handle);
                }
            } else if (newName != null) {
                this.createdFont = false;
                this.font2DHandle = null;
            }
        }
        initFromValues(values);
    }
    public Font(Map<? extends Attribute, ?> attributes) {
        initFromValues(AttributeValues.fromMap(attributes, RECOGNIZED_MASK));
    }
    protected Font(Font font) {
        if (font.values != null) {
            initFromValues(font.getAttributeValues().clone());
        } else {
            this.name = font.name;
            this.style = font.style;
            this.size = font.size;
            this.pointSize = font.pointSize;
        }
        this.font2DHandle = font.font2DHandle;
        this.createdFont = font.createdFont;
    }
    private static final int RECOGNIZED_MASK = AttributeValues.MASK_ALL
        & ~AttributeValues.getMask(EFONT);
    private static final int PRIMARY_MASK =
        AttributeValues.getMask(EFAMILY, EWEIGHT, EWIDTH, EPOSTURE, ESIZE,
                                ETRANSFORM, ESUPERSCRIPT, ETRACKING);
    private static final int SECONDARY_MASK =
        RECOGNIZED_MASK & ~PRIMARY_MASK;
    private static final int LAYOUT_MASK =
        AttributeValues.getMask(ECHAR_REPLACEMENT, EFOREGROUND, EBACKGROUND,
                                EUNDERLINE, ESTRIKETHROUGH, ERUN_DIRECTION,
                                EBIDI_EMBEDDING, EJUSTIFICATION,
                                EINPUT_METHOD_HIGHLIGHT, EINPUT_METHOD_UNDERLINE,
                                ESWAP_COLORS, ENUMERIC_SHAPING, EKERNING,
                                ELIGATURES, ETRACKING, ESUPERSCRIPT);
    private static final int EXTRA_MASK =
            AttributeValues.getMask(ETRANSFORM, ESUPERSCRIPT, EWIDTH);
    private void initFromValues(AttributeValues values) {
        this.values = values;
        values.defineAll(PRIMARY_MASK); 
        this.name = values.getFamily();
        this.pointSize = values.getSize();
        this.size = (int)(values.getSize() + 0.5);
        if (values.getWeight() >= 2f) this.style |= BOLD; 
        if (values.getPosture() >= .2f) this.style |= ITALIC; 
        this.nonIdentityTx = values.anyNonDefault(EXTRA_MASK);
        this.hasLayoutAttributes =  values.anyNonDefault(LAYOUT_MASK);
    }
    public static Font getFont(Map<? extends Attribute, ?> attributes) {
        if (attributes instanceof AttributeMap &&
            ((AttributeMap)attributes).getValues() != null) {
            AttributeValues values = ((AttributeMap)attributes).getValues();
            if (values.isNonDefault(EFONT)) {
                Font font = values.getFont();
                if (!values.anyDefined(SECONDARY_MASK)) {
                    return font;
                }
                values = font.getAttributeValues().clone();
                values.merge(attributes, SECONDARY_MASK);
                return new Font(values, font.name, font.style,
                                font.createdFont, font.font2DHandle);
            }
            return new Font(attributes);
        }
        Font font = (Font)attributes.get(TextAttribute.FONT);
        if (font != null) {
            if (attributes.size() > 1) { 
                AttributeValues values = font.getAttributeValues().clone();
                values.merge(attributes, SECONDARY_MASK);
                return new Font(values, font.name, font.style,
                                font.createdFont, font.font2DHandle);
            }
            return font;
        }
        return new Font(attributes);
    }
    private static boolean hasTempPermission() {
        if (System.getSecurityManager() == null) {
            return true;
        }
        File f = null;
        boolean hasPerm = false;
        try {
            f = File.createTempFile("+~JT", ".tmp", null);
            f.delete();
            f = null;
            hasPerm = true;
        } catch (Throwable t) {
        }
        return hasPerm;
    }
    public static Font createFont(int fontFormat, InputStream fontStream)
        throws java.awt.FontFormatException, java.io.IOException {
        if (fontFormat != Font.TRUETYPE_FONT &&
            fontFormat != Font.TYPE1_FONT) {
            throw new IllegalArgumentException ("font format not recognized");
        }
        boolean copiedFontData = false;
        try {
            final File tFile = AccessController.doPrivileged(
                new PrivilegedExceptionAction<File>() {
                    public File run() throws IOException {
                        return File.createTempFile("+~JF", ".tmp", null);
                    }
                }
            );
            int totalSize = 0;
            CreatedFontTracker tracker = null;
            try {
                final OutputStream outStream =
                    AccessController.doPrivileged(
                        new PrivilegedExceptionAction<OutputStream>() {
                            public OutputStream run() throws IOException {
                                return new FileOutputStream(tFile);
                            }
                        }
                    );
                if (!hasTempPermission()) {
                    tracker = CreatedFontTracker.getTracker();
                }
                try {
                    byte[] buf = new byte[8192];
                    for (;;) {
                        int bytesRead = fontStream.read(buf);
                        if (bytesRead < 0) {
                            break;
                        }
                        if (tracker != null) {
                            if (totalSize+bytesRead > tracker.MAX_FILE_SIZE) {
                                throw new IOException("File too big.");
                            }
                            if (totalSize+tracker.getNumBytes() >
                                tracker.MAX_TOTAL_BYTES)
                              {
                                throw new IOException("Total files too big.");
                            }
                            totalSize += bytesRead;
                            tracker.addBytes(bytesRead);
                        }
                        outStream.write(buf, 0, bytesRead);
                    }
                } finally {
                    outStream.close();
                }
                copiedFontData = true;
                Font font = new Font(tFile, fontFormat, true, tracker);
                return font;
            } finally {
                if (!copiedFontData) {
                    if (tracker != null) {
                        tracker.subBytes(totalSize);
                    }
                    AccessController.doPrivileged(
                        new PrivilegedExceptionAction<Void>() {
                            public Void run() {
                                tFile.delete();
                                return null;
                            }
                        }
                    );
                }
            }
        } catch (Throwable t) {
            if (t instanceof FontFormatException) {
                throw (FontFormatException)t;
            }
            if (t instanceof IOException) {
                throw (IOException)t;
            }
            Throwable cause = t.getCause();
            if (cause instanceof FontFormatException) {
                throw (FontFormatException)cause;
            }
            throw new IOException("Problem reading font data.");
        }
    }
    public static Font createFont(int fontFormat, File fontFile)
        throws java.awt.FontFormatException, java.io.IOException {
        fontFile = new File(fontFile.getPath());
        if (fontFormat != Font.TRUETYPE_FONT &&
            fontFormat != Font.TYPE1_FONT) {
            throw new IllegalArgumentException ("font format not recognized");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            FilePermission filePermission =
                new FilePermission(fontFile.getPath(), "read");
            sm.checkPermission(filePermission);
        }
        if (!fontFile.canRead()) {
            throw new IOException("Can't read " + fontFile);
        }
        return new Font(fontFile, fontFormat, false, null);
    }
    public AffineTransform getTransform() {
        if (nonIdentityTx) {
            AttributeValues values = getAttributeValues();
            AffineTransform at = values.isNonDefault(ETRANSFORM)
                ? new AffineTransform(values.getTransform())
                : new AffineTransform();
            if (values.getSuperscript() != 0) {
                int superscript = values.getSuperscript();
                double trans = 0;
                int n = 0;
                boolean up = superscript > 0;
                int sign = up ? -1 : 1;
                int ss = up ? superscript : -superscript;
                while ((ss & 7) > n) {
                    int newn = ss & 7;
                    trans += sign * (ssinfo[newn] - ssinfo[n]);
                    ss >>= 3;
                    sign = -sign;
                    n = newn;
                }
                trans *= pointSize;
                double scale = Math.pow(2./3., n);
                at.preConcatenate(AffineTransform.getTranslateInstance(0, trans));
                at.scale(scale, scale);
            }
            if (values.isNonDefault(EWIDTH)) {
                at.scale(values.getWidth(), 1f);
            }
            return at;
        }
        return new AffineTransform();
    }
    private static final float[] ssinfo = {
        0.0f,
        0.375f,
        0.625f,
        0.7916667f,
        0.9027778f,
        0.9768519f,
        1.0262346f,
        1.0591564f,
    };
    public String getFamily() {
        return getFamily_NoClientCode();
    }
    final String getFamily_NoClientCode() {
        return getFamily(Locale.getDefault());
    }
    public String getFamily(Locale l) {
        if (l == null) {
            throw new NullPointerException("null locale doesn't mean default");
        }
        return getFont2D().getFamilyName(l);
    }
    public String getPSName() {
        return getFont2D().getPostscriptName();
    }
    public String getName() {
        return name;
    }
    public String getFontName() {
      return getFontName(Locale.getDefault());
    }
    public String getFontName(Locale l) {
        if (l == null) {
            throw new NullPointerException("null locale doesn't mean default");
        }
        return getFont2D().getFontName(l);
    }
    public int getStyle() {
        return style;
    }
    public int getSize() {
        return size;
    }
    public float getSize2D() {
        return pointSize;
    }
    public boolean isPlain() {
        return style == 0;
    }
    public boolean isBold() {
        return (style & BOLD) != 0;
    }
    public boolean isItalic() {
        return (style & ITALIC) != 0;
    }
    public boolean isTransformed() {
        return nonIdentityTx;
    }
    public boolean hasLayoutAttributes() {
        return hasLayoutAttributes;
    }
    public static Font getFont(String nm) {
        return getFont(nm, null);
    }
    public static Font decode(String str) {
        String fontName = str;
        String styleName = "";
        int fontSize = 12;
        int fontStyle = Font.PLAIN;
        if (str == null) {
            return new Font(DIALOG, fontStyle, fontSize);
        }
        int lastHyphen = str.lastIndexOf('-');
        int lastSpace = str.lastIndexOf(' ');
        char sepChar = (lastHyphen > lastSpace) ? '-' : ' ';
        int sizeIndex = str.lastIndexOf(sepChar);
        int styleIndex = str.lastIndexOf(sepChar, sizeIndex-1);
        int strlen = str.length();
        if (sizeIndex > 0 && sizeIndex+1 < strlen) {
            try {
                fontSize =
                    Integer.valueOf(str.substring(sizeIndex+1)).intValue();
                if (fontSize <= 0) {
                    fontSize = 12;
                }
            } catch (NumberFormatException e) {
                styleIndex = sizeIndex;
                sizeIndex = strlen;
                if (str.charAt(sizeIndex-1) == sepChar) {
                    sizeIndex--;
                }
            }
        }
        if (styleIndex >= 0 && styleIndex+1 < strlen) {
            styleName = str.substring(styleIndex+1, sizeIndex);
            styleName = styleName.toLowerCase(Locale.ENGLISH);
            if (styleName.equals("bolditalic")) {
                fontStyle = Font.BOLD | Font.ITALIC;
            } else if (styleName.equals("italic")) {
                fontStyle = Font.ITALIC;
            } else if (styleName.equals("bold")) {
                fontStyle = Font.BOLD;
            } else if (styleName.equals("plain")) {
                fontStyle = Font.PLAIN;
            } else {
                styleIndex = sizeIndex;
                if (str.charAt(styleIndex-1) == sepChar) {
                    styleIndex--;
                }
            }
            fontName = str.substring(0, styleIndex);
        } else {
            int fontEnd = strlen;
            if (styleIndex > 0) {
                fontEnd = styleIndex;
            } else if (sizeIndex > 0) {
                fontEnd = sizeIndex;
            }
            if (fontEnd > 0 && str.charAt(fontEnd-1) == sepChar) {
                fontEnd--;
            }
            fontName = str.substring(0, fontEnd);
        }
        return new Font(fontName, fontStyle, fontSize);
    }
    public static Font getFont(String nm, Font font) {
        String str = null;
        try {
            str =System.getProperty(nm);
        } catch(SecurityException e) {
        }
        if (str == null) {
            return font;
        }
        return decode ( str );
    }
    transient int hash;
    public int hashCode() {
        if (hash == 0) {
            hash = name.hashCode() ^ style ^ size;
            if (nonIdentityTx &&
                values != null && values.getTransform() != null) {
                hash ^= values.getTransform().hashCode();
            }
        }
        return hash;
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null) {
            try {
                Font font = (Font)obj;
                if (size == font.size &&
                    style == font.style &&
                    nonIdentityTx == font.nonIdentityTx &&
                    hasLayoutAttributes == font.hasLayoutAttributes &&
                    pointSize == font.pointSize &&
                    name.equals(font.name)) {
                    if (values == null) {
                        if (font.values == null) {
                            return true;
                        } else {
                            return getAttributeValues().equals(font.values);
                        }
                    } else {
                        return values.equals(font.getAttributeValues());
                    }
                }
            }
            catch (ClassCastException e) {
            }
        }
        return false;
    }
    public String toString() {
        String  strStyle;
        if (isBold()) {
            strStyle = isItalic() ? "bolditalic" : "bold";
        } else {
            strStyle = isItalic() ? "italic" : "plain";
        }
        return getClass().getName() + "[family=" + getFamily() + ",name=" + name + ",style=" +
            strStyle + ",size=" + size + "]";
    } 
    private int fontSerializedDataVersion = 1;
    private void writeObject(java.io.ObjectOutputStream s)
      throws java.lang.ClassNotFoundException,
             java.io.IOException
    {
        if (values != null) {
          synchronized(values) {
            fRequestedAttributes = values.toSerializableHashtable();
            s.defaultWriteObject();
            fRequestedAttributes = null;
          }
        } else {
          s.defaultWriteObject();
        }
    }
    private void readObject(java.io.ObjectInputStream s)
      throws java.lang.ClassNotFoundException,
             java.io.IOException
    {
        s.defaultReadObject();
        if (pointSize == 0) {
            pointSize = (float)size;
        }
        if (fRequestedAttributes != null) {
            values = getAttributeValues(); 
            AttributeValues extras =
                AttributeValues.fromSerializableHashtable(fRequestedAttributes);
            if (!AttributeValues.is16Hashtable(fRequestedAttributes)) {
                extras.unsetDefault(); 
            }
            values = getAttributeValues().merge(extras);
            this.nonIdentityTx = values.anyNonDefault(EXTRA_MASK);
            this.hasLayoutAttributes =  values.anyNonDefault(LAYOUT_MASK);
            fRequestedAttributes = null; 
        }
    }
    public int getNumGlyphs() {
        return  getFont2D().getNumGlyphs();
    }
    public int getMissingGlyphCode() {
        return getFont2D().getMissingGlyphCode();
    }
    public byte getBaselineFor(char c) {
        return getFont2D().getBaselineFor(c);
    }
    public Map<TextAttribute,?> getAttributes(){
        return new AttributeMap(getAttributeValues());
    }
    public Attribute[] getAvailableAttributes() {
        Attribute attributes[] = {
            TextAttribute.FAMILY,
            TextAttribute.WEIGHT,
            TextAttribute.WIDTH,
            TextAttribute.POSTURE,
            TextAttribute.SIZE,
            TextAttribute.TRANSFORM,
            TextAttribute.SUPERSCRIPT,
            TextAttribute.CHAR_REPLACEMENT,
            TextAttribute.FOREGROUND,
            TextAttribute.BACKGROUND,
            TextAttribute.UNDERLINE,
            TextAttribute.STRIKETHROUGH,
            TextAttribute.RUN_DIRECTION,
            TextAttribute.BIDI_EMBEDDING,
            TextAttribute.JUSTIFICATION,
            TextAttribute.INPUT_METHOD_HIGHLIGHT,
            TextAttribute.INPUT_METHOD_UNDERLINE,
            TextAttribute.SWAP_COLORS,
            TextAttribute.NUMERIC_SHAPING,
            TextAttribute.KERNING,
            TextAttribute.LIGATURES,
            TextAttribute.TRACKING,
        };
        return attributes;
    }
    public Font deriveFont(int style, float size){
        if (values == null) {
            return new Font(name, style, size, createdFont, font2DHandle);
        }
        AttributeValues newValues = getAttributeValues().clone();
        int oldStyle = (this.style != style) ? this.style : -1;
        applyStyle(style, newValues);
        newValues.setSize(size);
        return new Font(newValues, null, oldStyle, createdFont, font2DHandle);
    }
    public Font deriveFont(int style, AffineTransform trans){
        AttributeValues newValues = getAttributeValues().clone();
        int oldStyle = (this.style != style) ? this.style : -1;
        applyStyle(style, newValues);
        applyTransform(trans, newValues);
        return new Font(newValues, null, oldStyle, createdFont, font2DHandle);
    }
    public Font deriveFont(float size){
        if (values == null) {
            return new Font(name, style, size, createdFont, font2DHandle);
        }
        AttributeValues newValues = getAttributeValues().clone();
        newValues.setSize(size);
        return new Font(newValues, null, -1, createdFont, font2DHandle);
    }
    public Font deriveFont(AffineTransform trans){
        AttributeValues newValues = getAttributeValues().clone();
        applyTransform(trans, newValues);
        return new Font(newValues, null, -1, createdFont, font2DHandle);
    }
    public Font deriveFont(int style){
        if (values == null) {
           return new Font(name, style, size, createdFont, font2DHandle);
        }
        AttributeValues newValues = getAttributeValues().clone();
        int oldStyle = (this.style != style) ? this.style : -1;
        applyStyle(style, newValues);
        return new Font(newValues, null, oldStyle, createdFont, font2DHandle);
    }
    public Font deriveFont(Map<? extends Attribute, ?> attributes) {
        if (attributes == null) {
            return this;
        }
        AttributeValues newValues = getAttributeValues().clone();
        newValues.merge(attributes, RECOGNIZED_MASK);
        return new Font(newValues, name, style, createdFont, font2DHandle);
    }
    public boolean canDisplay(char c){
        return getFont2D().canDisplay(c);
    }
    public boolean canDisplay(int codePoint) {
        if (!Character.isValidCodePoint(codePoint)) {
            throw new IllegalArgumentException("invalid code point: " +
                                               Integer.toHexString(codePoint));
        }
        return getFont2D().canDisplay(codePoint);
    }
    public int canDisplayUpTo(String str) {
        Font2D font2d = getFont2D();
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (font2d.canDisplay(c)) {
                continue;
            }
            if (!Character.isHighSurrogate(c)) {
                return i;
            }
            if (!font2d.canDisplay(str.codePointAt(i))) {
                return i;
            }
            i++;
        }
        return -1;
    }
    public int canDisplayUpTo(char[] text, int start, int limit) {
        Font2D font2d = getFont2D();
        for (int i = start; i < limit; i++) {
            char c = text[i];
            if (font2d.canDisplay(c)) {
                continue;
            }
            if (!Character.isHighSurrogate(c)) {
                return i;
            }
            if (!font2d.canDisplay(Character.codePointAt(text, i, limit))) {
                return i;
            }
            i++;
        }
        return -1;
    }
    public int canDisplayUpTo(CharacterIterator iter, int start, int limit) {
        Font2D font2d = getFont2D();
        char c = iter.setIndex(start);
        for (int i = start; i < limit; i++, c = iter.next()) {
            if (font2d.canDisplay(c)) {
                continue;
            }
            if (!Character.isHighSurrogate(c)) {
                return i;
            }
            char c2 = iter.next();
            if (!Character.isLowSurrogate(c2)) {
                return i;
            }
            if (!font2d.canDisplay(Character.toCodePoint(c, c2))) {
                return i;
            }
            i++;
        }
        return -1;
    }
    public float getItalicAngle() {
        return getItalicAngle(null);
    }
    private float getItalicAngle(FontRenderContext frc) {
        Object aa, fm;
        if (frc == null) {
            aa = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
            fm = RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
        } else {
            aa = frc.getAntiAliasingHint();
            fm = frc.getFractionalMetricsHint();
        }
        return getFont2D().getItalicAngle(this, identityTx, aa, fm);
    }
    public boolean hasUniformLineMetrics() {
        return false;   
    }
    private transient SoftReference flmref;
    private FontLineMetrics defaultLineMetrics(FontRenderContext frc) {
        FontLineMetrics flm = null;
        if (flmref == null
            || (flm = (FontLineMetrics)flmref.get()) == null
            || !flm.frc.equals(frc)) {
            float [] metrics = new float[8];
            getFont2D().getFontMetrics(this, identityTx,
                                       frc.getAntiAliasingHint(),
                                       frc.getFractionalMetricsHint(),
                                       metrics);
            float ascent  = metrics[0];
            float descent = metrics[1];
            float leading = metrics[2];
            float ssOffset = 0;
            if (values != null && values.getSuperscript() != 0) {
                ssOffset = (float)getTransform().getTranslateY();
                ascent -= ssOffset;
                descent += ssOffset;
            }
            float height = ascent + descent + leading;
            int baselineIndex = 0; 
            float[] baselineOffsets = { 0, (descent/2f - ascent) / 2f, -ascent };
            float strikethroughOffset = metrics[4];
            float strikethroughThickness = metrics[5];
            float underlineOffset = metrics[6];
            float underlineThickness = metrics[7];
            float italicAngle = getItalicAngle(frc);
            if (isTransformed()) {
                AffineTransform ctx = values.getCharTransform(); 
                if (ctx != null) {
                    Point2D.Float pt = new Point2D.Float();
                    pt.setLocation(0, strikethroughOffset);
                    ctx.deltaTransform(pt, pt);
                    strikethroughOffset = pt.y;
                    pt.setLocation(0, strikethroughThickness);
                    ctx.deltaTransform(pt, pt);
                    strikethroughThickness = pt.y;
                    pt.setLocation(0, underlineOffset);
                    ctx.deltaTransform(pt, pt);
                    underlineOffset = pt.y;
                    pt.setLocation(0, underlineThickness);
                    ctx.deltaTransform(pt, pt);
                    underlineThickness = pt.y;
                }
            }
            strikethroughOffset += ssOffset;
            underlineOffset += ssOffset;
            CoreMetrics cm = new CoreMetrics(ascent, descent, leading, height,
                                             baselineIndex, baselineOffsets,
                                             strikethroughOffset, strikethroughThickness,
                                             underlineOffset, underlineThickness,
                                             ssOffset, italicAngle);
            flm = new FontLineMetrics(0, cm, frc);
            flmref = new SoftReference(flm);
        }
        return (FontLineMetrics)flm.clone();
    }
    public LineMetrics getLineMetrics( String str, FontRenderContext frc) {
        FontLineMetrics flm = defaultLineMetrics(frc);
        flm.numchars = str.length();
        return flm;
    }
    public LineMetrics getLineMetrics( String str,
                                    int beginIndex, int limit,
                                    FontRenderContext frc) {
        FontLineMetrics flm = defaultLineMetrics(frc);
        int numChars = limit - beginIndex;
        flm.numchars = (numChars < 0)? 0: numChars;
        return flm;
    }
    public LineMetrics getLineMetrics(char [] chars,
                                    int beginIndex, int limit,
                                    FontRenderContext frc) {
        FontLineMetrics flm = defaultLineMetrics(frc);
        int numChars = limit - beginIndex;
        flm.numchars = (numChars < 0)? 0: numChars;
        return flm;
    }
    public LineMetrics getLineMetrics(CharacterIterator ci,
                                    int beginIndex, int limit,
                                    FontRenderContext frc) {
        FontLineMetrics flm = defaultLineMetrics(frc);
        int numChars = limit - beginIndex;
        flm.numchars = (numChars < 0)? 0: numChars;
        return flm;
    }
    public Rectangle2D getStringBounds( String str, FontRenderContext frc) {
        char[] array = str.toCharArray();
        return getStringBounds(array, 0, array.length, frc);
    }
    public Rectangle2D getStringBounds( String str,
                                    int beginIndex, int limit,
                                        FontRenderContext frc) {
        String substr = str.substring(beginIndex, limit);
        return getStringBounds(substr, frc);
    }
    public Rectangle2D getStringBounds(char [] chars,
                                    int beginIndex, int limit,
                                       FontRenderContext frc) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("beginIndex: " + beginIndex);
        }
        if (limit > chars.length) {
            throw new IndexOutOfBoundsException("limit: " + limit);
        }
        if (beginIndex > limit) {
            throw new IndexOutOfBoundsException("range length: " +
                                                (limit - beginIndex));
        }
        boolean simple = values == null ||
            (values.getKerning() == 0 && values.getLigatures() == 0 &&
              values.getBaselineTransform() == null);
        if (simple) {
            simple = ! FontUtilities.isComplexText(chars, beginIndex, limit);
        }
        if (simple) {
            GlyphVector gv = new StandardGlyphVector(this, chars, beginIndex,
                                                     limit - beginIndex, frc);
            return gv.getLogicalBounds();
        } else {
            String str = new String(chars, beginIndex, limit - beginIndex);
            TextLayout tl = new TextLayout(str, this, frc);
            return new Rectangle2D.Float(0, -tl.getAscent(), tl.getAdvance(),
                                         tl.getAscent() + tl.getDescent() +
                                         tl.getLeading());
        }
    }
    public Rectangle2D getStringBounds(CharacterIterator ci,
                                    int beginIndex, int limit,
                                       FontRenderContext frc) {
        int start = ci.getBeginIndex();
        int end = ci.getEndIndex();
        if (beginIndex < start) {
            throw new IndexOutOfBoundsException("beginIndex: " + beginIndex);
        }
        if (limit > end) {
            throw new IndexOutOfBoundsException("limit: " + limit);
        }
        if (beginIndex > limit) {
            throw new IndexOutOfBoundsException("range length: " +
                                                (limit - beginIndex));
        }
        char[]  arr = new char[limit - beginIndex];
        ci.setIndex(beginIndex);
        for(int idx = 0; idx < arr.length; idx++) {
            arr[idx] = ci.current();
            ci.next();
        }
        return getStringBounds(arr,0,arr.length,frc);
    }
    public Rectangle2D getMaxCharBounds(FontRenderContext frc) {
        float [] metrics = new float[4];
        getFont2D().getFontMetrics(this, frc, metrics);
        return new Rectangle2D.Float(0, -metrics[0],
                                metrics[3],
                                metrics[0] + metrics[1] + metrics[2]);
    }
    public GlyphVector createGlyphVector(FontRenderContext frc, String str)
    {
        return (GlyphVector)new StandardGlyphVector(this, str, frc);
    }
    public GlyphVector createGlyphVector(FontRenderContext frc, char[] chars)
    {
        return (GlyphVector)new StandardGlyphVector(this, chars, frc);
    }
    public GlyphVector createGlyphVector(   FontRenderContext frc,
                                            CharacterIterator ci)
    {
        return (GlyphVector)new StandardGlyphVector(this, ci, frc);
    }
    public GlyphVector createGlyphVector(   FontRenderContext frc,
                                            int [] glyphCodes)
    {
        return (GlyphVector)new StandardGlyphVector(this, glyphCodes, frc);
    }
    public GlyphVector layoutGlyphVector(FontRenderContext frc,
                                         char[] text,
                                         int start,
                                         int limit,
                                         int flags) {
        GlyphLayout gl = GlyphLayout.get(null); 
        StandardGlyphVector gv = gl.layout(this, frc, text,
                                           start, limit-start, flags, null);
        GlyphLayout.done(gl);
        return gv;
    }
    public static final int LAYOUT_LEFT_TO_RIGHT = 0;
    public static final int LAYOUT_RIGHT_TO_LEFT = 1;
    public static final int LAYOUT_NO_START_CONTEXT = 2;
    public static final int LAYOUT_NO_LIMIT_CONTEXT = 4;
    private static void applyTransform(AffineTransform trans, AttributeValues values) {
        if (trans == null) {
            throw new IllegalArgumentException("transform must not be null");
        }
        values.setTransform(trans);
    }
    private static void applyStyle(int style, AttributeValues values) {
        values.setWeight((style & BOLD) != 0 ? 2f : 1f);
        values.setPosture((style & ITALIC) != 0 ? .2f : 0f);
    }
    private static native void initIDs();
}
