public class FontFinder {
    private static final float DEFAULT_FONT_SIZE = 12;
    private static final Font fonts[] =
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    private static final int NUM_BLOCKS = 256;
    private static final int BLOCK_SIZE = 256;
    private static final int INDEX_MASK = 0xFF;
    private static final int BLOCK_SHIFT = 8;
    private static final int blocks[][] = new int[NUM_BLOCKS][];
    static Font findFontForChar(char c) {
        int blockNum = c >> BLOCK_SHIFT;
        int index = c & INDEX_MASK;
        if (blocks[blockNum] == null) {
            blocks[blockNum] = new int[BLOCK_SIZE];
        }
        if (blocks[blockNum][index] == 0) {
            blocks[blockNum][index] = 1;
            for (int i=0; i<fonts.length; i++) {
                if (fonts[i].canDisplay(c)) {
                    blocks[blockNum][index] = i+1;
                    break;
                }
            }
        }
        return getDefaultSizeFont(blocks[blockNum][index]-1);
    }
    static Font getDefaultSizeFont(int i) {
        if (fonts[i].getSize() != DEFAULT_FONT_SIZE) {
            fonts[i] = fonts[i].deriveFont(DEFAULT_FONT_SIZE);
        }
        return fonts[i];
    }
    static void findFonts(char text[], int runStart, int runLimit, List<Integer> runStarts,
            Map<Integer, Font> fonts) {
        Font prevFont = null;
        Font currFont;
        for (int i = runStart; i < runLimit; i++) {
            currFont = findFontForChar(text[i]);
            if (currFont != prevFont) {
                prevFont = currFont;
                Integer idx = new Integer(i);
                fonts.put(idx, currFont);
                if (i != runStart) {
                    runStarts.add(idx);
                }
            }
        }
    }
}
