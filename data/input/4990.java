public abstract class TextConstructionTests extends TextTests {
    static Group constructroot;
    static Group constructtestroot;
    public static void init() {
      if (hasGraphics2D) {
        constructroot = new Group(textroot, "construction", "Construction Benchmarks");
        constructtestroot = new Group(constructroot, "tests", "Construction Tests");
        new GVFromFontString();
        new GVFromFontChars();
        new GVFromFontCI();
        new GVFromFontGlyphs();
        new GVFromFontLayout();
        new TLFromFont();
        new TLFromMap();
      }
    }
    public TextConstructionTests(Group parent, String nodeName, String description) {
        super(parent, nodeName, description);
    }
    public static class TCContext extends G2DContext {
        char[] chars1;
        CharacterIterator ci;
        GlyphVector gv;
        int[] glyphs;
        int flags;
        public void init(TestEnvironment env, Result results) {
            super.init(env, results);
            chars1 = new char[chars.length + 2];
            System.arraycopy(chars, 0, chars1, 1, chars.length);
            ci = new ArrayCI(chars1, 1, chars.length);
            gv = font.createGlyphVector(frc, text);
            glyphs = gv.getGlyphCodes(0, gv.getNumGlyphs(), null);
            flags = Bidi.requiresBidi(chars, 0, chars.length)
                ? Font.LAYOUT_LEFT_TO_RIGHT
                : Font.LAYOUT_RIGHT_TO_LEFT;
        }
    }
    public Context createContext() {
        return new TCContext();
    }
    public static class GVFromFontString extends TextConstructionTests {
        public GVFromFontString() {
            super(constructtestroot, "gvfromfontstring", "Call Font.createGlyphVector(FRC, String)");
        }
        public void runTest(Object ctx, int numReps) {
            TCContext tcctx = (TCContext)ctx;
            final Font font = tcctx.font;
            final String text = tcctx.text;
            final FontRenderContext frc = tcctx.frc;
            GlyphVector gv;
            do {
                gv = font.createGlyphVector(frc, text);
            } while (--numReps >= 0);
        }
    }
    public static class GVFromFontChars extends TextConstructionTests {
        public GVFromFontChars() {
            super(constructtestroot, "gvfromfontchars", "Call Font.createGlyphVector(FRC, char[])");
        }
        public void runTest(Object ctx, int numReps) {
            TCContext tcctx = (TCContext)ctx;
            final Font font = tcctx.font;
            final char[] chars = tcctx.chars;
            final FontRenderContext frc = tcctx.frc;
            GlyphVector gv;
            do {
                gv = font.createGlyphVector(frc, chars);
            } while (--numReps >= 0);
        }
    }
    public static class GVFromFontCI extends TextConstructionTests {
        public GVFromFontCI() {
            super(constructtestroot, "gvfromfontci", "Call Font.createGlyphVector(FRC, CharacterIterator)");
        }
        public void runTest(Object ctx, int numReps) {
            TCContext tcctx = (TCContext)ctx;
            final Font font = tcctx.font;
            final CharacterIterator ci = tcctx.ci;
            final FontRenderContext frc = tcctx.frc;
            GlyphVector gv;
            do {
                gv = font.createGlyphVector(frc, ci);
            } while (--numReps >= 0);
        }
    }
    public static class GVFromFontGlyphs extends TextConstructionTests {
        public GVFromFontGlyphs() {
            super(constructtestroot, "gvfromfontglyphs", "Call Font.createGlyphVector(FRC, int[])");
        }
        public void runTest(Object ctx, int numReps) {
            TCContext tcctx = (TCContext)ctx;
            final Font font = tcctx.font;
            final int[] glyphs = tcctx.glyphs;
            final FontRenderContext frc = tcctx.frc;
            GlyphVector gv;
            do {
                gv = font.createGlyphVector(frc, glyphs);
            } while (--numReps >= 0);
        }
    }
    public static class GVFromFontLayout extends TextConstructionTests {
        public GVFromFontLayout() {
            super(constructtestroot, "gvfromfontlayout", "Call Font.layoutGlyphVector(...)");
        }
        public void runTest(Object ctx, int numReps) {
            TCContext tcctx = (TCContext)ctx;
            final Font font = tcctx.font;
            final char[] chars = tcctx.chars1;
            final int start = 1;
            final int limit = chars.length - 1;
            final FontRenderContext frc = tcctx.frc;
            final int flags = tcctx.flags;
            GlyphVector gv;
            do {
                gv = font.layoutGlyphVector(frc, chars, start, limit, flags);
            } while (--numReps >= 0);
        }
    }
    public static class TLFromFont extends TextConstructionTests {
        public TLFromFont() {
            super(constructtestroot, "tlfromfont", "TextLayout(String, Font, FRC)");
        }
        public void runTest(Object ctx, int numReps) {
            TCContext tcctx = (TCContext)ctx;
            final Font font = tcctx.font;
            final String text = tcctx.text;
            final FontRenderContext frc = tcctx.frc;
            TextLayout tl;
            do {
                tl = new TextLayout(text, font, frc);
            } while (--numReps >= 0);
        }
    }
    public static class TLMapContext extends G2DContext {
        Map map;
        public void init(TestEnvironment env, Result results) {
            super.init(env, results);
            map = (Map)env.getModifier(tlmapList);
        }
    }
    public static class TLFromMap extends TextConstructionTests {
        public TLFromMap() {
            super(constructtestroot, "tlfrommap", "TextLayout(String, Map, FRC)");
        }
        public Context createContext() {
            return new TLMapContext();
        }
        public void runTest(Object ctx, int numReps) {
            TLMapContext tlmctx = (TLMapContext)ctx;
            final String text = tlmctx.text;
            final FontRenderContext frc = tlmctx.frc;
            final Map map = tlmctx.map;
            TextLayout tl;
            do {
                tl = new TextLayout(text, map, frc);
            } while (--numReps >= 0);
        }
    }
    public static class ACIContext extends G2DContext {
        AttributedCharacterIterator aci;
        public void init(TestEnvironment env, Result results) {
            super.init(env, results);
            AttributedString astr = new AttributedString(text);
        }
    }
    public class TLFromACI extends TextConstructionTests {
        public TLFromACI() {
            super(constructtestroot, "tlfromaci", "TextLayout(ACI, FRC)");
        }
        public void runTest(Object ctx, int numReps) {
            TCContext tcctx = (TCContext)ctx;
            final Font font = tcctx.font;
            final String text = tcctx.text;
            final FontRenderContext frc = tcctx.frc;
            TextLayout tl;
            do {
                tl = new TextLayout(text, font, frc);
            } while (--numReps >= 0);
        }
    }
    public class TLClone extends TextConstructionTests {
        public TLClone() {
            super(constructtestroot, "tlclone", "call TextLayout.clone()");
        }
        public void runTest(Object ctx, int numReps) {
            TCContext tcctx = (TCContext)ctx;
            final Font font = tcctx.font;
            final String text = tcctx.text;
            final FontRenderContext frc = tcctx.frc;
            TextLayout tl;
            do {
                tl = new TextLayout(text, font, frc);
            } while (--numReps >= 0);
        }
    }
    public class TLJustify extends TextConstructionTests {
        public TLJustify() {
            super(constructtestroot, "tljustify", "call TextLayout.getJustifiedLayout(...)");
        }
        public void runTest(Object ctx, int numReps) {
            TCContext tcctx = (TCContext)ctx;
            final Font font = tcctx.font;
            final String text = tcctx.text;
            final FontRenderContext frc = tcctx.frc;
            TextLayout tl;
            do {
                tl = new TextLayout(text, font, frc);
            } while (--numReps >= 0);
        }
    }
    public class TLFromLBM extends TextConstructionTests {
        public TLFromLBM() {
            super(constructtestroot, "tlfromlbm", "call LineBreakMeasurer.next()");
        }
        public void runTest(Object ctx, int numReps) {
            TCContext tcctx = (TCContext)ctx;
            final Font font = tcctx.font;
            final String text = tcctx.text;
            final FontRenderContext frc = tcctx.frc;
            TextLayout tl;
            do {
                tl = new TextLayout(text, font, frc);
            } while (--numReps >= 0);
        }
    }
    public static final class ArrayCI implements CharacterIterator {
        char[] chars;
        int off;
        int max;
        int pos;
        ArrayCI(char[] chars, int off, int len) {
            if (off < 0 || len < 0 || (len > 0 && (chars == null || chars.length - off < len))) {
                throw new InternalError("bad ArrayCI params");
            }
            this.chars = chars;
            this.off = off;
            this.max = off + len;
            this.pos = off;
        }
        public char first() {
            if (max > off) {
                return chars[pos = off];
            }
            return DONE;
        }
        public char last() {
            if (max > off) {
                return chars[pos = max - 1];
            }
            pos = max;
            return DONE;
        }
        public char current() {
            return pos == max ? DONE : chars[pos];
        }
        public char next() {
            if (++pos < max) {
                return chars[pos];
            }
            pos = max;
            return DONE;
        }
        public char previous() {
            if (--pos >= off) {
                return chars[pos];
            }
            pos = off;
            return DONE;
        }
        public char setIndex(int position) {
            if (position < off || position > max) {
                throw new IllegalArgumentException("pos: " + position + " off: " + off + " len: " + (max - off));
            }
            return ((pos = position) < max) ? chars[position] : DONE;
        }
        public int getBeginIndex() {
            return off;
        }
        public int getEndIndex() {
            return max;
        }
        public int getIndex() {
            return pos;
        }
        public Object clone() {
            try {
                return super.clone();
            }
            catch (Exception e) {
                return new InternalError();
            }
        }
    }
}
