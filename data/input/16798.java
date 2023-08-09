public class CSS implements Serializable {
    public static final class Attribute {
        private Attribute(String name, String defaultValue, boolean inherited) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.inherited = inherited;
        }
        public String toString() {
            return name;
        }
        public String getDefaultValue() {
            return defaultValue;
        }
        public boolean isInherited() {
            return inherited;
        }
        private String name;
        private String defaultValue;
        private boolean inherited;
        public static final Attribute BACKGROUND =
            new Attribute("background", null, false);
        public static final Attribute BACKGROUND_ATTACHMENT =
            new Attribute("background-attachment", "scroll", false);
        public static final Attribute BACKGROUND_COLOR =
            new Attribute("background-color", "transparent", false);
        public static final Attribute BACKGROUND_IMAGE =
            new Attribute("background-image", "none", false);
        public static final Attribute BACKGROUND_POSITION =
            new Attribute("background-position", null, false);
        public static final Attribute BACKGROUND_REPEAT =
            new Attribute("background-repeat", "repeat", false);
        public static final Attribute BORDER =
            new Attribute("border", null, false);
        public static final Attribute BORDER_BOTTOM =
            new Attribute("border-bottom", null, false);
        public static final Attribute BORDER_BOTTOM_COLOR =
            new Attribute("border-bottom-color", null, false);
        public static final Attribute BORDER_BOTTOM_STYLE =
            new Attribute("border-bottom-style", "none", false);
        public static final Attribute BORDER_BOTTOM_WIDTH =
            new Attribute("border-bottom-width", "medium", false);
        public static final Attribute BORDER_COLOR =
            new Attribute("border-color", null, false);
        public static final Attribute BORDER_LEFT =
            new Attribute("border-left", null, false);
        public static final Attribute BORDER_LEFT_COLOR =
            new Attribute("border-left-color", null, false);
        public static final Attribute BORDER_LEFT_STYLE =
            new Attribute("border-left-style", "none", false);
        public static final Attribute BORDER_LEFT_WIDTH =
            new Attribute("border-left-width", "medium", false);
        public static final Attribute BORDER_RIGHT =
            new Attribute("border-right", null, false);
        public static final Attribute BORDER_RIGHT_COLOR =
            new Attribute("border-right-color", null, false);
        public static final Attribute BORDER_RIGHT_STYLE =
            new Attribute("border-right-style", "none", false);
        public static final Attribute BORDER_RIGHT_WIDTH =
            new Attribute("border-right-width", "medium", false);
        public static final Attribute BORDER_STYLE =
            new Attribute("border-style", "none", false);
        public static final Attribute BORDER_TOP =
            new Attribute("border-top", null, false);
        public static final Attribute BORDER_TOP_COLOR =
            new Attribute("border-top-color", null, false);
        public static final Attribute BORDER_TOP_STYLE =
            new Attribute("border-top-style", "none", false);
        public static final Attribute BORDER_TOP_WIDTH =
            new Attribute("border-top-width", "medium", false);
        public static final Attribute BORDER_WIDTH =
            new Attribute("border-width", "medium", false);
        public static final Attribute CLEAR =
            new Attribute("clear", "none", false);
        public static final Attribute COLOR =
            new Attribute("color", "black", true);
        public static final Attribute DISPLAY =
            new Attribute("display", "block", false);
        public static final Attribute FLOAT =
            new Attribute("float", "none", false);
        public static final Attribute FONT =
            new Attribute("font", null, true);
        public static final Attribute FONT_FAMILY =
            new Attribute("font-family", null, true);
        public static final Attribute FONT_SIZE =
            new Attribute("font-size", "medium", true);
        public static final Attribute FONT_STYLE =
            new Attribute("font-style", "normal", true);
        public static final Attribute FONT_VARIANT =
            new Attribute("font-variant", "normal", true);
        public static final Attribute FONT_WEIGHT =
            new Attribute("font-weight", "normal", true);
        public static final Attribute HEIGHT =
            new Attribute("height", "auto", false);
        public static final Attribute LETTER_SPACING =
            new Attribute("letter-spacing", "normal", true);
        public static final Attribute LINE_HEIGHT =
            new Attribute("line-height", "normal", true);
        public static final Attribute LIST_STYLE =
            new Attribute("list-style", null, true);
        public static final Attribute LIST_STYLE_IMAGE =
            new Attribute("list-style-image", "none", true);
        public static final Attribute LIST_STYLE_POSITION =
            new Attribute("list-style-position", "outside", true);
        public static final Attribute LIST_STYLE_TYPE =
            new Attribute("list-style-type", "disc", true);
        public static final Attribute MARGIN =
            new Attribute("margin", null, false);
        public static final Attribute MARGIN_BOTTOM =
            new Attribute("margin-bottom", "0", false);
        public static final Attribute MARGIN_LEFT =
            new Attribute("margin-left", "0", false);
        public static final Attribute MARGIN_RIGHT =
            new Attribute("margin-right", "0", false);
        static final Attribute MARGIN_LEFT_LTR =
            new Attribute("margin-left-ltr",
                          Integer.toString(Integer.MIN_VALUE), false);
        static final Attribute MARGIN_LEFT_RTL =
            new Attribute("margin-left-rtl",
                          Integer.toString(Integer.MIN_VALUE), false);
        static final Attribute MARGIN_RIGHT_LTR =
            new Attribute("margin-right-ltr",
                          Integer.toString(Integer.MIN_VALUE), false);
        static final Attribute MARGIN_RIGHT_RTL =
            new Attribute("margin-right-rtl",
                          Integer.toString(Integer.MIN_VALUE), false);
        public static final Attribute MARGIN_TOP =
            new Attribute("margin-top", "0", false);
        public static final Attribute PADDING =
            new Attribute("padding", null, false);
        public static final Attribute PADDING_BOTTOM =
            new Attribute("padding-bottom", "0", false);
        public static final Attribute PADDING_LEFT =
            new Attribute("padding-left", "0", false);
        public static final Attribute PADDING_RIGHT =
            new Attribute("padding-right", "0", false);
        public static final Attribute PADDING_TOP =
            new Attribute("padding-top", "0", false);
        public static final Attribute TEXT_ALIGN =
            new Attribute("text-align", null, true);
        public static final Attribute TEXT_DECORATION =
            new Attribute("text-decoration", "none", true);
        public static final Attribute TEXT_INDENT =
            new Attribute("text-indent", "0", true);
        public static final Attribute TEXT_TRANSFORM =
            new Attribute("text-transform", "none", true);
        public static final Attribute VERTICAL_ALIGN =
            new Attribute("vertical-align", "baseline", false);
        public static final Attribute WORD_SPACING =
            new Attribute("word-spacing", "normal", true);
        public static final Attribute WHITE_SPACE =
            new Attribute("white-space", "normal", true);
        public static final Attribute WIDTH =
            new Attribute("width", "auto", false);
         static final Attribute BORDER_SPACING =
            new Attribute("border-spacing", "0", true);
         static final Attribute CAPTION_SIDE =
            new Attribute("caption-side", "left", true);
        static final Attribute[] allAttributes = {
            BACKGROUND, BACKGROUND_ATTACHMENT, BACKGROUND_COLOR,
            BACKGROUND_IMAGE, BACKGROUND_POSITION, BACKGROUND_REPEAT,
            BORDER, BORDER_BOTTOM, BORDER_BOTTOM_WIDTH, BORDER_COLOR,
            BORDER_LEFT, BORDER_LEFT_WIDTH, BORDER_RIGHT, BORDER_RIGHT_WIDTH,
            BORDER_STYLE, BORDER_TOP, BORDER_TOP_WIDTH, BORDER_WIDTH,
            BORDER_TOP_STYLE, BORDER_RIGHT_STYLE, BORDER_BOTTOM_STYLE,
            BORDER_LEFT_STYLE,
            BORDER_TOP_COLOR, BORDER_RIGHT_COLOR, BORDER_BOTTOM_COLOR,
            BORDER_LEFT_COLOR,
            CLEAR, COLOR, DISPLAY, FLOAT, FONT, FONT_FAMILY, FONT_SIZE,
            FONT_STYLE, FONT_VARIANT, FONT_WEIGHT, HEIGHT, LETTER_SPACING,
            LINE_HEIGHT, LIST_STYLE, LIST_STYLE_IMAGE, LIST_STYLE_POSITION,
            LIST_STYLE_TYPE, MARGIN, MARGIN_BOTTOM, MARGIN_LEFT, MARGIN_RIGHT,
            MARGIN_TOP, PADDING, PADDING_BOTTOM, PADDING_LEFT, PADDING_RIGHT,
            PADDING_TOP, TEXT_ALIGN, TEXT_DECORATION, TEXT_INDENT, TEXT_TRANSFORM,
            VERTICAL_ALIGN, WORD_SPACING, WHITE_SPACE, WIDTH,
            BORDER_SPACING, CAPTION_SIDE,
            MARGIN_LEFT_LTR, MARGIN_LEFT_RTL, MARGIN_RIGHT_LTR, MARGIN_RIGHT_RTL
        };
        private static final Attribute[] ALL_MARGINS =
                { MARGIN_TOP, MARGIN_RIGHT, MARGIN_BOTTOM, MARGIN_LEFT };
        private static final Attribute[] ALL_PADDING =
                { PADDING_TOP, PADDING_RIGHT, PADDING_BOTTOM, PADDING_LEFT };
        private static final Attribute[] ALL_BORDER_WIDTHS =
                { BORDER_TOP_WIDTH, BORDER_RIGHT_WIDTH, BORDER_BOTTOM_WIDTH,
                  BORDER_LEFT_WIDTH };
        private static final Attribute[] ALL_BORDER_STYLES =
                { BORDER_TOP_STYLE, BORDER_RIGHT_STYLE, BORDER_BOTTOM_STYLE,
                  BORDER_LEFT_STYLE };
        private static final Attribute[] ALL_BORDER_COLORS =
                { BORDER_TOP_COLOR, BORDER_RIGHT_COLOR, BORDER_BOTTOM_COLOR,
                  BORDER_LEFT_COLOR };
    }
    static final class Value {
        private Value(String name) {
            this.name = name;
        }
        public String toString() {
            return name;
        }
        static final Value INHERITED = new Value("inherited");
        static final Value NONE = new Value("none");
        static final Value HIDDEN = new Value("hidden");
        static final Value DOTTED = new Value("dotted");
        static final Value DASHED = new Value("dashed");
        static final Value SOLID = new Value("solid");
        static final Value DOUBLE = new Value("double");
        static final Value GROOVE = new Value("groove");
        static final Value RIDGE = new Value("ridge");
        static final Value INSET = new Value("inset");
        static final Value OUTSET = new Value("outset");
        static final Value DISC = new Value("disc");
        static final Value CIRCLE = new Value("circle");
        static final Value SQUARE = new Value("square");
        static final Value DECIMAL = new Value("decimal");
        static final Value LOWER_ROMAN = new Value("lower-roman");
        static final Value UPPER_ROMAN = new Value("upper-roman");
        static final Value LOWER_ALPHA = new Value("lower-alpha");
        static final Value UPPER_ALPHA = new Value("upper-alpha");
        static final Value BACKGROUND_NO_REPEAT = new Value("no-repeat");
        static final Value BACKGROUND_REPEAT = new Value("repeat");
        static final Value BACKGROUND_REPEAT_X = new Value("repeat-x");
        static final Value BACKGROUND_REPEAT_Y = new Value("repeat-y");
        static final Value BACKGROUND_SCROLL = new Value("scroll");
        static final Value BACKGROUND_FIXED = new Value("fixed");
        private String name;
        static final Value[] allValues = {
            INHERITED, NONE, DOTTED, DASHED, SOLID, DOUBLE, GROOVE,
            RIDGE, INSET, OUTSET, DISC, CIRCLE, SQUARE, DECIMAL,
            LOWER_ROMAN, UPPER_ROMAN, LOWER_ALPHA, UPPER_ALPHA,
            BACKGROUND_NO_REPEAT, BACKGROUND_REPEAT,
            BACKGROUND_REPEAT_X, BACKGROUND_REPEAT_Y,
            BACKGROUND_FIXED, BACKGROUND_FIXED
        };
    }
    public CSS() {
        baseFontSize = baseFontSizeIndex + 1;
        valueConvertor = new Hashtable<Object, Object>();
        valueConvertor.put(CSS.Attribute.FONT_SIZE, new FontSize());
        valueConvertor.put(CSS.Attribute.FONT_FAMILY, new FontFamily());
        valueConvertor.put(CSS.Attribute.FONT_WEIGHT, new FontWeight());
        Object bs = new BorderStyle();
        valueConvertor.put(CSS.Attribute.BORDER_TOP_STYLE, bs);
        valueConvertor.put(CSS.Attribute.BORDER_RIGHT_STYLE, bs);
        valueConvertor.put(CSS.Attribute.BORDER_BOTTOM_STYLE, bs);
        valueConvertor.put(CSS.Attribute.BORDER_LEFT_STYLE, bs);
        Object cv = new ColorValue();
        valueConvertor.put(CSS.Attribute.COLOR, cv);
        valueConvertor.put(CSS.Attribute.BACKGROUND_COLOR, cv);
        valueConvertor.put(CSS.Attribute.BORDER_TOP_COLOR, cv);
        valueConvertor.put(CSS.Attribute.BORDER_RIGHT_COLOR, cv);
        valueConvertor.put(CSS.Attribute.BORDER_BOTTOM_COLOR, cv);
        valueConvertor.put(CSS.Attribute.BORDER_LEFT_COLOR, cv);
        Object lv = new LengthValue();
        valueConvertor.put(CSS.Attribute.MARGIN_TOP, lv);
        valueConvertor.put(CSS.Attribute.MARGIN_BOTTOM, lv);
        valueConvertor.put(CSS.Attribute.MARGIN_LEFT, lv);
        valueConvertor.put(CSS.Attribute.MARGIN_LEFT_LTR, lv);
        valueConvertor.put(CSS.Attribute.MARGIN_LEFT_RTL, lv);
        valueConvertor.put(CSS.Attribute.MARGIN_RIGHT, lv);
        valueConvertor.put(CSS.Attribute.MARGIN_RIGHT_LTR, lv);
        valueConvertor.put(CSS.Attribute.MARGIN_RIGHT_RTL, lv);
        valueConvertor.put(CSS.Attribute.PADDING_TOP, lv);
        valueConvertor.put(CSS.Attribute.PADDING_BOTTOM, lv);
        valueConvertor.put(CSS.Attribute.PADDING_LEFT, lv);
        valueConvertor.put(CSS.Attribute.PADDING_RIGHT, lv);
        Object bv = new BorderWidthValue(null, 0);
        valueConvertor.put(CSS.Attribute.BORDER_TOP_WIDTH, bv);
        valueConvertor.put(CSS.Attribute.BORDER_BOTTOM_WIDTH, bv);
        valueConvertor.put(CSS.Attribute.BORDER_LEFT_WIDTH, bv);
        valueConvertor.put(CSS.Attribute.BORDER_RIGHT_WIDTH, bv);
        Object nlv = new LengthValue(true);
        valueConvertor.put(CSS.Attribute.TEXT_INDENT, nlv);
        valueConvertor.put(CSS.Attribute.WIDTH, lv);
        valueConvertor.put(CSS.Attribute.HEIGHT, lv);
        valueConvertor.put(CSS.Attribute.BORDER_SPACING, lv);
        Object sv = new StringValue();
        valueConvertor.put(CSS.Attribute.FONT_STYLE, sv);
        valueConvertor.put(CSS.Attribute.TEXT_DECORATION, sv);
        valueConvertor.put(CSS.Attribute.TEXT_ALIGN, sv);
        valueConvertor.put(CSS.Attribute.VERTICAL_ALIGN, sv);
        Object valueMapper = new CssValueMapper();
        valueConvertor.put(CSS.Attribute.LIST_STYLE_TYPE,
                           valueMapper);
        valueConvertor.put(CSS.Attribute.BACKGROUND_IMAGE,
                           new BackgroundImage());
        valueConvertor.put(CSS.Attribute.BACKGROUND_POSITION,
                           new BackgroundPosition());
        valueConvertor.put(CSS.Attribute.BACKGROUND_REPEAT,
                           valueMapper);
        valueConvertor.put(CSS.Attribute.BACKGROUND_ATTACHMENT,
                           valueMapper);
        Object generic = new CssValue();
        int n = CSS.Attribute.allAttributes.length;
        for (int i = 0; i < n; i++) {
            CSS.Attribute key = CSS.Attribute.allAttributes[i];
            if (valueConvertor.get(key) == null) {
                valueConvertor.put(key, generic);
            }
        }
    }
    void setBaseFontSize(int sz) {
        if (sz < 1)
          baseFontSize = 0;
        else if (sz > 7)
          baseFontSize = 7;
        else
          baseFontSize = sz;
    }
    void setBaseFontSize(String size) {
        int relSize, absSize, diff;
        if (size != null) {
            if (size.startsWith("+")) {
                relSize = Integer.valueOf(size.substring(1)).intValue();
                setBaseFontSize(baseFontSize + relSize);
            } else if (size.startsWith("-")) {
                relSize = -Integer.valueOf(size.substring(1)).intValue();
                setBaseFontSize(baseFontSize + relSize);
            } else {
                setBaseFontSize(Integer.valueOf(size).intValue());
            }
        }
    }
    int getBaseFontSize() {
        return baseFontSize;
    }
    void addInternalCSSValue(MutableAttributeSet attr,
                             CSS.Attribute key, String value) {
        if (key == CSS.Attribute.FONT) {
            ShorthandFontParser.parseShorthandFont(this, value, attr);
        }
        else if (key == CSS.Attribute.BACKGROUND) {
            ShorthandBackgroundParser.parseShorthandBackground
                               (this, value, attr);
        }
        else if (key == CSS.Attribute.MARGIN) {
            ShorthandMarginParser.parseShorthandMargin(this, value, attr,
                                           CSS.Attribute.ALL_MARGINS);
        }
        else if (key == CSS.Attribute.PADDING) {
            ShorthandMarginParser.parseShorthandMargin(this, value, attr,
                                           CSS.Attribute.ALL_PADDING);
        }
        else if (key == CSS.Attribute.BORDER_WIDTH) {
            ShorthandMarginParser.parseShorthandMargin(this, value, attr,
                                           CSS.Attribute.ALL_BORDER_WIDTHS);
        }
        else if (key == CSS.Attribute.BORDER_COLOR) {
            ShorthandMarginParser.parseShorthandMargin(this, value, attr,
                                            CSS.Attribute.ALL_BORDER_COLORS);
        }
        else if (key == CSS.Attribute.BORDER_STYLE) {
            ShorthandMarginParser.parseShorthandMargin(this, value, attr,
                                            CSS.Attribute.ALL_BORDER_STYLES);
        }
        else if ((key == CSS.Attribute.BORDER) ||
                   (key == CSS.Attribute.BORDER_TOP) ||
                   (key == CSS.Attribute.BORDER_RIGHT) ||
                   (key == CSS.Attribute.BORDER_BOTTOM) ||
                   (key == CSS.Attribute.BORDER_LEFT)) {
            ShorthandBorderParser.parseShorthandBorder(attr, key, value);
        }
        else {
            Object iValue = getInternalCSSValue(key, value);
            if (iValue != null) {
                attr.addAttribute(key, iValue);
            }
        }
    }
    Object getInternalCSSValue(CSS.Attribute key, String value) {
        CssValue conv = (CssValue) valueConvertor.get(key);
        Object r = conv.parseCssValue(value);
        return r != null ? r : conv.parseCssValue(key.getDefaultValue());
    }
    Attribute styleConstantsKeyToCSSKey(StyleConstants sc) {
        return styleConstantToCssMap.get(sc);
    }
    Object styleConstantsValueToCSSValue(StyleConstants sc,
                                         Object styleValue) {
        Attribute cssKey = styleConstantsKeyToCSSKey(sc);
        if (cssKey != null) {
            CssValue conv = (CssValue)valueConvertor.get(cssKey);
            return conv.fromStyleConstants(sc, styleValue);
        }
        return null;
    }
    Object cssValueToStyleConstantsValue(StyleConstants key, Object value) {
        if (value instanceof CssValue) {
            return ((CssValue)value).toStyleConstants(key, null);
        }
        return null;
    }
    Font getFont(StyleContext sc, AttributeSet a, int defaultSize, StyleSheet ss) {
        ss = getStyleSheet(ss);
        int size = getFontSize(a, defaultSize, ss);
        StringValue vAlignV = (StringValue)a.getAttribute
                              (CSS.Attribute.VERTICAL_ALIGN);
        if ((vAlignV != null)) {
            String vAlign = vAlignV.toString();
            if ((vAlign.indexOf("sup") >= 0) ||
                (vAlign.indexOf("sub") >= 0)) {
                size -= 2;
            }
        }
        FontFamily familyValue = (FontFamily)a.getAttribute
                                            (CSS.Attribute.FONT_FAMILY);
        String family = (familyValue != null) ? familyValue.getValue() :
                                  Font.SANS_SERIF;
        int style = Font.PLAIN;
        FontWeight weightValue = (FontWeight) a.getAttribute
                                  (CSS.Attribute.FONT_WEIGHT);
        if ((weightValue != null) && (weightValue.getValue() > 400)) {
            style |= Font.BOLD;
        }
        Object fs = a.getAttribute(CSS.Attribute.FONT_STYLE);
        if ((fs != null) && (fs.toString().indexOf("italic") >= 0)) {
            style |= Font.ITALIC;
        }
        if (family.equalsIgnoreCase("monospace")) {
            family = Font.MONOSPACED;
        }
        Font f = sc.getFont(family, style, size);
        if (f == null
            || (f.getFamily().equals(Font.DIALOG)
                && ! family.equalsIgnoreCase(Font.DIALOG))) {
            family = Font.SANS_SERIF;
            f = sc.getFont(family, style, size);
        }
        return f;
    }
    static int getFontSize(AttributeSet attr, int defaultSize, StyleSheet ss) {
        FontSize sizeValue = (FontSize)attr.getAttribute(CSS.Attribute.
                                                         FONT_SIZE);
        return (sizeValue != null) ? sizeValue.getValue(attr, ss)
                                   : defaultSize;
    }
    Color getColor(AttributeSet a, CSS.Attribute key) {
        ColorValue cv = (ColorValue) a.getAttribute(key);
        if (cv != null) {
            return cv.getValue();
        }
        return null;
    }
    float getPointSize(String size, StyleSheet ss) {
        int relSize, absSize, diff, index;
        ss = getStyleSheet(ss);
        if (size != null) {
            if (size.startsWith("+")) {
                relSize = Integer.valueOf(size.substring(1)).intValue();
                return getPointSize(baseFontSize + relSize, ss);
            } else if (size.startsWith("-")) {
                relSize = -Integer.valueOf(size.substring(1)).intValue();
                return getPointSize(baseFontSize + relSize, ss);
            } else {
                absSize = Integer.valueOf(size).intValue();
                return getPointSize(absSize, ss);
            }
        }
        return 0;
    }
    float getLength(AttributeSet a, CSS.Attribute key, StyleSheet ss) {
        ss = getStyleSheet(ss);
        LengthValue lv = (LengthValue) a.getAttribute(key);
        boolean isW3CLengthUnits = (ss == null) ? false : ss.isW3CLengthUnits();
        float len = (lv != null) ? lv.getValue(isW3CLengthUnits) : 0;
        return len;
    }
    AttributeSet translateHTMLToCSS(AttributeSet htmlAttrSet) {
        MutableAttributeSet cssAttrSet = new SimpleAttributeSet();
        Element elem = (Element)htmlAttrSet;
        HTML.Tag tag = getHTMLTag(htmlAttrSet);
        if ((tag == HTML.Tag.TD) || (tag == HTML.Tag.TH)) {
            AttributeSet tableAttr = elem.getParentElement().
                                     getParentElement().getAttributes();
            int borderWidth;
            try {
                borderWidth = Integer.parseInt(
                    (String) tableAttr.getAttribute(HTML.Attribute.BORDER));
            } catch (NumberFormatException e) {
                borderWidth = 0;
            }
            if (borderWidth > 0) {
                translateAttribute(HTML.Attribute.BORDER, tableAttr, cssAttrSet);
            }
            String pad = (String)tableAttr.getAttribute(HTML.Attribute.CELLPADDING);
            if (pad != null) {
                LengthValue v =
                    (LengthValue)getInternalCSSValue(CSS.Attribute.PADDING_TOP, pad);
                v.span = (v.span < 0) ? 0 : v.span;
                cssAttrSet.addAttribute(CSS.Attribute.PADDING_TOP, v);
                cssAttrSet.addAttribute(CSS.Attribute.PADDING_BOTTOM, v);
                cssAttrSet.addAttribute(CSS.Attribute.PADDING_LEFT, v);
                cssAttrSet.addAttribute(CSS.Attribute.PADDING_RIGHT, v);
            }
        }
        if (elem.isLeaf()) {
            translateEmbeddedAttributes(htmlAttrSet, cssAttrSet);
        } else {
            translateAttributes(tag, htmlAttrSet, cssAttrSet);
        }
        if (tag == HTML.Tag.CAPTION) {
            Object v = htmlAttrSet.getAttribute(HTML.Attribute.ALIGN);
            if ((v != null) && (v.equals("top") || v.equals("bottom"))) {
                cssAttrSet.addAttribute(CSS.Attribute.CAPTION_SIDE, v);
                cssAttrSet.removeAttribute(CSS.Attribute.TEXT_ALIGN);
            } else {
                v = htmlAttrSet.getAttribute(HTML.Attribute.VALIGN);
                if (v != null) {
                    cssAttrSet.addAttribute(CSS.Attribute.CAPTION_SIDE, v);
                }
            }
        }
        return cssAttrSet;
    }
    private static final Hashtable<String, Attribute> attributeMap = new Hashtable<String, Attribute>();
    private static final Hashtable<String, Value> valueMap = new Hashtable<String, Value>();
    private static final Hashtable<HTML.Attribute, CSS.Attribute[]> htmlAttrToCssAttrMap = new Hashtable<HTML.Attribute, CSS.Attribute[]>(20);
    private static final Hashtable<Object, Attribute> styleConstantToCssMap = new Hashtable<Object, Attribute>(17);
    private static final Hashtable<String, CSS.Value> htmlValueToCssValueMap = new Hashtable<String, CSS.Value>(8);
    private static final Hashtable<String, CSS.Value> cssValueToInternalValueMap = new Hashtable<String, CSS.Value>(13);
    static {
        for (int i = 0; i < Attribute.allAttributes.length; i++ ) {
            attributeMap.put(Attribute.allAttributes[i].toString(),
                             Attribute.allAttributes[i]);
        }
        for (int i = 0; i < Value.allValues.length; i++ ) {
            valueMap.put(Value.allValues[i].toString(),
                             Value.allValues[i]);
        }
        htmlAttrToCssAttrMap.put(HTML.Attribute.COLOR,
                                 new CSS.Attribute[]{CSS.Attribute.COLOR});
        htmlAttrToCssAttrMap.put(HTML.Attribute.TEXT,
                                 new CSS.Attribute[]{CSS.Attribute.COLOR});
        htmlAttrToCssAttrMap.put(HTML.Attribute.CLEAR,
                                 new CSS.Attribute[]{CSS.Attribute.CLEAR});
        htmlAttrToCssAttrMap.put(HTML.Attribute.BACKGROUND,
                                 new CSS.Attribute[]{CSS.Attribute.BACKGROUND_IMAGE});
        htmlAttrToCssAttrMap.put(HTML.Attribute.BGCOLOR,
                                 new CSS.Attribute[]{CSS.Attribute.BACKGROUND_COLOR});
        htmlAttrToCssAttrMap.put(HTML.Attribute.WIDTH,
                                 new CSS.Attribute[]{CSS.Attribute.WIDTH});
        htmlAttrToCssAttrMap.put(HTML.Attribute.HEIGHT,
                                 new CSS.Attribute[]{CSS.Attribute.HEIGHT});
        htmlAttrToCssAttrMap.put(HTML.Attribute.BORDER,
                                 new CSS.Attribute[]{CSS.Attribute.BORDER_TOP_WIDTH, CSS.Attribute.BORDER_RIGHT_WIDTH, CSS.Attribute.BORDER_BOTTOM_WIDTH, CSS.Attribute.BORDER_LEFT_WIDTH});
        htmlAttrToCssAttrMap.put(HTML.Attribute.CELLPADDING,
                                 new CSS.Attribute[]{CSS.Attribute.PADDING});
        htmlAttrToCssAttrMap.put(HTML.Attribute.CELLSPACING,
                                 new CSS.Attribute[]{CSS.Attribute.BORDER_SPACING});
        htmlAttrToCssAttrMap.put(HTML.Attribute.MARGINWIDTH,
                                 new CSS.Attribute[]{CSS.Attribute.MARGIN_LEFT,
                                                     CSS.Attribute.MARGIN_RIGHT});
        htmlAttrToCssAttrMap.put(HTML.Attribute.MARGINHEIGHT,
                                 new CSS.Attribute[]{CSS.Attribute.MARGIN_TOP,
                                                     CSS.Attribute.MARGIN_BOTTOM});
        htmlAttrToCssAttrMap.put(HTML.Attribute.HSPACE,
                                 new CSS.Attribute[]{CSS.Attribute.PADDING_LEFT,
                                                     CSS.Attribute.PADDING_RIGHT});
        htmlAttrToCssAttrMap.put(HTML.Attribute.VSPACE,
                                 new CSS.Attribute[]{CSS.Attribute.PADDING_BOTTOM,
                                                     CSS.Attribute.PADDING_TOP});
        htmlAttrToCssAttrMap.put(HTML.Attribute.FACE,
                                 new CSS.Attribute[]{CSS.Attribute.FONT_FAMILY});
        htmlAttrToCssAttrMap.put(HTML.Attribute.SIZE,
                                 new CSS.Attribute[]{CSS.Attribute.FONT_SIZE});
        htmlAttrToCssAttrMap.put(HTML.Attribute.VALIGN,
                                 new CSS.Attribute[]{CSS.Attribute.VERTICAL_ALIGN});
        htmlAttrToCssAttrMap.put(HTML.Attribute.ALIGN,
                                 new CSS.Attribute[]{CSS.Attribute.VERTICAL_ALIGN,
                                                     CSS.Attribute.TEXT_ALIGN,
                                                     CSS.Attribute.FLOAT});
        htmlAttrToCssAttrMap.put(HTML.Attribute.TYPE,
                                 new CSS.Attribute[]{CSS.Attribute.LIST_STYLE_TYPE});
        htmlAttrToCssAttrMap.put(HTML.Attribute.NOWRAP,
                                 new CSS.Attribute[]{CSS.Attribute.WHITE_SPACE});
        styleConstantToCssMap.put(StyleConstants.FontFamily,
                                  CSS.Attribute.FONT_FAMILY);
        styleConstantToCssMap.put(StyleConstants.FontSize,
                                  CSS.Attribute.FONT_SIZE);
        styleConstantToCssMap.put(StyleConstants.Bold,
                                  CSS.Attribute.FONT_WEIGHT);
        styleConstantToCssMap.put(StyleConstants.Italic,
                                  CSS.Attribute.FONT_STYLE);
        styleConstantToCssMap.put(StyleConstants.Underline,
                                  CSS.Attribute.TEXT_DECORATION);
        styleConstantToCssMap.put(StyleConstants.StrikeThrough,
                                  CSS.Attribute.TEXT_DECORATION);
        styleConstantToCssMap.put(StyleConstants.Superscript,
                                  CSS.Attribute.VERTICAL_ALIGN);
        styleConstantToCssMap.put(StyleConstants.Subscript,
                                  CSS.Attribute.VERTICAL_ALIGN);
        styleConstantToCssMap.put(StyleConstants.Foreground,
                                  CSS.Attribute.COLOR);
        styleConstantToCssMap.put(StyleConstants.Background,
                                  CSS.Attribute.BACKGROUND_COLOR);
        styleConstantToCssMap.put(StyleConstants.FirstLineIndent,
                                  CSS.Attribute.TEXT_INDENT);
        styleConstantToCssMap.put(StyleConstants.LeftIndent,
                                  CSS.Attribute.MARGIN_LEFT);
        styleConstantToCssMap.put(StyleConstants.RightIndent,
                                  CSS.Attribute.MARGIN_RIGHT);
        styleConstantToCssMap.put(StyleConstants.SpaceAbove,
                                  CSS.Attribute.MARGIN_TOP);
        styleConstantToCssMap.put(StyleConstants.SpaceBelow,
                                  CSS.Attribute.MARGIN_BOTTOM);
        styleConstantToCssMap.put(StyleConstants.Alignment,
                                  CSS.Attribute.TEXT_ALIGN);
        htmlValueToCssValueMap.put("disc", CSS.Value.DISC);
        htmlValueToCssValueMap.put("square", CSS.Value.SQUARE);
        htmlValueToCssValueMap.put("circle", CSS.Value.CIRCLE);
        htmlValueToCssValueMap.put("1", CSS.Value.DECIMAL);
        htmlValueToCssValueMap.put("a", CSS.Value.LOWER_ALPHA);
        htmlValueToCssValueMap.put("A", CSS.Value.UPPER_ALPHA);
        htmlValueToCssValueMap.put("i", CSS.Value.LOWER_ROMAN);
        htmlValueToCssValueMap.put("I", CSS.Value.UPPER_ROMAN);
        cssValueToInternalValueMap.put("none", CSS.Value.NONE);
        cssValueToInternalValueMap.put("disc", CSS.Value.DISC);
        cssValueToInternalValueMap.put("square", CSS.Value.SQUARE);
        cssValueToInternalValueMap.put("circle", CSS.Value.CIRCLE);
        cssValueToInternalValueMap.put("decimal", CSS.Value.DECIMAL);
        cssValueToInternalValueMap.put("lower-roman", CSS.Value.LOWER_ROMAN);
        cssValueToInternalValueMap.put("upper-roman", CSS.Value.UPPER_ROMAN);
        cssValueToInternalValueMap.put("lower-alpha", CSS.Value.LOWER_ALPHA);
        cssValueToInternalValueMap.put("upper-alpha", CSS.Value.UPPER_ALPHA);
        cssValueToInternalValueMap.put("repeat", CSS.Value.BACKGROUND_REPEAT);
        cssValueToInternalValueMap.put("no-repeat",
                                       CSS.Value.BACKGROUND_NO_REPEAT);
        cssValueToInternalValueMap.put("repeat-x",
                                       CSS.Value.BACKGROUND_REPEAT_X);
        cssValueToInternalValueMap.put("repeat-y",
                                       CSS.Value.BACKGROUND_REPEAT_Y);
        cssValueToInternalValueMap.put("scroll",
                                       CSS.Value.BACKGROUND_SCROLL);
        cssValueToInternalValueMap.put("fixed",
                                       CSS.Value.BACKGROUND_FIXED);
        Object[] keys = CSS.Attribute.allAttributes;
        try {
            for (Object key : keys) {
                StyleContext.registerStaticAttributeKey(key);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        keys = CSS.Value.allValues;
        try {
            for (Object key : keys) {
                StyleContext.registerStaticAttributeKey(key);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    public static Attribute[] getAllAttributeKeys() {
        Attribute[] keys = new Attribute[Attribute.allAttributes.length];
        System.arraycopy(Attribute.allAttributes, 0, keys, 0, Attribute.allAttributes.length);
        return keys;
    }
    public static final Attribute getAttribute(String name) {
        return attributeMap.get(name);
    }
    static final Value getValue(String name) {
        return valueMap.get(name);
    }
    static URL getURL(URL base, String cssString) {
        if (cssString == null) {
            return null;
        }
        if (cssString.startsWith("url(") &&
            cssString.endsWith(")")) {
            cssString = cssString.substring(4, cssString.length() - 1);
        }
        try {
            URL url = new URL(cssString);
            if (url != null) {
                return url;
            }
        } catch (MalformedURLException mue) {
        }
        if (base != null) {
            try {
                URL url = new URL(base, cssString);
                return url;
            }
            catch (MalformedURLException muee) {
            }
        }
        return null;
    }
    static String colorToHex(Color color) {
      String colorstr = "#";
      String str = Integer.toHexString(color.getRed());
      if (str.length() > 2)
        str = str.substring(0, 2);
      else if (str.length() < 2)
        colorstr += "0" + str;
      else
        colorstr += str;
      str = Integer.toHexString(color.getGreen());
      if (str.length() > 2)
        str = str.substring(0, 2);
      else if (str.length() < 2)
        colorstr += "0" + str;
      else
        colorstr += str;
      str = Integer.toHexString(color.getBlue());
      if (str.length() > 2)
        str = str.substring(0, 2);
      else if (str.length() < 2)
        colorstr += "0" + str;
      else
        colorstr += str;
      return colorstr;
    }
    static final Color hexToColor(String value) {
        String digits;
        int n = value.length();
        if (value.startsWith("#")) {
            digits = value.substring(1, Math.min(value.length(), 7));
        } else {
            digits = value;
        }
        String hstr = "0x" + digits;
        Color c;
        try {
            c = Color.decode(hstr);
        } catch (NumberFormatException nfe) {
            c = null;
        }
         return c;
     }
    static Color stringToColor(String str) {
      Color color;
      if (str == null) {
          return null;
      }
      if (str.length() == 0)
        color = Color.black;
      else if (str.startsWith("rgb(")) {
          color = parseRGB(str);
      }
      else if (str.charAt(0) == '#')
        color = hexToColor(str);
      else if (str.equalsIgnoreCase("Black"))
        color = hexToColor("#000000");
      else if(str.equalsIgnoreCase("Silver"))
        color = hexToColor("#C0C0C0");
      else if(str.equalsIgnoreCase("Gray"))
        color = hexToColor("#808080");
      else if(str.equalsIgnoreCase("White"))
        color = hexToColor("#FFFFFF");
      else if(str.equalsIgnoreCase("Maroon"))
        color = hexToColor("#800000");
      else if(str.equalsIgnoreCase("Red"))
        color = hexToColor("#FF0000");
      else if(str.equalsIgnoreCase("Purple"))
        color = hexToColor("#800080");
      else if(str.equalsIgnoreCase("Fuchsia"))
        color = hexToColor("#FF00FF");
      else if(str.equalsIgnoreCase("Green"))
        color = hexToColor("#008000");
      else if(str.equalsIgnoreCase("Lime"))
        color = hexToColor("#00FF00");
      else if(str.equalsIgnoreCase("Olive"))
        color = hexToColor("#808000");
      else if(str.equalsIgnoreCase("Yellow"))
        color = hexToColor("#FFFF00");
      else if(str.equalsIgnoreCase("Navy"))
        color = hexToColor("#000080");
      else if(str.equalsIgnoreCase("Blue"))
        color = hexToColor("#0000FF");
      else if(str.equalsIgnoreCase("Teal"))
        color = hexToColor("#008080");
      else if(str.equalsIgnoreCase("Aqua"))
        color = hexToColor("#00FFFF");
      else if(str.equalsIgnoreCase("Orange"))
        color = hexToColor("#FF8000");
      else
          color = hexToColor(str); 
      return color;
    }
    private static Color parseRGB(String string) {
        int[] index = new int[1];
        index[0] = 4;
        int red = getColorComponent(string, index);
        int green = getColorComponent(string, index);
        int blue = getColorComponent(string, index);
        return new Color(red, green, blue);
    }
    private static int getColorComponent(String string, int[] index) {
        int length = string.length();
        char aChar;
        while(index[0] < length && (aChar = string.charAt(index[0])) != '-' &&
              !Character.isDigit(aChar) && aChar != '.') {
            index[0]++;
        }
        int start = index[0];
        if (start < length && string.charAt(index[0]) == '-') {
            index[0]++;
        }
        while(index[0] < length &&
                         Character.isDigit(string.charAt(index[0]))) {
            index[0]++;
        }
        if (index[0] < length && string.charAt(index[0]) == '.') {
            index[0]++;
            while(index[0] < length &&
                  Character.isDigit(string.charAt(index[0]))) {
                index[0]++;
            }
        }
        if (start != index[0]) {
            try {
                float value = Float.parseFloat(string.substring
                                               (start, index[0]));
                if (index[0] < length && string.charAt(index[0]) == '%') {
                    index[0]++;
                    value = value * 255f / 100f;
                }
                return Math.min(255, Math.max(0, (int)value));
            } catch (NumberFormatException nfe) {
            }
        }
        return 0;
    }
    static int getIndexOfSize(float pt, int[] sizeMap) {
        for (int i = 0; i < sizeMap.length; i ++ )
                if (pt <= sizeMap[i])
                        return i + 1;
        return sizeMap.length;
    }
    static int getIndexOfSize(float pt, StyleSheet ss) {
        int[] sizeMap = (ss != null) ? ss.getSizeMap() :
            StyleSheet.sizeMapDefault;
        return getIndexOfSize(pt, sizeMap);
    }
    static String[] parseStrings(String value) {
        int         current, last;
        int         length = (value == null) ? 0 : value.length();
        Vector<String> temp = new Vector<String>(4);
        current = 0;
        while (current < length) {
            while (current < length && Character.isWhitespace
                   (value.charAt(current))) {
                current++;
            }
            last = current;
            while (current < length && !Character.isWhitespace
                   (value.charAt(current))) {
                current++;
            }
            if (last != current) {
                temp.addElement(value.substring(last, current));
            }
            current++;
        }
        String[] retValue = new String[temp.size()];
        temp.copyInto(retValue);
        return retValue;
    }
    float getPointSize(int index, StyleSheet ss) {
        ss = getStyleSheet(ss);
        int[] sizeMap = (ss != null) ? ss.getSizeMap() :
            StyleSheet.sizeMapDefault;
        --index;
        if (index < 0)
          return sizeMap[0];
        else if (index > sizeMap.length - 1)
          return sizeMap[sizeMap.length - 1];
        else
          return sizeMap[index];
    }
    private void translateEmbeddedAttributes(AttributeSet htmlAttrSet,
                                             MutableAttributeSet cssAttrSet) {
        Enumeration keys = htmlAttrSet.getAttributeNames();
        if (htmlAttrSet.getAttribute(StyleConstants.NameAttribute) ==
            HTML.Tag.HR) {
            translateAttributes(HTML.Tag.HR, htmlAttrSet, cssAttrSet);
        }
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key instanceof HTML.Tag) {
                HTML.Tag tag = (HTML.Tag)key;
                Object o = htmlAttrSet.getAttribute(tag);
                if (o != null && o instanceof AttributeSet) {
                    translateAttributes(tag, (AttributeSet)o, cssAttrSet);
                }
            } else if (key instanceof CSS.Attribute) {
                cssAttrSet.addAttribute(key, htmlAttrSet.getAttribute(key));
            }
        }
    }
    private void translateAttributes(HTML.Tag tag,
                                            AttributeSet htmlAttrSet,
                                            MutableAttributeSet cssAttrSet) {
        Enumeration names = htmlAttrSet.getAttributeNames();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            if (name instanceof HTML.Attribute) {
                HTML.Attribute key = (HTML.Attribute)name;
                if (key == HTML.Attribute.ALIGN) {
                    String htmlAttrValue = (String)htmlAttrSet.getAttribute(HTML.Attribute.ALIGN);
                    if (htmlAttrValue != null) {
                        CSS.Attribute cssAttr = getCssAlignAttribute(tag, htmlAttrSet);
                        if (cssAttr != null) {
                            Object o = getCssValue(cssAttr, htmlAttrValue);
                            if (o != null) {
                                cssAttrSet.addAttribute(cssAttr, o);
                            }
                        }
                    }
                } else {
                    if (key == HTML.Attribute.SIZE && !isHTMLFontTag(tag)) {
                        continue;
                    }
                    translateAttribute(key, htmlAttrSet, cssAttrSet);
                }
            } else if (name instanceof CSS.Attribute) {
                cssAttrSet.addAttribute(name, htmlAttrSet.getAttribute(name));
            }
        }
    }
    private void translateAttribute(HTML.Attribute key,
                                           AttributeSet htmlAttrSet,
                                           MutableAttributeSet cssAttrSet) {
        CSS.Attribute[] cssAttrList = getCssAttribute(key);
        String htmlAttrValue = (String)htmlAttrSet.getAttribute(key);
        if (cssAttrList == null || htmlAttrValue == null) {
            return;
        }
        for (Attribute cssAttr : cssAttrList) {
            Object o = getCssValue(cssAttr, htmlAttrValue);
            if (o != null) {
                cssAttrSet.addAttribute(cssAttr , o);
            }
        }
    }
    Object getCssValue(CSS.Attribute cssAttr, String htmlAttrValue) {
        CssValue value = (CssValue)valueConvertor.get(cssAttr);
        Object o = value.parseHtmlValue(htmlAttrValue);
        return o;
    }
    private CSS.Attribute[] getCssAttribute(HTML.Attribute hAttr) {
        return htmlAttrToCssAttrMap.get(hAttr);
    }
    private CSS.Attribute getCssAlignAttribute(HTML.Tag tag,
                                                   AttributeSet htmlAttrSet) {
        return CSS.Attribute.TEXT_ALIGN;
    }
    private HTML.Tag getHTMLTag(AttributeSet htmlAttrSet) {
        Object o = htmlAttrSet.getAttribute(StyleConstants.NameAttribute);
        if (o instanceof HTML.Tag) {
            HTML.Tag tag = (HTML.Tag) o;
            return tag;
        }
        return null;
    }
    private boolean isHTMLFontTag(HTML.Tag tag) {
        return (tag != null && ((tag == HTML.Tag.FONT) || (tag == HTML.Tag.BASEFONT)));
    }
    private boolean isFloater(String alignValue) {
        return (alignValue.equals("left") || alignValue.equals("right"));
    }
    private boolean validTextAlignValue(String alignValue) {
        return (isFloater(alignValue) || alignValue.equals("center"));
    }
    static class CssValue implements Serializable {
        Object parseCssValue(String value) {
            return value;
        }
        Object parseHtmlValue(String value) {
            return parseCssValue(value);
        }
        Object fromStyleConstants(StyleConstants key, Object value) {
            return null;
        }
        Object toStyleConstants(StyleConstants key, View v) {
            return null;
        }
        public String toString() {
            return svalue;
        }
        String svalue;
    }
    static class StringValue extends CssValue {
        Object parseCssValue(String value) {
            StringValue sv = new StringValue();
            sv.svalue = value;
            return sv;
        }
        Object fromStyleConstants(StyleConstants key, Object value) {
            if (key == StyleConstants.Italic) {
                if (value.equals(Boolean.TRUE)) {
                    return parseCssValue("italic");
                }
                return parseCssValue("");
            } else if (key == StyleConstants.Underline) {
                if (value.equals(Boolean.TRUE)) {
                    return parseCssValue("underline");
                }
                return parseCssValue("");
            } else if (key == StyleConstants.Alignment) {
                int align = ((Integer)value).intValue();
                String ta;
                switch(align) {
                case StyleConstants.ALIGN_LEFT:
                    ta = "left";
                    break;
                case StyleConstants.ALIGN_RIGHT:
                    ta = "right";
                    break;
                case StyleConstants.ALIGN_CENTER:
                    ta = "center";
                    break;
                case StyleConstants.ALIGN_JUSTIFIED:
                    ta = "justify";
                    break;
                default:
                    ta = "left";
                }
                return parseCssValue(ta);
            } else if (key == StyleConstants.StrikeThrough) {
                if (value.equals(Boolean.TRUE)) {
                    return parseCssValue("line-through");
                }
                return parseCssValue("");
            } else if (key == StyleConstants.Superscript) {
                if (value.equals(Boolean.TRUE)) {
                    return parseCssValue("super");
                }
                return parseCssValue("");
            } else if (key == StyleConstants.Subscript) {
                if (value.equals(Boolean.TRUE)) {
                    return parseCssValue("sub");
                }
                return parseCssValue("");
            }
            return null;
        }
        Object toStyleConstants(StyleConstants key, View v) {
            if (key == StyleConstants.Italic) {
                if (svalue.indexOf("italic") >= 0) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            } else if (key == StyleConstants.Underline) {
                if (svalue.indexOf("underline") >= 0) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            } else if (key == StyleConstants.Alignment) {
                if (svalue.equals("right")) {
                    return new Integer(StyleConstants.ALIGN_RIGHT);
                } else if (svalue.equals("center")) {
                    return new Integer(StyleConstants.ALIGN_CENTER);
                } else if  (svalue.equals("justify")) {
                    return new Integer(StyleConstants.ALIGN_JUSTIFIED);
                }
                return new Integer(StyleConstants.ALIGN_LEFT);
            } else if (key == StyleConstants.StrikeThrough) {
                if (svalue.indexOf("line-through") >= 0) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            } else if (key == StyleConstants.Superscript) {
                if (svalue.indexOf("super") >= 0) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            } else if (key == StyleConstants.Subscript) {
                if (svalue.indexOf("sub") >= 0) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
            return null;
        }
        boolean isItalic() {
            return (svalue.indexOf("italic") != -1);
        }
        boolean isStrike() {
            return (svalue.indexOf("line-through") != -1);
        }
        boolean isUnderline() {
            return (svalue.indexOf("underline") != -1);
        }
        boolean isSub() {
            return (svalue.indexOf("sub") != -1);
        }
        boolean isSup() {
            return (svalue.indexOf("sup") != -1);
        }
    }
    class FontSize extends CssValue {
        int getValue(AttributeSet a, StyleSheet ss) {
            ss = getStyleSheet(ss);
            if (index) {
                return Math.round(getPointSize((int) value, ss));
            }
            else if (lu == null) {
                return Math.round(value);
            }
            else {
                if (lu.type == 0) {
                    boolean isW3CLengthUnits = (ss == null) ? false : ss.isW3CLengthUnits();
                    return Math.round(lu.getValue(isW3CLengthUnits));
                }
                if (a != null) {
                    AttributeSet resolveParent = a.getResolveParent();
                    if (resolveParent != null) {
                        int pValue = StyleConstants.getFontSize(resolveParent);
                        float retValue;
                        if (lu.type == 1 || lu.type == 3) {
                            retValue = lu.value * (float)pValue;
                        }
                        else {
                            retValue = lu.value + (float)pValue;
                        }
                        return Math.round(retValue);
                    }
                }
                return 12;
            }
        }
        Object parseCssValue(String value) {
            FontSize fs = new FontSize();
            fs.svalue = value;
            try {
                if (value.equals("xx-small")) {
                    fs.value = 1;
                    fs.index = true;
                } else if (value.equals("x-small")) {
                    fs.value = 2;
                    fs.index = true;
                } else if (value.equals("small")) {
                    fs.value = 3;
                    fs.index = true;
                } else if (value.equals("medium")) {
                    fs.value = 4;
                    fs.index = true;
                } else if (value.equals("large")) {
                    fs.value = 5;
                    fs.index = true;
                } else if (value.equals("x-large")) {
                    fs.value = 6;
                    fs.index = true;
                } else if (value.equals("xx-large")) {
                    fs.value = 7;
                    fs.index = true;
                } else {
                    fs.lu = new LengthUnit(value, (short)1, 1f);
                }
            } catch (NumberFormatException nfe) {
                fs = null;
            }
            return fs;
        }
        Object parseHtmlValue(String value) {
            if ((value == null) || (value.length() == 0)) {
                return null;
            }
            FontSize fs = new FontSize();
            fs.svalue = value;
            try {
                int baseFontSize = getBaseFontSize();
                if (value.charAt(0) == '+') {
                    int relSize = Integer.valueOf(value.substring(1)).intValue();
                    fs.value = baseFontSize + relSize;
                    fs.index = true;
                } else if (value.charAt(0) == '-') {
                    int relSize = -Integer.valueOf(value.substring(1)).intValue();
                    fs.value = baseFontSize + relSize;
                    fs.index = true;
                } else {
                    fs.value = Integer.parseInt(value);
                    if (fs.value > 7) {
                        fs.value = 7;
                    } else if (fs.value < 0) {
                        fs.value = 0;
                    }
                    fs.index = true;
                }
            } catch (NumberFormatException nfe) {
                fs = null;
            }
            return fs;
        }
        Object fromStyleConstants(StyleConstants key, Object value) {
            if (value instanceof Number) {
                FontSize fs = new FontSize();
                fs.value = getIndexOfSize(((Number)value).floatValue(), StyleSheet.sizeMapDefault);
                fs.svalue = Integer.toString((int)fs.value);
                fs.index = true;
                return fs;
            }
            return parseCssValue(value.toString());
        }
        Object toStyleConstants(StyleConstants key, View v) {
            if (v != null) {
                return Integer.valueOf(getValue(v.getAttributes(), null));
            }
            return Integer.valueOf(getValue(null, null));
        }
        float value;
        boolean index;
        LengthUnit lu;
    }
    static class FontFamily extends CssValue {
        String getValue() {
            return family;
        }
        Object parseCssValue(String value) {
            int cIndex = value.indexOf(',');
            FontFamily ff = new FontFamily();
            ff.svalue = value;
            ff.family = null;
            if (cIndex == -1) {
                setFontName(ff, value);
            }
            else {
                boolean done = false;
                int lastIndex;
                int length = value.length();
                cIndex = 0;
                while (!done) {
                    while (cIndex < length &&
                           Character.isWhitespace(value.charAt(cIndex)))
                        cIndex++;
                    lastIndex = cIndex;
                    cIndex = value.indexOf(',', cIndex);
                    if (cIndex == -1) {
                        cIndex = length;
                    }
                    if (lastIndex < length) {
                        if (lastIndex != cIndex) {
                            int lastCharIndex = cIndex;
                            if (cIndex > 0 && value.charAt(cIndex - 1) == ' '){
                                lastCharIndex--;
                            }
                            setFontName(ff, value.substring
                                        (lastIndex, lastCharIndex));
                            done = (ff.family != null);
                        }
                        cIndex++;
                    }
                    else {
                        done = true;
                    }
                }
            }
            if (ff.family == null) {
                ff.family = Font.SANS_SERIF;
            }
            return ff;
        }
        private void setFontName(FontFamily ff, String fontName) {
            ff.family = fontName;
        }
        Object parseHtmlValue(String value) {
            return parseCssValue(value);
        }
        Object fromStyleConstants(StyleConstants key, Object value) {
            return parseCssValue(value.toString());
        }
        Object toStyleConstants(StyleConstants key, View v) {
            return family;
        }
        String family;
    }
    static class FontWeight extends CssValue {
        int getValue() {
            return weight;
        }
        Object parseCssValue(String value) {
            FontWeight fw = new FontWeight();
            fw.svalue = value;
            if (value.equals("bold")) {
                fw.weight = 700;
            } else if (value.equals("normal")) {
                fw.weight = 400;
            } else {
                try {
                    fw.weight = Integer.parseInt(value);
                } catch (NumberFormatException nfe) {
                    fw = null;
                }
            }
            return fw;
        }
        Object fromStyleConstants(StyleConstants key, Object value) {
            if (value.equals(Boolean.TRUE)) {
                return parseCssValue("bold");
            }
            return parseCssValue("normal");
        }
        Object toStyleConstants(StyleConstants key, View v) {
            return (weight > 500) ? Boolean.TRUE : Boolean.FALSE;
        }
        boolean isBold() {
            return (weight > 500);
        }
        int weight;
    }
    static class ColorValue extends CssValue {
        Color getValue() {
            return c;
        }
        Object parseCssValue(String value) {
            Color c = stringToColor(value);
            if (c != null) {
                ColorValue cv = new ColorValue();
                cv.svalue = value;
                cv.c = c;
                return cv;
            }
            return null;
        }
        Object parseHtmlValue(String value) {
            return parseCssValue(value);
        }
        Object fromStyleConstants(StyleConstants key, Object value) {
            ColorValue colorValue = new ColorValue();
            colorValue.c = (Color)value;
            colorValue.svalue = colorToHex(colorValue.c);
            return colorValue;
        }
        Object toStyleConstants(StyleConstants key, View v) {
            return c;
        }
        Color c;
    }
    static class BorderStyle extends CssValue {
        CSS.Value getValue() {
            return style;
        }
        Object parseCssValue(String value) {
            CSS.Value cssv = CSS.getValue(value);
            if (cssv != null) {
                if ((cssv == CSS.Value.INSET) ||
                    (cssv == CSS.Value.OUTSET) ||
                    (cssv == CSS.Value.NONE) ||
                    (cssv == CSS.Value.DOTTED) ||
                    (cssv == CSS.Value.DASHED) ||
                    (cssv == CSS.Value.SOLID) ||
                    (cssv == CSS.Value.DOUBLE) ||
                    (cssv == CSS.Value.GROOVE) ||
                    (cssv == CSS.Value.RIDGE)) {
                    BorderStyle bs = new BorderStyle();
                    bs.svalue = value;
                    bs.style = cssv;
                    return bs;
                }
            }
            return null;
        }
        private void writeObject(java.io.ObjectOutputStream s)
                     throws IOException {
            s.defaultWriteObject();
            if (style == null) {
                s.writeObject(null);
            }
            else {
                s.writeObject(style.toString());
            }
        }
        private void readObject(ObjectInputStream s)
                throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            Object value = s.readObject();
            if (value != null) {
                style = CSS.getValue((String)value);
            }
        }
        transient private CSS.Value style;
    }
    static class LengthValue extends CssValue {
        boolean mayBeNegative;
        LengthValue() {
            this(false);
        }
        LengthValue(boolean mayBeNegative) {
            this.mayBeNegative = mayBeNegative;
        }
        float getValue() {
            return getValue(false);
        }
        float getValue(boolean isW3CLengthUnits) {
            return getValue(0, isW3CLengthUnits);
        }
        float getValue(float currentValue) {
            return getValue(currentValue, false);
        }
        float getValue(float currentValue, boolean isW3CLengthUnits) {
            if (percentage) {
                return span * currentValue;
            }
            return LengthUnit.getValue(span, units, isW3CLengthUnits);
        }
        boolean isPercentage() {
            return percentage;
        }
        Object parseCssValue(String value) {
            LengthValue lv;
            try {
                float absolute = Float.valueOf(value).floatValue();
                lv = new LengthValue();
                lv.span = absolute;
            } catch (NumberFormatException nfe) {
                LengthUnit lu = new LengthUnit(value,
                                               LengthUnit.UNINITALIZED_LENGTH,
                                               0);
                switch (lu.type) {
                case 0:
                    lv = new LengthValue();
                    lv.span =
                        (mayBeNegative) ? lu.value : Math.max(0, lu.value);
                    lv.units = lu.units;
                    break;
                case 1:
                    lv = new LengthValue();
                    lv.span = Math.max(0, Math.min(1, lu.value));
                    lv.percentage = true;
                    break;
                default:
                    return null;
                }
            }
            lv.svalue = value;
            return lv;
        }
        Object parseHtmlValue(String value) {
            if (value.equals(HTML.NULL_ATTRIBUTE_VALUE)) {
                value = "1";
            }
            return parseCssValue(value);
        }
        Object fromStyleConstants(StyleConstants key, Object value) {
            LengthValue v = new LengthValue();
            v.svalue = value.toString();
            v.span = ((Float)value).floatValue();
            return v;
        }
        Object toStyleConstants(StyleConstants key, View v) {
            return new Float(getValue(false));
        }
        boolean percentage;
        float span;
        String units = null;
    }
    static class BorderWidthValue extends LengthValue {
        BorderWidthValue(String svalue, int index) {
            this.svalue = svalue;
            span = values[index];
            percentage = false;
        }
        Object parseCssValue(String value) {
            if (value != null) {
                if (value.equals("thick")) {
                    return new BorderWidthValue(value, 2);
                }
                else if (value.equals("medium")) {
                    return new BorderWidthValue(value, 1);
                }
                else if (value.equals("thin")) {
                    return new BorderWidthValue(value, 0);
                }
            }
            return super.parseCssValue(value);
        }
        Object parseHtmlValue(String value) {
            if (value == HTML.NULL_ATTRIBUTE_VALUE) {
                return parseCssValue("medium");
            }
            return parseCssValue(value);
        }
        private static final float[] values = { 1, 2, 4 };
   }
    static class CssValueMapper extends CssValue {
        Object parseCssValue(String value) {
            Object retValue = cssValueToInternalValueMap.get(value);
            if (retValue == null) {
                retValue = cssValueToInternalValueMap.get(value.toLowerCase());
            }
            return retValue;
        }
        Object parseHtmlValue(String value) {
            Object retValue = htmlValueToCssValueMap.get(value);
            if (retValue == null) {
                retValue = htmlValueToCssValueMap.get(value.toLowerCase());
            }
            return retValue;
        }
    }
    static class BackgroundPosition extends CssValue {
        float horizontalPosition;
        float verticalPosition;
        short relative;
        Object parseCssValue(String value) {
            String[]  strings = CSS.parseStrings(value);
            int count = strings.length;
            BackgroundPosition bp = new BackgroundPosition();
            bp.relative = 5;
            bp.svalue = value;
            if (count > 0) {
                short found = 0;
                int index = 0;
                while (index < count) {
                    String string = strings[index++];
                    if (string.equals("center")) {
                        found |= 4;
                        continue;
                    }
                    else {
                        if ((found & 1) == 0) {
                            if (string.equals("top")) {
                                found |= 1;
                            }
                            else if (string.equals("bottom")) {
                                found |= 1;
                                bp.verticalPosition = 1;
                                continue;
                            }
                        }
                        if ((found & 2) == 0) {
                            if (string.equals("left")) {
                                found |= 2;
                                bp.horizontalPosition = 0;
                            }
                            else if (string.equals("right")) {
                                found |= 2;
                                bp.horizontalPosition = 1;
                            }
                        }
                    }
                }
                if (found != 0) {
                    if ((found & 1) == 1) {
                        if ((found & 2) == 0) {
                            bp.horizontalPosition = .5f;
                        }
                    }
                    else if ((found & 2) == 2) {
                        bp.verticalPosition = .5f;
                    }
                    else {
                        bp.horizontalPosition = bp.verticalPosition = .5f;
                    }
                }
                else {
                    LengthUnit lu = new LengthUnit(strings[0], (short)0, 0f);
                    if (lu.type == 0) {
                        bp.horizontalPosition = lu.value;
                        bp.relative = (short)(1 ^ bp.relative);
                    }
                    else if (lu.type == 1) {
                        bp.horizontalPosition = lu.value;
                    }
                    else if (lu.type == 3) {
                        bp.horizontalPosition = lu.value;
                        bp.relative = (short)((1 ^ bp.relative) | 2);
                    }
                    if (count > 1) {
                        lu = new LengthUnit(strings[1], (short)0, 0f);
                        if (lu.type == 0) {
                            bp.verticalPosition = lu.value;
                            bp.relative = (short)(4 ^ bp.relative);
                        }
                        else if (lu.type == 1) {
                            bp.verticalPosition = lu.value;
                        }
                        else if (lu.type == 3) {
                            bp.verticalPosition = lu.value;
                            bp.relative = (short)((4 ^ bp.relative) | 8);
                        }
                    }
                    else {
                        bp.verticalPosition = .5f;
                    }
                }
            }
            return bp;
        }
        boolean isHorizontalPositionRelativeToSize() {
            return ((relative & 1) == 1);
        }
        boolean isHorizontalPositionRelativeToFontSize() {
            return ((relative & 2) == 2);
        }
        float getHorizontalPosition() {
            return horizontalPosition;
        }
        boolean isVerticalPositionRelativeToSize() {
            return ((relative & 4) == 4);
        }
        boolean isVerticalPositionRelativeToFontSize() {
            return ((relative & 8) == 8);
        }
        float getVerticalPosition() {
            return verticalPosition;
        }
    }
    static class BackgroundImage extends CssValue {
        private boolean    loadedImage;
        private ImageIcon  image;
        Object parseCssValue(String value) {
            BackgroundImage retValue = new BackgroundImage();
            retValue.svalue = value;
            return retValue;
        }
        Object parseHtmlValue(String value) {
            return parseCssValue(value);
        }
        ImageIcon getImage(URL base) {
            if (!loadedImage) {
                synchronized(this) {
                    if (!loadedImage) {
                        URL url = CSS.getURL(base, svalue);
                        loadedImage = true;
                        if (url != null) {
                            image = new ImageIcon();
                            Image tmpImg = Toolkit.getDefaultToolkit().createImage(url);
                            if (tmpImg != null) {
                                image.setImage(tmpImg);
                            }
                        }
                    }
                }
            }
            return image;
        }
    }
    static class LengthUnit implements Serializable {
        static Hashtable<String, Float> lengthMapping = new Hashtable<String, Float>(6);
        static Hashtable<String, Float> w3cLengthMapping = new Hashtable<String, Float>(6);
        static {
            lengthMapping.put("pt", new Float(1f));
            lengthMapping.put("px", new Float(1.3f));
            lengthMapping.put("mm", new Float(2.83464f));
            lengthMapping.put("cm", new Float(28.3464f));
            lengthMapping.put("pc", new Float(12f));
            lengthMapping.put("in", new Float(72f));
            int res = 72;
            try {
                res = Toolkit.getDefaultToolkit().getScreenResolution();
            } catch (HeadlessException e) {
            }
            w3cLengthMapping.put("pt", new Float(res/72f));
            w3cLengthMapping.put("px", new Float(1f));
            w3cLengthMapping.put("mm", new Float(res/25.4f));
            w3cLengthMapping.put("cm", new Float(res/2.54f));
            w3cLengthMapping.put("pc", new Float(res/6f));
            w3cLengthMapping.put("in", new Float(res));
        }
        LengthUnit(String value, short defaultType, float defaultValue) {
            parse(value, defaultType, defaultValue);
        }
        void parse(String value, short defaultType, float defaultValue) {
            type = defaultType;
            this.value = defaultValue;
            int length = value.length();
            if (length > 0 && value.charAt(length - 1) == '%') {
                try {
                    this.value = Float.valueOf(value.substring(0, length - 1)).
                                               floatValue() / 100.0f;
                    type = 1;
                }
                catch (NumberFormatException nfe) { }
            }
            if (length >= 2) {
                units = value.substring(length - 2, length);
                Float scale = lengthMapping.get(units);
                if (scale != null) {
                    try {
                        this.value = Float.valueOf(value.substring(0,
                               length - 2)).floatValue();
                        type = 0;
                    }
                    catch (NumberFormatException nfe) { }
                }
                else if (units.equals("em") ||
                         units.equals("ex")) {
                    try {
                        this.value = Float.valueOf(value.substring(0,
                                      length - 2)).floatValue();
                        type = 3;
                    }
                    catch (NumberFormatException nfe) { }
                }
                else if (value.equals("larger")) {
                    this.value = 2f;
                    type = 2;
                }
                else if (value.equals("smaller")) {
                    this.value = -2;
                    type = 2;
                }
                else {
                    try {
                        this.value = Float.valueOf(value).floatValue();
                        type = 0;
                    } catch (NumberFormatException nfe) {}
                }
            }
            else if (length > 0) {
                try {
                    this.value = Float.valueOf(value).floatValue();
                    type = 0;
                } catch (NumberFormatException nfe) {}
            }
        }
        float getValue(boolean w3cLengthUnits) {
            Hashtable<String, Float> mapping = (w3cLengthUnits) ? w3cLengthMapping : lengthMapping;
            float scale = 1;
            if (units != null) {
                Float scaleFloat = mapping.get(units);
                if (scaleFloat != null) {
                    scale = scaleFloat.floatValue();
                }
            }
            return this.value * scale;
        }
        static float getValue(float value, String units, Boolean w3cLengthUnits) {
            Hashtable<String, Float> mapping = (w3cLengthUnits) ? w3cLengthMapping : lengthMapping;
            float scale = 1;
            if (units != null) {
                Float scaleFloat = mapping.get(units);
                if (scaleFloat != null) {
                    scale = scaleFloat.floatValue();
                }
            }
            return value * scale;
        }
        public String toString() {
            return type + " " + value;
        }
        short type;
        float value;
        String units = null;
        static final short UNINITALIZED_LENGTH = (short)10;
    }
    static class ShorthandFontParser {
        static void parseShorthandFont(CSS css, String value,
                                       MutableAttributeSet attr) {
            String[]   strings = CSS.parseStrings(value);
            int        count = strings.length;
            int        index = 0;
            short      found = 0;
            int        maxC = Math.min(3, count);
            while (index < maxC) {
                if ((found & 1) == 0 && isFontStyle(strings[index])) {
                    css.addInternalCSSValue(attr, CSS.Attribute.FONT_STYLE,
                                            strings[index++]);
                    found |= 1;
                }
                else if ((found & 2) == 0 && isFontVariant(strings[index])) {
                    css.addInternalCSSValue(attr, CSS.Attribute.FONT_VARIANT,
                                            strings[index++]);
                    found |= 2;
                }
                else if ((found & 4) == 0 && isFontWeight(strings[index])) {
                    css.addInternalCSSValue(attr, CSS.Attribute.FONT_WEIGHT,
                                            strings[index++]);
                    found |= 4;
                }
                else if (strings[index].equals("normal")) {
                    index++;
                }
                else {
                    break;
                }
            }
            if ((found & 1) == 0) {
                css.addInternalCSSValue(attr, CSS.Attribute.FONT_STYLE,
                                        "normal");
            }
            if ((found & 2) == 0) {
                css.addInternalCSSValue(attr, CSS.Attribute.FONT_VARIANT,
                                        "normal");
            }
            if ((found & 4) == 0) {
                css.addInternalCSSValue(attr, CSS.Attribute.FONT_WEIGHT,
                                        "normal");
            }
            if (index < count) {
                String fontSize = strings[index];
                int slashIndex = fontSize.indexOf('/');
                if (slashIndex != -1) {
                    fontSize = fontSize.substring(0, slashIndex);
                    strings[index] = strings[index].substring(slashIndex);
                }
                else {
                    index++;
                }
                css.addInternalCSSValue(attr, CSS.Attribute.FONT_SIZE,
                                        fontSize);
            }
            else {
                css.addInternalCSSValue(attr, CSS.Attribute.FONT_SIZE,
                                        "medium");
            }
            if (index < count && strings[index].startsWith("/")) {
                String lineHeight = null;
                if (strings[index].equals("/")) {
                    if (++index < count) {
                        lineHeight = strings[index++];
                    }
                }
                else {
                    lineHeight = strings[index++].substring(1);
                }
                if (lineHeight != null) {
                    css.addInternalCSSValue(attr, CSS.Attribute.LINE_HEIGHT,
                                            lineHeight);
                }
                else {
                    css.addInternalCSSValue(attr, CSS.Attribute.LINE_HEIGHT,
                                            "normal");
                }
            }
            else {
                css.addInternalCSSValue(attr, CSS.Attribute.LINE_HEIGHT,
                                        "normal");
            }
            if (index < count) {
                String family = strings[index++];
                while (index < count) {
                    family += " " + strings[index++];
                }
                css.addInternalCSSValue(attr, CSS.Attribute.FONT_FAMILY,
                                        family);
            }
            else {
                css.addInternalCSSValue(attr, CSS.Attribute.FONT_FAMILY,
                                        Font.SANS_SERIF);
            }
        }
        private static boolean isFontStyle(String string) {
            return (string.equals("italic") ||
                    string.equals("oblique"));
        }
        private static boolean isFontVariant(String string) {
            return (string.equals("small-caps"));
        }
        private static boolean isFontWeight(String string) {
            if (string.equals("bold") || string.equals("bolder") ||
                string.equals("italic") || string.equals("lighter")) {
                return true;
            }
            return (string.length() == 3 &&
                    string.charAt(0) >= '1' && string.charAt(0) <= '9' &&
                    string.charAt(1) == '0' && string.charAt(2) == '0');
        }
    }
    static class ShorthandBackgroundParser {
        static void parseShorthandBackground(CSS css, String value,
                                             MutableAttributeSet attr) {
            String[] strings = parseStrings(value);
            int count = strings.length;
            int index = 0;
            short found = 0;
            while (index < count) {
                String string = strings[index++];
                if ((found & 1) == 0 && isImage(string)) {
                    css.addInternalCSSValue(attr, CSS.Attribute.
                                            BACKGROUND_IMAGE, string);
                    found |= 1;
                }
                else if ((found & 2) == 0 && isRepeat(string)) {
                    css.addInternalCSSValue(attr, CSS.Attribute.
                                            BACKGROUND_REPEAT, string);
                    found |= 2;
                }
                else if ((found & 4) == 0 && isAttachment(string)) {
                    css.addInternalCSSValue(attr, CSS.Attribute.
                                            BACKGROUND_ATTACHMENT, string);
                    found |= 4;
                }
                else if ((found & 8) == 0 && isPosition(string)) {
                    if (index < count && isPosition(strings[index])) {
                        css.addInternalCSSValue(attr, CSS.Attribute.
                                                BACKGROUND_POSITION,
                                                string + " " +
                                                strings[index++]);
                    }
                    else {
                        css.addInternalCSSValue(attr, CSS.Attribute.
                                                BACKGROUND_POSITION, string);
                    }
                    found |= 8;
                }
                else if ((found & 16) == 0 && isColor(string)) {
                    css.addInternalCSSValue(attr, CSS.Attribute.
                                            BACKGROUND_COLOR, string);
                    found |= 16;
                }
            }
            if ((found & 1) == 0) {
                css.addInternalCSSValue(attr, CSS.Attribute.BACKGROUND_IMAGE,
                                        null);
            }
            if ((found & 2) == 0) {
                css.addInternalCSSValue(attr, CSS.Attribute.BACKGROUND_REPEAT,
                                        "repeat");
            }
            if ((found & 4) == 0) {
                css.addInternalCSSValue(attr, CSS.Attribute.
                                        BACKGROUND_ATTACHMENT, "scroll");
            }
            if ((found & 8) == 0) {
                css.addInternalCSSValue(attr, CSS.Attribute.
                                        BACKGROUND_POSITION, null);
            }
        }
        static boolean isImage(String string) {
            return (string.startsWith("url(") && string.endsWith(")"));
        }
        static boolean isRepeat(String string) {
            return (string.equals("repeat-x") || string.equals("repeat-y") ||
                    string.equals("repeat") || string.equals("no-repeat"));
        }
        static boolean isAttachment(String string) {
            return (string.equals("fixed") || string.equals("scroll"));
        }
        static boolean isPosition(String string) {
            return (string.equals("top") || string.equals("bottom") ||
                    string.equals("left") || string.equals("right") ||
                    string.equals("center") ||
                    (string.length() > 0 &&
                     Character.isDigit(string.charAt(0))));
        }
        static boolean isColor(String string) {
            return (CSS.stringToColor(string) != null);
        }
    }
    static class ShorthandMarginParser {
        static void parseShorthandMargin(CSS css, String value,
                                         MutableAttributeSet attr,
                                         CSS.Attribute[] names) {
            String[] strings = parseStrings(value);
            int count = strings.length;
            int index = 0;
            switch (count) {
            case 0:
                return;
            case 1:
                for (int counter = 0; counter < 4; counter++) {
                    css.addInternalCSSValue(attr, names[counter], strings[0]);
                }
                break;
            case 2:
                css.addInternalCSSValue(attr, names[0], strings[0]);
                css.addInternalCSSValue(attr, names[2], strings[0]);
                css.addInternalCSSValue(attr, names[1], strings[1]);
                css.addInternalCSSValue(attr, names[3], strings[1]);
                break;
            case 3:
                css.addInternalCSSValue(attr, names[0], strings[0]);
                css.addInternalCSSValue(attr, names[1], strings[1]);
                css.addInternalCSSValue(attr, names[2], strings[2]);
                css.addInternalCSSValue(attr, names[3], strings[1]);
                break;
            default:
                for (int counter = 0; counter < 4; counter++) {
                    css.addInternalCSSValue(attr, names[counter],
                                            strings[counter]);
                }
                break;
            }
        }
    }
    static class ShorthandBorderParser {
        static Attribute[] keys = {
            Attribute.BORDER_TOP, Attribute.BORDER_RIGHT,
            Attribute.BORDER_BOTTOM, Attribute.BORDER_LEFT,
        };
        static void parseShorthandBorder(MutableAttributeSet attributes,
                                            CSS.Attribute key, String value) {
            Object[] parts = new Object[CSSBorder.PARSERS.length];
            String[] strings = parseStrings(value);
            for (String s : strings) {
                boolean valid = false;
                for (int i = 0; i < parts.length; i++) {
                    Object v = CSSBorder.PARSERS[i].parseCssValue(s);
                    if (v != null) {
                        if (parts[i] == null) {
                            parts[i] = v;
                            valid = true;
                        }
                        break;
                    }
                }
                if (!valid) {
                    return;
                }
            }
            for (int i = 0; i < parts.length; i++) {
                if (parts[i] == null) {
                    parts[i] = CSSBorder.DEFAULTS[i];
                }
            }
            for (int i = 0; i < keys.length; i++) {
                if ((key == Attribute.BORDER) || (key == keys[i])) {
                    for (int k = 0; k < parts.length; k++) {
                        attributes.addAttribute(
                                        CSSBorder.ATTRIBUTES[k][i], parts[k]);
                    }
                }
            }
        }
    }
    static SizeRequirements calculateTiledRequirements(LayoutIterator iter, SizeRequirements r) {
        long minimum = 0;
        long maximum = 0;
        long preferred = 0;
        int lastMargin = 0;
        int totalSpacing = 0;
        int n = iter.getCount();
        for (int i = 0; i < n; i++) {
            iter.setIndex(i);
            int margin0 = lastMargin;
            int margin1 = (int) iter.getLeadingCollapseSpan();
            totalSpacing += Math.max(margin0, margin1);
            preferred += (int) iter.getPreferredSpan(0);
            minimum += iter.getMinimumSpan(0);
            maximum += iter.getMaximumSpan(0);
            lastMargin = (int) iter.getTrailingCollapseSpan();
        }
        totalSpacing += lastMargin;
        totalSpacing += 2 * iter.getBorderWidth();
        minimum += totalSpacing;
        preferred += totalSpacing;
        maximum += totalSpacing;
        if (r == null) {
            r = new SizeRequirements();
        }
        r.minimum = (minimum > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int)minimum;
        r.preferred = (preferred > Integer.MAX_VALUE) ? Integer.MAX_VALUE :(int) preferred;
        r.maximum = (maximum > Integer.MAX_VALUE) ? Integer.MAX_VALUE :(int) maximum;
        return r;
    }
    static void calculateTiledLayout(LayoutIterator iter, int targetSpan) {
        long preferred = 0;
        long currentPreferred;
        int lastMargin = 0;
        int totalSpacing = 0;
        int n = iter.getCount();
        int adjustmentWeightsCount = LayoutIterator.WorstAdjustmentWeight + 1;
        long gain[] = new long[adjustmentWeightsCount];
        long loss[] = new long[adjustmentWeightsCount];
        for (int i = 0; i < adjustmentWeightsCount; i++) {
            gain[i] = loss[i] = 0;
        }
        for (int i = 0; i < n; i++) {
            iter.setIndex(i);
            int margin0 = lastMargin;
            int margin1 = (int) iter.getLeadingCollapseSpan();
            iter.setOffset(Math.max(margin0, margin1));
            totalSpacing += iter.getOffset();
            currentPreferred = (long)iter.getPreferredSpan(targetSpan);
            iter.setSpan((int) currentPreferred);
            preferred += currentPreferred;
            gain[iter.getAdjustmentWeight()] +=
                (long)iter.getMaximumSpan(targetSpan) - currentPreferred;
            loss[iter.getAdjustmentWeight()] +=
                currentPreferred - (long)iter.getMinimumSpan(targetSpan);
            lastMargin = (int) iter.getTrailingCollapseSpan();
        }
        totalSpacing += lastMargin;
        totalSpacing += 2 * iter.getBorderWidth();
        for (int i = 1; i < adjustmentWeightsCount; i++) {
            gain[i] += gain[i - 1];
            loss[i] += loss[i - 1];
        }
        int allocated = targetSpan - totalSpacing;
        long desiredAdjustment = allocated - preferred;
        long adjustmentsArray[] = (desiredAdjustment > 0) ? gain : loss;
        desiredAdjustment = Math.abs(desiredAdjustment);
        int adjustmentLevel = 0;
        for (;adjustmentLevel <= LayoutIterator.WorstAdjustmentWeight;
             adjustmentLevel++) {
            if (adjustmentsArray[adjustmentLevel] >= desiredAdjustment) {
                break;
            }
        }
        float adjustmentFactor = 0.0f;
        if (adjustmentLevel <= LayoutIterator.WorstAdjustmentWeight) {
            desiredAdjustment -= (adjustmentLevel > 0) ?
                adjustmentsArray[adjustmentLevel - 1] : 0;
            if (desiredAdjustment != 0) {
                float maximumAdjustment =
                    adjustmentsArray[adjustmentLevel] -
                    ((adjustmentLevel > 0) ?
                     adjustmentsArray[adjustmentLevel - 1] : 0
                     );
                adjustmentFactor = desiredAdjustment / maximumAdjustment;
            }
        }
        int totalOffset = (int)iter.getBorderWidth();
        for (int i = 0; i < n; i++) {
            iter.setIndex(i);
            iter.setOffset( iter.getOffset() + totalOffset);
            if (iter.getAdjustmentWeight() < adjustmentLevel) {
                iter.setSpan((int)
                             ((allocated > preferred) ?
                              Math.floor(iter.getMaximumSpan(targetSpan)) :
                              Math.ceil(iter.getMinimumSpan(targetSpan))
                              )
                             );
            } else if (iter.getAdjustmentWeight() == adjustmentLevel) {
                int availableSpan = (allocated > preferred) ?
                    (int) iter.getMaximumSpan(targetSpan) - iter.getSpan() :
                    iter.getSpan() - (int) iter.getMinimumSpan(targetSpan);
                int adj = (int)Math.floor(adjustmentFactor * availableSpan);
                iter.setSpan(iter.getSpan() +
                             ((allocated > preferred) ? adj : -adj));
            }
            totalOffset = (int) Math.min((long) iter.getOffset() +
                                         (long) iter.getSpan(),
                                         Integer.MAX_VALUE);
        }
        int roundError = targetSpan - totalOffset -
            (int)iter.getTrailingCollapseSpan() -
            (int)iter.getBorderWidth();
        int adj = (roundError > 0) ? 1 : -1;
        roundError *= adj;
        boolean canAdjust = true;
        while (roundError > 0 && canAdjust) {
            canAdjust = false;
            int offsetAdjust = 0;
            for (int i = 0; i < n; i++) {
                iter.setIndex(i);
                iter.setOffset(iter.getOffset() + offsetAdjust);
                int curSpan = iter.getSpan();
                if (roundError > 0) {
                    int boundGap = (adj > 0) ?
                        (int)Math.floor(iter.getMaximumSpan(targetSpan)) - curSpan :
                        curSpan - (int)Math.ceil(iter.getMinimumSpan(targetSpan));
                    if (boundGap >= 1) {
                        canAdjust = true;
                        iter.setSpan(curSpan + adj);
                        offsetAdjust += adj;
                        roundError--;
                    }
                }
            }
        }
    }
    interface LayoutIterator {
        void setOffset(int offs);
        int getOffset();
        void setSpan(int span);
        int getSpan();
        int getCount();
        void setIndex(int i);
        float getMinimumSpan(float parentSpan);
        float getPreferredSpan(float parentSpan);
        float getMaximumSpan(float parentSpan);
        int getAdjustmentWeight(); 
        float getBorderWidth();
        float getLeadingCollapseSpan();
        float getTrailingCollapseSpan();
        public static final int WorstAdjustmentWeight = 2;
    }
    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
        s.defaultWriteObject();
        Enumeration keys = valueConvertor.keys();
        s.writeInt(valueConvertor.size());
        if (keys != null) {
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = valueConvertor.get(key);
                if (!(key instanceof Serializable) &&
                    (key = StyleContext.getStaticAttributeKey(key)) == null) {
                    key = null;
                    value = null;
                }
                else if (!(value instanceof Serializable) &&
                    (value = StyleContext.getStaticAttributeKey(value)) == null){
                    key = null;
                    value = null;
                }
                s.writeObject(key);
                s.writeObject(value);
            }
        }
    }
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException
    {
        s.defaultReadObject();
        int numValues = s.readInt();
        valueConvertor = new Hashtable<Object, Object>(Math.max(1, numValues));
        while (numValues-- > 0) {
            Object key = s.readObject();
            Object value = s.readObject();
            Object staticKey = StyleContext.getStaticAttribute(key);
            if (staticKey != null) {
                key = staticKey;
            }
            Object staticValue = StyleContext.getStaticAttribute(value);
            if (staticValue != null) {
                value = staticValue;
            }
            if (key != null && value != null) {
                valueConvertor.put(key, value);
            }
        }
    }
    private StyleSheet getStyleSheet(StyleSheet ss) {
        if (ss != null) {
            styleSheet = ss;
        }
        return styleSheet;
    }
    private transient Hashtable<Object, Object> valueConvertor;
    private int baseFontSize;
    private transient StyleSheet styleSheet = null;
    static int baseFontSizeIndex = 3;
}
