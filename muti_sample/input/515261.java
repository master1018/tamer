public final class FontLoader {
    private static final String FONTS_DEFINITIONS = "fonts.xml";
    private static final String NODE_FONTS = "fonts";
    private static final String NODE_FONT = "font";
    private static final String NODE_NAME = "name";
    private static final String NODE_FALLBACK = "fallback";
    private static final String ATTR_TTF = "ttf";
    private static final String FONT_EXT = ".ttf";
    private static final String[] FONT_STYLE_DEFAULT = { "", "-Regular" };
    private static final String[] FONT_STYLE_BOLD = { "-Bold" };
    private static final String[] FONT_STYLE_ITALIC = { "-Italic" };
    private static final String[] FONT_STYLE_BOLDITALIC = { "-BoldItalic" };
    private static final String[][] FONT_STYLES = {
        FONT_STYLE_DEFAULT,
        FONT_STYLE_BOLD,
        FONT_STYLE_ITALIC,
        FONT_STYLE_BOLDITALIC
    };
    private final Map<String, String> mFamilyToTtf = new HashMap<String, String>();
    private final Map<String, Map<Integer, Font>> mTtfToFontMap =
        new HashMap<String, Map<Integer, Font>>();
    private List<Font> mFallBackFonts = null;
    public static FontLoader create(String fontOsLocation) {
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
                parserFactory.setNamespaceAware(true);
            SAXParser parser = parserFactory.newSAXParser();
            File f = new File(fontOsLocation + File.separator + FONTS_DEFINITIONS);
            FontDefinitionParser definitionParser = new FontDefinitionParser(
                    fontOsLocation + File.separator);
            parser.parse(new FileInputStream(f), definitionParser);
            return definitionParser.getFontLoader();
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return null;
    }
    private FontLoader(List<FontInfo> fontList, List<String> fallBackList) {
        for (FontInfo info : fontList) {
            for (String family : info.families) {
                mFamilyToTtf.put(family, info.ttf);
            }
        }
        ArrayList<Font> list = new ArrayList<Font>();
        for (String path : fallBackList) {
            File f = new File(path + FONT_EXT);
            if (f.isFile()) {
                try {
                    Font font = Font.createFont(Font.TRUETYPE_FONT, f);
                    if (font != null) {
                        list.add(font);
                    }
                } catch (FontFormatException e) {
                } catch (IOException e) {
                }
            }
        }
        mFallBackFonts = Collections.unmodifiableList(list);
    }
    public List<Font> getFallBackFonts() {
        return mFallBackFonts;
    }
    public synchronized Font getFont(String family, int[] style) {
        if (family == null) {
            return null;
        }
        String ttf = mFamilyToTtf.get(family);
        if (ttf == null) {
            return null;
        }
        Map<Integer, Font> styleMap = mTtfToFontMap.get(ttf);
        if (styleMap == null) {
            styleMap = new HashMap<Integer, Font>();
            mTtfToFontMap.put(ttf, styleMap);
        }
        Font f = styleMap.get(style);
        if (f != null) {
            return f;
        }
        switch (style[0]) {
            case Typeface.NORMAL:
                f = getFont(ttf, FONT_STYLES[Typeface.NORMAL]);
                break;
            case Typeface.BOLD:
            case Typeface.ITALIC:
                f = getFont(ttf, FONT_STYLES[style[0]]);
                if (f == null) {
                    f = getFont(ttf, FONT_STYLES[Typeface.NORMAL]);
                    style[0] = Typeface.NORMAL;
                }
                break;
            case Typeface.BOLD_ITALIC:
                f = getFont(ttf, FONT_STYLES[style[0]]);
                if (f == null) {
                    f = getFont(ttf, FONT_STYLES[Typeface.BOLD]);
                    if (f != null) {
                        style[0] = Typeface.BOLD;
                    } else {
                        f = getFont(ttf, FONT_STYLES[Typeface.ITALIC]);
                        if (f != null) {
                            style[0] = Typeface.ITALIC;
                        } else {
                            f = getFont(ttf, FONT_STYLES[Typeface.NORMAL]);
                            style[0] = Typeface.NORMAL;
                        }
                    }
                }
                break;
        }
        if (f != null) {
            styleMap.put(style[0], f);
            return f;
        }
        return null;
    }
    private Font getFont(String ttf, String[] fontFileSuffix) {
        for (String suffix : fontFileSuffix) {
            String name = ttf + suffix + FONT_EXT;
            File f = new File(name);
            if (f.isFile()) {
                try {
                    Font font = Font.createFont(Font.TRUETYPE_FONT, f);
                    if (font != null) {
                        return font;
                    }
                } catch (FontFormatException e) {
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
    private final static class FontInfo {
        String ttf;
        final Set<String> families;
        FontInfo() {
            families = new HashSet<String>();
        }
    }
    private final static class FontDefinitionParser extends DefaultHandler {
        private final String mOsFontsLocation;
        private FontInfo mFontInfo = null;
        private final StringBuilder mBuilder = new StringBuilder();
        private List<FontInfo> mFontList;
        private List<String> mFallBackList;
        private FontDefinitionParser(String osFontsLocation) {
            super();
            mOsFontsLocation = osFontsLocation;
        }
        FontLoader getFontLoader() {
            return new FontLoader(mFontList, mFallBackList);
        }
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
                throws SAXException {
            if (NODE_FONTS.equals(localName)) {
                mFontList = new ArrayList<FontInfo>();
                mFallBackList = new ArrayList<String>();
            } else if (NODE_FONT.equals(localName)) {
                if (mFontList != null) {
                    String ttf = attributes.getValue(ATTR_TTF);
                    if (ttf != null) {
                        mFontInfo = new FontInfo();
                        mFontInfo.ttf = mOsFontsLocation + ttf;
                        mFontList.add(mFontInfo);
                    }
                }
            } else if (NODE_NAME.equals(localName)) {
            } else if (NODE_FALLBACK.equals(localName)) {
                if (mFallBackList != null) {
                    String ttf = attributes.getValue(ATTR_TTF);
                    if (ttf != null) {
                        mFallBackList.add(mOsFontsLocation + ttf);
                    }
                }
            }
            mBuilder.setLength(0);
            super.startElement(uri, localName, name, attributes);
        }
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            mBuilder.append(ch, start, length);
        }
        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (NODE_FONTS.equals(localName)) {
            } else if (NODE_FONT.equals(localName)) {
                mFontInfo = null;
            } else if (NODE_NAME.equals(localName)) {
                if (mFontInfo != null) {
                    String family = trimXmlWhitespaces(mBuilder.toString());
                    mFontInfo.families.add(family);
                }
            } else if (NODE_FALLBACK.equals(localName)) {
            }
        }
        private String trimXmlWhitespaces(String value) {
            if (value == null) {
                return null;
            }
            int index;
            while ((index = value.indexOf('\n')) != -1) {
                int left = index - 1;
                while (left >= 0) {
                    if (Character.isWhitespace(value.charAt(left))) {
                        left--;
                    } else {
                        break;
                    }
                }
                int right = index + 1;
                int count = value.length();
                while (right < count) {
                    if (Character.isWhitespace(value.charAt(right))) {
                        right++;
                    } else {
                        break;
                    }
                }
                String leftString = null;
                if (left >= 0) {
                    leftString = value.substring(0, left + 1);
                }
                String rightString = null;
                if (right < count) {
                    rightString = value.substring(right);
                }
                if (leftString != null) {
                    value = leftString;
                    if (rightString != null) {
                        value += " " + rightString;
                    }
                } else {
                    value = rightString != null ? rightString : "";
                }
            }
            int length = value.length();
            char[] buffer = value.toCharArray();
            for (int i = 0 ; i < length ; i++) {
                if (buffer[i] == '\\') {
                    if (buffer[i+1] == 'n') {
                        buffer[i+1] = '\n';
                    }
                    System.arraycopy(buffer, i+1, buffer, i, length - i - 1);
                    length--;
                }
            }
            return new String(buffer, 0, length);
        }
    }
}
