public final class FontResolver {
    private Font[] allFonts;
    private Font[] supplementaryFonts;
    private int[]  supplementaryIndices;
    private static final int DEFAULT_SIZE = 12; 
    private Font defaultFont = new Font(Font.DIALOG, Font.PLAIN, DEFAULT_SIZE);
    private static final int SHIFT = 9;
    private static final int BLOCKSIZE = 1<<(16-SHIFT);
    private static final int MASK = BLOCKSIZE-1;
    private int[][] blocks = new int[1<<SHIFT][];
    private FontResolver() {
    }
    private Font[] getAllFonts() {
        if (allFonts == null) {
            allFonts =
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
            for (int i=0; i < allFonts.length; i++) {
                allFonts[i] = allFonts[i].deriveFont((float)DEFAULT_SIZE);
            }
        }
        return allFonts;
    }
    private int getIndexFor(char c) {
        if (defaultFont.canDisplay(c)) {
            return 1;
        }
        for (int i=0; i < getAllFonts().length; i++) {
            if (allFonts[i].canDisplay(c)) {
                return i+2;
            }
        }
        return 1;
    }
    private Font [] getAllSCFonts() {
        if (supplementaryFonts == null) {
            ArrayList<Font> fonts = new ArrayList<Font>();
            ArrayList<Integer> indices = new ArrayList<Integer>();
            for (int i=0; i<getAllFonts().length; i++) {
                Font font = allFonts[i];
                Font2D font2D = FontUtilities.getFont2D(font);
                if (font2D.hasSupplementaryChars()) {
                    fonts.add(font);
                    indices.add(Integer.valueOf(i));
                }
            }
            int len = fonts.size();
            supplementaryIndices = new int[len];
            for (int i=0; i<len; i++) {
                supplementaryIndices[i] = indices.get(i);
            }
            supplementaryFonts = fonts.toArray(new Font[len]);
        }
        return supplementaryFonts;
    }
    private int getIndexFor(int cp) {
        if (defaultFont.canDisplay(cp)) {
            return 1;
        }
        for (int i = 0; i < getAllSCFonts().length; i++) {
            if (supplementaryFonts[i].canDisplay(cp)) {
                return supplementaryIndices[i]+2;
            }
        }
        return 1;
    }
    public int getFontIndex(char c) {
        int blockIndex = c>>SHIFT;
        int[] block = blocks[blockIndex];
        if (block == null) {
            block = new int[BLOCKSIZE];
            blocks[blockIndex] = block;
        }
        int index = c & MASK;
        if (block[index] == 0) {
            block[index] = getIndexFor(c);
        }
        return block[index];
    }
    public int getFontIndex(int cp) {
        if (cp < 0x10000) {
            return getFontIndex((char)cp);
        }
        return getIndexFor(cp);
    }
    public int nextFontRunIndex(CodePointIterator iter) {
        int cp = iter.next();
        int fontIndex = 1;
        if (cp != CodePointIterator.DONE) {
            fontIndex = getFontIndex(cp);
            while ((cp = iter.next()) != CodePointIterator.DONE) {
                if (getFontIndex(cp) != fontIndex) {
                    iter.prev();
                    break;
                }
            }
        }
        return fontIndex;
    }
    public Font getFont(int index, Map attributes) {
        Font font = defaultFont;
        if (index >= 2) {
            font = allFonts[index-2];
        }
        return font.deriveFont(attributes);
    }
    private static FontResolver INSTANCE;
    public static FontResolver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FontResolver();
        }
        return INSTANCE;
    }
}
