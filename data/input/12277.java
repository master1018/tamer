public class MenuItemLayoutHelper {
    public static final StringUIClientPropertyKey MAX_ARROW_WIDTH =
                        new StringUIClientPropertyKey("maxArrowWidth");
    public static final StringUIClientPropertyKey MAX_CHECK_WIDTH =
                        new StringUIClientPropertyKey("maxCheckWidth");
    public static final StringUIClientPropertyKey MAX_ICON_WIDTH =
                        new StringUIClientPropertyKey("maxIconWidth");
    public static final StringUIClientPropertyKey MAX_TEXT_WIDTH =
                        new StringUIClientPropertyKey("maxTextWidth");
    public static final StringUIClientPropertyKey MAX_ACC_WIDTH =
                        new StringUIClientPropertyKey("maxAccWidth");
    public static final StringUIClientPropertyKey MAX_LABEL_WIDTH =
                        new StringUIClientPropertyKey("maxLabelWidth");
    private JMenuItem mi;
    private JComponent miParent;
    private Font font;
    private Font accFont;
    private FontMetrics fm;
    private FontMetrics accFm;
    private Icon icon;
    private Icon checkIcon;
    private Icon arrowIcon;
    private String text;
    private String accText;
    private boolean isColumnLayout;
    private boolean useCheckAndArrow;
    private boolean isLeftToRight;
    private boolean isTopLevelMenu;
    private View htmlView;
    private int verticalAlignment;
    private int horizontalAlignment;
    private int verticalTextPosition;
    private int horizontalTextPosition;
    private int gap;
    private int leadingGap;
    private int afterCheckIconGap;
    private int minTextOffset;
    private int leftTextExtraWidth;
    private Rectangle viewRect;
    private RectSize iconSize;
    private RectSize textSize;
    private RectSize accSize;
    private RectSize checkSize;
    private RectSize arrowSize;
    private RectSize labelSize;
    protected MenuItemLayoutHelper() {
    }
    public MenuItemLayoutHelper(JMenuItem mi, Icon checkIcon, Icon arrowIcon,
                      Rectangle viewRect, int gap, String accDelimiter,
                      boolean isLeftToRight, Font font, Font accFont,
                      boolean useCheckAndArrow, String propertyPrefix) {
        reset(mi, checkIcon, arrowIcon, viewRect, gap, accDelimiter,
              isLeftToRight, font, accFont, useCheckAndArrow, propertyPrefix);
    }
    protected void reset(JMenuItem mi, Icon checkIcon, Icon arrowIcon,
                      Rectangle viewRect, int gap, String accDelimiter,
                      boolean isLeftToRight, Font font, Font accFont,
                      boolean useCheckAndArrow, String propertyPrefix) {
        this.mi = mi;
        this.miParent = getMenuItemParent(mi);
        this.accText = getAccText(accDelimiter);
        this.verticalAlignment = mi.getVerticalAlignment();
        this.horizontalAlignment = mi.getHorizontalAlignment();
        this.verticalTextPosition = mi.getVerticalTextPosition();
        this.horizontalTextPosition = mi.getHorizontalTextPosition();
        this.useCheckAndArrow = useCheckAndArrow;
        this.font = font;
        this.accFont = accFont;
        this.fm = mi.getFontMetrics(font);
        this.accFm = mi.getFontMetrics(accFont);
        this.isLeftToRight = isLeftToRight;
        this.isColumnLayout = isColumnLayout(isLeftToRight,
                horizontalAlignment, horizontalTextPosition,
                verticalTextPosition);
        this.isTopLevelMenu = (this.miParent == null) ? true : false;
        this.checkIcon = checkIcon;
        this.icon = getIcon(propertyPrefix);
        this.arrowIcon = arrowIcon;
        this.text = mi.getText();
        this.gap = gap;
        this.afterCheckIconGap = getAfterCheckIconGap(propertyPrefix);
        this.minTextOffset = getMinTextOffset(propertyPrefix);
        this.htmlView = (View) mi.getClientProperty(BasicHTML.propertyKey);
        this.viewRect = viewRect;
        this.iconSize = new RectSize();
        this.textSize = new RectSize();
        this.accSize = new RectSize();
        this.checkSize = new RectSize();
        this.arrowSize = new RectSize();
        this.labelSize = new RectSize();
        calcExtraWidths();
        calcWidthsAndHeights();
        setOriginalWidths();
        calcMaxWidths();
        this.leadingGap = getLeadingGap(propertyPrefix);
        calcMaxTextOffset(viewRect);
    }
    private void calcExtraWidths() {
        leftTextExtraWidth = getLeftExtraWidth(text);
    }
    private int getLeftExtraWidth(String str) {
        int lsb = SwingUtilities2.getLeftSideBearing(mi, fm, str);
        if (lsb < 0) {
            return -lsb;
        } else {
            return 0;
        }
    }
    private void setOriginalWidths() {
        iconSize.origWidth = iconSize.width;
        textSize.origWidth = textSize.width;
        accSize.origWidth = accSize.width;
        checkSize.origWidth = checkSize.width;
        arrowSize.origWidth = arrowSize.width;
    }
    private String getAccText(String acceleratorDelimiter) {
        String accText = "";
        KeyStroke accelerator = mi.getAccelerator();
        if (accelerator != null) {
            int modifiers = accelerator.getModifiers();
            if (modifiers > 0) {
                accText = KeyEvent.getKeyModifiersText(modifiers);
                accText += acceleratorDelimiter;
            }
            int keyCode = accelerator.getKeyCode();
            if (keyCode != 0) {
                accText += KeyEvent.getKeyText(keyCode);
            } else {
                accText += accelerator.getKeyChar();
            }
        }
        return accText;
    }
    private Icon getIcon(String propertyPrefix) {
        Icon icon = null;
        MenuItemCheckIconFactory iconFactory =
                (MenuItemCheckIconFactory) UIManager.get(propertyPrefix
                        + ".checkIconFactory");
        if (!isColumnLayout || !useCheckAndArrow || iconFactory == null
                || !iconFactory.isCompatible(checkIcon, propertyPrefix)) {
            icon = mi.getIcon();
        }
        return icon;
    }
    private int getMinTextOffset(String propertyPrefix) {
        int minimumTextOffset = 0;
        Object minimumTextOffsetObject =
                UIManager.get(propertyPrefix + ".minimumTextOffset");
        if (minimumTextOffsetObject instanceof Integer) {
            minimumTextOffset = (Integer) minimumTextOffsetObject;
        }
        return minimumTextOffset;
    }
    private int getAfterCheckIconGap(String propertyPrefix) {
        int afterCheckIconGap = gap;
        Object afterCheckIconGapObject =
                UIManager.get(propertyPrefix + ".afterCheckIconGap");
        if (afterCheckIconGapObject instanceof Integer) {
            afterCheckIconGap = (Integer) afterCheckIconGapObject;
        }
        return afterCheckIconGap;
    }
    private int getLeadingGap(String propertyPrefix) {
        if (checkSize.getMaxWidth() > 0) {
            return getCheckOffset(propertyPrefix);
        } else {
            return gap; 
        }
    }
    private int getCheckOffset(String propertyPrefix) {
        int checkIconOffset = gap;
        Object checkIconOffsetObject =
                UIManager.get(propertyPrefix + ".checkIconOffset");
        if (checkIconOffsetObject instanceof Integer) {
            checkIconOffset = (Integer) checkIconOffsetObject;
        }
        return checkIconOffset;
    }
    protected void calcWidthsAndHeights() {
        if (icon != null) {
            iconSize.width = icon.getIconWidth();
            iconSize.height = icon.getIconHeight();
        }
        if (!accText.equals("")) {
            accSize.width = SwingUtilities2.stringWidth(mi, accFm, accText);
            accSize.height = accFm.getHeight();
        }
        if (text == null) {
            text = "";
        } else if (!text.equals("")) {
            if (htmlView != null) {
                textSize.width =
                        (int) htmlView.getPreferredSpan(View.X_AXIS);
                textSize.height =
                        (int) htmlView.getPreferredSpan(View.Y_AXIS);
            } else {
                textSize.width = SwingUtilities2.stringWidth(mi, fm, text);
                textSize.height = fm.getHeight();
            }
        }
        if (useCheckAndArrow) {
            if (checkIcon != null) {
                checkSize.width = checkIcon.getIconWidth();
                checkSize.height = checkIcon.getIconHeight();
            }
            if (arrowIcon != null) {
                arrowSize.width = arrowIcon.getIconWidth();
                arrowSize.height = arrowIcon.getIconHeight();
            }
        }
        if (isColumnLayout) {
            labelSize.width = iconSize.width + textSize.width + gap;
            labelSize.height = max(checkSize.height, iconSize.height,
                    textSize.height, accSize.height, arrowSize.height);
        } else {
            Rectangle textRect = new Rectangle();
            Rectangle iconRect = new Rectangle();
            SwingUtilities.layoutCompoundLabel(mi, fm, text, icon,
                    verticalAlignment, horizontalAlignment,
                    verticalTextPosition, horizontalTextPosition,
                    viewRect, iconRect, textRect, gap);
            textRect.width += leftTextExtraWidth;
            Rectangle labelRect = iconRect.union(textRect);
            labelSize.height = labelRect.height;
            labelSize.width = labelRect.width;
        }
    }
    protected void calcMaxWidths() {
        calcMaxWidth(checkSize, MAX_CHECK_WIDTH);
        calcMaxWidth(arrowSize, MAX_ARROW_WIDTH);
        calcMaxWidth(accSize, MAX_ACC_WIDTH);
        if (isColumnLayout) {
            calcMaxWidth(iconSize, MAX_ICON_WIDTH);
            calcMaxWidth(textSize, MAX_TEXT_WIDTH);
            int curGap = gap;
            if ((iconSize.getMaxWidth() == 0)
                    || (textSize.getMaxWidth() == 0)) {
                curGap = 0;
            }
            labelSize.maxWidth =
                    calcMaxValue(MAX_LABEL_WIDTH, iconSize.maxWidth
                            + textSize.maxWidth + curGap);
        } else {
            iconSize.maxWidth = getParentIntProperty(MAX_ICON_WIDTH);
            calcMaxWidth(labelSize, MAX_LABEL_WIDTH);
            int candidateTextWidth = labelSize.maxWidth - iconSize.maxWidth;
            if (iconSize.maxWidth > 0) {
                candidateTextWidth -= gap;
            }
            textSize.maxWidth = calcMaxValue(MAX_TEXT_WIDTH, candidateTextWidth);
        }
    }
    protected void calcMaxWidth(RectSize rs, Object key) {
        rs.maxWidth = calcMaxValue(key, rs.width);
    }
    protected int calcMaxValue(Object propertyName, int value) {
        int maxValue = getParentIntProperty(propertyName);
        if (value > maxValue) {
            if (miParent != null) {
                miParent.putClientProperty(propertyName, value);
            }
            return value;
        } else {
            return maxValue;
        }
    }
    protected int getParentIntProperty(Object propertyName) {
        Object value = null;
        if (miParent != null) {
            value = miParent.getClientProperty(propertyName);
        }
        if ((value == null) || !(value instanceof Integer)) {
            value = 0;
        }
        return (Integer) value;
    }
    public static boolean isColumnLayout(boolean isLeftToRight,
                                         JMenuItem mi) {
        assert(mi != null);
        return isColumnLayout(isLeftToRight, mi.getHorizontalAlignment(),
                mi.getHorizontalTextPosition(), mi.getVerticalTextPosition());
    }
    public static boolean isColumnLayout(boolean isLeftToRight,
                                         int horizontalAlignment,
                                         int horizontalTextPosition,
                                         int verticalTextPosition) {
        if (verticalTextPosition != SwingConstants.CENTER) {
            return false;
        }
        if (isLeftToRight) {
            if (horizontalAlignment != SwingConstants.LEADING
                    && horizontalAlignment != SwingConstants.LEFT) {
                return false;
            }
            if (horizontalTextPosition != SwingConstants.TRAILING
                    && horizontalTextPosition != SwingConstants.RIGHT) {
                return false;
            }
        } else {
            if (horizontalAlignment != SwingConstants.LEADING
                    && horizontalAlignment != SwingConstants.RIGHT) {
                return false;
            }
            if (horizontalTextPosition != SwingConstants.TRAILING
                    && horizontalTextPosition != SwingConstants.LEFT) {
                return false;
            }
        }
        return true;
    }
    private void calcMaxTextOffset(Rectangle viewRect) {
        if (!isColumnLayout || !isLeftToRight) {
            return;
        }
        int offset = viewRect.x + leadingGap + checkSize.maxWidth
                + afterCheckIconGap + iconSize.maxWidth + gap;
        if (checkSize.maxWidth == 0) {
            offset -= afterCheckIconGap;
        }
        if (iconSize.maxWidth == 0) {
            offset -= gap;
        }
        if (offset < minTextOffset) {
            offset = minTextOffset;
        }
        calcMaxValue(SwingUtilities2.BASICMENUITEMUI_MAX_TEXT_OFFSET, offset);
    }
    public LayoutResult layoutMenuItem() {
        LayoutResult lr = createLayoutResult();
        prepareForLayout(lr);
        if (isColumnLayout()) {
            if (isLeftToRight()) {
                doLTRColumnLayout(lr, getLTRColumnAlignment());
            } else {
                doRTLColumnLayout(lr, getRTLColumnAlignment());
            }
        } else {
            if (isLeftToRight()) {
                doLTRComplexLayout(lr, getLTRColumnAlignment());
            } else {
                doRTLComplexLayout(lr, getRTLColumnAlignment());
            }
        }
        alignAccCheckAndArrowVertically(lr);
        return lr;
    }
    private LayoutResult createLayoutResult() {
        return new LayoutResult(
                new Rectangle(iconSize.width, iconSize.height),
                new Rectangle(textSize.width, textSize.height),
                new Rectangle(accSize.width,  accSize.height),
                new Rectangle(checkSize.width, checkSize.height),
                new Rectangle(arrowSize.width, arrowSize.height),
                new Rectangle(labelSize.width, labelSize.height)
        );
    }
    public ColumnAlignment getLTRColumnAlignment() {
        return ColumnAlignment.LEFT_ALIGNMENT;
    }
    public ColumnAlignment getRTLColumnAlignment() {
        return ColumnAlignment.RIGHT_ALIGNMENT;
    }
    protected void prepareForLayout(LayoutResult lr) {
        lr.checkRect.width = checkSize.maxWidth;
        lr.accRect.width = accSize.maxWidth;
        lr.arrowRect.width = arrowSize.maxWidth;
    }
    private void alignAccCheckAndArrowVertically(LayoutResult lr) {
        lr.accRect.y = (int)(lr.labelRect.y
                + (float)lr.labelRect.height/2
                - (float)lr.accRect.height/2);
        fixVerticalAlignment(lr, lr.accRect);
        if (useCheckAndArrow) {
            lr.arrowRect.y = (int)(lr.labelRect.y
                    + (float)lr.labelRect.height/2
                    - (float)lr.arrowRect.height/2);
            lr.checkRect.y = (int)(lr.labelRect.y
                    + (float)lr.labelRect.height/2
                    - (float)lr.checkRect.height/2);
            fixVerticalAlignment(lr, lr.arrowRect);
            fixVerticalAlignment(lr, lr.checkRect);
        }
    }
    private void fixVerticalAlignment(LayoutResult lr, Rectangle r) {
        int delta = 0;
        if (r.y < viewRect.y) {
            delta = viewRect.y - r.y;
        } else if (r.y + r.height > viewRect.y + viewRect.height) {
            delta = viewRect.y + viewRect.height - r.y - r.height;
        }
        if (delta != 0) {
            lr.checkRect.y += delta;
            lr.iconRect.y += delta;
            lr.textRect.y += delta;
            lr.accRect.y += delta;
            lr.arrowRect.y += delta;
            lr.labelRect.y += delta;
        }
    }
    private void doLTRColumnLayout(LayoutResult lr, ColumnAlignment alignment) {
        lr.iconRect.width = iconSize.maxWidth;
        lr.textRect.width = textSize.maxWidth;
        calcXPositionsLTR(viewRect.x, leadingGap, gap, lr.checkRect,
                lr.iconRect, lr.textRect);
        if (lr.checkRect.width > 0) { 
            lr.iconRect.x += afterCheckIconGap - gap;
            lr.textRect.x += afterCheckIconGap - gap;
        }
        calcXPositionsRTL(viewRect.x + viewRect.width, leadingGap, gap,
                lr.arrowRect, lr.accRect);
        int textOffset = lr.textRect.x - viewRect.x;
        if (!isTopLevelMenu && (textOffset < minTextOffset)) {
            lr.textRect.x += minTextOffset - textOffset;
        }
        alignRects(lr, alignment);
        calcTextAndIconYPositions(lr);
        lr.setLabelRect(lr.textRect.union(lr.iconRect));
    }
    private void doLTRComplexLayout(LayoutResult lr, ColumnAlignment alignment) {
        lr.labelRect.width = labelSize.maxWidth;
        calcXPositionsLTR(viewRect.x, leadingGap, gap, lr.checkRect,
                lr.labelRect);
        if (lr.checkRect.width > 0) { 
            lr.labelRect.x += afterCheckIconGap - gap;
        }
        calcXPositionsRTL(viewRect.x + viewRect.width,
                leadingGap, gap, lr.arrowRect, lr.accRect);
        int labelOffset = lr.labelRect.x - viewRect.x;
        if (!isTopLevelMenu && (labelOffset < minTextOffset)) {
            lr.labelRect.x += minTextOffset - labelOffset;
        }
        alignRects(lr, alignment);
        calcLabelYPosition(lr);
        layoutIconAndTextInLabelRect(lr);
    }
    private void doRTLColumnLayout(LayoutResult lr, ColumnAlignment alignment) {
        lr.iconRect.width = iconSize.maxWidth;
        lr.textRect.width = textSize.maxWidth;
        calcXPositionsRTL(viewRect.x + viewRect.width, leadingGap, gap,
                lr.checkRect, lr.iconRect, lr.textRect);
        if (lr.checkRect.width > 0) { 
            lr.iconRect.x -= afterCheckIconGap - gap;
            lr.textRect.x -= afterCheckIconGap - gap;
        }
        calcXPositionsLTR(viewRect.x, leadingGap, gap, lr.arrowRect,
                lr.accRect);
        int textOffset = (viewRect.x + viewRect.width)
                       - (lr.textRect.x + lr.textRect.width);
        if (!isTopLevelMenu && (textOffset < minTextOffset)) {
            lr.textRect.x -= minTextOffset - textOffset;
        }
        alignRects(lr, alignment);
        calcTextAndIconYPositions(lr);
        lr.setLabelRect(lr.textRect.union(lr.iconRect));
    }
    private void doRTLComplexLayout(LayoutResult lr, ColumnAlignment alignment) {
        lr.labelRect.width = labelSize.maxWidth;
        calcXPositionsRTL(viewRect.x + viewRect.width, leadingGap, gap,
                lr.checkRect, lr.labelRect);
        if (lr.checkRect.width > 0) { 
            lr.labelRect.x -= afterCheckIconGap - gap;
        }
        calcXPositionsLTR(viewRect.x, leadingGap, gap, lr.arrowRect, lr.accRect);
        int labelOffset = (viewRect.x + viewRect.width)
                        - (lr.labelRect.x + lr.labelRect.width);
        if (!isTopLevelMenu && (labelOffset < minTextOffset)) {
            lr.labelRect.x -= minTextOffset - labelOffset;
        }
        alignRects(lr, alignment);
        calcLabelYPosition(lr);
        layoutIconAndTextInLabelRect(lr);
    }
    private void alignRects(LayoutResult lr, ColumnAlignment alignment) {
        alignRect(lr.checkRect, alignment.getCheckAlignment(),
                  checkSize.getOrigWidth());
        alignRect(lr.iconRect, alignment.getIconAlignment(),
                  iconSize.getOrigWidth());
        alignRect(lr.textRect, alignment.getTextAlignment(),
                  textSize.getOrigWidth());
        alignRect(lr.accRect, alignment.getAccAlignment(),
                  accSize.getOrigWidth());
        alignRect(lr.arrowRect, alignment.getArrowAlignment(),
                  arrowSize.getOrigWidth());
    }
    private void alignRect(Rectangle rect, int alignment, int origWidth) {
        if (alignment == SwingConstants.RIGHT) {
            rect.x = rect.x + rect.width - origWidth;
        }
        rect.width = origWidth;
    }
    protected void layoutIconAndTextInLabelRect(LayoutResult lr) {
        lr.setTextRect(new Rectangle());
        lr.setIconRect(new Rectangle());
        SwingUtilities.layoutCompoundLabel(
                mi, fm, text,icon, verticalAlignment, horizontalAlignment,
                verticalTextPosition, horizontalTextPosition, lr.labelRect,
                lr.iconRect, lr.textRect, gap);
    }
    private void calcXPositionsLTR(int startXPos, int leadingGap,
                                   int gap, Rectangle... rects) {
        int curXPos = startXPos + leadingGap;
        for (Rectangle rect : rects) {
            rect.x = curXPos;
            if (rect.width > 0) {
                curXPos += rect.width + gap;
            }
        }
    }
    private void calcXPositionsRTL(int startXPos, int leadingGap,
                                   int gap, Rectangle... rects) {
        int curXPos = startXPos - leadingGap;
        for (Rectangle rect : rects) {
            rect.x = curXPos - rect.width;
            if (rect.width > 0) {
                curXPos -= rect.width + gap;
            }
        }
    }
    private void calcTextAndIconYPositions(LayoutResult lr) {
        if (verticalAlignment == SwingUtilities.TOP) {
            lr.textRect.y  = (int)(viewRect.y
                    + (float)lr.labelRect.height/2
                    - (float)lr.textRect.height/2);
            lr.iconRect.y  = (int)(viewRect.y
                    + (float)lr.labelRect.height/2
                    - (float)lr.iconRect.height/2);
        } else if (verticalAlignment == SwingUtilities.CENTER) {
            lr.textRect.y = (int)(viewRect.y
                    + (float)viewRect.height/2
                    - (float)lr.textRect.height/2);
            lr.iconRect.y = (int)(viewRect.y
                    + (float)viewRect.height/2
                    - (float)lr.iconRect.height/2);
        }
        else if (verticalAlignment == SwingUtilities.BOTTOM) {
            lr.textRect.y = (int)(viewRect.y
                    + viewRect.height
                    - (float)lr.labelRect.height/2
                    - (float)lr.textRect.height/2);
            lr.iconRect.y = (int)(viewRect.y
                    + viewRect.height
                    - (float)lr.labelRect.height/2
                    - (float)lr.iconRect.height/2);
        }
    }
    private void calcLabelYPosition(LayoutResult lr) {
        if (verticalAlignment == SwingUtilities.TOP) {
            lr.labelRect.y  = viewRect.y;
        } else if (verticalAlignment == SwingUtilities.CENTER) {
            lr.labelRect.y = (int)(viewRect.y
                    + (float)viewRect.height/2
                    - (float)lr.labelRect.height/2);
        } else if (verticalAlignment == SwingUtilities.BOTTOM) {
            lr.labelRect.y  = viewRect.y + viewRect.height
                    - lr.labelRect.height;
        }
    }
    public static JComponent getMenuItemParent(JMenuItem menuItem) {
        Container parent = menuItem.getParent();
        if ((parent instanceof JComponent) &&
             (!(menuItem instanceof JMenu) ||
               !((JMenu)menuItem).isTopLevelMenu())) {
            return (JComponent) parent;
        } else {
            return null;
        }
    }
    public static void clearUsedParentClientProperties(JMenuItem menuItem) {
        clearUsedClientProperties(getMenuItemParent(menuItem));
    }
    public static void clearUsedClientProperties(JComponent c) {
        if (c != null) {
            c.putClientProperty(MAX_ARROW_WIDTH, null);
            c.putClientProperty(MAX_CHECK_WIDTH, null);
            c.putClientProperty(MAX_ACC_WIDTH, null);
            c.putClientProperty(MAX_TEXT_WIDTH, null);
            c.putClientProperty(MAX_ICON_WIDTH, null);
            c.putClientProperty(MAX_LABEL_WIDTH, null);
            c.putClientProperty(BASICMENUITEMUI_MAX_TEXT_OFFSET, null);
        }
    }
    public static int max(int... values) {
        int maxValue = Integer.MIN_VALUE;
        for (int i : values) {
            if (i > maxValue) {
                maxValue = i;
            }
        }
        return maxValue;
    }
    public static Rectangle createMaxRect() {
        return new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    public static void addMaxWidth(RectSize size, int gap, Dimension result) {
        if (size.maxWidth > 0) {
            result.width += size.maxWidth + gap;
        }
    }
    public static void addWidth(int width, int gap, Dimension result) {
        if (width > 0) {
            result.width += width + gap;
        }
    }
    public JMenuItem getMenuItem() {
        return mi;
    }
    public JComponent getMenuItemParent() {
        return miParent;
    }
    public Font getFont() {
        return font;
    }
    public Font getAccFont() {
        return accFont;
    }
    public FontMetrics getFontMetrics() {
        return fm;
    }
    public FontMetrics getAccFontMetrics() {
        return accFm;
    }
    public Icon getIcon() {
        return icon;
    }
    public Icon getCheckIcon() {
        return checkIcon;
    }
    public Icon getArrowIcon() {
        return arrowIcon;
    }
    public String getText() {
        return text;
    }
    public String getAccText() {
        return accText;
    }
    public boolean isColumnLayout() {
        return isColumnLayout;
    }
    public boolean useCheckAndArrow() {
        return useCheckAndArrow;
    }
    public boolean isLeftToRight() {
        return isLeftToRight;
    }
    public boolean isTopLevelMenu() {
        return isTopLevelMenu;
    }
    public View getHtmlView() {
        return htmlView;
    }
    public int getVerticalAlignment() {
        return verticalAlignment;
    }
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }
    public int getVerticalTextPosition() {
        return verticalTextPosition;
    }
    public int getHorizontalTextPosition() {
        return horizontalTextPosition;
    }
    public int getGap() {
        return gap;
    }
    public int getLeadingGap() {
        return leadingGap;
    }
    public int getAfterCheckIconGap() {
        return afterCheckIconGap;
    }
    public int getMinTextOffset() {
        return minTextOffset;
    }
    public Rectangle getViewRect() {
        return viewRect;
    }
    public RectSize getIconSize() {
        return iconSize;
    }
    public RectSize getTextSize() {
        return textSize;
    }
    public RectSize getAccSize() {
        return accSize;
    }
    public RectSize getCheckSize() {
        return checkSize;
    }
    public RectSize getArrowSize() {
        return arrowSize;
    }
    public RectSize getLabelSize() {
        return labelSize;
    }
    protected void setMenuItem(JMenuItem mi) {
        this.mi = mi;
    }
    protected void setMenuItemParent(JComponent miParent) {
        this.miParent = miParent;
    }
    protected void setFont(Font font) {
        this.font = font;
    }
    protected void setAccFont(Font accFont) {
        this.accFont = accFont;
    }
    protected void setFontMetrics(FontMetrics fm) {
        this.fm = fm;
    }
    protected void setAccFontMetrics(FontMetrics accFm) {
        this.accFm = accFm;
    }
    protected void setIcon(Icon icon) {
        this.icon = icon;
    }
    protected void setCheckIcon(Icon checkIcon) {
        this.checkIcon = checkIcon;
    }
    protected void setArrowIcon(Icon arrowIcon) {
        this.arrowIcon = arrowIcon;
    }
    protected void setText(String text) {
        this.text = text;
    }
    protected void setAccText(String accText) {
        this.accText = accText;
    }
    protected void setColumnLayout(boolean columnLayout) {
        isColumnLayout = columnLayout;
    }
    protected void setUseCheckAndArrow(boolean useCheckAndArrow) {
        this.useCheckAndArrow = useCheckAndArrow;
    }
    protected void setLeftToRight(boolean leftToRight) {
        isLeftToRight = leftToRight;
    }
    protected void setTopLevelMenu(boolean topLevelMenu) {
        isTopLevelMenu = topLevelMenu;
    }
    protected void setHtmlView(View htmlView) {
        this.htmlView = htmlView;
    }
    protected void setVerticalAlignment(int verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }
    protected void setHorizontalAlignment(int horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }
    protected void setVerticalTextPosition(int verticalTextPosition) {
        this.verticalTextPosition = verticalTextPosition;
    }
    protected void setHorizontalTextPosition(int horizontalTextPosition) {
        this.horizontalTextPosition = horizontalTextPosition;
    }
    protected void setGap(int gap) {
        this.gap = gap;
    }
    protected void setLeadingGap(int leadingGap) {
        this.leadingGap = leadingGap;
    }
    protected void setAfterCheckIconGap(int afterCheckIconGap) {
        this.afterCheckIconGap = afterCheckIconGap;
    }
    protected void setMinTextOffset(int minTextOffset) {
        this.minTextOffset = minTextOffset;
    }
    protected void setViewRect(Rectangle viewRect) {
        this.viewRect = viewRect;
    }
    protected void setIconSize(RectSize iconSize) {
        this.iconSize = iconSize;
    }
    protected void setTextSize(RectSize textSize) {
        this.textSize = textSize;
    }
    protected void setAccSize(RectSize accSize) {
        this.accSize = accSize;
    }
    protected void setCheckSize(RectSize checkSize) {
        this.checkSize = checkSize;
    }
    protected void setArrowSize(RectSize arrowSize) {
        this.arrowSize = arrowSize;
    }
    protected void setLabelSize(RectSize labelSize) {
        this.labelSize = labelSize;
    }
    public int getLeftTextExtraWidth() {
        return leftTextExtraWidth;
    }
    public static boolean useCheckAndArrow(JMenuItem menuItem) {
        boolean b = true;
        if ((menuItem instanceof JMenu) &&
                (((JMenu) menuItem).isTopLevelMenu())) {
            b = false;
        }
        return b;
    }
    public static class LayoutResult {
        private Rectangle iconRect;
        private Rectangle textRect;
        private Rectangle accRect;
        private Rectangle checkRect;
        private Rectangle arrowRect;
        private Rectangle labelRect;
        public LayoutResult() {
            iconRect = new Rectangle();
            textRect = new Rectangle();
            accRect = new Rectangle();
            checkRect = new Rectangle();
            arrowRect = new Rectangle();
            labelRect = new Rectangle();
        }
        public LayoutResult(Rectangle iconRect, Rectangle textRect,
                            Rectangle accRect, Rectangle checkRect,
                            Rectangle arrowRect, Rectangle labelRect) {
            this.iconRect = iconRect;
            this.textRect = textRect;
            this.accRect = accRect;
            this.checkRect = checkRect;
            this.arrowRect = arrowRect;
            this.labelRect = labelRect;
        }
        public Rectangle getIconRect() {
            return iconRect;
        }
        public void setIconRect(Rectangle iconRect) {
            this.iconRect = iconRect;
        }
        public Rectangle getTextRect() {
            return textRect;
        }
        public void setTextRect(Rectangle textRect) {
            this.textRect = textRect;
        }
        public Rectangle getAccRect() {
            return accRect;
        }
        public void setAccRect(Rectangle accRect) {
            this.accRect = accRect;
        }
        public Rectangle getCheckRect() {
            return checkRect;
        }
        public void setCheckRect(Rectangle checkRect) {
            this.checkRect = checkRect;
        }
        public Rectangle getArrowRect() {
            return arrowRect;
        }
        public void setArrowRect(Rectangle arrowRect) {
            this.arrowRect = arrowRect;
        }
        public Rectangle getLabelRect() {
            return labelRect;
        }
        public void setLabelRect(Rectangle labelRect) {
            this.labelRect = labelRect;
        }
        public Map<String, Rectangle> getAllRects() {
            Map<String, Rectangle> result = new HashMap<String, Rectangle>();
            result.put("checkRect", checkRect);
            result.put("iconRect", iconRect);
            result.put("textRect", textRect);
            result.put("accRect", accRect);
            result.put("arrowRect", arrowRect);
            result.put("labelRect", labelRect);
            return result;
        }
    }
    public static class ColumnAlignment {
        private int checkAlignment;
        private int iconAlignment;
        private int textAlignment;
        private int accAlignment;
        private int arrowAlignment;
        public static final ColumnAlignment LEFT_ALIGNMENT =
                new ColumnAlignment(
                        SwingConstants.LEFT,
                        SwingConstants.LEFT,
                        SwingConstants.LEFT,
                        SwingConstants.LEFT,
                        SwingConstants.LEFT
                );
        public static final ColumnAlignment RIGHT_ALIGNMENT =
                new ColumnAlignment(
                        SwingConstants.RIGHT,
                        SwingConstants.RIGHT,
                        SwingConstants.RIGHT,
                        SwingConstants.RIGHT,
                        SwingConstants.RIGHT
                );
        public ColumnAlignment(int checkAlignment, int iconAlignment,
                               int textAlignment, int accAlignment,
                               int arrowAlignment) {
            this.checkAlignment = checkAlignment;
            this.iconAlignment = iconAlignment;
            this.textAlignment = textAlignment;
            this.accAlignment = accAlignment;
            this.arrowAlignment = arrowAlignment;
        }
        public int getCheckAlignment() {
            return checkAlignment;
        }
        public int getIconAlignment() {
            return iconAlignment;
        }
        public int getTextAlignment() {
            return textAlignment;
        }
        public int getAccAlignment() {
            return accAlignment;
        }
        public int getArrowAlignment() {
            return arrowAlignment;
        }
    }
    public static class RectSize {
        private int width;
        private int height;
        private int origWidth;
        private int maxWidth;
        public RectSize() {
        }
        public RectSize(int width, int height, int origWidth, int maxWidth) {
            this.width = width;
            this.height = height;
            this.origWidth = origWidth;
            this.maxWidth = maxWidth;
        }
        public int getWidth() {
            return width;
        }
        public int getHeight() {
            return height;
        }
        public int getOrigWidth() {
            return origWidth;
        }
        public int getMaxWidth() {
            return maxWidth;
        }
        public void setWidth(int width) {
            this.width = width;
        }
        public void setHeight(int height) {
            this.height = height;
        }
        public void setOrigWidth(int origWidth) {
            this.origWidth = origWidth;
        }
        public void setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
        }
        public String toString() {
            return "[w=" + width + ",h=" + height + ",ow="
                    + origWidth + ",mw=" + maxWidth + "]";
        }
    }
}
