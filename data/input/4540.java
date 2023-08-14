public final class CompositeFont extends Font2D {
    private boolean[] deferredInitialisation;
    String[] componentFileNames;
    String[] componentNames;
    private PhysicalFont[] components;
    int numSlots;
    int numMetricsSlots;
    int[] exclusionRanges;
    int[] maxIndices;
    int numGlyphs = 0;
    int localeSlot = -1; 
    boolean isStdComposite = true;
    public CompositeFont(String name, String[] compFileNames,
                         String[] compNames, int metricsSlotCnt,
                         int[] exclRanges, int[] maxIndexes,
                         boolean defer, SunFontManager fm) {
        handle = new Font2DHandle(this);
        fullName = name;
        componentFileNames = compFileNames;
        componentNames = compNames;
        if (compNames == null) {
            numSlots = componentFileNames.length;
        } else {
            numSlots = componentNames.length;
        }
        numMetricsSlots = metricsSlotCnt;
        exclusionRanges = exclRanges;
        maxIndices = maxIndexes;
        if (fm.getEUDCFont() != null) {
            numSlots++;
            if (componentNames != null) {
                componentNames = new String[numSlots];
                System.arraycopy(compNames, 0, componentNames, 0, numSlots-1);
                componentNames[numSlots-1] =
                    fm.getEUDCFont().getFontName(null);
            }
            if (componentFileNames != null) {
                componentFileNames = new String[numSlots];
                System.arraycopy(compFileNames, 0,
                                  componentFileNames, 0, numSlots-1);
            }
            components = new PhysicalFont[numSlots];
            components[numSlots-1] = fm.getEUDCFont();
            deferredInitialisation = new boolean[numSlots];
            if (defer) {
                for (int i=0; i<numSlots-1; i++) {
                    deferredInitialisation[i] = true;
                }
            }
        } else {
            components = new PhysicalFont[numSlots];
            deferredInitialisation = new boolean[numSlots];
            if (defer) {
                for (int i=0; i<numSlots; i++) {
                    deferredInitialisation[i] = true;
                }
            }
        }
        fontRank = Font2D.FONT_CONFIG_RANK;
        int index = fullName.indexOf('.');
        if (index>0) {
            familyName = fullName.substring(0, index);
            if (index+1 < fullName.length()) {
                String styleStr = fullName.substring(index+1);
                if ("plain".equals(styleStr)) {
                    style = Font.PLAIN;
                } else if ("bold".equals(styleStr)) {
                    style = Font.BOLD;
                } else if ("italic".equals(styleStr)) {
                    style = Font.ITALIC;
                } else if ("bolditalic".equals(styleStr)) {
                    style = Font.BOLD | Font.ITALIC;
                }
            }
        } else {
            familyName = fullName;
        }
    }
    CompositeFont(PhysicalFont physFont, CompositeFont compFont) {
        isStdComposite = false;
        handle = new Font2DHandle(this);
        fullName = physFont.fullName;
        familyName = physFont.familyName;
        style = physFont.style;
        numMetricsSlots = 1; 
        numSlots = compFont.numSlots+1;
        synchronized (FontManagerFactory.getInstance()) {
            components = new PhysicalFont[numSlots];
            components[0] = physFont;
            System.arraycopy(compFont.components, 0,
                             components, 1, compFont.numSlots);
            if (compFont.componentNames != null) {
                componentNames = new String[numSlots];
                componentNames[0] = physFont.fullName;
                System.arraycopy(compFont.componentNames, 0,
                                 componentNames, 1, compFont.numSlots);
            }
            if (compFont.componentFileNames != null) {
                componentFileNames = new String[numSlots];
                componentFileNames[0] = null;
                System.arraycopy(compFont.componentFileNames, 0,
                                  componentFileNames, 1, compFont.numSlots);
            }
            deferredInitialisation = new boolean[numSlots];
            deferredInitialisation[0] = false;
            System.arraycopy(compFont.deferredInitialisation, 0,
                             deferredInitialisation, 1, compFont.numSlots);
        }
    }
    private void doDeferredInitialisation(int slot) {
        if (deferredInitialisation[slot] == false) {
            return;
        }
        SunFontManager fm = SunFontManager.getInstance();
        synchronized (fm) {
            if (componentNames == null) {
                componentNames = new String[numSlots];
            }
            if (components[slot] == null) {
                if (componentFileNames != null &&
                    componentFileNames[slot] != null) {
                    components[slot] =
                        fm.initialiseDeferredFont(componentFileNames[slot]);
                }
                if (components[slot] == null) {
                    components[slot] = fm.getDefaultPhysicalFont();
                }
                String name = components[slot].getFontName(null);
                if (componentNames[slot] == null) {
                    componentNames[slot] = name;
                } else if (!componentNames[slot].equalsIgnoreCase(name)) {
                    components[slot] =
                        (PhysicalFont) fm.findFont2D(componentNames[slot],
                                                     style,
                                                FontManager.PHYSICAL_FALLBACK);
                }
            }
            deferredInitialisation[slot] = false;
        }
    }
    void replaceComponentFont(PhysicalFont oldFont, PhysicalFont newFont) {
        if (components == null) {
            return;
        }
        for (int slot=0; slot<numSlots; slot++) {
            if (components[slot] == oldFont) {
                components[slot] = newFont;
                if (componentNames != null) {
                    componentNames[slot] = newFont.getFontName(null);
                }
            }
        }
    }
    public boolean isExcludedChar(int slot, int charcode) {
        if (exclusionRanges == null || maxIndices == null ||
            slot >= numMetricsSlots) {
            return false;
        }
        int minIndex = 0;
        int maxIndex = maxIndices[slot];
        if (slot > 0) {
            minIndex = maxIndices[slot - 1];
        }
        int curIndex = minIndex;
        while (maxIndex > curIndex) {
            if ((charcode >= exclusionRanges[curIndex])
                && (charcode <= exclusionRanges[curIndex+1])) {
                return true;      
            }
            curIndex += 2;
        }
        return false;
    }
    public void getStyleMetrics(float pointSize, float[] metrics, int offset) {
        PhysicalFont font = getSlotFont(0);
        if (font == null) { 
            super.getStyleMetrics(pointSize, metrics, offset);
        } else {
            font.getStyleMetrics(pointSize, metrics, offset);
        }
    }
    public int getNumSlots() {
        return numSlots;
    }
    public PhysicalFont getSlotFont(int slot) {
        if (deferredInitialisation[slot]) {
            doDeferredInitialisation(slot);
        }
        SunFontManager fm = SunFontManager.getInstance();
        try {
            PhysicalFont font = components[slot];
            if (font == null) {
                try {
                    font = (PhysicalFont) fm.
                        findFont2D(componentNames[slot], style,
                                   FontManager.PHYSICAL_FALLBACK);
                    components[slot] = font;
                } catch (ClassCastException cce) {
                    font = fm.getDefaultPhysicalFont();
                }
            }
            return font;
        } catch (Exception e) {
            return fm.getDefaultPhysicalFont();
        }
    }
    FontStrike createStrike(FontStrikeDesc desc) {
        return new CompositeStrike(this, desc);
    }
    public boolean isStdComposite() {
        return isStdComposite;
    }
    protected int getValidatedGlyphCode(int glyphCode) {
        int slot = glyphCode >>> 24;
        if (slot >= numSlots) {
            return getMapper().getMissingGlyphCode();
        }
        int slotglyphCode = glyphCode & CompositeStrike.SLOTMASK;
        PhysicalFont slotFont = getSlotFont(slot);
        if (slotFont.getValidatedGlyphCode(slotglyphCode) ==
            slotFont.getMissingGlyphCode()) {
            return getMapper().getMissingGlyphCode();
        } else {
            return glyphCode;
        }
    }
    public CharToGlyphMapper getMapper() {
        if (mapper == null) {
            mapper = new CompositeGlyphMapper(this);
        }
        return mapper;
    }
    public boolean hasSupplementaryChars() {
        for (int i=0; i<numSlots; i++) {
            if (getSlotFont(i).hasSupplementaryChars()) {
                return true;
            }
        }
        return false;
    }
    public int getNumGlyphs() {
        if (numGlyphs == 0) {
            numGlyphs = getMapper().getNumGlyphs();
        }
        return numGlyphs;
    }
    public int getMissingGlyphCode() {
        return getMapper().getMissingGlyphCode();
    }
    public boolean canDisplay(char c) {
        return getMapper().canDisplay(c);
    }
    public boolean useAAForPtSize(int ptsize) {
        if (localeSlot == -1) {
            int numCoreSlots = numMetricsSlots;
            if (numCoreSlots == 1 && !isStdComposite()) {
                numCoreSlots = numSlots;
            }
            for (int slot=0; slot<numCoreSlots; slot++) {
                 if (getSlotFont(slot).supportsEncoding(null)) {
                     localeSlot = slot;
                     break;
                 }
            }
            if (localeSlot == -1) {
                localeSlot = 0;
            }
        }
        return getSlotFont(localeSlot).useAAForPtSize(ptsize);
    }
    public String toString() {
        String ls = (String)java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("line.separator"));
        String componentsStr = "";
        for (int i=0; i<numSlots; i++) {
            componentsStr += "    Slot["+i+"]="+getSlotFont(i)+ls;
        }
        return "** Composite Font: Family=" + familyName +
            " Name=" + fullName + " style=" + style + ls + componentsStr;
    }
}
