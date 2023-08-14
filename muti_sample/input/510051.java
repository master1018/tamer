public class StaticLayoutTest extends TestCase {
    public void testGetters1() {
        LayoutBuilder b = builder();
        FontMetricsInt fmi = b.paint.getFontMetricsInt();
        Log.i("TG1:paint", fmi.toString());
        Layout l = b.build();
        assertVertMetrics(l, 0, 0,
                fmi.ascent, fmi.descent);
        assertEquals(0, l.getLineStart(0));
        assertEquals(Layout.DIR_LEFT_TO_RIGHT, l.getParagraphDirection(0));
        assertEquals(false, l.getLineContainsTab(0));
        assertEquals(Layout.DIRS_ALL_LEFT_TO_RIGHT, l.getLineDirections(0));
        assertEquals(0, l.getEllipsisCount(0));
        assertEquals(0, l.getEllipsisStart(0));
        assertEquals(b.width, l.getEllipsizedWidth());
    }
    public void testGetters2() {
        LayoutBuilder b = builder()
            .setIncludePad(true);
        FontMetricsInt fmi = b.paint.getFontMetricsInt();
        Layout l = b.build();
        assertVertMetrics(l, fmi.top - fmi.ascent, fmi.bottom - fmi.descent,
                fmi.top, fmi.bottom);
    }
    public void testGetters3() {
        LayoutBuilder b = builder()
            .setIncludePad(true)
            .setWidth(50);
        FontMetricsInt fmi = b.paint.getFontMetricsInt();
        Layout l =  b.build();
        assertVertMetrics(l, fmi.top - fmi.ascent, fmi.bottom - fmi.descent,
            fmi.top, fmi.descent,
            fmi.ascent, fmi.bottom);
    }
    public void testGetters4() {
        LayoutBuilder b = builder()
            .setText("This is a longer test")
            .setIncludePad(true)
            .setWidth(50);
        FontMetricsInt fmi = b.paint.getFontMetricsInt();
        Layout l = b.build();
        assertVertMetrics(l, fmi.top - fmi.ascent, fmi.bottom - fmi.descent,
                fmi.top, fmi.descent,
                fmi.ascent, fmi.descent,
                fmi.ascent, fmi.bottom);
    }
    public void testGetters5() {
        LayoutBuilder b = builder()
            .setText("This is a longer test")
            .setIncludePad(true)
            .setWidth(150);
        b.paint.setTextSize(36);
        FontMetricsInt fmi = b.paint.getFontMetricsInt();
        if (fmi.leading == 0) { 
            Log.i("TG5", "leading is 0, skipping test");
            return;
        }
        Layout l = b.build();
        assertVertMetrics(l, fmi.top - fmi.ascent, fmi.bottom - fmi.descent,
                fmi.top, fmi.descent,
                fmi.ascent, fmi.descent,
                fmi.ascent, fmi.bottom);
    }
    public void testGetters6() {
        int spacingAdd = 2; 
        LayoutBuilder b = builder()
            .setText("This is a longer test")
            .setIncludePad(true)
            .setWidth(50)
            .setSpacingAdd(spacingAdd);
        FontMetricsInt fmi = b.paint.getFontMetricsInt();
        Layout l = b.build();
        assertVertMetrics(l, fmi.top - fmi.ascent, fmi.bottom - fmi.descent,
                fmi.top, fmi.descent + spacingAdd,
                fmi.ascent, fmi.descent + spacingAdd,
                fmi.ascent, fmi.bottom + spacingAdd);
    }
    public void testGetters7() {
        LayoutBuilder b = builder()
            .setText("This is a longer test")
            .setIncludePad(true)
            .setWidth(50)
            .setSpacingAdd(2)
            .setSpacingMult(1.5f);
        FontMetricsInt fmi = b.paint.getFontMetricsInt();
        Scaler s = new Scaler(b.spacingMult, b.spacingAdd);
        Layout l = b.build();
        assertVertMetrics(l, fmi.top - fmi.ascent, fmi.bottom - fmi.descent,
                fmi.top, fmi.descent + s.scale(fmi.descent - fmi.top),
                fmi.ascent, fmi.descent + s.scale(fmi.descent - fmi.ascent),
                fmi.ascent, fmi.bottom + s.scale(fmi.bottom - fmi.ascent));
    }
    public void testGetters8() {
        LayoutBuilder b = builder()
            .setText("This is a longer test")
            .setIncludePad(true)
            .setWidth(50)
            .setSpacingAdd(2)
            .setSpacingMult(.8f);
        FontMetricsInt fmi = b.paint.getFontMetricsInt();
        Scaler s = new Scaler(b.spacingMult, b.spacingAdd);
        Layout l = b.build();
        assertVertMetrics(l, fmi.top - fmi.ascent, fmi.bottom - fmi.descent,
                fmi.top, fmi.descent + s.scale(fmi.descent - fmi.top),
                fmi.ascent, fmi.descent + s.scale(fmi.descent - fmi.ascent),
                fmi.ascent, fmi.bottom + s.scale(fmi.bottom - fmi.ascent));
    }
    private static class Scaler {
        private final float sMult;
        private final float sAdd;
        Scaler(float sMult, float sAdd) {
            this.sMult = sMult - 1;
            this.sAdd = sAdd;
        }
        public int scale(float height) {
            int altVal = (int)(height * sMult + sAdd + 0.5);
            int rndVal = Math.round(height * sMult + sAdd);
            if (altVal != rndVal) {
                Log.i("Scale", "expected scale: " + rndVal +
                        " != returned scale: " + altVal);
            }
            return rndVal;
        }
    }
    private static LayoutBuilder builder() {
        return new LayoutBuilder();
    }
    private static class LayoutBuilder {
        String text = "This is a test";
        TextPaint paint = new TextPaint(); 
        int width = 100;
        Alignment align = ALIGN_NORMAL;
        float spacingMult = 1;
        float spacingAdd = 0;
        boolean includePad = false;
        LayoutBuilder setText(String text) {
            this.text = text;
            return this;
        }
        LayoutBuilder setPaint(TextPaint paint) {
            this.paint = paint;
            return this;
        }
        LayoutBuilder setWidth(int width) {
            this.width = width;
            return this;
        }
        LayoutBuilder setAlignment(Alignment align) {
            this.align = align;
            return this;
        }
        LayoutBuilder setSpacingMult(float spacingMult) {
            this.spacingMult = spacingMult;
            return this;
        }
        LayoutBuilder setSpacingAdd(float spacingAdd) {
            this.spacingAdd = spacingAdd;
            return this;
        }
        LayoutBuilder setIncludePad(boolean includePad) {
            this.includePad = includePad;
            return this;
        }
       Layout build() {
            return  new StaticLayout(text, paint, width, align, spacingMult,
                spacingAdd, includePad);
        }
    }
    private void assertVertMetrics(Layout l, int topPad, int botPad, int... values) {
        assertTopBotPadding(l, topPad, botPad);
        assertLinesMetrics(l, values);
    }
    private void assertLinesMetrics(Layout l, int... values) {
        if ((values.length & 0x1) != 0) {
            throw new IllegalArgumentException(String.valueOf(values.length));
        }
        int lines = values.length >> 1;
        assertEquals(lines, l.getLineCount());
        int t = 0;
        for (int i = 0, n = 0; i < lines; ++i, n += 2) {
            int a = values[n];
            int d = values[n+1];
            int h = -a + d;
            assertLineMetrics(l, i, t, a, d, h);
            t += h;
        }
        assertEquals(t, l.getHeight());
    }
    private void assertLineMetrics(Layout l, int line,
            int top, int ascent, int descent, int height) {
        String info = "line " + line;
        assertEquals(info, top, l.getLineTop(line));
        assertEquals(info, ascent, l.getLineAscent(line));
        assertEquals(info, descent, l.getLineDescent(line));
        assertEquals(info, height, l.getLineBottom(line) - top);
    }
    private void assertTopBotPadding(Layout l, int topPad, int botPad) {
        assertEquals(topPad, l.getTopPadding());
        assertEquals(botPad, l.getBottomPadding());
    }
}
