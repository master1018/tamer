public class CombiningPerf {
    private static Font font;
    private static FontRenderContext frc;
    public static void main(String[] args) throws Exception {
        System.err.println("start");
        GraphicsEnvironment.getLocalGraphicsEnvironment();
        font = new Font("Lucida Sans Regular", PLAIN, 12);
        frc = new FontRenderContext(null, false, false);
        String ascii = "the characters are critical noodles?";
        String french = "l'aper\u00e7u caract\u00e8re one \u00e9t\u00e9 cr\u00e9\u00e9s";
        String frenchX = "l'aper\u00e7u caracte\u0300re one e\u0301te\u0301 ere\u0301e\u0301s";
        for (int i = 0; i < 100; ++i) {
            TextLayout tl = new TextLayout(french, font, frc);
            tl = new TextLayout(ascii, font, frc);
            tl = new TextLayout(frenchX, font, frc);
        }
        long atime = test(ascii);
        System.err.println("atime: " + (atime/1000000.0) + " length: " + ascii.length());
        long ftime = test(french);
        System.err.println("ftime: " + (ftime/1000000.0) + " length: " + french.length());
        long xtime = test(frenchX);
        System.err.println("xtime: " + (xtime/1000000.0) + " length: " + frenchX.length());
        long limit = xtime * 2 / 3;
        if (atime > limit || ftime > limit) {
            throw new Exception("took too long");
        }
    }
    private static long test(String text) {
        long start = System.nanoTime();
        for (int i = 0; i < 2000; ++i) {
            TextLayout tl = new TextLayout(text, font, frc);
        }
        return System.nanoTime() - start;
    }
}
