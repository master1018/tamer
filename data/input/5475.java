class PangoFonts {
    public static final String CHARS_DIGITS = "0123456789";
    private static double fontScale;
    static {
        fontScale = 1.0d;
        GraphicsEnvironment ge =
           GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (!ge.isHeadless()) {
            GraphicsConfiguration gc =
                ge.getDefaultScreenDevice().getDefaultConfiguration();
            AffineTransform at = gc.getNormalizingTransform();
            fontScale = at.getScaleY();
        }
    }
    static Font lookupFont(String pangoName) {
        String family = "";
        int style = Font.PLAIN;
        int size = 10;
        StringTokenizer tok = new StringTokenizer(pangoName);
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();
            if (word.equalsIgnoreCase("italic")) {
                style |= Font.ITALIC;
            } else if (word.equalsIgnoreCase("bold")) {
                style |= Font.BOLD;
            } else if (CHARS_DIGITS.indexOf(word.charAt(0)) != -1) {
                try {
                    size = Integer.parseInt(word);
                } catch (NumberFormatException ex) {
                }
            } else {
                if (family.length() > 0) {
                    family += " ";
                }
                family += word;
            }
        }
        double dsize = size;
        int dpi = 96;
        Object value =
            Toolkit.getDefaultToolkit().getDesktopProperty("gnome.Xft/DPI");
        if (value instanceof Integer) {
            dpi = ((Integer)value).intValue() / 1024;
            if (dpi == -1) {
              dpi = 96;
            }
            if (dpi < 50) { 
                dpi = 50;
            }
            dsize = ((double)(dpi * size)/ 72.0);
        } else {
            dsize = size * fontScale;
        }
        size = (int)(dsize + 0.5);
        if (size < 1) {
            size = 1;
        }
        String fcFamilyLC = family.toLowerCase();
        if (FontUtilities.mapFcName(fcFamilyLC) != null) {
            Font font =  FontUtilities.getFontConfigFUIR(fcFamilyLC, style, size);
            font = font.deriveFont(style, (float)dsize);
            return new FontUIResource(font);
        } else {
            Font font = new Font(family, style, size);
            font = font.deriveFont(style, (float)dsize);
            FontUIResource fuir = new FontUIResource(font);
            return FontUtilities.getCompositeFontUIResource(fuir);
        }
    }
    static int getFontSize(String pangoName) {
        int size = 10;
        StringTokenizer tok = new StringTokenizer(pangoName);
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();
            if (CHARS_DIGITS.indexOf(word.charAt(0)) != -1) {
                try {
                    size = Integer.parseInt(word);
                } catch (NumberFormatException ex) {
                }
            }
        }
        return size;
    }
}
