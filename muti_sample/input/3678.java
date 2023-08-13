public class SwingUtilities2 {
    public static final Object LAF_STATE_KEY =
            new StringBuffer("LookAndFeel State");
     private static LSBCacheEntry[] fontCache;
     private static final int CACHE_SIZE = 6;
     private static int nextIndex;
     private static LSBCacheEntry searchKey;
     private static final int MIN_CHAR_INDEX = (int)'W';
     private static final int MAX_CHAR_INDEX = (int)'W' + 1;
    public static final FontRenderContext DEFAULT_FRC =
        new FontRenderContext(null, false, false);
    public static final Object AA_TEXT_PROPERTY_KEY =
                          new StringBuffer("AATextInfoPropertyKey");
    public static final String IMPLIED_CR = "CR";
    private static final StringBuilder SKIP_CLICK_COUNT =
        new StringBuilder("skipClickCount");
    public static class AATextInfo {
        private static AATextInfo getAATextInfoFromMap(Map hints) {
            Object aaHint   = hints.get(KEY_TEXT_ANTIALIASING);
            Object contHint = hints.get(KEY_TEXT_LCD_CONTRAST);
            if (aaHint == null ||
                aaHint == VALUE_TEXT_ANTIALIAS_OFF ||
                aaHint == VALUE_TEXT_ANTIALIAS_DEFAULT) {
                return null;
            } else {
                return new AATextInfo(aaHint, (Integer)contHint);
            }
        }
        public static AATextInfo getAATextInfo(boolean lafCondition) {
            SunToolkit.setAAFontSettingsCondition(lafCondition);
            Toolkit tk = Toolkit.getDefaultToolkit();
            Object map = tk.getDesktopProperty(SunToolkit.DESKTOPFONTHINTS);
            if (map instanceof Map) {
                return getAATextInfoFromMap((Map)map);
            } else {
                return null;
            }
        }
        Object aaHint;
        Integer lcdContrastHint;
        FontRenderContext frc;
        public AATextInfo(Object aaHint, Integer lcdContrastHint) {
            if (aaHint == null) {
                throw new InternalError("null not allowed here");
            }
            if (aaHint == VALUE_TEXT_ANTIALIAS_OFF ||
                aaHint == VALUE_TEXT_ANTIALIAS_DEFAULT) {
                throw new InternalError("AA must be on");
            }
            this.aaHint = aaHint;
            this.lcdContrastHint = lcdContrastHint;
            this.frc = new FontRenderContext(null, aaHint,
                                             VALUE_FRACTIONALMETRICS_DEFAULT);
        }
    }
    public static final Object COMPONENT_UI_PROPERTY_KEY =
                            new StringBuffer("ComponentUIPropertyKey");
    public static final StringUIClientPropertyKey BASICMENUITEMUI_MAX_TEXT_OFFSET =
        new StringUIClientPropertyKey ("maxTextOffset");
    private static Field inputEvent_CanAccessSystemClipboard_Field = null;
    private static final String UntrustedClipboardAccess =
        "UNTRUSTED_CLIPBOARD_ACCESS_KEY";
    private static final int CHAR_BUFFER_SIZE = 100;
    private static final Object charsBufferLock = new Object();
    private static char[] charsBuffer = new char[CHAR_BUFFER_SIZE];
    static {
        fontCache = new LSBCacheEntry[CACHE_SIZE];
    }
    private static int syncCharsBuffer(String s) {
        int length = s.length();
        if ((charsBuffer == null) || (charsBuffer.length < length)) {
            charsBuffer = s.toCharArray();
        } else {
            s.getChars(0, length, charsBuffer, 0);
        }
        return length;
    }
    public static final boolean isComplexLayout(char[] text, int start, int limit) {
        return FontUtilities.isComplexText(text, start, limit);
    }
    public static AATextInfo drawTextAntialiased(JComponent c) {
        if (c != null) {
            return (AATextInfo)c.getClientProperty(AA_TEXT_PROPERTY_KEY);
        }
        return null;
    }
    public static int getLeftSideBearing(JComponent c, FontMetrics fm,
                                         String string) {
        if ((string == null) || (string.length() == 0)) {
            return 0;
        }
        return getLeftSideBearing(c, fm, string.charAt(0));
    }
    public static int getLeftSideBearing(JComponent c, FontMetrics fm,
                                         char firstChar) {
        int charIndex = (int) firstChar;
        if (charIndex < MAX_CHAR_INDEX && charIndex >= MIN_CHAR_INDEX) {
            byte[] lsbs = null;
            FontRenderContext frc = getFontRenderContext(c, fm);
            Font font = fm.getFont();
            synchronized (SwingUtilities2.class) {
                LSBCacheEntry entry = null;
                if (searchKey == null) {
                    searchKey = new LSBCacheEntry(frc, font);
                } else {
                    searchKey.reset(frc, font);
                }
                for (LSBCacheEntry cacheEntry : fontCache) {
                    if (searchKey.equals(cacheEntry)) {
                        entry = cacheEntry;
                        break;
                    }
                }
                if (entry == null) {
                    entry = searchKey;
                    fontCache[nextIndex] = searchKey;
                    searchKey = null;
                    nextIndex = (nextIndex + 1) % CACHE_SIZE;
                }
                return entry.getLeftSideBearing(firstChar);
            }
        }
        return 0;
    }
    public static FontMetrics getFontMetrics(JComponent c, Graphics g) {
        return getFontMetrics(c, g, g.getFont());
    }
    public static FontMetrics getFontMetrics(JComponent c, Graphics g,
                                             Font font) {
        if (c != null) {
            return c.getFontMetrics(font);
        }
        return Toolkit.getDefaultToolkit().getFontMetrics(font);
    }
    public static int stringWidth(JComponent c, FontMetrics fm, String string){
        if (string == null || string.equals("")) {
            return 0;
        }
        boolean needsTextLayout = ((c != null) &&
                (c.getClientProperty(TextAttribute.NUMERIC_SHAPING) != null));
        if (needsTextLayout) {
            synchronized(charsBufferLock) {
                int length = syncCharsBuffer(string);
                needsTextLayout = isComplexLayout(charsBuffer, 0, length);
            }
        }
        if (needsTextLayout) {
            TextLayout layout = createTextLayout(c, string,
                                    fm.getFont(), fm.getFontRenderContext());
            return (int) layout.getAdvance();
        } else {
            return fm.stringWidth(string);
        }
    }
    public static String clipStringIfNecessary(JComponent c, FontMetrics fm,
                                               String string,
                                               int availTextWidth) {
        if ((string == null) || (string.equals("")))  {
            return "";
        }
        int textWidth = SwingUtilities2.stringWidth(c, fm, string);
        if (textWidth > availTextWidth) {
            return SwingUtilities2.clipString(c, fm, string, availTextWidth);
        }
        return string;
    }
    public static String clipString(JComponent c, FontMetrics fm,
                                    String string, int availTextWidth) {
        String clipString = "...";
        availTextWidth -= SwingUtilities2.stringWidth(c, fm, clipString);
        if (availTextWidth <= 0) {
            return clipString;
        }
        boolean needsTextLayout;
        synchronized (charsBufferLock) {
            int stringLength = syncCharsBuffer(string);
            needsTextLayout =
                isComplexLayout(charsBuffer, 0, stringLength);
            if (!needsTextLayout) {
                int width = 0;
                for (int nChars = 0; nChars < stringLength; nChars++) {
                    width += fm.charWidth(charsBuffer[nChars]);
                    if (width > availTextWidth) {
                        string = string.substring(0, nChars);
                        break;
                    }
                }
            }
        }
        if (needsTextLayout) {
            FontRenderContext frc = getFontRenderContext(c, fm);
            AttributedString aString = new AttributedString(string);
            if (c != null) {
                aString.addAttribute(TextAttribute.NUMERIC_SHAPING,
                        c.getClientProperty(TextAttribute.NUMERIC_SHAPING));
            }
            LineBreakMeasurer measurer =
                new LineBreakMeasurer(aString.getIterator(), frc);
            int nChars = measurer.nextOffset(availTextWidth);
            string = string.substring(0, nChars);
        }
        return string + clipString;
    }
    public static void drawString(JComponent c, Graphics g, String text,
                                  int x, int y) {
        if ( text == null || text.length() <= 0 ) { 
            return;
        }
        if (isPrinting(g)) {
            Graphics2D g2d = getGraphics2D(g);
            if (g2d != null) {
                float screenWidth = (float)
                   g2d.getFont().getStringBounds(text, DEFAULT_FRC).getWidth();
                TextLayout layout = createTextLayout(c, text, g2d.getFont(),
                                                   g2d.getFontRenderContext());
                layout = layout.getJustifiedLayout(screenWidth);
                Color col = g2d.getColor();
                if (col instanceof PrintColorUIResource) {
                    g2d.setColor(((PrintColorUIResource)col).getPrintColor());
                }
                layout.draw(g2d, x, y);
                g2d.setColor(col);
                return;
            }
        }
        AATextInfo info = drawTextAntialiased(c);
        if (info != null && (g instanceof Graphics2D)) {
            Graphics2D g2 = (Graphics2D)g;
            Object oldContrast = null;
            Object oldAAValue = g2.getRenderingHint(KEY_TEXT_ANTIALIASING);
            if (info.aaHint != oldAAValue) {
                g2.setRenderingHint(KEY_TEXT_ANTIALIASING, info.aaHint);
            } else {
                oldAAValue = null;
            }
            if (info.lcdContrastHint != null) {
                oldContrast = g2.getRenderingHint(KEY_TEXT_LCD_CONTRAST);
                if (info.lcdContrastHint.equals(oldContrast)) {
                    oldContrast = null;
                } else {
                    g2.setRenderingHint(KEY_TEXT_LCD_CONTRAST,
                                        info.lcdContrastHint);
                }
            }
            boolean needsTextLayout = ((c != null) &&
                (c.getClientProperty(TextAttribute.NUMERIC_SHAPING) != null));
            if (needsTextLayout) {
                synchronized(charsBufferLock) {
                    int length = syncCharsBuffer(text);
                    needsTextLayout = isComplexLayout(charsBuffer, 0, length);
                }
            }
            if (needsTextLayout) {
                TextLayout layout = createTextLayout(c, text, g2.getFont(),
                                                    g2.getFontRenderContext());
                layout.draw(g2, x, y);
            } else {
                g.drawString(text, x, y);
            }
            if (oldAAValue != null) {
                g2.setRenderingHint(KEY_TEXT_ANTIALIASING, oldAAValue);
            }
            if (oldContrast != null) {
                g2.setRenderingHint(KEY_TEXT_LCD_CONTRAST, oldContrast);
            }
        }
        else {
            g.drawString(text, x, y);
        }
    }
    public static void drawStringUnderlineCharAt(JComponent c,Graphics g,
                           String text, int underlinedIndex, int x,int y) {
        if (text == null || text.length() <= 0) {
            return;
        }
        SwingUtilities2.drawString(c, g, text, x, y);
        int textLength = text.length();
        if (underlinedIndex >= 0 && underlinedIndex < textLength ) {
            int underlineRectY = y;
            int underlineRectHeight = 1;
            int underlineRectX = 0;
            int underlineRectWidth = 0;
            boolean isPrinting = isPrinting(g);
            boolean needsTextLayout = isPrinting;
            if (!needsTextLayout) {
                synchronized (charsBufferLock) {
                    syncCharsBuffer(text);
                    needsTextLayout =
                        isComplexLayout(charsBuffer, 0, textLength);
                }
            }
            if (!needsTextLayout) {
                FontMetrics fm = g.getFontMetrics();
                underlineRectX = x +
                    SwingUtilities2.stringWidth(c,fm,
                                        text.substring(0,underlinedIndex));
                underlineRectWidth = fm.charWidth(text.
                                                  charAt(underlinedIndex));
            } else {
                Graphics2D g2d = getGraphics2D(g);
                if (g2d != null) {
                    TextLayout layout =
                        createTextLayout(c, text, g2d.getFont(),
                                       g2d.getFontRenderContext());
                    if (isPrinting) {
                        float screenWidth = (float)g2d.getFont().
                            getStringBounds(text, DEFAULT_FRC).getWidth();
                        layout = layout.getJustifiedLayout(screenWidth);
                    }
                    TextHitInfo leading =
                        TextHitInfo.leading(underlinedIndex);
                    TextHitInfo trailing =
                        TextHitInfo.trailing(underlinedIndex);
                    Shape shape =
                        layout.getVisualHighlightShape(leading, trailing);
                    Rectangle rect = shape.getBounds();
                    underlineRectX = x + rect.x;
                    underlineRectWidth = rect.width;
                }
            }
            g.fillRect(underlineRectX, underlineRectY + 1,
                       underlineRectWidth, underlineRectHeight);
        }
    }
    public static int loc2IndexFileList(JList list, Point point) {
        int index = list.locationToIndex(point);
        if (index != -1) {
            Object bySize = list.getClientProperty("List.isFileList");
            if (bySize instanceof Boolean && ((Boolean)bySize).booleanValue() &&
                !pointIsInActualBounds(list, index, point)) {
                index = -1;
            }
        }
        return index;
    }
    private static boolean pointIsInActualBounds(JList list, int index,
                                                Point point) {
        ListCellRenderer renderer = list.getCellRenderer();
        ListModel dataModel = list.getModel();
        Object value = dataModel.getElementAt(index);
        Component item = renderer.getListCellRendererComponent(list,
                          value, index, false, false);
        Dimension itemSize = item.getPreferredSize();
        Rectangle cellBounds = list.getCellBounds(index, index);
        if (!item.getComponentOrientation().isLeftToRight()) {
            cellBounds.x += (cellBounds.width - itemSize.width);
        }
        cellBounds.width = itemSize.width;
        return cellBounds.contains(point);
    }
    public static boolean pointOutsidePrefSize(JTable table, int row, int column, Point p) {
        if (table.convertColumnIndexToModel(column) != 0 || row == -1) {
            return true;
        }
        TableCellRenderer tcr = table.getCellRenderer(row, column);
        Object value = table.getValueAt(row, column);
        Component cell = tcr.getTableCellRendererComponent(table, value, false,
                false, row, column);
        Dimension itemSize = cell.getPreferredSize();
        Rectangle cellBounds = table.getCellRect(row, column, false);
        cellBounds.width = itemSize.width;
        cellBounds.height = itemSize.height;
        assert (p.x >= cellBounds.x && p.y >= cellBounds.y);
        return p.x > cellBounds.x + cellBounds.width ||
                p.y > cellBounds.y + cellBounds.height;
    }
    public static void setLeadAnchorWithoutSelection(ListSelectionModel model,
                                                     int lead, int anchor) {
        if (anchor == -1) {
            anchor = lead;
        }
        if (lead == -1) {
            model.setAnchorSelectionIndex(-1);
            model.setLeadSelectionIndex(-1);
        } else {
            if (model.isSelectedIndex(lead)) {
                model.addSelectionInterval(lead, lead);
            } else {
                model.removeSelectionInterval(lead, lead);
            }
            model.setAnchorSelectionIndex(anchor);
        }
    }
    public static boolean shouldIgnore(MouseEvent me, JComponent c) {
        return c == null || !c.isEnabled()
                         || !SwingUtilities.isLeftMouseButton(me)
                         || me.isConsumed();
    }
    public static void adjustFocus(JComponent c) {
        if (!c.hasFocus() && c.isRequestFocusEnabled()) {
            c.requestFocus();
        }
    }
    public static int drawChars(JComponent c, Graphics g,
                                 char[] data,
                                 int offset,
                                 int length,
                                 int x,
                                 int y) {
        if ( length <= 0 ) { 
            return x;
        }
        int nextX = x + getFontMetrics(c, g).charsWidth(data, offset, length);
        if (isPrinting(g)) {
            Graphics2D g2d = getGraphics2D(g);
            if (g2d != null) {
                FontRenderContext deviceFontRenderContext = g2d.
                    getFontRenderContext();
                FontRenderContext frc = getFontRenderContext(c);
                if (frc != null &&
                    !isFontRenderContextPrintCompatible
                    (deviceFontRenderContext, frc)) {
                    TextLayout layout =
                        createTextLayout(c, new String(data, offset, length),
                                       g2d.getFont(),
                                       deviceFontRenderContext);
                    float screenWidth = (float)g2d.getFont().
                        getStringBounds(data, offset, offset + length, frc).
                        getWidth();
                    layout = layout.getJustifiedLayout(screenWidth);
                    Color col = g2d.getColor();
                    if (col instanceof PrintColorUIResource) {
                        g2d.setColor(((PrintColorUIResource)col).getPrintColor());
                    }
                    layout.draw(g2d,x,y);
                    g2d.setColor(col);
                    return nextX;
                }
            }
        }
        AATextInfo info = drawTextAntialiased(c);
        if (info != null && (g instanceof Graphics2D)) {
            Graphics2D g2 = (Graphics2D)g;
            Object oldContrast = null;
            Object oldAAValue = g2.getRenderingHint(KEY_TEXT_ANTIALIASING);
            if (info.aaHint != null && info.aaHint != oldAAValue) {
                g2.setRenderingHint(KEY_TEXT_ANTIALIASING, info.aaHint);
            } else {
                oldAAValue = null;
            }
            if (info.lcdContrastHint != null) {
                oldContrast = g2.getRenderingHint(KEY_TEXT_LCD_CONTRAST);
                if (info.lcdContrastHint.equals(oldContrast)) {
                    oldContrast = null;
                } else {
                    g2.setRenderingHint(KEY_TEXT_LCD_CONTRAST,
                                        info.lcdContrastHint);
                }
            }
            g.drawChars(data, offset, length, x, y);
            if (oldAAValue != null) {
                g2.setRenderingHint(KEY_TEXT_ANTIALIASING, oldAAValue);
            }
            if (oldContrast != null) {
                g2.setRenderingHint(KEY_TEXT_LCD_CONTRAST, oldContrast);
            }
        }
        else {
            g.drawChars(data, offset, length, x, y);
        }
        return nextX;
    }
    public static float drawString(JComponent c, Graphics g,
                                   AttributedCharacterIterator iterator,
                                   int x,
                                   int y) {
        float retVal;
        boolean isPrinting = isPrinting(g);
        Color col = g.getColor();
        if (isPrinting) {
            if (col instanceof PrintColorUIResource) {
                g.setColor(((PrintColorUIResource)col).getPrintColor());
            }
        }
        Graphics2D g2d = getGraphics2D(g);
        if (g2d == null) {
            g.drawString(iterator,x,y); 
            retVal = x;
        } else {
            FontRenderContext frc;
            if (isPrinting) {
                frc = getFontRenderContext(c);
                if (frc.isAntiAliased() || frc.usesFractionalMetrics()) {
                    frc = new FontRenderContext(frc.getTransform(), false, false);
                }
            } else if ((frc = getFRCProperty(c)) != null) {
            } else {
                frc = g2d.getFontRenderContext();
            }
            TextLayout layout = new TextLayout(iterator, frc);
            if (isPrinting) {
                FontRenderContext deviceFRC = g2d.getFontRenderContext();
                if (!isFontRenderContextPrintCompatible(frc, deviceFRC)) {
                    float screenWidth = layout.getAdvance();
                    layout = new TextLayout(iterator, deviceFRC);
                    layout = layout.getJustifiedLayout(screenWidth);
                }
            }
            layout.draw(g2d, x, y);
            retVal = layout.getAdvance();
        }
        if (isPrinting) {
            g.setColor(col);
        }
        return retVal;
    }
    private static TextLayout createTextLayout(JComponent c, String s,
                                            Font f, FontRenderContext frc) {
        Object shaper = (c == null ?
                    null : c.getClientProperty(TextAttribute.NUMERIC_SHAPING));
        if (shaper == null) {
            return new TextLayout(s, f, frc);
        } else {
            Map<TextAttribute, Object> a = new HashMap<TextAttribute, Object>();
            a.put(TextAttribute.FONT, f);
            a.put(TextAttribute.NUMERIC_SHAPING, shaper);
            return new TextLayout(s, a, frc);
        }
    }
    private static boolean
        isFontRenderContextPrintCompatible(FontRenderContext frc1,
                                           FontRenderContext frc2) {
        if (frc1 == frc2) {
            return true;
        }
        if (frc1 == null || frc2 == null) { 
            return false;
        }
        if (frc1.getFractionalMetricsHint() !=
            frc2.getFractionalMetricsHint()) {
            return false;
        }
        if (!frc1.isTransformed() && !frc2.isTransformed()) {
            return true;
        }
        double[] mat1 = new double[4];
        double[] mat2 = new double[4];
        frc1.getTransform().getMatrix(mat1);
        frc2.getTransform().getMatrix(mat2);
        return
            mat1[0] == mat2[0] &&
            mat1[1] == mat2[1] &&
            mat1[2] == mat2[2] &&
            mat1[3] == mat2[3];
    }
    public static Graphics2D getGraphics2D(Graphics g) {
        if (g instanceof Graphics2D) {
            return (Graphics2D) g;
        } else if (g instanceof ProxyPrintGraphics) {
            return (Graphics2D)(((ProxyPrintGraphics)g).getGraphics());
        } else {
            return null;
        }
    }
    public static FontRenderContext getFontRenderContext(Component c) {
        assert c != null;
        if (c == null) {
            return DEFAULT_FRC;
        } else {
            return c.getFontMetrics(c.getFont()).getFontRenderContext();
        }
    }
    private static FontRenderContext getFontRenderContext(Component c, FontMetrics fm) {
        assert fm != null || c!= null;
        return (fm != null) ? fm.getFontRenderContext()
            : getFontRenderContext(c);
    }
    public static FontMetrics getFontMetrics(JComponent c, Font font) {
        FontRenderContext  frc = getFRCProperty(c);
        if (frc == null) {
            frc = DEFAULT_FRC;
        }
        return FontDesignMetrics.getMetrics(font, frc);
    }
    private static FontRenderContext getFRCProperty(JComponent c) {
        if (c != null) {
            AATextInfo info =
                (AATextInfo)c.getClientProperty(AA_TEXT_PROPERTY_KEY);
            if (info != null) {
                return info.frc;
            }
        }
        return null;
    }
    static boolean isPrinting(Graphics g) {
        return (g instanceof PrinterGraphics || g instanceof PrintGraphics);
    }
    public static boolean useSelectedTextColor(Highlighter.Highlight h, JTextComponent c) {
        Highlighter.HighlightPainter painter = h.getPainter();
        String painterClass = painter.getClass().getName();
        if (painterClass.indexOf("javax.swing.text.DefaultHighlighter") != 0 &&
                painterClass.indexOf("com.sun.java.swing.plaf.windows.WindowsTextUI") != 0) {
            return false;
        }
        try {
            DefaultHighlighter.DefaultHighlightPainter defPainter =
                    (DefaultHighlighter.DefaultHighlightPainter) painter;
            if (defPainter.getColor() != null &&
                    !defPainter.getColor().equals(c.getSelectionColor())) {
                return false;
            }
        } catch (ClassCastException e) {
            return false;
        }
        return true;
    }
    private static class LSBCacheEntry {
        private static final byte UNSET = Byte.MAX_VALUE;
        private static final char[] oneChar = new char[1];
        private byte[] lsbCache;
        private Font font;
        private FontRenderContext frc;
        public LSBCacheEntry(FontRenderContext frc, Font font) {
            lsbCache = new byte[MAX_CHAR_INDEX - MIN_CHAR_INDEX];
            reset(frc, font);
        }
        public void reset(FontRenderContext frc, Font font) {
            this.font = font;
            this.frc = frc;
            for (int counter = lsbCache.length - 1; counter >= 0; counter--) {
                lsbCache[counter] = UNSET;
            }
        }
        public int getLeftSideBearing(char aChar) {
            int index = aChar - MIN_CHAR_INDEX;
            assert (index >= 0 && index < (MAX_CHAR_INDEX - MIN_CHAR_INDEX));
            byte lsb = lsbCache[index];
            if (lsb == UNSET) {
                oneChar[0] = aChar;
                GlyphVector gv = font.createGlyphVector(frc, oneChar);
                lsb = (byte) gv.getGlyphPixelBounds(0, frc, 0f, 0f).x;
                if (lsb < 0) {
                    Object aaHint = frc.getAntiAliasingHint();
                    if (aaHint == VALUE_TEXT_ANTIALIAS_LCD_HRGB ||
                            aaHint == VALUE_TEXT_ANTIALIAS_LCD_HBGR) {
                        lsb++;
                    }
                }
                lsbCache[index] = lsb;
            }
            return lsb;
        }
        public boolean equals(Object entry) {
            if (entry == this) {
                return true;
            }
            if (!(entry instanceof LSBCacheEntry)) {
                return false;
            }
            LSBCacheEntry oEntry = (LSBCacheEntry) entry;
            return (font.equals(oEntry.font) &&
                    frc.equals(oEntry.frc));
        }
        public int hashCode() {
            int result = 17;
            if (font != null) {
                result = 37 * result + font.hashCode();
            }
            if (frc != null) {
                result = 37 * result + frc.hashCode();
            }
            return result;
        }
    }
   public static boolean canAccessSystemClipboard() {
       boolean canAccess = false;
       if (!GraphicsEnvironment.isHeadless()) {
           SecurityManager sm = System.getSecurityManager();
           if (sm == null) {
               canAccess = true;
           } else {
               try {
                   sm.checkSystemClipboardAccess();
                   canAccess = true;
               } catch (SecurityException e) {
               }
               if (canAccess && ! isTrustedContext()) {
                   canAccess = canCurrentEventAccessSystemClipboard(true);
               }
           }
       }
       return canAccess;
   }
    public static boolean canCurrentEventAccessSystemClipboard() {
        return  isTrustedContext()
            || canCurrentEventAccessSystemClipboard(false);
    }
    public static boolean canEventAccessSystemClipboard(AWTEvent e) {
        return isTrustedContext()
            || canEventAccessSystemClipboard(e, false);
    }
    private static synchronized boolean inputEvent_canAccessSystemClipboard(InputEvent ie) {
        if (inputEvent_CanAccessSystemClipboard_Field == null) {
            inputEvent_CanAccessSystemClipboard_Field =
                AccessController.doPrivileged(
                    new java.security.PrivilegedAction<Field>() {
                        public Field run() {
                            try {
                                Field field = InputEvent.class.
                                    getDeclaredField("canAccessSystemClipboard");
                                field.setAccessible(true);
                                return field;
                            } catch (SecurityException e) {
                            } catch (NoSuchFieldException e) {
                            }
                            return null;
                        }
                    });
        }
        if (inputEvent_CanAccessSystemClipboard_Field == null) {
            return false;
        }
        boolean ret = false;
        try {
            ret = inputEvent_CanAccessSystemClipboard_Field.
                getBoolean(ie);
        } catch(IllegalAccessException e) {
        }
        return ret;
    }
    private static boolean isAccessClipboardGesture(InputEvent ie) {
        boolean allowedGesture = false;
        if (ie instanceof KeyEvent) { 
            KeyEvent ke = (KeyEvent)ie;
            int keyCode = ke.getKeyCode();
            int keyModifiers = ke.getModifiers();
            switch(keyCode) {
            case KeyEvent.VK_C:
            case KeyEvent.VK_V:
            case KeyEvent.VK_X:
                allowedGesture = (keyModifiers == InputEvent.CTRL_MASK);
                break;
            case KeyEvent.VK_INSERT:
                allowedGesture = (keyModifiers == InputEvent.CTRL_MASK ||
                                  keyModifiers == InputEvent.SHIFT_MASK);
                break;
            case KeyEvent.VK_COPY:
            case KeyEvent.VK_PASTE:
            case KeyEvent.VK_CUT:
                allowedGesture = true;
                break;
            case KeyEvent.VK_DELETE:
                allowedGesture = ( keyModifiers == InputEvent.SHIFT_MASK);
                break;
            }
        }
        return allowedGesture;
    }
    private static boolean canEventAccessSystemClipboard(AWTEvent e,
                                                        boolean checkGesture) {
        if (EventQueue.isDispatchThread()) {
            if (e instanceof InputEvent
                && (! checkGesture || isAccessClipboardGesture((InputEvent)e))) {
                return inputEvent_canAccessSystemClipboard((InputEvent)e);
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    private static boolean canCurrentEventAccessSystemClipboard(boolean
                                                               checkGesture) {
        AWTEvent event = EventQueue.getCurrentEvent();
        return canEventAccessSystemClipboard(event, checkGesture);
    }
    private static boolean isTrustedContext() {
        return (System.getSecurityManager() == null)
            || (AppContext.getAppContext().
                get(UntrustedClipboardAccess) == null);
    }
    public static String displayPropertiesToCSS(Font font, Color fg) {
        StringBuffer rule = new StringBuffer("body {");
        if (font != null) {
            rule.append(" font-family: ");
            rule.append(font.getFamily());
            rule.append(" ; ");
            rule.append(" font-size: ");
            rule.append(font.getSize());
            rule.append("pt ;");
            if (font.isBold()) {
                rule.append(" font-weight: 700 ; ");
            }
            if (font.isItalic()) {
                rule.append(" font-style: italic ; ");
            }
        }
        if (fg != null) {
            rule.append(" color: #");
            if (fg.getRed() < 16) {
                rule.append('0');
            }
            rule.append(Integer.toHexString(fg.getRed()));
            if (fg.getGreen() < 16) {
                rule.append('0');
            }
            rule.append(Integer.toHexString(fg.getGreen()));
            if (fg.getBlue() < 16) {
                rule.append('0');
            }
            rule.append(Integer.toHexString(fg.getBlue()));
            rule.append(" ; ");
        }
        rule.append(" }");
        return rule.toString();
    }
    public static Object makeIcon(final Class<?> baseClass,
                                  final Class<?> rootClass,
                                  final String imageFile) {
        return new UIDefaults.LazyValue() {
            public Object createValue(UIDefaults table) {
                byte[] buffer =
                    java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedAction<byte[]>() {
                    public byte[] run() {
                        try {
                            InputStream resource = null;
                            Class<?> srchClass = baseClass;
                            while (srchClass != null) {
                                resource = srchClass.getResourceAsStream(imageFile);
                                if (resource != null || srchClass == rootClass) {
                                    break;
                                }
                                srchClass = srchClass.getSuperclass();
                            }
                            if (resource == null) {
                                return null;
                            }
                            BufferedInputStream in =
                                new BufferedInputStream(resource);
                            ByteArrayOutputStream out =
                                new ByteArrayOutputStream(1024);
                            byte[] buffer = new byte[1024];
                            int n;
                            while ((n = in.read(buffer)) > 0) {
                                out.write(buffer, 0, n);
                            }
                            in.close();
                            out.flush();
                            return out.toByteArray();
                        } catch (IOException ioe) {
                            System.err.println(ioe.toString());
                        }
                        return null;
                    }
                });
                if (buffer == null) {
                    return null;
                }
                if (buffer.length == 0) {
                    System.err.println("warning: " + imageFile +
                                       " is zero-length");
                    return null;
                }
                return new ImageIconUIResource(buffer);
            }
        };
    }
    public static boolean isLocalDisplay() {
        boolean isLocal;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge instanceof SunGraphicsEnvironment) {
            isLocal = ((SunGraphicsEnvironment) ge).isDisplayLocal();
        } else {
            isLocal = true;
        }
        return isLocal;
    }
    public static int getUIDefaultsInt(Object key) {
        return getUIDefaultsInt(key, 0);
    }
    public static int getUIDefaultsInt(Object key, Locale l) {
        return getUIDefaultsInt(key, l, 0);
    }
    public static int getUIDefaultsInt(Object key, int defaultValue) {
        return getUIDefaultsInt(key, null, defaultValue);
    }
    public static int getUIDefaultsInt(Object key, Locale l, int defaultValue) {
        Object value = UIManager.get(key, l);
        if (value instanceof Integer) {
            return ((Integer)value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String)value);
            } catch (NumberFormatException nfe) {}
        }
        return defaultValue;
    }
    public static Component compositeRequestFocus(Component component) {
        if (component instanceof Container) {
            Container container = (Container)component;
            if (container.isFocusCycleRoot()) {
                FocusTraversalPolicy policy = container.getFocusTraversalPolicy();
                Component comp = policy.getDefaultComponent(container);
                if (comp!=null) {
                    comp.requestFocus();
                    return comp;
                }
            }
            Container rootAncestor = container.getFocusCycleRootAncestor();
            if (rootAncestor!=null) {
                FocusTraversalPolicy policy = rootAncestor.getFocusTraversalPolicy();
                Component comp = policy.getComponentAfter(rootAncestor, container);
                if (comp!=null && SwingUtilities.isDescendingFrom(comp, container)) {
                    comp.requestFocus();
                    return comp;
                }
            }
        }
        if (component.isFocusable()) {
            component.requestFocus();
            return component;
        }
        return null;
    }
    public static boolean tabbedPaneChangeFocusTo(Component comp) {
        if (comp != null) {
            if (comp.isFocusTraversable()) {
                SwingUtilities2.compositeRequestFocus(comp);
                return true;
            } else if (comp instanceof JComponent
                       && ((JComponent)comp).requestDefaultFocus()) {
                 return true;
            }
        }
        return false;
    }
    public static <V> Future<V> submit(Callable<V> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        FutureTask<V> future = new FutureTask<V>(task);
        execute(future);
        return future;
    }
    public static <V> Future<V> submit(Runnable task, V result) {
        if (task == null) {
            throw new NullPointerException();
        }
        FutureTask<V> future = new FutureTask<V>(task, result);
        execute(future);
        return future;
    }
    private static void execute(Runnable command) {
        SwingUtilities.invokeLater(command);
    }
    public static void setSkipClickCount(Component comp, int count) {
        if (comp instanceof JTextComponent
                && ((JTextComponent) comp).getCaret() instanceof DefaultCaret) {
            ((JTextComponent) comp).putClientProperty(SKIP_CLICK_COUNT, count);
        }
    }
    public static int getAdjustedClickCount(JTextComponent comp, MouseEvent e) {
        int cc = e.getClickCount();
        if (cc == 1) {
            comp.putClientProperty(SKIP_CLICK_COUNT, null);
        } else {
            Integer sub = (Integer) comp.getClientProperty(SKIP_CLICK_COUNT);
            if (sub != null) {
                return cc - sub;
            }
        }
        return cc;
    }
    public enum Section {
        LEADING,
        MIDDLE,
        TRAILING
    }
    private static Section liesIn(Rectangle rect, Point p, boolean horizontal,
                                  boolean ltr, boolean three) {
        int p0;
        int pComp;
        int length;
        boolean forward;
        if (horizontal) {
            p0 = rect.x;
            pComp = p.x;
            length = rect.width;
            forward = ltr;
        } else {
            p0 = rect.y;
            pComp = p.y;
            length = rect.height;
            forward = true;
        }
        if (three) {
            int boundary = (length >= 30) ? 10 : length / 3;
            if (pComp < p0 + boundary) {
               return forward ? Section.LEADING : Section.TRAILING;
           } else if (pComp >= p0 + length - boundary) {
               return forward ? Section.TRAILING : Section.LEADING;
           }
           return Section.MIDDLE;
        } else {
            int middle = p0 + length / 2;
            if (forward) {
                return pComp >= middle ? Section.TRAILING : Section.LEADING;
            } else {
                return pComp < middle ? Section.TRAILING : Section.LEADING;
            }
        }
    }
    public static Section liesInHorizontal(Rectangle rect, Point p,
                                           boolean ltr, boolean three) {
        return liesIn(rect, p, true, ltr, three);
    }
    public static Section liesInVertical(Rectangle rect, Point p,
                                         boolean three) {
        return liesIn(rect, p, false, false, three);
    }
    public static int convertColumnIndexToModel(TableColumnModel cm,
                                                int viewColumnIndex) {
        if (viewColumnIndex < 0) {
            return viewColumnIndex;
        }
        return cm.getColumn(viewColumnIndex).getModelIndex();
    }
    public static int convertColumnIndexToView(TableColumnModel cm,
                                        int modelColumnIndex) {
        if (modelColumnIndex < 0) {
            return modelColumnIndex;
        }
        for (int column = 0; column < cm.getColumnCount(); column++) {
            if (cm.getColumn(column).getModelIndex() == modelColumnIndex) {
                return column;
            }
        }
        return -1;
    }
}
