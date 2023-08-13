public class FontFamily {
    private static ConcurrentHashMap<String, FontFamily>
        familyNameMap = new ConcurrentHashMap<String, FontFamily>();
    private static HashMap<String, FontFamily> allLocaleNames;
    protected String familyName;
    protected Font2D plain;
    protected Font2D bold;
    protected Font2D italic;
    protected Font2D bolditalic;
    protected boolean logicalFont = false;
    protected int familyRank;
    public static FontFamily getFamily(String name) {
        return familyNameMap.get(name.toLowerCase(Locale.ENGLISH));
    }
    public static String[] getAllFamilyNames() {
        return null;
    }
    static void remove(Font2D font2D) {
        String name = font2D.getFamilyName(Locale.ENGLISH);
        FontFamily family = getFamily(name);
        if (family == null) {
            return;
        }
        if (family.plain == font2D) {
            family.plain = null;
        }
        if (family.bold == font2D) {
            family.bold = null;
        }
        if (family.italic == font2D) {
            family.italic = null;
        }
        if (family.bolditalic == font2D) {
            family.bolditalic = null;
        }
        if (family.plain == null && family.bold == null &&
            family.plain == null && family.bold == null) {
            familyNameMap.remove(name);
        }
    }
    public FontFamily(String name, boolean isLogFont, int rank) {
        logicalFont = isLogFont;
        familyName = name;
        familyRank = rank;
        familyNameMap.put(name.toLowerCase(Locale.ENGLISH), this);
    }
    FontFamily(String name) {
        logicalFont = false;
        familyName = name;
        familyRank = Font2D.DEFAULT_RANK;
    }
    public String getFamilyName() {
        return familyName;
    }
    public int getRank() {
        return familyRank;
    }
    public void setFont(Font2D font, int style) {
        if (font.getRank() > familyRank) {
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger()
                                  .warning("Rejecting adding " + font +
                                           " of lower rank " + font.getRank() +
                                           " to family " + this +
                                           " of rank " + familyRank);
            }
            return;
        }
        switch (style) {
        case Font.PLAIN:
            plain = font;
            break;
        case Font.BOLD:
            bold = font;
            break;
        case Font.ITALIC:
            italic = font;
            break;
        case Font.BOLD|Font.ITALIC:
            bolditalic = font;
            break;
        default:
            break;
        }
    }
    public Font2D getFontWithExactStyleMatch(int style) {
        switch (style) {
        case Font.PLAIN:
            return plain;
        case Font.BOLD:
            return bold;
        case Font.ITALIC:
            return italic;
        case Font.BOLD|Font.ITALIC:
            return bolditalic;
        default:
            return null;
        }
    }
    public Font2D getFont(int style) {
        switch (style) {
        case Font.PLAIN:
            return plain;
        case Font.BOLD:
            if (bold != null) {
                return bold;
            } else if (plain != null && plain.canDoStyle(style)) {
                    return plain;
            } else {
                return null;
            }
        case Font.ITALIC:
            if (italic != null) {
                return italic;
            } else if (plain != null && plain.canDoStyle(style)) {
                    return plain;
            } else {
                return null;
            }
        case Font.BOLD|Font.ITALIC:
            if (bolditalic != null) {
                return bolditalic;
            } else if (italic != null && italic.canDoStyle(style)) {
                    return italic;
            } else if (bold != null && bold.canDoStyle(style)) {
                    return italic;
            } else if (plain != null && plain.canDoStyle(style)) {
                    return plain;
            } else {
                return null;
            }
        default:
            return null;
        }
    }
     Font2D getClosestStyle(int style) {
        switch (style) {
        case Font.PLAIN:
            if (bold != null) {
                return bold;
            } else if (italic != null) {
                return italic;
            } else {
                return bolditalic;
            }
        case Font.BOLD:
            if (plain != null) {
                return plain;
            } else if (bolditalic != null) {
                return bolditalic;
            } else {
                return italic;
            }
        case Font.ITALIC:
            if (bolditalic != null) {
                return bolditalic;
            } else if (plain != null) {
                return plain;
            } else {
                return bold;
            }
        case Font.BOLD|Font.ITALIC:
            if (italic != null) {
                return italic;
            } else if (bold != null) {
                return bold;
            } else {
                return plain;
            }
        }
        return null;
    }
    static synchronized void addLocaleNames(FontFamily family, String[] names){
        if (allLocaleNames == null) {
            allLocaleNames = new HashMap<String, FontFamily>();
        }
        for (int i=0; i<names.length; i++) {
            allLocaleNames.put(names[i].toLowerCase(), family);
        }
    }
    public static synchronized FontFamily getLocaleFamily(String name) {
        if (allLocaleNames == null) {
            return null;
        }
        return allLocaleNames.get(name.toLowerCase());
    }
    public String toString() {
        return
            "Font family: " + familyName +
            " plain="+plain+
            " bold=" + bold +
            " italic=" + italic +
            " bolditalic=" + bolditalic;
    }
}
