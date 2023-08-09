public final class GlyphLayout {
    private GVData _gvdata;
    private static volatile GlyphLayout cache;  
    private LayoutEngineFactory _lef;  
    private TextRecord _textRecord;    
    private ScriptRun _scriptRuns;     
    private FontRunIterator _fontRuns; 
    private int _ercount;
    private ArrayList _erecords;
    private Point2D.Float _pt;
    private FontStrikeDesc _sd;
    private float[] _mat;
    private int _typo_flags;
    private int _offset;
    public static final class LayoutEngineKey {
        private Font2D font;
        private int script;
        private int lang;
        LayoutEngineKey() {
        }
        LayoutEngineKey(Font2D font, int script, int lang) {
            init(font, script, lang);
        }
        void init(Font2D font, int script, int lang) {
            this.font = font;
            this.script = script;
            this.lang = lang;
        }
        LayoutEngineKey copy() {
            return new LayoutEngineKey(font, script, lang);
        }
        Font2D font() {
            return font;
        }
        int script() {
            return script;
        }
        int lang() {
            return lang;
        }
        public boolean equals(Object rhs) {
            if (this == rhs) return true;
            if (rhs == null) return false;
            try {
                LayoutEngineKey that = (LayoutEngineKey)rhs;
                return this.script == that.script &&
                       this.lang == that.lang &&
                       this.font.equals(that.font);
            }
            catch (ClassCastException e) {
                return false;
            }
        }
        public int hashCode() {
            return script ^ lang ^ font.hashCode();
        }
    }
    public static interface LayoutEngineFactory {
        public LayoutEngine getEngine(Font2D font, int script, int lang);
        public LayoutEngine getEngine(LayoutEngineKey key);
    }
    public static interface LayoutEngine {
        public void layout(FontStrikeDesc sd, float[] mat, int gmask,
                           int baseIndex, TextRecord text, int typo_flags, Point2D.Float pt, GVData data);
    }
    public static GlyphLayout get(LayoutEngineFactory lef) {
        if (lef == null) {
            lef = SunLayoutEngine.instance();
        }
        GlyphLayout result = null;
        synchronized(GlyphLayout.class) {
            if (cache != null) {
                result = cache;
                cache = null;
            }
        }
        if (result == null) {
            result = new GlyphLayout();
        }
        result._lef = lef;
        return result;
    }
    public static void done(GlyphLayout gl) {
        gl._lef = null;
        cache = gl; 
    }
    private static final class SDCache {
        public Font key_font;
        public FontRenderContext key_frc;
        public AffineTransform dtx;
        public AffineTransform invdtx;
        public AffineTransform gtx;
        public Point2D.Float delta;
        public FontStrikeDesc sd;
        private SDCache(Font font, FontRenderContext frc) {
            key_font = font;
            key_frc = frc;
            dtx = frc.getTransform();
            dtx.setTransform(dtx.getScaleX(), dtx.getShearY(),
                             dtx.getShearX(), dtx.getScaleY(),
                             0, 0);
            if (!dtx.isIdentity()) {
                try {
                    invdtx = dtx.createInverse();
                }
                catch (NoninvertibleTransformException e) {
                    throw new InternalError();
                }
            }
            float ptSize = font.getSize2D();
            if (font.isTransformed()) {
                gtx = font.getTransform();
                gtx.scale(ptSize, ptSize);
                delta = new Point2D.Float((float)gtx.getTranslateX(),
                                          (float)gtx.getTranslateY());
                gtx.setTransform(gtx.getScaleX(), gtx.getShearY(),
                                 gtx.getShearX(), gtx.getScaleY(),
                                 0, 0);
                gtx.preConcatenate(dtx);
            } else {
                delta = ZERO_DELTA;
                gtx = new AffineTransform(dtx);
                gtx.scale(ptSize, ptSize);
            }
            int aa =
                FontStrikeDesc.getAAHintIntVal(frc.getAntiAliasingHint(),
                                               FontUtilities.getFont2D(font),
                                               (int)Math.abs(ptSize));
            int fm = FontStrikeDesc.getFMHintIntVal
                (frc.getFractionalMetricsHint());
            sd = new FontStrikeDesc(dtx, gtx, font.getStyle(), aa, fm);
        }
        private static final Point2D.Float ZERO_DELTA = new Point2D.Float();
        private static
            SoftReference<ConcurrentHashMap<SDKey, SDCache>> cacheRef;
        private static final class SDKey {
            private final Font font;
            private final FontRenderContext frc;
            private final int hash;
            SDKey(Font font, FontRenderContext frc) {
                this.font = font;
                this.frc = frc;
                this.hash = font.hashCode() ^ frc.hashCode();
            }
            public int hashCode() {
                return hash;
            }
            public boolean equals(Object o) {
                try {
                    SDKey rhs = (SDKey)o;
                    return
                        hash == rhs.hash &&
                        font.equals(rhs.font) &&
                        frc.equals(rhs.frc);
                }
                catch (ClassCastException e) {
                }
                return false;
            }
        }
        public static SDCache get(Font font, FontRenderContext frc) {
            if (frc.isTransformed()) {
                AffineTransform transform = frc.getTransform();
                if (transform.getTranslateX() != 0 ||
                    transform.getTranslateY() != 0) {
                    transform = new AffineTransform(transform.getScaleX(),
                                                    transform.getShearY(),
                                                    transform.getShearX(),
                                                    transform.getScaleY(),
                                                    0, 0);
                    frc = new FontRenderContext(transform,
                                                frc.getAntiAliasingHint(),
                                                frc.getFractionalMetricsHint()
                                                );
                }
            }
            SDKey key = new SDKey(font, frc); 
            ConcurrentHashMap<SDKey, SDCache> cache = null;
            SDCache res = null;
            if (cacheRef != null) {
                cache = cacheRef.get();
                if (cache != null) {
                    res = cache.get(key);
                }
            }
            if (res == null) {
                res = new SDCache(font, frc);
                if (cache == null) {
                    cache = new ConcurrentHashMap<SDKey, SDCache>(10);
                    cacheRef = new
                       SoftReference<ConcurrentHashMap<SDKey, SDCache>>(cache);
                } else if (cache.size() >= 512) {
                    cache.clear();
                }
                cache.put(key, res);
            }
            return res;
        }
    }
    public StandardGlyphVector layout(Font font, FontRenderContext frc,
                                      char[] text, int offset, int count,
                                      int flags, StandardGlyphVector result)
    {
        if (text == null || offset < 0 || count < 0 || (count > text.length - offset)) {
            throw new IllegalArgumentException();
        }
        init(count);
        if (font.hasLayoutAttributes()) {
            AttributeValues values = ((AttributeMap)font.getAttributes()).getValues();
            if (values.getKerning() != 0) _typo_flags |= 0x1;
            if (values.getLigatures() != 0) _typo_flags |= 0x2;
        }
        _offset = offset;
        SDCache txinfo = SDCache.get(font, frc);
        _mat[0] = (float)txinfo.gtx.getScaleX();
        _mat[1] = (float)txinfo.gtx.getShearY();
        _mat[2] = (float)txinfo.gtx.getShearX();
        _mat[3] = (float)txinfo.gtx.getScaleY();
        _pt.setLocation(txinfo.delta);
        int lim = offset + count;
        int min = 0;
        int max = text.length;
        if (flags != 0) {
            if ((flags & Font.LAYOUT_RIGHT_TO_LEFT) != 0) {
              _typo_flags |= 0x80000000; 
            }
            if ((flags & Font.LAYOUT_NO_START_CONTEXT) != 0) {
                min = offset;
            }
            if ((flags & Font.LAYOUT_NO_LIMIT_CONTEXT) != 0) {
                max = lim;
            }
        }
        int lang = -1; 
        Font2D font2D = FontUtilities.getFont2D(font);
        _textRecord.init(text, offset, lim, min, max);
        int start = offset;
        if (font2D instanceof CompositeFont) {
            _scriptRuns.init(text, offset, count); 
            _fontRuns.init((CompositeFont)font2D, text, offset, lim);
            while (_scriptRuns.next()) {
                int limit = _scriptRuns.getScriptLimit();
                int script = _scriptRuns.getScriptCode();
                while (_fontRuns.next(script, limit)) {
                    Font2D pfont = _fontRuns.getFont();
                    if (pfont instanceof NativeFont) {
                        pfont = ((NativeFont)pfont).getDelegateFont();
                    }
                    int gmask = _fontRuns.getGlyphMask();
                    int pos = _fontRuns.getPos();
                    nextEngineRecord(start, pos, script, lang, pfont, gmask);
                    start = pos;
                }
            }
        } else {
            _scriptRuns.init(text, offset, count); 
            while (_scriptRuns.next()) {
                int limit = _scriptRuns.getScriptLimit();
                int script = _scriptRuns.getScriptCode();
                nextEngineRecord(start, limit, script, lang, font2D, 0);
                start = limit;
            }
        }
        int ix = 0;
        int stop = _ercount;
        int dir = 1;
        if (_typo_flags < 0) { 
            ix = stop - 1;
            stop = -1;
            dir = -1;
        }
        _sd = txinfo.sd;
        for (;ix != stop; ix += dir) {
            EngineRecord er = (EngineRecord)_erecords.get(ix);
            for (;;) {
                try {
                    er.layout();
                    break;
                }
                catch (IndexOutOfBoundsException e) {
                    _gvdata.grow();
                }
            }
        }
        StandardGlyphVector gv = _gvdata.createGlyphVector(font, frc, result);
        return gv;
    }
    private GlyphLayout() {
        this._gvdata = new GVData();
        this._textRecord = new TextRecord();
        this._scriptRuns = new ScriptRun();
        this._fontRuns = new FontRunIterator();
        this._erecords = new ArrayList(10);
        this._pt = new Point2D.Float();
        this._sd = new FontStrikeDesc();
        this._mat = new float[4];
    }
    private void init(int capacity) {
        this._typo_flags = 0;
        this._ercount = 0;
        this._gvdata.init(capacity);
    }
    private void nextEngineRecord(int start, int limit, int script, int lang, Font2D font, int gmask) {
        EngineRecord er = null;
        if (_ercount == _erecords.size()) {
            er = new EngineRecord();
            _erecords.add(er);
        } else {
            er = (EngineRecord)_erecords.get(_ercount);
        }
        er.init(start, limit, font, script, lang, gmask);
        ++_ercount;
    }
    public static final class GVData {
        public int _count; 
        public int _flags;
        public int[] _glyphs;
        public float[] _positions;
        public int[] _indices;
        private static final int UNINITIALIZED_FLAGS = -1;
        public void init(int size) {
            _count = 0;
            _flags = UNINITIALIZED_FLAGS;
            if (_glyphs == null || _glyphs.length < size) {
                if (size < 20) {
                    size = 20;
                }
                _glyphs = new int[size];
                _positions = new float[size * 2 + 2];
                _indices = new int[size];
            }
        }
        public void grow() {
            grow(_glyphs.length / 4); 
        }
        public void grow(int delta) {
            int size = _glyphs.length + delta;
            int[] nglyphs = new int[size];
            System.arraycopy(_glyphs, 0, nglyphs, 0, _count);
            _glyphs = nglyphs;
            float[] npositions = new float[size * 2 + 2];
            System.arraycopy(_positions, 0, npositions, 0, _count * 2 + 2);
            _positions = npositions;
            int[] nindices = new int[size];
            System.arraycopy(_indices, 0, nindices, 0, _count);
            _indices = nindices;
        }
        public void adjustPositions(AffineTransform invdtx) {
            invdtx.transform(_positions, 0, _positions, 0, _count);
        }
        public StandardGlyphVector createGlyphVector(Font font, FontRenderContext frc, StandardGlyphVector result) {
            if (_flags == UNINITIALIZED_FLAGS) {
                _flags = 0;
                if (_count > 1) { 
                    boolean ltr = true;
                    boolean rtl = true;
                    int rtlix = _count; 
                    for (int i = 0; i < _count && (ltr || rtl); ++i) {
                        int cx = _indices[i];
                        ltr = ltr && (cx == i);
                        rtl = rtl && (cx == --rtlix);
                    }
                    if (rtl) _flags |= GlyphVector.FLAG_RUN_RTL;
                    if (!rtl && !ltr) _flags |= GlyphVector.FLAG_COMPLEX_GLYPHS;
                }
                _flags |= GlyphVector.FLAG_HAS_POSITION_ADJUSTMENTS;
            }
            int[] glyphs = new int[_count];
            System.arraycopy(_glyphs, 0, glyphs, 0, _count);
            float[] positions = null;
            if ((_flags & GlyphVector.FLAG_HAS_POSITION_ADJUSTMENTS) != 0) {
                positions = new float[_count * 2 + 2];
                System.arraycopy(_positions, 0, positions, 0, positions.length);
            }
            int[] indices = null;
            if ((_flags & GlyphVector.FLAG_COMPLEX_GLYPHS) != 0) {
                indices = new int[_count];
                System.arraycopy(_indices, 0, indices, 0, _count);
            }
            if (result == null) {
                result = new StandardGlyphVector(font, frc, glyphs, positions, indices, _flags);
            } else {
                result.initGlyphVector(font, frc, glyphs, positions, indices, _flags);
            }
            return result;
        }
    }
    private final class EngineRecord {
        private int start;
        private int limit;
        private int gmask;
        private int eflags;
        private LayoutEngineKey key;
        private LayoutEngine engine;
        EngineRecord() {
            key = new LayoutEngineKey();
        }
        void init(int start, int limit, Font2D font, int script, int lang, int gmask) {
            this.start = start;
            this.limit = limit;
            this.gmask = gmask;
            this.key.init(font, script, lang);
            this.eflags = 0;
            for (int i = start; i < limit; ++i) {
                int ch = _textRecord.text[i];
                if (isHighSurrogate((char)ch) &&
                    i < limit - 1 &&
                    isLowSurrogate(_textRecord.text[i+1])) {
                    ch = toCodePoint((char)ch,_textRecord.text[++i]); 
                }
                int gc = getType(ch);
                if (gc == NON_SPACING_MARK ||
                    gc == ENCLOSING_MARK ||
                    gc == COMBINING_SPACING_MARK) { 
                    this.eflags = 0x4;
                    break;
                }
            }
            this.engine = _lef.getEngine(key); 
        }
        void layout() {
            _textRecord.start = start;
            _textRecord.limit = limit;
            engine.layout(_sd, _mat, gmask, start - _offset, _textRecord,
                          _typo_flags | eflags, _pt, _gvdata);
        }
    }
}
